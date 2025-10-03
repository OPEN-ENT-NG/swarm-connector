package fr.openent.swarm.services.impl;

import fr.openent.swarm.helper.LogHelper;
import fr.openent.swarm.model.School;
import fr.openent.swarm.model.TeacherInfos;
import fr.openent.swarm.model.UserInfos;
import fr.openent.swarm.repository.impl.DefaultUserRepository;
import fr.openent.swarm.repository.UserService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultUserService implements UserService {
    private static final Logger log = LoggerFactory.getLogger(DefaultUserService.class);

    private final DefaultUserRepository userRepository;

    public DefaultUserService() {
        userRepository = new DefaultUserRepository();
    }

    public Future<List<UserInfos>> getUsersByMEFIds(String userId, List<String> mefIds, boolean addMyself, List<String> finalTeacherStructureIds) {
        Promise<List<UserInfos>> promise = Promise.promise();

        this.userRepository.getTeacherInfos(userId)
            .compose(teacherInfosJson -> getUsersInfosFormatted(teacherInfosJson, mefIds, addMyself, finalTeacherStructureIds))
            .onSuccess(promise::complete)
            .onFailure(err -> {
                String errorMessage = "Error when fetching users by classes, profile, structure and MEFids";
                LogHelper.logError(this, "getUsersByMEFIds", errorMessage, err.getMessage());
                promise.fail(err.getMessage());
            });

        return promise.future();
    }

    private Future<List<UserInfos>> getUsersInfosFormatted(JsonObject teacherInfosJson, List<String> mefIds, boolean addMyself, List<String> finalTeacherStructureIds) {
        Promise<List<UserInfos>> promise = Promise.promise();

        TeacherInfos teacherInfos = new TeacherInfos(teacherInfosJson);
        List<String> classIds = teacherInfos.getAllClassIds();

        this.userRepository.getStudentsInfos(classIds, mefIds)
            .compose(studentsInfosJson -> formatData(studentsInfosJson, teacherInfos, addMyself, finalTeacherStructureIds))
            .onSuccess(promise::complete)
            .onFailure(err -> {
                String errorMessage = "Error when fetching users by classes, profile and MEFids";
                LogHelper.logError(this, "getUsersInfosFormatted", errorMessage, err.getMessage());
                promise.fail(err.getMessage());
            });

        return promise.future();
    }

    private Future<List<UserInfos>> formatData(JsonArray studentsInfosJson, TeacherInfos teacherInfos, boolean addMyself, List<String> finalTeacherStructureIds) {
        Promise<List<UserInfos>> promise = Promise.promise();

        List<UserInfos> studentsInfos = studentsInfosJson.stream()
                .filter(Objects::nonNull)
                .map(JsonObject.class::cast)
                .map(UserInfos::new)
                .collect(Collectors.toList());

        // Fill result with structures infos from teacherInfos
        studentsInfos.forEach(student -> {
            List<String> classIds = student.getAllClassIds();
            List<School> matchingSchools = teacherInfos.getSchools().stream()
                    .filter(school -> school.getClasses() != null &&
                            school.getClasses().stream().anyMatch(cl -> classIds.contains(cl.getId())))
                    .collect(Collectors.toList());
            student.setStructures(matchingSchools);
        });

        if (addMyself) {
            List<School> filteredSchools = teacherInfos.getSchools().stream()
                    .filter(school -> finalTeacherStructureIds.contains(school.getId()))
                    .collect(Collectors.toList());
            teacherInfos.setSchools(filteredSchools);
            studentsInfos.add(new UserInfos(teacherInfos));
        }

        promise.complete(studentsInfos);

        return promise.future();
    }

}
