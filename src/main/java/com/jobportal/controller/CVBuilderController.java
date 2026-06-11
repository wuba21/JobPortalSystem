package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.dao.CVDAO;
import com.jobportal.dao.impl.CVDAOImpl;
import com.jobportal.model.CV;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the CV Builder scene.
 * Handles form input, template selection, photo upload, PDF generation, and DB persistence.
 */
public class CVBuilderController {

    // ── Personal Info ─────────────────────────────────────────────────────────
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField linkedinField;
    @FXML private ComboBox<String> genderCombo;

    // ── Education ─────────────────────────────────────────────────────────────
    @FXML private TextField universityField;
    @FXML private TextField degreeField;
    @FXML private TextField departmentField;
    @FXML private TextField gradYearField;

    // ── Experience ────────────────────────────────────────────────────────────
    @FXML private TextField companyField;
    @FXML private TextField positionField;
    @FXML private TextField startDateField;
    @FXML private TextField endDateField;
    @FXML private TextArea  expDescArea;

    // ── Skills ────────────────────────────────────────────────────────────────
    @FXML private TextField skill1Field;
    @FXML private TextField skill2Field;
    @FXML private TextField skill3Field;
    @FXML private TextField skill4Field;
    @FXML private TextField skill5Field;

    // ── Activities ────────────────────────────────────────────────────────────
    @FXML private TextField orgNameField;
    @FXML private TextField orgPositionField;
    @FXML private TextArea  activityDescArea;

    // ── Career Objective ──────────────────────────────────────────────────────
    @FXML private TextArea objectiveArea;

    // ── Template & Photo ──────────────────────────────────────────────────────
    @FXML private ComboBox<String> templateCombo;
    @FXML private ImageView photoPreview;
    @FXML private ImageView templatePreview;
    @FXML private Label statusLabel;

    private String selectedPhotoPath = null;
    private int    currentCVId       = -1;   // -1 means new CV

    private final CVDAO cvDAO = new CVDAOImpl();

    // Paths to the bundled template preview images inside resources/CV/
    private static final String[] TEMPLATE_IMAGES = {
        "/CV/1VGE2R2iED2J3VaE5s20KXcLWPXrr6.png",
        "/CV/GobYkNGjKLMq57nrJbbr3m2fMpYZeW.png",
        "/CV/SBH1pvrgUztNXXKL3eRS5GbcF0EHiC.png",
        "/CV/rrh11hPn7oiHIiVFt3S1rv28xv9qBY.png"
    };

    @FXML
    public void initialize() {
        // Gender options
        genderCombo.getItems().addAll("Male", "Female", "Other", "Prefer not to say");

        // Template options
        templateCombo.getItems().addAll(
            "Template 1 – Purple Header",
            "Template 2 – Clean Modern",
            "Template 3 – Cover Letter Style",
            "Template 4 – ATS Friendly"
        );
        templateCombo.getSelectionModel().selectFirst();
        updateTemplatePreview();

        templateCombo.setOnAction(e -> updateTemplatePreview());

        // Pre-fill from session
        if (SessionManager.getCurrentUser() != null) {
            fullNameField.setText(safe(SessionManager.getCurrentUser().getFullName()));
            emailField.setText(safe(SessionManager.getCurrentUser().getEmail()));
            phoneField.setText(safe(SessionManager.getCurrentUser().getPhone()));
        }

        // Load existing CV if present
        loadExistingCV();
    }

    // ── Template Preview ──────────────────────────────────────────────────────
    private void updateTemplatePreview() {
        int idx = templateCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) idx = 0;
        try {
            var stream = getClass().getResourceAsStream(TEMPLATE_IMAGES[idx]);
            if (stream != null) {
                templatePreview.setImage(new Image(stream));
            }
        } catch (Exception ignored) {}
    }

    // ── Photo Upload ──────────────────────────────────────────────────────────
    @FXML
    private void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Profile Photo");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(MainApp.getPrimaryStage());
        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            photoPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    // ── Load Existing CV ──────────────────────────────────────────────────────
    private void loadExistingCV() {
        if (SessionManager.getCurrentUser() == null) return;
        CV cv = cvDAO.findByUserId(SessionManager.getCurrentUser().getId());
        if (cv == null) return;
        currentCVId = cv.getId();
        fillForm(cv);
    }

    private void fillForm(CV cv) {
        setText(fullNameField,   cv.getFullName());
        setText(emailField,      cv.getEmail());
        setText(phoneField,      cv.getPhone());
        setText(addressField,    cv.getAddress());
        setText(linkedinField,   cv.getLinkedin());
        genderCombo.setValue(cv.getGender());

        setText(universityField, cv.getUniversityName());
        setText(degreeField,     cv.getDegree());
        setText(departmentField, cv.getDepartment());
        setText(gradYearField,   cv.getGraduationYear());

        // Experience — stored as "Company|Position|Start|End|Desc"
        if (cv.getExperience() != null) {
            String[] parts = cv.getExperience().split("\\|", -1);
            if (parts.length >= 5) {
                setText(companyField,  parts[0]);
                setText(positionField, parts[1]);
                setText(startDateField,parts[2]);
                setText(endDateField,  parts[3]);
                expDescArea.setText(parts[4]);
            }
        }

        // Skills — stored as "s1,s2,s3,s4,s5"
        if (cv.getSkills() != null) {
            String[] sk = cv.getSkills().split(",", -1);
            setText(skill1Field, sk.length > 0 ? sk[0] : "");
            setText(skill2Field, sk.length > 1 ? sk[1] : "");
            setText(skill3Field, sk.length > 2 ? sk[2] : "");
            setText(skill4Field, sk.length > 3 ? sk[3] : "");
            setText(skill5Field, sk.length > 4 ? sk[4] : "");
        }

        // Activities — stored as "OrgName|Position|Desc"
        if (cv.getActivities() != null) {
            String[] act = cv.getActivities().split("\\|", -1);
            if (act.length >= 3) {
                setText(orgNameField,     act[0]);
                setText(orgPositionField, act[1]);
                activityDescArea.setText(act[2]);
            }
        }

        objectiveArea.setText(safe(cv.getObjective()));

        if (cv.getPhotoPath() != null) {
            selectedPhotoPath = cv.getPhotoPath();
            File f = new File(cv.getPhotoPath());
            if (f.exists()) photoPreview.setImage(new Image(f.toURI().toString()));
        }

        int tmpl = cv.getCvTemplate();
        if (tmpl >= 1 && tmpl <= 4) {
            templateCombo.getSelectionModel().select(tmpl - 1);
            updateTemplatePreview();
        }
    }

    // ── Build CV Object from Form ─────────────────────────────────────────────
    private CV buildCVFromForm() {
        CV cv = new CV();
        if (SessionManager.getCurrentUser() != null)
            cv.setUserId(SessionManager.getCurrentUser().getId());

        cv.setFullName(fullNameField.getText().trim());
        cv.setEmail(emailField.getText().trim());
        cv.setPhone(phoneField.getText().trim());
        cv.setAddress(addressField.getText().trim());
        cv.setLinkedin(linkedinField.getText().trim());
        cv.setGender(genderCombo.getValue());

        cv.setUniversityName(universityField.getText().trim());
        cv.setDegree(degreeField.getText().trim());
        cv.setDepartment(departmentField.getText().trim());
        cv.setGraduationYear(gradYearField.getText().trim());

        // Pack experience as pipe-separated string
        cv.setExperience(
            companyField.getText().trim() + "|" +
            positionField.getText().trim() + "|" +
            startDateField.getText().trim() + "|" +
            endDateField.getText().trim() + "|" +
            expDescArea.getText().trim()
        );

        // Pack skills as comma-separated
        cv.setSkills(
            skill1Field.getText().trim() + "," +
            skill2Field.getText().trim() + "," +
            skill3Field.getText().trim() + "," +
            skill4Field.getText().trim() + "," +
            skill5Field.getText().trim()
        );

        // Pack activities
        cv.setActivities(
            orgNameField.getText().trim() + "|" +
            orgPositionField.getText().trim() + "|" +
            activityDescArea.getText().trim()
        );

        cv.setObjective(objectiveArea.getText().trim());
        cv.setPhotoPath(selectedPhotoPath);
        cv.setCvTemplate(templateCombo.getSelectionModel().getSelectedIndex() + 1);

        return cv;
    }

    // ── Save CV ───────────────────────────────────────────────────────────────
    @FXML
    private void handleSaveCV() {
        CV cv = buildCVFromForm();
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (currentCVId == -1) {
                    int id = cvDAO.save(cv);
                    if (id > 0) { currentCVId = id; return true; }
                    return false;
                } else {
                    cv.setId(currentCVId);
                    return cvDAO.update(cv);
                }
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) showStatus("✅ CV saved successfully!", "green");
            else                 showStatus("❌ Failed to save CV.", "red");
        });
        new Thread(task).start();
    }

    // ── Preview CV ────────────────────────────────────────────────────────────
    @FXML
    private void handlePreviewCV() {
        // Show a simple dialog preview
        CV cv = buildCVFromForm();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CV Preview — " + cv.getFullName());
        alert.setHeaderText(cv.getFullName() + " | " + cv.getEmail() + " | " + cv.getPhone());
        alert.setContentText(
            "🎓 Education:\n  " + cv.getDegree() + " @ " + cv.getUniversityName() + " (" + cv.getGraduationYear() + ")" +
            "\n\n💼 Experience:\n  " + cv.getExperience().replace("|", " – ") +
            "\n\n🛠 Skills:\n  " + cv.getSkills().replace(",", "  •  ") +
            "\n\n🎯 Objective:\n  " + cv.getObjective()
        );
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }

    // ── Generate PDF ──────────────────────────────────────────────────────────
    @FXML
    private void handleGeneratePDF() {
        CV cv = buildCVFromForm();
        if (cv.getFullName().isEmpty()) {
            AlertUtil.showError("Validation", "Please enter your Full Name before generating a PDF.");
            return;
        }
        showStatus("⏳ Generating PDF...", "#f59e0b");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return generatePDF(cv);
            }
        };
        task.setOnSucceeded(e -> {
            String path = task.getValue();
            cv.setPdfPath(path);
            // Persist CV with pdf path
            if (currentCVId == -1) {
                int id = cvDAO.save(cv);
                if (id > 0) currentCVId = id;
            } else {
                cv.setId(currentCVId);
                cvDAO.update(cv);
            }
            showStatus("✅ PDF saved to Desktop: CV_" + cv.getFullName().replace(" ", "_") + ".pdf", "green");
            AlertUtil.showInfo("PDF Generated", "Your CV has been saved to:\n" + path);
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showStatus("❌ PDF generation failed.", "red");
        });
        new Thread(task).start();
    }

    /**
     * Generates the PDF using Apache PDFBox, styled according to the selected template.
     * Returns the absolute path of the saved file.
     */
    private String generatePDF(CV cv) throws IOException {
        String fileName = "CV_" + cv.getFullName().replace(" ", "_") + ".pdf";
        String destPath = System.getProperty("user.home") + "/Desktop/" + fileName;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageW = page.getMediaBox().getWidth();   // 595
            float pageH = page.getMediaBox().getHeight();  // 842
            float margin = 45f;

            // Fonts
            PDFont bold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont regular= new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont italic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            int template = cv.getCvTemplate();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ── Header background colour per template ──────────────────────
                float headerH = 130f;
                float[] rgb   = headerColor(template);
                cs.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
                cs.addRect(0, pageH - headerH, pageW, headerH);
                cs.fill();

                // ── Profile photo (top-right corner of header) ────────────────
                if (cv.getPhotoPath() != null) {
                    File imgFile = new File(cv.getPhotoPath());
                    if (imgFile.exists()) {
                        PDImageXObject img = PDImageXObject.createFromFile(cv.getPhotoPath(), doc);
                        float imgSize = 85f;
                        cs.drawImage(img, pageW - margin - imgSize, pageH - headerH + 22f, imgSize, imgSize);
                    }
                }

                // ── Name & contact in header ───────────────────────────────────
                cs.setNonStrokingColor(1f, 1f, 1f);
                drawText(cs, bold,    22, margin, pageH - 50f,  safe(cv.getFullName()));
                drawText(cs, regular, 10, margin, pageH - 72f,
                    safe(cv.getEmail()) + "  |  " + safe(cv.getPhone()) + "  |  " + safe(cv.getAddress()));
                drawText(cs, italic,  9,  margin, pageH - 88f,
                    (cv.getLinkedin() != null && !cv.getLinkedin().isEmpty() ? "LinkedIn: " + cv.getLinkedin() : "") +
                    (cv.getGender() != null ? "   Gender: " + cv.getGender() : ""));

                // ── Template label badge ───────────────────────────────────────
                drawText(cs, bold, 8, pageW - 150f, pageH - 118f, "Template " + template);

                // Body — black text from here on
                cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);

                float y = pageH - headerH - 28f;

                // ── Career Objective ─────────────────────────────────────────
                y = drawSection(cs, bold, regular, "CAREER OBJECTIVE", cv.getObjective(), margin, y, pageW - margin*2);

                // ── Education ────────────────────────────────────────────────
                String edu = safe(cv.getDegree()) + " in " + safe(cv.getDepartment()) +
                             "\n" + safe(cv.getUniversityName()) + " | Graduated: " + safe(cv.getGraduationYear());
                y = drawSection(cs, bold, regular, "EDUCATION", edu, margin, y, pageW - margin*2);

                // ── Experience ────────────────────────────────────────────────
                String[] exp = cv.getExperience() != null ? cv.getExperience().split("\\|", -1) : new String[5];
                String expText = safe(get(exp,1)) + " at " + safe(get(exp,0)) +
                                 " (" + safe(get(exp,2)) + " – " + safe(get(exp,3)) + ")\n" + safe(get(exp,4));
                y = drawSection(cs, bold, regular, "EXPERIENCE", expText, margin, y, pageW - margin*2);

                // ── Skills ────────────────────────────────────────────────────
                String skillsText = cv.getSkills() != null
                    ? cv.getSkills().replace(",", "   •   ").replace(",,", "").trim()
                    : "";
                y = drawSection(cs, bold, regular, "SKILLS", skillsText, margin, y, pageW - margin*2);

                // ── Activities ────────────────────────────────────────────────
                String[] act = cv.getActivities() != null ? cv.getActivities().split("\\|", -1) : new String[3];
                String actText = safe(get(act,1)) + " at " + safe(get(act,0)) + "\n" + safe(get(act,2));
                y = drawSection(cs, bold, regular, "ACTIVITIES & ORGANIZATIONS", actText, margin, y, pageW - margin*2);

                // ── Footer ────────────────────────────────────────────────────
                cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
                drawText(cs, italic, 8, margin, 20f, "Generated by Job Portal CV Builder");
            }

            doc.save(destPath);
        }
        return destPath;
    }

    // ── Apply Using This CV ───────────────────────────────────────────────────
    @FXML
    private void handleApplyUsingCV() {
        if (currentCVId == -1) {
            AlertUtil.showError("Save First", "Please save or generate your CV first before applying.");
            return;
        }
        if (SessionManager.getCurrentJob() == null) {
            AlertUtil.showInfo("No Job Selected",
                "Go to Browse Jobs, select a job, then come back to apply with this CV.");
            MainApp.changeScene("job_dashboard.fxml", "Browse Jobs");
            return;
        }
        // Navigate to apply page — CV path will be picked up from DB
        MainApp.changeScene("apply.fxml", "Apply for Job");
    }

    // ── Back ──────────────────────────────────────────────────────────────────
    @FXML
    private void handleBack() {
        MainApp.changeScene("dashboard.fxml", "Dashboard");
    }

    // ── Menu Handlers ─────────────────────────────────────────────────────────
    @FXML private void menuSaveCV()      { handleSaveCV(); }
    @FXML private void menuGeneratePDF() { handleGeneratePDF(); }
    @FXML private void menuExit()        { handleBack(); }

    @FXML private void menuTemplate1() { templateCombo.getSelectionModel().select(0); updateTemplatePreview(); }
    @FXML private void menuTemplate2() { templateCombo.getSelectionModel().select(1); updateTemplatePreview(); }
    @FXML private void menuTemplate3() { templateCombo.getSelectionModel().select(2); updateTemplatePreview(); }
    @FXML private void menuTemplate4() { templateCombo.getSelectionModel().select(3); updateTemplatePreview(); }

    @FXML
    private void menuAbout() {
        AlertUtil.showInfo("About CV Builder",
            "Job Portal CV Builder v1.0\n\nCreate professional CVs with 4 templates.\nGenerate PDF and apply directly to jobs.");
    }

    // ── PDF Helpers ───────────────────────────────────────────────────────────
    private float[] headerColor(int template) {
        return switch (template) {
            case 1  -> new float[]{0.38f, 0.15f, 0.62f};  // Purple
            case 2  -> new float[]{0.11f, 0.53f, 0.80f};  // Blue
            case 3  -> new float[]{0.13f, 0.55f, 0.44f};  // Teal
            default -> new float[]{0.10f, 0.10f, 0.10f};  // Black (ATS)
        };
    }

    private void drawText(PDPageContentStream cs, PDFont font, float size,
                          float x, float y, String text) throws IOException {
        if (text == null || text.isBlank()) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        // Safety: strip non-latin chars that PDFBox Type1 can't encode
        cs.showText(text.replaceAll("[^\\x20-\\x7E]", ""));
        cs.endText();
    }

    /**
     * Draws a labelled section with word-wrapped body text.
     * Returns the new Y position after the section.
     */
    private float drawSection(PDPageContentStream cs,
                              PDFont boldFont, PDFont bodyFont,
                              String title, String body,
                              float x, float y, float maxWidth) throws IOException {
        if (body == null || body.isBlank()) return y;
        float lineH = 14f;

        // Divider line
        cs.setStrokingColor(0.75f, 0.75f, 0.75f);
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y - 4f);
        cs.lineTo(x + maxWidth, y - 4f);
        cs.stroke();

        // Section title
        cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
        drawText(cs, boldFont, 11, x, y - 16f, title);
        y -= 30f;

        // Body — split on newlines then wrap long lines
        cs.setNonStrokingColor(0.25f, 0.25f, 0.25f);
        for (String line : body.split("\n")) {
            List<String> wrapped = wrapText(line, bodyFont, 9.5f, maxWidth);
            for (String wl : wrapped) {
                if (y < 60) break;
                drawText(cs, bodyFont, 9.5f, x + 10, y, wl);
                y -= lineH;
            }
        }
        return y - 8f;
    }

    /** Simple greedy word-wrap. */
    private List<String> wrapText(String text, PDFont font, float size, float maxW) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            float w = font.getStringWidth(test.replaceAll("[^\\x20-\\x7E]", "")) / 1000f * size;
            if (w > maxW && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private String safe(String s)           { return s != null ? s : ""; }
    private String get(String[] arr, int i) { return (arr != null && i < arr.length) ? arr[i] : ""; }
    private void   setText(TextField f, String v) { if (v != null) f.setText(v); }
    private void   showStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
    }
}
