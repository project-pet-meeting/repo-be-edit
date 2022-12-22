package sideproject.petmeeting.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    RECOMMEND("추천"),
    FREEPRESENT("무료나눔"),
    FRIEND("친구맺기"),
    ETC("기타");

    final String value;
}
