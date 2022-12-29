package sideproject.petmeeting.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PetResponseDto {

    private Long id;
    private String name;
    private Integer age;
    private Double weight;
    private String species;
    private String gender;
    private String imageUrl;
    private Long memberId;
    private String memberNickname;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
