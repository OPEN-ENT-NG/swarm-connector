package fr.openent.swarm;

import fr.openent.swarm.controller.SwarmController;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.http.BaseServer;

public class Swarm extends BaseServer {

    public static final String VIEW_RIGHT = "swarm.view";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        EventBus eb = getEventBus(vertx);

        addController(new SwarmController());
        startPromise.tryComplete();
        startPromise.tryFail("[Swarm-connector@Swarm::start] Fail to start Swarm-connector");
    }
}