package com.worknear.api.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over object storage. The mock implementation writes to the local
 * filesystem; swap in an S3/R2-backed implementation later without touching callers.
 */
public interface StorageService {

    /**
     * Stores a file under the given logical folder and returns a publicly resolvable URL.
     */
    StoredFile store(MultipartFile file, String folder);

    record StoredFile(String url, String originalName) {}
}
