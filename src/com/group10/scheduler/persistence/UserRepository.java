package com.group10.scheduler.persistence;

import java.util.List;

import com.group10.scheduler.accounts.RegisteredUser;

public interface UserRepository {
    List<RegisteredUser> loadUsers();
    void saveUsers(List<RegisteredUser> users);
}
