package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.Deck;
import com.flashdash.model.Question;
import com.flashdash.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldFindAllQuestionsByDeckFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        Question question1 = TestUtils.createQuestion(deck, "What is Java?");
        Question question2 = TestUtils.createQuestion(deck, "What is Spring?");
        questionRepository.save(question1);
        questionRepository.save(question2);

        // Act
        List<Question> questions = questionRepository.findAllByDeckFrn(deck.getDeckFrn());

        // Assert
        assertThat(questions).hasSize(2);
        assertThat(questions).extracting(Question::getQuestion)
                .containsExactlyInAnyOrder("What is Java?", "What is Spring?");
        assertThat(questions).extracting(Question::getDeckFrn)
                .containsOnly(deck.getDeckFrn());
    }

    @Test
    void shouldFindSpecificQuestionByDeckFrnAndQuestionFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        Question question = TestUtils.createQuestion(deck, "What is Java?");
        questionRepository.save(question);

        // Act
        Optional<Question> foundQuestion = questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn());

        // Assert
        assertThat(foundQuestion).isPresent();
        assertThat(foundQuestion.get().getQuestion()).isEqualTo("What is Java?");
        assertThat(foundQuestion.get().getDeckFrn()).isEqualTo(deck.getDeckFrn());
    }

    @Test
    void shouldReturnEmptyWhenQuestionNotExistsForDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<Question> foundQuestion = questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "frn:flashdash:question:nonexistent");

        // Assert
        assertThat(foundQuestion).isNotPresent();
    }

    @Test
    void shouldDeleteAllQuestionsByDeckFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        Question question1 = TestUtils.createQuestion(deck, "What is Java?");
        Question question2 = TestUtils.createQuestion(deck, "What is Spring?");
        questionRepository.save(question1);
        questionRepository.save(question2);

        // Act
        questionRepository.deleteAllByDeckFrn(deck.getDeckFrn());

        // Assert
        List<Question> deletedQuestions = questionRepository.findAllByDeckFrn(deck.getDeckFrn());
        assertThat(deletedQuestions).isEmpty();
    }

    @Test
    void shouldNotFindDeletedQuestions() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        Question question1 = TestUtils.createQuestion(deck, "What is Java?");
        Question question2 = TestUtils.createQuestion(deck, "What is Spring?");
        questionRepository.save(question1);
        questionRepository.save(question2);

        // Act
        questionRepository.delete(question1);

        // Assert
        List<Question> questions = questionRepository.findAllByDeckFrn(deck.getDeckFrn());
        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).getQuestion()).isEqualTo("What is Spring?");
    }
}
