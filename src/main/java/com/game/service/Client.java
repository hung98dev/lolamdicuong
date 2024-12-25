package com.game.service;

import io.undertow.websockets.core.WebSocketChannel;

public class Client {
    private final String sessionId;
    private final WebSocketChannel channel;
    private final Session session;
    private long lastActiveTime;  // Thời gian hoạt động cuối cùng

    public Client(String sessionId, WebSocketChannel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.session = new Session();
        this.lastActiveTime = System.currentTimeMillis();  // Khởi tạo thời gian hoạt động
    }

    public String getSessionId() {
        return sessionId;
    }

    public WebSocketChannel getChannel() {
        return channel;
    }

    public Session getSession() {
        return session;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();  // Cập nhật thời gian hoạt động khi nhận tin nhắn mới
    }
}
