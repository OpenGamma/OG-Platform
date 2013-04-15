/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link ViewProcessWorker} that rolls work between two delegate workers to allow the secondary one to recompile a view while the first is still calculating values from the old
 * compilation.
 */
public class ParallelRecompilationViewProcessWorker implements ViewProcessWorker {

  private static final Logger s_logger = LoggerFactory.getLogger(ParallelRecompilationViewProcessWorker.class);

  private static enum WorkerAction {

    TERMINATE,

    PROCEED,

    DEFER,

    BLOCK

  };

  /**
   * Base class of the context used by the delegate workers.
   */
  protected abstract class AbstractViewProcessWorkerContext implements ViewProcessWorkerContext {

    private final int _id;
    private final ViewCycleExecutionSequence _sequence;
    private CompiledViewDefinitionWithGraphs _lastCompiled;
    private ViewProcessWorker _worker;
    private WorkerAction _action;
    private ViewExecutionDataProvider _deferredCompilation;
    private ViewCycleMetadata _deferredCycleStarted;

    public AbstractViewProcessWorkerContext(final ViewExecutionOptions options) {
      _id = _nextWorkerId++;
      _sequence = options.getExecutionSequence().copy();
      _worker = getDelegate().createWorker(this, options, getViewDefinition());
      // Discard the first item from the sequence
      _sequence.poll(options.getDefaultExecutionOptions());
    }

    protected abstract AbstractViewProcessWorkerContext newInstance(ViewExecutionOptions options);

    protected AbstractViewProcessWorkerContext createSecondaryWorker() {
      return createSecondaryWorker(_sequence);
    }

    protected AbstractViewProcessWorkerContext createSecondaryWorker(final ViewCycleExecutionSequence sequence) {
      return newInstance(getOptions(sequence));
    }

    protected boolean isCompiled() {
      return _lastCompiled != null;
    }

    /**
     * Called on a secondary context to indicate a primary is about to start a cycle.
     * 
     * @return {@code TERMINATE} to terminate the primary (must unblock this one), or {@code PROCEED} to allow the primary to continue (and start calculating)
     */
    protected abstract WorkerAction primaryCycleStarted();

    /**
     * Called on a primary context to indicate a secondary is about to start a cycle.
     * 
     * @return {@code PROCEED} to allow the secondary to continue (may terminate this one), {@code IGNORE} to discard/defer the notification, or {@code BLOCK} to block the secondary
     */
    protected abstract WorkerAction secondaryCycleStarted();

    /**
     * Called on a secondary context to indicate a primary has completed a fragment.
     * 
     * @return {@code TERMINATE} to terminate the primary (must unblock this one), or {@code PROCEED} to allow the primary to continue (and post its result)
     */
    protected abstract WorkerAction primaryCycleFragmentCompleted();

    /**
     * Called on a secondary context to indicate a primary has completed a cycle.
     * 
     * @return {@code TERMINATE} to terminate the primary (must unblock this one), or {@code PROCEED} to allow the primary to continue (and post its result)
     */
    protected abstract WorkerAction primaryCycleCompleted();

    protected synchronized WorkerAction block() {
      s_logger.debug("Blocking worker {}", this);
      WorkerAction action = _action;
      while (action == null) {
        try {
          wait();
          action = _action;
        } catch (InterruptedException e) {
          s_logger.debug("Interrupted", e);
          action = WorkerAction.TERMINATE;
        }
      }
      _action = null;
      s_logger.debug("Unblocked {} with action {}", this, action);
      return action;
    }

    protected synchronized void unblock(final WorkerAction action) {
      s_logger.debug("Unblocking worker {} with {}", this, action);
      _action = action;
      notifyAll();
    }

    protected void terminate() {
      s_logger.debug("Terminating delegate");
      _worker.terminate();
    }

    protected void deferredActions() {
      if (_deferredCompilation != null) {
        try {
          getContext().viewDefinitionCompiled(_deferredCompilation, _lastCompiled);
        } finally {
          _deferredCompilation = null;
        }
      }
      if (_deferredCycleStarted != null) {
        try {
          getContext().cycleStarted(_deferredCycleStarted);
        } finally {
          _deferredCycleStarted = null;
        }
      }
    }

    protected void notifyCycleStarted(ViewCycleMetadata cycleMetadata) {
      deferredActions();
      getContext().cycleStarted(cycleMetadata);
    }

    protected void notifyCycleFragmentCompleted(ViewComputationResultModel result, ViewDefinition viewDefinition) {
      getContext().cycleFragmentCompleted(result, viewDefinition);
    }

    protected void notifyCycleCompleted(ViewCycle cycle) {
      getContext().cycleCompleted(cycle);
    }

    protected void notifyCycleExecutionFailed(ViewCycleExecutionOptions options, Exception exception) {
      getContext().cycleExecutionFailed(options, exception);
    }

    // ViewProcessWorkerContext

    @Override
    public final ViewProcessContext getProcessContext() {
      return getContext().getProcessContext();
    }

    @Override
    public final void viewDefinitionCompiled(ViewExecutionDataProvider dataProvider, CompiledViewDefinitionWithGraphs compiled) {
      if (ParallelRecompilationViewProcessWorker.this.viewDefinitionCompiled(this, compiled)) {
        _deferredCompilation = dataProvider;
      } else {
        s_logger.debug("Terminating delegate");
        _worker.terminate();
      }
    }

    @Override
    public final void viewDefinitionCompilationFailed(Instant compilationTime, Exception exception) {
      s_logger.info("View definition compilation failure");
      try {
        getContext().viewDefinitionCompilationFailed(compilationTime, exception);
      } finally {
        workerCompleted();
      }
    }

    @Override
    public final void cycleStarted(ViewCycleMetadata cycleMetadata) {
      WorkerAction action = ParallelRecompilationViewProcessWorker.this.cycleStarted(this);
      while (action == WorkerAction.BLOCK) {
        action = block();
      }
      if (action == WorkerAction.TERMINATE) {
        terminate();
        return;
      }
      if (action != WorkerAction.DEFER) {
        notifyCycleStarted(cycleMetadata);
      } else {
        _deferredCycleStarted = cycleMetadata;
      }
    }

    @Override
    public final void cycleFragmentCompleted(ViewComputationResultModel result, ViewDefinition viewDefinition) {
      WorkerAction action = ParallelRecompilationViewProcessWorker.this.cycleFragmentCompleted(this);
      while (action == WorkerAction.BLOCK) {
        action = block();
      }
      if (action == WorkerAction.TERMINATE) {
        terminate();
        return;
      }
      if (action != WorkerAction.DEFER) {
        notifyCycleFragmentCompleted(result, viewDefinition);
      }
    }

    @Override
    public final void cycleCompleted(ViewCycle cycle) {
      WorkerAction action = ParallelRecompilationViewProcessWorker.this.cycleCompleted(this);
      while (action == WorkerAction.BLOCK) {
        action = block();
      }
      if (action == WorkerAction.TERMINATE) {
        terminate();
        return;
      }
      deferredActions();
      notifyCycleCompleted(cycle);
    }

    @Override
    public final void cycleExecutionFailed(ViewCycleExecutionOptions options, Exception exception) {
      WorkerAction action = ParallelRecompilationViewProcessWorker.this.cycleExecutionFailed(this);
      while (action == WorkerAction.BLOCK) {
        action = block();
      }
      if (action == WorkerAction.TERMINATE) {
        terminate();
        return;
      }
      deferredActions();
      try {
        notifyCycleExecutionFailed(options, exception);
      } finally {
        workerCompleted();
      }
    }

    @Override
    public final void workerCompleted() {
      if (ParallelRecompilationViewProcessWorker.this.workerCompleted(this)) {
        getContext().workerCompleted();
      }
    }

    // Object

    @Override
    public String toString() {
      return _id + "[" + getContext() + "]";
    }

  }

  /**
   * Context used by workers that are participating in a parallel execution strategy.
   */
  protected class ParallelExecutionContext extends AbstractViewProcessWorkerContext {

    private boolean _resultsAvailable;

    public ParallelExecutionContext(final ViewExecutionOptions options) {
      super(options);
    }

    // AbstractViewProcessWorkerContext

    @Override
    protected AbstractViewProcessWorkerContext newInstance(final ViewExecutionOptions options) {
      return new ParallelExecutionContext(options);
    }

    @Override
    protected void notifyCycleFragmentCompleted(ViewComputationResultModel result, ViewDefinition viewDefinition) {
      _resultsAvailable = true;
      super.notifyCycleFragmentCompleted(result, viewDefinition);
    }

    @Override
    protected void notifyCycleCompleted(ViewCycle cycle) {
      _resultsAvailable = true;
      super.notifyCycleCompleted(cycle);
    }

    @Override
    protected WorkerAction primaryCycleStarted() {
      // Allow primary to continue until we've got a result
      if (_resultsAvailable) {
        return WorkerAction.TERMINATE;
      } else {
        return WorkerAction.PROCEED;
      }
    }

    @Override
    protected WorkerAction secondaryCycleStarted() {
      // Secondary can always run, eventually
      return WorkerAction.DEFER;
    }

    @Override
    protected WorkerAction primaryCycleFragmentCompleted() {
      // Allow primary to continue until we've got a result
      if (_resultsAvailable) {
        return WorkerAction.TERMINATE;
      } else {
        return WorkerAction.PROCEED;
      }
    }

    @Override
    protected WorkerAction primaryCycleCompleted() {
      // Allow primary to continue until we've got a result
      if (_resultsAvailable) {
        return WorkerAction.TERMINATE;
      } else {
        return WorkerAction.PROCEED;
      }
    }

    // Object

    @Override
    public String toString() {
      return "Parallel/" + super.toString();
    }

  }

  /**
   * Context used by workers that are participating in a deferred execution strategy.
   */
  protected class DeferredExecutionContext extends AbstractViewProcessWorkerContext {

    public DeferredExecutionContext(final ViewExecutionOptions options) {
      super(options);
    }

    // AbstractViewProcessWorkerContext

    @Override
    protected AbstractViewProcessWorkerContext newInstance(final ViewExecutionOptions options) {
      return new DeferredExecutionContext(options);
    }

    @Override
    protected WorkerAction primaryCycleStarted() {
      if (isCompiled()) {
        // Terminate the primary worker, and unblock this one
        unblock(WorkerAction.PROCEED);
        return WorkerAction.TERMINATE;
      } else {
        // Allow the primary to continue until we've compiled ourselves
        return WorkerAction.PROCEED;
      }
    }

    @Override
    protected WorkerAction secondaryCycleStarted() {
      // Block the secondary worker until the primary has finished a cycle
      return WorkerAction.BLOCK;
    }

    @Override
    protected WorkerAction primaryCycleFragmentCompleted() {
      // Allow the primary to continue
      return WorkerAction.PROCEED;
    }

    @Override
    protected WorkerAction primaryCycleCompleted() {
      if (isCompiled()) {
        // Compiled and ready to run - unblock
        unblock(WorkerAction.PROCEED);
      }
      // Allow the primary worker to post its result; we'll kill it when it starts its next cycle
      return WorkerAction.PROCEED;
    }

    // Object

    @Override
    public String toString() {
      return "Deferred/" + super.toString();
    }

  }

  /**
   * Context used by workers that are participating in an immediate execution strategy.
   */
  protected class ImmediateExecutionContext extends AbstractViewProcessWorkerContext {

    public ImmediateExecutionContext(final ViewExecutionOptions options) {
      super(options);
    }

    // AbstractViewProcessWorkerContext

    @Override
    protected AbstractViewProcessWorkerContext newInstance(final ViewExecutionOptions options) {
      return new ImmediateExecutionContext(options);
    }

    @Override
    protected WorkerAction primaryCycleStarted() {
      if (isCompiled()) {
        // Terminate the primary worker
        return WorkerAction.TERMINATE;
      } else {
        // Allow the primary to continue until we've compiled ourselves
        return WorkerAction.PROCEED;
      }
    }

    @Override
    protected WorkerAction secondaryCycleStarted() {
      // Always continue - we'll kill the primary at the first opportunity
      return WorkerAction.PROCEED;
    }

    @Override
    protected WorkerAction primaryCycleFragmentCompleted() {
      if (isCompiled()) {
        // Terminate the primary worker
        return WorkerAction.TERMINATE;
      } else {
        return WorkerAction.PROCEED;
      }
    }

    @Override
    protected WorkerAction primaryCycleCompleted() {
      if (isCompiled()) {
        // Terminate the primary worker
        return WorkerAction.TERMINATE;
      } else {
        return WorkerAction.PROCEED;
      }
    }

    // Object

    @Override
    public String toString() {
      return "Immediate/" + super.toString();
    }

  }

  private final ViewProcessWorkerFactory _delegate;
  private final ViewProcessWorkerContext _context;
  private final EnumSet<ViewExecutionFlags> _flags;
  private final Integer _maxSuccessiveDeltaCycles;
  private final ViewCycleExecutionOptions _defaultExecutionOptions;

  private TargetResolverChangeListener _resolverChanges;
  private int _nextWorkerId;
  private ViewDefinition _viewDefinition;
  private AbstractViewProcessWorkerContext _primary;
  private AbstractViewProcessWorkerContext _secondary;
  private boolean _terminated;

  /**
   * Creates a new worker. The worker will not do anything; the caller must spawn a primary delegate.
   * 
   * @param delegate the factory for spawning delegate workers, not null
   * @param context the context controlling this worker, not null
   * @param options the options for this worker (and its spawned workers), not null
   * @param viewDefinition the initial view definition, not null
   */
  public ParallelRecompilationViewProcessWorker(final ViewProcessWorkerFactory delegate, final ViewProcessWorkerContext context, final ViewExecutionOptions options,
      final ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(options, "options");
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    _delegate = delegate;
    _context = context;
    _flags = options.getFlags();
    _maxSuccessiveDeltaCycles = options.getMaxSuccessiveDeltaCycles();
    _defaultExecutionOptions = options.getDefaultExecutionOptions();
    _viewDefinition = viewDefinition;
  }

  protected ViewProcessWorkerFactory getDelegate() {
    return _delegate;
  }

  protected ViewProcessWorkerContext getContext() {
    return _context;
  }

  protected EnumSet<ViewExecutionFlags> getFlags() {
    return _flags;
  }

  protected Integer getMaxSuccessiveDeltaCycles() {
    return _maxSuccessiveDeltaCycles;
  }

  protected ViewCycleExecutionOptions getDefaultExecutionOptions() {
    return _defaultExecutionOptions;
  }

  protected ViewExecutionOptions getOptions(final ViewCycleExecutionSequence sequence) {
    return new ExecutionOptions(sequence, getFlags(), getMaxSuccessiveDeltaCycles(), getDefaultExecutionOptions());
  }

  protected synchronized ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  protected AbstractViewProcessWorkerContext createParallel(final ViewExecutionOptions options) {
    return new ParallelExecutionContext(options);
  }

  public synchronized void startParallel(final ViewExecutionOptions options) {
    setPrimary(createParallel(options));
  }

  protected AbstractViewProcessWorkerContext createDeferred(final ViewExecutionOptions options) {
    return new DeferredExecutionContext(options);
  }

  public synchronized void startDeferred(final ViewExecutionOptions options) {
    setPrimary(createDeferred(options));
  }

  protected AbstractViewProcessWorkerContext createImmediate(final ViewExecutionOptions options) {
    return new ImmediateExecutionContext(options);
  }

  public synchronized void startImmediate(final ViewExecutionOptions options) {
    setPrimary(createImmediate(options));
  }

  private void setPrimary(final AbstractViewProcessWorkerContext primary) {
    if (_terminated || (_primary != null)) {
      throw new IllegalStateException();
    }
    _primary = primary;
  }

  protected void checkForRecompilation(final AbstractViewProcessWorkerContext primary, CompiledViewDefinitionWithGraphs compiled) {
    final ViewCycleExecutionSequence tailSequence = (_secondary == null) ? primary._sequence.copy() : null;
    final ViewCycleExecutionOptions nextCycle = primary._sequence.poll(getDefaultExecutionOptions());
    if (nextCycle != null) {
      final VersionCorrection vc = nextCycle.getResolverVersionCorrection();
      boolean changes = false;
      if ((vc == null) || VersionCorrection.LATEST.equals(vc)) {
        if (_resolverChanges == null) {
          _resolverChanges = new TargetResolverChangeListener() {
            @Override
            protected void onChanged() {
              // Something has changed; request a cycle on the primary and that may then do the necessary
              ViewProcessWorker worker = null;
              synchronized (this) {
                if (!_terminated && (_primary != null)) {
                  worker = _primary._worker;
                }
              }
              if (worker != null) {
                worker.requestCycle();
              }
            }
          };
          getContext().getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().addChangeListener(_resolverChanges);
        }
        final Collection<UniqueId> uids = compiled.getResolvedIdentifiers().values();
        final Set<ObjectId> oids = Sets.newHashSetWithExpectedSize(uids.size());
        for (UniqueId uid : uids) {
          final ObjectId oid = uid.getObjectId();
          changes |= _resolverChanges.isChanged(oid);
          oids.add(oid);
        }
        _resolverChanges.watchOnly(oids);
      } else {
        if (_resolverChanges != null) {
          getContext().getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().removeChangeListener(_resolverChanges);
          _resolverChanges = null;
        }
      }
      if (tailSequence == null) {
        // Already got a secondary worker; just went this far to update any change listeners
        s_logger.debug("Secondary worker already active");
        return;
      }
      if ((_resolverChanges == null) || changes) {
        s_logger.info("Starting secondary worker");
        _secondary = primary.createSecondaryWorker(tailSequence);
      }
    }
  }

  protected synchronized boolean viewDefinitionCompiled(final AbstractViewProcessWorkerContext context, final CompiledViewDefinitionWithGraphs compiled) {
    if (!_terminated) {
      if (_primary == context) {
        s_logger.info("View definition compiled by primary worker");
        _primary._lastCompiled = compiled;
        checkForRecompilation(context, compiled);
        return true;
      }
      if (_secondary == context) {
        s_logger.info("View definition compiled by secondary worker");
        CompiledViewDefinitionWithGraphs primaryCompile = _primary._lastCompiled;
        final Map<ComputationTargetReference, UniqueId> primaryResolutions = primaryCompile.getResolvedIdentifiers();
        final Map<ComputationTargetReference, UniqueId> secondaryResolutions = compiled.getResolvedIdentifiers();
        if (primaryResolutions.equals(secondaryResolutions)) {
          // Nothing has changed, the primary is still valid
          s_logger.debug("Rejecting compilation from secondary worker");
          _secondary = null;
          return false;
        } else {
          s_logger.debug("Secondary compilation valid");
          _secondary._lastCompiled = compiled;
          return true;
        }
      }
    }
    return false;
  }

  protected synchronized WorkerAction cycleStarted(final AbstractViewProcessWorkerContext context) {
    if (!_terminated) {
      if (_primary == context) {
        s_logger.info("Cycle started from primary worker");
        checkForRecompilation(context, context._lastCompiled);
        if (_secondary != null) {
          final WorkerAction action = _secondary.primaryCycleStarted();
          if (action == WorkerAction.TERMINATE) {
            s_logger.info("Promoting secondary worker");
            _primary = _secondary;
            _secondary = null;
          }
          return action;
        } else {
          return WorkerAction.PROCEED;
        }
      }
      if (_secondary == context) {
        s_logger.info("Cycle started from secondary worker");
        final WorkerAction action = _primary.secondaryCycleStarted();
        if (action == WorkerAction.TERMINATE) {
          s_logger.info("Terminating secondary worker");
          _secondary = null;
        }
        return action;
      }
    }
    return WorkerAction.TERMINATE;
  }

  protected synchronized WorkerAction cycleFragmentCompleted(final AbstractViewProcessWorkerContext context) {
    if (!_terminated) {
      if (_primary == context) {
        s_logger.debug("Cycle fragment completed from primary worker");
        if (_secondary != null) {
          WorkerAction action = _secondary.primaryCycleFragmentCompleted();
          if (action == WorkerAction.TERMINATE) {
            s_logger.info("Promoting secondary worker");
            _primary = _secondary;
            _secondary = null;
          }
          return action;
        } else {
          return WorkerAction.PROCEED;
        }
      }
      if (_secondary == context) {
        s_logger.debug("Cycle fragment completed from secondary worker");
        return WorkerAction.PROCEED;
      }
    }
    return WorkerAction.TERMINATE;
  }

  protected synchronized WorkerAction cycleCompleted(final AbstractViewProcessWorkerContext context) {
    if (!_terminated) {
      if (_primary == context) {
        s_logger.info("Cycle completed from primary worker");
        if (_secondary != null) {
          WorkerAction action = _secondary.primaryCycleCompleted();
          if (action == WorkerAction.TERMINATE) {
            s_logger.info("Promoting secondary worker");
            _primary = _secondary;
            _secondary = null;
          }
          return action;
        } else {
          return WorkerAction.PROCEED;
        }
      }
      if (_secondary == context) {
        s_logger.info("Cycle completed from secondary worker");
        return WorkerAction.PROCEED;
      }
    }
    return WorkerAction.TERMINATE;
  }

  protected synchronized WorkerAction cycleExecutionFailed(final AbstractViewProcessWorkerContext context) {
    if (!_terminated) {
      if (_primary == context) {
        s_logger.info("Cycle execution failed from primary worker");
        if (_secondary != null) {
          s_logger.info("Promoting secondary worker");
          _primary = _secondary;
          _secondary = null;
          return WorkerAction.TERMINATE;
        } else {
          return WorkerAction.PROCEED;
        }
      }
      if (_secondary == context) {
        s_logger.info("Cycle execution failed from secondary worker");
        s_logger.info("Terminating secondary worker");
        _secondary = null;
        return WorkerAction.TERMINATE;
      }
    }
    return WorkerAction.TERMINATE;
  }

  protected synchronized boolean workerCompleted(final AbstractViewProcessWorkerContext context) {
    if (context._worker == null) {
      return false;
    }
    context._worker = null;
    if (_primary == context) {
      final AbstractViewProcessWorkerContext secondary = _secondary;
      if (secondary != null) {
        s_logger.info("Primary worker completed - promoting secondary worker");
        _secondary = null;
        _primary = secondary;
        secondary.unblock(WorkerAction.PROCEED);
        return false;
      } else {
        s_logger.info("Primary worker completed - no secondary worker");
        _primary = null;
        if (_resolverChanges != null) {
          getContext().getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().removeChangeListener(_resolverChanges);
          _resolverChanges = null;
        }
        return true;
      }
    } else if (_secondary == context) {
      assert _primary != null;
      s_logger.info("Secondary worker completed");
      _secondary = null;
      return false;
    } else {
      // E.g. a late or incorrect notification from a worker 
      throw new IllegalStateException();
    }
  }

  // ViewProcessWorker

  @Override
  public boolean triggerCycle() {
    do {
      ViewProcessWorker primary = null;
      synchronized (this) {
        if (!_terminated && (_primary != null)) {
          primary = _primary._worker;
        }
      }
      if (primary != null) {
        s_logger.debug("Triggering cycle on primary worker {}", primary);
        if (primary.requestCycle()) {
          return true;
        } else {
          synchronized (this) {
            if (!_terminated && (primary == _primary)) {
              s_logger.debug("Primary worker unable to handle request");
              return false;
            }
          }
          s_logger.debug("Primary worker has terminated; repeating request");
          continue;
        }
      } else {
        s_logger.debug("Ignoring triggerCycle on terminated worker");
        return false;
      }
    } while (true);
  }

  @Override
  public boolean requestCycle() {
    do {
      ViewProcessWorker primary = null;
      synchronized (this) {
        if (!_terminated && (_primary != null)) {
          primary = _primary._worker;
        }
      }
      if (primary != null) {
        s_logger.debug("Requesting cycle from primary worker {}", primary);
        if (primary.requestCycle()) {
          return true;
        } else {
          synchronized (this) {
            if (!_terminated && (primary == _primary)) {
              s_logger.debug("Primary worker unable to handle request");
              return false;
            }
          }
          s_logger.debug("Primary worker has terminated; repeating request");
          continue;
        }
      } else {
        s_logger.debug("Ignoring requestCycle on terminated worker");
        return false;
      }
    } while (true);
  }

  @Override
  public void updateViewDefinition(ViewDefinition viewDefinition) {
    s_logger.info("Updating view definition");
    ViewProcessWorker worker = null;
    synchronized (this) {
      _viewDefinition = viewDefinition;
      if (_secondary != null) {
        worker = _secondary._worker;
        _secondary = _secondary.createSecondaryWorker();
      } else if (_primary != null) {
        _secondary = _primary.createSecondaryWorker();
      }
    }
    if (worker != null) {
      s_logger.info("Terminating previous secondary worker {}", worker);
      worker.terminate();
    }
  }

  @Override
  public void terminate() {
    s_logger.info("Terminating worker(s)");
    ViewProcessWorker primary, secondary;
    synchronized (this) {
      if (_terminated) {
        s_logger.warn("Already terminated");
        return;
      }
      primary = (_primary != null) ? _primary._worker : null;
      secondary = (_secondary != null) ? _secondary._worker : null;
      _terminated = true;
    }
    if (primary != null) {
      s_logger.debug("Terminating primary worker {}", primary);
      primary.terminate();
    }
    if (secondary != null) {
      s_logger.debug("Terminating secondary worker {}", secondary);
      secondary.terminate();
    }
  }

  @Override
  public void join() throws InterruptedException {
    s_logger.info("Joining worker(s)");
    do {
      ViewProcessWorker primary;
      synchronized (this) {
        if (_primary == null) {
          break;
        }
        primary = _primary._worker;
      }
      s_logger.debug("Joining primary worker {}", primary);
      primary.join();
      synchronized (this) {
        if (_primary == primary) {
          if (_secondary != null) {
            s_logger.debug("Promoting secondary worker {}", _secondary);
            _primary = _secondary;
            _secondary = null;
          } else {
            _primary = null;
            break;
          }
        } else {
          if (_primary == null) {
            break;
          } else {
            s_logger.debug("Primary worker {} changed to {} during wait", primary, _primary);
          }
        }
      }
    } while (true);
    s_logger.debug("Primary worker joined");
  }

  @Override
  public boolean join(final long timeout) throws InterruptedException {
    s_logger.info("Joining worker(s)");
    final long waitUntil = System.currentTimeMillis() + timeout;
    do {
      ViewProcessWorker primary;
      synchronized (this) {
        if (_primary == null) {
          break;
        }
        primary = _primary._worker;
      }
      s_logger.debug("Joining primary worker {}", primary);
      final long waitDuration = waitUntil - System.currentTimeMillis();
      if (waitDuration > 0) {
        s_logger.debug("Waiting for {}ms for primary worker {}", waitDuration, primary);
        primary.join(waitDuration);
      } else {
        s_logger.debug("Timeout elapsed joining {}", primary);
        return false;
      }
      primary.join();
      synchronized (this) {
        if (_primary == primary) {
          if (_secondary != null) {
            s_logger.debug("Promoting secondary worker {}", _secondary);
            _primary = _secondary;
            _secondary = null;
          } else {
            _primary = null;
            break;
          }
        } else {
          if (_primary == null) {
            break;
          } else {
            s_logger.debug("Primary worker {} changed to {} during wait", primary, _primary);
          }
        }
      }
    } while (true);
    s_logger.debug("Primary worker joined");
    return true;
  }

  @Override
  public synchronized boolean isTerminated() {
    return _primary == null;
  }

}
