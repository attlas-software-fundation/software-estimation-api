package com.intecx.inscope.service.support;

import com.intecx.inscope.entity.ReportSnapshot;
import com.intecx.inscope.enumeration.ProjectStatus;
import com.intecx.inscope.enumeration.ProjectType;
import com.intecx.inscope.enumeration.RiskLevel;
import com.intecx.inscope.enumeration.SprintUnit;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EstimationPdfGenerator {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color HEADER = new Color(45, 45, 45);
    private static final Color LINE = new Color(210, 210, 210);
    private static final Color LABEL = new Color(90, 90, 90);
    private static final DecimalFormat MONEY = moneyFormat();

    public byte[] generate(ReportSnapshot snapshot, List<PdfStorySection> stories) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BaseFont regular = font("Inter-Regular.ttf");
            BaseFont bold = font("Inter-Bold.ttf");
            Font title = new Font(bold, 16, Font.NORMAL, Color.BLACK);
            Font subtitle = new Font(regular, 9, Font.NORMAL, LABEL);
            Font label = new Font(bold, 8, Font.NORMAL, Color.BLACK);
            Font value = new Font(regular, 8, Font.NORMAL, Color.BLACK);
            Font head = new Font(bold, 8, Font.NORMAL, Color.WHITE);
            Font totalLabel = new Font(bold, 9, Font.NORMAL, Color.BLACK);

            Document document = new Document(new Rectangle(595, 842), 36, 36, 32, 46);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PageFooter(regular));
            document.open();

            PdfPTable header = new PdfPTable(new float[] { 1.4f, 1.6f });
            header.setWidthPercentage(100);
            header.addCell(logoCell());
            header.addCell(titleCell(snapshot.getProjectName(), title, subtitle));
            document.add(header);
            document.add(spacer(10));

            PdfPTable meta = new PdfPTable(1);
            meta.setWidthPercentage(100);
            meta.setHorizontalAlignment(Element.ALIGN_LEFT);
            metaLine(meta, "Proyecto", snapshot.getProjectName(), label, value);
            metaLine(meta, "Tipo", typeLabel(snapshot.getProjectType()), label, value);
            metaLine(meta, "Estado", statusLabel(snapshot.getProjectStatus()), label, value);
            metaLine(meta, "Responsable", snapshot.getProjectOwner(), label, value);
            metaLine(meta, "Fecha de emisión", snapshot.getCreatedAt().format(DATE_TIME), label, value);
            document.add(meta);
            document.add(spacer(12));

            PdfPTable summary = new PdfPTable(new float[] { 3.2f, 1.4f });
            summary.setWidthPercentage(100);
            summary.addCell(headCell("Concepto", head, Element.ALIGN_LEFT));
            summary.addCell(headCell("Valor", head, Element.ALIGN_RIGHT));
            summaryRow(summary, "Puntos de esfuerzo", String.valueOf(snapshot.getBaseEffortPoints()), value);
            summaryRow(summary, "Sprints", String.valueOf(snapshot.getTotalSprints()), value);
            summaryRow(summary, "Horas de esfuerzo", plain(snapshot.getTotalEffortHours()), value);
            summaryRow(summary, "Historias", snapshot.getStoriesWithPoints() + " de " + snapshot.getStoriesTotal(), value);
            summaryRow(summary, "Tareas", snapshot.getTasksWithHours() + " de " + snapshot.getTasksTotal(), value);
            summaryRow(summary, "Nivel de riesgo", riskLabel(snapshot.getRiskLevel()), value);
            summaryRow(summary, "Margen de contingencia", snapshot.getContingencyMarginPercentage().stripTrailingZeros().toPlainString() + " %", value);
            document.add(summary);
            document.add(spacer(12));

            PdfPTable totals = new PdfPTable(new float[] { 2.2f, 1.2f });
            totals.setWidthPercentage(42);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalRow(totals, "Tiempo base", time(snapshot.getBaseTime(), snapshot.getTimeUnit()), value);
            totalRow(totals, "Tiempo contingencia", time(snapshot.getContingencyTime(), snapshot.getTimeUnit()), value);
            totalRow(totals, "Tiempo total", time(snapshot.getTotalTime(), snapshot.getTimeUnit()), totalLabel);
            totalRow(totals, "Costo base", money(snapshot.getBaseCost()), value);
            totalRow(totals, "Costo contingencia", money(snapshot.getContingencyCost()), value);
            totalRow(totals, "Costo total", money(snapshot.getTotalCost()), totalLabel);
            document.add(totals);
            document.add(spacer(14));
            document.add(sectionTitle("Historias y tareas", bold));
            document.add(spacer(6));
            if (stories == null || stories.isEmpty()) {
                document.add(new Phrase("Sin historias registradas.", value));
            } else {
                for (PdfStorySection story : stories) {
                    document.add(storyTable(story, head, value, bold));
                    document.add(spacer(8));
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF de estimación", ex);
        }
    }

    public String fileName(String projectName) {
        String cleaned = projectName == null ? "" : projectName.trim().replaceAll("[\\\\/:*?\"<>|]", "");
        cleaned = cleaned.replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            return "reporte.pdf";
        }
        return cleaned + ".pdf";
    }

    private PdfPCell logoCell() throws Exception {
        byte[] bytes = read("/logo/intecx-logo.png");
        Image logo = Image.getInstance(bytes);
        logo.scaleToFit(132, 42);
        PdfPCell cell = new PdfPCell(logo, false);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell titleCell(String projectName, Font title, Font subtitle) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(LINE);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.addElement(centered("Reporte de estimación", title));
        cell.addElement(centered(projectName, subtitle));
        return cell;
    }

    private PdfPTable centered(String text, Font font) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(1);
        table.addCell(cell);
        return table;
    }

    private void metaLine(PdfPTable table, String name, String text, Font label, Font value) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(name + ": ", label));
        phrase.add(new Chunk(text == null ? "" : text, value));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(2);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void summaryRow(PdfPTable table, String name, String text, Font font) {
        table.addCell(plainCell(name, font, Element.ALIGN_LEFT, true));
        table.addCell(plainCell(text, font, Element.ALIGN_RIGHT, true));
    }

    private void totalRow(PdfPTable table, String name, String text, Font font) {
        table.addCell(plainCell(name, font, Element.ALIGN_LEFT, false));
        table.addCell(plainCell(text, font, Element.ALIGN_RIGHT, false));
    }

    private PdfPCell headCell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        cell.setBorderColor(Color.WHITE);
        cell.setBorderWidth(0.6f);
        return cell;
    }

    private PdfPCell plainCell(String text, Font font, int align, boolean grid) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        cell.setBorderColor(LINE);
        if (!grid) {
            cell.setBorder(Rectangle.NO_BORDER);
        }
        return cell;
    }

    private PdfPTable sectionTitle(String text, BaseFont bold) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(bold, 11, Font.NORMAL, Color.BLACK)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        table.addCell(cell);
        return table;
    }

    private PdfPTable storyTable(PdfStorySection story, Font head, Font value, BaseFont bold) {
        PdfPTable table = new PdfPTable(new float[] { 3.2f, 2.2f, 0.8f });
        table.setWidthPercentage(100);
        table.setHeaderRows(2);
        table.setSplitLate(false);
        String points = story.points() == null ? "Sin puntos" : story.points() + " SP";
        PdfPCell banner = new PdfPCell(new Phrase(story.code() + "  " + story.title() + "  " + points,
                new Font(bold, 8, Font.NORMAL, Color.WHITE)));
        banner.setColspan(3);
        banner.setBackgroundColor(HEADER);
        banner.setPadding(5);
        banner.setBorderColor(HEADER);
        table.addCell(banner);
        table.addCell(headCell("Tarea", head, Element.ALIGN_LEFT));
        table.addCell(headCell("Perfiles", head, Element.ALIGN_LEFT));
        table.addCell(headCell("Horas", head, Element.ALIGN_RIGHT));
        if (story.tasks().isEmpty()) {
            PdfPCell empty = plainCell("Sin tareas", value, Element.ALIGN_LEFT, true);
            empty.setColspan(3);
            table.addCell(empty);
            return table;
        }
        for (PdfTaskLine task : story.tasks()) {
            table.addCell(plainCell(task.title(), value, Element.ALIGN_LEFT, true));
            table.addCell(plainCell(task.profiles(), value, Element.ALIGN_LEFT, true));
            table.addCell(plainCell(String.valueOf(task.hours()), value, Element.ALIGN_RIGHT, true));
        }
        return table;
    }

    private PdfPTable spacer(float height) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        table.addCell(cell);
        return table;
    }

    private BaseFont font(String fileName) throws Exception {
        byte[] bytes = read("/fonts/" + fileName);
        return BaseFont.createFont(fileName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
    }

    private byte[] read(String path) throws Exception {
        try (InputStream input = EstimationPdfGenerator.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("No se encontró el recurso " + path);
            }
            return input.readAllBytes();
        }
    }

    private String money(BigDecimal amount) {
        return "S/ " + MONEY.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private String plain(BigDecimal amount) {
        return amount == null ? "0" : amount.stripTrailingZeros().toPlainString();
    }

    private String time(BigDecimal amount, SprintUnit unit) {
        String label = unit == SprintUnit.WEEKS ? "semanas" : "días";
        return plain(amount) + " " + label;
    }

    private String statusLabel(ProjectStatus status) {
        return switch (status) {
            case DRAFT -> "Borrador";
            case UNDER_REVIEW -> "En evaluación";
            case ESTIMATED -> "Estimado";
            case APPROVED -> "Aprobado";
            case REJECTED -> "Rechazado";
            case IN_PROGRESS -> "En ejecución";
            case COMPLETED -> "Finalizado";
        };
    }

    private String typeLabel(ProjectType type) {
        return switch (type) {
            case MONITORING -> "Monitoreo IoT";
            case AUTOMATION -> "Automatización IoT";
            case TELEMETRY -> "Telemetría IoT";
            case CONTROL_SUPERVISION -> "Control y supervisión IoT";
            case PREDICTIVE_MAINTENANCE -> "Mantenimiento predictivo IoT";
            case INTEGRATION -> "Integración IoT";
        };
    }

    private String riskLabel(RiskLevel level) {
        return switch (level) {
            case LOW -> "Bajo";
            case MEDIUM -> "Medio";
            case HIGH -> "Alto";
            case CRITICAL -> "Crítico";
        };
    }

    private static final class PageFooter extends PdfPageEventHelper {
        private final BaseFont font;
        private PdfTemplate total;

        private PageFooter(BaseFont font) {
            this.font = font;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(24, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            String text = "Página " + writer.getPageNumber() + " de ";
            float x = (document.left() + document.right()) / 2;
            float y = 22;
            canvas.beginText();
            canvas.setFontAndSize(font, 8);
            canvas.setColorFill(LABEL);
            canvas.showTextAligned(Element.ALIGN_RIGHT, text, x, y, 0);
            canvas.endText();
            canvas.addTemplate(total, x + 2, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(font, 8);
            total.setColorFill(LABEL);
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        }
    }

    private DecimalFormat moneyFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        format.setGroupingUsed(true);
        return format;
    }
}
