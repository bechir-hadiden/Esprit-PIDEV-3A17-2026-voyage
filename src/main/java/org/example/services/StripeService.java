package org.example.services;

import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.example.utils.Config;

public class StripeService {

    public static boolean isConfigured() {
        String apiKey = getStripeSecretKey();
        if (apiKey == null) {
            return false;
        }
        String trimmed = apiKey.trim();
        return !trimmed.isEmpty() && !"YOUR_API_KEY_HERE".equals(trimmed);
    }

    private static String getStripeSecretKey() {
        return Config.get("stripe.api.key");
    }

    private static RequestOptions buildRequestOptions() {
        String apiKey = getStripeSecretKey();
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_API_KEY_HERE".equals(apiKey.trim())) {
            throw new IllegalStateException("Stripe API key is missing or invalid.");
        }

        try {
            return RequestOptions.builder()
                    .setApiKey(apiKey.trim())
                    .build();
        } catch (Throwable t) {
            throw new IllegalStateException(
                    "Stripe SDK initialization failed. Verify module config and Stripe dependency.",
                    t
            );
        }
    }

    public static class StripeCheckoutResult {
        public final String url;
        public final String sessionId;

        public StripeCheckoutResult(String url, String sessionId) {
            this.url = url;
            this.sessionId = sessionId;
        }
    }

    public static StripeCheckoutResult createCheckoutSession(double amountInDT) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Stripe API key is missing or invalid.");
        }
        if (amountInDT <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        // Stripe expects amounts in cents (long)
        long amountInCents = Math.round(amountInDT * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://checkout.stripe.com/success")
                .setCancelUrl("https://checkout.stripe.com/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Paiement Facture")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            Session session = Session.create(params, buildRequestOptions());
            return new StripeCheckoutResult(session.getUrl(), session.getId());
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to create Stripe checkout session.", t);
        }
    }

    public static boolean isSessionPaid(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId, buildRequestOptions());
            return "complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus());
        } catch (Throwable t) {
            System.err.println("Error retrieving Stripe session: " + t.getMessage());
            return false;
        }
    }
}
