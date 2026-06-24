package com.mohanad.edufinance.util;

import com.mohanad.edufinance.model.Student;
import com.mohanad.edufinance.model.Teacher;

/**
 * Utility class to handle PDF receipt generation for student payments
 * and teacher payroll processing. Uses the iText library.
 * (Skeleton implementation for Phase 5 & 6, finalized in Phase 7).
 *
 * @author MOHANAD
 */
public class PdfReceiptGenerator {

    /**
     * Generates a PDF payment receipt for a student installment.
     *
     * @param student       The student who made the payment
     * @param paymentAmount The amount paid
     */
    public static void generateStudentReceipt(Student student, double paymentAmount) {
        System.out.println("=== [PDF Receipt Generator] ===");
        System.out.printf("جاري إنشاء وصل دفع للطلب: %s%n", student.getName());
        System.out.printf("المبلغ المحصل: %.2f ر.ل%n", paymentAmount);
        System.out.printf("المتبقي عليه: %.2f ر.ل%n", (student.getTotalFees() - student.getPaidAmount()));
        System.out.println("=================================");
        
        // Custom warning indicating it's currently a placeholder
        System.out.println("ملاحظة: سيتم توليد ملف PDF الفعلي في المرحلة السابعة.");
    }

    /**
     * Generates a PDF payroll receipt for a teacher payment.
     *
     * @param teacher The teacher being paid
     * @param amount  The total computed payroll amount
     */
    public static void generateTeacherReceipt(Teacher teacher, double amount) {
        System.out.println("=== [PDF Payroll Generator] ===");
        System.out.printf("جاري إنشاء وصل صرف راتب للمعلم: %s%n", teacher.getName());
        System.out.printf("نوع الصرف: %s | قيمة الاستحقاق: %.2f ر.ل%n", 
                "FIXED".equalsIgnoreCase(teacher.getPayType()) ? "راتب ثابت" : "بالحصة", amount);
        System.out.println("=================================");
        
        // Custom warning indicating it's currently a placeholder
        System.out.println("ملاحظة: سيتم توليد ملف PDF الفعلي في المرحلة السابعة.");
    }
}
