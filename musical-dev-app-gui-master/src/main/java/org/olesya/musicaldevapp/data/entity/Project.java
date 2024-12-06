package org.olesya.musicaldevapp.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private UUID projectId;
    private String projectName;
    private LocalDate createDate;
    private LocalDate lastChangeDate;
    private String version;
}
