package com.example.SimpleChat_WebSocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration              // Spring의 설정 클래스임을 정의
@EnableWebSocket            // Spring에서 WebSocket 지원을 활성화 (SebSocket 관련 컴포넌트인 WebSocketHandler, WebSocketConfigurer 등을 스캔하고 설정하도록 함)
@RequiredArgsConstructor    // final로 선언된 필드를 매개변수로 받는 생성자를 자동으로 생성
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    // WebSocket 연결을 처리하기 위한 "엔드포인트"와 "WebSocket 핸들러"를 설정하는 메서드 (매개변수 WebSocketHandlerRegistry는 WebSocket 핸들러를 등록할 수 있는 API로 엔드포인트와 CORS 설정등을 함께 정의)
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                // /ws/connect 경로로 WebSocket 연결을 허용
                .addHandler(webSocketHandler, "/ws/connect")
                // CORS 허용 (어떤 도메인에서든 WebSocket 연결 허용)
                .setAllowedOrigins("*");
    }
}
