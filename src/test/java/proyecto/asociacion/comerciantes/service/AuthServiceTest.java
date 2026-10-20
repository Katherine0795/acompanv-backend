package proyecto.asociacion.comerciantes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import proyecto.asociacion.comerciantes.dto.ActualizarUsuarioDto;
import proyecto.asociacion.comerciantes.dto.RegistrarUsuarioDto;
import proyecto.asociacion.comerciantes.model.RolEntity;
import proyecto.asociacion.comerciantes.model.UsuarioEntity;
import proyecto.asociacion.comerciantes.repository.RolRepository;
import proyecto.asociacion.comerciantes.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, rolRepository, passwordEncoder);
    }

    private UsuarioEntity usuario(String username, String passwordPlano, boolean estado) {
        UsuarioEntity u = new UsuarioEntity();
        u.setId(1);
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(passwordPlano));
        u.setEstado(estado);
        u.setRoles(new ArrayList<>());
        return u;
    }

    @Test
    void loginCorrectoDevuelveUsuario() {
        when(usuarioRepository.findByUsername("admin1"))
                .thenReturn(Optional.of(usuario("admin1", "123456", true)));

        UsuarioEntity resultado = authService.login("admin1", "123456");

        assertEquals("admin1", resultado.getUsername());
    }

    @Test
    void loginConPasswordIncorrectoLanzaBadCredentials() {
        when(usuarioRepository.findByUsername("admin1"))
                .thenReturn(Optional.of(usuario("admin1", "123456", true)));

        assertThrows(BadCredentialsException.class, () -> authService.login("admin1", "otra"));
    }

    @Test
    void loginConUsuarioInexistenteLanzaBadCredentials() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login("nadie", "123456"));
    }

    @Test
    void loginConUsuarioInactivoLanzaDisabled() {
        when(usuarioRepository.findByUsername("cajero1"))
                .thenReturn(Optional.of(usuario("cajero1", "123456", false)));

        DisabledException ex = assertThrows(DisabledException.class,
                () -> authService.login("cajero1", "123456"));
        assertEquals("Usuario inactivo", ex.getMessage());
    }

    @Test
    void registrarGuardaPasswordCifradoConBCrypt() {
        RegistrarUsuarioDto dto = new RegistrarUsuarioDto();
        dto.setUsername("nuevo");
        dto.setPassword("clave123");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioEntity guardado = authService.registrar(dto);

        assertNotEquals("clave123", guardado.getPassword());
        assertTrue(guardado.getPassword().startsWith("$2"));
        assertTrue(passwordEncoder.matches("clave123", guardado.getPassword()));
    }

    @Test
    void actualizarSinPasswordConservaElHashActual() {
        UsuarioEntity existente = usuario("admin1", "123456", true);
        String hashAnterior = existente.getPassword();
        RolEntity rol = new RolEntity(1, "ADMIN");

        ActualizarUsuarioDto dto = new ActualizarUsuarioDto();
        dto.setUsername("admin1");
        dto.setPassword("");
        dto.setIdRol(1);
        dto.setEstado(true);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(rolRepository.findById(1)).thenReturn(Optional.of(rol));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.actualizar(1, dto);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(hashAnterior, captor.getValue().getPassword());
    }
}
