package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherRequestDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherResponseDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherUpdateRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.mappers.TeacherMapper;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final TeacherMapper teacherMapper;

    public TeacherService(TeacherRepository teacherRepository,
                          SectionRepository sectionRepository,
                          TeacherMapper teacherMapper) {
        this.teacherRepository = teacherRepository;
        this.sectionRepository = sectionRepository;
        this.teacherMapper = teacherMapper;
    }

    private void validateFields(TeacherRequestDTO dto){
        if (dto.teacherRun() == null || dto.teacherRun().isBlank()){
            throw new IllegalArgumentException("The teacher RUN is required.");
        }
        validateProfile(dto.teacherFullname(), dto.teacherEmail(), dto.teacherProtitle(), dto.teacherDegree());
    }

    private void validateUpdateFields(TeacherUpdateRequestDTO dto){
        validateProfile(dto.teacherFullname(), dto.teacherEmail(), dto.teacherProtitle(), dto.teacherDegree());
    }

    private void validateProfile(String fullname, String email, String protitle, String degree){
        if (fullname == null || fullname.isBlank()){
            throw new IllegalArgumentException("The teacher full name is required.");
        }
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("The teacher email is required.");
        }
        if (protitle == null || protitle.isBlank()){
            throw new IllegalArgumentException("The teacher professional title is required.");
        }
        if (degree == null || degree.isBlank()){
            throw new IllegalArgumentException("The teacher academic degree is required.");
        }
    }

    private TeacherEntity findTeacherOrThrow(Long id){
        return teacherRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + id));
    }

    public TeacherResponseDTO create(TeacherRequestDTO dto){
        validateFields(dto);

        if (teacherRepository.existsByTeacherRun(dto.teacherRun())){
            throw new IllegalArgumentException("A teacher with this RUN already exists: " + dto.teacherRun());
        }
        if (teacherRepository.existsByTeacherEmail(dto.teacherEmail())){
            throw new IllegalArgumentException("A teacher with this email already exists: " + dto.teacherEmail());
        }

        TeacherEntity teacher = teacherMapper.toEntity(dto);
        teacher.setTeacherStatus(TeacherEntity.TeacherStatus.ACTIVE);

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public List<TeacherResponseDTO> getAll(){
        return teacherMapper.toResponseList(teacherRepository.findAll());
    }

    public TeacherResponseDTO getById(Long id){
        return teacherMapper.toResponse(findTeacherOrThrow(id));
    }

    public TeacherResponseDTO update(Long id, TeacherUpdateRequestDTO dto){
        validateUpdateFields(dto);
        TeacherEntity teacher = findTeacherOrThrow(id);

        boolean emailChanged = !teacher.getTeacherEmail().equals(dto.teacherEmail());
        if (emailChanged && teacherRepository.existsByTeacherEmail(dto.teacherEmail())){
            throw new IllegalArgumentException("A teacher with this email already exists: " + dto.teacherEmail());
        }

        teacherMapper.updateEntity(teacher, dto);

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public TeacherResponseDTO changeStatus(Long id, TeacherEntity.TeacherStatus newStatus){
        TeacherEntity teacher = findTeacherOrThrow(id);

        if (newStatus == TeacherEntity.TeacherStatus.INACTIVE && sectionRepository.existsByTeacherAndAcademicPeriod_AcademicPeriodStatus(teacher, AcademicPeriodEntity.AcademicPeriodStatus.OPEN)){
            throw new IllegalStateException("Cannot deactivate: the teacher has sections in an open academic period.");
        }

        teacher.setTeacherStatus(newStatus);

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public void delete(Long id){
        TeacherEntity teacher = findTeacherOrThrow(id);

        if (sectionRepository.existsByTeacher(teacher)){
            throw new IllegalStateException("Cannot delete: the teacher has associated sections.");
        }

        teacherRepository.delete(teacher);
    }
}
