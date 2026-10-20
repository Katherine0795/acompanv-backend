package proyecto.asociacion.comerciantes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyecto.asociacion.comerciantes.dto.ComprobanteDto;
import proyecto.asociacion.comerciantes.dto.DetalleComprobanteDto;
import proyecto.asociacion.comerciantes.model.ComprobanteEntity;
import proyecto.asociacion.comerciantes.model.Deuda;
import proyecto.asociacion.comerciantes.model.EstadoDeuda;
import proyecto.asociacion.comerciantes.mappers.ComprobanteMapper;
import proyecto.asociacion.comerciantes.repository.ComprobanteRepository;
import proyecto.asociacion.comerciantes.repository.IDeudaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.NoSuchElementException;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComprobanteService {
    //inyeccion de dependencias crea una clase desde otro lado y lo hace Spring
    private ComprobanteRepository comprobanteRepository;

    private ComprobanteMapper comprobanteMapper;

    private IDeudaRepository deudaRepository;

    //segunda forma de instanciar automaticamente con spring Inyector de dependencias
    //autowired es algo mas lento
    public ComprobanteService(ComprobanteRepository comprobanteRepository,
                              ComprobanteMapper comprobanteMapper2,
                              IDeudaRepository deudaRepository
                              ) {
        this.comprobanteRepository = comprobanteRepository;
        this.comprobanteMapper = comprobanteMapper2;
        this.deudaRepository = deudaRepository;
    }

    public List<ComprobanteDto> listar(){
        List<ComprobanteEntity> listaEntity = comprobanteRepository.findAll();//encontrar todo en bd
        List<ComprobanteDto> listaDto = comprobanteMapper.toDtoList(listaEntity);//convertir una listaEntity a una listaDto
        return listaDto;
    }

    @Transactional
    public ComprobanteDto registrar(ComprobanteDto comprobanteDto) {

        if (comprobanteDto.getNumero() == null || comprobanteDto.getNumero().isBlank()) {
            throw new IllegalArgumentException("El número de comprobante es obligatorio");
        }
        if (comprobanteDto.getIdPuesto() == null) {
            throw new IllegalArgumentException("El puesto es obligatorio");
        }

        if (comprobanteRepository.existsByNumero(
                comprobanteDto.getNumero().trim())) {

            throw new RuntimeException("El número de comprobante ya existe");
        }

        comprobanteDto.setNumero(comprobanteDto.getNumero().trim());
        comprobanteDto.setFecha(LocalDateTime.now());
        if (comprobanteDto.getListaDetalle() == null) {
            comprobanteDto.setListaDetalle(new ArrayList<>());
        }
        if (comprobanteDto.getVuelto() == null) {
            comprobanteDto.setVuelto(BigDecimal.ZERO);
        }

        // Cada detalle cancela una deuda completa (pago total, sin abonos)
        BigDecimal totalDeudas = BigDecimal.ZERO;
        List<Deuda> deudasPagadas = new ArrayList<>();

        for (DetalleComprobanteDto detalle :
                comprobanteDto.getListaDetalle()) {

            if (detalle.getIdDeuda() == null) {
                throw new IllegalArgumentException("Cada detalle debe indicar la deuda");
            }
            Deuda deuda = deudaRepository.findById(detalle.getIdDeuda())
                    .orElseThrow(() -> new NoSuchElementException("No existe la deuda " + detalle.getIdDeuda()));

            if (deuda.getEstado() == EstadoDeuda.PAGADA) {
                throw new IllegalStateException("La deuda " + deuda.getId() + " ya está pagada");
            }
            if (!deuda.getPuesto().getId().equals(comprobanteDto.getIdPuesto())) {
                throw new IllegalArgumentException("La deuda " + deuda.getId() + " no pertenece al puesto seleccionado");
            }

            detalle.setMontoPagado(deuda.getMonto());
            detalle.setFechaRegistro(LocalDateTime.now());
            totalDeudas = totalDeudas.add(deuda.getMonto());

            deuda.setEstado(EstadoDeuda.PAGADA);
            deuda.setFechaPago(LocalDate.now());
            deudasPagadas.add(deuda);
        }

        if (!deudasPagadas.isEmpty()) {
            comprobanteDto.setTotal(totalDeudas);
        }
        if (comprobanteDto.getTotal() == null) {
            throw new IllegalArgumentException("El total es obligatorio");
        }

        ComprobanteEntity entity =
                comprobanteMapper.toEntity(comprobanteDto);

        comprobanteRepository.save(entity);
        deudaRepository.saveAll(deudasPagadas);

        comprobanteDto.setId(entity.getId());

        return comprobanteDto;
    }

    public ComprobanteDto obtenerPorId(Long id) {

        ComprobanteEntity entity =
                comprobanteRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException("Comprobante no existe"));

        return comprobanteMapper.toDto(entity);
    }

    public ComprobanteDto actualizar(Long id, ComprobanteDto dto) {

        ComprobanteEntity entity =
                comprobanteRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException("Comprobante no existe"));

        entity.setNumero(dto.getNumero());
        entity.setTipo(dto.getTipo());
        entity.setIdUsuario(dto.getIdUsuario());
        entity.setVuelto(dto.getVuelto());

        // Si el comprobante ya cancela deudas, el puesto y el total no se pueden cambiar
        if (entity.getDetalles().isEmpty()) {
            entity.setIdPuesto(dto.getIdPuesto());
            entity.setTotal(dto.getTotal());
        }

        comprobanteRepository.save(entity);

        dto.setId(entity.getId());

        return dto;
    }
}
