package sideproject.petmeeting.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    SHARE("공유"),
    HELP("도움"),
    RECOMMEND("추천"),

    TALK("잡담");

    final String value;
}
