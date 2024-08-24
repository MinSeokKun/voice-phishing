package com.voice.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voice.app.domain.Member;


public interface MemberRepository extends JpaRepository<Member, Integer> {
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.username = :username")
    Boolean existsByUsername(@Param("username") String username);

    Optional<Member> findByUsername(String username);
    Optional<Member> findById(long id);

    @Query(value = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM member WHERE username LIKE '%' || :kw || '%' OR email LIKE '%' || :kw || '%' OR name LIKE '%' || :kw || '%' ORDER BY create_date DESC) a WHERE ROWNUM <= :endRow) WHERE rnum > :startRow",
            countQuery = "SELECT COUNT(*) FROM member WHERE username LIKE '%' || :kw || '%' OR email LIKE '%' || :kw || '%' OR name LIKE '%' || :kw || '%'",
            nativeQuery = true)
    List<Member> findAllWithPaginationAndKeyword(@Param("startRow") long startRow, @Param("endRow") long endRow, @Param("kw") String kw);

    @Query(value = "SELECT COUNT(*) FROM member WHERE username LIKE '%' || :kw || '%' OR email LIKE '%' || :kw || '%' OR name LIKE '%' || :kw || '%'",
            nativeQuery = true)
    long countByKeyword(@Param("kw") String kw);

    Optional<Member> findByApiKey(String apiKey);
}