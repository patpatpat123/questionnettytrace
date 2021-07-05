package com.question;

import brave.http.HttpTracing;
import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.brave.ReactorNettyHttpTracing;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.net.InetSocketAddress;

@Service
public class WebClientServiceImpl {

    @Value("${the.proxy.url}")
    private String proxyUrl;
    @Value("${the.destination.url}")
    private String destinationUrl;

    @Autowired
    private SomeConfiguration someConfiguration;
    @Autowired
    private HttpTracing       httpTracing;

    public Mono<String> sendPostRequest(String requestRefundToPartnerRequest) {
        final var httpClient      = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL)
                .secure(sslContextSpec -> sslContextSpec.sslContext(someConfiguration.getExternalProxyWebClient()))
                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).address(new InetSocketAddress(proxyUrl, 443)));

        final var decoratedClient = ReactorNettyHttpTracing.create(httpTracing).decorateHttpClient(httpClient);

        return WebClient.create(destinationUrl).mutate().clientConnector(new ReactorClientHttpConnector(decoratedClient)).build()
                .post().body(BodyInserters.fromValue(requestRefundToPartnerRequest))
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class));
    }

}
