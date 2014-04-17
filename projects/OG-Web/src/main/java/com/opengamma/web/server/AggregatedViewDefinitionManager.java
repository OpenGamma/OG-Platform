/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.aggregation.PortfolioAggregator;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Manages the lifecycle of aggregated view definitions. There is really no such thing as an aggregated view
 * definition, only an aggregated portfolio, but the web client exposes the idea of aggregating a view definition as a
 * shortcut for aggregating the underlying portfolio and requesting the same outputs.
 */
public class AggregatedViewDefinitionManager {

  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final ConfigSource _combinedConfigSource;
  private final ConfigMaster _userConfigMaster;
  private final PortfolioMaster _userPortfolioMaster;
  private final Map<String, AggregationFunction<?>> _portfolioAggregators;
  private final SavePortfolio _portfolioSaver;
  
  private final ReentrantLock _lock = new ReentrantLock();
  private final Map<Pair<UniqueId, List<String>>, PortfolioReference> _aggregatedPortfolios = Maps.newHashMap();
  private final Map<Pair<UniqueId, List<String>>, ViewDefinitionReference> _aggregatedViewDefinitions = Maps.newHashMap();

  public AggregatedViewDefinitionManager(PositionSource positionSource,
                                         SecuritySource securitySource,
                                         ConfigSource combinedConfigSource,
                                         ConfigMaster userConfigMaster,
                                         PortfolioMaster userPortfolioMaster,
                                         PositionMaster userPositionMaster,
                                         Map<String, AggregationFunction<?>> portfolioAggregators) {
    _positionSource = positionSource;
    _securitySource = securitySource;
    _combinedConfigSource = combinedConfigSource;
    _userConfigMaster = userConfigMaster;
    _userPortfolioMaster = userPortfolioMaster;
    _portfolioAggregators = portfolioAggregators;
    _portfolioSaver = new SavePortfolio(Executors.newSingleThreadExecutor(), userPortfolioMaster, userPositionMaster);
  }
  
  public Set<String> getAggregatorNames() {
    return ImmutableSet.copyOf(_portfolioAggregators.keySet());
  }
  
  public UniqueId getViewDefinitionId(UniqueId baseViewDefinitionId, String aggregatorName) {
    List<String> aggregators;
    if (aggregatorName != null) {
      aggregators = Collections.singletonList(aggregatorName);
    } else {
      aggregators = Collections.emptyList();
    }
    return getViewDefinitionId(baseViewDefinitionId, aggregators);
  }

  public UniqueId getViewDefinitionId(UniqueId baseViewDefinitionId, List<String> aggregatorNames) {
    // TODO: what about changes to the base view definition?
    ArgumentChecker.notNull(baseViewDefinitionId, "baseViewDefinitionId");
    ArgumentChecker.notNull(aggregatorNames, "aggregatorNames");
    ViewDefinition baseViewDefinition = (ViewDefinition) _combinedConfigSource.get(baseViewDefinitionId).getValue();
    if (baseViewDefinition == null) {
      throw new OpenGammaRuntimeException("Unknown view definition with unique ID " + baseViewDefinitionId);
    }
    UniqueId basePortfolioId = baseViewDefinition.getPortfolioId();
    if (aggregatorNames.isEmpty() || basePortfolioId == null) {
      return baseViewDefinitionId;
    }
    Pair<UniqueId, List<String>> aggregatedViewDefinitionKey = Pairs.of(baseViewDefinition.getUniqueId(), aggregatorNames);
    Pair<UniqueId, List<String>> aggregatedPortfolioKey = Pairs.of(basePortfolioId, aggregatorNames);
    _lock.lock();
    try {
      ViewDefinitionReference aggregatedViewDefinitionReference = _aggregatedViewDefinitions.get(aggregatedViewDefinitionKey);
      if (aggregatedViewDefinitionReference == null) {
        PortfolioReference aggregatedPortfolioReference = _aggregatedPortfolios.get(aggregatedPortfolioKey);
        if (aggregatedPortfolioReference == null) {
          UniqueId aggregatedPortfolioId = aggregatePortfolio(basePortfolioId, aggregatorNames);
          aggregatedPortfolioReference = new PortfolioReference(basePortfolioId, aggregatedPortfolioId);
          _aggregatedPortfolios.put(aggregatedPortfolioKey, aggregatedPortfolioReference);
        }
        String aggregatedViewDefinitionName = getAggregatedViewDefinitionName(baseViewDefinition.getName(), aggregatorNames);
        ViewDefinition aggregatedViewDefinition = baseViewDefinition.copyWith(aggregatedViewDefinitionName,
                                                                              aggregatedPortfolioReference.incrementReferenceCount(),
                                                                              baseViewDefinition.getMarketDataUser());
        
        // Treat as a transient view definition that should not be persistent
        //aggregatedViewDefinition.setPersistent(false);
        
        ConfigItem<ViewDefinition> configItem = ConfigItem.of(aggregatedViewDefinition);
        configItem.setName(aggregatedViewDefinition.getName());
        UniqueId viewDefinitionId = _userConfigMaster.add(new ConfigDocument(configItem)).getUniqueId();
        aggregatedViewDefinitionReference = new ViewDefinitionReference(viewDefinitionId, aggregatedPortfolioReference);
        _aggregatedViewDefinitions.put(aggregatedViewDefinitionKey, aggregatedViewDefinitionReference);
      }
      return aggregatedViewDefinitionReference.incrementReferenceCount();
    } finally {
      _lock.unlock();
    }
  }
  
  public void releaseViewDefinition(UniqueId baseViewDefinitionId, String aggregatorName) {
    List<String> aggregatorNames;
    if (aggregatorName != null) {
      aggregatorNames = Collections.singletonList(aggregatorName);
    } else {
      aggregatorNames = Collections.emptyList();
    }
    releaseViewDefinition(baseViewDefinitionId, aggregatorNames);
  }

  public void releaseViewDefinition(UniqueId baseViewDefinitionId, List<String> aggregatorNames) {
    Pair<UniqueId, List<String>> aggregatedViewDefinitionKey = Pairs.of(baseViewDefinitionId, aggregatorNames);
    ViewDefinitionReference viewDefinitionReference = _aggregatedViewDefinitions.get(aggregatedViewDefinitionKey);
    if (viewDefinitionReference == null) {
      return;
    }
    _lock.lock();
    try {
      if (viewDefinitionReference.decrementReferenceCount() <= 0) {
        PortfolioReference portfolioReference = viewDefinitionReference.getPortfolioReference();
        if (portfolioReference.decrementReferenceCount() <= 0) {
          _userPortfolioMaster.remove(portfolioReference.getPortfolioId());
          Pair<UniqueId, List<String>> aggregatedPortfolioKey = Pairs.of(portfolioReference.getBasePortfolioId(), aggregatorNames);
          _aggregatedPortfolios.remove(aggregatedPortfolioKey);
        }
        _userConfigMaster.remove(viewDefinitionReference.getViewDefinitionId());
        _aggregatedViewDefinitions.remove(aggregatedViewDefinitionKey);
      }
    } finally {
      _lock.unlock();
    }
  }
  
  private String getAggregatedViewDefinitionName(String baseViewDefinitionName, List<String> aggregatorNames) {
    return baseViewDefinitionName + " aggregated by " + StringUtils.join(aggregatorNames, ", ");
  }

  private UniqueId aggregatePortfolio(UniqueId basePortfolioId, List<String> aggregatorNames) {
    // REVIEW jonathan 2011-11-13 -- portfolio aggregation is currently painful. The positions obtained from the
    // position source during the orginal portfolio lookup contain munged identifiers that do not correspond to
    // anything in the position master. We end up rewriting the positions even though there is no need, then we cannot
    // clean them up when the portfolio is no longer required in case other portfolios have now referenced the new
    // positions.
    Portfolio basePortfolio = _positionSource.getPortfolio(basePortfolioId, VersionCorrection.LATEST);
    Portfolio resolvedPortfolio =
        PortfolioCompiler.resolvePortfolio(basePortfolio, Executors.newSingleThreadExecutor(), _securitySource);
    List<AggregationFunction<?>> aggregationFunctions = Lists.newArrayListWithCapacity(aggregatorNames.size());
    for (String aggregatorName : aggregatorNames) {
      AggregationFunction<?> aggregationFunction = _portfolioAggregators.get(aggregatorName);
      if (aggregationFunction == null) {
        throw new OpenGammaRuntimeException("Unknown aggregator '" + aggregatorName + "'");
      }
      aggregationFunctions.add(aggregationFunction);
    }
    PortfolioAggregator aggregator = new PortfolioAggregator(aggregationFunctions);
    Portfolio aggregatedPortfolio = aggregator.aggregate(resolvedPortfolio);
    return _portfolioSaver.savePortfolio(aggregatedPortfolio, false);
  }
  
  //-------------------------------------------------------------------------
  private static class PortfolioReference {
    
    private final UniqueId _basePortfolioId;
    private final UniqueId _portfolioId;
    private long _referenceCount;
    
    public PortfolioReference(UniqueId basePortfolioId, UniqueId portfolioId) {
      _basePortfolioId = basePortfolioId;
      _portfolioId = portfolioId;
    }
    
    public UniqueId getBasePortfolioId() {
      return _basePortfolioId;
    }
    
    public UniqueId getPortfolioId() {
      return _portfolioId;
    }
    
    public UniqueId incrementReferenceCount() {
      _referenceCount++;
      return _portfolioId;
    }
    
    public long decrementReferenceCount() {
      return --_referenceCount;
    }
    
  }
  
  private static class ViewDefinitionReference {
    
    private final UniqueId _viewDefinitionId;
    private final PortfolioReference _portfolioReference;
    private long _referenceCount;
    
    public ViewDefinitionReference(UniqueId viewDefinitionId, PortfolioReference portfolioReference) {
      _viewDefinitionId = viewDefinitionId;
      _portfolioReference = portfolioReference;
    }

    public UniqueId getViewDefinitionId() {
      return _viewDefinitionId;
    }
    
    public PortfolioReference getPortfolioReference() {
      return _portfolioReference;
    }
    
    public UniqueId incrementReferenceCount() {
      _referenceCount++;
      return _viewDefinitionId;
    }
    
    public long decrementReferenceCount() {
      return --_referenceCount;
    }
    
  }

}
