/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Gets the credit ratings of an {@link LegalEntity}.
 */
//TODO bean for the top level class
public class LegalEntityCreditRatings implements LegalEntityMeta<LegalEntity> {
  private final boolean _useRating;
  private final Set<String> _perAgencyRatings;
  private final boolean _useRatingDescription;
  private final Set<String> _perAgencyRatingDescriptions;

  protected LegalEntityCreditRatings(final boolean useRating, final Set<String> ratings, final boolean useRatingDescription,
      final Set<String> ratingDescriptions) {
    _useRating = useRating;
    _perAgencyRatings = ratings;
    _useRatingDescription = useRatingDescription;
    _perAgencyRatingDescriptions = ratingDescriptions;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "legal entity");
    final Set<CreditRating> creditRatings = legalEntity.getCreditRatings();
    if (creditRatings == null) {
      throw new IllegalStateException("Credit ratings for this legal entity " + legalEntity + " were null");
    }
    if (!(_useRating || _useRatingDescription)) {
      return creditRatings;
    }
    int ratingCount = 0;
    int ratingDescriptionCount = 0;
    final Set<Object> selections = new HashSet<>();
    for (final CreditRating creditRating : creditRatings) {
      final String agencyName = creditRating.getAgencyName();
      if (_useRating) {
        final Pair<String, String> agencyRatingPair = Pairs.of(agencyName, creditRating.getRating());
        if (_perAgencyRatings.isEmpty()) {
          selections.add(agencyRatingPair);
        }
        if (_perAgencyRatings.contains(agencyName)) {
          selections.add(agencyRatingPair);
          ratingCount++;
        }
      }
      if (_useRatingDescription) {
        if (creditRating.getRatingDescription() == null) {
          throw new IllegalStateException("Credit rating " + creditRating + " does not contain rating description for " + agencyName);
        }
        final Pair<String, String> agencyRatingDescription = Pairs.of(agencyName, creditRating.getRatingDescription());
        if (_perAgencyRatingDescriptions.isEmpty()) {
          selections.add(agencyRatingDescription);
        }
        if (_perAgencyRatingDescriptions.contains(agencyName)) {
          selections.add(agencyRatingDescription);
          ratingDescriptionCount++;
        }
      }
    }
    if (ratingCount != _perAgencyRatings.size()) {
      throw new IllegalStateException("Credit ratings " + creditRatings + " do not contain matches for " + _perAgencyRatings);
    }
    if (ratingDescriptionCount != _perAgencyRatingDescriptions.size()) {
      throw new IllegalStateException("Credit ratings " + creditRatings + " do not contain matches for " + _perAgencyRatings);
    }
    return selections;
  }

  public static class Builder {
    private boolean _useLongTerm;
    private boolean _rating;
    private final Set<String> _ratingsToUse;
    private boolean _ratingDescription;
    private final Set<String> _ratingDescriptionsToUse;
    private boolean _useTerm;

    protected Builder() {
      _ratingsToUse = new HashSet<>();
      _ratingDescriptionsToUse = new HashSet<>();
    }

    public Builder useRatings() {
      _rating = true;
      return this;
    }

    //TODO add above and below logic - will need a comparator for the rating strings

    public Builder useRatingForAgency(final String agencyName) {
      _rating = true;
      _ratingsToUse.add(agencyName);
      return this;
    }

    //TODO add above and below logic - will need a comparator for the rating descriptions
    public Builder useRatingDescriptions() {
      _ratingDescription = true;
      return this;
    }

    public Builder useRatingDescriptionForAgency(final String agencyName) {
      _ratingDescription = true;
      _ratingDescriptionsToUse.add(agencyName);
      return this;
    }

    public Builder useTerm(final boolean useLongTerm) {
      _useTerm = true;
      _useLongTerm = useLongTerm;
      return this;
    }

    //TODO work out how to use the term of the rating.
    public LegalEntityCreditRatings create() {
      return new LegalEntityCreditRatings(_rating, _ratingsToUse, _ratingDescription, _ratingDescriptionsToUse);
    }
  }
}
