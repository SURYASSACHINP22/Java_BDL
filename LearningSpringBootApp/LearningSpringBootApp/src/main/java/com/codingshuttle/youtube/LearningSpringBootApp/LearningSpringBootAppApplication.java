package com.codingshuttle.youtube.LearningSpringBootApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // this is the staring point in that enablecongiguration file which enables spring boot
public class LearningSpringBootAppApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(LearningSpringBootAppApplication.class);

    public static void main(String[] args) {

		SpringApplication.run(LearningSpringBootAppApplication.class, args);

		// here this is where we can configure

	}


//
//	@Autowired
//	private final UpiPayments Upipayments = new UpiPayments();

	//if we use the autowired then there is no need to using the constructor dependency enjections
//	public LearningSpringBootAppApplication(UpiPayments Upipayments) {
//		this.Upipayments = Upipayments;
//	} no need of this becouse @autowired   but we avoid this


//
//	private RozarpayPaymentService paymentService = new RozarpayPaymentService();
//	public LearningSpringBootAppApplication(RozarpayPaymentService paymentService) {
//		this.paymentService = paymentService;
//	}

	private final PaymentService paymentService;
	public LearningSpringBootAppApplication(PaymentService paymentService) {
		this.paymentService = paymentService;
	}


	@Override
	public void run (String... args) throws Exception {
		String payment = paymentService.pay();   // this will crete an abiguty so we need to make at lest one bean as primery
		System.out.println("payment done : " + payment);


	}

}
