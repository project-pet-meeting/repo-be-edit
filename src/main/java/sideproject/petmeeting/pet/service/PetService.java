package sideproject.petmeeting.pet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.S3Uploader;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.pet.domain.Pet;
import sideproject.petmeeting.pet.dto.PetRequestDto;
import sideproject.petmeeting.pet.dto.PetResponseDto;
import sideproject.petmeeting.pet.repository.PetRepository;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class PetService {

    private final PetRepository petRepository;
    private final S3Uploader s3Uploader;


    @Transactional
    public PetResponseDto createPet(PetRequestDto petRequestDto, MultipartFile image, Member member) throws IOException {
        String imageUrl = s3Uploader.upload(image, "pet/image");

        Pet pet = Pet.builder()
                .name(petRequestDto.getName())
                .age(petRequestDto.getAge())
                .species(petRequestDto.getSpecies())
                .gender(petRequestDto.getGender())
                .weight(petRequestDto.getWeight())
                .imageUrl(imageUrl)
                .member(member)
                .build();
        petRepository.save(pet);

        return getPetResponseDto(pet);
    }

    @Transactional(readOnly = true)
    public PetResponseDto getPet(Long petId) {
        Pet pet = petRepository.findPetFetchJoin(petId).orElseThrow(
                () -> new BusinessException("존재하지 않는 반려동물 id 입니다.", ErrorCode.PET_NOT_EXIST)
        );

        return getPetResponseDto(pet);
    }

    @Transactional
    public PetResponseDto updatePet(Long petId, PetRequestDto petRequestDto, MultipartFile image, Member member) throws IOException {
        Pet pet = petRepository.findById(petId).orElseThrow(
                () -> new BusinessException("존재하지 않는 반려동물 id 입니다.", ErrorCode.PET_NOT_EXIST)
        );

        if (!pet.getMember().getId().equals(member.getId())) {
            throw new BusinessException("수정 권한이 없습니다.", ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imageUrl = pet.getImageUrl();

        // 이미지 존재 시 삭제 후 업로드
        if (imageUrl != null) {
            s3Uploader.deleteImage(imageUrl);
        }

        imageUrl = s3Uploader.upload(image, "pet/image");
        pet.update(petRequestDto, imageUrl);

        return getPetResponseDto(pet);
    }

    @Transactional
    public void petDelete(Long petId, Member member) {
        Pet pet = petRepository.findById(petId).orElseThrow(
                () -> new BusinessException("존재하지 않는 반려동물 id 입니다.", ErrorCode.PET_NOT_EXIST)
        );

        if (!pet.getMember().getId().equals(member.getId())) {
            throw new BusinessException("삭제 권한이 없습니다.", ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imageUrl = pet.getImageUrl();

        if (imageUrl != null) {
            s3Uploader.deleteImage(imageUrl);
        }

        petRepository.deleteById(petId);
    }

    private PetResponseDto getPetResponseDto(Pet pet) {

        return PetResponseDto.builder()
                .id(pet.getId())
                .name(pet.getName())
                .age(pet.getAge())
                .weight(pet.getWeight())
                .species(pet.getSpecies())
                .gender(pet.getGender())
                .imageUrl(pet.getImageUrl())
                .memberId(pet.getMember().getId())
                .memberNickname(pet.getMember().getNickname())
                .createdAt(pet.getCreatedAt())
                .modifiedAt(pet.getModifiedAt())
                .build();
    }
}
