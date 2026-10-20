package proyecto.asociacion.comerciantes.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import proyecto.asociacion.comerciantes.model.UsuarioEntity;
import proyecto.asociacion.comerciantes.repository.UsuarioRepository;

// Al iniciar, cifra con BCrypt las contrasenas que aun esten en texto plano.
// Se puede ejecutar muchas veces: las ya cifradas ($2a$, $2b$, $2y$) no se tocan.
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        int migrados = 0;
        for (UsuarioEntity usuario : usuarioRepository.findAll()) {
            String password = usuario.getPassword();
            if (password != null && !password.startsWith("$2")) {
                usuario.setPassword(passwordEncoder.encode(password));
                usuarioRepository.save(usuario);
                migrados++;
            }
        }
        if (migrados > 0) {
            System.out.println("[PasswordMigrationRunner] Contrasenas cifradas con BCrypt: " + migrados);
        }
    }
}
