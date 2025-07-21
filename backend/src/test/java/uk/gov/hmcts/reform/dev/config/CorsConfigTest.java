package uk.gov.hmcts.reform.dev.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("CorsConfig Tests")
class CorsConfigTest {

    private CorsConfigurationSource source;

    @BeforeEach
    void setUp() {
        CorsConfig corsConfig = new CorsConfig();
        source = corsConfig.corsConfigurationSource();
    }

    @Nested
    @DisplayName("Configuration Source")
    class ConfigurationSource {

        @Test
        @DisplayName("should create CORS configuration source with correct type")
        void shouldCreateCorsConfigurationSourceWithCorrectType() {
            assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
        }

        @Test
        @DisplayName("should return configuration for API paths")
        void shouldReturnConfigurationForApiPaths() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");

            CorsConfiguration config = source.getCorsConfiguration(request);

            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("Allowed Values Configuration")
    class AllowedValuesConfiguration {

        private CorsConfiguration config;

        @BeforeEach
        void setUp() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            config = source.getCorsConfiguration(request);
        }

        @Test
        @DisplayName("should configure allowed origins")
        void shouldConfigureAllowedOrigins() {
            assertThat(requireNonNull(config).getAllowedOrigins())
                .hasSize(2)
                .containsExactly("http://localhost:3000", "http://frontend:3000");
        }

        @Test
        @DisplayName("should configure allowed methods")
        void shouldConfigureAllowedMethods() {
            assertThat(requireNonNull(config).getAllowedMethods())
                .hasSize(6)
                .containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        }

        @Test
        @DisplayName("should configure allowed headers")
        void shouldConfigureAllowedHeaders() {
            assertThat(requireNonNull(config).getAllowedHeaders())
                .hasSize(1)
                .containsExactly("*");
        }

        @Test
        @DisplayName("should allow credentials")
        void shouldAllowCredentials() {
            assertThat(requireNonNull(config).getAllowCredentials()).isTrue();
        }
    }

    @Nested
    @DisplayName("Path Matching")
    class PathMatching {

        @Nested
        @DisplayName("API Paths")
        class ApiPaths {

            @ParameterizedTest(name = "should apply to {0}")
            @ValueSource(strings = {
                "/api/users",
                "/api/"
            })
            @DisplayName("should apply CORS configuration")
            void shouldApplyCorsConfiguration(String path) {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI(path);

                assertThat(source.getCorsConfiguration(request)).isNotNull();
            }
        }

        @Nested
        @DisplayName("Non-API Paths")
        class NonApiPaths {

            @ParameterizedTest(name = "should not apply to {0}")
            @ValueSource(strings = {
                "/other/path",
                "/",
                "/health",
                "/status",
                "/public/info"
            })
            @DisplayName("should not apply CORS configuration")
            void shouldNotApplyCorsConfiguration(String path) {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI(path);

                assertThat(source.getCorsConfiguration(request)).isNull();
            }
        }
    }
}
