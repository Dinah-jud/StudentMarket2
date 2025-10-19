package za.ac.student_trade.service.Impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import za.ac.student_trade.domain.Product;
import za.ac.student_trade.domain.StripeResponse;

@Service
public class StripeServiceImpl {

    @Value("${stripe.secreteKey}")
    private String secretKey;

    public StripeResponse checkoutProduct(Product product) {
        Stripe.apiKey = secretKey;

        try {
            // Use product price and currency
            long amountInCents = (product.getPrice() != null ? product.getPrice() : 0) * 100; // Stripe expects cents
            String currency = (product.getCurrency() != null ? product.getCurrency() : "zar").toLowerCase();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            .setUnitAmount(amountInCents)
                            .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(product.getProductName())
                                            .build()
                            )
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            // Build the Stripe checkout session
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:3001/success")
                            .setCancelUrl("http://localhost:3001/cancel")
                            .addLineItem(lineItem)
                            .build();

            Session session = Session.create(params);

            System.out.println("=== Stripe Checkout Debug ===");
            System.out.println("Product Name: " + product.getProductName());
            System.out.println("Price: " + product.getPrice());
            System.out.println("Currency: " + product.getCurrency());
            System.out.println("=============================");

            return StripeResponse.builder()
                    .status("Success")
                    .message("Payment Session Created")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {
            System.err.println("Stripe API Error: " + e.getMessage());
            e.printStackTrace();

            return StripeResponse.builder()
                    .status("Error")
                    .message("Failed to create payment session: " + e.getMessage())
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
        }
    }
}
