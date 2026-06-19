package com.jobportal.service;

import com.jobportal.dao.UserDAO;
import com.jobportal.dao.impl.UserDAOImpl;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.User;
import com.jobportal.util.ValidationUtil;
import com.jobportal.util.SessionManager;

import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public User login(String email, String password) throws ValidationException {
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Invalid email format.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }
        User user = userDAO.authenticate(email, password);
        if (user == null) {
            throw new ValidationException("Invalid email or password.");
        }

        // Send login notification to Admin
        new Thread(() -> {
            com.jobportal.util.EmailUtil.sendRealAdminNotification(
                "User Login Alert",
                "A user has logged in.\n\nName: " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nType: " + user.getUserType()
            );
        }).start();

        return user;
    }

    public boolean register(User user) throws ValidationException {
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }

        // Check password strength (Phase 1: Strong password requirement)
        String passwordError = com.jobportal.util.PasswordUtil.getPasswordStrengthError(user.getPassword());
        if (passwordError != null) {
            throw new ValidationException(passwordError);
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (userDAO.findByEmail(user.getEmail()) != null) {
            throw new ValidationException("Email already registered.");
        }
        boolean success = userDAO.register(user);
        if (success) {
            // Send registration notification to Admin
            new Thread(() -> {
                com.jobportal.util.EmailUtil.sendRealAdminNotification(
                    "New User Registration Alert",
                    "A new user has registered in the Job Portal.\n\nName: " + user.getFullName() + "\nEmail: " + user.getEmail() + "\nType: " + user.getUserType()
                );
            }).start();
        }
        return success;
    }

    public User findById(int id) {
        return userDAO.findById(id);
    }

    public List<User> findAll() {
        return userDAO.findAll();
    }

    public List<User> getMessageableUsers(int currentUserId, String currentUserType) {
        return userDAO.findMessageableUsers(currentUserId, currentUserType);
    }

    public boolean update(User user) {
        return userDAO.update(user);
    }

    public boolean checkEmailAndPhoneMatch(String email, String phone) {
        User user = userDAO.findByEmail(email);
        if (user != null) {
            return user.getPhone() != null && user.getPhone().equals(phone);
        }
        return false;
    }

    public boolean resetPassword(String email, String newPassword) throws ValidationException {
        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new ValidationException("Email not found.");
        }

        // Check password strength (Phase 1: Strong password requirement)
        String passwordError = com.jobportal.util.PasswordUtil.getPasswordStrengthError(newPassword);
        if (passwordError != null) {
            throw new ValidationException(passwordError);
        }

        return userDAO.updatePassword(user.getId(), newPassword);
    }

    public boolean delete(int id) {
        return userDAO.delete(id);
    }

    public int countByType(String userType) {
        return userDAO.countByType(userType);
    }

    public int getOrLoadEmployerId(User user) {
        if (user == null || !"EMPLOYER".equals(user.getUserType())) {
            return -1;
        }
        int empId = SessionManager.getEmployerId();
        if (empId > 0) {
            return empId;
        }
        // Query database
        empId = userDAO.getEmployerIdByUserId(user.getId());
        if (empId <= 0) {
            // Auto-create to prevent data inconsistency
            String companyName = user.getFullName() + " Company";
            empId = userDAO.createEmployer(user.getId(), companyName, "Other");
        }
        if (empId > 0) {
            SessionManager.setEmployerId(empId);
        }
        return empId;
    }
}
