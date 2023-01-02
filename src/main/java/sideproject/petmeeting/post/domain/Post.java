package sideproject.petmeeting.post.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.post.dto.PostRequestDto;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(value = STRING)
    private Category category;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    @NotEmpty
    @Size(max = 2000)
    private String imageUrl;

    @ColumnDefault("0")
    private Integer numHeart;

    @JsonIgnore
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private Member member;

    @JsonIgnore
    @JoinColumn(name = "post_id")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HeartPost> heartPost;

    @ColumnDefault("0")
    private Integer viewCnt;


    /**
     * 게시글 수정
     * @param postRequestDto
     * @param imageUrl
     */
    public void update(PostRequestDto postRequestDto, String imageUrl) {
        this.category = postRequestDto.getCategory();
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.imageUrl =imageUrl;
    }

    /**
     * 좋아요 합계 저장
     * @param numHeart
     */
    public void addCountHeart(Integer numHeart) {
        this.numHeart = numHeart;
    }

    public void viewCnt() {
        this.viewCnt++;
    }



}
