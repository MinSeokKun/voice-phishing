package com.voice.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voice.app.domain.STTResult;

public interface STTResultRepository extends JpaRepository<STTResult, Long> {
    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM stt_result WHERE member_id = :memberId ORDER BY created_at DESC) a WHERE ROWNUM <= :maxRow) WHERE rnum > :minRow",
                nativeQuery = true)
    List<STTResult> findByMemberIdWithPagination(@Param("memberId") Long memberId, 
                                                @Param("minRow") int minRow, 
                                                @Param("maxRow") int maxRow);

    @Query(value = "SELECT COUNT(*) FROM stt_result WHERE member_id = :memberId", 
            nativeQuery = true)
    long countByMemberId(@Param("memberId") Long memberId);

    Optional<STTResult> findByMemberIdAndId(Long memberId, Long id);

    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM stt_result WHERE member_id = :memberNum ORDER BY created_at DESC) a WHERE ROWNUM <= :maxRow) WHERE rnum > :minRow",
            nativeQuery = true)
	List<STTResult> findByMemberMemberNumWithPagination(@Param("memberNum") Long memberNum, 
														@Param("minRow") int minRow, 
														@Param("maxRow") int maxRow);
    
    @Query(value = "SELECT COUNT(*) FROM stt_result WHERE member_id = :memberNum", 
		nativeQuery = true)
    long countByMemberMemberNum(@Param("memberNum") Long memberNum);

    //마이페이지 그래프 쿼리
    @Query(value = "SELECT TRUNC(s.created_at, 'MM') as month, SUM(s.file_duration), s.external " +
            "FROM STT_RESULT s " +
            "WHERE s.member_id = :memberId AND s.created_at >= :startDate AND s.created_at < :endDate " +
            "GROUP BY TRUNC(s.created_at, 'MM'), s.external", 
            nativeQuery = true)
    List<Object[]> getMonthlySumDurationForMemberWithExternal(@Param("memberId") Long memberId, 
                                                            @Param("startDate") LocalDateTime startDate, 
                                                            @Param("endDate") LocalDateTime endDate);

    //admin memberlist
    @Query("SELECT SUM(s.fileDurationSeconds) FROM STTResult s WHERE s.member.id = :memberId AND s.external = :external")
    double sumFileDurationByMemberAndExternal(@Param("memberId") Long memberId, @Param("external") boolean external);
}
