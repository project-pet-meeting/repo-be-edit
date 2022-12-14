package sideproject.petmeeting.pet.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.pet.domain.Pet;
import sideproject.petmeeting.pet.dto.PetRequestDto;
import sideproject.petmeeting.pet.dto.PetResponseDto;
import sideproject.petmeeting.pet.repository.PetRepository;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class PetServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetService petService;

    private static final String USERNAME = "petService@Username.com";
    private static final String PASSWORD = "password";

    @BeforeEach
    public void setup() {

        Member member = Member.builder()
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .image("test-image")
                .location("??????")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @AfterEach
    public void after() {
        petRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @Transactional
    @DisplayName("???????????? ?????? ?????? ????????? - ????????????")
    public void createPetTest() throws IOException {
        // Given
        PetRequestDto petRequestDto = PetRequestDto.builder()
                .name("?????????")
                .age(1)
                .weight(2.5)
                .species("?????????")
                .gender("???")
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "jjang.png",
                "image/png",
                "<<png data>>".getBytes());

        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        // When
        PetResponseDto savedPet = petService.createPet(petRequestDto, image, savedMember);

        // Then
        assertThat(savedPet.getName()).isEqualTo("?????????");
        assertThat(savedPet.getAge()).isEqualTo(1);
        assertThat(savedPet.getWeight()).isEqualTo(2.5);
        assertThat(savedPet.getSpecies()).isEqualTo("?????????");
        assertThat(savedPet.getGender()).isEqualTo("???");
    }


    @Test
    @Transactional
    @DisplayName("???????????? ?????? ????????? - ????????????")
    public void getPetTest() {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("?????????")
                .age(2)
                .weight(3.5)
                .species("?????????")
                .gender("???")
                .imageUrl("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        // When
        PetResponseDto savedPet = petService.getPet(pet.getId());

        // Then
        assertThat(savedPet.getName()).isEqualTo("?????????");
        assertThat(savedPet.getAge()).isEqualTo(2);
        assertThat(savedPet.getWeight()).isEqualTo(3.5);
        assertThat(savedPet.getSpecies()).isEqualTo("?????????");
        assertThat(savedPet.getGender()).isEqualTo("???");
    }

    @Test
    @Transactional
    @DisplayName("???????????? ?????? ?????? ????????? - ?????? ??????")
    public void updatePetTest() throws IOException {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("?????????")
                .age(2)
                .weight(3.5)
                .species("?????????")
                .gender("???")
                .gender("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        PetRequestDto petRequestDto = PetRequestDto.builder()
                .name("?????????")
                .age(3)
                .weight(4.5)
                .species("?????????")
                .gender("???")
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "memberImage.jpeg",
                "image/jpeg",
                "<<jpeg data>>".getBytes());

        // When
        PetResponseDto savedPet = petService.updatePet(pet.getId(), petRequestDto, image, savedMember);

        // Then
        assertThat(savedPet.getName()).isEqualTo("?????????");
        assertThat(savedPet.getAge()).isEqualTo(3);
        assertThat(savedPet.getWeight()).isEqualTo(4.5);
        assertThat(savedPet.getSpecies()).isEqualTo("?????????");
        assertThat(savedPet.getGender()).isEqualTo("???");
    }

    @Test
    @Transactional
    @DisplayName("???????????? ?????? ?????? ????????? - ????????????")
    public void deletePetTest() {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("?????????")
                .age(2)
                .weight(3.5)
                .species("?????????")
                .gender("???")
                .gender("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        // When
        petService.petDelete(pet.getId(), savedMember);

        // Then
        assertThat(petRepository.findById(pet.getId())).isEmpty();


    }

}