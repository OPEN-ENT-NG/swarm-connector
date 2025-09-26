package fr.openent.swarm.repository;

import fr.openent.swarm.model.UserInfos;
import io.vertx.core.Future;

import java.util.List;

public interface UserService {
    /**
     * Fetch and filter users according to their profile, classes, structures and MEF ids
     *
     * @return a {@link Future} with a List<{@link UserInfos}> containing the corresponding user infos
     */
    Future<List<UserInfos>> getUsersByMEFIds(String userId, List<String> mefIds, boolean addMyself, List<String> finalTeacherStructureIds);
}
