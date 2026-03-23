package hanium.aidiary;

import hanium.aidiary.domain.*;
import hanium.aidiary.dto.calendar.group.GroupCalendarDetailResponse;
import hanium.aidiary.repository.DiaryRepository;
import hanium.aidiary.repository.FileRepository;
import hanium.aidiary.repository.GroupRepository;
import hanium.aidiary.repository.MemberRepository;
import hanium.aidiary.service.DiaryService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class N1QueryVerificationTest {

    @Autowired DiaryService diaryService;
    @Autowired DiaryRepository diaryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired GroupRepository groupRepository;
    @Autowired FileRepository fileRepository;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    // ===== 공통 데이터 생성 메서드 =====
    private List<Member> createTestData(int memberCount) {
        Group group = groupRepository.save(Group.builder()
                .name("테스트그룹")
                .invitationCode("TESTCODE1")
                .build());

        List<Member> members = new ArrayList<>();
        LocalDateTime targetDate = LocalDateTime.of(2024, 3, 15, 10, 0);

        for (int i = 1; i <= memberCount; i++) {
            File file = fileRepository.save(File.builder()
                    .origFilename("profile" + i + ".jpg")
                    .filename("profile" + i + ".jpg")
                    .fileUrl("http://test.com/profile" + i + ".jpg")
                    .fileType("image/jpeg")
                    .build());

            Member member = memberRepository.save(Member.builder()
                    .email("test" + i + "@test.com")
                    .password("12345678")
                    .nickName("멤버" + i)
                    .file(file)
                    .group(group)
                    .type(MemberType.USER)
                    .build());
            members.add(member);

            diaryRepository.save(Diary.builder()
                    .member(member)
                    .moodEmojiName("happy")
                    .createDate(targetDate)
                    .thing("오늘 있었던 일")
                    .impression("감사한 하루")
                    .build());
        }
        return members;
    }

    private Statistics getStats() {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
        return stats;
    }

    // ===== 테스트 1: 기존 방식 (멤버 5명) =====
    @Test
    @DisplayName("[기존 방식] 멤버 5명 - 예상 쿼리: N+3 = 8개")
    void oldApproach_5members() {
        List<Member> members = createTestData(5);
        entityManager.flush();
        entityManager.clear();

        Statistics stats = getStats();
        System.out.println("\n========== [기존 방식] 멤버 5명 시작 ==========\n");

        long start = System.nanoTime();
        GroupCalendarDetailResponse response = diaryService.findGroupCanlendarDetailV1(
                members.get(0).getId(), 2024, 3, 15);
        long end = System.nanoTime();

        System.out.println(">>> 실행된 쿼리 수: " + stats.getPrepareStatementCount() + "개  (예상: 8개)");
        System.out.println(">>> 실행 시간: " + (end - start) / 1_000_000 + "ms");
        System.out.println(">>> 조회된 일기 수: " + response.getGroupCalendarDetailList().size() + "개");
        System.out.println("========== [기존 방식] 멤버 5명 종료 ==========\n");
    }

    // ===== 테스트 2: 기존 방식 (멤버 100명) =====
    @Test
    @DisplayName("[기존 방식] 멤버 100명 - 예상 쿼리: N+3 = 103개")
    void oldApproach_100members() {
        List<Member> members = createTestData(100);
        entityManager.flush();
        entityManager.clear();

        Statistics stats = getStats();
        System.out.println("\n========== [기존 방식] 멤버 100명 시작 ==========\n");

        long start = System.nanoTime();
        GroupCalendarDetailResponse response = diaryService.findGroupCanlendarDetailV1(
                members.get(0).getId(), 2024, 3, 15);
        long end = System.nanoTime();

        System.out.println(">>> 실행된 쿼리 수: " + stats.getPrepareStatementCount() + "개  (예상: 103개)");
        System.out.println(">>> 실행 시간: " + (end - start) / 1_000_000 + "ms");
        System.out.println(">>> 조회된 일기 수: " + response.getGroupCalendarDetailList().size() + "개");
        System.out.println("========== [기존 방식] 멤버 100명 종료 ==========\n");
    }

    // ===== 테스트 3: 개선된 방식 (멤버 100명) =====
    @Test
    @DisplayName("[개선된 방식] 멤버 100명 - 예상 쿼리: 2개 고정")
    void newApproach_100members() {
        List<Member> members = createTestData(100);
        entityManager.flush();
        entityManager.clear();

        Statistics stats = getStats();
        System.out.println("\n========== [개선된 방식] 멤버 100명 시작 ==========\n");

        long start = System.nanoTime();
        GroupCalendarDetailResponse response = diaryService.findGroupCanlendarDetail(
                members.get(0).getId(), 2024, 3, 15);
        long end = System.nanoTime();

        System.out.println(">>> 실행된 쿼리 수: " + stats.getPrepareStatementCount() + "개  (예상: 2개)");
        System.out.println(">>> 실행 시간: " + (end - start) / 1_000_000 + "ms");
        System.out.println(">>> 조회된 일기 수: " + response.getGroupCalendarDetailList().size() + "개");
        System.out.println("========== [개선된 방식] 멤버 100명 종료 ==========\n");
    }
}
