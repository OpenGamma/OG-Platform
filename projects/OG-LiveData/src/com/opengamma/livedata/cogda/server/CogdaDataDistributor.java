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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.livedata.server.LastKnownValueStore;
import com.opengamma.livedata.server.LastKnownValueStoreProvider;
import com.opengamma.util.ArgumentChecker;

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
public class CogdaDataDistributor implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaDataDistributor.class);
  private final String _externalIdScheme;
  private final LastKnownValueStoreProvider _lastKnownValueStoreProvider;
  private final Map<String, NormalizationRuleSet> _normalization;
  
  private final ConcurrentMap<LiveDataSpecification, LastKnownValueStore> _valueStores =
      new ConcurrentHashMap<LiveDataSpecification, LastKnownValueStore>();
  private final ConcurrentMap<LiveDataSpecification, FieldHistoryStore> _normalizationState =
      new ConcurrentHashMap<LiveDataSpecification, FieldHistoryStore>();
  
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
  protected NormalizationRuleSet constructNormalizationRuleSet(String normalizationScheme) {
    // TODO kirk 2012-07-23 -- FIXME
    return StandardRules.getNoNormalization();
  }

  public void addDistribution(String uniqueIdentifier) {
    
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
    // Iterate over all normalization schemes.
    for (Map.Entry<String, NormalizationRuleSet> normalizationEntry : _normalization.entrySet()) {
      LiveDataSpecification ldspec = new LiveDataSpecification(normalizationEntry.getKey(), id);
      LastKnownValueStore lkvStore = ensureLastKnownValueStore(id, normalizationEntry.getKey());
      
      NormalizationRuleSet ruleSet = normalizationEntry.getValue();
      FudgeMsg normalizedFields = ruleSet.getNormalizedMessage(fields, id.getValue(), _normalizationState.get(ldspec));
      
      // update the LKV store
      lkvStore.updateFields(normalizedFields);
      
      // Blast them out.
      distributeNormalizedUpdate(ldspec, normalizedFields);
    }
  }

  /**
   * Distribute results, after normalization and LKV storage, to downstream channels.
   * 
   * @param ldspec           Specification of the data
   * @param normalizedFields Fully normalized field data for that specification
   */
  protected void distributeNormalizedUpdate(LiveDataSpecification ldspec, FudgeMsg normalizedFields) {
    // TODO kirk 2012-07-23 -- This.
  }

}
