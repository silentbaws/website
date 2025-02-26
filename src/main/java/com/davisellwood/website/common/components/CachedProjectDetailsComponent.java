package com.davisellwood.website.common.components;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import proto.davisellwood.website.models.ProgrammingProjectOuterClass.ProgrammingProject;

@Component
@Slf4j
public class CachedProjectDetailsComponent {
    private final Database database;

    public CachedProjectDetailsComponent(SpringStorageProvider storageProvider) {
        database = storageProvider.database();
    }

    @Cacheable("allWorkProjects")
    public List<ProgrammingProject> getAllProjects() {
        log.info("Getting all projects from db");
        return database.getAll(ProgrammingProject.class, "projects");
    }

    @Cacheable("workProject")
    public Optional<ProgrammingProject> getProjectFromPathId(String pathId) {
        log.info("Getting project from db");
        return Optional.of(database.get(ProgrammingProject.class, "projects", "pathId", pathId));
    }
}
