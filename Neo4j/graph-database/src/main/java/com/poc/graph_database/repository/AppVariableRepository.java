package com.poc.graph_database.repository;

import com.poc.graph_database.model.AppVariable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppVariableRepository extends Neo4jRepository<AppVariable, Long> {
    Optional<AppVariable> findByName(String name);
}
