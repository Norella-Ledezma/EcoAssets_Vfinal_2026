package com.ecommerce.electronicos.seguridad;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; 
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    // 1. Inyectamos nuestro nuevo filtro JWT
    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Es buena práctica desactivar CSRF cuando pasamos a JWT
            .csrf(csrf -> csrf.disable()) 
            
            .authorizeHttpRequests(auth -> auth
                // Público: Ver productos y detalles, registro, y agregamos /login para nuestro login manual
                .requestMatchers("/", "/dispositivos", "/dispositivos/detalle/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/img/**").permitAll()
                .requestMatchers("/registro", "/login", "/error").permitAll()
                
                // Solo Admin: Panel admin y eliminar cualquier usuario
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/usuarios/eliminar/**").authenticated()
                
                // Publicar productos 
                .requestMatchers("/dispositivos/nuevo", "/dispositivos/guardar").authenticated()
                
                // Ver perfil propio 
                .requestMatchers("/usuarios/perfil/**").authenticated()
                
                // Eliminar dispositivos
                .requestMatchers("/dispositivos/eliminar/**").authenticated()
                .anyRequest().authenticated()
            )
            
            // 2. Desactivar login automático
            .formLogin(form -> form.disable())

            .oauth2Login(oauth2 -> oauth2
            .loginPage("/login") // Usa la misma página de login
            .successHandler(oAuth2AuthenticationSuccessHandler) // Nuestro manejador personalizado
        )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                // Borraremos la cookie donde guardaremos el JWT más adelante
                .deleteCookies("JWT_TOKEN") 
            )
            
            // 3. Manejo de excepciones mixto (API vs Web)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"No autorizado\"}");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                })
                .accessDeniedPage("/dispositivos")
            )
            
            // 4. Declaramos que ya no guardaremos sesión en el servidor (STATELESS)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 5. Comentado [LM32] de la guía: Insertar el filtro JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

}
