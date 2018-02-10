package com.example.uppercasegateway;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.support.GenericHandler;

@SpringBootApplication
public class UppercaseGatewayApplication {

	private String tips = "tips";

	@Bean
	IntegrationFlow incomingFlow(ConnectionFactory cf) {
		return IntegrationFlows
				.from(Amqp.inboundGateway(cf, this.tips))
				.handle((GenericHandler<String>) (s, map) -> s.toUpperCase())
				.get();
	}

	public static void main(String[] args) {
		SpringApplication.run(UppercaseGatewayApplication.class, args);
	}
}
