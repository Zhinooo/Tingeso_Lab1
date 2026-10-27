package SIGA_IPT.backend.seed;

import SIGA_IPT.backend.dtos.CareerDTOs.CareerRequestDTO;
import SIGA_IPT.backend.dtos.CourseDTOs.CourseRequestDTO;
import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanRequestDTO;
import SIGA_IPT.backend.repositories.CareerRepository;
import SIGA_IPT.backend.services.CareerService;
import SIGA_IPT.backend.services.CourseService;
import SIGA_IPT.backend.services.StudyplanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);
    private static final String CAREERS_FILE = "seed/careers.csv";
    private static final String COURSES_FILE = "seed/courses.csv";
    private static final String SEPARATOR = ";";

    private final CareerRepository careerRepository;
    private final CareerService careerService;
    private final StudyplanService studyplanService;
    private final CourseService courseService;

    public SeedDataRunner(CareerRepository careerRepository,
                          CareerService careerService,
                          StudyplanService studyplanService,
                          CourseService courseService){
        this.careerRepository = careerRepository;
        this.careerService = careerService;
        this.studyplanService = studyplanService;
        this.courseService = courseService;
    }

    private List<String[]> readRows(String file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(file).getInputStream(), StandardCharsets.UTF_8))){
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(SEPARATOR, -1))
                    .toList();
        }
    }

    private List<String> splitPrerequisites(String value){
        if (value.isBlank()){
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).toList();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        Map<String, Long> planIdsByCareer = new LinkedHashMap<>();
        for (String[] career : readRows(CAREERS_FILE)){
            String careerCode = career[0];
            if (careerRepository.existsByCareerCode(careerCode)){
                log.info("Seed data: career {} already exists, skipping it.", careerCode);
                continue;
            }
            careerService.create(new CareerRequestDTO(careerCode, career[1], career[2]));
            Long planId = studyplanService.create(new StudyplanRequestDTO(career[3], careerCode, true)).studyplanId();
            planIdsByCareer.put(careerCode, planId);
        }

        int createdCourses = 0;
        for (String[] course : readRows(COURSES_FILE)){
            Long planId = planIdsByCareer.get(course[0]);
            if (planId == null){
                continue;
            }
            courseService.create(new CourseRequestDTO(
                    course[2],
                    course[3],
                    Integer.valueOf(course[1]),
                    Integer.valueOf(course[4]),
                    Integer.valueOf(course[5]),
                    Integer.valueOf(course[6]),
                    Integer.valueOf(course[7]),
                    planId,
                    splitPrerequisites(course[8])));
            createdCourses++;
        }
        log.info("Seed data loaded: {} careers and {} courses.", planIdsByCareer.size(), createdCourses);
    }
}
