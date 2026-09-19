package com.softdreams.activityhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    @Query("""
				SELECT u.id, r
				FROM User u
				JOIN u.roles r
				WHERE u.id IN :userIds
			""")
    List<Object[]> findRolesByUserIds(@Param("userIds") List<String> userIds);
}
