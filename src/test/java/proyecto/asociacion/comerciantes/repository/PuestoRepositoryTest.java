package proyecto.asociacion.comerciantes.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import proyecto.asociacion.comerciantes.model.Puesto;
import proyecto.asociacion.comerciantes.model.Socio;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de la capa de acceso a datos: insertar, actualizar, eliminar y listar.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PuestoRepositoryTest {

    @Autowired
    private IPuestoRepository puestoRepository;

    @Autowired
    private ISocioRepository socioRepository;

    private Puesto nuevoPuesto(String codigo, Socio socio) {
        Puesto puesto = new Puesto();
        puesto.setCodigo(codigo);
        puesto.setDescripcion("Puesto de prueba " + codigo);
        puesto.setSocio(socio);
        return puesto;
    }

    private Socio socio() {
        Socio socio = new Socio();
        socio.setNombre("Ana Torres");
        socio.setTelefono("900000004");
        return socioRepository.save(socio);
    }

    @Test
    void insertarPuesto() {
        Puesto guardado = puestoRepository.save(nuevoPuesto("P001", socio()));

        assertNotNull(guardado.getId());
        assertTrue(guardado.isEstado());
        assertNotNull(guardado.getSocio());
    }

    @Test
    void actualizarPuesto() {
        Puesto guardado = puestoRepository.save(nuevoPuesto("P001", null));

        guardado.setDescripcion("Puesto de frutas");
        guardado.setEsPropiedad(true);
        puestoRepository.saveAndFlush(guardado);

        Puesto leido = puestoRepository.findById(guardado.getId()).orElseThrow();
        assertEquals("Puesto de frutas", leido.getDescripcion());
        assertTrue(leido.isEsPropiedad());
    }

    @Test
    void eliminarPuesto() {
        Puesto guardado = puestoRepository.save(nuevoPuesto("P001", null));
        Long id = guardado.getId();

        puestoRepository.deleteById(id);
        puestoRepository.flush();

        assertTrue(puestoRepository.findById(id).isEmpty());
    }

    @Test
    void listarPuestos() {
        puestoRepository.save(nuevoPuesto("P001", null));
        puestoRepository.save(nuevoPuesto("P002", null));

        assertEquals(2, puestoRepository.findAll().size());
    }

    @Test
    void listarPuestosPorSocio() {
        Socio socio = socio();
        puestoRepository.save(nuevoPuesto("P001", socio));
        puestoRepository.save(nuevoPuesto("P002", socio));
        puestoRepository.save(nuevoPuesto("P003", null));

        List<Puesto> puestos = puestoRepository.findBySocioId(socio.getId());

        assertEquals(2, puestos.size());
    }
}
