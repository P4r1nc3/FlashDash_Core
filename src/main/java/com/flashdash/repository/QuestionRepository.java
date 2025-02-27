package com.flashdash.repository;

import com.flashdash.model.Deck;
import com.flashdash.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByDeck(Deck deck);

    Optional<Question> findByDeckAndQuestionId(Deck deck, Long questionId);

    @Modifying
    @Transactional
    void deleteAllByDeck(Deck deck);
}
