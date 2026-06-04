package com.jhl.silver.union.web.websocket;

import com.jhl.silver.union.commons.gson.GsonHelper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class CustNoticeWebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (!(userIdObj instanceof Long)) {
            closeQuietly(session);
            return;
        }
        Long userId = (Long) userIdObj;
        userSessions.computeIfAbsent(userId, key -> new CopyOnWriteArraySet<>()).add(session);
        sessionUserMap.put(session.getId(), userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session);
    }

    public void sendToUser(Long userId, CustNoticeWsMessage message) {
        if (userId == null || message == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String payload = GsonHelper.toJson(message, true);
        TextMessage textMessage = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                log.warn("Send ws notice failed, sessionId={}", session.getId());
                removeSession(session);
            }
        }
    }

    private void removeSession(WebSocketSession session) {
        if (session == null) {
            return;
        }
        Long userId = sessionUserMap.remove(session.getId());
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    private void closeQuietly(WebSocketSession session) {
        if (session == null) {
            return;
        }
        try {
            session.close();
        } catch (IOException e) {
            log.warn("Close ws session failed, sessionId={}", session.getId());
        }
    }
}
