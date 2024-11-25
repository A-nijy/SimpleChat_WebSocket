package com.example.SimpleChat_WebSocket.dto;

import com.example.SimpleChat_WebSocket.enumeration.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessgeDto {   // 웹소켓 통신에서 주고받을 DTO

    private MessageType type;       // JOIN(입장), LEAVE(나가기), MESSAGE(데이터 전달)
    private Long RoomNumber;        // 특정 유저들과 소통할 방(공간)의 고유 번호
    private String sender;          // 해당 메시지를 전달하는 수신자
    private String message;         // 전달할 메시지
}
