package com.davisellwood.website.common.components;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import proto.davisellwood.website.models.ProgrammingProjectOuterClass.ProgrammingProject;

@Component
@Slf4j
public class CachedProjectDetailsComponent {
    private static final Duration PROJECT_CACHE_DURATION = Duration.ofMinutes(5);

    private Instant lastFetchedProjectsTime = Instant.ofEpochMilli(0);

    private final Database database;
    private Map<String, ProgrammingProject> projectsCache;

    public CachedProjectDetailsComponent(SpringStorageProvider storageProvider) {
        database = storageProvider.database();

        projectsCache = Map.of();
    }

    private void updateProjects() {
        if (Duration.between(lastFetchedProjectsTime, Instant.now()).compareTo(PROJECT_CACHE_DURATION) < 0) {
            return;
        }
        log.info("Project cache expired, attempting to fetch new values");

        Map<String, ProgrammingProject> newMap = new HashMap<>();
        List<ProgrammingProject> projects = database.getAll(ProgrammingProject.class, "projects");

        for (ProgrammingProject project : projects) {
            newMap.put(project.getPathId(), project);
        }

        lastFetchedProjectsTime = Instant.now();
        projectsCache = newMap;
    }

    public List<ProgrammingProject> getAllProjects() {
        updateProjects();
        return projectsCache.values().stream().toList();
    }

    public Optional<ProgrammingProject> getProjectFromPathId(String pathId) {
        updateProjects();
        String projectKey = pathId.toLowerCase();

        return Optional.ofNullable(projectsCache.get(projectKey));
    }
}
