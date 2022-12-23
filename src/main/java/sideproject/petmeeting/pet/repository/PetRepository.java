package sideproject.petmeeting.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sideproject.petmeeting.pet.domain.Pet;

import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    // 반려동물 조회
    @Query("select p from Pet p left join fetch p.member where p.id = :petId")
    Optional<Pet> findPetFetchJoin(@Param("petId")Long petId);
}
