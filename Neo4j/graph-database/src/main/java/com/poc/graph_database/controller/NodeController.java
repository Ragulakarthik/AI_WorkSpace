package com.poc.graph_database.controller;

import com.poc.graph_database.model.*;
import com.poc.graph_database.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/nodes")
public class NodeController {

    private final PageRepository pageRepository;
    private final VariableRepository variableRepository;
    private final APIRepository apiRepository;
    private final AppRepository appRepository;
    private final AppVariableRepository appVariableRepository;

    public NodeController(PageRepository pageRepository, VariableRepository variableRepository,
                          APIRepository apiRepository, AppRepository appRepository,
                          AppVariableRepository appVariableRepository) {
        this.pageRepository = pageRepository;
        this.variableRepository = variableRepository;
        this.apiRepository = apiRepository;
        this.appRepository = appRepository;
        this.appVariableRepository = appVariableRepository;
    }

    @PostMapping
    public ResponseEntity<?> addNode(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String type = body.get("type");

        return switch (type) {
            case "Page" -> ResponseEntity.ok(pageRepository.findByName(name).orElseGet(() -> pageRepository.save(new Page(name))));
            case "Variable" -> ResponseEntity.ok(variableRepository.findByName(name).orElseGet(() -> variableRepository.save(new Variable(name))));
            case "API" -> ResponseEntity.ok(apiRepository.findByName(name).orElseGet(() -> apiRepository.save(new API(name))));
            case "App" -> ResponseEntity.ok(appRepository.findByName(name).orElseGet(() -> appRepository.save(new App(name))));
            case "AppVariable" -> ResponseEntity.ok(appVariableRepository.findByName(name).orElseGet(() -> appVariableRepository.save(new AppVariable(name))));
            default -> ResponseEntity.badRequest().body("Unknown type: " + type);
        };
    }
}
