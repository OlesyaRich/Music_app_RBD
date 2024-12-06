package org.olesya.musicaldevapp.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectAggregatedInfo {
    private UUID projectId;
    private String projectName;
    private LocalDate createDate;
    private LocalDate lastChangeDate;
    private Integer projectRequirementsAmount;

    public ProjectAggregatedInfo(UUID projectId, String projectName, LocalDate createDate, LocalDate lastChangeDate) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.createDate = createDate;
        this.lastChangeDate = lastChangeDate;
    }
}
