package com.game.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SecurityFilter {
    private static final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 300;
    private static final long BLACKLIST_DURATION = 3600000; // 1 hour

    public static boolean isAllowed(String ip) {
        // Kiểm tra IP có trong blacklist không
        if (blacklist.containsKey(ip)) {
            long bannedTime = blacklist.get(ip);
            if (System.currentTimeMillis() - bannedTime > BLACKLIST_DURATION) {
                blacklist.remove(ip);
            } else {
                return false;
            }
        }

        // Đếm số request
        AtomicInteger count = requestCounts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            blacklist.put(ip, System.currentTimeMillis());
            return false;
        }

        return true;
    }

    // Reset counter mỗi phút
    public static void resetCounters() {
        requestCounts.clear();
    }
}