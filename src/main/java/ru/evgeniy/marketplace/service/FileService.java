package ru.evgeniy.marketplace.service;

import org.springframework.web.multipart.MultipartFile;
import ru.evgeniy.marketplace.dto.ImageDTO;

public interface FileService {

    ImageDTO readImage(String path);

    ImageDTO writeImage(MultipartFile file, String directory);

    void removeImage(String path);
}