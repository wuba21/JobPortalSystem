package com.jobportal.dao;

import com.jobportal.model.CV;

/**
 * DAO interface for CV operations.
 */
public interface CVDAO {
    /** Save a new CV and return its generated ID, or -1 on failure. */
    int save(CV cv);

    /** Update an existing CV record. */
    boolean update(CV cv);

    /** Load the most-recent CV for a given user, or null if none exists. */
    CV findByUserId(int userId);

    /** Update only the pdfPath column for a given CV id. */
    boolean updatePdfPath(int cvId, String pdfPath);
}
