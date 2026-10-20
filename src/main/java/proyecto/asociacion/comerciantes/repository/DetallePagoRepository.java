package proyecto.asociacion.comerciantes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.asociacion.comerciantes.model.DetallePagoEntity;

public interface DetallePagoRepository extends JpaRepository<DetallePagoEntity, Long> {

    boolean existsByComprobante_Id(Long idComprobante);

    List<DetallePagoEntity> findByComprobante_IdIn(List<Long> idsComprobante);
}
