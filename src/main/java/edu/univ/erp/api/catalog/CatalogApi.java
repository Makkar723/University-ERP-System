package edu.univ.erp.api.catalog;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.univ.erp.access.AccessDeniedException;
import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.data.dao.AuthDao;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.EnrollmentDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.ServiceRegistry;

public class CatalogApi {
    private static final Logger logger = LoggerFactory.getLogger(CatalogApi.class);
    private final CatalogService catalogService;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final AuthDao authDao;

    public CatalogApi() {
        this.catalogService = ServiceRegistry.getCatalogService();
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.authDao = new AuthDao();
    }

    public ApiResponse<List<CourseRow>> listCourses() {
        try {
            List<Course> courses = catalogService.listCourses();
            List<CourseRow> courseRows = new ArrayList<>();
            
            for (Course course : courses) {
                CourseRow row = new CourseRow();
                row.setCourseId(course.getCourseId());
                row.setCode(course.getCode());
                row.setTitle(course.getTitle());
                row.setCredits(course.getCredits());
                courseRows.add(row);
            }
            
            return ApiResponse.ok(courseRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error listing courses", ex);
            return ApiResponse.error("Error listing courses: " + ex.getMessage());
        }
    }

    public ApiResponse<List<SectionRow>> listAllSections() {
        try {
            List<Section> sections = catalogService.listAllSections();
            List<SectionRow> sectionRows = new ArrayList<>();
            
            for (Section section : sections) {
                SectionRow row = toSectionRow(section);
                sectionRows.add(row);
            }
            
            return ApiResponse.ok(sectionRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error listing sections", ex);
            return ApiResponse.error("Error listing sections: " + ex.getMessage());
        }
    }

    public ApiResponse<List<SectionRow>> listSectionsForCourse(int courseId) {
        try {
            List<Section> sections = catalogService.listSectionsForCourse(courseId);
            List<SectionRow> sectionRows = new ArrayList<>();
            
            for (Section section : sections) {
                SectionRow row = toSectionRow(section);
                sectionRows.add(row);
            }
            
            return ApiResponse.ok(sectionRows);
        } catch (AccessDeniedException ex) {
            return ApiResponse.error(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error listing sections for course", ex);
            return ApiResponse.error("Error listing sections: " + ex.getMessage());
        }
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

        // Enrich with instructor information
        if (section.getInstructorId() != null) {
            try {
                UserAuth instructor = authDao.findById(section.getInstructorId());
                row.setInstructorName(instructor.getUsername());
            } catch (Exception e) {
                row.setInstructorName("ID: " + section.getInstructorId());
            }
        } else {
            row.setInstructorName("None");
        }

        // Enrich with enrollment count
        try {
            int enrolled = enrollmentDao.countBySection(section.getSectionId());
            row.setEnrolled(enrolled);
            int capacity = section.getCapacity() != null ? section.getCapacity() : 0;
            row.setSeatsLeft(capacity - enrolled);
        } catch (Exception e) {
            row.setEnrolled(0);
            row.setSeatsLeft(0);
        }

        return row;
    }
}

