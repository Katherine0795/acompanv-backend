package proyecto.asociacion.comerciantes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class RegistrarUsuarioConRolDto {

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Integer idRol;
}
