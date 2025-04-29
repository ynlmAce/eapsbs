package com.bs.eaps.websocket;

import org.springframework.context.ApplicationEvent;

public class ChatMessageSentEvent extends ApplicationEvent {
    private final Long sessionId;
    private final Object messageObj;

    public ChatMessageSentEvent(Object source, Long sessionId, Object messageObj) {
        super(source);
        this.sessionId = sessionId;
        this.messageObj = messageObj;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Object getMessageObj() {
        return messageObj;
    }
}