package org.olesya.musicaldevapp.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Moderation {
    private UUID trackId;
    private UUID projectId;

    private String projectName;

    public Moderation(UUID trackId, UUID projectId) {
        this.trackId = trackId;
        this.projectId = projectId;
    }
}
