/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessInternal;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.CacheException;

/**
 * An MBean implementation for attributes and operations on a view process.
 * 
 */
public class ViewProcessMXBeanImpl implements ViewProcessMXBean {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessMXBeanImpl.class);
  /**
   * The backing view process instance
   */
  private final ViewProcessInternal _viewProcess;

  private final ObjectName _objectName;

  private ViewProcessor _viewProcessor;

  private enum PhaseState {
    PENDING, SUCCESSFUL, FAILED
  }

  private enum Outcome {
    PENDING, YES, NO
  }

  private enum CycleState {
    PENDING, STARTED, COMPLETED, FAILED
  }

  private volatile PhaseState _compiled = PhaseState.PENDING;
  private volatile Outcome _hasMarketDataPermissions = Outcome.PENDING;
  private volatile Instant _compilationFailedValuationTime;
  private volatile Exception _compilationFailedException;
  private volatile CycleState _lastCycle = CycleState.PENDING;
  private volatile CompiledViewDefinition _lastCompiledViewDefinition;
  private volatile ViewComputationResultModel _lastViewComputationResultModel;
  private volatile Duration _lastSuccessfulCycleDuration;
  private volatile Instant _lastSuccessfulCycleTimeStamp;

  /**
   * Create a management View
   *
   * @param viewProcess the underlying view process
   * @param viewProcessor the view processor responsible for the view process
   * @param splitByViewProcessor
   */
  public ViewProcessMXBeanImpl(ViewProcessInternal viewProcess,
                               ViewProcessor viewProcessor,
                               boolean splitByViewProcessor) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(viewProcessor, "ViewProcessor");
    _viewProcess = viewProcess;
    _viewProcessor = viewProcessor;
    _objectName = createObjectName(viewProcessor.getName(), viewProcess.getUniqueId(), splitByViewProcessor);

    if (_viewProcess instanceof ViewProcessImpl) {
      ViewProcessImpl viewProcessImpl = (ViewProcessImpl) viewProcess;
      viewProcessImpl.attachListener(new InternalViewResultListener() {
        @Override
        public UserPrincipal getUser() {
          return null;
        }

        @Override
        public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
          _compiled = PhaseState.SUCCESSFUL;
          synchronized (ViewProcessMXBeanImpl.this) {
            _lastCompiledViewDefinition = compiledViewDefinition;
            _lastViewComputationResultModel = null;
          }
          _hasMarketDataPermissions = hasMarketDataPermissions ? Outcome.YES : Outcome.NO;
        }

        @Override
        public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
          _compiled = PhaseState.FAILED;
          _compilationFailedException = exception;
          _compilationFailedValuationTime = valuationTime;
        }

        @Override
        public void cycleStarted(ViewCycleMetadata cycleMetadata) {
          _lastCycle = CycleState.STARTED;
        }

        @Override
        public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
        }

        @Override
        public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
          synchronized (ViewProcessMXBeanImpl.this) {
            _lastViewComputationResultModel = fullResult;
          }
          _lastCycle = CycleState.COMPLETED;
          _lastSuccessfulCycleDuration = fullResult.getCalculationDuration();
          _lastSuccessfulCycleTimeStamp = fullResult.getCalculationTime();
        }

        @Override
        public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
          _lastCycle = CycleState.FAILED;
        }

        @Override
        public void processCompleted() {
        }

        @Override
        public void processTerminated(boolean executionInterrupted) {
        }

        @Override
        public void clientShutdown(Exception e) {
        }
        
      }, ViewResultMode.FULL_ONLY, ViewResultMode.NONE);
    }
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=View,ViewProcessor=<viewProcessorName>,name=<viewProcessId>"
   */
  static ObjectName createObjectName(String viewProcessorName, UniqueId viewProcessId, boolean splitByViewProcessor) {
    try {
      String beanNamePrefix = splitByViewProcessor ?
          "com.opengamma:type=ViewProcessors,ViewProcessor=ViewProcessor " + viewProcessorName :
          "com.opengamma:type=ViewProcessor";
      return new ObjectName(beanNamePrefix + ",ViewProcesses=ViewProcesses,name=ViewProcess " + viewProcessId.getValue());
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
  }
  
  @Override
  public String getUniqueId() {
    return _viewProcess.getUniqueId().toString();
  }
  
  @Override
  public String getPortfolioId() {
    return _viewProcess.getLatestViewDefinition().getPortfolioId().toString();
  }
  
  @Override
  public String getViewName() {
    @SuppressWarnings("unchecked")
    ConfigItem<ViewDefinition> configItem = (ConfigItem<ViewDefinition>) _viewProcessor.getConfigSource().get(_viewProcess.getDefinitionId());
    return configItem.getName();
  }

  @Override
  public String getDefinitionId() {
    return _viewProcess.getDefinitionId().toString();
  }
  
  @Override
  public boolean isPersistent() {
    return _viewProcess.getLatestViewDefinition().isPersistent();
  }

  @Override
  public String getState() {
    return _viewProcess.getState().name();
  }
  
  @Override
  public String getCompilationState() {
    return _compiled.name();
  }
  
  @Override
  public String getMarketDataPermissionsState() {
    return _hasMarketDataPermissions.name();
  }

  @Override
  public String getCompilationFailedValuationTime() {
    return convertInstant(_compilationFailedValuationTime);
  }

  private String convertInstant(Instant instant) {
    return instant != null ? ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).toString() : null;
  }

  @Override
  public String getCompilationFailedException() {
    return _compilationFailedException != null ? _compilationFailedException.getMessage() : null;
  }
  
  @Override
  public String getLastComputeCycleState() {
    return _lastCycle.name();
  }

  @Override
  public String getLastSuccessfulCycleTimeStamp() {
    return convertInstant(_lastSuccessfulCycleTimeStamp);
  }

  @Override
  public Long getTimeSinceLastSuccessfulCycle() {
    return _lastSuccessfulCycleTimeStamp != null ?
        _lastSuccessfulCycleTimeStamp.periodUntil(Instant.now(), ChronoUnit.MILLIS) :
        null;
  }

  @Override
  public Long getLastSuccessfulCycleDuration() {
    return _lastSuccessfulCycleDuration != null ? _lastSuccessfulCycleDuration.toMillis() : null;
  }

  @Override
  public void shutdown() {
    _viewProcess.shutdown();
  }
  
  @Override
  public void suspend() {
    _viewProcess.suspend();
  }

  @Override
  public void resume() {
    _viewProcess.resume();
  }

  /**
   * Gets the objectName field.
   * 
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }

  @Override
  public ViewProcessStatsProcessor generateResultsModelStatistics() {

    CompiledViewDefinition compiledViewDef;
    ViewComputationResultModel viewComputationResultModel;
    synchronized (this) {
      compiledViewDef = _lastCompiledViewDefinition;
      viewComputationResultModel = _lastViewComputationResultModel;
    }
    if (compiledViewDef == null || viewComputationResultModel == null) {
      return null;
    }
    ViewProcessStatsProcessor statsProcessor = new ViewProcessStatsProcessor(compiledViewDef, viewComputationResultModel);
    statsProcessor.processResult();
    return statsProcessor;
  }
}

