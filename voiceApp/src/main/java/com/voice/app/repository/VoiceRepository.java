package com.voice.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voice.app.domain.Member;
import com.voice.app.domain.Voice;

public interface VoiceRepository extends JpaRepository<Voice, Long> {
	@Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM voice WHERE member = :memberId ORDER BY id) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
			nativeQuery = true)
			List<Voice> findByMemberWithPagination(@Param("memberId") Long memberId, @Param("startRow") int startRow, @Param("endRow") int endRow);

    long countByMember(Member member);
    
    // 위험도로 검색
    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM voice WHERE member = :memberId AND per >= :minPer AND per <= :maxPer ORDER BY id DESC) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
            nativeQuery = true)
     List<Voice> findByMemberAndRiskWithPagination(@Param("memberId") Long member, 
                                                   @Param("minPer") int minPer, 
                                                   @Param("maxPer") int maxPer, 
                                                   @Param("startRow") int startRow, 
                                                   @Param("endRow") int endRow);

     @Query(value = "SELECT COUNT(*) FROM voice WHERE member = :memberId AND per >= :minPer AND per <= :maxPer",
            nativeQuery = true)
     long countByMemberAndRisk(@Param("memberId") Long memberId, 
                               @Param("minPer") int minPer, 
                               @Param("maxPer") int maxPer);

     @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM voice ORDER BY id DESC) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
            countQuery = "SELECT COUNT(*) FROM voice",
            nativeQuery = true)
    	List<Voice> findAllWithPagination(@Param("startRow") long startRow, @Param("endRow") long endRow);
    
    
    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT v.* FROM voice v LEFT JOIN member m ON v.member = m.id WHERE v.tit LIKE '%' || :kw || '%' OR v.file_name LIKE '%' || :kw || '%' OR m.username LIKE '%' || :kw || '%' ORDER BY v.create_date DESC) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
            countQuery = "SELECT COUNT(*) FROM voice v LEFT JOIN member m ON v.member = m.id WHERE v.tit LIKE '%' || :kw || '%' OR v.file_name LIKE '%' || :kw || '%' OR m.username LIKE '%' || :kw || '%'",
            nativeQuery = true)
     List<Voice> findAllWithPaginationAndKeyword(@Param("startRow") long startRow, @Param("endRow") long endRow, @Param("kw") String kw);

     @Query(value = "SELECT COUNT(*) FROM voice v LEFT JOIN member m ON v.member = m.id WHERE v.tit LIKE '%' || :kw || '%' OR v.file_name LIKE '%' || :kw || '%' OR m.username LIKE '%' || :kw || '%'",
            nativeQuery = true)
     long countByKeyword(@Param("kw") String kw);

//    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM voice ORDER BY id) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
// 	       countQuery = "SELECT COUNT(*) FROM voice",
// 	       nativeQuery = true)
//	List<Voice> findAllWithPagination(@Param("startRow") long startRow, @Param("endRow") long endRow);
    List<Voice> findAllByOrderByCreateDateDesc();
    
    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM voice WHERE member = :memberId AND per >= :minPer AND per <= :maxPer AND (LOWER(tit) LIKE LOWER(CONCAT(CONCAT('%', :searchTerm), '%')) OR LOWER(data) LIKE LOWER(CONCAT(CONCAT('%', :searchTerm), '%'))) ORDER BY id DESC) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
            nativeQuery = true)
     List<Voice> searchVoices(@Param("memberId") Long memberId, 
                              @Param("searchTerm") String searchTerm,
                              @Param("minPer") int minPer, 
                              @Param("maxPer") int maxPer, 
                              @Param("startRow") int startRow, 
                              @Param("endRow") int endRow);

     @Query(value = "SELECT COUNT(*) FROM voice WHERE member = :memberId AND per >= :minPer AND per <= :maxPer AND (LOWER(tit) LIKE LOWER(CONCAT(CONCAT('%', :searchTerm), '%')) OR LOWER(data) LIKE LOWER(CONCAT(CONCAT('%', :searchTerm), '%')))",
            nativeQuery = true)
     long countSearchVoices(@Param("memberId") Long memberId, 
                            @Param("searchTerm") String searchTerm,
                            @Param("minPer") int minPer, 
                            @Param("maxPer") int maxPer);
    @Modifying
    @Query("DELETE FROM Voice v WHERE v.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}