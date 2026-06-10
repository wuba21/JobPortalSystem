package com.jobportal.service;

import com.jobportal.dao.NotificationDAO;
import com.jobportal.dao.impl.NotificationDAOImpl;
import com.jobportal.model.Notification;

import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAOImpl();
    }

    public boolean createNotification(int userId, String message) {
        return notificationDAO.createNotification(userId, message);
    }

    public List<Notification> getNotificationsByUser(int userId) {
        return notificationDAO.getNotificationsByUser(userId);
    }

    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    public boolean clearAllNotifications(int userId) {
        return notificationDAO.clearAllNotifications(userId);
    }

    public int countUnread(int userId) {
        return notificationDAO.countUnread(userId);
    }
}
