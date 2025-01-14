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

import static fr.openent.swarm.core.constants.Field.*;

public class DefaultUserService implements UserService {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);

    public Future<JsonArray> getUsersByMEF(String userId, JsonArray mefIds){
        Promise<JsonArray> promise = Promise.promise();
        this.getUserInfos(userId)
                .onSuccess(userInfos -> {
                    String query = "MATCH (u:User) " +
                            "WHERE {profile} IN u.profiles " +
                            "AND ANY(id IN {listIdMef} WHERE id = u.module) " +
                            "AND ANY(structure IN u.structures WHERE structure IN {userStructures}) " +
                            "AND ANY(classe IN u.classes WHERE classe IN {userClasses}) " +
                            "OPTIONAL MATCH (s:Structure) WHERE s.externalId IN u.structures " +
                            "OPTIONAL MATCH (c:Class) WHERE c.externalId IN u.classes " +
                            "OPTIONAL MATCH (g:Group) WHERE g.externalId IN u.groups " +
                            "RETURN u.id AS id, " +
                            "u.firstName AS firstName, " +
                            "u.lastName AS lastName, " +
                            "COLLECT(DISTINCT{structureId: s.id, name: s.name}) AS structures, " +
                            "COLLECT(DISTINCT{classId: c.id, name: c.name}) AS classes, " +
                            "CASE WHEN g IS NULL THEN [] " +
                            "ELSE COLLECT(DISTINCT {groupId: g.id, name: g.name}) END AS groups";
                    JsonObject params = new JsonObject()
                            .put(LIST_ID_MEF, mefIds)
                            .put(USER_STRUCTURES, userInfos.getJsonArray(STRUCTURES))
                            .put(USER_CLASSES, userInfos.getJsonArray(CLASSES))
                            .put(PROFILE, STUDENT);
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
    private Future<JsonObject> getUserInfos(String userId){
        Promise<JsonObject> promise = Promise.promise();
        String query = "MATCH (u:User) " +
                        "WHERE u.id = {userId} " +
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
