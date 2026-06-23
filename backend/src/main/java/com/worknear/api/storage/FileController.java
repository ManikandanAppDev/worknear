package com.worknear.api.storage;

import com.worknear.api.config.WorkNearProperties;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves files persisted by the mock {@link LocalStorageService}.
 */
@Hidden
@RestController
@RequestMapping("/files")
public class FileController {

    private final Path root;

    public FileController(WorkNearProperties properties) {
        this.root = Paths.get(properties.storage().localDir()).toAbsolutePath().normalize();
    }

    @GetMapping("/{folder}/{name}")
    public ResponseEntity<Resource> serve(@PathVariable String folder, @PathVariable String name) throws Exception {
        Path file = root.resolve(folder).resolve(name).normalize();
        if (!file.startsWith(root)) {
            return ResponseEntity.badRequest().build();
        }
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
