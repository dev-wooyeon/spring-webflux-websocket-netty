package sample.webflux.websocket.netty.server;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ServerLogic {

    private final AtomicBoolean newClient;

    public ServerLogic() {
        newClient = new AtomicBoolean(true);
    }

    public Mono<Void> start(WebSocketSession session, long interval) {
        return session.receive().doOnNext(message -> {
            if (newClient.get()) {
                log.info("Server -> client connected id[{}]", session.getId());
            }
        })
        .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> log.info("Server -> received from client id=[{}]: [{}]", session.getId(), message))
                .filter(message -> newClient.get())
                .doOnNext(message -> newClient.set(false))
                .flatMap(message -> sendAtInterval(session, interval))
                .then();

    }

    private Flux<Void> sendAtInterval(WebSocketSession session, long interval) {
        return
            Flux
                .interval(Duration.ofMillis(interval))
                .map(value -> Long.toString(value))
                .flatMap(message ->
                    session
                        .send(Mono.fromCallable(() -> session.textMessage(message)))
                        .then(
                            Mono
                                .fromRunnable(() -> log.info("Server -> sent: [{}] to client id=[{}]", message, session.getId()))
                        )
                );
    }

}
