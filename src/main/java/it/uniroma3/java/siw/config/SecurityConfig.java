package it.uniroma3.java.siw.config;

import it.uniroma3.java.siw.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(customUserDetailsService)
            .authorizeHttpRequests(auth -> auth
                // Risorse statiche sempre accessibili
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Pagine pubbliche di autenticazione
                .requestMatchers("/", "/login", "/register").permitAll()
                // UC4: iscriviti a un corso — autenticato (prima della regola pubblica sui corsi)
                .requestMatchers(new AntPathRequestMatcher("/corsi/*/iscriviti", "POST")).hasAnyRole("USER", "ADMIN")
                // UC1 e UC2: lista e dettaglio corsi — pubblici
                .requestMatchers("/corsi", "/corsi/**").permitAll()
                // UC3: lista e dettaglio istruttori — pubblici
                .requestMatchers("/istruttori", "/istruttori/**").permitAll()
                // Abbonamenti — pubblici (sola lettura)
                .requestMatchers("/abbonamenti", "/abbonamenti/**").permitAll()
                // Pagine di errore — sempre accessibili
                .requestMatchers("/error/**").permitAll()
                // Iscrizioni — utenti autenticati
                .requestMatchers("/iscrizioni/**").hasAnyRole("USER", "ADMIN")
                // Profilo utente — autenticato
                .requestMatchers("/profilo/**").hasAnyRole("USER", "ADMIN")
                // Tutto /admin/** — solo ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Qualsiasi altra richiesta — autenticata
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Redirect post-login in base al ruolo:
     * ADMIN → /admin/corsi  |  USER → /corsi
     */
    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            boolean isAdmin = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            String target = isAdmin ? "/admin/corsi" : "/corsi";
            response.sendRedirect(request.getContextPath() + target);
        };
    }
}

