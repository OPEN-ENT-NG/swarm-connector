package fr.openent.swarm.repository.impl;

import fr.openent.swarm.services.UserRepository;
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
import static fr.openent.swarm.core.constants.Field.MEF_IDS;
import static fr.openent.swarm.helper.PromiseHelper.handlerEither;

public class DefaultUserRepository implements UserRepository {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserRepository.class);

    public Future<JsonObject> getTeacherInfos(String userId) {
        Promise<JsonObject> promise = Promise.promise();

        String query =
                "MATCH (u:User {id: {" + USER_ID + "} }) " +
                "OPTIONAL MATCH (u)-[:IN]->(:ProfileGroup)-[:DEPENDS]->(clazz:Class)-[:BELONGS]->(struct1:Structure) " +
                "OPTIONAL MATCH (u)-[:IN]->(:Group)-[:DEPENDS]->(struct2:Structure) " +
                "WITH u, struct1, struct2, COLLECT(DISTINCT {name: clazz.name, id: clazz.id}) as classes " +
                "WITH u, COLLECT(DISTINCT {name: struct1.name, id: struct1.id, classes: classes}) as schools1," +
                "COLLECT(DISTINCT {name: struct2.name, id: struct2.id}) as schools2 " +
                // Collect group infos
                "OPTIONAL MATCH (g:Group) WHERE g.externalId IN u.groups " +
                "WITH u, schools1, schools2, COLLECT(DISTINCT {id: g.id, name: g.name}) AS groups " +
                // Return user infos with details on schools (structures + classes)
                "RETURN u.id as id," +
                    "u.firstName AS firstName, " +
                    "u.lastName AS lastName, " +
                    "COALESCE(u.email, u.emailSearchField, u.emailAcademy) AS mail, " +
                    "groups, " +
                    "(schools1 + schools2) AS schools;";

        JsonObject params = new JsonObject().put(USER_ID, userId);

        String errorMessage = "[SwarmConnector@DefaultUserRepository::getTeacherInfos] Error when trying to execute query";
        Neo4j.getInstance().execute(query, params, Neo4jResult.validUniqueResultHandler(handlerEither(promise, errorMessage)));

        return promise.future();
    }

    public Future<JsonArray> getStudentsInfos(List<String> classIds, List<String> mefIds) {
        Promise<JsonArray> promise = Promise.promise();

        String query =
                "MATCH (c:Class)<-[:DEPENDS]-(cpg:ProfileGroup) " +
                "MATCH (cpg)<-[:IN]-(u:User)-[:IN]->(spg:ProfileGroup) " +
                "MATCH (cpg)-[:DEPENDS]->(spg)-[:HAS_PROFILE]->(p:Profile) " +
                "WHERE c.id IN {" + CLASS_IDS + "} " +
                "AND p.name = {"+ PROFILE +"} " +
                "AND u.module IN {" + MEF_IDS + "}" +
                "OPTIONAL MATCH (g:Group) WHERE g.externalId IN u.groups " +
                "WITH u, c, COLLECT(DISTINCT {id: g.id, name: g.name}) AS groups " +
                "RETURN DISTINCT u.id AS id," +
                    "u.firstName AS firstName," +
                    "u.lastName AS lastName, " +
                    "COALESCE(u.email, u.emailSearchField, u.emailAcademy) AS mail, " +
                    "[] AS structures, " +
                    "COLLECT(DISTINCT {id: c.id, name: c.name}) AS classes, " +
                    "groups;";

        JsonObject params = new JsonObject()
                .put(CLASS_IDS, classIds)
                .put(PROFILE, STUDENT)
                .put(MEF_IDS, mefIds);

        String errorMessage = "[SwarmConnector@DefaultUserService::getStudentsInfos] Error when trying to execute query";
        Neo4j.getInstance().execute(query, params, Neo4jResult.validResultHandler(handlerEither(promise, errorMessage)));

        return promise.future();
    }

}
