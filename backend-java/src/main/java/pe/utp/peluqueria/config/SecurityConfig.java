package pe.utp.peluqueria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.utp.peluqueria.security.JwtAuthFilter;

/** Seguridad de la API: sin estado, autenticación por JWT y autorización por rol. */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /** La API no usa cookies de sesión (el token viaja en el encabezado), por eso CSRF se desactiva aquí. */
    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/servicios", "/api/estilistas", "/api/estilistas/*/horarios-libres").permitAll()
                .requestMatchers("/api/admin/**", "/api/notificaciones/pendientes",
                        "/api/notificaciones/*/enviada", "/api/notificaciones/*/fallida").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/agenda/**").hasAnyRole("ESTILISTA", "ADMINISTRADOR")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> res.sendError(401)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
