/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * A partial implementation of {@link MarketDataAvailabilityProvider} which can be coupled with a {@link MarketDataProvider} to provide resolution of value specifications suitable for establishing
 * subscriptions.
 */
public abstract class AbstractMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  /**
   * A {@link MarketDataAvailabilityProvider} coupled to a copy of an {@link AbstractMarketDataAvailabilityProvider} allowing the market data provider to override the behaviors which construct the
   * resulting value specifications.
   */
  public abstract static class Delegate implements MarketDataAvailabilityProvider {

    private final MarketDataAvailabilityProvider _underlying;

    public Delegate(final AbstractMarketDataAvailabilityProvider underlying) {
      _underlying = underlying.withDelegate(this);
    }

    private Delegate() {
      _underlying = null;
    }

    protected abstract ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, ExternalId identifier, ValueRequirement desiredValue);

    protected abstract ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, ExternalIdBundle identifiers, ValueRequirement desiredValue);

    protected abstract ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, UniqueId identifier, ValueRequirement desiredValue);

    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
      return _underlying.getAvailability(targetSpec, target, desiredValue);
    }

  }

  private final Delegate _delegate;

  protected AbstractMarketDataAvailabilityProvider(final Delegate delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  protected AbstractMarketDataAvailabilityProvider() {
    _delegate = new Delegate() {

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
        return null;
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
        return null;
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
        return null;
      }

    };
  }

  protected abstract MarketDataAvailabilityProvider withDelegate(Delegate delegate);

  /**
   * Resolves the availability of an item that can be referenced by external identifier.
   *
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifier the external identifier of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    return _delegate.getAvailability(targetSpec, identifier, desiredValue);
  }

  /**
   * Resolves the availability of an item that can be referenced by one or more external identifiers.
   *
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifiers the external identifiers of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
    return _delegate.getAvailability(targetSpec, identifiers, desiredValue);
  }

  /**
   * Resolves the availability of an item that can only be referenced by unique identifier
   *
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifier the unique identifier of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    return _delegate.getAvailability(targetSpec, identifier, desiredValue);
  }

  /**
   * Tests how the target can be referenced and defers to one of the other {@code getAvailability} methods.
   *
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param target the target to evaluate, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return the resolved subscription value if available, null otherwise
   */
  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    if (target instanceof ExternalBundleIdentifiable) {
      return getAvailability(targetSpec, ((ExternalBundleIdentifiable) target).getExternalIdBundle(), desiredValue);
    } else if (target instanceof ExternalIdentifiable) {
      return getAvailability(targetSpec, ((ExternalIdentifiable) target).getExternalId(), desiredValue);
    } else if (target instanceof UniqueIdentifiable) {
      return getAvailability(targetSpec, ((UniqueIdentifiable) target).getUniqueId(), desiredValue);
    } else {
      return null;
    }
  }

}
