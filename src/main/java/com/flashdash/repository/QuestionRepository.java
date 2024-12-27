package com.flashdash.repository;

import com.flashdash.model.Deck;
import com.flashdash.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.deck = :deck AND q.isDeleted = false")
    List<Question> findAllByDeck(Deck deck);

    @Query("SELECT q FROM Question q WHERE q.deck = :deck AND q.questionId = :questionId AND q.isDeleted = false")
    Optional<Question> findByDeckAndQuestionId(Deck deck, Long questionId);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.isDeleted = true WHERE q = :question")
    void softDeleteQuestion(Question question);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.isDeleted = true WHERE q.deck = :deck")
    void softDeleteAllByDeck(Deck deck);
}
