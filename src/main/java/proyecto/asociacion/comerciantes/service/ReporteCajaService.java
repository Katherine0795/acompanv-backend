package proyecto.asociacion.comerciantes.service;

import org.springframework.stereotype.Service;
import proyecto.asociacion.comerciantes.dto.ComprobanteDto;
import proyecto.asociacion.comerciantes.dto.ReporteCajaDto;
import proyecto.asociacion.comerciantes.mappers.ComprobanteMapper;
import proyecto.asociacion.comerciantes.model.ComprobanteEntity;
import proyecto.asociacion.comerciantes.model.DetallePagoEntity;
import proyecto.asociacion.comerciantes.repository.ComprobanteRepository;
import proyecto.asociacion.comerciantes.repository.DetallePagoRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import com.lowagie.text.Image;
import java.io.InputStream;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ReporteCajaService {

    private final ComprobanteRepository comprobanteRepository;
    private final ComprobanteMapper comprobanteMapper;
    private final DetallePagoRepository detallePagoRepository;

    public ReporteCajaService(ComprobanteRepository comprobanteRepository,
                              ComprobanteMapper comprobanteMapper,
                              DetallePagoRepository detallePagoRepository) {
        this.comprobanteRepository = comprobanteRepository;
        this.comprobanteMapper = comprobanteMapper;
        this.detallePagoRepository = detallePagoRepository;
    }

    public ReporteCajaDto generarReporte(LocalDate inicio, LocalDate finFecha) {

        LocalDateTime inicioDT = inicio.atStartOfDay();
        LocalDateTime finDT = finFecha.plusDays(1).atStartOfDay();

        List<ComprobanteEntity> comprobantes =
                comprobanteRepository.findByRangoFecha(inicioDT, finDT);

        List<ComprobanteDto> comprobantesDto =
                comprobanteMapper.toDtoList(comprobantes);

        BigDecimal totalGeneral = comprobantes.stream()
                .map(ComprobanteEntity::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Desglose por método de pago: el total de cada comprobante se suma
        // al método del pago que lo cancela (EFECTIVO, YAPE, PLIN, TRANSFERENCIA).
        Map<Long, String> metodoPorComprobante = new HashMap<>();
        if (!comprobantes.isEmpty()) {
            List<Long> ids = comprobantes.stream().map(ComprobanteEntity::getId).toList();
            for (DetallePagoEntity dp : detallePagoRepository.findByComprobante_IdIn(ids)) {
                metodoPorComprobante.put(dp.getComprobante().getId(), dp.getPago().getMetodo().name());
            }
        }

        Map<String, BigDecimal> porMetodo = new TreeMap<>();
        for (ComprobanteEntity c : comprobantes) {
            String metodo = metodoPorComprobante.getOrDefault(c.getId(), "SIN PAGO REGISTRADO");
            porMetodo.merge(metodo, c.getTotal(), BigDecimal::add);
        }

        ReporteCajaDto dto = new ReporteCajaDto();
        dto.setTotalGeneral(totalGeneral);
        dto.setTotalPorMetodoPago(porMetodo);
        dto.setCantidadComprobantes(comprobantes.size());
        dto.setComprobantes(comprobantesDto);

        return dto;
    }

    public ReporteCajaDto generarReporteMensual(int anio, int mes) {

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.plusMonths(1).minusDays(1);

        return generarReporte(inicio, fin);
    }

    public byte[] generarPdf(ReporteCajaDto reporte, LocalDate inicio, LocalDate fin) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            // LOGO
            InputStream logoStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("static/img/logo.png");

            if (logoStream != null) {
                Image logo = Image.getInstance(logoStream.readAllBytes());
                logo.scaleToFit(120, 120); // tamaño del logo
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            }

            document.add(new Paragraph("REPORTE DE CAJA"));
            document.add(new Paragraph("Desde: " + inicio));
            document.add(new Paragraph("Hasta: " + fin));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("TOTAL GENERAL: " + reporte.getTotalGeneral()));
            document.add(new Paragraph("CANTIDAD COMPROBANTES: " + reporte.getCantidadComprobantes()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("TOTAL POR MÉTODO DE PAGO:"));

            for (Map.Entry<String, BigDecimal> entry : reporte.getTotalPorMetodoPago().entrySet()) {
                document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
            }

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

}
