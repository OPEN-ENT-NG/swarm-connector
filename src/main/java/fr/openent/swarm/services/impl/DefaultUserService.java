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

public class DefaultUserService implements UserService {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);

    public Future<JsonArray> getUsersByMEF(String userId, List<String> mefIds, Boolean addMyself){
        Promise<JsonArray> promise = Promise.promise();

        this.getUserInfos(userId)
            .onSuccess(userInfos -> {
                String query = getUsersByMEFQuery(addMyself);
                JsonObject params = new JsonObject()
                        .put(PROFILE, STUDENT)
                        .put(LIST_ID_MEF, mefIds)
                        .put(USER_STRUCTURES, userInfos.getJsonArray(STRUCTURES))
                        .put(USER_CLASSES, userInfos.getJsonArray(CLASSES))
                        .put(USER_ID, userId);
                Neo4j.getInstance().execute(query, params, Neo4jResult.validResultHandler(event -> {
                    if (event.isLeft()) {
                        log.error("[Swarm-Connector@DefaultUserService::getUsersByMEF] Error when trying to execute query" + event.left().getValue());
                        promise.fail(event.left().getValue());
                    } else {
                        promise.complete(event.right().getValue());
                    }
                }));
            });

        return promise.future();
    }

    private static String getUsersByMEFQuery(Boolean addMyself) {
        String addMyselfQuery = Boolean.TRUE.equals(addMyself) ? "OR u.id = {userId} " : "";

        return "MATCH (u:User) " +
                "WHERE (" +
                    "{profile} IN u.profiles " +
                    "AND ANY(id IN {listIdMef} WHERE id = u.module) " +
                    "AND ANY(structure IN u.structures WHERE structure IN {userStructures}) " +
                    "AND ANY(classe IN u.classes WHERE classe IN {userClasses}) " +
                ") " + addMyselfQuery +
                "OPTIONAL MATCH (s:Structure) WHERE s.externalId IN u.structures " +
                "OPTIONAL MATCH (c:Class) WHERE c.externalId IN u.classes " +
                "OPTIONAL MATCH (g:Group) WHERE g.externalId IN u.groups " +
                "RETURN u.id AS id, " +
                "u.firstName AS firstName, " +
                "u.lastName AS lastName, " +
                "COALESCE(u.email, u.emailSearchField, u.emailAcademy) AS mail, " +
                "COLLECT(DISTINCT{id: s.id, name: s.name}) AS structures, " +
                "COLLECT(DISTINCT{id: c.id, name: c.name}) AS classes, " +
                "CASE WHEN g IS NULL THEN [] " +
                "ELSE COLLECT(DISTINCT {id: g.id, name: g.name}) END AS groups;";
    }

    private Future<JsonObject> getUserInfos(String userId){
        Promise<JsonObject> promise = Promise.promise();
        String query = "MATCH (u:User) WHERE u.id = {userId} " +
                        "RETURN DISTINCT " +
                        "u.structures as structures, " +
                        "u.classes as classes;";
        JsonObject params = new JsonObject()
                .put(USER_ID, userId);
        Neo4j.getInstance().execute(query, params, Neo4jResult.validUniqueResultHandler(event -> {
            if (event.isLeft()) {
                log.error("[Swarm-Connector@DefaultUserService::getUserInfos] Error when trying to execute query" + event.left().getValue());
                promise.fail(event.left().getValue());
            } else {
                promise.complete(event.right().getValue());
            }
        }));
        return promise.future();
    }
}
