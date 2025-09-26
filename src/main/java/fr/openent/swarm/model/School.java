package fr.openent.swarm.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.openent.swarm.core.constants.Field.*;

public class School extends IdName {

    private List<IdName> classes;

    public School (JsonObject schoolJson) {
        super(schoolJson);
        setClasses(schoolJson.getJsonArray(CLASSES));
    }

    public List<IdName> getClasses() {
        return classes;
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

    public JsonObject toJson() {
        JsonObject result = super.toJson();
        return result.put(CLASSES, this.classes.stream().map(IdName::toJson).collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    }
}
