package com.poc.graph_database.repository;

import com.poc.graph_database.model.Variable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VariableRepository extends Neo4jRepository<Variable, Long> {
    Optional<Variable> findByName(String name);
}
