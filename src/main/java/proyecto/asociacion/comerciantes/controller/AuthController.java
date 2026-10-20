package proyecto.asociacion.comerciantes.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyecto.asociacion.comerciantes.dto.ActualizarUsuarioDto;
import proyecto.asociacion.comerciantes.dto.RegistrarUsuarioConRolDto;
import proyecto.asociacion.comerciantes.dto.RegistrarUsuarioDto;
import proyecto.asociacion.comerciantes.model.UsuarioEntity;
import proyecto.asociacion.comerciantes.service.AuthService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REGISTRAR USUARIO
    @PostMapping("/register")
    public ResponseEntity<UsuarioEntity> registrar(@Valid @RequestBody RegistrarUsuarioDto dto) {
        return ResponseEntity.ok(authService.registrar(dto));
    }

    // ACTUALIZAR USUARIO
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<UsuarioEntity> actualizar(
            @PathVariable Integer id,
            @RequestBody ActualizarUsuarioDto dto) {

        return ResponseEntity.ok(
                authService.actualizar(id, dto)
        );
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<UsuarioEntity> login(@Valid @RequestBody RegistrarUsuarioDto dto) {
        return ResponseEntity.ok(
                authService.login(dto.getUsername(), dto.getPassword())
        );
    }

    // LISTAR
    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioEntity>> listar() {
        return ResponseEntity.ok(authService.listar());
    }

    // ASIGNAR ROL
    @PostMapping("/asignar-rol")
    public ResponseEntity<UsuarioEntity> asignarRol(
            @RequestParam Integer idUsuario,
            @RequestParam Integer idRol) {

        return ResponseEntity.ok(authService.asignarRol(idUsuario, idRol));
    }

    // REGISTRAR CON ROL
    @PostMapping("/registrar-con-rol")
    public ResponseEntity<UsuarioEntity> registrarConRol(
            @Valid @RequestBody RegistrarUsuarioConRolDto dto) {

        return ResponseEntity.ok(authService.registrarConRol(dto));
    }
}