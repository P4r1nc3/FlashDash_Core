package com.flashdash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flashdash.model.Deck;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, String> {
    List<Deck> findAllByUserFrn(String userFrn);
    Optional<Deck> findByDeckFrnAndUserFrn(String deckFrn, String userFrn);
    @Transactional
    void deleteByDeckFrn(String deckFrn);
}
