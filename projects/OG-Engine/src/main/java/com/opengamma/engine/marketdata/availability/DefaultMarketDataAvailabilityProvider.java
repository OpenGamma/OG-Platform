/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * An implementation of {@link MarketDataAvailabilityProvider} that produces arbitrary value specifications for all values.
 * <p>
 * This is provided for use in test cases only, and would normally be used as the provider passed to an existing {@link MarketDataAvailabilityFilter}. A market data provider would normally require
 * more control over the specifications issued in order to manage later subscriptions to the values.
 */
public class DefaultMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  private static IdentifierResolver s_identifiers = (IdentifierResolver) ComputationTargetType.PRIMITIVE;

  /**
   * Creates a default computation target specification for an object that is either {@link ExternalBundleIdentifiable} or {@link ExternalIdentifiable} using the
   * {@link ComputationTargetType#PRIMITIVE} logic.
   * <p>
   * This is suitable for cases where the market data provider can recognize the target object identifiers, but the system could not resolve it to a strict specification (for example it is for a
   * ticker describing a security that is not present in the security master).
   * 
   * @param target the object to create a specification for, not null
   * @return a specification, not null
   * @throws IllegalArgumentException if the target is not a suitable type
   */
  public static ComputationTargetSpecification createPrimitiveComputationTargetSpecification(final Object target) {
    final UniqueId uid;
    if (target instanceof ExternalBundleIdentifiable) {
      // VersionCorrection.LATEST is correct for this - the primitive identifier resolution hacking is independent of the resolution time
      uid = s_identifiers.resolveExternalId(((ExternalBundleIdentifiable) target).getExternalIdBundle(), VersionCorrection.LATEST);
    } else if (target instanceof ExternalIdentifiable) {
      // VersionCorrection.LATEST is correct for this - the primitive identifier resolution hacking is independent of the resolution time
      uid = s_identifiers.resolveExternalId(((ExternalIdentifiable) target).getExternalId().toBundle(), VersionCorrection.LATEST);
    } else {
      throw new IllegalArgumentException(target.toString());
    }
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, uid);
  }

  @Override
  public ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
    if (targetSpec == null) {
      targetSpec = createPrimitiveComputationTargetSpecification(target);
    }
    return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, MarketDataSourcingFunction.UNIQUE_ID).get());
  }

  @Override
  public MarketDataAvailabilityFilter getAvailabilityFilter() {
    return new ProviderMarketDataAvailabilityFilter(this);
  }

  @Override
  public Serializable getAvailabilityHintKey() {
    return getClass();
  }

}
