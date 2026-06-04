package com.jhl.silver.union.web.websocket;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class CustNoticeWebSocketConfig implements WebSocketConfigurer {
    @Resource
    private CustNoticeWebSocketHandler webSocketHandler;
    @Resource
    private CustNoticeHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/notice")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
