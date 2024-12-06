package org.olesya.musicaldevapp.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID userId;
    private UUID roleId;
    private String userName;
    private String password;
    private Integer userAge;

    private String roleName;

    public User(UUID userId, UUID roleId, String userName, String password, Integer userAge) {
        this.userId = userId;
        this.roleId = roleId;
        this.userName = userName;
        this.password = password;
        this.userAge = userAge;
    }
}
