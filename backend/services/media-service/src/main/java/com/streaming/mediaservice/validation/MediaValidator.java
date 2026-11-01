package com.streaming.mediaservice.validation;

import com.streaming.mediaservice.config.MediaProperties;
import com.streaming.mediaservice.entity.MediaType;
import com.streaming.mediaservice.exception.MediaSizeExceededException;
import com.streaming.mediaservice.exception.MediaValidationException;
import com.streaming.mediaservice.exception.UnsupportedMediaTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MediaValidator {

    private final MediaProperties mediaProperties;

    public String validate(
            MultipartFile file,
            MediaType mediaType) {

        validateFilePresent(file);

        validateSize(file, mediaType);

        validateFilename(file);

        return validateImageContent(file);
    }

    private void validateFilePresent(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new MediaValidationException(
                    "Media file is required"
            );
        }
    }

    private void validateSize(
            MultipartFile file,
            MediaType mediaType) {

        long maximumSize = getMaximumSize(mediaType);

        if (file.getSize() > maximumSize) {

            throw new MediaSizeExceededException(
                    file.getSize(),
                    maximumSize
            );
        }
    }

    private long getMaximumSize(MediaType mediaType) {

        return switch (mediaType) {

            case AVATAR ->
                    mediaProperties
                            .getLimits()
                            .getAvatarMaxSize()
                            .toBytes();

            case BANNER ->
                    mediaProperties
                            .getLimits()
                            .getBannerMaxSize()
                            .toBytes();

            case STREAM_THUMBNAIL ->
                    mediaProperties
                            .getLimits()
                            .getStreamThumbnailMaxSize()
                            .toBytes();
        };
    }

    private String validateImageContent(
            MultipartFile file){

        try {

            String detectedType = detectImageType(file);

            if (!mediaProperties
                    .getAllowedImageTypes()
                    .contains(detectedType)) {

                throw new UnsupportedMediaTypeException(
                        detectedType
                );
            }

            /*
             * Verify that the file can actually be decoded
             * as an image for formats supported by ImageIO.
             */
            if (!detectedType.equals("image/webp")) {

                BufferedImage image =
                        ImageIO.read(file.getInputStream());

                if (image == null) {

                    throw new MediaValidationException(
                            "File is not a valid image"
                    );
                }
            }

            return detectedType;

        } catch (IOException exception) {

            throw new MediaValidationException(
                    "Unable to read image file"
            );
        }
    }

    private String detectImageType(
            MultipartFile file) throws IOException {

        try (InputStream inputStream =
                     file.getInputStream()) {

            byte[] header = inputStream.readNBytes(12);

            if (header.length >= 8 &&
                    header[0] == (byte) 0x89 &&
                    header[1] == 0x50 &&
                    header[2] == 0x4E &&
                    header[3] == 0x47 &&
                    header[4] == 0x0D &&
                    header[5] == 0x0A &&
                    header[6] == 0x1A &&
                    header[7] == 0x0A) {

                return "image/png";
            }

            if (header.length >= 3 &&
                    header[0] == (byte) 0xFF &&
                    header[1] == (byte) 0xD8 &&
                    header[2] == (byte) 0xFF) {

                return "image/jpeg";
            }

            if (header.length >= 12 &&
                    header[0] == 'R' &&
                    header[1] == 'I' &&
                    header[2] == 'F' &&
                    header[3] == 'F' &&
                    header[8] == 'W' &&
                    header[9] == 'E' &&
                    header[10] == 'B' &&
                    header[11] == 'P') {

                return "image/webp";
            }

            throw new UnsupportedMediaTypeException(
                    file.getContentType()
            );
        }
    }

    private void validateFilename(
            MultipartFile file) {

        String filename =
                file.getOriginalFilename();

        if (filename == null ||
                filename.isBlank()) {

            throw new MediaValidationException(
                    "Original filename is required"
            );
        }
    }
}