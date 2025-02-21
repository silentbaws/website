package com.davisellwood.website.common.spring.components;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;
import proto.davisellwood.website.models.ProgrammingProjectOuterClass.ProgrammingProject;

@Component
@Slf4j
public class CachedProjectDetailsComponent {
    private static final Duration PROJECT_CACHE_DURATION = Duration.ofMinutes(5);
    private static final String PROJECT_KEYS_ENTRY_KEY = "project-keys";
    private static final String PROJECT_KEY_PREFIX = "project-details-";

    private Instant lastFetchedProjectsTime = Instant.ofEpochMilli(0);

    private final Database database;
    private Map<String, ProgrammingProject> projects;

    public CachedProjectDetailsComponent(SpringStorageProvider storageProvider) {
        database = storageProvider.database();
        projects = Map.of();
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

    public List<ProgrammingProject> getAllProjects() {
        updateProjects();
        return projects.values().stream().toList();
    }

    public Optional<ProgrammingProject> getProjectFromPathId(String pathId) {
        updateProjects();
        String projectKey = PROJECT_KEY_PREFIX + pathId.toLowerCase();

        if (!projects.containsKey(projectKey)) {
            return Optional.empty();
        }

        return Optional.of(projects.get(projectKey));
    }
}
