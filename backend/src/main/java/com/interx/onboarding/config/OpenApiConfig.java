package com.interx.onboarding.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 명세서(OpenAPI/Swagger) 설정.
 * /swagger-ui/index.html 에서 인터랙티브 문서를 볼 수 있고, Authorize 버튼으로
 * JWT 액세스 토큰을 넣으면 인증이 필요한 엔드포인트도 직접 호출해볼 수 있다.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI interAToZOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("INTER A to Z API")
                        .description("인터엑스 신규 입사자 온보딩 포털 백엔드 API 명세서")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
