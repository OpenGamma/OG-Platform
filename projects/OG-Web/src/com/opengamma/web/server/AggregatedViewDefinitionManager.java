/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.aggregation.PortfolioAggregator;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.engine.view.ViewDefinitionRepository;

/**
 * Manages the lifecycle of aggregated view definitions. There is really no such thing as an aggregated view
 * definition, only an aggregated portfolio, but the web client exposes the idea of aggregating a view definition as a
 * shortcut for aggregating the underlying portfolio and requesting the same outputs.
 */
public class AggregatedViewDefinitionManager {

  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;
  private final ViewDefinitionRepository _viewDefinitionRepository;
  private final ManageableViewDefinitionRepository _userViewDefinitionRepository;
  private final PortfolioMaster _userPortfolioMaster;
  private final Map<String, AggregationFunction<?>> _portfolioAggregators;
  private final SavePortfolio _portfolioSaver;
  
  private final ReentrantLock _lock = new ReentrantLock();  
  private final Map<Pair<UniqueId, String>, PortfolioReference> _aggregatedPortfolios = new HashMap<Pair<UniqueId, String>, PortfolioReference>();
  private final Map<Pair<UniqueId, String>, ViewDefinitionReference> _aggregatedViewDefinitions = new HashMap<Pair<UniqueId, String>, ViewDefinitionReference>();
  
  public AggregatedViewDefinitionManager(PositionSource positionSource, SecuritySource securitySource,
      ViewDefinitionRepository viewDefinitionRepository, ManageableViewDefinitionRepository userViewDefinitionRepository,
      PortfolioMaster userPortfolioMaster, PositionMaster userPositionMaster, Map<String, AggregationFunction<?>> portfolioAggregators) {
    _positionSource = positionSource;
    _securitySource = securitySource;
    _viewDefinitionRepository = viewDefinitionRepository;
    _userViewDefinitionRepository = userViewDefinitionRepository;
    _userPortfolioMaster = userPortfolioMaster;
    _portfolioAggregators = portfolioAggregators;
    _portfolioSaver = new SavePortfolio(Executors.newSingleThreadExecutor(), userPortfolioMaster, userPositionMaster);
  }
  
  public Set<String> getAggregatorNames() {
    return ImmutableSet.copyOf(_portfolioAggregators.keySet());
  }
  
  public UniqueId getViewDefinitionId(UniqueId baseViewDefinitionId, String aggregatorName) {
    // TODO: what about changes to the base view definition?
    ArgumentChecker.notNull(baseViewDefinitionId, "baseViewDefinitionId");
    ViewDefinition baseViewDefinition = _viewDefinitionRepository.getDefinition(baseViewDefinitionId);
    if (baseViewDefinition == null) {
      throw new OpenGammaRuntimeException("Unknown view definition with unique ID " + baseViewDefinitionId);
    }
    UniqueId basePortfolioId = baseViewDefinition.getPortfolioId();
    if (aggregatorName == null || basePortfolioId == null) {
      return baseViewDefinitionId;
    }
    Pair<UniqueId, String> aggregatedViewDefinitionKey = Pair.of(baseViewDefinition.getUniqueId(), aggregatorName);
    Pair<UniqueId, String> aggregatedPortfolioKey = Pair.of(basePortfolioId, aggregatorName);
    _lock.lock();
    try {
      ViewDefinitionReference aggregatedViewDefinitionReference = _aggregatedViewDefinitions.get(aggregatedViewDefinitionKey);
      if (aggregatedViewDefinitionReference == null) {
        PortfolioReference aggregatedPortfolioReference = _aggregatedPortfolios.get(aggregatedPortfolioKey);
        if (aggregatedPortfolioReference == null) {
          UniqueId aggregatedPortfolioId = aggregatePortfolio(basePortfolioId, aggregatorName);
          aggregatedPortfolioReference = new PortfolioReference(basePortfolioId, aggregatedPortfolioId);
          _aggregatedPortfolios.put(aggregatedPortfolioKey, aggregatedPortfolioReference);
        }
        String aggregatedViewDefinitionName = getAggregatedViewDefinitionName(baseViewDefinition.getName(), aggregatorName);
        ViewDefinition aggregatedViewDefinition = baseViewDefinition.copyWith(aggregatedViewDefinitionName,
            aggregatedPortfolioReference.incrementReferenceCount(), baseViewDefinition.getMarketDataUser());
        AddViewDefinitionRequest addViewDefinitionRequest = new AddViewDefinitionRequest(aggregatedViewDefinition);
        UniqueId viewDefinitionId = _userViewDefinitionRepository.addViewDefinition(addViewDefinitionRequest);
        aggregatedViewDefinitionReference = new ViewDefinitionReference(viewDefinitionId, aggregatedPortfolioReference);
        _aggregatedViewDefinitions.put(aggregatedViewDefinitionKey, aggregatedViewDefinitionReference);
      }
      return aggregatedViewDefinitionReference.incrementReferenceCount();
    } finally {
      _lock.unlock();
    }
  }
  
  public void releaseViewDefinition(UniqueId baseViewDefinitionId, String aggregatorName) {
    Pair<UniqueId, String> aggregatedViewDefinitionKey = Pair.of(baseViewDefinitionId, aggregatorName);
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
          Pair<UniqueId, String> aggregatedPortfolioKey = Pair.of(portfolioReference.getBasePortfolioId(), aggregatorName);
          _aggregatedPortfolios.remove(aggregatedPortfolioKey);
        }
        _userViewDefinitionRepository.removeViewDefinition(viewDefinitionReference.getViewDefinitionId());
        _aggregatedViewDefinitions.remove(aggregatedViewDefinitionKey);
      }
    } finally {
      _lock.unlock();
    }
  }
  
  private String getAggregatedViewDefinitionName(String baseViewDefinitionName, String aggregatorName) {
    return baseViewDefinitionName + " aggregated by " + aggregatorName;
  }
  
  private UniqueId aggregatePortfolio(UniqueId basePortfolioId, String aggregatorName) {
    // REVIEW jonathan 2011-11-13 -- portfolio aggregation is currently painful. The positions obtained from the
    // position source during the orginal portfolio lookup contain munged identifiers that do not correspond to
    // anything in the position master. We end up rewriting the positions even though there is no need, then we cannot
    // clean them up when the portfolio is no longer required in case other portfolios have now referenced the new
    // positions.
    AggregationFunction<?> aggregationFunction = _portfolioAggregators.get(aggregatorName);
    if (aggregationFunction == null) {
      throw new OpenGammaRuntimeException("Unknown aggregator '" + aggregatorName + "'");
    }
    PortfolioAggregator aggregator = new PortfolioAggregator(aggregationFunction);
    Portfolio basePortfolio = _positionSource.getPortfolio(basePortfolioId);
    basePortfolio = PortfolioCompiler.resolvePortfolio(basePortfolio, Executors.newSingleThreadExecutor(), _securitySource);
    Portfolio aggregatedPortfolio = aggregator.aggregate(basePortfolio);
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
