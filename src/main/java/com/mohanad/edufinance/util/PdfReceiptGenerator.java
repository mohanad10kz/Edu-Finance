package com.mohanad.edufinance.util;

import com.mohanad.edufinance.model.Student;
import com.mohanad.edufinance.model.Teacher;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to generate professional Arabic PDF receipts for Student Payments
 * and Teacher Payroll. Uses iText 5 to support RTL text layout and character shaping
 * by embedding the Tahoma Unicode font from resources.
 *
 * @author MOHANAD
 */
public class PdfReceiptGenerator {

    private static BaseFont baseFont;
    private static Font fontTitle;
    private static Font fontHeader;
    private static Font fontBody;
    private static Font fontFooter;

    // Static initializer to load and prepare the Arabic font from resources
    static {
        try (InputStream is = PdfReceiptGenerator.class.getResourceAsStream("/fonts/tahoma.ttf")) {
            if (is == null) {
                throw new IOException("لم يتم العثور على ملف خط Tahoma في الموارد.");
            }
            byte[] fontBytes = is.readAllBytes();
            
            // Register font with Identity-H encoding for Arabic character mapping
            baseFont = BaseFont.createFont("tahoma.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, BaseFont.CACHED, fontBytes, null);
            
            // Initialize styles
            fontTitle = new Font(baseFont, 20, Font.BOLD);
            fontHeader = new Font(baseFont, 14, Font.BOLD);
            fontBody = new Font(baseFont, 12, Font.NORMAL);
            fontFooter = new Font(baseFont, 10, Font.ITALIC);
        } catch (Exception e) {
            System.err.println("خطأ أثناء تحميل خط Tahoma للـ PDF. سيتم استخدام الخط الافتراضي.");
            e.printStackTrace();
            try {
                // Fallback to standard Helvetica if loading fails
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                fontTitle = new Font(baseFont, 20, Font.BOLD);
                fontHeader = new Font(baseFont, 14, Font.BOLD);
                fontBody = new Font(baseFont, 12, Font.NORMAL);
                fontFooter = new Font(baseFont, 10, Font.ITALIC);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Generates a PDF receipt for a student tuition payment in RTL Arabic.
     * Saved as "وصل_دفع_[اسم_الطالب].pdf" in the project root directory.
     *
     * @param student       The student making the payment
     * @param paymentAmount The amount paid in this transaction
     */
    public static void generateStudentReceipt(Student student, double paymentAmount) {
        String filename = "وصل_دفع_" + student.getName().replace(" ", "_") + ".pdf";
        Document document = new Document();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Create a table to wrap the entire layout in RTL
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            // 1. Header (Logo / System Name)
            PdfPCell cellHeader = new PdfPCell();
            cellHeader.setBorder(PdfPCell.NO_BORDER);
            cellHeader.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeader.setPaddingBottom(15);
            
            Paragraph title = new Paragraph("نظام EduFinance للمديرية المالية", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(title);
            
            Paragraph subtitle = new Paragraph("وصل استلام رسوم دراسية رسمية", fontHeader);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(subtitle);
            
            table.addCell(cellHeader);

            // 2. Info Block (Date, Bill ID, etc.)
            PdfPCell cellInfo = new PdfPCell();
            cellInfo.setBorder(PdfPCell.BOTTOM);
            cellInfo.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellInfo.setPaddingBottom(10);
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateText = "تاريخ العملية: " + dtf.format(LocalDateTime.now());
            Paragraph datePara = new Paragraph(dateText, fontBody);
            datePara.setAlignment(Element.ALIGN_LEFT);
            cellInfo.addElement(datePara);
            
            table.addCell(cellInfo);

            // 3. Receipt Body Details (Student name, Grade, Fees)
            PdfPCell cellDetails = new PdfPCell();
            cellDetails.setBorder(PdfPCell.NO_BORDER);
            cellDetails.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellDetails.setPaddingTop(20);
            cellDetails.setPaddingBottom(20);

            // Table inside the details to align labels and values
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(90);
            detailsTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            detailsTable.setWidths(new float[]{1f, 1f});

            addDetailRow(detailsTable, "اسم الطالب رباعي:", student.getName());
            addDetailRow(detailsTable, "المرحلة الدراسية (الصف):", student.getGradeName());
            addDetailRow(detailsTable, "إجمالي رسوم الصف الدراسي:", String.format("%.2f ر.ل", student.getTotalFees()));
            addDetailRow(detailsTable, "المبلغ المدفوع حالياً:", String.format("%.2f ر.ل", paymentAmount));
            addDetailRow(detailsTable, "إجمالي المبالغ المدفوعة سابقاً:", String.format("%.2f ر.ل", student.getPaidAmount() - paymentAmount));
            addDetailRow(detailsTable, "المبلغ المتبقي للتحصيل:", String.format("%.2f ر.ل", student.getRemainingAmount()));

            cellDetails.addElement(detailsTable);
            table.addCell(cellDetails);

            // 4. Footer & Signature
            PdfPCell cellFooter = new PdfPCell();
            cellFooter.setBorder(PdfPCell.TOP);
            cellFooter.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellFooter.setPaddingTop(15);
            
            Paragraph footerText = new Paragraph("ملاحظة: هذا الوصل مستند مالي رسمي لرسوم الدراسة. يرجى الاحتفاظ به.", fontFooter);
            footerText.setAlignment(Element.ALIGN_CENTER);
            cellFooter.addElement(footerText);

            Paragraph signText = new Paragraph("\nتوقيع المستلم المالي: ____________________\n", fontBody);
            signText.setAlignment(Element.ALIGN_RIGHT);
            cellFooter.addElement(signText);

            table.addCell(cellFooter);

            // Add the main RTL table to the document
            document.add(table);
            
            System.out.println("نجاح: تم توليد فاتورة الطالب بنجاح: " + filename);
        } catch (DocumentException | IOException e) {
            System.err.println("خطأ أثناء إنشاء ملف PDF الاستلام للطلب.");
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    /**
     * Generates a PDF payroll advice for a teacher payout.
     * Saved as "مسير_راتب_[اسم_المعلم].pdf" in the project root directory.
     *
     * @param teacher The teacher being paid
     * @param amount  The total computed payment amount
     */
    public static void generateTeacherReceipt(Teacher teacher, double amount) {
        String filename = "مسير_راتب_" + teacher.getName().replace(" ", "_") + ".pdf";
        Document document = new Document();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Create a table to wrap the entire layout in RTL
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            // 1. Header
            PdfPCell cellHeader = new PdfPCell();
            cellHeader.setBorder(PdfPCell.NO_BORDER);
            cellHeader.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeader.setPaddingBottom(15);
            
            Paragraph title = new Paragraph("نظام EduFinance للمديرية المالية", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(title);
            
            Paragraph subtitle = new Paragraph("إشعار صرف مستحقات مالية لأعضاء هيئة التدريس", fontHeader);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(subtitle);
            
            table.addCell(cellHeader);

            // 2. Info Block
            PdfPCell cellInfo = new PdfPCell();
            cellInfo.setBorder(PdfPCell.BOTTOM);
            cellInfo.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellInfo.setPaddingBottom(10);
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateText = "تاريخ الصرف: " + dtf.format(LocalDateTime.now());
            Paragraph datePara = new Paragraph(dateText, fontBody);
            datePara.setAlignment(Element.ALIGN_LEFT);
            cellInfo.addElement(datePara);
            
            table.addCell(cellInfo);

            // 3. Details Block
            PdfPCell cellDetails = new PdfPCell();
            cellDetails.setBorder(PdfPCell.NO_BORDER);
            cellDetails.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellDetails.setPaddingTop(20);
            cellDetails.setPaddingBottom(20);

            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(90);
            detailsTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            detailsTable.setWidths(new float[]{1f, 1f});

            addDetailRow(detailsTable, "اسم عضو هيئة التدريس:", teacher.getName());
            
            String payTypeDisplay = "FIXED".equalsIgnoreCase(teacher.getPayType()) ? "راتب شهري ثابت" : "بالحصة (أجر مقابل حصص)";
            addDetailRow(detailsTable, "طريقة التعاقد:", payTypeDisplay);
            
            String valLabel = "FIXED".equalsIgnoreCase(teacher.getPayType()) ? "الراتب الأساسي الثابت:" : "سعر الحصة الواحدة:";
            addDetailRow(detailsTable, valLabel, String.format("%.2f ر.ل", teacher.getPayValue()));
            
            addDetailRow(detailsTable, "الصف الدراسي المرتبط:", teacher.getGradeName());
            addDetailRow(detailsTable, "إجمالي المبلغ المنصرف:", String.format("%.2f ر.ل", amount));

            cellDetails.addElement(detailsTable);
            table.addCell(cellDetails);

            // 4. Footer & Signature
            PdfPCell cellFooter = new PdfPCell();
            cellFooter.setBorder(PdfPCell.TOP);
            cellFooter.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cellFooter.setPaddingTop(15);
            
            Paragraph footerText = new Paragraph("ملاحظة: هذا الإشعار مستند مالي يثبت استلام المستحقات المالية الصادرة.", fontFooter);
            footerText.setAlignment(Element.ALIGN_CENTER);
            cellFooter.addElement(footerText);

            Paragraph signText = new Paragraph("\nتوقيع المعلم المستلم: ____________________\n", fontBody);
            signText.setAlignment(Element.ALIGN_RIGHT);
            cellFooter.addElement(signText);

            table.addCell(cellFooter);

            // Add the main RTL table to the document
            document.add(table);
            
            System.out.println("نجاح: تم توليد مسير راتب المعلم بنجاح: " + filename);
        } catch (DocumentException | IOException e) {
            System.err.println("خطأ أثناء إنشاء ملف PDF مسير الراتب للمعلم.");
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    /**
     * Helper method to insert a key-value row into the details table with RTL support.
     *
     * @param table The PDF table
     * @param label The attribute label
     * @param value The value text
     */
    private static void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontHeader));
        cellLabel.setBorder(PdfPCell.NO_BORDER);
        cellLabel.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLabel.setPadding(6);
        
        PdfPCell cellValue = new PdfPCell(new Phrase(value, fontBody));
        cellValue.setBorder(PdfPCell.NO_BORDER);
        cellValue.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cellValue.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellValue.setPadding(6);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
}
