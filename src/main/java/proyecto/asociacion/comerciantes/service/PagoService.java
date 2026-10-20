package proyecto.asociacion.comerciantes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proyecto.asociacion.comerciantes.dto.DetallePagoDto;
import proyecto.asociacion.comerciantes.dto.PagoDto;
import proyecto.asociacion.comerciantes.model.ComprobanteEntity;
import proyecto.asociacion.comerciantes.model.DetallePagoEntity;
import proyecto.asociacion.comerciantes.model.PagoEntity;
import proyecto.asociacion.comerciantes.mappers.PagoMapper;
import proyecto.asociacion.comerciantes.repository.ComprobanteRepository;
import proyecto.asociacion.comerciantes.repository.DetallePagoRepository;
import proyecto.asociacion.comerciantes.repository.PagoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PagoService {
    private PagoRepository pagoRepository;

    private PagoMapper pagoMapper;

    private ComprobanteRepository comprobanteRepository;

    private DetallePagoRepository detallePagoRepository;

    public PagoService(PagoRepository pagoRepository,
                              PagoMapper pagoMapper2,
                              ComprobanteRepository comprobanteRepository,
                              DetallePagoRepository detallePagoRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.pagoMapper = pagoMapper2;
        this.comprobanteRepository = comprobanteRepository;
        this.detallePagoRepository = detallePagoRepository;
    }

    public List<PagoDto> listar(){
        List<PagoEntity> listaEntity = pagoRepository.findAll();
        List<PagoDto> listaDto = pagoMapper.toDtoList(listaEntity);
        return listaDto;
    }

    @Transactional
    public PagoDto registrar(PagoDto pagoDto) {

        if (pagoDto.getMetodo() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        if (pagoDto.getDetalles() == null || pagoDto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un comprobante");
        }

        PagoEntity pagoEntity = new PagoEntity();
        pagoEntity.setMetodo(pagoDto.getMetodo());
        pagoEntity.setFechaRegistro(LocalDateTime.now());

        BigDecimal totalComprobantes = BigDecimal.ZERO;
        List<DetallePagoEntity> detalles = new ArrayList<>();

        for (DetallePagoDto detalleDto : pagoDto.getDetalles()) {
            Long idComprobante = detalleDto.getIdComprobante() != null
                    ? detalleDto.getIdComprobante()
                    : (detalleDto.getComprobante() != null ? detalleDto.getComprobante().getId() : null);

            if (idComprobante == null) {
                throw new IllegalArgumentException("Cada detalle debe indicar el comprobante");
            }

            ComprobanteEntity comprobante = comprobanteRepository.findById(idComprobante)
                    .orElseThrow(() -> new NoSuchElementException("No existe el comprobante " + idComprobante));

            if (detallePagoRepository.existsByComprobante_Id(idComprobante)) {
                throw new IllegalStateException("El comprobante " + comprobante.getNumero() + " ya tiene un pago registrado");
            }

            DetallePagoEntity detalle = new DetallePagoEntity();
            detalle.setComprobante(comprobante);
            detalles.add(detalle);
            totalComprobantes = totalComprobantes.add(comprobante.getTotal());
        }

        // Pago total: el monto recibido debe cubrir todos los comprobantes
        BigDecimal monto = pagoDto.getMontoAcumulado() != null ? pagoDto.getMontoAcumulado() : totalComprobantes;
        if (monto.compareTo(totalComprobantes) < 0) {
            throw new IllegalArgumentException("El pago debe ser total: se requieren S/ " + totalComprobantes);
        }
        pagoEntity.setMontoAcumulado(monto);
        pagoEntity.setDetalles(detalles);

        PagoEntity guardado = pagoRepository.save(pagoEntity);
        return pagoMapper.toDto(guardado);
    }
}
