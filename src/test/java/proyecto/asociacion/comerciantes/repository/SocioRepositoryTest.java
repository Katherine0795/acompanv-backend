package proyecto.asociacion.comerciantes.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import proyecto.asociacion.comerciantes.model.Socio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de la capa de acceso a datos: insertar, actualizar, eliminar y listar.
// Cada prueba se revierte al terminar (@Transactional).
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SocioRepositoryTest {

    @Autowired
    private ISocioRepository socioRepository;

    private Socio nuevoSocio(String nombre, String telefono) {
        Socio socio = new Socio();
        socio.setNombre(nombre);
        socio.setDni("12345678");
        socio.setTelefono(telefono);
        return socio;
    }

    @Test
    void insertarSocio() {
        Socio guardado = socioRepository.save(nuevoSocio("Juan Perez", "900000001"));

        assertNotNull(guardado.getId());
        assertNotNull(guardado.getFechaCreacion());
        assertTrue(guardado.isEstado());
    }

    @Test
    void actualizarSocio() {
        Socio guardado = socioRepository.save(nuevoSocio("Juan Perez", "900000001"));

        guardado.setNombre("Juan Perez Actualizado");
        guardado.setTelefono("911111111");
        socioRepository.saveAndFlush(guardado);

        Socio leido = socioRepository.findById(guardado.getId()).orElseThrow();
        assertEquals("Juan Perez Actualizado", leido.getNombre());
        assertEquals("911111111", leido.getTelefono());
    }

    @Test
    void eliminarSocio() {
        Socio guardado = socioRepository.save(nuevoSocio("Juan Perez", "900000001"));
        Long id = guardado.getId();

        socioRepository.deleteById(id);
        socioRepository.flush();

        assertTrue(socioRepository.findById(id).isEmpty());
    }

    @Test
    void listarSocios() {
        socioRepository.save(nuevoSocio("Juan Perez", "900000001"));
        socioRepository.save(nuevoSocio("Maria Lopez", "900000002"));

        List<Socio> socios = socioRepository.findAll();

        assertEquals(2, socios.size());
    }
}
