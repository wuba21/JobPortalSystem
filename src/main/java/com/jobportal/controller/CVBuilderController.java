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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;

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
        CV cv = buildCVFromForm();
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("CV Preview — " + cv.getFullName());
        dialog.setHeaderText(null);
        
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);
        
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setPrefWidth(550);
        container.setStyle("-fx-background-color: #f8fafc;");
        
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8;");
        
        if (selectedPhotoPath != null) {
            File file = new File(selectedPhotoPath);
            if (file.exists()) {
                try {
                    ImageView imgView = new ImageView(new Image(file.toURI().toString()));
                    imgView.setFitWidth(90);
                    imgView.setFitHeight(90);
                    imgView.setPreserveRatio(true);
                    
                    StackPane imgHolder = new StackPane(imgView);
                    imgHolder.setStyle("-fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 45; -fx-background-radius: 45; -fx-overflow: hidden;");
                    imgHolder.setPrefSize(90, 90);
                    headerBox.getChildren().add(imgHolder);
                } catch (Exception ignored) {}
            }
        }
        
        VBox headerText = new VBox(5);
        Label nameLbl = new Label(safe(cv.getFullName()));
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        Label contactLbl = new Label(
            (cv.getEmail().isEmpty() ? "" : "📧 " + cv.getEmail()) +
            (cv.getPhone().isEmpty() ? "" : "  |  📞 " + cv.getPhone()) +
            (cv.getAddress().isEmpty() ? "" : "  |  📍 " + cv.getAddress())
        );
        contactLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        
        Label subLbl = new Label(
            (cv.getLinkedin().isEmpty() ? "" : "🔗 LinkedIn: " + cv.getLinkedin()) +
            (cv.getGender() == null || cv.getGender().isEmpty() ? "" : "  |  👤 Gender: " + cv.getGender())
        );
        subLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
        
        headerText.getChildren().addAll(nameLbl, contactLbl, subLbl);
        headerBox.getChildren().add(headerText);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        
        container.getChildren().add(headerBox);
        
        VBox detailsBox = new VBox(15);
        detailsBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        
        addPreviewSection(detailsBox, "🎯 CAREER OBJECTIVE", cv.getObjective());
        
        String eduText = "";
        if (!cv.getUniversityName().isEmpty()) {
            eduText = "🎓 " + safe(cv.getDegree()) + " in " + safe(cv.getDepartment()) + "\n🏫 " + safe(cv.getUniversityName()) + " (" + safe(cv.getGraduationYear()) + ")";
        }
        addPreviewSection(detailsBox, "🎓 EDUCATION", eduText);
        
        String expText = "";
        String[] exp = cv.getExperience() != null ? cv.getExperience().split("\\|", -1) : new String[5];
        if (!get(exp, 0).isEmpty() || !get(exp, 1).isEmpty()) {
            expText = "💼 " + safe(get(exp, 1)) + " at " + safe(get(exp, 0)) + " (" + safe(get(exp, 2)) + " - " + safe(get(exp, 3)) + ")\n📝 " + safe(get(exp, 4));
        }
        addPreviewSection(detailsBox, "💼 EXPERIENCE", expText);
        
        String skillsText = cv.getSkills() != null ? cv.getSkills().replace(",", "   •   ").replace(",,", "").trim() : "";
        addPreviewSection(detailsBox, "🛠 SKILLS", skillsText);
        
        String actText = "";
        String[] act = cv.getActivities() != null ? cv.getActivities().split("\\|", -1) : new String[3];
        if (!get(act, 0).isEmpty()) {
            actText = "🏅 " + safe(get(act, 1)) + " at " + safe(get(act, 0)) + "\n📝 " + safe(get(act, 2));
        }
        addPreviewSection(detailsBox, "🏅 ACTIVITIES & ORGANIZATIONS", actText);
        
        ScrollPane scrollPane = new ScrollPane(detailsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        container.getChildren().add(scrollPane);
        
        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().setPrefWidth(580);
        dialog.getDialogPane().setPrefHeight(600);
        
        dialog.showAndWait();
    }

    // ── Generate PDF ──────────────────────────────────────────────────────────
    @FXML
    private void handleGeneratePDF() {
        CV cv = buildCVFromForm();
        if (cv.getFullName().isEmpty()) {
            AlertUtil.showError("Validation", "Please enter your Full Name before generating a PDF.");
            return;
        }

        // Prompt user for save path using FileChooser
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CV PDF");
        fc.setInitialFileName("CV_" + cv.getFullName().replace(" ", "_") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fc.showSaveDialog(MainApp.getPrimaryStage());
        if (file == null) {
            return; // User cancelled the dialog
        }

        String destPath = file.getAbsolutePath();
        showStatus("⏳ Generating PDF...", "#f59e0b");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return generatePDF(cv, destPath);
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
            showStatus("✅ PDF saved to: " + file.getName(), "green");
            AlertUtil.showInfo("PDF Generated", "Your CV has been saved successfully to:\n" + path);
            clearForm(); // Clear CV form fields after generation
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showStatus("❌ PDF generation failed.", "red");
            AlertUtil.showError("Error", "Failed to generate PDF: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    /**
     * Generates the PDF using Apache PDFBox, styled according to the selected template.
     * Returns the absolute path of the saved file.
     */
    private String generatePDF(CV cv, String destPath) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageW = page.getMediaBox().getWidth();   // 595
            float pageH = page.getMediaBox().getHeight();  // 842

            // Fonts
            PDFont bold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont regular= new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont italic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            int template = cv.getCvTemplate();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                if (template == 1) {
                    // ── TEMPLATE 1: PURPLE HEADER & TWO COLUMN ──────────────────
                    // Purple Banner
                    cs.setNonStrokingColor(0.38f, 0.15f, 0.62f); // Purple
                    cs.addRect(0, pageH - 120f, pageW, 120f);
                    cs.fill();

                    // Sidebar Background (right-hand column)
                    cs.setNonStrokingColor(0.96f, 0.94f, 0.98f); // Soft Purple/Gray
                    cs.addRect(390f, 0f, 205f, pageH - 120f);
                    cs.fill();

                    // Profile Photo in Banner (circular-like placement)
                    if (cv.getPhotoPath() != null) {
                        File imgFile = new File(cv.getPhotoPath());
                        if (imgFile.exists()) {
                            PDImageXObject img = PDImageXObject.createFromFile(cv.getPhotoPath(), doc);
                            cs.drawImage(img, pageW - 125f, pageH - 100f, 80f, 80f);
                        }
                    }

                    // Header Info
                    cs.setNonStrokingColor(1f, 1f, 1f); // White
                    drawText(cs, bold, 24, 45f, pageH - 50f, safe(cv.getFullName()));
                    drawText(cs, regular, 11, 45f, pageH - 80f, "Curriculum Vitae");

                    float yLeft = pageH - 120f - 30f;
                    float yRight = pageH - 120f - 30f;
                    float[] titleColor = new float[]{0.38f, 0.15f, 0.62f};

                    // Left Column (width = 320)
                    yLeft = drawColumnSection(cs, bold, regular, "CAREER OBJECTIVE", cv.getObjective(), 35f, yLeft, 320f, titleColor);
                    yLeft = drawColumnSection(cs, bold, regular, "EDUCATION", getEducationText(cv), 35f, yLeft, 320f, titleColor);
                    yLeft = drawColumnSection(cs, bold, regular, "EXPERIENCE", getExperienceText(cv), 35f, yLeft, 320f, titleColor);
                    yLeft = drawColumnSection(cs, bold, regular, "ACTIVITIES & ORGANIZATIONS", getActivitiesText(cv), 35f, yLeft, 320f, titleColor);

                    // Right Column (width = 160)
                    yRight = drawColumnSection(cs, bold, regular, "CONTACT", getContactText(cv), 405f, yRight, 160f, titleColor);
                    yRight = drawColumnSection(cs, bold, regular, "SKILLS", getSkillsText(cv), 405f, yRight, 160f, titleColor);

                } else if (template == 2) {
                    // ── TEMPLATE 2: CLEAN MODERN WITH SIDEBAR ──────────────────
                    // Sidebar background (left)
                    cs.setNonStrokingColor(0.09f, 0.13f, 0.22f); // Dark Slate Blue
                    cs.addRect(0f, 0f, 180f, pageH);
                    cs.fill();

                    // Profile Photo in Sidebar
                    if (cv.getPhotoPath() != null) {
                        File imgFile = new File(cv.getPhotoPath());
                        if (imgFile.exists()) {
                            PDImageXObject img = PDImageXObject.createFromFile(cv.getPhotoPath(), doc);
                            cs.drawImage(img, 45f, pageH - 135f, 90f, 90f);
                        }
                    }

                    // Sidebar Content (Contact, Skills)
                    float ySidebar = pageH - 160f;
                    ySidebar = drawSidebarSection(cs, bold, regular, "CONTACT", getContactText(cv), 20f, ySidebar, 140f);
                    ySidebar = drawSidebarSection(cs, bold, regular, "SKILLS", getSkillsText(cv), 20f, ySidebar, 140f);

                    // Main Content (Name, Title, Objective, Education, Experience, Activities)
                    float yMain = pageH - 60f;
                    cs.setNonStrokingColor(0.09f, 0.13f, 0.22f);
                    drawText(cs, bold, 28, 205f, yMain, safe(cv.getFullName()));
                    yMain -= 25f;
                    cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                    drawText(cs, italic, 11, 205f, yMain, "Professional Resume");
                    yMain -= 35f;

                    float[] titleColor = new float[]{0.09f, 0.13f, 0.22f};
                    yMain = drawColumnSection(cs, bold, regular, "CAREER OBJECTIVE", cv.getObjective(), 205f, yMain, 345f, titleColor);
                    yMain = drawColumnSection(cs, bold, regular, "EDUCATION", getEducationText(cv), 205f, yMain, 345f, titleColor);
                    yMain = drawColumnSection(cs, bold, regular, "EXPERIENCE", getExperienceText(cv), 205f, yMain, 345f, titleColor);
                    yMain = drawColumnSection(cs, bold, regular, "ACTIVITIES", getActivitiesText(cv), 205f, yMain, 345f, titleColor);

                } else if (template == 3) {
                    // ── TEMPLATE 3: COVER LETTER STYLE (ELEGANT FORMAL) ────────
                    float y = pageH - 50f;
                    cs.setNonStrokingColor(0.13f, 0.55f, 0.44f); // Teal Accent
                    
                    // Profile Photo (Top Right)
                    if (cv.getPhotoPath() != null) {
                        File imgFile = new File(cv.getPhotoPath());
                        if (imgFile.exists()) {
                            PDImageXObject img = PDImageXObject.createFromFile(cv.getPhotoPath(), doc);
                            cs.drawImage(img, pageW - 110f, pageH - 100f, 65f, 65f);
                        }
                    }

                    drawText(cs, bold, 26, 50f, y, safe(cv.getFullName()));
                    y -= 22f;
                    
                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    String contactLine = safe(cv.getEmail()) + "  |  " + safe(cv.getPhone()) + "  |  " + safe(cv.getAddress());
                    drawText(cs, regular, 9.5f, 50f, y, contactLine);
                    y -= 15f;
                    
                    if (cv.getLinkedin() != null && !cv.getLinkedin().isEmpty()) {
                        drawText(cs, italic, 9f, 50f, y, "LinkedIn: " + cv.getLinkedin());
                        y -= 15f;
                    }

                    // Divider Line
                    cs.setStrokingColor(0.13f, 0.55f, 0.44f);
                    cs.setLineWidth(1.5f);
                    cs.moveTo(50f, y - 5f);
                    cs.lineTo(pageW - 50f, y - 5f);
                    cs.stroke();
                    y -= 25f;

                    float[] titleColor = new float[]{0.13f, 0.55f, 0.44f};
                    y = drawColumnSection(cs, bold, regular, "CAREER OBJECTIVE", cv.getObjective(), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "EDUCATION", getEducationText(cv), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "EXPERIENCE", getExperienceText(cv), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "SKILLS", getSkillsText(cv), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "ACTIVITIES & ORGANIZATIONS", getActivitiesText(cv), 50f, y, 495f, titleColor);

                } else {
                    // ── TEMPLATE 4: ATS FRIENDLY ─────────────────────────────────
                    float y = pageH - 50f;
                    cs.setNonStrokingColor(0f, 0f, 0f); // Plain Black
                    
                    drawText(cs, bold, 22, 50f, y, safe(cv.getFullName()));
                    y -= 20f;
                    
                    String contactInfo = safe(cv.getEmail()) + "  |  " + safe(cv.getPhone()) + "  |  " + safe(cv.getAddress());
                    drawText(cs, regular, 9.5f, 50f, y, contactInfo);
                    y -= 14f;
                    if (cv.getLinkedin() != null && !cv.getLinkedin().isEmpty()) {
                        drawText(cs, regular, 9f, 50f, y, "LinkedIn: " + cv.getLinkedin());
                        y -= 14f;
                    }
                    
                    // Divider Line
                    cs.setStrokingColor(0f, 0f, 0f);
                    cs.setLineWidth(1f);
                    cs.moveTo(50f, y - 4f);
                    cs.lineTo(pageW - 50f, y - 4f);
                    cs.stroke();
                    y -= 20f;

                    float[] titleColor = new float[]{0f, 0f, 0f};
                    y = drawColumnSection(cs, bold, regular, "CAREER OBJECTIVE", cv.getObjective(), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "EXPERIENCE", getExperienceText(cv), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "EDUCATION", getEducationText(cv), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "SKILLS", getSkillsText(cv), 50f, y, 495f, titleColor);
                    y = drawColumnSection(cs, bold, regular, "ACTIVITIES", getActivitiesText(cv), 50f, y, 495f, titleColor);
                }

                // Page Footer
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                drawText(cs, italic, 8, 50f, 20f, "Generated by Job Portal CV Builder");
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
        return drawColumnSection(cs, boldFont, bodyFont, title, body, x, y, maxWidth, new float[]{0.15f, 0.15f, 0.15f});
    }

    private float drawColumnSection(PDPageContentStream cs, PDFont boldFont, PDFont bodyFont,
                                    String title, String body, float x, float y, float width, float[] titleColor) throws IOException {
        if (body == null || body.trim().isBlank()) return y;
        float lineH = 14f;

        // Section title
        cs.setNonStrokingColor(titleColor[0], titleColor[1], titleColor[2]);
        drawText(cs, boldFont, 11, x, y - 10f, title);

        // Divider line
        cs.setStrokingColor(titleColor[0], titleColor[1], titleColor[2]);
        cs.setLineWidth(0.8f);
        cs.moveTo(x, y - 14f);
        cs.lineTo(x + width, y - 14f);
        cs.stroke();

        y -= 28f;

        // Body — split on newlines then wrap long lines
        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
        for (String line : body.split("\n")) {
            List<String> wrapped = wrapText(line, bodyFont, 9f, width);
            for (String wl : wrapped) {
                if (y < 45) break;
                drawText(cs, bodyFont, 9, x + 5f, y, wl);
                y -= lineH;
            }
        }
        return y - 10f; // Gap after section
    }

    private void drawSidebarText(PDPageContentStream cs, PDFont font, float size,
                                 float x, float y, String text) throws IOException {
        if (text == null || text.isBlank()) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(1f, 1f, 1f); // White
        cs.newLineAtOffset(x, y);
        cs.showText(text.replaceAll("[^\\x20-\\x7E]", ""));
        cs.endText();
    }

    private float drawSidebarSection(PDPageContentStream cs, PDFont boldFont, PDFont bodyFont,
                                     String title, String body, float x, float y, float width) throws IOException {
        if (body == null || body.trim().isBlank()) return y;
        float lineH = 12f;

        // Title in Gold/Accent Color
        cs.setNonStrokingColor(1.0f, 0.84f, 0.0f);
        drawSidebarText(cs, boldFont, 11, x, y - 10f, title);

        // Divider
        cs.setStrokingColor(1.0f, 0.84f, 0.0f);
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y - 14f);
        cs.lineTo(x + width, y - 14f);
        cs.stroke();

        y -= 26f;

        // Body in white
        cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
        for (String line : body.split("\n")) {
            List<String> wrapped = wrapText(line, bodyFont, 8.5f, width);
            for (String wl : wrapped) {
                if (y < 30) break;
                cs.beginText();
                cs.setFont(bodyFont, 8.5f);
                cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
                cs.newLineAtOffset(x, y);
                cs.showText(wl.replaceAll("[^\\x20-\\x7E]", ""));
                cs.endText();
                y -= lineH;
            }
        }
        return y - 10f;
    }

    private String getEducationText(CV cv) {
        if (cv.getUniversityName().isEmpty()) return "";
        return safe(cv.getDegree()) + " in " + safe(cv.getDepartment()) + "\n" +
               safe(cv.getUniversityName()) + "\nGraduation Year: " + safe(cv.getGraduationYear());
    }

    private String getExperienceText(CV cv) {
        String[] exp = cv.getExperience() != null ? cv.getExperience().split("\\|", -1) : new String[5];
        if (get(exp, 0).isEmpty() && get(exp, 1).isEmpty()) return "";
        return safe(get(exp, 1)) + " at " + safe(get(exp, 0)) + "\n" +
               safe(get(exp, 2)) + " – " + safe(get(exp, 3)) + "\n" +
               safe(get(exp, 4));
    }

    private String getActivitiesText(CV cv) {
        String[] act = cv.getActivities() != null ? cv.getActivities().split("\\|", -1) : new String[3];
        if (get(act, 0).isEmpty()) return "";
        return safe(get(act, 1)) + " at " + safe(get(act, 0)) + "\n" + safe(get(act, 2));
    }

    private String getContactText(CV cv) {
        StringBuilder sb = new StringBuilder();
        if (!cv.getEmail().isEmpty()) sb.append("Email: ").append(cv.getEmail()).append("\n");
        if (!cv.getPhone().isEmpty()) sb.append("Phone: ").append(cv.getPhone()).append("\n");
        if (!cv.getAddress().isEmpty()) sb.append("Address: ").append(cv.getAddress()).append("\n");
        if (cv.getLinkedin() != null && !cv.getLinkedin().isEmpty()) sb.append("LinkedIn: ").append(cv.getLinkedin()).append("\n");
        if (cv.getGender() != null && !cv.getGender().isEmpty()) sb.append("Gender: ").append(cv.getGender());
        return sb.toString().trim();
    }

    private String getSkillsText(CV cv) {
        if (cv.getSkills() == null || cv.getSkills().isEmpty()) return "";
        String[] sk = cv.getSkills().split(",", -1);
        StringBuilder sb = new StringBuilder();
        for (String s : sk) {
            if (s != null && !s.trim().isEmpty()) {
                sb.append("• ").append(s.trim()).append("\n");
            }
        }
        return sb.toString().trim();
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

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        linkedinField.clear();
        genderCombo.setValue(null);

        universityField.clear();
        degreeField.clear();
        departmentField.clear();
        gradYearField.clear();

        companyField.clear();
        positionField.clear();
        startDateField.clear();
        endDateField.clear();
        expDescArea.clear();

        skill1Field.clear();
        skill2Field.clear();
        skill3Field.clear();
        skill4Field.clear();
        skill5Field.clear();

        orgNameField.clear();
        orgPositionField.clear();
        activityDescArea.clear();

        objectiveArea.clear();

        selectedPhotoPath = null;
        photoPreview.setImage(null);
        templateCombo.getSelectionModel().selectFirst();
        updateTemplatePreview();

        currentCVId = -1;
    }

    private void addPreviewSection(VBox container, String title, String content) {
        if (content == null || content.trim().isEmpty()) return;
        
        VBox sectionBox = new VBox(5);
        sectionBox.setPadding(new Insets(0, 0, 10, 0));
        
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 3 0;");
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        
        Label contentLbl = new Label(content);
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px; -fx-line-spacing: 3;");
        
        sectionBox.getChildren().addAll(titleLbl, contentLbl);
        container.getChildren().add(sectionBox);
    }
}
