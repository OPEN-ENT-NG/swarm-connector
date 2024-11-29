package fr.openent.swarm.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import org.entcore.common.user.UserInfos;

public interface UserService {
    /**
     * Récupère les utilisateurs ayant le profil "Student" et dont le module fait partie d'une liste spécifique d'ID MEF.
     *
     * @return un Future contenant un JsonArray avec les utilisateurs correspondants.
     */
    Future<JsonArray> getUsersByMEF(String userId, JsonArray mefIds);
}
