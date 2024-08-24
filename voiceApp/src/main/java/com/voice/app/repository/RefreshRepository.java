package com.voice.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voice.app.domain.RefreshEntity;

import jakarta.transaction.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RefreshEntity r WHERE r.refresh = :refresh")
    Boolean existsByRefresh(@Param("refresh") String refresh);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshEntity r WHERE r.refresh = :refresh")
    void deleteByRefresh(@Param("refresh") String refresh);
    
    // username으로 Refresh 토큰 삭제
    @Modifying
    @Query("DELETE FROM RefreshEntity r WHERE r.username = ?1")
    void deleteByUsername(String username);
}