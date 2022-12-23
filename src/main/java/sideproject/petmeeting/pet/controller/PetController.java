package sideproject.petmeeting.pet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.pet.dto.PetRequestDto;
import sideproject.petmeeting.pet.dto.PetResponseDto;
import sideproject.petmeeting.pet.service.PetService;
import sideproject.petmeeting.security.UserDetailsImpl;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/pet", produces = HAL_JSON_VALUE)
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<Object> createPet(@RequestPart(value = "data") @Valid PetRequestDto petRequestDto, // @valid 객체 검증 수행
                                             @RequestPart(value = "image" ,required = false) @Valid MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        PetResponseDto petResponseDto = petService.createPet(petRequestDto, image, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(petResponseDto);
        responseResource.add(linkTo(PetController.class).withSelfRel());
        responseResource.add(linkTo(PetController.class).slash(petResponseDto.getId()).withRel("pet-get"));
        responseResource.add(linkTo(PetController.class).slash(petResponseDto.getId()).withRel("pet-edit"));
        responseResource.add(linkTo(PetController.class).slash(petResponseDto.getId()).withRel("pet-delete"));

        Response response = new Response(StatusEnum.CREATED, "반려동물 정보 저장 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<Object> getPet(@PathVariable Long petId) {
        PetResponseDto petResponseDto = petService.getPet(petId);

        ResponseResource responseResource = new ResponseResource(petResponseDto);
        responseResource.add(linkTo(PetController.class).slash(petResponseDto.getId()).withSelfRel());

        Response response = new Response(StatusEnum.OK, "반려동물 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{petId}")
    public ResponseEntity<Object> updatePet(@PathVariable Long petId,
                                             @RequestPart(value = "data") @Valid PetRequestDto petRequestDto,
                                             @RequestPart(value = "image" ,required = false) @Valid MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        PetResponseDto petResponseDto = petService.updatePet(petId, petRequestDto, image, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(petResponseDto);
        responseResource.add(linkTo(PetController.class).slash(petResponseDto.getId()).withSelfRel());

        Response response = new Response(StatusEnum.OK, "반려동물 정보 수정 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping( "/{petId}")
    public ResponseEntity<Object> deletePet(@PathVariable Long petId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        petService.petDelete(petId, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(null);
        responseResource.add(linkTo(PetController.class).slash(petId).withSelfRel());

        Response response = new Response(StatusEnum.OK, "반려동물 정보 삭제 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
