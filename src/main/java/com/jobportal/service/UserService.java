package com.jobportal.service;

import com.jobportal.dao.UserDAO;
import com.jobportal.dao.impl.UserDAOImpl;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.User;
import com.jobportal.util.ValidationUtil;

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
        return user;
    }

    public boolean register(User user) throws ValidationException {
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }
        if (!ValidationUtil.isValidPassword(user.getPassword())) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (userDAO.findByEmail(user.getEmail()) != null) {
            throw new ValidationException("Email already registered.");
        }
        return userDAO.register(user);
    }

    public User findById(int id) {
        return userDAO.findById(id);
    }

    public List<User> findAll() {
        return userDAO.findAll();
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
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        return userDAO.updatePassword(user.getId(), newPassword);
    }

    public boolean delete(int id) {
        return userDAO.delete(id);
    }

    public int countByType(String userType) {
        return userDAO.countByType(userType);
    }
}
