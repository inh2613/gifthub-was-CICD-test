package org.swmaestro.repl.gifthub.notifications.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.NotificationReadResponseDto;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private NotificationService notificationService;

	/**
	 * 알림 목록 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void listNotificationTest() throws Exception {
		// given
		String accessToken = "myAccessToken";
		String username = "이진우";
		List<NotificationReadResponseDto> notifications = new ArrayList<>();
		notifications.add(NotificationReadResponseDto.builder()
				.id(1L)
				.type(NotificationType.EXPIRATION)
				.message("유효기간이 3일 남았습니다.")
				.voucherId(1L)
				.notifiedAt(LocalDateTime.now())
				.build());
		// when
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(notificationService.list(username)).thenReturn(notifications);
		// then
		mockMvc.perform(get("/notifications")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].id").value(1L))
				.andExpect(jsonPath("$.data[0].type").value(NotificationType.EXPIRATION.name()))
				.andExpect(jsonPath("$.data[0].message").value("유효기간이 3일 남았습니다."))
				.andReturn();

	}
}
