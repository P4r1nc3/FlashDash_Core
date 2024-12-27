package com.flashdash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    @Query("SELECT d FROM Deck d WHERE d.user = :user AND d.isDeleted = false")
    List<Deck> findAllByUser(User user);

    @Query("SELECT d FROM Deck d WHERE d.id = :deckId AND d.user = :user AND d.isDeleted = false")
    Optional<Deck> findByIdAndUser(Long deckId, User user);

    @Modifying
    @Transactional
    @Query("UPDATE Deck d SET d.isDeleted = true WHERE d = :deck")
    void softDeleteDeck(Deck deck);
}
