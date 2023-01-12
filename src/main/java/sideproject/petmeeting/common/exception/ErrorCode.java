package sideproject.petmeeting.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "INVALID_INPUT_VALUE", "잘못된 입력값 입니다."),
    METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED", " 허용되지 않은 메서드 입니다."),
    ENTITY_NOT_FOUND(400, "ENTITY_NOT_FOUND", "개체를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "내부 서버 에러"),
    INVALID_TYPE_VALUE(400, "INVALID_TYPE_VALUE", "잘못된 유형 값"),
    HANDLE_ACCESS_DENIED(403, "HANDLE_ACCESS_DENIED", "접근 권한이 없습니다."),

    // == File Upload ==//
    FILE_NOT_EXIST(404, "FILE_NOT_EXIST", "파일이 없습니다. 파일을 추가해 주세요."),
    FILE_SIZE_EXCEED(413, "FILE_SIZE_EXCEED", "업로드 할 수 있는 파일 최대 크기는 20MB 입니다."),
    INVALID_FILE_TYPE(415, "INVALID_FILE_TYPE", "업로드 할 수 있는 파일 형식은 jpg, jpeg, png 입니다."),
    FILE_CONVERT_FAIL(400, "FILE_CONVERT_FAIL","MultipartFile -> File 변환 실패" ),

    // == Pet ==//
    PET_NOT_EXIST(404, "PET_NOT_EXIST", "반려동물 정보가 존재하지 않습니다."),

    // == Post ==//
    POST_NOT_EXIST(404, "POST_NOT_EXIST", "게시글이 존재하지 않습니다."),
    ALREADY_HEARTED(409, "ALREADY_HEARTED", "이미 좋아요를 누른 게시글 입니다."),
    HEART_NOT_FOUND(400, "HEART_NOT_FOUND", "좋아요를 누르지 않은 게시글 입니다."),
    KEYWORD_NOT_FOUND(400, "KEYWORD_NOT_FOUND", "키워드와 일치하는 검색 결과를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(400, "CATEGORY_NOT_FOUND", "해당 카테고리의 게시글을 찾을 수 없습니다."),

    // == Meeting ==//
    MEETING_NOT_EXIST(404, "MEETING_NOT_EXIST", "모임이 존재하지 않습니다."),
    ALREADY_ATTENDANCE_MEETING(409, "ALREADY_ATTENDANCE_MEETING", "이미 참여중인 모임입니다."),
    MEETING_RECRUIT_FULL(400, "MEETING_RECRUIT_FULL", "모집 인원이 마감되었습니다."),
    ATTENDANCE_NOT_EXIST(404, "ATTENDANCE_NOT_EXIST", "모임 참석 정보가 존재하지 않습니다."),
    ATTENDANCE_LIST_ACCESS_DENIED(403, "ATTENDANCE_LIST_ACCESS_DENIED", "참석자 리스트 접근 권한이 없습니다."),

    // == Token == //
    NEED_LOGIN(401, "NEED_LOGIN", "로그인이 필요합니다."),
    INVALID_TOKEN(404, "INVALID_TOKEN", "올바르지 않은 토큰입니다."),

    // == MyPage ==//
    MY_POST_NOT_EXIST(404, "MY_POST_NOT_EXIST", "내가 작성한 게시글이 존재하지 않습니다."),
    MY_MEETING_NOT_EXIST(404, "MY_MEETING_NOT_EXIST", "내가 만든 모임이 존재하지 않습니다."),
    MY_HEART_POST_NOT_EXIST(404, "MY_MEETING_NOT_EXIST", "내가 '좋아요'한 게시글이 존재하지 않습니다."),

    //== Member ==//
    MEMBER_NOT_EXIST(404, "MEMBER_NOT_EXIST", "회원이 존재 하지 않습니다."),

    //== Follow ==//
    FOLLOW_NOT_EXIST(404, "FOLLOW_NOT_EXIST", "팔로우 내역이 없습니다."),

    ;


    private final int status;
    private final String code;
    private final String message;

}
