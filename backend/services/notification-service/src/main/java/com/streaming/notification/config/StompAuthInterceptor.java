package com.streaming.notification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)) {

            String authorization =
                    accessor.getFirstNativeHeader(AUTHORIZATION);

            if (authorization == null
                    || !authorization.startsWith("Bearer ")) {

                throw new IllegalArgumentException(
                        "Missing Authorization header"
                );
            }

            String token = authorization.substring(7);

            Jwt jwt = jwtDecoder.decode(token);

            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(jwt);

            accessor.setUser(authentication);

            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes()
                        .put(AUTHORIZATION, authorization);
            }

            accessor.setLeaveMutable(true);

            return MessageBuilder.createMessage(
                    message.getPayload(),
                    accessor.getMessageHeaders()
            );
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {

            if (accessor.getUser() == null) {

                String authorization = null;

                if (accessor.getSessionAttributes() != null) {
                    authorization =
                            (String) accessor.getSessionAttributes()
                                    .get(AUTHORIZATION);
                }

                if (authorization == null
                        || !authorization.startsWith("Bearer ")) {

                    throw new IllegalArgumentException(
                            "Missing authenticated WebSocket session"
                    );
                }

                String token = authorization.substring(7);

                Jwt jwt = jwtDecoder.decode(token);

                accessor.setUser(
                        new JwtAuthenticationToken(jwt)
                );
            }

            accessor.setLeaveMutable(true);

            return MessageBuilder.createMessage(
                    message.getPayload(),
                    accessor.getMessageHeaders()
            );
        }

        return message;
    }
}