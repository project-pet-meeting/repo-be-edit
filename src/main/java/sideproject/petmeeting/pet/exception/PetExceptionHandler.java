package sideproject.petmeeting.pet.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.common.exception.ErrorResponse;
import sideproject.petmeeting.pet.controller.PetController;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

@RestControllerAdvice(basePackageClasses = PetController.class) // Controller Level using, 에러 처리를 위한 어노테이션
@Order(Ordered.HIGHEST_PRECEDENCE) // 전역 예외 처리보다 먼저 처리되게 순서 지정
@Slf4j
public class PetExceptionHandler {

    /**
     * @Valid 유효성 체크에 통과하지 못하면 MethodArgumentNotValidException 발생
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        final ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());
        // Of 메서드:Null 값을 입력 받을 시 NullPointerException
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse>handleConstraintViolationException(ConstraintViolationException e) {
        log.error("handleConstraintViolationException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());

        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
    }

//    /**
//     * Handler 에서 예외처리 되지 않은 Exception 처리
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(Exception.class)
//    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
//        log.error("handleException", e);
//        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
//
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(IOException.class)
    protected ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        log.error("handleIOException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * RequestPart 데이터가 없을 시 발생하는 에러 예외 처리
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.error("handleMissingServletRequestPartException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * request parameter 가 없을 시 발생하는 에러 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("handleMissingServletRequestParameterException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



    /**
     * 지원하지 않은 HTTP method 호출 시 발생하는 에러 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 지원하지 않은 Content Type 으로 호출 시 발생하는 에러 예외 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE, e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * enum type 일치하지 않아서 발생하는 에러 예외 처리
     */
    @ExceptionHandler(InvalidFormatException.class)
    protected ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException e) {
        log.error("handleInvalidFormatException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE,e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



//    /**
//    * Authentication 객체가 필요한 권한을 보유하지 않은 경우 발생합
//     */
//    @ExceptionHandler(AccessDeniedException.class)
//    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
//        log.error("handleAccessDeniedException", e);
//        final ErrorResponse response = ErrorResponse.of(ErrorCode.HANDLE_ACCESS_DENIED);
//
//        return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.HANDLE_ACCESS_DENIED.getStatus()));
//    }



}

