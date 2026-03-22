package hanium.aidiary.dto.like;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeRequestDTO {
    private Long diaryId;

    public LikeRequestDTO(Long diaryId) {
        this.diaryId = diaryId;
    }
}
