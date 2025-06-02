package fr.openent.swarm.services.impl;

import fr.openent.swarm.services.UserService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;

import java.util.List;

import static fr.openent.swarm.core.constants.Field.*;
import static fr.openent.swarm.helper.PromiseHelper.handlerEither;

public class DefaultUserService implements UserService {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);

    public Future<JsonArray> getUsersByMEFIds(JsonObject userInfos, List<String> mefIds, Boolean addMyself){
        Promise<JsonArray> promise = Promise.promise();

        this.filterUserByClasses(userInfos)
            .compose(userIds -> this.getUsersByProfileStructureAndMEFIds(userInfos, userIds, mefIds, addMyself))
            .onSuccess(promise::complete)
            .onFailure(err -> {
                log.error("Swarm-Connector@DefaultUserService::getUsersByMEFIds] Error when fetching users by classes, profile, structure and MEFids : " + err.getMessage());
                promise.fail(err.getMessage());
            });

        return promise.future();
    }

    private Future<JsonObject> filterUserByClasses(JsonObject userInfos) {
        Promise<JsonObject> promise = Promise.promise();

        String query = "MATCH (u:User) " +
                "WHERE ANY(classe IN u.classes WHERE classe IN {userClasses}) " +
                "RETURN COLLECT(DISTINCT u.id) AS userIds;";
        JsonObject params = new JsonObject().put(USER_CLASSES, userInfos.getJsonArray(CLASSES));

        String errorMessage = "[Swarm-Connector@DefaultUserService::filterUserByClasses] Error when trying to execute query";
        Neo4j.getInstance().execute(query, params, Neo4jResult.validUniqueResultHandler(handlerEither(promise, errorMessage)));

        return promise.future();
    }

    private Future<JsonArray> getUsersByProfileStructureAndMEFIds(JsonObject userInfos, JsonObject userIdsObject, List<String> mefIds, Boolean addMyself) {
        Promise<JsonArray> promise = Promise.promise();

        JsonArray userIds = userIdsObject.getJsonArray(USER_IDS);

        String query =
                // Filter classic users
                "MATCH (u:User) " +
                "WHERE u.id IN {userIds} " +
                    "AND {profile} IN u.profiles " +
                    "AND ANY(id IN {listIdMef} WHERE id = u.module) " +
                    "AND ANY(structure IN u.structures WHERE structure IN {userStructures}) " +
                "WITH COLLECT(DISTINCT u) AS filteredUsers " +
                // Add myself if addMyself is true
                "OPTIONAL MATCH (myself:User) WHERE {addMyself} = true AND myself.id = {userId} " +
                "WITH filteredUsers + CASE WHEN {addMyself} = true THEN [myself] ELSE [] END AS users " +
                "UNWIND users AS u " +
                "WITH u " +
                // Get infos for each user
                "OPTIONAL MATCH (s:Structure) WHERE s.externalId IN u.structures " +
                "OPTIONAL MATCH (c:Class) WHERE c.externalId IN u.classes " +
                "OPTIONAL MATCH (g:Group) WHERE g.externalId IN u.groups " +
                // Return these infos
                "RETURN DISTINCT u.id AS id, " +
                    "u.firstName AS firstName, " +
                    "u.lastName AS lastName, " +
                    "COALESCE(u.email, u.emailSearchField, u.emailAcademy) AS mail, " +
                    "COLLECT(DISTINCT {id: s.id, name: s.name}) AS structures, " +
                    "COLLECT(DISTINCT{id: c.id, name: c.name}) AS classes, " +
                    "CASE WHEN g IS NULL THEN [] " +
                    "ELSE COLLECT(DISTINCT {id: g.id, name: g.name}) END AS groups;";

        JsonObject params = new JsonObject()
                .put(USER_IDS, userIds)
                .put(PROFILE, STUDENT)
                .put(LIST_ID_MEF, mefIds)
                .put(USER_STRUCTURES, userInfos.getJsonArray(STRUCTURES))
                .put(ADD_MYSELF, Boolean.TRUE.equals(addMyself))
                .put(USER_ID, userInfos.getString(ID));

        String errorMessage = "[Swarm-Connector@DefaultUserService::getUsersByProfileStructureAndMEFIds] Error when trying to execute query";
        Neo4j.getInstance().execute(query, params, Neo4jResult.validResultHandler(handlerEither(promise, errorMessage)));

        return promise.future();
    }

    public Future<JsonObject> getUserInfos(String userId) {
        Promise<JsonObject> promise = Promise.promise();

        String query = "MATCH (u:User) WHERE u.id = {userId} " +
                        "RETURN DISTINCT " +
                        "u.id as id, " +
                        "u.structures as structures, " +
                        "u.classes as classes;";
        JsonObject params = new JsonObject()
                .put(USER_ID, userId);

        String errorMessage = "[Swarm-Connector@DefaultUserService::getUserInfos] Error when trying to execute query";
        Neo4j.getInstance().execute(query, params, Neo4jResult.validUniqueResultHandler(handlerEither(promise, errorMessage)));

        return promise.future();
    }
}
