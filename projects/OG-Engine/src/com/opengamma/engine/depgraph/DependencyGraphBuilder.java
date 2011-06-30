/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * Builds a dependency graph that describes how to calculate values that will satisfy a given
 * set of value requirements.
 */
public class DependencyGraphBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  // Injected Inputs:
  private String _calculationConfigurationName;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private ComputationTargetResolver _targetResolver;
  private CompiledFunctionResolver _functionResolver;
  private FunctionCompilationContext _compilationContext;

  /**
   * Number of additional threads to launch while requirements are being added or the graph is being built.
   * The total number of threads used for graph construction may be up to this value or may be one higher
   * as a thread blocked on graph construction in the call to {@link #getDependencyGraph} will join in with
   * the remaining construction.
   */
  private volatile int _maxAdditionalThreads /* = 0 */;

  // State:
  private final int _objectId = s_nextObjectId.incrementAndGet();
  private final ConcurrentMap<ValueRequirement, ResolveTask> _requirements = new ConcurrentHashMap<ValueRequirement, ResolveTask>();
  private final Set<TerminatableJob> _activeJobs = new HashSet<TerminatableJob>();

  /**
   * Incrementing identifier for background thread identifiers. Users must hold the _activeJobs monitor.
   */
  private int _nextJobThreadId;

  /**
   * @return the calculationConfigurationName
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * @param calculationConfigurationName the calculationConfigurationName to set
   */
  public void setCalculationConfigurationName(String calculationConfigurationName) {
    _calculationConfigurationName = calculationConfigurationName;
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  public void setLiveDataAvailabilityProvider(LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  /**
   * @return the functionResolver
   */
  public CompiledFunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  /**
   * @param functionResolver the functionResolver to set
   */
  public void setFunctionResolver(CompiledFunctionResolver functionResolver) {
    _functionResolver = functionResolver;
  }

  /**
   * @return the targetResolver
   */
  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  /**
   * @param targetResolver the targetResolver to set
   */
  public void setTargetResolver(ComputationTargetResolver targetResolver) {
    _targetResolver = targetResolver;
  }

  /**
   * @return the compilationContext
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  /**
   * @param compilationContext the compilationContext to set
   */
  public void setCompilationContext(FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }

  public int getMaxAdditionalThreads() {
    return _maxAdditionalThreads;
  }

  /**
   * Sets the maximum number of background threads to use for graph building. Set to zero to disable
   * background building. When set to a non-zero amount, if there is additional pending work jobs may
   * be started.
   * 
   * @param maxAdditionalThreads maximum number of background threads to use
   */
  public void setMaxAdditionalThreads(final int maxAdditionalThreads) {
    ArgumentChecker.isTrue(maxAdditionalThreads >= 0, "maxAdditionalThreads");
    _maxAdditionalThreads = maxAdditionalThreads;
    if (maxAdditionalThreads > 0) {
      startBackgroundBuild(maxAdditionalThreads);
    }
  }

  protected void checkInjectedInputs() {
    ArgumentChecker.notNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getFunctionResolver(), "functionResolver");
    ArgumentChecker.notNullInjected(getTargetResolver(), "targetResolver");
    ArgumentChecker.notNullInjected(getCalculationConfigurationName(), "calculationConfigurationName");
  }

  /**
   * Adds a target requirement to the graph. The requirement is queued and the call returns; construction
   * of the graph will happen on a background thread (if additional threads is non-zero), or when the
   * call to {@link #getDependencyGraph} is made. If it was not possible to satisfy the requirement that
   * must be checked after graph construction is complete.
   * 
   * @param requirement requirement to add, not {@code null}
   */
  public void addTarget(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    checkInjectedInputs();
    addTargetImpl(requirement);
  }

  /**
   * Adds target requirements to the graph. The requirements are queued and the call returns; construction
   * of the graph will happen on a background thread (if additional threads is non-zero), or when the
   * call to {@link #getDependencyGraph} is made. If it was not possible to satisfy one or more requirements
   * that must be checked after graph construction is complete.
   * 
   * @param requirements requirements to add, not {@code null} and not containing {@code null}s.
   */
  public void addTarget(Set<ValueRequirement> requirements) {
    ArgumentChecker.noNulls(requirements, "requirements");
    checkInjectedInputs();
    for (ValueRequirement requirement : requirements) {
      addTargetImpl(requirement);
    }
  }

  protected void addTargetImpl(final ValueRequirement requirement) {
    s_logger.debug("addTargetImpl {}", requirement);
    ResolveTask task = _requirements.get(requirement);
    if (task == null) {
      task = new ResolveTask(requirement);
      final ResolveTask existing = _requirements.putIfAbsent(requirement, task);
      if (existing != null) {
        task = existing;
      }
    }
    // TODO: this needs to be broken out; addTargetImpl as called by the addTarget methods is not
    // the same behavior as needed by a target added during graph traversal.

    // NOTE: Is it safe to query the size of a set without the monitor? I don't care if it isn't
    // exactly accurate in the event of the set being modified as we will repeat the check with
    // the monitor held. I just want to avoid entering a monitor unnecessarily.
    if (_activeJobs.size() < getMaxAdditionalThreads()) {
      synchronized (_activeJobs) {
        if (_activeJobs.size() < getMaxAdditionalThreads()) {
          startBackgroundConstructionJob();
        }
      }
    }
  }

  /**
   * Caller must hold the _activeJobs monitor.
   */
  private void startBackgroundConstructionJob() {
    final TerminatableJob job = createConstructionJob();
    _activeJobs.add(job);
    final Thread t = new Thread(job);
    t.setDaemon(true);
    t.setName(toString() + "-" + (_nextJobThreadId++));
    t.start();
    s_logger.info("Started background thread {}", t);
  }

  protected TerminatableJob createConstructionJob() {
    return new TerminatableJob() {

      @Override
      protected void runOneCycle() {
        if (!buildGraph()) {
          terminate();
        }
      }

      @Override
      protected void postRunCycle() {
        s_logger.info("Construction thread ended");
        synchronized (_activeJobs) {
          _activeJobs.remove(this);
        }
      }
    };
  }

  /**
   * Main process loop, takes a runnable task and executes it. If the graph has not been built when
   * getDependencyGraph is called, the calling thread will also join this. There are additional
   * threads that also run in a pool to complete the work of the graph building.
   * 
   * @return true if there is more work still to do, false if all the work is done
   */
  protected boolean buildGraph() {
    // TODO: take a runnable task and deal with it
    return false;
  }

  /**
   * Tests if the graph has been built or if work is still required. Graphs are only built in the
   * background if additional threads is set to non-zero.
   * 
   * @return {@code true} if the graph has been built, {@code false} if it is outstanding.
   */
  public boolean isGraphBuilt() {
    // TODO
    return false;
  }

  /**
   * Returns the dependency graph if it has been completed by background threads. If the graph has
   * not been completed it will return {@code null}. If the number of additional threads is set to
   * zero then the graph will not be built until {@link #getDependencyGraph} is called.
   * 
   * @return the graph if built or {@code null} otherwise
   */
  public DependencyGraph pollDependencyGraph() {
    // TODO
    return null;
  }

  /**
   * Cancels any construction threads. If background threads had been started for graph construction, they
   * will be stopped and the construction abandoned. Note that this will also reset the number of
   * additional threads to zero to prevent further threads from being started by the existing ones before
   * they terminate. If a thread is already blocked in a call to {@link getDependencyGraph} it will receive
   * a {@link CancellationException} unless the graph construction completes before the cancellation is
   * noted by that or other background threads. The cancellation is temporary, the additional threads
   * can be reset afterwards for continued background building or a subsequent call to getDependencyGraph
   * can finish the work.
   */
  public void cancelActiveBuild() {
    setMaxAdditionalThreads(0);
    synchronized (_activeJobs) {
      for (TerminatableJob job : _activeJobs) {
        job.terminate();
      }
      _activeJobs.clear();
    }
  }

  /**
   * If there are runnable tasks but not as many active jobs as the requested number then additional threads
   * will be started. This is called when the number of background threads is changed.
   * 
   * @param numThreads maximum number of active jobs desired
   */
  protected void startBackgroundBuild(final int numThreads) {
    final int runnableCount = 0; // TODO: get the size of the run-queue
    if (runnableCount == 0) {
      s_logger.info("No pending runnable tasks for background building");
    } else {
      synchronized (_activeJobs) {
        int createThreads = numThreads - _activeJobs.size();
        if (createThreads <= 0) {
          s_logger.info("Already {} background building threads running ({} requested)", _activeJobs.size(), numThreads);
          return;
        }
        if (createThreads > runnableCount) {
          createThreads = runnableCount;
        }
        s_logger.info("Creating {} additional building threads for {} runnable tasks", createThreads, runnableCount);
        for (int i = 0; i < createThreads; i++) {
          startBackgroundConstructionJob();
        }
      }
    }
  }

  // DON'T CHECK IN WITH =true
  private static final boolean DEBUG_DUMP_DEPENDENCY_GRAPH = false;

  /**
   * Returns the constructed dependency graph able to compute as may of the requirements requested as
   * possible. If graph construction has not completed, will block the caller until it has and the
   * calling thread will be used for the remaining graph construction work (which will be the full
   * graph construction if additional threads is set to zero). For a non-blocking form see
   * {@link #pollDependencyGraph}.
   * 
   * @return the graph, not {@code null}
   */
  public DependencyGraph getDependencyGraph() {
    if (!isGraphBuilt()) {
      final TerminatableJob job = createConstructionJob();
      synchronized (_activeJobs) {
        _activeJobs.add(job);
      }
      job.run();
      if (!isGraphBuilt()) {
        throw new CancellationException("Dependency graph construction incomplete");
      }
    }
    final DependencyGraph graph = new DependencyGraph(getCalculationConfigurationName());
    // TODO: populate the graph from the current state
    if (DEBUG_DUMP_DEPENDENCY_GRAPH) {
      try {
        final PrintStream ps = new PrintStream(new FileOutputStream("/tmp/dependencyGraph.txt"));
        graph.dumpStructureASCII(ps);
        ps.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return graph;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + _objectId;
  }

}
