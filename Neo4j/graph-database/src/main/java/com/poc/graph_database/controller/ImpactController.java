package com.poc.graph_database.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/impacted")
public class ImpactController {

    private final Driver driver;

    public ImpactController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<Map<String, Object>>> getImpacted(
            @PathVariable String name,
            @RequestParam String type) {

        String query = "MATCH p=(impacted)-[*]->(n:" + type + " {name: $name}) " +
                       "RETURN DISTINCT labels(impacted) AS type, impacted.name AS name, " +
                       "[r IN relationships(p) | type(r)] AS relationships, " +
                       "[node IN nodes(p) | node.name] AS path";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("name", name));
            List<Map<String, Object>> impacted = new ArrayList<>();
            result.list().forEach(record -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", record.get("name").asString());
                entry.put("type", record.get("type").asList());

                List<Object> nodes = record.get("path").asList();
                List<Object> rels = record.get("relationships").asList();
                StringBuilder path = new StringBuilder();
                for (int i = 0; i < nodes.size(); i++) {
                    path.append(nodes.get(i));
                    if (i < rels.size()) path.append(" -[").append(rels.get(i)).append("]-> ");
                }
                entry.put("path", path.toString());
                impacted.add(entry);
            });
            return ResponseEntity.ok(impacted);
        }
    }
}
