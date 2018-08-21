package com.nayidisha.stripe;

import com.nayidisha.stripe.service.StripeService;
import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StripeApplication {

	@Value("${stripe.apiKey}")
	private String apiKey;
	public static void main(String[] args) {
		SpringApplication.run(StripeApplication.class, args);
	}

	@Bean
	public StripeService stripeService() {
		Stripe.apiKey = apiKey;
		return new StripeService();
	}
}
