package edu.univ.erp.api.instructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.api.types.SectionStatsDto;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.StudentEnrollmentRow;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.ServiceRegistry;

public class InstructorApi {
    private static final Logger logger = LoggerFactory.getLogger(InstructorApi.class);
    private final InstructorService instructorService;
    private final CourseDao courseDao;

    public InstructorApi() {
        this.instructorService = ServiceRegistry.getInstructorService();
        // This DAO is used only for enrichment
        this.courseDao = new CourseDao();
    }

    public ApiResponse<List<SectionRow>> getMySections() {
        try {
            List<Section> sections = instructorService.getMySections();
            List<SectionRow> sectionRows = new ArrayList<>();
            
            for (Section section : sections) {
                SectionRow row = toSectionRow(section);
                sectionRows.add(row);
            }
            
            return ApiResponse.ok(sectionRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting instructor sections", ex);
            return ApiResponse.error("Error getting sections: " + ex.getMessage());
        }
    }

    public ApiResponse<List<Section>> getMySectionsAsDomain() {
        try {
            List<Section> sections = instructorService.getMySections();
            return ApiResponse.ok(sections);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting instructor sections", ex);
            return ApiResponse.error("Error getting sections: " + ex.getMessage());
        }
    }

    public ApiResponse<List<StudentEnrollmentRow>> getSectionRoster(int sectionId) {
        try {
            List<StudentEnrollmentRow> roster = instructorService.getSectionRoster(sectionId);
            return ApiResponse.ok(roster);
        } catch (AccessDeniedException ex) {
            // Preserve existing error messages like "Not your section."
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting section roster", ex);
            return ApiResponse.error("Error getting roster: " + ex.getMessage());
        }
    }

    public ApiResponse<List<String>> getComponentsForSection(int sectionId) {
        try {
            List<String> components = instructorService.getComponentsForSection(sectionId);
            return ApiResponse.ok(components);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting components", ex);
            return ApiResponse.error("Error getting components: " + ex.getMessage());
        }
    }

    public ApiResponse<List<String>> getOrCreateDefaultComponents(int sectionId) {
        try {
            List<String> components = instructorService.getOrCreateDefaultComponents(sectionId);
            return ApiResponse.ok(components);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting/creating default components", ex);
            return ApiResponse.error("Error getting components: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> addComponentToSection(int sectionId, String componentName) {
        try {
            instructorService.addComponentToSection(sectionId, componentName);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            // Preserve existing error messages like "Cannot change anything- Maintenance mode is enabled"
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error adding component", ex);
            return ApiResponse.error("Error adding component: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> removeComponentFromSection(int sectionId, String componentName) {
        try {
            instructorService.removeComponentFromSection(sectionId, componentName);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error removing component", ex);
            return ApiResponse.error("Error removing component: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> renameComponentForSection(int sectionId, String oldName, String newName) {
        try {
            instructorService.renameComponentForSection(sectionId, oldName, newName);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error renaming component", ex);
            return ApiResponse.error("Error renaming component: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> updateComponentScores(int sectionId, String component, Map<Integer, Double> enrollmentIdToScore) {
        try {
            instructorService.updateComponentScores(sectionId, component, enrollmentIdToScore);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error updating component scores", ex);
            return ApiResponse.error("Error updating scores: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> computeFinalGrades(int sectionId, Map<String, Double> componentToWeightPercent) {
        try {
            instructorService.computeFinalGrades(sectionId, componentToWeightPercent);
            return ApiResponse.ok((Void) null);
        } catch (IllegalArgumentException ex) {
            // Preserve existing error messages
            return ApiResponse.error(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error computing final grades", ex);
            return ApiResponse.error("Error computing final grades: " + ex.getMessage());
        }
    }

    public ApiResponse<SectionStatsDto> getSectionStats(int sectionId) {
        try {
            Map<String, Double> stats = instructorService.getSectionStats(sectionId);
            SectionStatsDto dto = new SectionStatsDto(stats);
            return ApiResponse.ok(dto);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error getting section stats", ex);
            return ApiResponse.error("Error getting stats: " + ex.getMessage());
        }
    }

    public ApiResponse<Void> exportGradesCsv(int sectionId, File targetFile) {
        return ApiResponse.error("Grade CSV export is not yet implemented");
    }

    private SectionRow toSectionRow(Section section) {
        SectionRow row = new SectionRow();
        row.setSectionId(section.getSectionId());
        row.setCourseId(section.getCourseId());
        row.setDayTime(section.getDayTime());
        row.setRoom(section.getRoom());
        row.setCapacity(section.getCapacity());
        row.setSemester(section.getSemester());
        row.setYear(section.getYear());

        // Enrich with course information
        try {
            Course course = courseDao.findById(section.getCourseId());
            row.setCourseCode(course.getCode());
            row.setCourseTitle(course.getTitle());
            row.setCourseCredits(course.getCredits());
        } catch (Exception e) {
            row.setCourseCode("N/A");
            row.setCourseTitle("N/A");
            row.setCourseCredits(null);
        }

        return row;
    }
}

