package com.davisellwood.website.common.advice;

import com.davisellwood.website.common.components.CachedProjectDetailsComponent;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import proto.davisellwood.website.models.ProgrammingProjectOuterClass.ProgrammingProject;

@ControllerAdvice("com.davisellwood.website.views")
@Slf4j
public class ProjectDirectoryAdvice {
    private final CachedProjectDetailsComponent projectDetailsComponent;

    public ProjectDirectoryAdvice(CachedProjectDetailsComponent projectDetailsComponent) {
        this.projectDetailsComponent = projectDetailsComponent;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAllAttributes(toThymeMap(projectDetailsComponent.getAllProjects()));
    }

    public Map<String, List<Map<String, String>>> toThymeMap(List<ProgrammingProject> projects) {
        return Map.of("projects", projects.stream().map(project -> Map.of(
                "name", project.getName(),
                "pathId", project.getPathId(),
                "previewImageURI", project.getPreviewImageURI())).toList());
    }
}