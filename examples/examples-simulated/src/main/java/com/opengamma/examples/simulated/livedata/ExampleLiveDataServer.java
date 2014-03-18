/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.livedata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.ehcache.CacheManager;

import org.apache.commons.io.IOUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An ultra-simple market data simulator, we load the initial values from a CSV file (with a header row)
 * and the format
 * <identification-scheme>, <identifier-value>, <requirement-name>, <value>
 * typically, for last price, you'd use "Market_Value" @see MarketDataRequirementNames
 */
public class ExampleLiveDataServer extends StandardLiveDataServer {

  private static final Logger s_logger = LoggerFactory.getLogger(ExampleLiveDataServer.class);

  private static final FudgeContext s_fudgeConext = OpenGammaFudgeContext.getInstance();
  private static final int NUM_FIELDS = 3;
  /**
   * Default scaling factor
   */
  public static final double SCALING_FACTOR = 0.005; // i.e. 0.5% * 1SD
  /**
   * Default max millis between ticks
   */
  public static final int MAX_MILLIS_BETWEEN_TICKS = 500;

  private final Map<String, FudgeMsg> _marketValues = Maps.newConcurrentMap();
  private final Map<String, FudgeMsg> _baseValues = Maps.newConcurrentMap();
  private volatile double _scalingFactor;
  private volatile int _maxMillisBetweenTicks;
  private final TerminatableJob _marketDataSimulatorJob = new SimulatedMarketDataJob();
  private final ExecutorService _executorService = NamedThreadPoolFactory.newCachedThreadPool("ExampleLiveDataServer");

  public ExampleLiveDataServer(final CacheManager cacheManager, final Resource initialValuesFile) {
    this(cacheManager, initialValuesFile, SCALING_FACTOR, MAX_MILLIS_BETWEEN_TICKS);
  }

  public ExampleLiveDataServer(final CacheManager cacheManager, final Resource initialValuesFile, final double scalingFactor, final int maxMillisBetweenTicks) {
    super(cacheManager);
    readInitialValues(initialValuesFile);
    _scalingFactor = scalingFactor;
    _maxMillisBetweenTicks = maxMillisBetweenTicks;
  }

  //-------------------------------------------------------------------------
  private void readInitialValues(final Resource initialValuesFile) {
    CSVReader reader = null;
    try {
      reader = new CSVReader(new BufferedReader(new InputStreamReader(initialValuesFile.getInputStream())));
      // Read header row
      @SuppressWarnings("unused")
      final String[] headers = reader.readNext();
      String[] line;
      int lineNum = 1;
      while ((line = reader.readNext()) != null) {
        lineNum++;
        if (line.length > 0 && line[0].startsWith("#")) {
          continue;
        }
        if (line.length != NUM_FIELDS) {
          s_logger.error("Not enough fields in CSV on line " + lineNum);
        } else {
          final String identifier = line[0];
          final String fieldName = line[1];
          final String valueStr = line[2];
          final Double value = Double.parseDouble(valueStr);
          addTicks(identifier, fieldName, value);
        }
      }
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * Gets the marketValues.
   * @return the marketValues
   */
  public Map<String, FudgeMsg> getMarketValues() {
    return ImmutableMap.copyOf(_marketValues);
  }

  /**
   * Gets the scalingFactor.
   * @return the scalingFactor
   */
  public double getScalingFactor() {
    return _scalingFactor;
  }

  /**
   * Sets the scalingFactor.
   * @param scalingFactor  the scalingFactor
   */
  public void setScalingFactor(final double scalingFactor) {
    _scalingFactor = scalingFactor;
  }

  /**
   * Gets the maxMillisBetweenTicks.
   * @return the maxMillisBetweenTicks
   */
  public int getMaxMillisBetweenTicks() {
    return _maxMillisBetweenTicks;
  }

  /**
   * Sets the maxMillisBetweenTicks.
   * @param maxMillisBetweenTicks  the maxMillisBetweenTicks
   */
  public void setMaxMillisBetweenTicks(final int maxMillisBetweenTicks) {
    _maxMillisBetweenTicks = maxMillisBetweenTicks;
  }

  /**
   * @param uniqueId the uniqueId, not null
   * @param fieldName the field name, not null
   * @param value the market value, not null
   */
  public void addTicks(final String uniqueId, final String fieldName, final Double value) {
    ArgumentChecker.notNull(uniqueId, "unique identifier");
    ArgumentChecker.notNull(fieldName, "field name");
    ArgumentChecker.notNull(value, "market value");

    final FudgeMsg previousTicks = _marketValues.get(uniqueId);
    MutableFudgeMsg ticks = null;
    if (previousTicks == null) {
      ticks = s_fudgeConext.newMessage();
    } else {
      ticks = s_fudgeConext.newMessage(previousTicks);
      if (ticks.hasField(fieldName)) {
        ticks.remove(fieldName);
      }
    }
    ticks.add(fieldName, value);
    _marketValues.put(uniqueId, ticks);
    _baseValues.put(uniqueId, ticks);
  }

  @Override
  protected Map<String, Object> doSubscribe(final Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Subscriptions");
    s_logger.debug("doSubscribe on {}", uniqueIds);

    final Set<String> requestSubcriptions = Sets.newTreeSet(uniqueIds);
    final Set<String> validSubscriptions = Maps.filterKeys(_marketValues, Predicates.in(requestSubcriptions)).keySet();
    Map<String, Object> subscriptionHandles = Maps.toMap(validSubscriptions, new Function<String, Object>() {
      @Override
      public Object apply(String uniqueId) {
        return new AtomicReference<String>(uniqueId);
      }
    });

    requestSubcriptions.removeAll(validSubscriptions);
    if (!requestSubcriptions.isEmpty()) {
      s_logger.warn("Could not subscribe for {}", requestSubcriptions);
    }
    return subscriptionHandles;
  }

  @Override
  protected void doUnsubscribe(final Collection<Object> subscriptionHandles) {
    s_logger.debug("doUnsubscribe on {}", subscriptionHandles);
    // No-op; don't maintain or forward any subscription state
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(final Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Unique IDs");
    s_logger.debug("doSnapshot on {}", uniqueIds);
    return new HashMap<>(Maps.filterKeys(_marketValues, Predicates.in(uniqueIds)));
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return ExternalSchemes.OG_SYNTHETIC_TICKER;
  }

  @Override
  protected void doConnect() {
    s_logger.info("ExampleLiveDataServer connecting..");
    _executorService.submit(_marketDataSimulatorJob);
  }

  @Override
  protected void doDisconnect() {
    if (_executorService != null) {
      _executorService.shutdownNow();
      try {
        _executorService.awaitTermination(1, TimeUnit.SECONDS);
      } catch (final InterruptedException ex) {
        Thread.interrupted();
      }
    }
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(final Subscription subscription) {
    return true;
  }

  private class SimulatedMarketDataJob extends TerminatableJob {

    private final Random _random = new Random();

    private double wiggleValue(final double value, final double centre) {
      return (9 * value + centre) / 10 + (_random.nextGaussian() * (value * _scalingFactor));
    }

    @Override
    protected void runOneCycle() {
      final Set<String> activeSubscriptionIds = getActiveSubscriptionIds();
      if (!activeSubscriptionIds.isEmpty()) {
        for (final String identifier : activeSubscriptionIds) {
          final FudgeMsg lastValues = _marketValues.get(identifier);
          final FudgeMsg baseValues = _baseValues.get(identifier);
          if (lastValues != null && baseValues != null) {
            final MutableFudgeMsg nextValues = s_fudgeConext.newMessage();
            for (final FudgeField field : lastValues) {
              final double lastValue = (Double) field.getValue();
              final double baseValue = baseValues.getDouble(field.getName());
              final double value = wiggleValue(lastValue, baseValue);
              nextValues.add(field.getName(), value);
            }
            _marketValues.put(identifier, nextValues);
            liveDataReceived(identifier, nextValues);
            s_logger.debug("{} lastValues: {} nextValues: {}", identifier, lastValues, nextValues);
          } else {
            s_logger.error("Active subscription for {} is missing in example market data server initial database", identifier);
          }
        }
      }
      try {
        Thread.sleep(_random.nextInt(_maxMillisBetweenTicks));
      } catch (final InterruptedException e) {
        s_logger.error("Sleep interrupted, finishing");
        Thread.interrupted();
      }
    }
  }
}
