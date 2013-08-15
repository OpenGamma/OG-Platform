/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.RandomizingMarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class RandomizingMarketDataProvider extends AbstractMarketDataProvider {

  private static final Timer s_timer = new Timer();

  private final MarketDataProvider _underlying;
  private final RandomizingMarketDataSpecification _marketDataSpec;

  private volatile RandomizingMarketDataSnapshot _snapshot;
  private volatile Set<ValueSpecification> _specsToTick = Collections.emptySet();

  public RandomizingMarketDataProvider(RandomizingMarketDataSpecification spec, MarketDataProvider underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(spec, "spec");
    _marketDataSpec = spec;
    _underlying = underlying;
    _underlying.addListener(new Listener());
  }

  @Override
  public void addListener(MarketDataListener listener) {
    _underlying.addListener(listener);
  }

  @Override
  public void removeListener(MarketDataListener listener) {
    _underlying.removeListener(listener);
  }

  @Override
  public void subscribe(ValueSpecification valueSpecification) {
    _underlying.subscribe(valueSpecification);
  }

  @Override
  public void subscribe(Set<ValueSpecification> valueSpecifications) {
    _underlying.subscribe(valueSpecifications);
  }

  @Override
  public void unsubscribe(ValueSpecification valueSpecification) {
    _underlying.unsubscribe(valueSpecification);
  }

  @Override
  public void unsubscribe(Set<ValueSpecification> valueSpecifications) {
    _underlying.unsubscribe(valueSpecifications);
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(MarketDataSpecification marketDataSpec) {
    return _underlying.getAvailabilityProvider(marketDataSpec);
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _underlying.getPermissionProvider();
  }

  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    return _marketDataSpec.equals(marketDataSpec);
  }

  @Override
  public RandomizingMarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    if (_snapshot == null) {
      s_timer.schedule(new RandomizingTask(), _marketDataSpec.getAverageCyclePeriod());
    }
    _snapshot = new RandomizingMarketDataSnapshot(_underlying.snapshot(marketDataSpec), _marketDataSpec, _specsToTick);
    return _snapshot;
  }

  @Override
  public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
    return _underlying.getRealTimeDuration(fromInstant, toInstant);
  }

  private class Listener implements MarketDataListener {

    @Override
    public void subscriptionsSucceeded(Collection<ValueSpecification> specifications) {
      RandomizingMarketDataProvider.this.subscriptionsSucceeded(specifications);
    }

    @Override
    public void subscriptionFailed(ValueSpecification specification, String msg) {
      RandomizingMarketDataProvider.this.subscriptionFailed(specification, msg);
    }

    @Override
    public void subscriptionStopped(ValueSpecification specification) {
      RandomizingMarketDataProvider.this.subscriptionStopped(specification);
    }

    @Override
    public void valuesChanged(Collection<ValueSpecification> specifications) {
      RandomizingMarketDataProvider.this.valuesChanged(specifications);
    }
  }

  private class RandomizingTask extends TimerTask {

    @Override
    public void run() {
      if (_snapshot != null) {
        Set<ValueSpecification> specs = _snapshot.getSpecifications();
        Set<ValueSpecification> specsToTick = Sets.newHashSet();
        for (ValueSpecification spec : specs) {
          if (_marketDataSpec.getUpdateProbability() > Math.random()) {
            specsToTick.add(spec);
          }
        }
        _specsToTick = specsToTick;
        valuesChanged(specs);
      }
      // reschedule this task after a random interval which is the average +/-50%
      s_timer.schedule(this, ((long) (_marketDataSpec.getAverageCyclePeriod() * (0.5 + Math.random()))));
    }
  }
}
