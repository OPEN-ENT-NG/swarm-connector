package fr.openent.swarm.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.openent.swarm.core.constants.Field.*;

public class UserInfos {

    private String id;
    private String firstName;
    private String lastName;
    private String mail;
    private List<IdName> structures;
    private List<IdName> classes;
    private List<IdName> groups;

    public UserInfos(JsonObject userInfosJson) {
        setId(userInfosJson.getString(ID));
        setFirstName(userInfosJson.getString(FIRST_NAME));
        setLastName(userInfosJson.getString(LAST_NAME));
        setMail(userInfosJson.getString(MAIL));
        setStructures(userInfosJson.getJsonArray(STRUCTURES));
        setClasses(userInfosJson.getJsonArray(CLASSES));
        setGroups(userInfosJson.getJsonArray(GROUPS));
    }

    public UserInfos(TeacherInfos teacherInfos) {
        setId(teacherInfos.getId());
        setFirstName(teacherInfos.getFirstName());
        setLastName(teacherInfos.getLastName());
        setMail(teacherInfos.getMail());
        setStructures(teacherInfos.getSchools());
        setClasses(teacherInfos.getAllClasses());
        setGroups(new ArrayList<>()); // TODO one day, when ordered
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMail() {
        return mail;
    }

    public List<IdName> getStructures() {
        return structures;
    }

    public List<IdName> getClasses() {
        return classes;
    }

    public List<IdName> getGroups() {
        return groups;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setStructures(List<School> schools) {
        this.structures = schools.stream()
                .filter(Objects::nonNull)
                .map(IdName::new)
                .collect(Collectors.toList());
    }

    public void setStructures(JsonArray structures) {
        if (structures != null && !structures.isEmpty()) {
            this.structures = structures.stream()
                    .filter(Objects::nonNull)
                    .map(JsonObject.class::cast)
                    .map(IdName::new)
                    .collect(Collectors.toList());
        }
        else {
            this.structures = new ArrayList<>();
        }
    }

    public void setClasses(List<IdName> classes) {
        this.classes = classes;
    }

    public void setClasses(JsonArray classes) {
        if (classes != null && !classes.isEmpty()) {
            this.classes = classes.stream()
                    .filter(Objects::nonNull)
                    .map(JsonObject.class::cast)
                    .map(IdName::new)
                    .collect(Collectors.toList());
        }
        else {
            this.classes = new ArrayList<>();
        }
    }

    public void setGroups(List<IdName> groups) {
        this.groups = groups;
    }

    public void setGroups(JsonArray groups) {
        if (groups != null && !groups.isEmpty()) {
            this.groups = groups.stream()
                    .filter(Objects::nonNull)
                    .map(JsonObject.class::cast)
                    .map(IdName::new)
                    .collect(Collectors.toList());
        }
        else {
            this.groups = new ArrayList<>();
        }
    }

    public List<String> getAllClassIds() {
        return this.getClasses().stream()
                .map(IdName::getId)
                .collect(Collectors.toList());
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(ID, this.id)
                .put(FIRST_NAME, this.firstName)
                .put(LAST_NAME, this.lastName)
                .put(MAIL, this.mail)
                .put(STRUCTURES, this.structures.stream().map(IdName::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll))
                .put(CLASSES, this.classes.stream().map(IdName::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll))
                .put(GROUPS, this.groups.stream().map(IdName::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    }
}
