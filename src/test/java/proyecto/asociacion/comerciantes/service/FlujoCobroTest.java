package proyecto.asociacion.comerciantes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import proyecto.asociacion.comerciantes.dto.ComprobanteDto;
import proyecto.asociacion.comerciantes.dto.DetalleComprobanteDto;
import proyecto.asociacion.comerciantes.dto.DetallePagoDto;
import proyecto.asociacion.comerciantes.dto.PagoDto;
import proyecto.asociacion.comerciantes.dto.ReporteCajaDto;
import proyecto.asociacion.comerciantes.model.Deuda;
import proyecto.asociacion.comerciantes.model.EstadoDeuda;
import proyecto.asociacion.comerciantes.model.MetodoPago;
import proyecto.asociacion.comerciantes.model.MotivoCobroEntity;
import proyecto.asociacion.comerciantes.model.Puesto;
import proyecto.asociacion.comerciantes.model.Socio;
import proyecto.asociacion.comerciantes.repository.IDeudaRepository;
import proyecto.asociacion.comerciantes.repository.IPuestoRepository;
import proyecto.asociacion.comerciantes.repository.ISocioRepository;
import proyecto.asociacion.comerciantes.repository.MotivoCobroRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Flujo completo: deudas -> comprobante (pago total) -> pago -> reporte de caja
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FlujoCobroTest {

    @Autowired private ComprobanteService comprobanteService;
    @Autowired private PagoService pagoService;
    @Autowired private ReporteCajaService reporteCajaService;
    @Autowired private ISocioRepository socioRepository;
    @Autowired private IPuestoRepository puestoRepository;
    @Autowired private MotivoCobroRepository motivoCobroRepository;
    @Autowired private IDeudaRepository deudaRepository;

    private Puesto puesto;
    private Deuda deuda1;
    private Deuda deuda2;

    @BeforeEach
    void datos() {
        Socio socio = new Socio();
        socio.setNombre("Juan Perez");
        socio.setTelefono("900000001");
        socio = socioRepository.save(socio);

        puesto = new Puesto();
        puesto.setCodigo("P001");
        puesto.setSocio(socio);
        puesto = puestoRepository.save(puesto);

        MotivoCobroEntity motivo = new MotivoCobroEntity();
        motivo.setDescripcion("Mantenimiento");
        motivo = motivoCobroRepository.save(motivo);

        deuda1 = deudaRepository.save(deuda(socio, motivo, "50.00"));
        deuda2 = deudaRepository.save(deuda(socio, motivo, "20.00"));
    }

    private Deuda deuda(Socio socio, MotivoCobroEntity motivo, String monto) {
        Deuda d = new Deuda();
        d.setSocio(socio);
        d.setPuesto(puesto);
        d.setMotivo(motivo);
        d.setMonto(new BigDecimal(monto));
        d.setFecha(LocalDate.of(2026, 5, 1));
        return d;
    }

    private ComprobanteDto comprobante(String numero, Long... idsDeuda) {
        ComprobanteDto dto = new ComprobanteDto();
        dto.setNumero(numero);
        dto.setTipo("BOLETA");
        dto.setIdPuesto(puesto.getId());
        dto.setVuelto(BigDecimal.ZERO);
        List<DetalleComprobanteDto> detalles = new ArrayList<>();
        for (Long id : idsDeuda) {
            DetalleComprobanteDto d = new DetalleComprobanteDto();
            d.setIdDeuda(id);
            detalles.add(d);
        }
        dto.setListaDetalle(detalles);
        return dto;
    }

    private PagoDto pago(MetodoPago metodo, String monto, Long idComprobante) {
        PagoDto pago = new PagoDto();
        pago.setMetodo(metodo);
        pago.setMontoAcumulado(new BigDecimal(monto));
        DetallePagoDto detalle = new DetallePagoDto();
        detalle.setIdComprobante(idComprobante);
        pago.setDetalles(List.of(detalle));
        return pago;
    }

    @Test
    void comprobanteCancelaDeudasYCalculaTotal() {
        ComprobanteDto resultado = comprobanteService.registrar(
                comprobante("C-001", deuda1.getId(), deuda2.getId()));

        assertNotNull(resultado.getId());
        assertEquals(0, new BigDecimal("70.00").compareTo(resultado.getTotal()));

        Deuda leida1 = deudaRepository.findById(deuda1.getId()).orElseThrow();
        Deuda leida2 = deudaRepository.findById(deuda2.getId()).orElseThrow();
        assertEquals(EstadoDeuda.PAGADA, leida1.getEstado());
        assertEquals(EstadoDeuda.PAGADA, leida2.getEstado());
        assertNotNull(leida1.getFechaPago());
    }

    @Test
    void noSePuedePagarDosVecesLaMismaDeuda() {
        comprobanteService.registrar(comprobante("C-001", deuda1.getId()));

        assertThrows(IllegalStateException.class,
                () -> comprobanteService.registrar(comprobante("C-002", deuda1.getId())));
    }

    @Test
    void pagoConIdComprobanteSeRegistraYApareceEnElReportePorMetodo() {
        ComprobanteDto c = comprobanteService.registrar(
                comprobante("C-001", deuda1.getId(), deuda2.getId()));

        PagoDto registrado = pagoService.registrar(pago(MetodoPago.YAPE, "70.00", c.getId()));
        assertNotNull(registrado.getId());

        ReporteCajaDto reporte = reporteCajaService.generarReporte(LocalDate.now(), LocalDate.now());
        assertEquals(0, new BigDecimal("70.00").compareTo(reporte.getTotalGeneral()));
        assertEquals(0, new BigDecimal("70.00").compareTo(reporte.getTotalPorMetodoPago().get("YAPE")));
    }

    @Test
    void pagoMenorAlTotalSeRechaza() {
        ComprobanteDto c = comprobanteService.registrar(comprobante("C-001", deuda1.getId()));

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.registrar(pago(MetodoPago.EFECTIVO, "10.00", c.getId())));
    }

    @Test
    void comprobanteNoPuedePagarseDosVeces() {
        ComprobanteDto c = comprobanteService.registrar(comprobante("C-001", deuda1.getId()));
        pagoService.registrar(pago(MetodoPago.EFECTIVO, "50.00", c.getId()));

        assertThrows(IllegalStateException.class,
                () -> pagoService.registrar(pago(MetodoPago.PLIN, "50.00", c.getId())));
    }
}
