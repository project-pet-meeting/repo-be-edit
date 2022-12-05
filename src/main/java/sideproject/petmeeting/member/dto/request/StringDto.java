package sideproject.petmeeting.member.dto.request;

import lombok.Getter;

@Getter
public class StringDto {
    private String string;

    public StringDto(String string) {
        this.string = string;
    }
}
