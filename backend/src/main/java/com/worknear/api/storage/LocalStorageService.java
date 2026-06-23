package com.worknear.api.storage;

import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.config.WorkNearProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Mock storage: persists uploads to a local directory and serves them via
 * {@code /files/**}. Active whenever {@code worknear.storage.mock=true}.
 */
@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private final WorkNearProperties.Storage props;

    public LocalStorageService(WorkNearProperties properties) {
        this.props = properties.storage();
    }

    @Override
    public StoredFile store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        String storedName = UUID.randomUUID() + (ext != null ? "." + ext : "");
        try {
            Path dir = Paths.get(props.localDir(), folder).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            file.transferTo(target);
            String url = props.baseUrl() + "/" + folder + "/" + storedName;
            log.debug("Stored file {} -> {}", original, url);
            return new StoredFile(url, original);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }
}
