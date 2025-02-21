package com.davisellwood.website.work;

import com.davisellwood.website.CachedProjectDetailsComponent;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.davisellwood.website.dagger.spring.bindings.SpringStorageProvider;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import proto.davisellwood.website.models.ProgrammingProjectOuterClass.ProgrammingProject;

@Controller
@EnableCaching
@Slf4j
public class WorkController {
    public static final String PATH_PREFIX = "work";

    private final ObjectStore objectStore;
    private final CachedProjectDetailsComponent projectDetailsComponent;

    @Inject
    public WorkController(SpringStorageProvider storageProvider,
            CachedProjectDetailsComponent projectDetailsComponent) {
        this.projectDetailsComponent = projectDetailsComponent;
        this.objectStore = storageProvider.objectStore();
    }

    @GetMapping(PATH_PREFIX)
    public String getWorkHome(Model model) {
        return PATH_PREFIX + "/home";
    }

    @GetMapping(PATH_PREFIX + "/{pathProjectKey}")
    public String getMethodName(
            @PathVariable String pathProjectKey,
            Model model) {
        Optional<ProgrammingProject> project = projectDetailsComponent.getProjectFromPathId(pathProjectKey);

        if (project.isEmpty()) {
            return "error";
        }

        model.addAllAttributes(toThymeMap(project.get()));
        return PATH_PREFIX + "/project";
    }

    public Map<String, Object> toThymeMap(ProgrammingProject project) {
        return Map.of(
                "name", project.getName(),
                "description", project.getDescription(),
                "technologies", project.getTechnologiesList(),
                "features", project.getFeaturesList().stream().map(feature -> Map.of(
                        "name", feature.getName(),
                        "description", feature.getDescription())).collect(Collectors.toList()));
    }
}
