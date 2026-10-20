package proyecto.asociacion.comerciantes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "socio", uniqueConstraints = @UniqueConstraint(name = "uq_socio_telefono", columnNames = "telefono"))
public class Socio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String dni;

    @Column(nullable = false, unique = true)
    private String telefono;

    @Column(nullable = false)
    private boolean estado = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "socio", fetch = FetchType.LAZY)
    private List<Puesto> puestos;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
