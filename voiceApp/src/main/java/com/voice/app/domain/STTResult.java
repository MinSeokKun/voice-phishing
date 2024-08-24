package com.voice.app.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "STT_RESULT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class STTResult {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stt_seq")
    @SequenceGenerator(name = "stt_seq", sequenceName = "STT_SEQ", allocationSize = 50)
    private Long id;

    @Column(name = "FILE_NAME") // 파일명
    private String fileName;

    @Column(name = "FILE_DURATION") // 파일 길이
    private Double fileDurationSeconds;

    @Column(name = "FILE_SIZE") // 파일 크기
    private Long fileSizeBytes;

    @Column(name = "FILE_EXTENSION") // 파일 확장자명
    private String fileExtension;

    @Column(name = "EXECUTION_TIME") // 실행 시간
    private Double executionTimeSeconds;

    @Column(name = "TOTAL_PROCESSING_TIME")
    private Double totalProcessingTimeSeconds;

    @Lob
    @Column(name = "RESULT_TEXT", columnDefinition = "CLOB") // STT 내용
    private String resultText;

    @Column(name = "CREATED_AT") // 생성일자
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Column(name = "EXTERNAL") // 외부접근유무
    private boolean external = false;
    
    
    @Transient
    public String getFormattedFileSize() {
        return formatBytes(this.fileSizeBytes);
    }

    // 바이트를 KB, MB 등으로 변환하는 메서드
    private String formatBytes(long bytes) {
        String[] sizes = {"Bytes", "KB", "MB", "GB", "TB"};
        if (bytes == 0) return "0 Byte";
        int i = (int) Math.floor(Math.log(bytes) / Math.log(1024));
        return Math.round(bytes / Math.pow(1024, i)) + " " + sizes[i];
    }
}