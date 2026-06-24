package com.poc.graph_database.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
public class Page {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private App app;

    @Relationship(type = "HAS_VARIABLE", direction = Relationship.Direction.OUTGOING)
    private List<Variable> variables;

    @Relationship(type = "USES", direction = Relationship.Direction.OUTGOING)
    private List<AppVariable> usedAppVariables;

    public Page() {}

    public Page(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public App getApp() { return app; }
    public List<Variable> getVariables() { return variables; }
    public List<AppVariable> getUsedAppVariables() { return usedAppVariables; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setApp(App app) { this.app = app; }
    public void setVariables(List<Variable> variables) { this.variables = variables; }
    public void setUsedAppVariables(List<AppVariable> usedAppVariables) { this.usedAppVariables = usedAppVariables; }
}
