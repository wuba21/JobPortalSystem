package com.jobportal.service;

import com.jobportal.dao.MessageDAO;
import com.jobportal.dao.impl.MessageDAOImpl;
import com.jobportal.model.Message;
import com.jobportal.model.User;

import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO;

    public MessageService() {
        this.messageDAO = new MessageDAOImpl();
    }

    public boolean sendMessage(int senderId, int receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        Message msg = new Message(senderId, receiverId, content.trim());
        return messageDAO.sendMessage(msg);
    }

    public List<Message> getConversation(int user1Id, int user2Id) {
        return messageDAO.getConversation(user1Id, user2Id);
    }

    public List<User> getChatContacts(int userId) {
        return messageDAO.getChatContacts(userId);
    }

    public int getUnreadCount(int userId) {
        return messageDAO.getUnreadCount(userId);
    }

    public boolean markConversationAsRead(int senderId, int receiverId) {
        return messageDAO.markConversationAsRead(senderId, receiverId);
    }
}
