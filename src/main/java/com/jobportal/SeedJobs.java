package com.jobportal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

public class SeedJobs {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/job_portal";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the database!");

            // Users are created per company below

            String[] companies = {"Google", "Amazon", "Meta", "Microsoft"};
            String[] locations = {"Addis Ababa, Ethiopia", "Gondar, Ethiopia", "Bahir Dar, Ethiopia", "Debre Markos, Ethiopia"};
            String[] titles = {"Senior Software Engineer", "Cloud Solutions Architect", "AI Research Scientist", "Data Engineering Manager"};

            for (int i = 0; i < companies.length; i++) {
                String company = companies[i];
                int empId = -1;
                
                // check if employer exists
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM employers WHERE company_name = ?")) {
                    ps.setString(1, company);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) empId = rs.getInt("id");
                    }
                }
                
                if (empId == -1) {
                    // Create user for employer
                    int userId = 1;
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (full_name, email, password, phone, user_type) VALUES (?, ?, 'pwd', '000', 'EMPLOYER')", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, company + " Admin");
                        ps.setString(2, "admin@" + company.toLowerCase() + ".com");
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) userId = rs.getInt(1);
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO employers (user_id, company_name, company_description) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, userId);
                        ps.setString(2, company);
                        ps.setString(3, "Global Tech Leader");
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) empId = rs.getInt(1);
                        }
                    }
                }

                String title = titles[i];
                String location = locations[i];
                
                String sql = "INSERT INTO jobs (employer_id, title, description, requirements, location, salary_min, salary_max, job_type, category, experience_level, deadline, is_active, department) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, empId);
                    ps.setString(2, title);
                    ps.setString(3, "Join our amazing team at " + company + " in " + location + ". We are looking for top-tier talent to build world-class products. We provide excellent benefits, remote flexibility, and stock options.");
                    ps.setString(4, "- 5+ years of experience in Software Engineering.\n- Strong Java, Python or C++ skills.\n- Experience with distributed systems and cloud platforms.\n- Excellent communication skills.");
                    ps.setString(5, location);
                    ps.setBigDecimal(6, new BigDecimal("150000"));
                    ps.setBigDecimal(7, new BigDecimal("250000"));
                    ps.setString(8, "FULL_TIME");
                    ps.setString(9, "Technology");
                    ps.setString(10, "SENIOR");
                    
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, 3); // 3 months deadline so it is > 30 days
                    ps.setDate(11, new Date(cal.getTimeInMillis()));
                    ps.setString(12, "Engineering");
                    
                    ps.executeUpdate();
                    System.out.println("Inserted job: " + title + " at " + company + " in " + location);
                }
            }
            System.out.println("Seed completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
