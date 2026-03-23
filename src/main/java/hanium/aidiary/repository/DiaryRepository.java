package hanium.aidiary.repository;

import hanium.aidiary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("SELECT d FROM Diary d " +
           "JOIN FETCH d.member m " +
           "WHERE m.group.id = :groupId " +
           "AND YEAR(d.createDate) = :year " +
           "AND MONTH(d.createDate) = :month " +
           "AND DAY(d.createDate) = :day")
    List<Diary> findByGroupIdAndDate(
            @Param("groupId") Long groupId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("day") int day);
}
