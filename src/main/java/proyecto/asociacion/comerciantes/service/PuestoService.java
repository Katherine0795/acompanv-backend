package proyecto.asociacion.comerciantes.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto.asociacion.comerciantes.dto.PuestoRequestDto;
import proyecto.asociacion.comerciantes.dto.PuestoResponseDto;
import proyecto.asociacion.comerciantes.model.Puesto;
import proyecto.asociacion.comerciantes.repository.IPuestoRepository;
import proyecto.asociacion.comerciantes.mappers.PuestoMapper;
import proyecto.asociacion.comerciantes.model.Socio;
import proyecto.asociacion.comerciantes.repository.ISocioRepository;

import java.util.List;

@AllArgsConstructor
@Service
public class PuestoService implements IPuestoService {
    private final IPuestoRepository repository;
    private final ISocioRepository socioRepository;

    @Override
    public List<PuestoResponseDto> findAll() {
        List<Puesto> listaPuesto = repository.findAll();
        return listaPuesto.stream().map(PuestoMapper::toPuestoResponseDto).toList();
    }

    @Override
    public PuestoResponseDto findById(Long id) {
        return repository.findById(id)
                .map(PuestoMapper::toPuestoResponseDto)
                .orElse(null);
    }

    @Override
    public PuestoResponseDto save(PuestoRequestDto dto) {
        if (dto == null) return null;
        Puesto puesto = PuestoMapper.toPuesto(dto);
        if (dto.getIdSocio() != null) {
            Socio socio = socioRepository.findById(dto.getIdSocio()).orElse(null);
            if (socio == null) return null;
            puesto.setSocio(socio);
        }
        return PuestoMapper.toPuestoResponseDto(repository.save(puesto));
    }

    @Override
    public PuestoResponseDto update(PuestoRequestDto requestDto, Long id) {
        if (requestDto == null || id == null) {
            return null;
        }

        return repository.findById(id)
                .map(puestoExistente -> {
                    Puesto puestoActualizado = PuestoMapper.toPuesto(requestDto);
                    puestoActualizado.setId(puestoExistente.getId());
                    puestoActualizado.setEstado(puestoExistente.isEstado());

                    // Si se proporciona un nuevo idSocio, actualizar la relación
                    if (requestDto.getIdSocio() != null) {
                        Socio socio = socioRepository.findById(requestDto.getIdSocio()).orElse(null);
                        if (socio == null) {
                            return null; // El socio no existe
                        }
                        puestoActualizado.setSocio(socio);
                    } else {
                        puestoActualizado.setSocio(puestoExistente.getSocio());
                    }

                    return PuestoMapper.toPuestoResponseDto(repository.save(puestoActualizado));
                })
                .orElse(null);
    }

    @Override
    public PuestoResponseDto deleteById(Long id) {
        if (id == null) {
            return null;
        }
        return repository.findById(id)
                .map(puesto -> {
                    PuestoResponseDto response = PuestoMapper.toPuestoResponseDto(puesto);
                    repository.deleteById(id);
                    return response;
                })
                .orElse(null);
    }

    @Override
    public List<PuestoResponseDto> findBySocioId(Long socioId) {
        if (socioId == null) {
            return List.of();
        }
        List<Puesto> listaPuesto = repository.findBySocioId(socioId);
        return listaPuesto.stream().map(PuestoMapper::toPuestoResponseDto).toList();
    }
}
