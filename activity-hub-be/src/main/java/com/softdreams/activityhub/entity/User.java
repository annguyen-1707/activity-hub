package com.softdreams.activityhub.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "username", unique = true, length = 255)
    String username;

    String password;

    @Column(columnDefinition = "NVARCHAR(255)")
    String firstName;

    LocalDate dob;

    @Column(columnDefinition = "NVARCHAR(255)")
    String lastName;

    @ManyToMany(fetch = FetchType.LAZY)
    Set<Role> roles;
}
