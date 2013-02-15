/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Lookup to convert live data specifications to/from value specifications.
 */
/* package */class LiveDataSpecificationLookup {

  private static final String NORMALIZER_PROPERTY = "Normalizer";

  private static final String IDENTIFIER_PROPERTY = "Id";

  private final String _normalizationRules;

  /**
   * Constructs a new instance.
   * 
   * @param normalizationRules the normalization rules to use
   */
  public LiveDataSpecificationLookup(final String normalizationRules) {
    ArgumentChecker.notNull(normalizationRules, "normalizationRules");
    _normalizationRules = normalizationRules;
  }

  protected String getNormalizationRules() {
    return _normalizationRules;
  }

  /**
   * Creates a live data specification corresponding to the given data identifier.
   * 
   * @param identifier a market data identifier, not null
   * @return the live data specification, not null
   */
  public LiveDataSpecification createLiveDataSpecification(final ExternalId identifier) {
    return new LiveDataSpecification(getNormalizationRules(), identifier.toBundle());
  }

  /**
   * Creates a live data specification corresponding to the given data identifiers.
   * 
   * @param identifiers the market data identifiers, not null
   * @return the live data specification, not null
   */
  public LiveDataSpecification createLiveDataSpecification(final ExternalIdBundle identifiers) {
    return new LiveDataSpecification(getNormalizationRules(), identifiers);
  }

  /**
   * Creates a live data specification based on a value specification returned by {@link #getValueSpecification}.
   * 
   * @param valueSpec the value specification, not null
   * @return the original live data specification
   */
  public static LiveDataSpecification getLiveDataSpecification(final ValueSpecification valueSpec) {
    final String normalizer = valueSpec.getProperty(NORMALIZER_PROPERTY);
    final Set<String> identifierStrings = valueSpec.getProperties().getValues(IDENTIFIER_PROPERTY);
    final ExternalId[] identifiers = new ExternalId[identifierStrings.size()];
    int i = 0;
    for (final String identifierString : identifierStrings) {
      identifiers[i++] = ExternalId.parse(identifierString);
    }
    // TODO: What are we doing with the value name - I think we've got this wrong
    return new LiveDataSpecification(normalizer, identifiers);
  }

  // [PLAT-3044] Both of these are wrong; conversion is specific to a live data provider; the delegate pattern should be used

  /**
   * Creates a value specification corresponding to the give live data specification.
   * 
   * @param liveDataSpec the live data specification, not null
   * @return the equivalent value specification that can be passed to {@link #getLiveDataSpecification}
   */
  public static ValueSpecification getValueSpecification(final LiveDataSpecification liveDataSpec) {
    final ValueProperties.Builder builder = ValueProperties.with(ValuePropertyNames.FUNCTION, "LiveData").with(NORMALIZER_PROPERTY, liveDataSpec.getNormalizationRuleSetId());
    for (final ExternalId identifier : liveDataSpec.getIdentifiers()) {
      builder.with(IDENTIFIER_PROPERTY, identifier.toString());
    }
    // TODO: Where do we get the value name from? Is that composed somewhere else?
    throw new UnsupportedOperationException("[PLAT-3044] Where do we get the value name from?");
  }

}
