package fr.openent.swarm.controller;

import fr.openent.swarm.Swarm;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.webutils.security.SecuredAction;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.http.RouteMatcher;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;
import java.util.Map;

public class SwarmController extends ControllerHelper {

    public SwarmController() {
        super();
    }

    @Override
    public void init(Vertx vertx, JsonObject config, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
        super.init(vertx, config, rm, securedActions);
    }

    @Get("")
    @ApiDoc("Swarm test route")
    @fr.wseduc.security.SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void render(HttpServerRequest request) {
        JsonObject response = new JsonObject().put("message", "Ok");
        renderJson(request, response);
    }
}
