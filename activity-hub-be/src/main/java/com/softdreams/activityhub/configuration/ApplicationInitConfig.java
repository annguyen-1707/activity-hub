package com.softdreams.activityhub.configuration;

import java.util.HashSet;
import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.softdreams.activityhub.constant.PredefinedRole;
import com.softdreams.activityhub.entity.Category;
import com.softdreams.activityhub.entity.Role;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.repository.CategoryRepository;
import com.softdreams.activityhub.repository.RoleRepository;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CategoryRepository categoryRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }

            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                        Category.builder().code("PHONE").name("Điện thoại").description("Điện thoại di động & Smartphone thông minh").active(true).build(),
                        Category.builder().code("LAPTOP").name("Laptop").description("Máy tính xách tay cao cấp, gaming & văn phòng").active(true).build(),
                        Category.builder().code("TABLET").name("Máy tính bảng").description("Máy tính bảng iPad, Android đa năng").active(true).build(),
                        Category.builder().code("WATCH").name("Đồng hồ thông minh").description("Smartwatch theo dõi sức khỏe & thể thao").active(true).build(),
                        Category.builder().code("HEADPHONE").name("Tai nghe").description("Tai nghe Bluetooth, chống ồn True Wireless").active(true).build(),
                        Category.builder().code("ACCESSORY").name("Phụ kiện").description("Cáp sạc, pin dự phòng, ốp lưng & phụ kiện").active(true).build()
                ));
                log.info("Default categories have been initialized.");
            }

            log.info("Application initialization completed .....");
        };
    }
}
