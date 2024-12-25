package com.game.socket;

import com.game.service.Client;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketServer {
    private static final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
    private static final long CLIENT_TIMEOUT = 60 * 1000; // Thời gian chờ (60 giây) để xóa client không hoạt động
    private static final ConcurrentHashMap<String, Long> ipRequestTimestamps = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_SECOND = 10; // Giới hạn số lượng yêu cầu từ một IP mỗi giây

    public void start() {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(Handlers.websocket(new WebSocketConnectionCallback() {
                    @Override
                    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                        String sessionId = generateSessionId();
                        Client client = new Client(sessionId, channel);
                        clients.put(sessionId, client);

                        // In ra tổng số kết nối hiện tại
                        System.out.println("New connection. Total connections: " + clients.size());

                        // Lấy địa chỉ IP từ WebSocketChannel
                        SocketAddress socketAddress = channel.getPeerAddress();
                        String clientIP = socketAddress.toString().split(":")[0].replace("/", "");  // Lấy IP từ SocketAddress

                        long currentTime = System.currentTimeMillis();
                        Long lastRequestTime = ipRequestTimestamps.getOrDefault(clientIP, 0L);

                        if (currentTime - lastRequestTime < 10000) { // Nếu có quá nhiều kết nối trong 1 giây, ngừng kết nối
                            System.out.println("IP " + clientIP + " đang tấn công DDoS, từ chối kết nối.");
                            try {
                                channel.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        ipRequestTimestamps.put(clientIP, currentTime);

                        channel.getReceiveSetter().set(new AbstractReceiveListener() {
                            private void onFullBinaryMessage(WebSocketChannel channel, StreamSourceFrameChannel messageChannel) {
                                // Cập nhật thời gian hoạt động của client khi nhận tin nhắn
                                client.updateLastActiveTime();

                                // Chuyển StreamSourceFrameChannel thành ByteBuffer và xử lý
                                ByteBuffer buffer = ByteBuffer.allocate(1024);  // Kích thước tối đa của tin nhắn (tùy chỉnh)
                                try {
                                    int bytesRead;
                                    while ((bytesRead = messageChannel.read(buffer)) > 0) {
                                        buffer.flip();  // Đảo ngược buffer để đọc dữ liệu
                                        MessageHandler.handleBinaryMessage(client, buffer);
                                        buffer.clear();  // Xóa buffer sau khi đã xử lý
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            protected void onClose(WebSocketChannel channel, StreamSourceFrameChannel sourceChannel) {
                                // Xóa client khi kết nối WebSocket đóng
                                clients.remove(client.getSessionId());

                                // In ra tổng số kết nối còn lại
                                System.out.println("Connection closed. Total connections: " + clients.size());
                            }

                            @Override
                            protected void onError(WebSocketChannel channel, Throwable throwable) {
                                // Xử lý khi có lỗi xảy ra trong kết nối và xóa client
                                System.err.println("Error with client " + client.getSessionId() + ": " + throwable.getMessage());
                                clients.remove(client.getSessionId());

                                // In ra tổng số kết nối còn lại khi có lỗi
                                System.out.println("Error occurred. Total connections: " + clients.size());
                            }
                        });

                        channel.resumeReceives();
                    }
                }))
                .build();

        server.start();
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    // Phương thức để gửi tin nhắn di chuyển
    public static void broadcastMovement(Client client, double posX, double posY) {
        ByteBuffer buffer = ByteBuffer.allocate(17);
        buffer.put((byte) 1);  // CMD di chuyển
        buffer.putLong(client.getSessionId().hashCode());  // sessionId dưới dạng long
        buffer.putDouble(posX);
        buffer.putDouble(posY);
        buffer.flip();

        broadcast(buffer);
    }

    // Phương thức để gửi tin nhắn chat
    public static void broadcastChat(Client client, String message) {
        ByteBuffer buffer = ByteBuffer.allocate(3 + message.length());
        buffer.put((byte) 3);  // CMD chat
        buffer.putShort((short) message.length());
        buffer.put(message.getBytes());
        buffer.flip();

        broadcast(buffer);
    }

    private static void broadcast(ByteBuffer buffer) {
        clients.values().forEach(client -> {
            WebSocketChannel channel = client.getChannel();
            if (channel.isOpen()) {
                WebSockets.sendBinary(buffer.duplicate(), channel, null);
            }
        });
    }

    // Kiểm tra thời gian không hoạt động và xóa client
    public static void checkInactiveClients() {
        long currentTime = System.currentTimeMillis();
        clients.entrySet().removeIf(entry -> {
            Client client = entry.getValue();
            return (currentTime - client.getLastActiveTime()) > CLIENT_TIMEOUT;  // Nếu client không hoạt động lâu quá
        });
    }
}
