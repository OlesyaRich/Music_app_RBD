package org.olesya.musicaldevapp.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUser {
    private UUID projectUserId;
    private UUID projectId;
    private UUID userId;

    private String projectName;
    private String userName;

    public ProjectUser(UUID projectUserId, UUID projectId, UUID userId) {
        this.projectUserId = projectUserId;
        this.projectId = projectId;
        this.userId = userId;
    }
}
