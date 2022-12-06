package sideproject.petmeeting.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice// Global Level using, 에러 전역 처리를 위한 어노테이션(Controller, Service 에서 throw 한 예외를 일괄 처리
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 요구사항에 따른 Exception
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException e) {
        log.error("handleEntityNotFoundException", e);
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse errorResponse = ErrorResponse.of(errorCode);

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorCode.getStatus()));
    }


    /**
     * Handler 에서 예외처리 되지 않은 Exception 처리
     * @param e
     * @return
     */
//    @ExceptionHandler(Exception.class)
//    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
//        log.error("handleException", e);
//        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
//
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }


    /**
     * 이미지 파일 업로드 용량 초과 시 발생하는 에러 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("handleMaxUploadSizeExceededException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEED);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
