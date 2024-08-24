package com.voice.app.heon;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TranscriptionController {

    private final STTApiClient sttApiClient;

    private final DeepService deepService;
    
    public TranscriptionController(STTApiClient sttApiClient, DeepService deepService) {
        this.sttApiClient = sttApiClient;
        this.deepService = deepService;
    }

//    @GetMapping("/")
//    public String showTranscribePage() {
//        return "transcribe";
//    }

//    @PostMapping("/transcribe")
//    @ResponseBody
//    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
//    	try {
//            File tempFile = File.createTempFile("audio", null);
//            file.transferTo(tempFile);
//            String result = sttApiClient.transcribeAudio(tempFile, file.getOriginalFilename());
//            tempFile.delete();
//            return ResponseEntity.ok(result);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process file");
//        }
//    }
    
    @PostMapping("/transcribe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> transcribeWithProgress(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        return sttApiClient.transcribeFileWithProgress(file);
    }
    
    @PostMapping("/transcribe-and-predict")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> transcribeAndPredict(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 음성을 텍스트로 변환
            ResponseEntity<Map<String, Object>> transcriptionResponse = sttApiClient.transcribeFileWithProgress(file);

            if (!transcriptionResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(transcriptionResponse.getStatusCode())
                        .body(Collections.singletonMap("error", "음성 변환 중 오류가 발생했습니다."));
            }

            Map<String, Object> transcriptionResult = transcriptionResponse.getBody();
            if (transcriptionResult == null || !transcriptionResult.containsKey("text")) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "변환된 텍스트를 찾을 수 없습니다."));
            }

            String transcribedText = (String) transcriptionResult.get("text");

            // 2. 변환된 텍스트로 보이스피싱 예측
            ResponseEntity<Map<String, Object>> phishingResponse = deepService.predictVoicePhishing(transcribedText);

            if (!phishingResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(phishingResponse.getStatusCode())
                        .body(Collections.singletonMap("error", "보이스피싱 예측 중 오류가 발생했습니다."));
            }

            Map<String, Object> phishingResult = phishingResponse.getBody();
            if (phishingResult == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "보이스피싱 예측 결과를 찾을 수 없습니다."));
            }

            // 3. 두 결과를 합쳐서 반환
            Map<String, Object> combinedResult = new HashMap<>(transcriptionResult);
            combinedResult.putAll(phishingResult);
            combinedResult.put("fullText", transcribedText);

            return ResponseEntity.ok(combinedResult);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "처리가 중단되었습니다: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "예기치 못한 오류가 발생했습니다: " + e.getMessage()));
        }
    }

}