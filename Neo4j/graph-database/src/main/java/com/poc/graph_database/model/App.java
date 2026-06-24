package com.poc.graph_database.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
public class App {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "HAS_APP_VARIABLE", direction = Relationship.Direction.OUTGOING)
    private List<AppVariable> appVariables;

    public App() {}

    public App(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<AppVariable> getAppVariables() { return appVariables; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAppVariables(List<AppVariable> appVariables) { this.appVariables = appVariables; }
}
