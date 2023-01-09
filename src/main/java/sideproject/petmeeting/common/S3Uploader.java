package sideproject.petmeeting.common;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor    // final 멤버변수가 있으면 생성자 항목에 포함시킴
@Service
public class S3Uploader {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 이미지 파일 저장
     * @param image : 저정할 이미지
     * @param imagePath : 저장 경로
     * @return : S3 업로드 된 파일  URL 주소 반환
     * @throws IOException : 예외 처리
     */
    public String upload(MultipartFile image, String imagePath) throws IOException{
        if (validateFileExist(image)) {
            throw new BusinessException("파일이 존재하지 않습니다.", ErrorCode.FILE_NOT_EXIST);
        }

        String fileType = Objects.requireNonNull(image.getOriginalFilename()).substring(image.getOriginalFilename().lastIndexOf(".")+1).toLowerCase();

        if(!validateFileType(Objects.requireNonNull(fileType))) {
            throw new BusinessException("지원하지 않은 파일 유형입니다.", ErrorCode.INVALID_FILE_TYPE);
        }

        String fileName = imagePath + "/" + UUID.randomUUID() + image.getName() + "." + fileType;

        return putS3(image, fileName, fileType);
    }

    private boolean validateFileExist(MultipartFile image) {
        return image.isEmpty();
    }

    private boolean validateFileType(String fileType) {
        return fileType.equals("png")
                || fileType.equals("bmp")
                || fileType.equals("jpg")
                || fileType.equals("jpeg");
    }


    /**
     * S3로 업로드
     * @param uploadFile : 업로드 할 파일
     * @param fileName : 파일명
     * @return : 업로드 된 이미지 URL
     */
    private String putS3(MultipartFile uploadFile, String fileName, String fileType) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileType);
        metadata.setContentLength(uploadFile.getSize());
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)    // PublicRead 권한으로 업로드(누구나 읽을 수 있음)
        );
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * 이미지 파일 삭제
     * @param imageUrl : 삭제 할 파일명
     * @param imagePath : 이미지 경로
     */
    public void deleteImage(String imageUrl, String imagePath)  {
        log.info(imageUrl);
        String fileName =  imagePath + "/" + imageUrl.substring(imageUrl.lastIndexOf("/")+1);
        log.info(fileName);

        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

}
