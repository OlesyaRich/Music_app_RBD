package org.olesya.musicaldevapp.utils;

import lombok.Getter;
import lombok.Setter;
import org.olesya.musicaldevapp.data.entity.User;

public class CurrentUserContainer {
    @Getter
    @Setter
    private static User currentUser;
}
