package com.softdreams.activityhub.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    boolean existsByRoles_Name(String name);

    Optional<User> findByUsername(String username);

    @Query(
            """
				SELECT u
				FROM User u
				WHERE (
					:keyword IS NULL
					OR :keyword = ''
					OR u.username LIKE CONCAT('%', :keyword, '%')
					OR u.firstName LIKE CONCAT('%', :keyword, '%')
					OR u.lastName LIKE CONCAT('%', :keyword, '%')
				)
			""")
    Page<User> searchUserByKeyword(Pageable pageable, @Param("keyword") String keyword);
}
