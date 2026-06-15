package com.shopping.controller;

import com.shopping.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostMapping
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "type", defaultValue = "products") String type) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error(400, "文件大小不能超过10MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return Result.error(400, "仅支持 JPG/PNG/GIF/WebP 格式");
        }

        String subDir = switch (type) {
            case "reviews" -> "reviews";
            case "products" -> "products";
            case "shops" -> "shops";
            default -> "products";
        };

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path uploadPath = Paths.get(uploadDir, subDir);
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + subDir + "/" + newFilename;
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    @PostMapping("/multiple")
    public Result<List<String>> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        if (files.length > 5) {
            return Result.error(400, "最多上传5张图片");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            Result<String> result = uploadFile(file);
            if (result.getCode() != 200) {
                return Result.error(result.getCode(), result.getMessage());
            }
            urls.add(result.getData());
        }
        return Result.success(urls);
    }
}
