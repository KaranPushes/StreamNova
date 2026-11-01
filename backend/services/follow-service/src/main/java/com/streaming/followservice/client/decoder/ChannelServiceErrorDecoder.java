package com.streaming.followservice.client.decoder;

import com.streaming.followservice.exception.ChannelNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class ChannelServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder =
            new Default();

    @Override
    public Exception decode(
            String methodKey,
            Response response) {

        if (response.status() == 404) {
            return new ChannelNotFoundException();
        }

        return defaultErrorDecoder.decode(
                methodKey,
                response
        );
    }
}