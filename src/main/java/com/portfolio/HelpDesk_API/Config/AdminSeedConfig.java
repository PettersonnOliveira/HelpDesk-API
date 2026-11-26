package com.portfolio.HelpDesk_API.Config;

import com.portfolio.HelpDesk_API.Repositories.UserRepository;
import com.portfolio.HelpDesk_API.entities.Role;
import com.portfolio.HelpDesk_API.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedConfig.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeedConfig(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${admin.email:admin@sistema.com}") String adminEmail,
                           @Value("${admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        String email = adminEmail == null ? "" : adminEmail.trim();

        if (email.isBlank()) {
            log.warn("ADMIN_EMAIL não configurado; seed do usuário administrador ignorado.");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_PASSWORD não definida; seed do usuário administrador ignorado.");
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.info("Usuário administrador já existe para o e-mail {}. Nenhuma ação necessária.", email);
            return;
        }

        User admin = new User(
                "Administrador",
                email,
                passwordEncoder.encode(adminPassword),
                Role.ADMIN
        );

        userRepository.save(admin);
        log.info("Usuário administrador padrão criado com sucesso para o e-mail {}.", email);
    }
}
