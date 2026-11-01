package com.streaming.chat.exception;

import com.streaming.chat.dto.ChatErrorResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(ChatException.class)
    @SendTo("/topic/chat.errors")
    public ChatErrorResponse handleChatException(
            ChatException exception
    ) {
        return new ChatErrorResponse(
                exception.getCode(),
                exception.getMessage()
        );
    }

    @MessageExceptionHandler(Exception.class)
    @SendTo("/topic/chat.errors")
    public ChatErrorResponse handleException(Exception exception) {
        return new ChatErrorResponse(
                "CHAT_ERROR",
                "Unable to process chat message"
        );
    }
}