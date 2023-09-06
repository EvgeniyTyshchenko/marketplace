package ru.evgeniy.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import ru.evgeniy.marketplace.utils.exception.RuntimeIOException;

public class ImageDTO {

    @Schema(description = "Массив байтов, представляющий содержимое файла")
    public byte[] bytes;

    @Schema(description = "Тип медиафайла")
    public MediaType mediaType;

    @Schema(description = "Путь к файлу")
    public String path;

    public ImageDTO(byte[] bytes, MediaType mediaType, String path) {
        this.bytes = bytes;
        this.mediaType = mediaType;
        this.path = path;
    }

    public static MediaType toMediatype(String fileName) {
        if (fileName != null) {
            int index = fileName.lastIndexOf(".");
            String extension = fileName.substring(index);
            switch (extension) {
                case ".jpeg":
                case ".jpg":
                    return MediaType.IMAGE_JPEG;
                case ".png":
                    return MediaType.IMAGE_PNG;
            }
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        throw new RuntimeIOException(HttpStatus.BAD_REQUEST);
    }
}