/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.aggregation.CurrenciesAggregationFunction;
import com.opengamma.financial.aggregation.PortfolioAggregator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.viewer.status.ViewStatus;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.tuple.Pair;

/**
 * View status calculation task
 */
public class ViewStatusCalculationTask implements Callable<PerViewStatusResult> {
  
  private final class ViewStatusResultListener extends AbstractViewResultListener {
    private final CountDownLatch _latch;
    private final PerViewStatusResult _statusResult;
    private final ViewDefinition _viewDefinition;
    private AtomicLong _count = new AtomicLong(0);

    private ViewStatusResultListener(CountDownLatch latch, PerViewStatusResult statusResult, ViewDefinition viewDefinition) {
      _latch = latch;
      _statusResult = statusResult;
      _viewDefinition = viewDefinition;
    }

    @Override
    public UserPrincipal getUser() {
      return _user;
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
      s_logger.error("View definition compiled");
      CompiledViewCalculationConfiguration compiledCalculationConfiguration = compiledViewDefinition.getCompiledCalculationConfiguration(DEFAULT_CALC_CONFIG);
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = compiledCalculationConfiguration.getTerminalOutputSpecifications();
      for (ValueSpecification valueSpec : terminalOutputs.keySet()) {
        ComputationTargetType computationTargetType = valueSpec.getTargetSpecification().getType();
        if (isValidTargetType(computationTargetType)) {
          UniqueId uniqueId = valueSpec.getTargetSpecification().getUniqueId();
          String currency = getCurrency(uniqueId, computationTargetType);
          if (currency != null) {
            _statusResult.put(new ViewStatusKeyBean(_securityType, valueSpec.getValueName(), currency, computationTargetType.getName()), ViewStatus.NO_VALUE);
          } else {
            s_logger.error("Discarding result as NULL return as Currency for id: {} targetType:{}", uniqueId, computationTargetType);
          }
        }
      }
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      s_logger.debug("View definition {} failed to initialize", _viewDefinition);
      try {
        processGraphFailResult(_statusResult);
      } finally {
        _latch.countDown();
      }
    }

    public void cycleStarted(ViewCycleMetadata cycleInfo) {
      s_logger.debug("Cycle started");
    }

    @Override
    public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
      s_logger.debug("Cycle execution failed", exception);
    }

    @Override
    public void processCompleted() {
      s_logger.debug("Process completed");
    }

    @Override
    public void processTerminated(boolean executionInterrupted) {
      s_logger.debug("Process terminated");
    }

    @Override
    public void clientShutdown(Exception e) {
      s_logger.debug("Client shutdown");
    }

    @Override
    public void cycleFragmentCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      s_logger.debug("cycle fragment completed");
    }

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      s_logger.debug("cycle {} completed", _count.get());
      if (_count.getAndIncrement() > 5) {
        processStatusResult(fullResult, _statusResult);
        _latch.countDown();
      }
    }
  }

  private static final String MIXED_CURRENCY = "MIXED_CURRENCY";
  private static final Logger s_logger = LoggerFactory.getLogger(ViewStatusCalculationTask.class);
  
  private static final String DEFAULT_CALC_CONFIG = "Default";
  
  private final String _securityType;
  private final Set<String> _valueRequirementNames;
  private final UniqueId _portfolioId;
  private final UserPrincipal _user;
  private final ToolContext _toolContext;
  private final CurrenciesAggregationFunction _currenciesAggrFunction;
  private final Map<UniqueId, String> _targetCurrenciesCache = Maps.newConcurrentMap();
  private final MarketDataSpecification _marketDataSpecification;
  
  public ViewStatusCalculationTask(ToolContext toolcontext, UniqueId portfolioId, UserPrincipal user, String securityType, 
      Collection<String> valueRequirementNames, MarketDataSpecification marketDataSpecification) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(valueRequirementNames, "valueRequirementNames");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(toolcontext, "toolcontext");
    ArgumentChecker.notNull(marketDataSpecification, "marketDataSpecification");
    
    _portfolioId = portfolioId;
    _user = user;
    _securityType = securityType;
    _valueRequirementNames = ImmutableSet.copyOf(valueRequirementNames);
    _toolContext = toolcontext;
    _currenciesAggrFunction = new CurrenciesAggregationFunction(_toolContext.getSecuritySource());
    _marketDataSpecification = marketDataSpecification;
  }

  @Override
  public PerViewStatusResult call() throws Exception {
    s_logger.debug("Start calculating result for security:{} with values{}", _securityType, Sets.newTreeSet(_valueRequirementNames).toString());
    
    final PerViewStatusResult statusResult = new PerViewStatusResult(_securityType);
    //No need to do any work if there are no ValueRequirements to compute
    if (_valueRequirementNames.isEmpty()) {
      return statusResult;
    }
    final ViewDefinition viewDefinition = createViewDefinition();
    final ViewProcessor viewProcessor = _toolContext.getViewProcessor();
    final ViewClient client = viewProcessor.createViewClient(_user);

    final CountDownLatch latch = new CountDownLatch(1);
    client.setResultListener(new ViewStatusResultListener(latch, statusResult, viewDefinition));
    client.attachToViewProcess(viewDefinition.getUniqueId(), ExecutionOptions.infinite(_marketDataSpecification));
   
    try {
      s_logger.info("main thread waiting");
      if (!latch.await(30, TimeUnit.SECONDS)) {
        s_logger.error("Timed out waiting for {}", viewDefinition);
      }
    } catch (final InterruptedException ex) {
      throw new OpenGammaRuntimeException("Interrupted while waiting for " + viewDefinition, ex);
    }
    client.detachFromViewProcess();
    removeViewDefinition(viewDefinition);
    s_logger.debug("PerViewStatusResult for securityType:{} is {}", _securityType, statusResult);
    return statusResult;
  }

  

  protected boolean isValidTargetType(final ComputationTargetType computationTargetType) {
    if (ComputationTargetType.POSITION.isCompatible(computationTargetType) || ComputationTargetType.PORTFOLIO.isCompatible(computationTargetType) ||
        ComputationTargetType.PORTFOLIO_NODE.isCompatible(computationTargetType) || ComputationTargetType.TRADE.isCompatible(computationTargetType)) {
      return true;
    }
    return false;
  }

  private void removeViewDefinition(ViewDefinition viewDefinition) {
    s_logger.debug("Removing ViewDefintion with id: {}", viewDefinition.getUniqueId());
    ConfigMaster configMaster = _toolContext.getConfigMaster();
    configMaster.remove(viewDefinition.getUniqueId().getObjectId());
    s_logger.debug("ViewDefinition {} removed", viewDefinition.getUniqueId());
  }

  private ViewDefinition createViewDefinition() {
    final ViewDefinition viewDefinition = new ViewDefinition("VS_VIEW_" + GUIDGenerator.generate().toString(), _portfolioId, _user);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    for (String requiredOutput : _valueRequirementNames) {
      defaultCalConfig.addPortfolioRequirementName(_securityType, requiredOutput);
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return storeViewDefinition(viewDefinition);
  }
  
  private ViewDefinition storeViewDefinition(final ViewDefinition viewDefinition) {
    ConfigItem<ViewDefinition> config = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
    config = ConfigMasterUtils.storeByName(_toolContext.getConfigMaster(), config);
    return config.getValue();
  }
  
  private void processGraphFailResult(final PerViewStatusResult statusResult) {
    PositionSource positionSource = _toolContext.getPositionSource();
    Portfolio portfolio = positionSource.getPortfolio(_portfolioId, VersionCorrection.LATEST);
    List<Position> positions = PortfolioAggregator.flatten(portfolio);
    Set<String> currencies = Sets.newHashSet();
    for (Position position : positions) {
      if (position.getSecurity() == null) {
        position.getSecurityLink().resolve(_toolContext.getSecuritySource());
      }
      if (position.getSecurity() != null && _securityType.equals(position.getSecurity().getSecurityType())) {
        currencies.add(getCurrency(position.getUniqueId(), ComputationTargetType.POSITION));
      }
    }
    for (String valueName : _valueRequirementNames) {
      for (String currency : currencies) {
        statusResult.put(new ViewStatusKeyBean(_securityType, valueName, currency, ComputationTargetType.POSITION.getName()), ViewStatus.GRAPH_FAIL);
      }
    }
  }
  
  private void processStatusResult(ViewComputationResultModel fullResult, PerViewStatusResult statusResult) {
    ViewCalculationResultModel calculationResult = fullResult.getCalculationResult(DEFAULT_CALC_CONFIG);
    Collection<ComputationTargetSpecification> allTargets = calculationResult.getAllTargets();
    for (ComputationTargetSpecification targetSpec : allTargets) {
      ComputationTargetType targetType = targetSpec.getSpecification().getType();
      if (isValidTargetType(targetType)) {
        Map<Pair<String, ValueProperties>, ComputedValueResult> values = calculationResult.getValues(targetSpec);
        for (Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> valueEntry : values.entrySet()) {
          String valueName = valueEntry.getKey().getFirst();
          String currency = getCurrency(targetSpec.getUniqueId(), targetType);
          s_logger.debug("{} currency returned for id:{} targetType:{}", currency, targetSpec.getUniqueId(), targetType);
          if (currency != null) {
            ComputedValueResult computedValue = valueEntry.getValue();
            if (isGoodValue(computedValue)) {
              statusResult.put(new ViewStatusKeyBean(_securityType, valueName, currency, targetType.getName()), ViewStatus.VALUE);
            }
          } else {
            s_logger.error("Discarding result as NULL return as Currency for id: {} targetType:{}", targetSpec.getUniqueId(), targetType);
          }
        }
      }
    }
  }
  
  private String getCurrency(UniqueId uniqueId, ComputationTargetType computationTargetType) {
    synchronized (_targetCurrenciesCache) {
      String currency = _targetCurrenciesCache.get(uniqueId);
      if (currency == null) {
        if (ComputationTargetType.PORTFOLIO_NODE.isCompatible(computationTargetType) || ComputationTargetType.PORTFOLIO.isCompatible(computationTargetType)) {
          currency = MIXED_CURRENCY;
        } else if (ComputationTargetType.POSITION.isCompatible(computationTargetType)) {
          PositionSource positionSource = _toolContext.getPositionSource();
          Position position = positionSource.getPosition(uniqueId);
          if (position.getSecurity() == null) {
            position.getSecurityLink().resolve(_toolContext.getSecuritySource());
          }
          if (position.getSecurity() != null) {
            currency = _currenciesAggrFunction.classifyPosition(position);
          } 
        } else if (ComputationTargetType.TRADE.isCompatible(computationTargetType)) {
          PositionSource positionSource = _toolContext.getPositionSource();
          Trade trade = positionSource.getTrade(uniqueId);
          if (trade.getSecurity() == null) {
            trade.getSecurityLink().resolve(_toolContext.getSecuritySource());
          }
          if (trade.getSecurity() != null) {
            currency = CurrenciesAggregationFunction.classifyBasedOnSecurity(trade.getSecurity(), _toolContext.getSecuritySource());
          }
        }
      }
      if (currency == null) {
        currency = CurrenciesAggregationFunction.NO_CURRENCY;
      }
      _targetCurrenciesCache.put(uniqueId, currency);
      return currency;
    }
  }
  
  private boolean isGoodValue(final ComputedValueResult computedValue) {
    if (computedValue == null || computedValue.getValue() == null || StringUtils.EMPTY.equals(computedValue.getValue())) {
      return false;
    } else {
      return !(computedValue.getValue() instanceof MissingValue);
    }
  }
  
}
