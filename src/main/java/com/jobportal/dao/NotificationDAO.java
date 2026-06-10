package com.jobportal.dao;

import com.jobportal.model.Notification;
import java.util.List;

public interface NotificationDAO {
    boolean createNotification(int userId, String message);
    List<Notification> getNotificationsByUser(int userId);
    boolean markAsRead(int notificationId);
    boolean clearAllNotifications(int userId);
    int countUnread(int userId);
}
