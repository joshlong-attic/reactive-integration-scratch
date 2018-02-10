package com.example.reactiveintegration;

import lombok.extern.java.Log;
import org.reactivestreams.Publisher;
import org.springframework.amqp.core.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log
@SpringBootApplication
public class ReactiveIntegrationApplication {

	@Configuration
	@RestController
	public static class AmqpGatewayConfiguration {

		private String tips = "tips";

		// https://docs.spring.io/spring-integration/reference/html/messaging-endpoints-chapter.html#_reactor_mono
		// REACTIVE GATEWAYS

		@PostMapping("/send")
		Publisher<Boolean> send(@RequestBody Mono<Map<String, String>> incoming) {
			return
					incoming
							.map(map -> map.getOrDefault("message", "world"))
							.map(msg -> MessageBuilder.withPayload(msg).build())
							.map(msg -> requests().send(msg));
		}

		@Bean
		MessageChannel requests() {
			return MessageChannels.direct().get();
		}

		@Bean
		MessageChannel responses() {
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
							.handle((GenericHandler<String>) (payload, headers) -> {
								log.info("received response: " + payload);
								headers.forEach((k, v) -> log.info("\t" + k + '=' + v));
								return null;
							})
							.get();
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(ReactiveIntegrationApplication.class, args);
	}
}


