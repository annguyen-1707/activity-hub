package com.softdreams.activityhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
