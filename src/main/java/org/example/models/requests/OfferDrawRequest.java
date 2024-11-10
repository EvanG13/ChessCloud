package org.example.models.requests;

import org.example.enums.OfferDrawAction;

public record OfferDrawRequest(String gameId, OfferDrawAction drawAction) {}
