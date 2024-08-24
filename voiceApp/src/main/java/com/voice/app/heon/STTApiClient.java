package com.voice.app.heon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class STTApiClient {

    @Value("${stt.api.url}")
    private String apiUrl;

    @Value("${stt.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public STTApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public ResponseEntity<Map<String, Object>> transcribeFileWithProgress(MultipartFile file) throws IOException, InterruptedException {
        try {
            Path currentPath = Paths.get("");
            File tempFile = new File(currentPath.toAbsolutePath().toString() + "/" + file.getOriginalFilename());
            file.transferTo(tempFile);
            log.info("Temporary file created: " + tempFile.getAbsolutePath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-API-KEY", apiKey);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Sending request to URL: {}", apiUrl + "/dadeum/openapi/file");
            log.info("Request headers: {}", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/dadeum/openapi/file",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response headers: {}", response.getHeaders());
            log.info("Response body: {}", response.getBody());

            Map<String, Object> result = new HashMap<>();
            if (response.getStatusCode().is3xxRedirection()) {
            	String newUrl = response.getHeaders().toString();
            	log.info("Redirect to :" + newUrl);
            }
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("Raw API Response: " + responseBody);

                if (responseBody != null && responseBody.trim().startsWith("{")) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        result.put("progress", 100);
                        
                        if (jsonResponse.has("transcription")) {
                            String transcription = jsonResponse.getString("transcription");
                            result.put("text", transcription);
                            
                            List<Map<String, Object>> utteranceList = new ArrayList<>();
                            Map<String, Object> utteranceMap = new HashMap<>();
                            utteranceMap.put("text", transcription);
                            utteranceList.add(utteranceMap);
                            result.put("utterances", utteranceList);
                            
                            // 보이스피싱 확률 계산
                            double voicePhishingProbability = calculateVoicePhishingProbability(utteranceList);
                            result.put("voicePhishingProbability", voicePhishingProbability);
                        } else {
                            result.put("text", "변환된 텍스트를 찾을 수 없습니다.");
                        }
                        
                        // 추가 정보 저장
                        if (jsonResponse.has("duration")) {
                            result.put("duration", jsonResponse.getDouble("duration"));
                        }
                        if (jsonResponse.has("fileName")) {
                            result.put("fileName", jsonResponse.getString("fileName"));
                        }
                        if (jsonResponse.has("fileSize")) {
                            result.put("fileSize", jsonResponse.getInt("fileSize"));
                        }
                        if (jsonResponse.has("processingTime")) {
                            result.put("processingTime", jsonResponse.getDouble("processingTime"));
                        }
                    } catch (JSONException e) {
                        log.error("Failed to parse JSON response: " + e.getMessage());
                        result.put("error", "JSON 파싱 오류: " + e.getMessage());
                    }
                } else {
                    log.error("Unexpected response format: " + responseBody);
                    result.put("error", "예상치 못한 응답 형식");
                }
            } else {
                result.put("error", "API 요청 실패: " + response.getStatusCode());
            }

            tempFile.delete();  // 임시 파일 삭제
            
            return ResponseEntity.ok(result);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP Error: ", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(errorResult);
        } catch (Exception e) {
            log.error("Unexpected error in transcribeFileWithProgress: ", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    // POINT: 보이스피싱 확률을 계산하는 메서드
    private double calculateVoicePhishingProbability(List<Map<String, Object>> utterances) {
        // 여기에 보이스피싱 확률을 계산하는 로직을 구현합니다.
        // 예를 들어, 특정 키워드의 빈도, 문장 구조, 음성 특징 등을 분석할 수 있습니다.
        // 이 예제에서는 간단히 랜덤 값을 반환합니다.
        return Math.random();
    }
    
//    public String transcribeAudio(File audioFile,  String originalFileName) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("X-API-KEY", apiKey);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", new FileSystemResource(audioFile) {
//            @Override
//            public String getFilename() {
//                return originalFileName;
//            }
//        });
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        log.info("Sending request to URL: {}", apiUrl + "/dadeum/openapi/file");
//        log.info("Request headers: {}", headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    apiUrl + "/dadeum/openapi/file",
//                    HttpMethod.POST,
//                    requestEntity,
//                    String.class
//            );
//
//            log.info("Response status: {}", response.getStatusCode());
//            log.info("Response headers: {}", response.getHeaders());
//            log.info("Response body: {}", response.getBody());
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                if (response.getBody() == null || response.getBody().isEmpty()) {
//                    return "Transcription successful, but no content returned.";
//                }
//                return response.getBody();
//            } else {
//                throw new RuntimeException("Failed to transcribe audio: " + response.getStatusCode());
//            }
//        } catch (HttpClientErrorException e) {
//            log.error("HTTP Client Error: ", e);
//            throw new RuntimeException("Failed to transcribe audio: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
//        } catch (HttpServerErrorException e) {
//            log.error("HTTP Server Error: ", e);
//            throw new RuntimeException("Failed to transcribe audio: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
//        } catch (Exception e) {
//            log.error("Unexpected error: ", e);
//            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage());
//        }
//    }
}
