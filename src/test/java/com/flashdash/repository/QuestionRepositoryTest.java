package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.User;
import com.flashdash.model.question.Question;
import com.flashdash.model.question.QuestionDeck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAllQuestionsByDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        QuestionDeck deck = TestUtils.createQuestionDeck(user);
        deckRepository.save(deck);

        Question question1 = TestUtils.createQuestion(deck, "What is Java?");
        Question question2 = TestUtils.createQuestion(deck, "What is Spring?");
        questionRepository.save(question1);
        questionRepository.save(question2);

        // Act
        List<Question> questions = questionRepository.findAllByDeck(deck);

        // Assert
        assertThat(questions).hasSize(2);
        assertThat(questions).extracting(Question::getQuestion)
                .containsExactlyInAnyOrder("What is Java?", "What is Spring?");
    }


    @Test
    void shouldFindSpecificQuestionByDeckAndId() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        QuestionDeck deck = TestUtils.createQuestionDeck(user);
        deckRepository.save(deck);

        Question question = TestUtils.createQuestion(deck, "What is Java?");
        question = questionRepository.save(question);

        // Act
        Optional<Question> foundQuestion = questionRepository.findByDeckAndQuestionId(deck, question.getQuestionId());

        // Assert
        assertThat(foundQuestion).isPresent();
        assertThat(foundQuestion.get().getQuestion()).isEqualTo("What is Java?");
    }

    @Test
    void shouldReturnEmptyWhenQuestionNotInDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        QuestionDeck deck = TestUtils.createQuestionDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<Question> foundQuestion = questionRepository.findByDeckAndQuestionId(deck, 999L);

        // Assert
        assertThat(foundQuestion).isNotPresent();
    }

    @Test
    void shouldDeleteAllQuestionsByDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        QuestionDeck deck = TestUtils.createQuestionDeck(user);
        deckRepository.save(deck);

        Question question1 = TestUtils.createQuestion(deck, "What is Java?");
        Question question2 = TestUtils.createQuestion(deck, "What is Spring?");
        questionRepository.save(question1);
        questionRepository.save(question2);

        // Act
        questionRepository.deleteAllByDeck(deck);
        List<Question> questions = questionRepository.findAllByDeck(deck);

        // Assert
        assertThat(questions).isEmpty();
    }
}
