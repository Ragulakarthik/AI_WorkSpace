package com.poc.graph_database.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node
public class Variable {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "CONNECTED_TO", direction = Relationship.Direction.OUTGOING)
    private List<API> apis;

    public Variable() {}

    public Variable(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<API> getApis() { return apis; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setApis(List<API> apis) { this.apis = apis; }
}
