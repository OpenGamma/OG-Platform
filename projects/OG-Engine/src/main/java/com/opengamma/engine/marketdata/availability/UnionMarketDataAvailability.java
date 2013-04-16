/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.BlockingOperation;

/**
 * Indicates that market data is available if any of the underlyings claim that it is. If none of the underlying claim availability, but at least one throws a {@link MarketDataNotSatisfiableException}
 * the market data is considered missing. Otherwise it is not available.
 * 
 * @param <T> the component type
 */
public abstract class UnionMarketDataAvailability<T> {

  private final Collection<? extends T> _underlyings;

  /**
   * 
   */
  public static class Filter extends UnionMarketDataAvailability<MarketDataAvailabilityFilter> implements MarketDataAvailabilityFilter {

    public Filter(final Collection<? extends MarketDataAvailabilityFilter> underlyings) {
      super(underlyings);
    }

    @Override
    protected Object getAvailabilityImpl(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue, final MarketDataAvailabilityFilter underlying) {
      return underlying.isAvailable(targetSpec, target, desiredValue) ? Boolean.TRUE : null;
    }

    @Override
    public boolean isAvailable(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
      return getAvailabilityImpl(targetSpec, target, desiredValue) != null;
    }

    @Override
    public MarketDataAvailabilityProvider withProvider(MarketDataAvailabilityProvider provider) {
      provider = AbstractMarketDataAvailabilityProvider.of(provider);
      final List<MarketDataAvailabilityProvider> union = new ArrayList<MarketDataAvailabilityProvider>();
      for (final MarketDataAvailabilityFilter underlying : getUnderlyings()) {
        union.add(underlying.withProvider(provider));
      }
      return new Provider(union);
    }

  }

  /**
   * 
   */
  public static class Provider extends UnionMarketDataAvailability<MarketDataAvailabilityProvider> implements MarketDataAvailabilityProvider {

    public Provider(final Collection<? extends MarketDataAvailabilityProvider> underlyings) {
      super(underlyings);
    }

    @Override
    protected Object getAvailabilityImpl(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue, final MarketDataAvailabilityProvider underlying) {
      return underlying.getAvailability(targetSpec, target, desiredValue);
    }

    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
      return (ValueSpecification) getAvailabilityImpl(targetSpec, target, desiredValue);
    }

    @Override
    public MarketDataAvailabilityFilter getAvailabilityFilter() {
      final List<MarketDataAvailabilityFilter> union = new ArrayList<MarketDataAvailabilityFilter>();
      for (final MarketDataAvailabilityProvider underlying : getUnderlyings()) {
        union.add(underlying.getAvailabilityFilter());
      }
      return new Filter(union);
    }

    @Override
    public Serializable getAvailabilityHintKey() {
      final ArrayList<Serializable> key = new ArrayList<Serializable>(getUnderlyings().size());
      for (MarketDataAvailabilityProvider underlying : getUnderlyings()) {
        key.add(underlying.getAvailabilityHintKey());
      }
      return key;
    }

  }

  protected UnionMarketDataAvailability(final Collection<? extends T> underlyings) {
    _underlyings = underlyings;
  }

  protected Collection<? extends T> getUnderlyings() {
    return _underlyings;
  }

  protected abstract Object getAvailabilityImpl(ComputationTargetSpecification targetSpec, Object target, ValueRequirement desiredValue, T underlying);

  protected Object getAvailabilityImpl(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    MarketDataNotSatisfiableException missing = null;
    boolean failed = false;
    for (final T underlying : getUnderlyings()) {
      try {
        final Object v = getAvailabilityImpl(targetSpec, target, desiredValue, underlying);
        if (v != null) {
          return v;
        }
      } catch (final BlockingOperation e) {
        failed = true;
      } catch (final MarketDataNotSatisfiableException e) {
        missing = e;
      }
    }
    if (failed) {
      // Blocking mode is off, nothing declared AVAILABLE, and at least one wanted to block
      throw BlockingOperation.block();
    } else {
      if (missing != null) {
        throw missing;
      } else {
        return null;
      }
    }
  }

}
