package com.codingshuttle.youtube.LearningSpringBootApp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Component
//@Service
//@Repository
//@RestController
//@Controller
@ConditionalOnProperty(name = "payment.provider",havingValue = "upi")
public class UpiPayments implements PaymentService{

    @Override
    public String pay(){
        String Payments = "Upi Payments";
        System.out.println(Payments);
        return Payments;
    }
}

