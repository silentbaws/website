package com.davisellwood.website.work;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ServerErrorException;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.slf4j.Slf4j;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;
import proto.davisellwood.website.models.ProgrammingProjectOuterClass.ProgrammingProject;

@Controller
@EnableCaching
@Slf4j
public class WorkController {
    public static final String PATH_PREFIX = "work";

    private static final Duration PROJECT_CACHE_DURATION = Duration.ofMinutes(5);

    private static final String PROJECT_KEYS_ENTRY_KEY = "project-keys";
    private static final String PROJECT_KEY_PREFIX = "project-details-";

    private final Database database;
    private final ObjectStore objectStore;

    private Map<String, ProgrammingProject> projects;
    private Instant lastFetchedProjectsTime = Instant.ofEpochMilli(0);

    @Inject
    public WorkController(SpringStorageProvider storageProvider) {
        this.database = storageProvider.database();
        this.objectStore = storageProvider.objectStore();

        projects = new HashMap<>();
    }

    @GetMapping(PATH_PREFIX)
    public String getWorkHome(Model model) {
        return PATH_PREFIX + "/home";
    }

    @GetMapping(PATH_PREFIX + "/{pathProjectKey}")
    public String getMethodName(
        @PathVariable String pathProjectKey,
        Model model
    ) {
        String projectKey = PROJECT_KEY_PREFIX + pathProjectKey.toLowerCase();
        updateProjects();

        if (!projects.containsKey(projectKey)) {
            return "error";
        }
        try {
            model.addAllAttributes(toThymeMap(projects.get(projectKey)));
        } catch (Exception e) {
            throw new ServerErrorException("server error", e);
        }

        return PATH_PREFIX + "/project";
    }
    
    public Map<String, Object> toThymeMap(ProgrammingProject project) throws Exception {
        return Map.of(
            "name", project.getName(),
            "description", project.getDescription(),
            "technologies", project.getTechnologiesList(),
            "features", project.getFeaturesList().stream().map(feature -> Map.of(
                "name", feature.getName(),
                "description", feature.getDescription()
            )).collect(Collectors.toList())
        );
    }

    private void updateProjects() {
        if (Duration.between(lastFetchedProjectsTime, Instant.now()).compareTo(PROJECT_CACHE_DURATION) < 0) {
            return;
        }
        log.info("Project cache expired, attempting to fetch new values");

        Map<String, ProgrammingProject> newMap = new HashMap<>();
        DBEntry projectKeys = database.get(PROJECT_KEYS_ENTRY_KEY);
        if (projectKeys == null) {
            return;
        }

        for (String key : projectKeys.getStringListValue().getStringValueList()) {
            try {
                newMap.put(key, ProgrammingProject.parseFrom(database.get(key).getByteValue()));
            } catch (InvalidProtocolBufferException e) {
                log.error("Parsing error getting project details: ", e);
            }
        }

        lastFetchedProjectsTime = Instant.now();
        projects = newMap;
    }
}
