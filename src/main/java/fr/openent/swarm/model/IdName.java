package fr.openent.swarm.model;

import io.vertx.core.json.JsonObject;

import static fr.openent.swarm.core.constants.Field.*;
import static fr.openent.swarm.core.constants.Field.GROUPS;

public class IdName {

    private String id;

    private String name;

    public IdName(JsonObject idNameJson) {
        setId(idNameJson.getString(ID, ""));
        setName(idNameJson.getString(NAME, ""));
    }
    public IdName(School school) {
        setId(school.getId());
        setName(school.getName());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(ID, this.id)
                .put(NAME, this.name);
    }
}
