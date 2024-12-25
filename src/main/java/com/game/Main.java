package com.game;

import com.game.socket.GameWebSocketServer;

public class Main {
    public static void main(String[] args) {
        // Khởi tạo và bắt đầu server WebSocket
        GameWebSocketServer gameWebSocketServer = new GameWebSocketServer();
        gameWebSocketServer.start();

        // In ra thông báo để biết server đã bắt đầu
        System.out.println("Game WebSocket Server started on ws://localhost:8080");

        // Bạn có thể thêm logic xử lý khác ở đây, như giám sát thời gian không hoạt động của client hoặc ghi log
        // Ví dụ: kiểm tra các client không hoạt động và xóa chúng mỗi phút
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Kiểm tra mỗi phút
                    GameWebSocketServer.checkInactiveClients();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }).start();
    }
}
