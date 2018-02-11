package com.example.reactiveintegration;

import lombok.extern.java.Log;
import org.springframework.amqp.core.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Mono;

@Log
@SpringBootApplication
public class ReactiveIntegrationApplication {

	public static final String REQUESTS = "requests";

	@Configuration
	@IntegrationComponentScan
	public static class AmqpGatewayConfiguration {

		private final String tips = "tips";

		// REACTIVE GATEWAYS
		// https://docs.spring.io/spring-integration/reference/html/messaging-endpoints-chapter.html#_reactor_mono

		@Bean(REQUESTS)
		MessageChannel requests() {
			return MessageChannels.direct().get();
		}

		@Bean
		Binding binding() {
			return BindingBuilder.bind(queue()).to(exchange()).with(this.tips).noargs();
		}

		@Bean
		Queue queue() {
			return QueueBuilder.durable(this.tips).build();
		}

		@Bean
		Exchange exchange() {
			return ExchangeBuilder.directExchange(this.tips).durable(true).build();
		}

		@Bean
		IntegrationFlow gateway(AmqpTemplate template) {
			return
					IntegrationFlows
							.from(requests())
							.handle(Amqp.outboundGateway(template).routingKey(this.tips))
							.get();
		}

		@Bean
		ApplicationRunner run(UppercaseGateway gateway) {
			return args ->
					gateway
							.uppercase("world")
							.subscribe(x -> log.info("received " + x));
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveIntegrationApplication.class, args);
	}
}

@MessagingGateway
interface UppercaseGateway {

	@Gateway(requestChannel = ReactiveIntegrationApplication.REQUESTS)
	Mono<String> uppercase(String text);
}