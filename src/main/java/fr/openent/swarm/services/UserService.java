package fr.openent.swarm.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface UserService {
    /**
     * Fetch and filter users according to their profile, classes, structures and MEF ids
     *
     * @return a {@link Future} with a {@link JsonArray} containing the corresponding user infos
     */
    Future<JsonArray> getUsersByMEFIds(JsonObject userInfos, List<String> mefIds, Boolean addMyself);

    /**
     * Fetch current user infos.
     *
     * @return a {@link Future} with a {@link JsonObject} describing the connected user infos
     */
    Future<JsonObject> getUserInfos(String userId);
}
