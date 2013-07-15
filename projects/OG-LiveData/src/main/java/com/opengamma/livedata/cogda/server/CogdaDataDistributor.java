/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.LiveDataValueUpdateBeanFudgeBuilder;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.livedata.server.LastKnownValueStore;
import com.opengamma.livedata.server.LastKnownValueStoreProvider;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.metric.MetricProducer;

/**
 * Listens to a channel of raw data updates, normalizes, writes to the LKV store, and then
 * publishes all value updates (for the normalized results) to a different channel
 * for detection by the data servers.
 * <p/>
 * It has three ways that the list of active subscriptions can be built:
 * <ol>
 *   <li>You can just wait for updates to come through. Whenever an update is received
 *       where there is not a distribution order, one will be built.</li>
 *   <li>By scanning the LKV store on startup.</li>
 *   <li>You can explicitly add them (perhaps via startup configuration)
 *       via calls to {@link #addDistribution(String)}.</li>
 * </ol>
 * <p/>
 * In general, if not bootstrapping for the first time, the first and second ways should be
 * sufficient.
 */
public abstract class CogdaDataDistributor implements Lifecycle, MetricProducer {
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaDataDistributor.class);
  // Constructor injectors:
  private final String _externalIdScheme;
  private final LastKnownValueStoreProvider _lastKnownValueStoreProvider;
  private final Map<String, NormalizationRuleSet> _normalization;
  
  // TODO kirk 2012-08-13 -- Have support for multiple senders, one per normalization
  // rule. Otherwise there's way too much filtering. But that's a second-order effect.
  private FudgeMessageSender _normalizedMessageSender;
  
  // Internal state:
  private final ConcurrentMap<LiveDataSpecification, LastKnownValueStore> _valueStores =
      new ConcurrentHashMap<LiveDataSpecification, LastKnownValueStore>();
  private final ConcurrentMap<LiveDataSpecification, FieldHistoryStore> _normalizationState =
      new ConcurrentHashMap<LiveDataSpecification, FieldHistoryStore>();
  
  // Metrics:
  private Meter _tickMeter = new Meter();
  
  public CogdaDataDistributor(
      String externalIdScheme,
      LastKnownValueStoreProvider lastKnownValueStoreProvider,
      String... normalizationSchemes) {
    ArgumentChecker.notNull(externalIdScheme, "externalIdScheme");
    ArgumentChecker.notNull(lastKnownValueStoreProvider, "lastKnownValueStoreProvider");
    
    _externalIdScheme = externalIdScheme;
    _lastKnownValueStoreProvider = lastKnownValueStoreProvider;
    _normalization = Collections.unmodifiableMap(constructNormalizationRules(normalizationSchemes));
  }
  
  @Override
  public synchronized void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    _tickMeter = summaryRegistry.meter(namePrefix + ".ticks");
  }

  /**
   * Gets the externalIdScheme.
   * @return the externalIdScheme
   */
  public String getExternalIdScheme() {
    return _externalIdScheme;
  }

  /**
   * Gets the normalizedMessageSender.
   * @return the normalizedMessageSender
   */
  public FudgeMessageSender getNormalizedMessageSender() {
    return _normalizedMessageSender;
  }

  /**
   * Sets the normalizedMessageSender.
   * @param normalizedMessageSender  the normalizedMessageSender
   */
  public void setNormalizedMessageSender(FudgeMessageSender normalizedMessageSender) {
    _normalizedMessageSender = normalizedMessageSender;
  }

  /**
   * @param normalizationSchemes
   * @return
   */
  private Map<String, NormalizationRuleSet> constructNormalizationRules(String[] normalizationSchemes) {
    Map<String, NormalizationRuleSet> normalization = new TreeMap<String, NormalizationRuleSet>();
    for (String normalizationScheme : normalizationSchemes) {
      normalization.put(normalizationScheme, constructNormalizationRuleSet(normalizationScheme));
    }
    return normalization;
  }

  /**
   * @param normalizationScheme name of the scheme to be generated.
   * @return                    the rule set for that scheme.
   */
  protected abstract NormalizationRuleSet constructNormalizationRuleSet(String normalizationScheme);

  public void addDistribution(String uniqueIdentifier) {
    for (String normalizationScheme : _normalization.keySet()) {
      ensureLastKnownValueStore(ExternalId.of(_externalIdScheme, uniqueIdentifier), normalizationScheme);
    }
    
  }

  @Override
  public void start() {
    scanAllKeys();
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isRunning() {
    return false;
  }

  /**
   * 
   */
  protected void scanAllKeys() {
    Set<String> allIdentifiers = null;
    try {
      allIdentifiers = _lastKnownValueStoreProvider.getAllIdentifiers(_externalIdScheme);
    } catch (UnsupportedOperationException uoe) {
      return;
    }
    
    for (String id: allIdentifiers) {
      for (String normalizationScheme : _normalization.keySet()) {
        ensureLastKnownValueStore(ExternalId.of(_externalIdScheme, id), normalizationScheme);
      }
    }
  }
  
  /**
   * Prepare the LKV store for the given specification and populate the normalization
   * state.
   * 
   * @param id identifier for which to create store
   * @param normalizationScheme normalization scheme of store
   * @return The value store
   */
  protected LastKnownValueStore ensureLastKnownValueStore(ExternalId id, String normalizationScheme) {
    LastKnownValueStore lkvStore = _lastKnownValueStoreProvider.newInstance(id, normalizationScheme);
    LiveDataSpecification ldspec = new LiveDataSpecification(normalizationScheme, id);
    if (_valueStores.putIfAbsent(ldspec, lkvStore) == null) {
      s_logger.debug("Created new LKV store and history state for {}", ldspec);
      // We actually did the creation. Also create the field history map.
      FieldHistoryStore historyStore = new FieldHistoryStore(lkvStore.getFields());
      _normalizationState.put(ldspec, historyStore);
      return lkvStore;
    }
    return _valueStores.get(ldspec);
  }

  /**
   * Received raw, unnormalized values.
   * Will apply normalization, store results in the LKV store, and then rebroadcast
   * the normalized values.
   * 
   * @param uniqueId  the identifier for the updates
   * @param fields    updated fields
   */
  public void updateReceived(String uniqueId, FudgeMsg fields) {
    updateReceived(ExternalId.of(_externalIdScheme, uniqueId), fields);
  }

  /**
   * Received raw, unnormalized values.
   * Will apply normalization, store results in the LKV store, and then rebroadcast
   * the normalized values.
   * 
   * @param id     the identifier for the updates
   * @param fields updated fields
   */
  public void updateReceived(ExternalId id, FudgeMsg fields) {
    _tickMeter.mark();
    // Iterate over all normalization schemes.
    for (Map.Entry<String, NormalizationRuleSet> normalizationEntry : _normalization.entrySet()) {
      LiveDataSpecification ldspec = new LiveDataSpecification(normalizationEntry.getKey(), id);
      LastKnownValueStore lkvStore = ensureLastKnownValueStore(id, normalizationEntry.getKey());
      
      NormalizationRuleSet ruleSet = normalizationEntry.getValue();
      FudgeMsg normalizedFields = ruleSet.getNormalizedMessage(fields, id.getValue(), _normalizationState.get(ldspec));
      
      // If nothing to update, this returns null.
      if ((normalizedFields != null) && !normalizedFields.isEmpty()) {
        // update the LKV store
        lkvStore.updateFields(normalizedFields);
        
        // Blast them out.
        distributeNormalizedUpdate(ldspec, normalizedFields);
      }
    }
  }

  /**
   * Distribute results, after normalization and LKV storage, to downstream channels.
   * 
   * @param ldspec           Specification of the data
   * @param normalizedFields Fully normalized field data for that specification
   */
  protected void distributeNormalizedUpdate(LiveDataSpecification ldspec, FudgeMsg normalizedFields) {
    if (getNormalizedMessageSender() == null) {
      // Nothing to do here.
      return;
    }
    FudgeSerializer serializer = new FudgeSerializer(getNormalizedMessageSender().getFudgeContext());
    LiveDataValueUpdateBean updateBean = new LiveDataValueUpdateBean(0, ldspec, normalizedFields);
    FudgeMsg msg = LiveDataValueUpdateBeanFudgeBuilder.toFudgeMsg(serializer, updateBean);
    getNormalizedMessageSender().send(msg);
  }
  
}
