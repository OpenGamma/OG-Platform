/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;
import java.util.ArrayList;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link MarketDataAvailabilityFilter} based on a {@link MarketDataAvailabilityProvider}
 * <p>
 * A filter would normally be created from a provider if necessary by calling its {@link MarketDataAvailabilityProvider#getAvailabilityFilter()} method which may offer a more efficient conversion than
 * constructing an instance of this class. This is provided to assist in the implementation of that method.
 */
public class ProviderMarketDataAvailabilityFilter implements MarketDataAvailabilityFilter {

  private final MarketDataAvailabilityProvider _provider;

  public ProviderMarketDataAvailabilityFilter(final MarketDataAvailabilityProvider provider) {
    ArgumentChecker.notNull(provider, "provider");
    _provider = provider;
  }

  protected MarketDataAvailabilityProvider getProvider() {
    return _provider;
  }

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    return getProvider().getAvailability(targetSpec, target, desiredValue) != null;
  }

  @Override
  public MarketDataAvailabilityProvider withProvider(final MarketDataAvailabilityProvider provider) {
    if (getProvider() == provider) {
      return provider;
    } else {
      return new MarketDataAvailabilityProvider() {

        @Override
        public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
          if (isAvailable(targetSpec, target, desiredValue)) {
            return provider.getAvailability(targetSpec, target, desiredValue);
          } else {
            return null;
          }
        }

        @Override
        public MarketDataAvailabilityFilter getAvailabilityFilter() {
          return ProviderMarketDataAvailabilityFilter.this;
        }

        @Override
        public Serializable getAvailabilityHintKey() {
          final ArrayList<Serializable> key = new ArrayList<Serializable>(2);
          key.add(getProvider().getAvailabilityHintKey());
          key.add(provider.getAvailabilityHintKey());
          return key;
        }

      };
    }
  }

}
