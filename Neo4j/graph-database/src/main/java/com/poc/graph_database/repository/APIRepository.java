package com.poc.graph_database.repository;

import com.poc.graph_database.model.API;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface APIRepository extends Neo4jRepository<API, Long> {
    Optional<API> findByName(String name);
}
