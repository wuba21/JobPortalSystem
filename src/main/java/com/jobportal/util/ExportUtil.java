package com.jobportal.util;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.FontFactory;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

public class ExportUtil {

    public static void exportToPDF(String title, String[] headers, List<List<String>> rows, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        // Add Title
        document.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("Generated on: " + new Date().toString()));
        document.add(new Paragraph(" ")); // spacer

        // Create Table
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        
        // Add Headers
        for (String header : headers) {
            table.addCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        }
        
        // Add Rows
        for (List<String> row : rows) {
            for (String cell : row) {
                table.addCell(new Phrase(cell != null ? cell : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
            }
        }
        
        document.add(table);
        document.close();
    }

    public static void exportToExcel(String title, String[] headers, List<List<String>> rows, File file) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report");

        // Title Row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        // Header Row
        Row headerRow = sheet.createRow(2);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        int rowNum = 3;
        for (List<String> rowData : rows) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.size(); i++) {
                row.createCell(i).setCellValue(rowData.get(i) != null ? rowData.get(i) : "");
            }
        }

        // Auto size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
        workbook.close();
    }
}
