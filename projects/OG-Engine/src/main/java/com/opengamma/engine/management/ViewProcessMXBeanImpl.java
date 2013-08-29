/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import net.sf.ehcache.CacheException;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessInternal;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for attributes and operations on a view process.
 * 
 */
public class ViewProcessMXBeanImpl implements ViewProcessMXBean {

  /**
   * The backing view process instance
   */
  private final ViewProcessInternal _viewProcess;

  private final ObjectName _objectName;

  private ViewProcessor _viewProcessor;
  
  private enum PhaseState {
    PENDING, SUCCESSFUL, FAILED;
  }
  
  private enum Outcome {
    PENDING, YES, NO;
  }
  
  private enum CycleState {
    PENDING, STARTED, COMPLETED, FAILED;
  }
  private volatile PhaseState _compiled = PhaseState.PENDING;
  private volatile Outcome _hasMarketDataPermissions = Outcome.PENDING;
  private volatile Instant _compilationFailedValuationTime;
  private volatile Exception _compilationFailedException;
  private volatile CycleState _lastCycle = CycleState.PENDING;
  /**
   * Create a management View
   * 
   * @param viewProcess the underlying view process
   * @param viewProcessor the view processor responsible for the view process
   */
  public ViewProcessMXBeanImpl(ViewProcessInternal viewProcess, ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(viewProcessor, "ViewProcessor");
    _viewProcess = viewProcess;
    _viewProcessor = viewProcessor;
    _objectName = createObjectName(viewProcessor.getName(), viewProcess.getUniqueId());
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
          _hasMarketDataPermissions = hasMarketDataPermissions ? Outcome.YES : Outcome.NO;
        }

        @Override
        public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
          _compiled = PhaseState.FAILED;
          _compilationFailedException = exception;
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
          _lastCycle = CycleState.COMPLETED;
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
        
      });
    }
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=View,ViewProcessor=<viewProcessorName>,name=<viewProcessId>"
   */
  static ObjectName createObjectName(String viewProcessorName, UniqueId viewProcessId) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=ViewProcess,ViewProcessor=ViewProcessor " + viewProcessorName + ",name=ViewProcess " + viewProcessId.getValue());
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
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
    if (_compilationFailedValuationTime != null) {
      return ZonedDateTime.ofInstant(_compilationFailedValuationTime, ZoneId.systemDefault()).toString();
    }
    return null;
  }
  
  @Override
  public String getCompilationFailedException() {
    return _compilationFailedException.getMessage();
  }
  
  @Override
  public String getLastComputeCycleState() {
    return _lastCycle.name();
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
  
}
