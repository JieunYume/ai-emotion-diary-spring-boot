package hanium.aidiary.dto;

import hanium.aidiary.domain.Member;
import hanium.aidiary.domain.MemberType;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class MemberInfoResponse {
    Long id;
    String email;
    String name;
    MemberType type;
}
