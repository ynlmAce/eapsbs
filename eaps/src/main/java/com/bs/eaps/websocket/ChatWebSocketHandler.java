package com.bs.eaps.websocket;

import com.bs.eaps.security.JwtUtil;
import com.bs.eaps.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.bs.eaps.dto.chat.ChatSendDTO;
import com.bs.eaps.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import com.bs.eaps.service.UserService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未提供token"));
            return;
        }
        try {
            String username = jwtUtil.extractUsername(token);
            // 通过UserService查库获取userId
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("用户不存在"));
                return;
            }
            userSessionMap.put(userId, session);
            session.getAttributes().put("userId", userId);
            log.info("WebSocket连接建立: userId={}", userId);
        } catch (Exception e) {
            log.error("WebSocket鉴权失败", e);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("token无效"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未认证用户"));
            return;
        }
        try {
            // 解析前端发送的JSON消息
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            Long sessionId = jsonNode.get("sessionId").asLong();
            String content = jsonNode.get("content").asText();
            String contentType = jsonNode.has("contentType") ? jsonNode.get("contentType").asText() : "text";

            ChatSendDTO sendDTO = new ChatSendDTO();
            sendDTO.setSessionId(sessionId);
            sendDTO.setContent(content);
            sendDTO.setContentType(contentType);

            // 调用服务层持久化消息
            Object result = chatService.sendChatMessage(userId, sendDTO);

            // 推送给所有会话参与方
            sendMessageToSessionParticipants(sessionId, result);
        } catch (Exception e) {
            log.error("WebSocket消息处理异常", e);
            session.sendMessage(new TextMessage("{\"error\":1,\"message\":\"消息处理失败\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessionMap.remove(userId);
            log.info("WebSocket连接关闭: userId={}", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输异常", exception);
        session.close(CloseStatus.SERVER_ERROR.withReason("WebSocket异常"));
    }

    private String getTokenFromSession(WebSocketSession session) {
        // 支持URL参数、Header等多种方式获取token
        String token = null;
        Map<String, Object> attributes = session.getAttributes();
        if (attributes.containsKey("token")) {
            token = (String) attributes.get("token");
        } else if (session.getUri() != null && session.getUri().getQuery() != null) {
            String[] params = session.getUri().getQuery().split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
        }
        return token;
    }

    // 可扩展：向指定用户推送消息
    public void sendMessageToUser(Long userId, String message) throws Exception {
        WebSocketSession session = userSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    // 推送消息给会话所有参与方
    public void sendMessageToSessionParticipants(Long sessionId, Object messageObj) throws Exception {
        java.util.List<Long> participantIds = chatService.getParticipantIdsBySessionId(sessionId);
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "chat_message");
        payload.put("sessionId", sessionId);
        payload.put("message", messageObj);

        String json = objectMapper.writeValueAsString(payload);

        for (Long userId : participantIds) {
            WebSocketSession ws = userSessionMap.get(userId);
            if (ws != null && ws.isOpen()) {
                ws.sendMessage(new TextMessage(json));
            }
        }
    }

    // 事件监听：收到消息发送事件后推送WebSocket
    @EventListener
    public void onChatMessageSent(ChatMessageSentEvent event) {
        log.info("监听到ChatMessageSentEvent: sessionId={}, message={}", event.getSessionId(), event.getMessageObj());
        try {
            sendMessageToSessionParticipants(event.getSessionId(), event.getMessageObj());
        } catch (Exception e) {
            log.warn("WebSocket事件推送失败", e);
        }
    }
}