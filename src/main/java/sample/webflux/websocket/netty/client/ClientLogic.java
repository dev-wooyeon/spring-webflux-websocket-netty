package sample.webflux.websocket.netty.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientLogic {

    private final static AtomicInteger MESSAGE_ID;

    static {
        MESSAGE_ID = new AtomicInteger(0);
    }

    public void start(Client client) {
        Mono
            .fromRunnable(
                () -> client.send("Test message " + MESSAGE_ID.getAndIncrement())
            )
            .thenMany(client.receive())
            .doOnNext(
                message ->
                    log.info("Client id=[{}] -> received: [{}]", client.session().map(WebSocketSession::getId).orElse(""), message)
            )
            .subscribe();
    }
}