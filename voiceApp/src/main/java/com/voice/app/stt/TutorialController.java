package com.voice.app.stt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.voice.app.heon.DeepService;

@RestController
@RequestMapping("/api/tutorial")
public class TutorialController {
    private final TutorialService tutorialService;
    private final DeepService deepService;
    
    public TutorialController(TutorialService tutorialService, DeepService deepService) {
        this.tutorialService = tutorialService;
        this.deepService = deepService;
    }
    
    @PostMapping(value = "/transcribe/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void transcribeFile(@RequestPart("file") MultipartFile file) throws IOException, InterruptedException {
        tutorialService.transcribeFile(file);
    }
    
    @PostMapping(value = "/transcribe/progress", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, Object>> transcribeAndPredict(@RequestPart("file") MultipartFile file) throws IOException, InterruptedException {
        // 1. 음성을 텍스트로 변환
        ResponseEntity<Map<String, Object>> transcriptionResponse = tutorialService.transcribeFileWithProgress(file);
        
        if (transcriptionResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> transcriptionResult = transcriptionResponse.getBody();
            
            // utterances에서 전체 텍스트 추출
            List<Map<String, Object>> utterances = (List<Map<String, Object>>) transcriptionResult.get("utterances");
            StringBuilder fullText = new StringBuilder();
            for (Map<String, Object> utterance : utterances) {
                fullText.append(utterance.get("text")).append(" ");
            }
            String transcribedText = fullText.toString().trim();
            System.out.println(transcribedText);
            // 2. 변환된 텍스트로 보이스피싱 예측
            try {
            	ResponseEntity<Map<String, Object>> phishingResponse = deepService.predictVoicePhishing(transcribedText);
            	if (phishingResponse.getStatusCode().is2xxSuccessful()) {
            		Map<String, Object> phishingResult = phishingResponse.getBody();
            		
            		// 3. 두 결과를 합쳐서 반환
            		Map<String, Object> combinedResult = new HashMap<>(transcriptionResult);
            		combinedResult.putAll(phishingResult);
            		
            		// 전체 텍스트 추가
            		combinedResult.put("fullText", transcribedText);
            		
            		return ResponseEntity.ok(combinedResult);
            	} else {
            		return phishingResponse; // 보이스피싱 예측 중 오류 발생 시 해당 오류 반환
            	}
            	
            } catch (Exception e) {
            	e.printStackTrace();
            	return null;
            }
            
        } else {
            return transcriptionResponse; // 음성 변환 중 오류 발생 시 해당 오류 반환
        }
    }
}