/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * A partial implementation of {@link MarketDataAvailabilityProvider}.
 */
public abstract class AbstractMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  /**
   * Resolves the availability of an item that can be referenced by external identifier.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifier the external identifier of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected abstract ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue);

  /**
   * Resolves the availability of an item that can be referenced by one or more external identifiers.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifiers the external identifiers of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected abstract ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue);

  /**
   * Resolves the availability of an item that can only be referenced by unique identifier
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifier the unique identifier of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected abstract ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue);

  /**
   * Resolves the availability of the null target.
   * 
   * @param targetSpec the target specification, always {@link ComputationTargetSpecification#NULL}
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected abstract ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue);

  /**
   * Tests how the target can be referenced and defers to one of the other {@code getAvailability} methods.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param target the target to evaluate, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  @Override
  public final ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    if (target instanceof ExternalBundleIdentifiable) {
      final ValueSpecification availability = getAvailability(targetSpec, ((ExternalBundleIdentifiable) target).getExternalIdBundle(), desiredValue);
      if (availability != null) {
        return availability;
      }
    }
    if (target instanceof ExternalIdentifiable) {
      final ValueSpecification availability = getAvailability(targetSpec, ((ExternalIdentifiable) target).getExternalId(), desiredValue);
      if (availability != null) {
        return availability;
      }
    }
    if (target instanceof UniqueIdentifiable) {
      final ValueSpecification availability = getAvailability(targetSpec, ((UniqueIdentifiable) target).getUniqueId(), desiredValue);
      if (availability != null) {
        return availability;
      }
    }
    if (target == null) {
      return getAvailability(targetSpec, desiredValue);
    } else {
      return null;
    }
  }

  @Override
  public MarketDataAvailabilityFilter getAvailabilityFilter() {
    return new ProviderMarketDataAvailabilityFilter(this);
  }

  public static AbstractMarketDataAvailabilityProvider of(final MarketDataAvailabilityProvider provider) {
    if (provider instanceof AbstractMarketDataAvailabilityProvider) {
      return (AbstractMarketDataAvailabilityProvider) provider;
    } else {
      return new AbstractMarketDataAvailabilityProvider() {

        @Override
        public MarketDataAvailabilityFilter getAvailabilityFilter() {
          return provider.getAvailabilityFilter();
        }

        @Override
        protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
          return provider.getAvailability(targetSpec, identifier, desiredValue);
        }

        @Override
        protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
          return provider.getAvailability(targetSpec, identifiers, desiredValue);
        }

        @Override
        protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
          return provider.getAvailability(targetSpec, identifier, desiredValue);
        }

        @Override
        protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
          return provider.getAvailability(targetSpec, null, desiredValue);
        }

        @Override
        public Serializable getAvailabilityHintKey() {
          return provider.getAvailabilityHintKey();
        }

      };
    }
  }

}
