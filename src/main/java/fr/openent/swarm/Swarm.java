package fr.openent.swarm;

import fr.openent.swarm.controller.SwarmController;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.http.BaseServer;

public class Swarm extends BaseServer {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final Promise<Void> promise = Promise.promise();
        super.start(promise);
        promise.future()
        .onSuccess(e -> {
            final EventBus eb = getEventBus(vertx);
            addController(new SwarmController());
            startPromise.tryComplete();
        }).onFailure(th -> {
            log.error("[Swarm-connector@Swarm::start] Fail to start Swarm-connector", th);
            startPromise.tryFail("[Swarm-connector@Swarm::start] Fail to start Swarm-connector");
        });
    }
}