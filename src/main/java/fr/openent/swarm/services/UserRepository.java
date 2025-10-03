package fr.openent.swarm.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface UserRepository {
    /**
     * Fetch infos of connected teacher
     *
     * @return a {@link Future} with a {@link JsonObject} containing the corresponding user infos
     */
    Future<JsonObject> getTeacherInfos(String userId);

    /**
     * Fetch and filter users according to their profile, classes and MEF ids
     *
     * @return a {@link Future} with a {@link JsonArray} containing the corresponding user infos
     */
    Future<JsonArray> getStudentsInfos(List<String> classIds, List<String> mefIds);
}
