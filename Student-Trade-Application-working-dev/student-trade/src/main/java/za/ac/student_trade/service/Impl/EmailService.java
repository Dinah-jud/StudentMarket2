package za.ac.student_trade.service.Impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import za.ac.student_trade.domain.Transaction;
import za.ac.student_trade.service.Impl.InvoiceService;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private InvoiceService invoiceService;

    /**
     * Send invoice email to seller with PDF attachment
     */
    public void sendSellerInvoice(Transaction transaction) throws Exception {
        byte[] invoicePdf = invoiceService.generateInvoicePDF(transaction);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@studenttrade.com");
        helper.setTo(transaction.getSeller().getEmail());
        helper.setSubject("🎉 Sale Confirmed - Invoice #" + transaction.getTransactionId());

        String htmlContent = buildSellerInvoiceEmail(transaction);
        helper.setText(htmlContent, true);

        // Attach PDF invoice
        helper.addAttachment(
                "Invoice_" + transaction.getTransactionId() + ".pdf",
                new ByteArrayResource(invoicePdf),
                "application/pdf"
        );

        // Attach product image inline
        if (transaction.getImageOfProduct() != null && transaction.getImageOfProduct().length > 0) {
            helper.addInline(
                    "productImage",
                    new ByteArrayResource(transaction.getImageOfProduct()),
                    transaction.getProduct().getImageType() != null ?
                            transaction.getProduct().getImageType() : "image/png"
            );
        }

        mailSender.send(message);
    }

    /**
     * Send invoice email to buyer with PDF attachment
     */
    public void sendBuyerInvoice(Transaction transaction) throws Exception {
        byte[] invoicePdf = invoiceService.generateInvoicePDF(transaction);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@studenttrade.com");
        helper.setTo(transaction.getBuyer().getEmail());
        helper.setSubject("✅ Purchase Confirmed - Invoice #" + transaction.getTransactionId());

        String htmlContent = buildBuyerInvoiceEmail(transaction);
        helper.setText(htmlContent, true);

        // Attach PDF invoice
        helper.addAttachment(
                "Invoice_" + transaction.getTransactionId() + ".pdf",
                new ByteArrayResource(invoicePdf),
                "application/pdf"
        );

        // Attach product image inline
        if (transaction.getImageOfProduct() != null && transaction.getImageOfProduct().length > 0) {
            helper.addInline(
                    "productImage",
                    new ByteArrayResource(transaction.getImageOfProduct()),
                    transaction.getProduct().getImageType() != null ?
                            transaction.getProduct().getImageType() : "image/png"
            );
        }

        mailSender.send(message);
    }

    private String buildSellerInvoiceEmail(Transaction transaction) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .invoice-box { background: white; padding: 20px; border-radius: 8px; 
                                   margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .product-img { max-width: 200px; border-radius: 8px; margin: 15px 0; }
                    .info-row { display: flex; justify-content: space-between; padding: 10px 0; 
                                border-bottom: 1px solid #e9ecef; }
                    .label { font-weight: bold; color: #495057; }
                    .value { color: #6c757d; }
                    .highlight { background: #d4edda; padding: 15px; border-radius: 5px; 
                                 border-left: 4px solid #28a745; margin: 15px 0; }
                    .footer { text-align: center; color: #6c757d; padding: 20px; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Congratulations!</h1>
                        <p>Your product has been sold</p>
                    </div>
                    <div class="content">
                        <div class="highlight">
                            <strong>Transaction Completed Successfully!</strong><br>
                            Invoice #%s
                        </div>
                        
                        <div class="invoice-box">
                            <h2 style="color: #667eea; margin-top: 0;">Sale Details</h2>
                            
                            <div class="info-row">
                                <span class="label">Product:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Sale Price:</span>
                                <span class="value" style="color: #28a745; font-size: 18px; font-weight: bold;">R %.2f</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Condition:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Transaction Date:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div style="text-align: center;">
                                <img src="cid:productImage" class="product-img" alt="Product Image" />
                            </div>
                        </div>
                        
                        <div class="invoice-box">
                            <h3 style="color: #667eea;">Buyer Information</h3>
                            <div class="info-row">
                                <span class="label">Name:</span>
                                <span class="value">%s %s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Student ID:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Email:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>
                        
                        <div style="background: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; margin: 15px 0;">
                            <strong>⚠️ Next Steps:</strong><br>
                            Please contact the buyer to arrange pickup/delivery details.
                        </div>
                        
                        <div style="text-align: center; margin: 20px 0;">
                            <p style="color: #6c757d; margin: 10px 0;">
                                📎 Your detailed invoice is attached as a PDF
                            </p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Thank you for using Student Trade Marketplace</p>
                        <p>Questions? Contact us at support@studenttrade.ac.za</p>
                        <p style="font-size: 10px; color: #adb5bd;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                transaction.getTransactionId(),
                transaction.getProductLabel(),
                transaction.getPrice(),
                transaction.getProductCondition(),
                transaction.getTransactionDate().format(formatter),
                transaction.getBuyer().getFirstName(),
                transaction.getBuyer().getLastName(),
                transaction.getBuyer().getStudentId(),
                transaction.getBuyer().getEmail()
        );
    }

    private String buildBuyerInvoiceEmail(Transaction transaction) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .invoice-box { background: white; padding: 20px; border-radius: 8px; 
                                   margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .product-img { max-width: 200px; border-radius: 8px; margin: 15px 0; }
                    .info-row { display: flex; justify-content: space-between; padding: 10px 0; 
                                border-bottom: 1px solid #e9ecef; }
                    .label { font-weight: bold; color: #495057; }
                    .value { color: #6c757d; }
                    .highlight { background: #d1ecf1; padding: 15px; border-radius: 5px; 
                                 border-left: 4px solid #17a2b8; margin: 15px 0; }
                    .footer { text-align: center; color: #6c757d; padding: 20px; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Purchase Confirmed!</h1>
                        <p>Thank you for your purchase</p>
                    </div>
                    <div class="content">
                        <div class="highlight">
                            <strong>Payment Successful!</strong><br>
                            Invoice #%s
                        </div>
                        
                        <div class="invoice-box">
                            <h2 style="color: #667eea; margin-top: 0;">Purchase Details</h2>
                            
                            <div class="info-row">
                                <span class="label">Product:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Amount Paid:</span>
                                <span class="value" style="color: #17a2b8; font-size: 18px; font-weight: bold;">R %.2f</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Condition:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Purchase Date:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div style="text-align: center;">
                                <img src="cid:productImage" class="product-img" alt="Product Image" />
                            </div>
                        </div>
                        
                        <div class="invoice-box">
                            <h3 style="color: #667eea;">Seller Information</h3>
                            <div class="info-row">
                                <span class="label">Name:</span>
                                <span class="value">%s %s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Student ID:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Email:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>
                        
                        <div style="background: #d4edda; padding: 15px; border-radius: 5px; border-left: 4px solid #28a745; margin: 15px 0;">
                            <strong>📦 Next Steps:</strong><br>
                            Please contact the seller to arrange pickup/delivery details.
                        </div>
                        
                        <div style="text-align: center; margin: 20px 0;">
                            <p style="color: #6c757d; margin: 10px 0;">
                                📎 Your detailed invoice is attached as a PDF
                            </p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Thank you for shopping at Student Trade Marketplace</p>
                        <p>Questions? Contact us at support@studenttrade.ac.za</p>
                        <p style="font-size: 10px; color: #adb5bd;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                transaction.getTransactionId(),
                transaction.getProductLabel(),
                transaction.getPrice(),
                transaction.getProductCondition(),
                transaction.getTransactionDate().format(formatter),
                transaction.getSeller().getFirstName(),
                transaction.getSeller().getLastName(),
                transaction.getSeller().getStudentId(),
                transaction.getSeller().getEmail()
        );
    }

    /**
     * Legacy methods - kept for backward compatibility
     * @deprecated Use sendSellerInvoice() instead
     */
    @Deprecated
    public void sendPurchaseNotification(
            String toEmail,
            String productName,
            double price,
            String buyerName,
            String buyerEmail,
            byte[] imageBytes,
            String imageType
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@studenttrade.com");
        helper.setTo(toEmail);
        helper.setSubject("🎉 Your product '" + productName + "' has been sold!");

        String htmlContent = String.format(
                "<h2>Congratulations! 🎉</h2>" +
                        "<p>Your product has been purchased.</p>" +
                        "<p><b>Product Name:</b> %s<br>" +
                        "<b>Sale Price:</b> R%.2f</p>" +
                        "<p><b>Buyer Information:</b><br>" +
                        "Name: %s<br>" +
                        "Email: %s</p>" +
                        "<p><img src='cid:productImage' style='max-width:300px; border-radius:8px;' /></p>" +
                        "<p>Please contact the buyer to arrange pickup/delivery.</p>" +
                        "<p>Thank you for using Student Trade Marketplace!</p>",
                productName, price, buyerName, buyerEmail
        );

        helper.setText(htmlContent, true);

        if (imageBytes != null && imageBytes.length > 0) {
            helper.addInline(
                    "productImage",
                    new ByteArrayResource(imageBytes),
                    imageType != null ? imageType : "image/png"
            );
        }

        mailSender.send(message);
    }

    /**
     * @deprecated Use sendBuyerInvoice() instead
     */
    @Deprecated
    public void sendPurchaseConfirmation(
            String toEmail,
            String productName,
            double price,
            String sellerName,
            String sellerEmail,
            byte[] imageBytes,
            String imageType
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@studenttrade.com");
        helper.setTo(toEmail);
        helper.setSubject("Purchase Confirmation: " + productName);

        String htmlContent = String.format(
                "<h2>Thank you for your purchase!</h2>" +
                        "<p><b>Product Name:</b> %s<br>" +
                        "<b>Price:</b> R%.2f</p>" +
                        "<p><b>Seller Information:</b><br>" +
                        "Name: %s<br>" +
                        "Email: %s</p>" +
                        "<p><img src='cid:productImage' style='max-width:300px; border-radius:8px;' /></p>" +
                        "<p>Please contact the seller to arrange pickup/delivery.</p>" +
                        "<p>Happy trading!</p>",
                productName, price, sellerName, sellerEmail
        );

        helper.setText(htmlContent, true);

        if (imageBytes != null && imageBytes.length > 0) {
            helper.addInline(
                    "productImage",
                    new ByteArrayResource(imageBytes),
                    imageType != null ? imageType : "image/png"
            );
        }

        mailSender.send(message);
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("StudentTrade Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP will expire in 5 minutes.");
        mailSender.send(message);
    }
}