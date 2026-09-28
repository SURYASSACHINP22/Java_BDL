package com.codingshuttle.youtube.LearningSpringBootApp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.provider",havingValue = "razorpay")
public class RozarpayPaymentService implements PaymentService {
    @Override
    public String pay(){
        String payment = "Rozorpay Payment";
        System.out.println("Payment from: " + payment);
        return payment;
    }
}
