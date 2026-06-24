package com.poc.graph_database.repository;

import com.poc.graph_database.model.App;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppRepository extends Neo4jRepository<App, Long> {
    Optional<App> findByName(String name);
}
