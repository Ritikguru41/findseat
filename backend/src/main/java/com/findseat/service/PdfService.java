package com.findseat.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Generates the PDF movie ticket (same purpose as the old pdfkit generator).
 * Layout: dark blue header, movie/show/user details on the left, QR on the right.
 */
@Service
public class PdfService {

    private static final Color DARK_BLUE = new Color(30, 58, 138);
    private static final Color LIGHT_BLUE = new Color(37, 99, 235);
    private static final Color GREY = new Color(110, 110, 110);
    private static final Color INK = new Color(30, 30, 30);

    public byte[] generateTicketPdf(Map<String, Object> data) throws Exception {
        String bookingId  = str(data.get("bookingId"));
        String userId     = String.valueOf(data.get("userId"));
        String showId     = String.valueOf(data.get("showId"));
        String movieTitle = str(data.get("movieTitle"));
        String genre      = str(data.get("genre"));
        String userName   = str(data.get("userName"));
        String seats      = str(data.get("seats"));
        String showDate   = str(data.get("showDate"));
        String showTime   = str(data.get("showTime"));
        String screen     = str(data.get("screen"));
        String cinemaName = str(data.get("cinemaName"));
        String location   = str(data.get("cinemaLocation"));
        String amount     = str(data.get("totalAmount"));
        String paymentId  = str(data.get("paymentId"));

        String qrData = String.format(
                "{\"bookingId\":\"%s\",\"userId\":\"%s\",\"showId\":\"%s\"}",
                bookingId, userId, showId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(650, 320), 12, 12, 12, 12);
        PdfWriter.getInstance(document, out);
        document.open();

        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(191, 219, 254));
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, DARK_BLUE);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 8, GREY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, INK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, INK);

        PdfPTable main = new PdfPTable(2);
        main.setWidthPercentage(100);
        main.setWidths(new float[]{62f, 38f});

        PdfPCell header = new PdfPCell();
        header.setColspan(2);
        header.setBackgroundColor(DARK_BLUE);
        header.setPadding(8);
        header.setBorder(Rectangle.NO_BORDER);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph brand = new Paragraph("FINDSEAT", brandFont);
        brand.setAlignment(Element.ALIGN_CENTER);
        header.addElement(brand);
        Paragraph tagline = new Paragraph("PREMIUM CINEMA", subtitleFont);
        tagline.setAlignment(Element.ALIGN_CENTER);
        header.addElement(tagline);
        main.addCell(header);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPaddingTop(8);

        Paragraph movie = new Paragraph(movieTitle, titleFont);
        movie.setSpacingAfter(4);
        left.addElement(movie);
        if (!genre.isBlank()) {
            left.addElement(new Paragraph("Genre: " + genre, labelFont));
        }
        left.addElement(new Paragraph("Screen: " + screen, labelFont));
        left.addElement(new Paragraph("\u00A0", smallFont));

        left.addElement(new Paragraph("PASSENGER", labelFont));
        left.addElement(new Paragraph(userName, valueFont));
        left.addElement(new Paragraph("\u00A0", smallFont));

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{50f, 50f});
        info.addCell(cell("DATE", labelFont));
        info.addCell(cell("TIME", labelFont));
        info.addCell(cell(showDate, valueFont));
        info.addCell(cell(showTime, valueFont));
        info.addCell(cell("SEATS", labelFont));
        info.addCell(cell("AMOUNT", labelFont));
        info.addCell(cell(seats, valueFont));
        info.addCell(cell("\u20B9" + amount, valueFont));
        left.addElement(info);

        left.addElement(new Paragraph("\u00A0", smallFont));
        left.addElement(new Paragraph(cinemaName + (location.isBlank() ? "" : ", " + location), smallFont));
        left.addElement(new Paragraph("Booking ID: " + bookingId, smallFont));
        if (!paymentId.isBlank()) {
            left.addElement(new Paragraph("Payment ID: " + paymentId, smallFont));
        }
        main.addCell(left);

        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix matrix = qrWriter.encode(qrData, BarcodeFormat.QR_CODE, 140, 140);
        ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", qrOut);
        Image qrImage = Image.getInstance(qrOut.toByteArray());

        PdfPCell right = new PdfPCell(qrImage, true);
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        right.setPaddingTop(8);
        main.addCell(right);

        document.add(main);
        document.close();
        return out.toByteArray();
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(2);
        return c;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
