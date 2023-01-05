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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
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

//        File uploadFile = convert(image).orElseThrow(
//                () -> new BusinessException("MultipartFile -> File 변환 실패", ErrorCode.FILE_CONVERT_FAIL));


//        String fileName = imagePath + "/" + UUID.randomUUID() + uploadFile.getName();
        String fileName = imagePath + "/" + UUID.randomUUID() + image.getName() + "." + fileType;

        //        removeNewFile(uploadFile);  // 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

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


//    /**
//     * MultipartFile -> File 전환
//     * 전환 시 로컬에 파일 생성됨
//     * @param image : File 전환 할 MultipartFile
//     * @return : File
//     * @throws IOException : 예외 처리
//     */
//    private Optional<File> convert(MultipartFile image) throws IOException {
//        File convertFile = new File(Objects.requireNonNull(image.getOriginalFilename()));
//        // 지정된 경로에 파일 생성
//        if (convertFile.createNewFile()) {
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(image.getBytes());
//            }
//            return Optional.of(convertFile);
//        }
//        return Optional.empty();
//    }


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


//    /**
//     * MultipartFile -> File 전환하면서 생성된 로컬에 저장된 파일 삭제
//     * @param targetFile : File 전환하면서 생성된 로컬에 저장된 파일
//     */
//    private void removeNewFile(File targetFile) {
//        if(targetFile.delete()) {
//            log.info("파일이 삭제되었습니다.");
//        }else {
//            log.info("파일이 삭제되지 못했습니다.");
//        }
//    }

    /**
     * 이미지 파일 삭제
     * @param imageUrl : 삭제 할 파일명
     * @param imagePath : 이미지 경로
     */
    public void deleteImage(String imageUrl, String imagePath)  {
//        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8); //한글 인코딩
        log.info(imageUrl);
        String fileName =  imagePath + "/" + imageUrl.substring(imageUrl.lastIndexOf("/")+1);
        log.info(fileName);

        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

}

