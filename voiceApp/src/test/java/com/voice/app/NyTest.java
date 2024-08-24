package com.voice.app;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.voice.app.domain.Member;
import com.voice.app.domain.Voice;
import com.voice.app.repository.MemberRepository;
import com.voice.app.repository.VoiceRepository;

@SpringBootTest
public class NyTest {
	
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private VoiceRepository voiceRepository;
//	@Autowired
//    private PasswordEncoder passwordEncoder;

	@Test
	void listTest() {
		Voice l1 = new Voice();
		l1.setPer(85);
		l1.setTit("서울역");
		l1.setFileName("통화녹음 01011112222.m4a");		
		l1.setData("'지금 유선상으로 00씨 앞으로 더 이상 말씀드릴 건 없는데 소환장 발부할게요. 일단 출석하셔서 수사를 받으시면 되시겠습니다.' --- 55%");
		l1.setCreateDate(LocalDateTime.now());
		Member member = memberRepository.findById(1).get();
		l1.setMember(member);
		this.voiceRepository.save(l1);
	}
	
	@Test
	void listTest2() {
		Voice l1 = new Voice();
		l1.setPer(95);
		l1.setTit("동부경찰서 방시원 경장");
		l1.setFileName("통화녹음 01033332222.m4a");
		l1.setData("'지금 본인 명의로 개설 중인 계좌의 개수가 몇개이시죠? 이 부분은 만약 말씀하신 계좌의 개수와 추후 저희 금융조사팀에서 조사되는 ***님 계좌의 개수가 다르다면 대포통장의 범죄에 가담한 것으로 간주하기 때문에 여쭙는 것이고요.' --- 70%");
		l1.setCreateDate(LocalDateTime.now());
		Member member = memberRepository.findById(1).get();
		l1.setMember(member);
		this.voiceRepository.save(l1);
	}
	
	@Test
	void listTest3() {
		Voice l1 = new Voice();
		l1.setPer(70);
		l1.setTit("김정민 검사");
		l1.setFileName("통화녹음 01019192266.m4a");
		l1.setData("'저희가 얼마 전에 강남구 선릉에 위치한 오피스텔에서 성매매 알선 및 금융 위반 혐의로 주범 이지연이라는 40대 여성을 검거했는데 혹시 아시는 분이십니까?' --- 45%");
		l1.setCreateDate(LocalDateTime.now());
		Member member = memberRepository.findById(1).get();
		l1.setMember(member);
		this.voiceRepository.save(l1);
	}
	@Test
	void listTest4() {
		Voice l1 = new Voice();
		l1.setPer(30);
		l1.setTit("대출");
		l1.setFileName("통화녹음 01055552222.m4a");
		l1.setData("'지금 유선상으로 00씨 앞으로 더 이상 말씀드릴 건 없는데 소환장 발부할게요. 일단 출석하셔서 수사를 받으시면 되시겠습니다.' --- 55%");
		l1.setCreateDate(LocalDateTime.now());
		Member member = memberRepository.findById(1).get();
		l1.setMember(member);
		this.voiceRepository.save(l1);
	}
	
	@Test
	void listTest5() {
		Voice l1 = new Voice();
		l1.setPer(55);
		l1.setTit("국민은행");
		l1.setFileName("통화녹음 01078782222.m4a");
		l1.setData("'지금 본인 명의로 개설 중인 계좌의 개수가 몇개이시죠? 이 부분은 만약 말씀하신 계좌의 개수와 추후 저희 금융조사팀에서 조사되는 ***님 계좌의 개수가 다르다면 대포통장의 범죄에 가담한 것으로 간주하기 때문에 여쭙는 것이고요.' --- 70%");
		l1.setCreateDate(LocalDateTime.now());
		Member member = memberRepository.findById(1).get();
		l1.setMember(member);
		this.voiceRepository.save(l1);
	}
	
	@Test
	void listTest6() {
		Voice l1 = new Voice();
		l1.setPer(60);
		l1.setTit("강남");
		l1.setFileName("통화녹음 01044443535.m4a");
		l1.setData("'저희가 얼마 전에 강남구 선릉에 위치한 오피스텔에서 성매매 알선 및 금융 위반 혐의로 주범 이지연이라는 40대 여성을 검거했는데 혹시 아시는 분이십니까?' --- 45%");
		l1.setCreateDate(LocalDateTime.now());
		Member member = memberRepository.findById(1).get();
		l1.setMember(member);
		this.voiceRepository.save(l1);
	}
}
		