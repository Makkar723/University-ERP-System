package edu.univ.erp.service;

import java.util.List;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.data.dao.CourseDao;
import edu.univ.erp.data.dao.SectionDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;

public class CatalogService {
    private final CourseDao courseDao;
    private final SectionDao sectionDao;
    
    public CatalogService(CourseDao courseDao, SectionDao sectionDao) {
        this.courseDao = courseDao;
        this.sectionDao = sectionDao;
    }
    
    public List<Course> listCourses() {
        AccessController.requireLoggedIn();
        return courseDao.listCourses();
    }
    
    public List<Section> listSectionsForCourse(int courseId) {
        AccessController.requireLoggedIn();
        return sectionDao.listSectionsByCourse(courseId);
    }
    
    public List<Section> listAllSections() {
        AccessController.requireLoggedIn();
        List<Course> courses = courseDao.listCourses();
        List<Section> allSections = new java.util.ArrayList<>();
        
        for (Course course : courses) {
            List<Section> sections = sectionDao.listSectionsByCourse(course.getCourseId());
            allSections.addAll(sections);
        }
        
        return allSections;
    }
}

