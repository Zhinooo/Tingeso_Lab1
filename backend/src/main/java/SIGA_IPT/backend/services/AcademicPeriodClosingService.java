package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.mappers.AcademicPeriodMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AcademicPeriodClosingService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodMapper academicPeriodMapper;

    public AcademicPeriodClosingService(AcademicPeriodRepository academicPeriodRepository,
                                        EnrollmentRepository enrollmentRepository,
                                        CourseRepository courseRepository,
                                        StudentRepository studentRepository,
                                        AcademicPeriodMapper academicPeriodMapper){
        this.academicPeriodRepository = academicPeriodRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.academicPeriodMapper = academicPeriodMapper;
    }

    private AcademicPeriodEntity findAcademicPeriodOrThrow(Long id){
        return academicPeriodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Academic period not found: " + id));
    }

    private boolean isApproved(EnrollmentEntity enrollment){
        return enrollment.getFinalGrade() >= EnrollmentEntity.PASSING_GRADE;
    }

    private Long courseIdOf(EnrollmentEntity enrollment){
        return enrollment.getCourse().getCourseId();
    }

    private void validateAllGradesRegistered(List<EnrollmentEntity> enrollments){
        List<String> incompleteCourses = enrollments.stream()
                .filter(enrollment -> enrollment.getFinalGrade() == null)
                .map(enrollment -> enrollment.getCourse().getCourseCode())
                .distinct()
                .sorted()
                .toList();

        if (!incompleteCourses.isEmpty()){
            throw new IllegalStateException("Cannot close: these sections have pending final grades: "
                    + String.join(", ", incompleteCourses));
        }
    }

    private void updateStudentStatus(StudentEntity student, List<EnrollmentEntity> periodEnrollments){
        if (student.getStudentStatus() == StudentEntity.StudentStatus.GRADUATED
            || student.getStudentStatus() == StudentEntity.StudentStatus.EXPELLED){
            return;
        }

        List<EnrollmentEntity> history = enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(student, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);
        Set<Long> previouslyFailedCourseIds = history.stream()
                .filter(enrollment -> !isApproved(enrollment))
                .map(this::courseIdOf)
                .collect(Collectors.toSet());
        Set<Long> approvedCourseIds = history.stream()
                .filter(this::isApproved)
                .map(this::courseIdOf)
                .collect(Collectors.toSet());

        boolean failedTwice = false;
        for (EnrollmentEntity enrollment : periodEnrollments){
            if (isApproved(enrollment)){
                approvedCourseIds.add(courseIdOf(enrollment));
            } else if (previouslyFailedCourseIds.contains(courseIdOf(enrollment))){
                failedTwice = true;
            }
        }

        if (failedTwice){
            student.setStudentStatus(StudentEntity.StudentStatus.EXPELLED);
            studentRepository.save(student);
            return;
        }

        Set<Long> planCourseIds = courseRepository.findByStudyplan(student.getStudyplan()).stream()
                .map(CourseEntity::getCourseId)
                .collect(Collectors.toSet());

        if (!planCourseIds.isEmpty() && approvedCourseIds.containsAll(planCourseIds)){
            student.setStudentStatus(StudentEntity.StudentStatus.GRADUATED);
            studentRepository.save(student);
        }
    }

    public AcademicPeriodResponseDTO close(Long id){
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(id);

        if (academicPeriod.getAcademicPeriodStatus() != AcademicPeriodEntity.AcademicPeriodStatus.OPEN){
            throw new IllegalStateException("The academic period is already closed.");
        }

        List<EnrollmentEntity> enrollments = enrollmentRepository.findByAcademicPeriod(academicPeriod);
        validateAllGradesRegistered(enrollments);

        enrollments.stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getStudent().getStudentId()))
                .values()
                .forEach(studentEnrollments -> updateStudentStatus(studentEnrollments.get(0).getStudent(), studentEnrollments));

        academicPeriod.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        return academicPeriodMapper.toResponse(academicPeriodRepository.save(academicPeriod));
    }
}
