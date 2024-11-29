package fr.openent.swarm.controller;

import fr.openent.swarm.services.UserService;
import fr.openent.swarm.services.impl.DefaultUserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;

import static fr.openent.swarm.core.constants.Field.*;

public class SwarmController extends ControllerHelper {

    private final UserService userService;

    public SwarmController() {
        super();
        this.userService = new DefaultUserService();
    }

    @Get("")
    @ApiDoc("Swarm test route")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void render(HttpServerRequest request) {
        JsonObject response = new JsonObject().put("message", "Ok");
        renderJson(request, response);
    }

    @Get("/users")
    @ApiDoc("Get users by MEF")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void getUsersByMEF(HttpServerRequest request) {
        String userId = request.getParam(USER_ID);
        String mefIdsJson = request.getParam(MEF_IDS);
        if (userId == null || userId.isEmpty() || mefIdsJson == null || mefIdsJson.isEmpty()) {
            log.error("Swarm-Connector@SwarmController::getUsersByMEF] no userId / mefIds provided.");
            badRequest(request);
        } else {
            JsonArray mefIds = new JsonArray(mefIdsJson);
            userService.getUsersByMEF(userId, mefIds)
                    .onSuccess(users -> {
                        JsonObject response = new JsonObject().put(USERS, users);
                        renderJson(request, response);
                    })
                    .onFailure(cause -> {
                        log.error("Swarm-Connector@SwarmController::getUsersByMEF] Error fetching users by MEF: " + cause.getMessage());
                        badRequest(request);
                    });
        }
    }
}
