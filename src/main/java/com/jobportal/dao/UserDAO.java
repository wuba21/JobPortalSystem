package com.jobportal.dao;

import com.jobportal.model.User;
import java.util.List;

public interface UserDAO {
    User authenticate(String email, String password);
    boolean register(User user);
    User findById(int id);
    User findByEmail(String email);
    List<User> findAll();
    boolean update(User user);
    boolean updatePassword(int userId, String newPassword);
    boolean delete(int id);
    int countByType(String userType);
    int getEmployerIdByUserId(int userId);
    int createEmployer(int userId, String companyName, String industry);
}
