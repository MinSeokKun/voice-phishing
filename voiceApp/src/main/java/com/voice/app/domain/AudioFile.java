package com.voice.app.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AUDIO_FILES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audio_file_seq")
    @SequenceGenerator(name = "audio_file_seq", sequenceName = "AUDIO_FILE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FILE_NAME", length = 255)
    private String fileName;

    @Column(name = "FILE_PATH", length = 1000)
    private String filePath;

    @Column(name = "FILE_SIZE")
    private Long fileSizeBytes;

    @Column(name = "FILE_EXTENSION", length = 10)
    private String fileExtension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NUM")
    private Member member;

    @OneToOne
    @JoinColumn(name = "STT_RESULT_ID")
    private STTResult sttResult;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Column(name = "EXTERNAL")
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