package fr.openent.swarm.controller;

import fr.openent.swarm.helper.LogHelper;
import fr.openent.swarm.model.UserInfos;
import fr.openent.swarm.repository.UserService;
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
import org.entcore.common.user.UserUtils;

import java.util.List;

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
        JsonObject response = new JsonObject().put(MESSAGE, OK);
        renderJson(request, response);
    }

    @Get("/users")
    @ApiDoc("Get users by MEF")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void getUsersByMEF(HttpServerRequest request) {
        String userId = request.getParam(USER_ID);
        List<String> mefIds = request.params().getAll(MEF_IDS);
        boolean addMyself = Boolean.parseBoolean(request.getParam(ADD_MYSELF));

        if (userId == null || userId.isEmpty() || mefIds == null || mefIds.isEmpty()) {
            String errorMessage = "No userId / mefIds provided";
            LogHelper.logError(this, "getUsersByMEF", errorMessage);
            badRequest(request);
            return;
        }

        UserUtils.getUserInfos(eb, userId, user -> {
            if (user == null) {
                String errorMessage = "User not found in session";
                LogHelper.logError(this, "getUsersByMEF", errorMessage);
                unauthorized(request, errorMessage);
                return;
            }

            List<String> finalTeacherStructureIds = user.getStructures();

            userService.getUsersByMEFIds(userId, mefIds, addMyself, finalTeacherStructureIds)
                .onSuccess(users -> renderJson(request, users.stream().map(UserInfos::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll)))
                .onFailure(err -> {
                    String errorMessage = "Error fetching users by MEF";
                    LogHelper.logError(this, "getUsersByMEF", errorMessage, err.getMessage());
                    badRequest(request);
                });
        });
    }
}
