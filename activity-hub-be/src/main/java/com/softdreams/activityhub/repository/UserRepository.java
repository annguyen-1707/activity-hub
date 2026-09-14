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

    Optional<User> findByUsername(String username);

    @Query(
            value = """
        SELECT *
        FROM users
        WHERE (
            :keyword IS NULL
            OR :keyword = ''
            OR username LIKE CONCAT('%', :keyword, '%')
            OR first_name LIKE CONCAT('%', :keyword, '%')
            OR last_name LIKE CONCAT('%', :keyword, '%')
        )
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM users
        WHERE (
            :keyword IS NULL
            OR :keyword = ''
            OR username LIKE CONCAT('%', :keyword, '%')
            OR first_name LIKE CONCAT('%', :keyword, '%')
            OR last_name LIKE CONCAT('%', :keyword, '%')
        )
        """,
            nativeQuery = true
    )
    Page<User> searchUserByKeyword(Pageable pageable, @Param("keyword") String keyword);
}
