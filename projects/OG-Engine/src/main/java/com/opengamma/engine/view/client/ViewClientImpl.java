/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.resource.EngineResourceRetainer;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.client.merging.RateLimitingMergingViewProcessListener;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.permission.ViewPermissionContext;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.engine.view.permission.ViewPortfolioPermissionProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of {@link ViewClient}.
 */
public class ViewClientImpl implements ViewClient {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewClientImpl.class);

  private final ReentrantLock _clientLock = new ReentrantLock();

  private final UniqueId _id;
  private final ViewProcessorImpl _viewProcessor;
  private final UserPrincipal _user;
  private final EngineResourceRetainer _latestCycleRetainer;

  private final AtomicReference<ViewResultMode> _resultMode = new AtomicReference<>(ViewResultMode.FULL_ONLY);
  private final AtomicReference<ViewResultMode> _fragmentResultMode = new AtomicReference<>(ViewResultMode.NONE);

  private final AtomicBoolean _isViewCycleAccessSupported = new AtomicBoolean(false);
  private final AtomicBoolean _isAttached = new AtomicBoolean(false);
  private ViewClientState _state = ViewClientState.STARTED;

  // Per-process state
  private volatile ViewPermissionProvider _permissionProvider;
  private volatile ViewPortfolioPermissionProvider _portfolioPermissionProvider;

  @SuppressWarnings("unused")
  private volatile boolean _canAccessCompiledViewDefinition;

  @SuppressWarnings("unused")
  private volatile boolean _canAccessComputationResults;
  private volatile CountDownLatch _completionLatch = new CountDownLatch(0);
  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<>();

  private final AtomicReference<CompiledViewDefinition> _latestCompiledViewDefinition = new AtomicReference<>();

  private final RateLimitingMergingViewProcessListener _mergingViewProcessListener;

  private final AtomicReference<ViewResultListener> _userResultListener = new AtomicReference<>();
  private final Set<Pair<String, ValueSpecification>> _elevatedLogSpecs = new HashSet<>();

  /**
   * Holds the context information to be passed to the view process where it will be added to each log statement (using the MDC mechanism).
   */
  private Map<String, String> _viewProcessContextMap;

  /**
   * Constructs an instance.
   * 
   * @param id the unique identifier assigned to this view client
   * @param viewProcessor the parent view processor to which this client belongs
   * @param user the user who owns this client
   * @param timer the timer to use for scheduled tasks
   */
  public ViewClientImpl(UniqueId id, ViewProcessorImpl viewProcessor, UserPrincipal user, ScheduledExecutorService timer) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(timer, "timer");

    _id = id;
    _viewProcessor = viewProcessor;
    _user = user;
    _latestCycleRetainer = new EngineResourceRetainer(viewProcessor.getViewCycleManager());

    ViewResultListener mergedViewProcessListener = new ViewResultListener() {

      @Override
      public UserPrincipal getUser() {
        return ViewClientImpl.this.getUser();
      }

      @Override
      public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
        updateLatestCompiledViewDefinition(compiledViewDefinition);

        _canAccessCompiledViewDefinition = _permissionProvider.canAccessCompiledViewDefinition(getUser(), compiledViewDefinition);
        _canAccessComputationResults = _permissionProvider.canAccessComputationResults(getUser(), compiledViewDefinition, hasMarketDataPermissions);

        PortfolioFilter filter = _portfolioPermissionProvider.createPortfolioFilter(getUser());

        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {

          if (_canAccessComputationResults) {

            CompiledViewDefinition replacementViewDef = createFilteredViewDefinition(compiledViewDefinition, filter);
            listener.viewDefinitionCompiled(replacementViewDef, hasMarketDataPermissions);
          } else {
            // TODO [PLAT-1144] -- so we know whether or not the user is permissioned for various things, but what do we
            // pass to downstream listeners? Some special perm denied message in place of results on each computation
            // cycle?

            // In order to do something, we'll tell listeners that the compilation failed. Clearly
            // in the future this wants to be more fine grained
            listener.viewDefinitionCompilationFailed(
                Instant.now(), new Exception("User: " + _user + " does not have permission for data in this view"));
          }
        }
      }

      private CompiledViewDefinition createFilteredViewDefinition(CompiledViewDefinition compiledViewDefinition, PortfolioFilter filter) {

        // TODO see [PLAT-2478] and DependencyGraphStructureBuilder
        if (compiledViewDefinition instanceof CompiledViewDefinitionWithGraphsImpl) {
          CompiledViewDefinitionWithGraphsImpl cvdwg = (CompiledViewDefinitionWithGraphsImpl) compiledViewDefinition;
          Collection<DependencyGraphExplorer> dependencyGraphExplorers = cvdwg.getDependencyGraphExplorers();
          Collection<DependencyGraph> graphs = new ArrayList<DependencyGraph>(dependencyGraphExplorers.size());
          for (DependencyGraphExplorer explorer : dependencyGraphExplorers) {
            graphs.add(explorer.getWholeGraph());
          }
          return new CompiledViewDefinitionWithGraphsImpl(cvdwg.getResolverVersionCorrection(), cvdwg.getCompilationIdentifier(), cvdwg.getViewDefinition(), graphs,
              cvdwg.getResolvedIdentifiers(), filter.generateRestrictedPortfolio(compiledViewDefinition.getPortfolio()), cvdwg.getFunctionInitId(),
              cvdwg.getCompiledCalculationConfigurations(), cvdwg.getValidFrom(), cvdwg.getValidTo());
        } else {
          // Would be better if there was a builder for this!
          return new CompiledViewDefinitionImpl(compiledViewDefinition.getResolverVersionCorrection(), compiledViewDefinition.getCompilationIdentifier(),
              compiledViewDefinition.getViewDefinition(), filter.generateRestrictedPortfolio(compiledViewDefinition.getPortfolio()),
              compiledViewDefinition.getCompiledCalculationConfigurations(), compiledViewDefinition.getValidFrom(), compiledViewDefinition.getValidTo());
        }
      }

      @Override
      public void cycleStarted(ViewCycleMetadata cycleMetadata) {
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          // Only send cycle started results in conjunction with fragments
          if (!getFragmentResultMode().equals(ViewResultMode.NONE)) {
            listener.cycleStarted(cycleMetadata);
          }
        }
      }

      @Override
      public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
        boolean isFirstResult = updateLatestResult(fullResult);
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          ViewResultMode resultMode = getResultMode();
          if (!resultMode.equals(ViewResultMode.NONE)) {
            ViewComputationResultModel userFullResult = isFullResultRequired(resultMode, isFirstResult) ? fullResult : null;
            ViewDeltaResultModel userDeltaResult = isDeltaResultRequired(resultMode, isFirstResult) ? deltaResult : null;
            if (userFullResult != null || userDeltaResult != null) {
              listener.cycleCompleted(userFullResult, userDeltaResult);
            } else if (!isFirstResult || resultMode != ViewResultMode.DELTA_ONLY) {
              // Would expect this if it's the first result and we're in delta only mode, otherwise log a warning
              s_logger.warn("Ignored CycleCompleted call with no useful results to propagate");
            }
          }
        }
      }

      @Override
      public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
        ViewComputationResultModel prevResult = _latestResult.get();
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          ViewResultMode resultMode = getFragmentResultMode();
          if (!resultMode.equals(ViewResultMode.NONE)) {
            ViewComputationResultModel userFullResult = isFullResultRequired(resultMode, prevResult == null) ? fullFragment : null;
            ViewDeltaResultModel userDeltaResult = isDeltaResultRequired(resultMode, prevResult == null) ? deltaFragment : null;
            if (userFullResult != null || userDeltaResult != null) {
              listener.cycleFragmentCompleted(userFullResult, userDeltaResult);
            } else {
              if ((prevResult != null) || (resultMode != ViewResultMode.DELTA_ONLY)) {
                // Would expect this if it's the first result and we're in delta only mode, otherwise log a warning
                s_logger.warn("Ignored CycleFragmentCompleted call with no useful results to propagate");
              }
            }
          }
        }
      }

      @Override
      public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          listener.cycleExecutionFailed(executionOptions, exception);
        }
      }

      @Override
      public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          listener.viewDefinitionCompilationFailed(valuationTime, exception);
        }
      }

      @Override
      public void processCompleted() {
        ViewClientImpl.this.processCompleted();
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          listener.processCompleted();
        }
      }

      @Override
      public void processTerminated(boolean executionInterrupted) {
        ViewClientImpl.this.detachFromViewProcess();
        ViewResultListener listener = _userResultListener.get();
        if (listener != null) {
          listener.processTerminated(executionInterrupted);
        }
      }

      @Override
      public void clientShutdown(Exception e) {
        throw new UnsupportedOperationException("Shutdown notification unexpectedly received from merging result listener");
      }

    };

    _mergingViewProcessListener = new RateLimitingMergingViewProcessListener(mergedViewProcessListener, getViewProcessor().getViewCycleManager(), timer);
    _mergingViewProcessListener.setPaused(true);
  }

  @Override
  public UniqueId getUniqueId() {
    return _id;
  }

  @Override
  public UserPrincipal getUser() {
    return _user;
  }

  @Override
  public ViewProcessorImpl getViewProcessor() {
    return _viewProcessor;
  }

  @Override
  public ViewClientState getState() {
    return _state;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isAttached() {
    return _isAttached.get();
  }

  @Override
  public void attachToViewProcess(UniqueId viewDefinitionId, ViewExecutionOptions executionOptions) {
    attachToViewProcess(viewDefinitionId, executionOptions, false);
  }

  @Override
  public void attachToViewProcess(UniqueId viewDefinitionId, ViewExecutionOptions executionOptions, boolean privateProcess) {
    final boolean isPaused;
    _clientLock.lock();
    try {
      checkNotTerminated();
      checkNotAttached();
      // The client is detached right now so the merging update listener is paused. Although the following calls may
      // cause initial updates to be pushed through, they will not be seen until the merging update listener is
      // resumed, at which point the new permission provider will be in place. This is important to prevent potential
      // deadlocks as the initial notifications will be passed to the merging listener while the _clientLock is held -
      // they must not be passed on to the underlying listener.
      ViewProcessorImpl viewProcessor = getViewProcessor();
      ViewPermissionContext context = privateProcess ? viewProcessor.attachClientToPrivateViewProcess(getUniqueId(), _mergingViewProcessListener, viewDefinitionId, executionOptions,
          _viewProcessContextMap) : viewProcessor.attachClientToSharedViewProcess(getUniqueId(), _mergingViewProcessListener, viewDefinitionId, executionOptions, _viewProcessContextMap);
      isPaused = attachToViewProcessCore(context);
    } finally {
      _clientLock.unlock();
    }
    _mergingViewProcessListener.setPaused(isPaused);
  }

  @Override
  public void attachToViewProcess(UniqueId processId) {
    final boolean isPaused;
    _clientLock.lock();
    try {
      checkNotTerminated();
      checkNotAttached();
      // See concurrency notes in {@link #attachToViewProcess(UniqueId,ViewExecutionOptions,boolean)}.
      ViewPermissionContext context = getViewProcessor().attachClientToViewProcess(getUniqueId(), _mergingViewProcessListener, processId);
      isPaused = attachToViewProcessCore(context);
    } finally {
      _clientLock.unlock();
    }
    _mergingViewProcessListener.setPaused(isPaused);
  }

  /**
   * @param context the permission set
   * @return the pause state to pass to the merging result listener
   */
  private boolean attachToViewProcessCore(final ViewPermissionContext context) {
    _permissionProvider = context.getViewPermissionProvider();
    _portfolioPermissionProvider = context.getViewPortfolioPermissionProvider();
    _isAttached.set(true);
    _completionLatch = new CountDownLatch(1);
    return getState() == ViewClientState.PAUSED;
  }

  @Override
  public void detachFromViewProcess() {
    _clientLock.lock();
    try {
      processCompleted();
      getViewProcessor().detachClientFromViewProcess(getUniqueId());
      getLatestCycleRetainer().replaceRetainedCycle(null);
      _mergingViewProcessListener.setPaused(true);
      _mergingViewProcessListener.reset();
      _latestResult.set(null);
      _isAttached.set(false);
      _permissionProvider = null;
    } finally {
      _clientLock.unlock();
    }
  }

  @Override
  public MarketDataInjector getLiveDataOverrideInjector() {
    // [PLAT-1174] - this shouldn't be here
    return getViewProcessor().getLiveDataOverrideInjector(getUniqueId());
  }

  @Override
  public ViewDefinition getLatestViewDefinition() {
    return getViewProcessor().getLatestViewDefinition(getUniqueId());
  }
  
  @Override
  public ViewProcess getViewProcess() {
    _clientLock.lock();
    try {
      checkAttached();
      return getViewProcessor().getViewProcessForClient(getUniqueId());
    } finally {
      _clientLock.unlock();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void setResultListener(ViewResultListener resultListener) {
    _userResultListener.set(resultListener);
  }

  @Override
  public void setUpdatePeriod(long periodMillis) {
    _mergingViewProcessListener.setMinimumUpdatePeriodMillis(periodMillis);
  }

  @Override
  public ViewResultMode getResultMode() {
    return _resultMode.get();
  }

  @Override
  public void setResultMode(ViewResultMode resultMode) {
    _resultMode.set(resultMode);
  }

  @Override
  public ViewResultMode getFragmentResultMode() {
    return _fragmentResultMode.get();
  }

  @Override
  public void setFragmentResultMode(ViewResultMode fragmentResultMode) {
    _fragmentResultMode.set(fragmentResultMode);
  }

  //-------------------------------------------------------------------------
  @Override
  public void pause() {
    _clientLock.lock();
    try {
      checkNotTerminated();
      if (isAttached()) {
        _mergingViewProcessListener.setPaused(true);
      }
      _state = ViewClientState.PAUSED;
    } finally {
      _clientLock.unlock();
    }
  }

  @Override
  public void resume() {
    boolean unpause = false;
    _clientLock.lock();
    try {
      checkNotTerminated();
      unpause = isAttached();
      _state = ViewClientState.STARTED;
    } finally {
      _clientLock.unlock();
    }
    if (unpause) {
      _mergingViewProcessListener.setPaused(false);
    }
  }

  @Override
  public void triggerCycle() {
    getViewProcessor().triggerCycle(getUniqueId());
  }

  @Override
  public boolean isCompleted() {
    // Race condition between checking attachment and operating on the latch is fine; if the client is being attached
    // concurrently then there's no guarantee which process this method refers to.
    checkAttached();
    return _completionLatch.getCount() == 0;
  }

  @Override
  public void waitForCompletion() throws InterruptedException {
    // Race condition between checking attachment and operating on the latch is fine; if the client is being attached
    // concurrently then there's no guarantee which process this method refers to.
    checkAttached();
    try {
      _completionLatch.await();
    } catch (InterruptedException e) {
      s_logger.debug("Interrupted while waiting for completion of the view process");
      throw e;
    }
  }

  @Override
  public boolean isResultAvailable() {
    // Race condition between checking attachment and getting the latest result is fine; if the client is being
    // attached concurrently then there's no guarantee which process this method refers to.
    checkAttached();
    return _latestResult.get() != null;
  }

  @Override
  public ViewComputationResultModel getLatestResult() {
    // Race condition between checking attachment and getting the latest result is fine; if the client is being
    // attached concurrently then there's no guarantee which process this method refers to.
    checkAttached();
    return _latestResult.get();
  }

  @Override
  public CompiledViewDefinition getLatestCompiledViewDefinition() {
    return _latestCompiledViewDefinition.get();
  }

  @Override
  public boolean isViewCycleAccessSupported() {
    return _isViewCycleAccessSupported.get();
  }

  @Override
  public void setViewCycleAccessSupported(boolean isViewCycleAccessSupported) {
    _clientLock.lock();
    try {
      _isViewCycleAccessSupported.set(isViewCycleAccessSupported);
      _mergingViewProcessListener.setLatestResultCycleRetained(isViewCycleAccessSupported);
      if (!isViewCycleAccessSupported) {
        getLatestCycleRetainer().replaceRetainedCycle(null);
      }
    } finally {
      _clientLock.unlock();
    }
  }

  @Override
  public void setViewProcessContextMap(Map<String, String> context) {

    if (isAttached()) {
      throw new IllegalStateException("Unable to set process context whilst attached to a view process");
    }
    _viewProcessContextMap = context;
  }

  @Override
  public EngineResourceReference<? extends ViewCycle> createLatestCycleReference() {
    if (!isViewCycleAccessSupported()) {
      throw new UnsupportedOperationException("Access to computation cycles is not supported from this client");
    }
    ViewComputationResultModel latestResult = getLatestResult();
    if (latestResult == null) {
      return null;
    }
    return _viewProcessor.getViewCycleManager().createReference(latestResult.getViewCycleId());
  }

  @Override
  public EngineResourceReference<? extends ViewCycle> createCycleReference(UniqueId cycleId) {
    if (!isViewCycleAccessSupported()) {
      throw new UnsupportedOperationException("Access to computation cycles is not supported from this client");
    }
    return _viewProcessor.getViewCycleManager().createReference(cycleId);
  }

  //-------------------------------------------------------------------------
  @Override
  public void setMinimumLogMode(ExecutionLogMode minimumLogMode, Set<Pair<String, ValueSpecification>> resultSpecifications) {
    _clientLock.lock();
    try {
      checkAttached();
      Set<Pair<String, ValueSpecification>> affected = new HashSet<>(resultSpecifications);
      switch (minimumLogMode) {
        case INDICATORS:
          affected.retainAll(_elevatedLogSpecs);
          _elevatedLogSpecs.removeAll(affected);
          break;
        case FULL:
          affected.removeAll(_elevatedLogSpecs);
          _elevatedLogSpecs.addAll(affected);
          break;
      }
      if (!affected.isEmpty()) {
        getViewProcessor().getViewProcessForClient(getUniqueId()).getExecutionLogModeSource().setMinimumLogMode(minimumLogMode, affected);
      }
    } finally {
      _clientLock.unlock();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void shutdown() {
    _clientLock.lock();
    try {
      if (_state == ViewClientState.TERMINATED) {
        return;
      }
      detachFromViewProcess();
      getViewProcessor().removeViewClient(getUniqueId());
      _mergingViewProcessListener.terminate();
      _state = ViewClientState.TERMINATED;
      ViewResultListener listener = _userResultListener.get();
      if (listener != null) {
        listener.clientShutdown(null);
      }
    } finally {
      _clientLock.unlock();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ViewClient[" + getUniqueId() + "]";
  }

  //-------------------------------------------------------------------------
  private void processCompleted() {
    _completionLatch.countDown();
  }

  private void checkAttached() {
    if (!isAttached()) {
      throw new IllegalStateException("This method is not valid on a detached view client.");
    }
  }

  private void checkNotAttached() {
    if (isAttached()) {
      throw new IllegalStateException("This method is not valid on an attached view client.");
    }
  }

  private void checkNotTerminated() {
    if (getState() == ViewClientState.TERMINATED) {
      throw new IllegalStateException("The client has been terminated. It is not possible to use a terminated client.");
    }
  }

  private EngineResourceRetainer getLatestCycleRetainer() {
    return _latestCycleRetainer;
  }

  /**
   * Updates the latest result.
   * 
   * @param result the new result
   * @return true if the new result was the first
   */
  private boolean updateLatestResult(ViewComputationResultModel result) {
    if (isViewCycleAccessSupported()) {
      getLatestCycleRetainer().replaceRetainedCycle(result.getViewCycleId());
    }
    ViewComputationResultModel oldResult = _latestResult.getAndSet(result);
    return oldResult == null;
  }

  private void updateLatestCompiledViewDefinition(CompiledViewDefinition compiledViewDefinition) {
    _latestCompiledViewDefinition.set(compiledViewDefinition);
  }

  private boolean isFullResultRequired(ViewResultMode resultMode, boolean isFirstResult) {
    switch (resultMode) {
      case BOTH:
      case FULL_ONLY:
        return true;
      case FULL_THEN_DELTA:
        return isFirstResult;
      default:
        return false;
    }
  }

  private boolean isDeltaResultRequired(ViewResultMode resultMode, boolean isFirstResult) {
    switch (resultMode) {
      case BOTH:
      case DELTA_ONLY:
        return true;
      case FULL_THEN_DELTA:
        return !isFirstResult;
      default:
        return false;
    }
  }

}
