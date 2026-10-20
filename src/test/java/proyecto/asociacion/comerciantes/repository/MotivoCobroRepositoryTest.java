package proyecto.asociacion.comerciantes.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import proyecto.asociacion.comerciantes.model.MotivoCobroEntity;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de la capa de acceso a datos: insertar, actualizar, eliminar y listar.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MotivoCobroRepositoryTest {

    @Autowired
    private MotivoCobroRepository motivoCobroRepository;

    private MotivoCobroEntity nuevoMotivo(String descripcion) {
        MotivoCobroEntity motivo = new MotivoCobroEntity();
        motivo.setDescripcion(descripcion);
        return motivo;
    }

    @Test
    void insertarMotivo() {
        MotivoCobroEntity guardado = motivoCobroRepository.save(nuevoMotivo("Limpieza"));

        assertNotNull(guardado.getId());
    }

    @Test
    void actualizarMotivo() {
        MotivoCobroEntity guardado = motivoCobroRepository.save(nuevoMotivo("Limpieza"));

        guardado.setDescripcion("Limpieza mensual");
        motivoCobroRepository.saveAndFlush(guardado);

        assertEquals("Limpieza mensual",
                motivoCobroRepository.findById(guardado.getId()).orElseThrow().getDescripcion());
    }

    @Test
    void eliminarMotivo() {
        MotivoCobroEntity guardado = motivoCobroRepository.save(nuevoMotivo("Limpieza"));
        Integer id = guardado.getId();

        motivoCobroRepository.deleteById(id);
        motivoCobroRepository.flush();

        assertTrue(motivoCobroRepository.findById(id).isEmpty());
    }

    @Test
    void listarMotivos() {
        motivoCobroRepository.save(nuevoMotivo("Limpieza"));
        motivoCobroRepository.save(nuevoMotivo("Seguridad"));
        motivoCobroRepository.save(nuevoMotivo("Mantenimiento"));

        assertEquals(3, motivoCobroRepository.findAll().size());
    }
}
