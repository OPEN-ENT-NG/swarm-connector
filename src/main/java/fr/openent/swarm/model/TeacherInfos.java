package fr.openent.swarm.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.openent.swarm.core.constants.Field.*;

public class TeacherInfos extends UserInfos {

    private List<School> schools;

    public TeacherInfos(JsonObject teacherInfosJson) {
        super(teacherInfosJson);
        setSchools(teacherInfosJson.getJsonArray(SCHOOLS));
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(JsonArray schools) {
        if (schools != null && !schools.isEmpty()) {
            this.schools = schools.stream()
                    .filter(Objects::nonNull)
                    .map(JsonObject.class::cast)
                    .map(School::new)
                    .collect(Collectors.toList());
        }
        else {
            this.schools = new ArrayList<>();
        }
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
    }

    public List<IdName> getAllClasses() {
        return this.getSchools().stream()
                .flatMap(school -> school.getClasses().stream())
                .collect(Collectors.toList());
    }

    public List<String> getAllClassIds() {
        return this.getSchools().stream()
                .flatMap(school -> school.getClasses().stream().map(IdName::getId))
                .collect(Collectors.toList());
    }

    public JsonObject toJson() {
        JsonObject result = super.toJson();
        return result.put(SCHOOLS, this.schools.stream().map(School::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    }
}
