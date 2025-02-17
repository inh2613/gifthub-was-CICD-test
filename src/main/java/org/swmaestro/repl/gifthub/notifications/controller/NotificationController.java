package org.swmaestro.repl.gifthub.notifications.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림 관련 API")
public class NotificationController {
	private final NotificationService notificationService;
	private final JwtProvider jwtProvider;

	@GetMapping
	@Operation(summary = "Notification 목록 조회 메서드", description = "클라이언트에서 요청한 알림 목록 정보를 조회하기 위한 메서드입니다. 응답으로 알림 type, message, notified date, 기프티콘 정보를 반환합니다.")
	public ResponseEntity<Message> listNotification(@RequestHeader("Authorization") String accessToken) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("알림 목록 조회에 성공하였습니다!")
						.data(notificationService.list(username))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}
}
