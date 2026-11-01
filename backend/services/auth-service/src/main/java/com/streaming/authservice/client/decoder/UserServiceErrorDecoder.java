package com.streaming.authservice.client.decoder;

import com.streaming.authservice.exception.UserAlreadyExistsException;
import com.streaming.authservice.exception.UserNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class UserServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        return switch (response.status()) {

            case 404 -> new UserNotFoundException();

            case 409 -> new UserAlreadyExistsException();

            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}