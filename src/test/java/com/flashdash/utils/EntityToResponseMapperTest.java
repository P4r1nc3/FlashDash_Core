package com.flashdash.utils;

import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import com.flashdash.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntityToResponseMapperTest {

    @Test
    void shouldConvertDeckToDeckResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        // Act
        DeckResponse deckResponse = EntityToResponseMapper.toDeckResponse(deck);

        // Assert
        assertThat(deckResponse).isNotNull();
        assertThat(deckResponse.getDeckId()).isEqualTo(deck.getId());
        assertThat(deckResponse.getName()).isEqualTo(deck.getName());
        assertThat(deckResponse.getDescription()).isEqualTo(deck.getDescription());
        assertThat(deckResponse.getCreatedAt()).isEqualTo(deck.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
        assertThat(deckResponse.getUpdatedAt()).isEqualTo(deck.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC));
    }

    @Test
    void shouldConvertDeckListToDeckResponseList() {
        // Arrange
        User user = TestUtils.createUser();
        List<Deck> decks = List.of(TestUtils.createDeck(user));

        // Act
        List<DeckResponse> deckResponses = EntityToResponseMapper.toDeckResponseList(decks);

        // Assert
        assertThat(deckResponses).isNotEmpty();
        assertThat(deckResponses).hasSize(decks.size());
        assertThat(deckResponses.get(0).getDeckId()).isEqualTo(decks.get(0).getId());
        assertThat(deckResponses.get(0).getName()).isEqualTo(decks.get(0).getName());
        assertThat(deckResponses.get(0).getDescription()).isEqualTo(decks.get(0).getDescription());
    }
}
