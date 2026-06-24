package com.poc.graph_database.controller;

import com.poc.graph_database.model.*;
import com.poc.graph_database.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/relationships")
public class RelationshipController {

    private final PageRepository pageRepository;
    private final VariableRepository variableRepository;
    private final APIRepository apiRepository;
    private final AppRepository appRepository;
    private final AppVariableRepository appVariableRepository;

    public RelationshipController(PageRepository pageRepository, VariableRepository variableRepository,
                                  APIRepository apiRepository, AppRepository appRepository,
                                  AppVariableRepository appVariableRepository) {
        this.pageRepository = pageRepository;
        this.variableRepository = variableRepository;
        this.apiRepository = apiRepository;
        this.appRepository = appRepository;
        this.appVariableRepository = appVariableRepository;
    }

    @PostMapping
    public ResponseEntity<String> addRelationship(@RequestBody Map<String, String> body) {
        String fromName = body.get("fromName");
        String fromType = body.get("fromType");
        String toName = body.get("toName");
        String toType = body.get("toType");
        String relationshipType = body.get("relationshipType");

        switch (relationshipType) {
            case "BELONGS_TO" -> {
                Page page = pageRepository.findByName(fromName).orElseThrow();
                App app = appRepository.findByName(toName).orElseThrow();
                page.setApp(app);
                pageRepository.save(page);
            }
            case "HAS_VARIABLE" -> {
                Page page = pageRepository.findByName(fromName).orElseThrow();
                Variable variable = variableRepository.findByName(toName).orElseThrow();
                if (page.getVariables() == null) page.setVariables(new ArrayList<>());
                page.getVariables().add(variable);
                pageRepository.save(page);
            }
            case "USES" -> {
                Page page = pageRepository.findByName(fromName).orElseThrow();
                AppVariable appVariable = appVariableRepository.findByName(toName).orElseThrow();
                if (page.getUsedAppVariables() == null) page.setUsedAppVariables(new ArrayList<>());
                page.getUsedAppVariables().add(appVariable);
                pageRepository.save(page);
            }
            case "HAS_APP_VARIABLE" -> {
                App app = appRepository.findByName(fromName).orElseThrow();
                AppVariable appVariable = appVariableRepository.findByName(toName).orElseThrow();
                if (app.getAppVariables() == null) app.setAppVariables(new ArrayList<>());
                app.getAppVariables().add(appVariable);
                appRepository.save(app);
            }
            case "CONNECTED_TO" -> {
                Variable variable = variableRepository.findByName(fromName).orElseThrow();
                API api = apiRepository.findByName(toName).orElseThrow();
                if (variable.getApis() == null) variable.setApis(new ArrayList<>());
                variable.getApis().add(api);
                variableRepository.save(variable);
            }
            default -> {
                return ResponseEntity.badRequest().body("Unknown relationship type: " + relationshipType);
            }
        }

        return ResponseEntity.ok(fromName + " -[" + relationshipType + "]-> " + toName);
    }
}
