package com.voice.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voice.app.domain.AudioFile;
import com.voice.app.domain.STTResult;

public interface AudioFileRepository extends JpaRepository<AudioFile, Long>{
	
	AudioFile findBySttResult(STTResult sttResult);

}