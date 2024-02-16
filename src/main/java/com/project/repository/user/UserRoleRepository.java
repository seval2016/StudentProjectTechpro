package com.project.repository.user;

import com.project.entity.concretes.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<User,Integer> {
}
