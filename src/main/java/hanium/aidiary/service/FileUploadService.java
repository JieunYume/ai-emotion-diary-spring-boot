package hanium.aidiary.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import hanium.aidiary.domain.File;
import hanium.aidiary.exception.CustomException;
import hanium.aidiary.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import static hanium.aidiary.handler.ErrorCode.USER_FILE_ERROR;

@RequiredArgsConstructor
@Service
public class FileUploadService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final long PRESIGNED_URL_EXPIRATION_MS = 1000 * 60 * 60; // 1시간

    private final AmazonS3 amazonS3;
    private final FileRepository fileRepository;

    public File uploadFile(MultipartFile media) {
        String mediaName = createFileName(media.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(media.getSize());
        objectMetadata.setContentType(media.getContentType());

        try (InputStream inputStream = media.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, media.getOriginalFilename(), inputStream, objectMetadata));
        } catch (IOException e) {
            throw new CustomException(USER_FILE_ERROR);
        }

        File file = File.builder()
                .origFilename(media.getOriginalFilename())
                .filename(mediaName)
                .fileType(media.getContentType())
                .fileUrl(media.getOriginalFilename()) // S3 키(파일명) 저장
                .build();

        return fileRepository.save(file);
    }

    // Presigned URL 생성 (1시간 유효)
    public String getPresignedUrl(String filename) {
        Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRATION_MS);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, filename)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(request).toString();
    }

    // S3에 저장되어있는 미디어 파일 삭제
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    public String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    public String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }
}
