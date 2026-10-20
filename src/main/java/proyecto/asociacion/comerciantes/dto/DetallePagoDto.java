package proyecto.asociacion.comerciantes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetallePagoDto {
    private Long id;
    private Long idComprobante;
    private ComprobanteDto comprobante;
    private PagoDto pago;

}
