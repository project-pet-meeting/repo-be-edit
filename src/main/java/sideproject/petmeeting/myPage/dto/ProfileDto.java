package sideproject.petmeeting.myPage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import sideproject.petmeeting.pet.domain.Pet;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDto {
    private Long id;
    private String nickname;
    private String email;
    private String location;
    private String image;
    private List<Pet> pet;
}
