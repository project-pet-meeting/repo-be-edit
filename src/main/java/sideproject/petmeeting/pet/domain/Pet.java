package sideproject.petmeeting.pet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.pet.dto.PetRequestDto;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Pet extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotEmpty
    private String name;
    private Integer age;
    private Double weight;
    private String species;
    private String gender;

    @Size(max = 2000)
    private String imageUrl;

    @JsonIgnore
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private Member member;

    public void update(PetRequestDto petRequestDto, String imageUrl) {
        this.name = petRequestDto.getName();
        this.age = petRequestDto.getAge();
        this.weight = petRequestDto.getWeight();
        this.species = petRequestDto.getSpecies();
        this.gender = petRequestDto.getGender();
        this.imageUrl = imageUrl;
    }
}
