package com.voice.app.stt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.voice.app.domain.AudioFile;
import com.voice.app.domain.Member;
import com.voice.app.domain.STTResult;
import com.voice.app.repository.AudioFileRepository;
import com.voice.app.repository.MemberRepository;
import com.voice.app.repository.STTResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;



@Service
@Slf4j
@RequiredArgsConstructor
public class TutorialService {

    private boolean stopPolling;
    private String transcribeId = null;
    private String accessToken = null;

    private final STTResultRepository sttResultRepository;
    private final MemberRepository memberRepository;
    private final AudioFileRepository audioFileRepository;
    
    @Value("${vito.client_id}")
    String client_id;

    @Value("${vito.client_secret}")
    String client_secret;

    @Value("${audio.save.path}")
    String audioSavePath;

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    //프로그래스바관련
    public ResponseEntity<Map<String, Object>> transcribeFileWithProgress(MultipartFile file) throws IOException, InterruptedException {
        try {
            accessToken = getAccessToken();
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://openapi.vito.ai/v1")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.MULTIPART_FORM_DATA))
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                    .build();
            
            Path currentPath = Paths.get("");
            File tempFile = new File(currentPath.toAbsolutePath().toString() + "/" + file.getOriginalFilename());
            file.transferTo(tempFile);
            System.out.println("Temporary file created: " + tempFile.getAbsolutePath());
            
            MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder.part("file", new FileSystemResource(tempFile));
            multipartBodyBuilder.part("config", "{}");
            
            String response = webClient.post()
                    .uri("/transcribe")
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            System.out.println("Vito API response: " + response);
            
            JSONObject jsonObject = new JSONObject(response);
            transcribeId = jsonObject.getString("id");
            Map<String, Object> result = new HashMap<>();
            stopPolling = false;
            int progress = 0;
            while (!stopPolling && progress < 100) {
                System.out.println("Polling Progress: " + progress);
                Thread.sleep(5000);  // 5초마다 상태 체크
                String statusResponse = webClient.get()
                        .uri("/transcribe/" + transcribeId)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JSONObject statusJson = new JSONObject(statusResponse);
                String status = statusJson.getString("status");

                if ("completed".equals(status)) {
                    result.put("progress", 100);
                    if (statusJson.has("results") && statusJson.getJSONObject("results").has("utterances")) {
                        JSONArray utterances = statusJson.getJSONObject("results").getJSONArray("utterances");
                        List<Map<String, Object>> utteranceList = new ArrayList<>();
                        for (int i = 0; i < utterances.length(); i++) {
                            JSONObject utterance = utterances.getJSONObject(i);
                            Map<String, Object> utteranceMap = new HashMap<>();
                            utteranceMap.put("text", utterance.getString("msg"));
                            utteranceMap.put("startAt", utterance.getInt("start_at"));
                            utteranceMap.put("duration", utterance.getInt("duration"));
                            utteranceList.add(utteranceMap);
                        }
                        result.put("utterances", utteranceList);
                        
                        // POINT: 보이스피싱 확률 계산
                        double voicePhishingProbability = calculateVoicePhishingProbability(utteranceList);
                        result.put("voicePhishingProbability", voicePhishingProbability);
                    } else {
                        result.put("text", "변환된 텍스트를 찾을 수 없습니다.");
                    }
                    stopPolling = true;
                } else {
                    progress += 10;  // 예시로 10%씩 증가
                    result.put("progress", Math.min(progress, 90));  // 최대 90%까지만
                    result.put("text", "변환 중...");
                }

                log.info("Current status: " + statusJson.toString());
            }

            tempFile.delete();  // 임시 파일 삭제
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("transcribeFileWithProgress 에러 " + e);
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
    
    public String getAccessToken(){
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai")
                .build();


        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", client_id);
        formData.add("client_secret", client_secret);


        String response = webClient
                .post()
                .uri("/v1/authenticate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info(response);
        JSONObject jsonObject = new JSONObject(response.toString());
        return jsonObject.getString("access_token");
    }


    public void transcribeFile(MultipartFile multipartFile) throws IOException, InterruptedException {

        accessToken = getAccessToken();
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.MULTIPART_FORM_DATA))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                .build();

        Path currentPath = Paths.get("");
        File file = new File(currentPath.toAbsolutePath().toString() + "/"+multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new FileSystemResource(file));
        multipartBodyBuilder.part("config", "{}");


        // POST 요청 보내기
        String response = null;
        try{
        	System.out.println("response try-catch 시작");
            response = webClient.post()
                    .uri("/transcribe")
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("post 끝");
            System.out.println("VITO API : " + response);
        }catch (WebClientResponseException e){
            log.error(String.valueOf(e));
        }

        JSONObject jsonObject = new JSONObject(response.toString());

        try{
            if(jsonObject.getString("code").equals("H0002")){
                log.info("accessToken 만료로 재발급 받습니다");
                accessToken = getAccessToken();
                response = webClient.post()
                        .uri("/transcribe")
                        .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
        }catch (JSONException e){
            log.info("code 확인 불가 오류 catch");
            log.info(e.toString());
        }
        log.info("transcribe 요청 id : " + jsonObject.getString("id"));

        stopPolling = false;
        transcribeId = jsonObject.getString("id");
        startPolling();
    }

    public STTResult transcribeFile(MultipartFile multipartFile, Long memberNum) throws IOException, InterruptedException {
        File savedFile = null;
        try {
            String fileExtension = getFileExtension(multipartFile.getOriginalFilename());
    
            // 현재 시간을 포맷팅하여 파일명으로 사용
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "audio_" + timestamp + "." + fileExtension;
            
            // 사용자의 홈 디렉토리를 기준으로 Desktop 폴더 경로 생성
            String userHome = System.getProperty("user.home");
            String desktopPath = userHome + File.separator + "Desktop";
            
            // audioSavePath가 상대 경로인 경우를 대비해 절대 경로로 변환
            File audioSaveDir = new File(desktopPath, audioSavePath);
            if (!audioSaveDir.exists()) {
                audioSaveDir.mkdirs(); // 디렉토리가 없으면 생성
            }
            
            savedFile = new File(audioSaveDir, fileName);
            multipartFile.transferTo(savedFile);

            accessToken = getAccessToken();
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://openapi.vito.ai/v1")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                    .build();

            MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder.part("file", new FileSystemResource(savedFile));
            multipartBodyBuilder.part("config", "{}");

            String response = null;
            try {
                response = webClient.post()
                        .uri("/transcribe")
                        .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                log.info("Transcription request completed");
            } catch (WebClientResponseException e) {
                log.error("Error during transcription request: ", e);
                throw new RuntimeException("Transcription request failed", e);
            }

            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("code") && jsonObject.getString("code").equals("H0002")) {
                log.info("Access token expired. Refreshing token.");
                accessToken = getAccessToken();
                response = webClient.post()
                        .uri("/transcribe")
                        .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }

            String transcribeId = jsonObject.getString("id");
            log.info("Transcribe request ID: {}", transcribeId);

            long startTime = System.currentTimeMillis();
            String jsonResponse = pollForResult(transcribeId);
            long endTime = System.currentTimeMillis();

            String transcribedText = extractTextFromJson(jsonResponse);

            long fileSizeBytes = multipartFile.getSize();
            double totalProcessingTimeSeconds = (endTime - startTime) / 1000.0;

            double fileDurationSeconds = getFileDurationFromJson(jsonResponse);
            System.out.println("********************" + fileDurationSeconds);
            
            Member member = memberRepository.findById(memberNum)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            STTResult result = STTResult.builder()
                    .fileName(fileName)
                    .fileDurationSeconds(fileDurationSeconds)
                    .fileSizeBytes(fileSizeBytes)
                    .fileExtension(fileExtension)
                    .executionTimeSeconds(totalProcessingTimeSeconds)
                    .totalProcessingTimeSeconds(totalProcessingTimeSeconds)
                    .resultText(transcribedText)
                    .createdAt(LocalDateTime.now())
                    .member(member)
                    .build();

            STTResult savedResult = sttResultRepository.save(result);

            // Save audio file information
            AudioFile audioFile = AudioFile.builder()
                    .fileName(fileName)
                    .filePath(savedFile.getAbsolutePath())  // 지정된 경로에 저장된 파일 경로
                    .fileSizeBytes(fileSizeBytes)
                    .fileExtension(fileExtension)
                    .uploadedAt(LocalDateTime.now())
                    .member(member)
                    .sttResult(savedResult)
                    .external(false)  // 외부 파일 여부 설정
                    .build();

            audioFileRepository.save(audioFile);

            return savedResult;
        } finally {
            
        }
    }

    private String pollForResult(String transcribeId) throws InterruptedException {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.vito.ai/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                .build();
    
        String response = null;
        boolean completed = false;
        int attempts = 0;
        int maxAttempts = 60; // 5분 (5초 간격으로 60번 시도)
    
        while (!completed && attempts < maxAttempts) {
            log.info("Polling for transcription result. Attempt: {}", attempts + 1);
            response = webClient.get()
                    .uri("/transcribe/" + transcribeId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
    
            log.info("Poll response: {}", response);
    
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("completed")) {
                completed = true;
            } else {
                attempts++;
                Thread.sleep(5000); // Wait for 5 seconds before next poll
            }
        }
    
        if (!completed) {
            log.warn("Transcription did not complete within the expected time.");
            return "";
        }
    
        log.info("Transcription completed");
        return response;  // 전체 JSON 응답을 반환
    }
    
    // 텍스트 추출을 위한 새로운 메서드
    private String extractTextFromJson(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray utterances = jsonObject.getJSONObject("results").getJSONArray("utterances");
        
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < utterances.length(); i++) {
            JSONObject utterance = utterances.getJSONObject(i);
            fullText.append(utterance.getString("msg")).append(" ");
        }
        
        return fullText.toString().trim();
    }

    private double getFileDurationFromJson(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            log.error("Empty or null JSON response");
            return 0.0;
        }
    
        try {
            // 전체 JSON 응답 로깅 (주의: 민감한 정보가 포함될 수 있으므로 프로덕션 환경에서는 주의해서 사용)
            log.debug("Raw JSON response: {}", jsonResponse);
    
            JSONObject json = new JSONObject(jsonResponse);
            
            if (!json.has("results")) {
                log.error("JSON does not contain 'results' key");
                return 0.0;
            }
    
            JSONObject results = json.getJSONObject("results");
            
            if (!results.has("utterances")) {
                log.error("Results does not contain 'utterances' key");
                return 0.0;
            }
    
            JSONArray utterances = results.getJSONArray("utterances");
            
            if (utterances.length() == 0) {
                log.warn("Utterances array is empty");
                return 0.0;
            }
    
            double totalDurationMs = 0;
            for (int i = 0; i < utterances.length(); i++) {
                JSONObject utterance = utterances.getJSONObject(i);
                if (!utterance.has("start_at") || !utterance.has("duration")) {
                    log.warn("Utterance at index {} is missing 'start_at' or 'duration'", i);
                    continue;
                }
                double startAt = utterance.getDouble("start_at");
                double duration = utterance.getDouble("duration");
                totalDurationMs = Math.max(totalDurationMs, startAt + duration);
            }
            
            double totalDurationSeconds = totalDurationMs / 1000.0;
            log.info("Total file duration calculated from JSON: {} seconds", totalDurationSeconds);
            return totalDurationSeconds;
        } catch (JSONException e) {
            log.error("Error parsing JSON response: {}", e.getMessage());
            log.debug("Problematic JSON: {}", jsonResponse);
            return 0.0;
        }
    }
    
    private double getFileDuration(File file) {
        try {
            MultimediaObject multimediaObject = new MultimediaObject(file);
            long durationInMilliseconds = multimediaObject.getInfo().getDuration();
            return durationInMilliseconds / 1000.0;
        } catch (EncoderException e) {
            log.error("Error getting file duration: ", e);
            return 0.0;
        }
    }

     // 5초마다 실행 (주기는 필요에 따라 조절)
    public void startPolling() throws InterruptedException {
        log.info("Polling 함수 첫 시작");
        String response = null;
        Thread.sleep(5000);
        while (!stopPolling) {
            System.out.println("while polling 시작 반복중");
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://openapi.vito.ai/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "bearer " + accessToken)
                    .build();


            String uri = "/transcribe/" + transcribeId;
            response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();



            JSONObject jsonObject = new JSONObject(response.toString());
            // status 확인하여 폴링 중단 여부 결정
            if (jsonObject.getString("status").equals("completed")) {
                stopPolling = true;
            }

            try {
                Thread.sleep(5000); // 폴링 주기 (5초)를 설정
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("while polling 끝 반복중");
        }

        log.info("폴링함수 끝");
        log.info(response.toString());
    }

//    public void transcribeWebSocketFile(MultipartFile multipartFile) throws IOException, InterruptedException {
//        Logger logger = Logger.getLogger(TutorialService.class.getName());
//        OkHttpClient client = new OkHttpClient();
//        String token = getAccessToken();
//
//        HttpUrl.Builder httpBuilder = HttpUrl.get("https://openapi.vito.ai/v1/transcribe:streaming").newBuilder();
//        httpBuilder.addQueryParameter("sample_rate", "44100");
//        httpBuilder.addQueryParameter("encoding", "WAV");
//        httpBuilder.addQueryParameter("use_itn", "true");
//        httpBuilder.addQueryParameter("use_disfluency_filter", "true");
//        httpBuilder.addQueryParameter("use_profanity_filter", "true");
//
//        String url = httpBuilder.toString().replace("https://", "wss://");
//
//        Request request = new Request.Builder()
//                .url(url)
//                .addHeader("Authorization", "Bearer " + token)
//                .build();
//
//        VitoWebSocketListener webSocketListener = new VitoWebSocketListener();
//        WebSocket vitoWebSocket = client.newWebSocket(request, webSocketListener);
//
//        FileInputStream fis = null;
//        Path currentPath = Paths.get("");
//        File file = new File(currentPath.toAbsolutePath().toString() + "/"+multipartFile.getOriginalFilename());
//        multipartFile.transferTo(file);
//        try {
//            fis = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        byte[] buffer = new byte[1024];
//        int readBytes;
//        while ((readBytes = fis.read(buffer)) != -1) {
//            boolean sent = vitoWebSocket.send(ByteString.of(buffer, 0, readBytes));
//            if (!sent) {
//                logger.log(Level.WARNING, "Send buffer is full. Cannot complete request. Increase sleep interval.");
//                System.exit(1);
//            }
//            Thread.sleep(0, 100);
//        }
//        fis.close();
//        vitoWebSocket.send("EOS");
//
//        webSocketListener.waitClose();
//        client.dispatcher().executorService().shutdown();
//
//    }

    //마이페이지 그래프
    public Map<YearMonth, Map<String, Double>> getMonthlyUsageForMember(Long memberNum, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = sttResultRepository.getMonthlySumDurationForMemberWithExternal(memberNum, startDate, endDate);
        
        System.out.println("Query results size: " + results.size());
    
        Map<YearMonth, Map<String, Double>> monthlyUsage = new TreeMap<>();
    
        for (Object[] result : results) {
            LocalDateTime dateTime = ((Timestamp) result[0]).toLocalDateTime();
            YearMonth yearMonth = YearMonth.from(dateTime);
            Double durationInMinutes = ((Number) result[1]).doubleValue() / 60.0;
            boolean isExternal = (Integer) result[2] != 0;
            
            System.out.println("Processing result: " + dateTime + ", " + durationInMinutes + ", " + isExternal);
            
            monthlyUsage.computeIfAbsent(yearMonth, k -> new HashMap<>());
            String usageType = isExternal ? "Open API 사용" : "내부 사용";
            monthlyUsage.get(yearMonth).put(usageType, durationInMinutes);
            System.out.println("달에 사용한 내역: " + durationInMinutes);
            System.out.println("API OR 내부사용: " + usageType);
        }
    
        System.out.println("Final monthlyUsage: " + monthlyUsage);
    
        return monthlyUsage;
    }

    public Page<STTResult> getSTTResultsByMemberNum(Long memberNum, int page, int size) {
        int minRow = page * size;
        int maxRow = (page + 1) * size;
        List<STTResult> results = sttResultRepository.findByMemberMemberNumWithPagination(memberNum, minRow, maxRow);
        long total = sttResultRepository.countByMemberMemberNum(memberNum);

        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

}

@Slf4j
class VitoWebSocketListener extends WebSocketListener {
    private static final Logger logger = Logger.getLogger(TutorialService.class.getName());
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private CountDownLatch latch = null;

    private static void log(Level level, String msg, Object... args) {
        logger.log(level, msg, args);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log(Level.INFO, "Open " + response.message());
        latch = new CountDownLatch(1);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println(text);
        log.info(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println(bytes.hex());
        log.info(bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        log(Level.INFO, "Closing {0} {1}", code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        log(Level.INFO, "Closed {0} {1}", code, reason);
        latch.countDown();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
        latch.countDown();
    }

    public void waitClose() throws InterruptedException {
        log(Level.INFO, "Wait for finish");
        latch.await();
    }
}