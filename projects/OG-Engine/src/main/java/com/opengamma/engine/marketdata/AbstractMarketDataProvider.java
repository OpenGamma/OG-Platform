/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Abstract base class for {@link MarketDataProvider} implementations.
 */
public abstract class AbstractMarketDataProvider implements MarketDataProvider {

  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();

  @Override
  public void addListener(final MarketDataListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeListener(final MarketDataListener listener) {
    _listeners.remove(listener);
  }

  @Override
  public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
    return Duration.between(fromInstant, toInstant);
  }

  //-------------------------------------------------------------------------
  protected void valueChanged(final ValueSpecification specification) {
    valuesChanged(Collections.singleton(specification));
  }

  protected void valuesChanged(final Collection<ValueSpecification> specifications) {
    for (final MarketDataListener listener : getListeners()) {
      listener.valuesChanged(specifications);
    }
  }

  protected void subscriptionSucceeded(final ValueSpecification specification) {
    subscriptionsSucceeded(Collections.singleton(specification));
  }

  protected void subscriptionsSucceeded(final Collection<ValueSpecification> specifications) {
    for (final MarketDataListener listener : getListeners()) {
      listener.subscriptionsSucceeded(specifications);
    }
  }

  protected void subscriptionFailed(final ValueSpecification specification, final String msg) {
    for (final MarketDataListener listener : getListeners()) {
      listener.subscriptionFailed(specification, msg);
    }
  }

  protected void subscriptionFailed(final Collection<ValueSpecification> specifications, final String msg) {
    for (final ValueSpecification specification : specifications) {
      subscriptionFailed(specification, msg);
    }
  }

  protected void subscriptionStopped(final ValueSpecification specification) {
    for (final MarketDataListener listener : getListeners()) {
      listener.subscriptionStopped(specification);
    }
  }

  protected void subscriptionStopped(final Collection<ValueSpecification> specifications) {
    for (final ValueSpecification specification : specifications) {
      subscriptionStopped(specification);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * @return Collection will be unmodifiable. Iterating over it will not throw {link ConcurrentModificationException}.
   */
  protected Collection<MarketDataListener> getListeners() {
    return Collections.unmodifiableCollection(_listeners);
  }

}
