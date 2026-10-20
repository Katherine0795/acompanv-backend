package proyecto.asociacion.comerciantes.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import proyecto.asociacion.comerciantes.dto.DetallePagoDto;
import proyecto.asociacion.comerciantes.model.DetallePagoEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetallePagoMapper {
    @Mapping(target = "pago", ignore = true)
    @Mapping(target = "idComprobante", source = "comprobante.id")
    DetallePagoDto toDto(DetallePagoEntity entity);

    @Mapping(target = "pago", ignore = true)
    DetallePagoEntity toEntity(DetallePagoDto dto);

    List<DetallePagoDto> toDtoList(List<DetallePagoEntity> entities);
}

