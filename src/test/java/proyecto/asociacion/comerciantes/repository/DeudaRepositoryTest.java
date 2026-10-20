package proyecto.asociacion.comerciantes.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import proyecto.asociacion.comerciantes.model.Deuda;
import proyecto.asociacion.comerciantes.model.EstadoDeuda;
import proyecto.asociacion.comerciantes.model.MotivoCobroEntity;
import proyecto.asociacion.comerciantes.model.Puesto;
import proyecto.asociacion.comerciantes.model.Socio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de la capa de acceso a datos: insertar, actualizar, eliminar y listar.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeudaRepositoryTest {

    @Autowired
    private IDeudaRepository deudaRepository;

    @Autowired
    private ISocioRepository socioRepository;

    @Autowired
    private IPuestoRepository puestoRepository;

    @Autowired
    private MotivoCobroRepository motivoCobroRepository;

    private int secuencia = 0;

    private Deuda nuevaDeuda(String monto) {
        secuencia++;

        Socio socio = new Socio();
        socio.setNombre("Carlos Ramos");
        socio.setTelefono("90000000" + secuencia);
        socio = socioRepository.save(socio);

        Puesto puesto = new Puesto();
        puesto.setCodigo("P00" + secuencia);
        puesto.setSocio(socio);
        puesto = puestoRepository.save(puesto);

        MotivoCobroEntity motivo = new MotivoCobroEntity();
        motivo.setDescripcion("Mantenimiento");
        motivo = motivoCobroRepository.save(motivo);

        Deuda deuda = new Deuda();
        deuda.setSocio(socio);
        deuda.setPuesto(puesto);
        deuda.setMotivo(motivo);
        deuda.setMonto(new BigDecimal(monto));
        deuda.setFecha(LocalDate.of(2026, 5, 1));
        return deuda;
    }

    @Test
    void insertarDeuda() {
        Deuda guardada = deudaRepository.save(nuevaDeuda("50.00"));

        assertNotNull(guardada.getId());
        assertEquals(EstadoDeuda.PENDIENTE, guardada.getEstado());
        assertNotNull(guardada.getFechaCreacion());
    }

    @Test
    void actualizarDeuda() {
        Deuda guardada = deudaRepository.save(nuevaDeuda("50.00"));

        guardada.setMonto(new BigDecimal("75.50"));
        guardada.setEstado(EstadoDeuda.PAGADA);
        guardada.setFechaPago(LocalDate.of(2026, 5, 10));
        deudaRepository.saveAndFlush(guardada);

        Deuda leida = deudaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("75.50").compareTo(leida.getMonto()));
        assertEquals(EstadoDeuda.PAGADA, leida.getEstado());
        assertEquals(LocalDate.of(2026, 5, 10), leida.getFechaPago());
    }

    @Test
    void eliminarDeuda() {
        Deuda guardada = deudaRepository.save(nuevaDeuda("50.00"));
        Long id = guardada.getId();

        deudaRepository.deleteById(id);
        deudaRepository.flush();

        assertTrue(deudaRepository.findById(id).isEmpty());
    }

    @Test
    void listarDeudas() {
        deudaRepository.save(nuevaDeuda("50.00"));
        deudaRepository.save(nuevaDeuda("20.00"));

        assertEquals(2, deudaRepository.findAll().size());
    }
}
