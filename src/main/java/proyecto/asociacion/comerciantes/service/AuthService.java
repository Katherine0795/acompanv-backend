package proyecto.asociacion.comerciantes.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proyecto.asociacion.comerciantes.dto.RegistrarUsuarioConRolDto;
import proyecto.asociacion.comerciantes.dto.RegistrarUsuarioDto;
import proyecto.asociacion.comerciantes.dto.ActualizarUsuarioDto;
import proyecto.asociacion.comerciantes.model.RolEntity;
import proyecto.asociacion.comerciantes.model.UsuarioEntity;
import proyecto.asociacion.comerciantes.repository.UsuarioRepository;
import proyecto.asociacion.comerciantes.repository.RolRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTRO
    public UsuarioEntity registrar(RegistrarUsuarioDto dto) {

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(true);

        return usuarioRepository.save(usuario);
    }

    // LISTAR USUARIOS
    public List<UsuarioEntity> listar() {
        return usuarioRepository.findAll();
    }

    // ACTUALIZAR USUARIO
    public UsuarioEntity actualizar(
            Integer id,
            ActualizarUsuarioDto dto) {

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        usuario.setUsername(dto.getUsername());

        if (dto.getPassword() != null &&
                !dto.getPassword().isBlank()) {

            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        RolEntity rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no existe"));

        usuario.setRoles(new ArrayList<>());
        usuario.getRoles().add(rol);

        usuario.setEstado(dto.getEstado());

        return usuarioRepository.save(usuario);
    }

    // LOGIN
    public UsuarioEntity login(String username, String password) {

        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario o contrasena incorrectos"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BadCredentialsException("Usuario o contrasena incorrectos");
        }

        if (!Boolean.TRUE.equals(usuario.getEstado())) {
            throw new DisabledException("Usuario inactivo");
        }

        return usuario;
    }

    // ASIGNAR ROL
    public UsuarioEntity asignarRol(Integer idUsuario, Integer idRol) {

        UsuarioEntity usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        RolEntity rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no existe"));

        usuario.getRoles().add(rol);

        return usuarioRepository.save(usuario);
    }

    // REGISTRAR USUARIO CON ROL
    public UsuarioEntity registrarConRol(RegistrarUsuarioConRolDto dto) {

        // 1. Crear usuario
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(true);

        // 2. Buscar rol
        RolEntity rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no existe"));

        // 3. Asignar rol
        usuario.setRoles(new ArrayList<>());
        usuario.getRoles().add(rol);

        // 4. Guardar todo
        return usuarioRepository.save(usuario);
    }
}