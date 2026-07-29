package com.interx.onboarding.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError(400, ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        // 로그인 실패는 브루트포스 탐지를 위해 서버 로그에도 남긴다 (자세한 사유는 노출하지 않음)
        log.warn("인증 실패: path={}", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(401, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("권한 없는 접근 시도: path={}", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(403, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "입력값을 확인해주세요."
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiError(400, message));
    }

    /**
     * 예상치 못한 예외는 원인(스택트레이스 포함)을 서버 로그에 남기되, 클라이언트에는 내부 구현
     * 세부사항(SQL, 클래스명, 스택트레이스 등)이 노출되지 않도록 안전한 메시지만 반환한다.
     * (예전엔 ex.getMessage()를 그대로 응답에 포함시켜, DB 컬럼 길이 문제 같은 내부 오류가 그대로
     * API 응답에 노출된 적이 있었음 — 디버깅에는 편했지만 운영 환경에서는 정보 노출 위험이 있다.)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        log.error("처리되지 않은 예외 발생: path={}", request.getDescription(false), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
