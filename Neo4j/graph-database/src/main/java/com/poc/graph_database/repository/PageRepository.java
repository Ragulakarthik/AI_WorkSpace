package com.poc.graph_database.repository;

import com.poc.graph_database.model.Page;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageRepository extends Neo4jRepository<Page, Long> {
    Optional<Page> findByName(String name);
}
