package com.salah.taskmate.shared.aspect;

import com.salah.taskmate.shared.annotation.StandardApiResponse;
import com.salah.taskmate.shared.api.ApiResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class ApiResponseAspect {
    @Around("@annotation(standardApiResponse)")
    public Object standardResponse(ProceedingJoinPoint joinPoint, StandardApiResponse standardApiResponse) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();

            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .success(true)
                    .message(standardApiResponse.message())
                    .data(body)
                    .timestamp(LocalDateTime.now())
                    .status(responseEntity.getStatusCodeValue())
                    .build();

            return ResponseEntity.status(responseEntity.getStatusCode())
                    .headers(responseEntity.getHeaders())
                    .body(apiResponse);
        }


        return ApiResponse.builder()
                .success(true)
                .message(standardApiResponse.message())
                .data(result)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .build();
    }
}
