package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.swmaestro.repl.gifthub.auth.dto.NaverDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
public class NaverService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	@Value("${naver.client-id}")
	private String clientId;
	@Value("${naver.state}")
	private String state;
	@Value("${naver.response-type}")
	private String responseType;
	@Value("${naver.authorization-uri}")
	private String authorizationUri;
	@Value("${naver.redirect-uri}")
	private String redirectUri;
	@Value("${naver.client-secret}")
	private String clientSecret;
	@Value("${naver.token-uri}")
	private String tokenUri;
	@Value("${naver.user-info-uri}")
	private String userInfoUri;
	private final JsonParser parser = new JsonParser();
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	public String getAuthorizationUrl() {
		return UriComponentsBuilder
				.fromUriString(authorizationUri)
				.queryParam("client_id", clientId)
				.queryParam("response_type", responseType)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("state", state)
				.build().toString();
	}

	public TokenDto getNaverToken(String type, String code) throws IOException {
		URL url = new URL(UriComponentsBuilder
				.fromUriString(tokenUri)
				.queryParam("grant_type", "authorization_code")
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", code)
				.build()
				.toString());

		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		BufferedReader br;

		if (responseCode == 200) { // 정상 호출
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else {  // 에러 발생
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}

		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}

		br.close();

		JsonElement element = parser.parse(response.toString());

		return TokenDto.builder()
				.accessToken(element.getAsJsonObject().get("access_token").getAsString())
				.refreshToken(element.getAsJsonObject().get("refresh_token").getAsString())
				.build();
	}

	public NaverDto getUserInfo(TokenDto token) throws IOException {
		String accessToken = token.getAccessToken();

		URL url = new URL(userInfoUri);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Bearer " + accessToken);

		int responseCode = con.getResponseCode();
		BufferedReader br;

		if (responseCode == 200) { // 정상 호출
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else {  // 에러 발생
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}

		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}

		br.close();

		JsonElement element = parser.parse(response.toString());

		return NaverDto.builder()
				.id(element.getAsJsonObject().get("response").getAsJsonObject().get("id").getAsString())
				.email(element.getAsJsonObject().get("response").getAsJsonObject().get("email").getAsString())
				.nickname(element.getAsJsonObject().get("response").getAsJsonObject().get("nickname").getAsString())
				.build();
	}

	public Member signUp(NaverDto naverDto) {
		SignUpDto signUpDto = SignUpDto.builder()
				.username(naverDto.getEmail())
				.nickname(naverDto.getNickname())
				.password(naverDto.getId())
				.build();

		if (!memberService.isDuplicateUsername(naverDto.getEmail())) {
			memberService.create(signUpDto);
		}

		return memberService.read(naverDto.getEmail());
	}

	public TokenDto signIn(NaverDto naverDto) {
		String accessToken = jwtProvider.generateToken(naverDto.getEmail());
		String refreshToken = jwtProvider.generateRefreshToken(naverDto.getEmail());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, naverDto.getEmail());

		return tokenDto;
	}
}
