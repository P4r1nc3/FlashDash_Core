package com.flashdash.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flashdash.core.model.Deck;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, String> {
    List<Deck> findAllByUserFrn(String userFrn);
    Optional<Deck> findByDeckFrnAndUserFrn(String deckFrn, String userFrn);
}
