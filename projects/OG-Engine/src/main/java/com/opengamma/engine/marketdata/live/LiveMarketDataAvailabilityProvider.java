/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.availability.AbstractMarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Market data availability delegate that can perform conversion to/from the {@link LiveDataSpecification} objects needed by the data provider.
 */
/* package */class LiveMarketDataAvailabilityProvider extends AbstractMarketDataAvailabilityProvider {

  private static final String IDENTIFIER_PROPERTY = "Id";

  /**
   * All specifications will use the same normalization rule set.
   */
  private final ComputationTargetSpecification _target;

  /**
   * Constructs a new instance.
   * 
   * @param normalizationRules the normalization rules to use
   */
  public LiveMarketDataAvailabilityProvider(final String normalizationRules) {
    ArgumentChecker.notNull(normalizationRules, "normalizationRules");
    _target = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Data", normalizationRules));
  }

  protected ComputationTargetSpecification getTarget() {
    // [PLAT-3044] This is wrong if snapshots are to work; the target must be resolvable to at least the identifiers that produced the specification. It would be better to use
    // the supplied target specification and encode the normalization as a property.
    return _target;
  }

  protected String getSyntheticFunctionName() {
    return "LiveMarketData";
  }

  /**
   * Creates a live data specification based on a {@link ValueSpecification} created by this availability provider. The normalization ruleset is used as the target. The external identifiers are marked
   * with properties.
   * 
   * @param properties the properties, not null
   * @return the original live data specification
   */
  public static LiveDataSpecification getLiveDataSpecification(final ValueSpecification valueSpec) {
    final String normalizer = valueSpec.getTargetSpecification().getUniqueId().getValue();
    final Set<String> identifierStrings = valueSpec.getProperties().getValues(IDENTIFIER_PROPERTY);
    final ExternalId[] identifiers = new ExternalId[identifierStrings.size()];
    int i = 0;
    for (final String identifierString : identifierStrings) {
      identifiers[i++] = ExternalId.parse(identifierString);
    }
    return new LiveDataSpecification(normalizer, identifiers);
  }

  protected ValueProperties.Builder createValueProperties() {
    return ValueProperties.with(ValuePropertyNames.FUNCTION, getSyntheticFunctionName());
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    return new ValueSpecification(desiredValue.getValueName(), getTarget(), createValueProperties().with(IDENTIFIER_PROPERTY, identifier.toString()).get());
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
    final String[] identifierStrings = new String[identifiers.size()];
    int i = 0;
    for (final ExternalId identifier : identifiers) {
      identifierStrings[i++] = identifier.toString();
    }
    return new ValueSpecification(desiredValue.getValueName(), getTarget(), createValueProperties().with(IDENTIFIER_PROPERTY, identifierStrings).get());
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    // Can't provide any live data unless there is an external identifier recognized by the data provider
    return null;
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
    // Can't provide any live data for the NULL target
    return null;
  }

}
