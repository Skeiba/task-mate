package com.salah.taskmate.user;

import java.util.UUID;

public interface UserService {
    User findById(UUID userId);
}
