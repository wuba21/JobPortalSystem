package com.jobportal.dao;

import com.jobportal.model.Message;
import com.jobportal.model.User;
import java.util.List;

public interface MessageDAO {
    boolean sendMessage(Message message);
    List<Message> getConversation(int user1Id, int user2Id);
    List<User> getChatContacts(int userId);
    int getUnreadCount(int userId);
    boolean markConversationAsRead(int senderId, int receiverId);
}
