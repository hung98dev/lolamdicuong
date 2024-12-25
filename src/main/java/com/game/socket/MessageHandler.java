package com.game.socket;

import com.game.service.Client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {

    private static final long MESSAGE_INTERVAL_MS = 50; // Giới hạn tần suất xử lý tin nhắn (20 msg/s)
    private static final ConcurrentHashMap<String, Long> lastMessageTime = new ConcurrentHashMap<>();

    // Xử lý tin nhắn nhị phân từ client
    public static void handleBinaryMessage(Client client, ByteBuffer buffer) {
        try {
            // Đọc CMD (1 byte) và dữ liệu
            byte cmd = buffer.get();
            short dataLength = buffer.getShort();
            byte[] data = new byte[dataLength];
            buffer.get(data);

            long currentTime = System.currentTimeMillis();
            long lastTime = lastMessageTime.getOrDefault(client.getSessionId(), 0L);

            switch (cmd) {
                case 1: // CMD di chuyển
                    if (currentTime - lastTime >= MESSAGE_INTERVAL_MS) {
                        handleMoveMessage(client, data);
                        lastMessageTime.put(client.getSessionId(), currentTime);
                    }
                    break;
                case 2: // CMD tấn công
                    if (currentTime - lastTime >= MESSAGE_INTERVAL_MS) {
                        handleAttackMessage(client, data);
                        lastMessageTime.put(client.getSessionId(), currentTime);
                    }
                    break;
                case 3: // CMD chat
                    if (currentTime - lastTime >= MESSAGE_INTERVAL_MS * 2) {
                        handleChatMessage(client, data);
                        lastMessageTime.put(client.getSessionId(), currentTime);
                    }
                    break;
                default:
                    System.out.println("CMD không hợp lệ: " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xử lý tin nhắn di chuyển
    private static void handleMoveMessage(Client client, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        double posX = buffer.getDouble();
        double posY = buffer.getDouble();

        client.getSession().setPosX(posX);
        client.getSession().setPosY(posY);

        GameWebSocketServer.broadcastMovement(client, posX, posY);
    }

    // Xử lý tin nhắn tấn công
    private static void handleAttackMessage(Client client, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int targetId = buffer.getInt();
        System.out.println("Player " + client.getSessionId() + " attacked target " + targetId);
    }

    // Xử lý tin nhắn chat
    private static void handleChatMessage(Client client, byte[] data) {
        String chatMessage = new String(data);
        System.out.println("Player " + client.getSessionId() + ": " + chatMessage);
        GameWebSocketServer.broadcastChat(client, chatMessage);
    }
}
