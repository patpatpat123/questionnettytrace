package com.question;

import io.netty.handler.ssl.SslContext;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.NettySslUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;
import reactor.netty.channel.MicrometerChannelMetricsRecorder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class SomeConfiguration {

    @Value("${the.keystore.file}")
    private String keyStorePath;
    @Value("${the.keystore.password}")
    private String keyStorePassword;
    @Value("${the.key.password}")
    private String keyPassword;
    @Value("${the.truststore.path}")
    private String trustStorePath;
    @Value("${the.truststore.password}")
    private String truststorePassword;

    private SslContext sslContext;

    @PostConstruct
    public void initExternalWebClient() {
        try (var newInputStreamKeyStorePathToInternal = Files.newInputStream(Paths.get(URI.create(keyStorePath)));
             var newInputStreamTrustStorePath = Files.newInputStream(Paths.get(URI.create(trustStorePath)))) {
            var sslFactory = SSLFactory.builder()
                    .withIdentityMaterial(newInputStreamKeyStorePathToInternal, keyStorePassword.toCharArray(), keyPassword.toCharArray())
                    .withTrustMaterial(newInputStreamTrustStorePath, truststorePassword.toCharArray()).build();
            this.sslContext = NettySslUtils.forClient(sslFactory).build();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public NettyServerCustomizer nettyServerCustomizer() {
        Schedulers.enableMetrics();
        return httpServer -> httpServer.metrics(true, () -> new MicrometerChannelMetricsRecorder("test", "test"));
    }


    /**
     * Gets external proxy web client.
     *
     * @return the external proxy web client
     */
    public SslContext getExternalProxyWebClient() {
        return sslContext;
    }

}
