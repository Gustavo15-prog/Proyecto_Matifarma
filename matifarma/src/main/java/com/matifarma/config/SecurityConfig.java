package com.matifarma.config;

import com.matifarma.dao.EmpleadoDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, LoginSuccessHandler exito, LoginFailureHandler fallo,
                                           EmpleadoDao empleadoDao) throws Exception {
        final String ADMIN = "ADMINISTRADOR", VEND = "VENDEDOR", ALM = "ALMACENERO";
        http
            .addFilterBefore(new EstadoSesionFilter(empleadoDao), AuthorizationFilter.class)
            .authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                // Consultas compartidas por todos los roles
                .requestMatchers("/dashboard", "/consultar-stock", "/api/dashboard", "/api/stock", "/api/productos/buscar").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/categorias").authenticated()
                // Inventario: almacenero y administrador
                .requestMatchers("/inventario", "/api/inventario/**", "/api/proveedores/activos").hasAnyRole(ADMIN, ALM)
                // Ventas: vendedor y administrador
                .requestMatchers("/ventas", "/api/ventas/**").hasAnyRole(ADMIN, VEND)
                // Solo administrador
                .requestMatchers("/productos", "/proveedores", "/alertas", "/reportes", "/usuarios",
                        "/api/productos/**", "/api/categorias", "/api/proveedores/**", "/api/alertas/**",
                        "/api/reportes/**", "/api/usuarios/**").hasRole(ADMIN)
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").loginProcessingUrl("/login")
                .successHandler(exito).failureHandler(fallo).permitAll())
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID"))
            .exceptionHandling(e -> e
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**"))
                .accessDeniedHandler((req, res, ex) -> {
                    if (req.getRequestURI().startsWith("/api/")) {
                        res.setStatus(HttpStatus.FORBIDDEN.value());
                        res.setContentType("application/json;charset=UTF-8");
                        res.getWriter().write("{\"mensaje\":\"No tiene permisos para realizar esta operación\",\"errores\":{}}");
                    } else {
                        res.sendRedirect(req.getContextPath() + "/dashboard?denegado");
                    }
                }));
        return http.build();
    }
}
