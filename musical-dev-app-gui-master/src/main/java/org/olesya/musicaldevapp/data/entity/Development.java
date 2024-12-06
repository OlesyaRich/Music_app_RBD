package org.olesya.musicaldevapp.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Development {
    private UUID fileId;
    private UUID projectId;
    private File codeFile;
    private String version;
    private LocalDate createDate;
    private LocalDate lastChangeDate;
    private String fileExtension;

    private String projectName;

    public Development(UUID fileId, UUID projectId, File codeFile, String version, LocalDate createDate, LocalDate lastChangeDate, String fileExtension) {
        this.fileId = fileId;
        this.projectId = projectId;
        this.codeFile = codeFile;
        this.version = version;
        this.createDate = createDate;
        this.lastChangeDate = lastChangeDate;
        this.fileExtension = fileExtension;
    }
}
