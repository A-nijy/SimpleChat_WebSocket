package com.example.SimpleChat_WebSocket.config;


import com.example.SimpleChat_WebSocket.dto.WebSocketMessageDto;
import com.example.SimpleChat_WebSocket.enumeration.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.SimpleChat_WebSocket.enumeration.MessageType.JOIN;

@Component
@RequiredArgsConstructor
public class WebSocketSimpleChatHandler extends TextWebSocketHandler {

    // JSON 형식으로된 문자열과 객체 간의 변화를 편리하게 도와주는 용도
    private final ObjectMapper objectMapper;

    // Set을 통해 웹소켓 세션을 저장하여 관리하는 용도
    // 즉, 웹소켓 연결한 모든 사용자(클라이언트 세션)를 관리하기 위함
    // WebSocketSession은 클라이언트(사용자)와의 웹소켓 연결에 대한 정보를 나타내는 객체 (웹소켓 통신(송수신)에 사용된다.)
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // MAP을 통해 방(그룹)과 해당 방에 참여 중인 사용자를 관리하는 용도
    // 즉, 특정 방에 참여 중인 사용자(클라이언트 세션)들을 관리하기 위함
    // Long = 방 코드(고유 id), Set<WebSocketSession> = 해당 방에 참여중인 사용자들의 정보
    private final Map<Long, Set<WebSocketSession>> roomSessionMap = new HashMap<>();


    // 소켓 연결 확인 메서드 (소켓 연결 성공 시 호출되는 메서드)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Set<세션>에 현재 연결된 사용자 세션을 추가
        sessions.add(session);
    }

    // 소켓 요청(메시지) 처리 메서드 (소켓 연결 이후 메시지를 수신할 때 호출되는 메서드)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // 클라이언트가 보낸 데이터(JSON 형태의 문자열)를 따로 가져오기
        String data = message.getPayload();

        // 위에서 가져온 data를 바로 "WebSocketMessageDto" 객체로 변환
        WebSocketMessageDto webSocketMessageDto = objectMapper.readValue(data, WebSocketMessageDto.class);

        // 메시지에서 type값에 따라 동작 분류 (JOIN = 방 참여, LEAVE = 방 나가기, MESSAGE = 데이터 전달)
        switch (webSocketMessageDto.getType()){
            case JOIN:
                // 방 코드 가져오기
                Long roomNumber = webSocketMessageDto.getRoomNumber();
                // Map에 해당 방 코드를 사용하는 방이 있는지 찾아 해당 방과 매핑된 WebSocketSession들을 가져온다.
                Set<WebSocketSession> sessionRoom = roomSessionMap.get(roomNumber);

                // 만약 해당 방과 매핑된 WebSocketSession이 존재하지 않는다면? (= 해당 방이 존재하지 않는다면)
                if (sessionRoom == null){
                    // 빈 Set<WebSocketSession>를 생성
                    sessionRoom = new HashSet<>();
                    // Map에 해당 코드의 방과 Set<WebSocketSession>을 매핑하여 추가
                    roomSessionMap.put(roomNumber, sessionRoom);
                }
                // 해당 방과 매핑된 WebSocketSession에 사용자 세션(정보)를 추가하여 해당 방에 참여시킨다.
                sessionRoom.add(session);
                // 방 입장하는 메시지 담기
                webSocketMessageDto.setMessage("사용자가 입장했습니다.");
                break;

            case LEAVE:
                // Map에서 현재 사용자의 방 번호인 MAP을 찾고 해당 방과 매핑된 Sessions에서 해당 사용자 세션을 제거
                roomSessionMap.get(webSocketMessageDto.getRoomNumber())
                        .remove(session);
                // 방 나가는 메시지 담기
        }

        // 데이터(메시지) 전송
        // 특정 방과 매핑된 모든 세션을 가져옴
        Set<WebSocketSession> sessionsInRoom = roomSessionMap.get(webSocketMessageDto.getRoomNumber());

        //만약 해당 방과 매핑된 세션(사용자)가 존재한다면
        if(sessionsInRoom != null){
            for (WebSocketSession webSocketSession : sessionsInRoom) {
                // mapper를 이용하여 DTO를 JSON 문자열로 변환
                String messagePayload = objectMapper.writeValueAsString(webSocketMessageDto);

                // JSON 문자열을 TextMessage에 담는다.
                TextMessage textMessage = new TextMessage(messagePayload);
                // TextMessage를 세션에 메시지로 전송한다.
                webSocketSession.sendMessage(textMessage);
            }
        }
    }

    // 소켓 연결 종료 처리 메서드 (WebSocket 연결이 종료될 때 호출되는 메서드)
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 웹소켓 연결된 모든 사용자를 관리하는 세션에서 해당 사용자 세션을 제거
        sessions.remove(session);
        // 연결 종료 메시지를 클라이언트에게 전달
        session.sendMessage(new TextMessage("웹소켓 연결 종료"));
    }
}
