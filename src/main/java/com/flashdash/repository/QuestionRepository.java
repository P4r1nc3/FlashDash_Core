package com.flashdash.repository;

import com.flashdash.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findAllByDeckFrn(String deckFrn);

    Optional<Question> findByDeckFrnAndQuestionFrn(String deckFrn, String questionFrn);

    @Transactional
    void deleteAllByDeckFrn(String deckFrn);
}
