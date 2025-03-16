package com.flashdash.core.repository;

import com.flashdash.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserFrn(String userFrn);
    Optional<User> findByActivationToken(String activationToken);
    List<User> findByUserFrnIn(List<String> userFrns);
}
