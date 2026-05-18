package edu.univ.erp.api.reports;

import java.io.File;

import edu.univ.erp.api.common.ApiResponse;
import edu.univ.erp.api.student.StudentApi;


public class ReportsApi {
    private final StudentApi studentApi;

    public ReportsApi() {
        this.studentApi = new StudentApi();
    }


    public ApiResponse<Void> exportTranscriptCsv(int studentUserId, File targetFile) {
        return studentApi.downloadTranscriptCsv(studentUserId, targetFile);
    }


    public ApiResponse<Void> exportTranscriptPdf(int studentUserId, File targetFile) {
        return studentApi.downloadTranscriptPdf(studentUserId, targetFile);
    }


    public ApiResponse<Void> exportClassListCsv(int sectionId, File targetFile) {
        // Not yet implemented
        return ApiResponse.error("Class list CSV export is not yet implemented");
    }
}

