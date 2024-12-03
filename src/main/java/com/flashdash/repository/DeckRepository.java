package com.flashdash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flashdash.model.Deck;
import com.flashdash.model.User;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findAllByUser(User user);
    Optional<Deck> findByIdAndUser(Long deckId, User user);
}

