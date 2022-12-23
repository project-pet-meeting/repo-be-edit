package sideproject.petmeeting.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PetRequestDto {
    @NotEmpty(message = "이름을 입력해 주세요.")
    private String name;
    @NotNull(message = "나이를 입력해 주세요.")
    private Integer age;
    @NotNull(message = "몸무게를 입력해 주세요.")
    private Double weight;
    @NotEmpty(message = "종을 입력해 주세요.")
    private String species;
    @NotEmpty(message = "성별을 입력해 주세요.")
    private String gender;
}
