package com.davisellwood.website.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProjectFeature {
    private String name;
    private String description;
}
