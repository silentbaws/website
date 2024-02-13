package com.davisellwood.website.models;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkProject {
    private String name;
    private String description;
    private List<String> technologies;
    private List<ProjectFeature> features;
}
