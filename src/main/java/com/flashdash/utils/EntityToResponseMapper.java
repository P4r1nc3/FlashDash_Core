package com.flashdash.utils;

import com.flashdash.model.Deck;
import com.p4r1nc3.flashdash.core.model.DeckResponse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class EntityToResponseMapper {

    private EntityToResponseMapper() {
    }

    public static DeckResponse toDeckResponse(Deck deck) {
        DeckResponse deckResponse = new DeckResponse();
        deckResponse.setDeckId(deck.getId());
        deckResponse.setName(deck.getName());
        deckResponse.setDescription(deck.getDescription());
        deckResponse.setCreatedAt(deck.getCreatedAt().atOffset(ZoneOffset.UTC));
        deckResponse.setUpdatedAt(deck.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return deckResponse;
    }

    public static List<DeckResponse> toDeckResponseList(List<Deck> deckList) {
        return deckList.stream()
                .map(EntityToResponseMapper::toDeckResponse)
                .collect(Collectors.toList());
    }
}
