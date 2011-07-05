/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Cancellable;

/**
 * Builds a dependency graph that describes how to calculate values that will satisfy a given
 * set of value requirements.
 */
public class DependencyGraphBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  // DON'T CHECK IN WITH =true
  private static final boolean NO_BACKGROUND_THREADS = false;

  private static int s_defaultMaxAdditionalThreads = NO_BACKGROUND_THREADS ? 0 : Runtime.getRuntime().availableProcessors();

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
  private volatile int _maxAdditionalThreads = s_defaultMaxAdditionalThreads;

  private final ExecutorService _executor = Executors.newCachedThreadPool(new ThreadFactory() {

    @Override
    public Thread newThread(final Runnable r) {
      final Thread t = new Thread(r) {
        @Override
        public void start() {
          super.start();
          s_logger.info("Starting background thread {}", this);
        }
      };
      t.setDaemon(true);
      synchronized (_activeJobs) {
        t.setName(DependencyGraphBuilder.this.toString() + "-" + (_nextJobThreadId++));
      }
      return t;
    }

  });

  // State:
  private final int _objectId = s_nextObjectId.incrementAndGet();
  private final ConcurrentMap<ValueRequirement, ConcurrentMap<ResolveTask, ResolveTask>> _requirements = new ConcurrentHashMap<ValueRequirement, ConcurrentMap<ResolveTask, ResolveTask>>();
  private final ConcurrentMap<ValueSpecification, ConcurrentMap<ResolveTask, ResolvedValueProducer>> _specifications =
      new ConcurrentHashMap<ValueSpecification, ConcurrentMap<ResolveTask, ResolvedValueProducer>>();
  private final AtomicInteger _activeJobCount = new AtomicInteger();
  private final Set<Job> _activeJobs = new HashSet<Job>();
  private final Queue<ResolveTask> _runQueue = new ConcurrentLinkedQueue<ResolveTask>();
  private final Collection<Throwable> _exceptions = new ConcurrentLinkedQueue<Throwable>();
  private final Set<DependencyNode> _graphNodes = Collections.synchronizedSet(new HashSet<DependencyNode>());
  private final Map<ValueRequirement, ValueSpecification> _terminalOutputs = new ConcurrentHashMap<ValueRequirement, ValueSpecification>();

  // TODO: we could have different run queues for the different states. When the PENDING one is considered, a bulk lookup operation can then be done

  // TODO: The number of active jobs for thread spawn decisions could come from a variable rather shared among a set of builders that themselves be
  // running concurrently (e.g. different configurations) so that the total background threads for the group is pegged, NOT the count per builder which
  // may be too much.

  // TODO: We should use an external execution framework rather than the one here; there are far better (and probably more accurate) implementations of
  // the algorithm in other projects I've worked on. 

  /**
   * Incrementing identifier for background thread identifiers. Users must hold the _activeJobs monitor.
   */
  private int _nextJobThreadId;

  private final ResolvedValueCallback _getTerminalValuesCallback = new ResolvedValueCallback() {

    private final ConcurrentMap<ValueSpecification, DependencyNode> _spec2Node = new ConcurrentHashMap<ValueSpecification, DependencyNode>();
    private final ConcurrentMap<ParameterizedFunction, ConcurrentMap<ComputationTarget, List<DependencyNode>>> _func2target2nodes =
        new ConcurrentHashMap<ParameterizedFunction, ConcurrentMap<ComputationTarget, List<DependencyNode>>>();

    @Override
    public void failed(final ValueRequirement value) {
      // TODO: extract some useful exception state from somewhere?
      s_logger.error("Couldn't resolve {}", value);
    }

    private List<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final ComputationTarget target) {
      ConcurrentMap<ComputationTarget, List<DependencyNode>> target2nodes = _func2target2nodes.get(function);
      if (target2nodes == null) {
        target2nodes = new ConcurrentHashMap<ComputationTarget, List<DependencyNode>>();
        final ConcurrentMap<ComputationTarget, List<DependencyNode>> existing = _func2target2nodes.putIfAbsent(function, target2nodes);
        if (existing != null) {
          target2nodes = existing;
        }
      }
      List<DependencyNode> nodes = target2nodes.get(target);
      if (nodes == null) {
        nodes = new ArrayList<DependencyNode>();
        final List<DependencyNode> existing = target2nodes.putIfAbsent(target, nodes);
        if (existing != null) {
          nodes = existing;
        }
      }
      return nodes;
    }

    private boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
      for (ValueSpecification a : as) {
        if (bs.contains(a)) {
          // Exact match
          continue;
        }
        for (ValueSpecification b : bs) {
          if (a.getValueName() == b.getValueName()) {
            // Match the name, but other data wasn't exact so reject
            return true;
          }
        }
      }
      return false;
    }

    private boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
      return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
    }

    private DependencyNode getOrCreateNode(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue) {
      s_logger.debug("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
      final List<DependencyNode> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getComputationTarget());
      final DependencyNode node;
      synchronized (nodes) {
        DependencyNode useExisting = null;
        // [PLAT-346] Here is a good spot to tackle PLAT-346; what do we merge into a single node, and which outputs
        // do we discard if there are multiple functions that can produce them.
        for (DependencyNode existingNode : nodes) {
          if (mismatchUnion(existingNode.getOutputValues(), resolvedValue.getFunctionOutputs())) {
            s_logger.debug("Can't reuse {} for {}", existingNode, resolvedValue);
          } else {
            s_logger.debug("Reusing {} for {}", existingNode, resolvedValue);
            useExisting = existingNode;
            break;
          }
        }
        if (useExisting != null) {
          node = useExisting;
        } else {
          node = new DependencyNode(resolvedValue.getComputationTarget());
          node.setFunction(resolvedValue.getFunction());
          nodes.add(node);
        }
      }
      for (ValueSpecification output : resolvedValue.getFunctionOutputs()) {
        node.addOutputValue(output);
      }
      for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
        node.addInputValue(input);
        DependencyNode inputNode = _spec2Node.get(input);
        if (inputNode != null) {
          s_logger.debug("Found node {} for input {}", inputNode, input);
          node.addInputNode(inputNode);
        } else {
          s_logger.debug("Finding node productions for {}", input);
          final ConcurrentMap<ResolveTask, ResolvedValueProducer> resolver = _specifications.get(input);
          if (resolver != null) {
            for (Map.Entry<ResolveTask, ResolvedValueProducer> resolvedEntry : resolver.entrySet()) {
              resolvedEntry.getValue().addCallback(new ResolvedValueCallback() {

                @Override
                public void failed(final ValueRequirement value) {
                  s_logger.warn("Failed production for {} ({})", input, value);
                }

                @Override
                public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
                  final DependencyNode inputNode = getOrCreateNode(valueRequirement, resolvedValue);
                  node.addInputNode(inputNode);
                }

              });
            }
          } else {
            s_logger.warn("No registered node production for {}", input);
          }
        }
      }
      final DependencyNode existingNode = _spec2Node.putIfAbsent(resolvedValue.getValueSpecification(), node);
      if (existingNode == null) {
        s_logger.debug("Adding {} to graph set", node);
        _graphNodes.add(node);
        return node;
      }
      return existingNode;
    }

    @Override
    public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
      s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
      getOrCreateNode(valueRequirement, resolvedValue);
      _terminalOutputs.put(valueRequirement, resolvedValue.getValueSpecification());
    }

    @Override
    public String toString() {
      return "TerminalValueCallback";
    }

  };

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
    startBackgroundBuild();
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
    final ResolvedValueProducer resolvedValue = resolveRequirement(requirement, null);
    resolvedValue.addCallback(_getTerminalValuesCallback);
    // If the run-queue was empty, we won't have started a thread, so double check 
    startBackgroundConstructionJob();
  }

  /**
   * For compatability with DependencyGraphBuilderFunctionalIntegrationTest in OG-Integration. When
   * branch is merged with the mainline, remove this.
   * 
   * @param requirement requirement to add, not {@code null}
   * @deprecated update OG-Integration and remove this when the branch is merged
   */
  @Deprecated
  protected void addTargetImpl(final ValueRequirement requirement) {
    final ResolvedValueProducer resolvedValue = resolveRequirement(requirement, null);
    resolvedValue.addCallback(_getTerminalValuesCallback);
    startBackgroundConstructionJob();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();
    resolvedValue.addCallback(new ResolvedValueCallback() {

      @Override
      public void failed(final ValueRequirement value) {
        exception.set(new UnsatisfiableDependencyGraphException(value));
      }

      @Override
      public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
        exception.set(null);
      }

    });
    try {
      latch.await(1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    RuntimeException ex = exception.get();
    if (ex != null) {
      throw ex;
    }
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
      final ResolvedValueProducer resolvedValue = resolveRequirement(requirement, null);
      resolvedValue.addCallback(_getTerminalValuesCallback);
    }
    // If the run-queue was empty, we may not have started enough threads, so double check 
    startBackgroundConstructionJob();
  }

  private final class RequirementResolver extends AggregateResolvedValueProducer {

    private final ResolveTask _parentTask;
    private final Set<ResolveTask> _tasks = new HashSet<ResolveTask>();

    public RequirementResolver(final ValueRequirement valueRequirement, final ResolveTask parentTask) {
      super(valueRequirement);
      s_logger.debug("Created requirement resolver {}/{}", valueRequirement, parentTask);
      _parentTask = parentTask;
    }

    protected void addTask(final ResolveTask task) {
      if (_tasks.add(task)) {
        addProducer(task);
      }
    }

    @Override
    protected void finished() {
      boolean addFallback = false;
      synchronized (this) {
        if (getPendingTasks() == 0) {
          addFallback = true;
          setPendingTasks(-1);
        }
      }
      if (addFallback) {
        final ResolveTask task = getOrCreateTaskResolving(getValueRequirement(), _parentTask);
        if (_tasks.add(task)) {
          task.addCallback(this);
        } else {
          super.finished();
        }
      } else {
        super.finished();
      }
    }

    @Override
    public String toString() {
      return "Resolve[" + getValueRequirement() + ", " + _parentTask + "]";
    }

  }

  protected ResolvedValueProducer resolveRequirement(final ValueRequirement requirement, final ResolveTask dependent) {
    s_logger.debug("addTargetImpl {}", requirement);
    if ((dependent != null) && dependent.hasParent(requirement)) {
      s_logger.debug("Can't introduce a ValueRequirement loop");
      return new ResolvedValueProducer() {
        @Override
        public void addCallback(final ResolvedValueCallback callback) {
          callback.failed(requirement);
        }
      };
    }
    RequirementResolver resolver = null;
    for (ResolveTask task : getTasksResolving(requirement)) {
      if ((dependent != null) && dependent.hasParent(task)) {
        // Can't use this task; a loop would be introduced
        continue;
      }
      if (resolver == null) {
        resolver = new RequirementResolver(requirement, dependent);
      }
      resolver.addTask(task);
    }
    if (resolver != null) {
      resolver.start();
      return resolver;
    } else {
      s_logger.debug("Using direct resolution {}/{}", requirement, dependent);
      return getOrCreateTaskResolving(requirement, dependent);
    }
  }

  protected void addToRunQueue(final ResolveTask runnable) {
    s_logger.debug("Queuing ({})", runnable);
    final boolean dontSpawn = _runQueue.isEmpty();
    _runQueue.add(runnable);
    if (!dontSpawn) {
      startBackgroundConstructionJob();
    }
  }

  protected boolean startBackgroundConstructionJob() {
    int activeJobs = _activeJobCount.get();
    while (activeJobs < getMaxAdditionalThreads()) {
      if (_activeJobCount.compareAndSet(activeJobs, activeJobs + 1)) {
        synchronized (_activeJobs) {
          final Job job = createConstructionJob();
          _activeJobs.add(job);
          _executor.execute(job);
        }
        return true;
      }
      activeJobs = _activeJobCount.get();
    }
    return false;
  }

  /**
   * Job running thread.
   */
  protected final class Job implements Runnable, Cancellable {

    private volatile boolean _poison;

    private Job() {
    }

    @Override
    public void run() {
      s_logger.info("Building job started");
      boolean jobsLeftToRun;
      do {
        do {
          try {
            jobsLeftToRun = buildGraph();
          } catch (Throwable t) {
            s_logger.warn("Graph builder exception", t);
            postException(t);
            jobsLeftToRun = false;
          }
        } while (!_poison && jobsLeftToRun);
        s_logger.debug("Building job stopping");
        int activeJobs = _activeJobCount.decrementAndGet();
        // Watch for late arrivals in the run queue; they might have seen the old value
        // of activeJobs and not started anything.
        while (!_runQueue.isEmpty() && (activeJobs < getMaxAdditionalThreads()) && !_poison) {
          if (_activeJobCount.compareAndSet(activeJobs, activeJobs + 1)) {
            s_logger.debug("Building job resuming");
            // Note the log messages may go from "resuming" to stopped if the poison arrives between
            // the check above and the check below. This might look odd, but what the hey - they're
            // only DEBUG level messages.
            jobsLeftToRun = true;
            break;
          }
          activeJobs = _activeJobCount.get();
        }
      } while (!_poison && jobsLeftToRun);
      synchronized (_activeJobs) {
        _activeJobs.remove(this);
      }
      s_logger.info("Building job stopped");
    }

    @Override
    public boolean cancel(final boolean mayInterrupt) {
      _poison = true;
      return true;
    }

  }

  protected Job createConstructionJob() {
    return new Job();
  }

  /**
   * Main process loop, takes a runnable task and executes it. If the graph has not been built when
   * getDependencyGraph is called, the calling thread will also join this. There are additional
   * threads that also run in a pool to complete the work of the graph building.
   * 
   * @return true if there is more work still to do, false if all the work is done
   */
  protected boolean buildGraph() {
    final ResolveTask task = _runQueue.poll();
    if (task == null) {
      return false;
    }
    task.run(this);
    return true;
  }

  /**
   * Tests if the graph has been built or if work is still required. Graphs are only built in the
   * background if additional threads is set to non-zero.
   * 
   * @return {@code true} if the graph has been built, {@code false} if it is outstanding.
   */
  public boolean isGraphBuilt() {
    synchronized (_activeJobs) {
      if (!_activeJobs.isEmpty()) {
        // One or more active jobs, so can't be built yet
        return false;
      }
    }
    // no active jobs, so built if there is nothing in the run queue
    return _runQueue.isEmpty();
  }

  /**
   * Returns the dependency graph if it has been completed by background threads. If the graph has
   * not been completed it will return {@code null}. If the number of additional threads is set to
   * zero then the graph will not be built until {@link #getDependencyGraph} is called.
   * 
   * @return the graph if built or {@code null} otherwise
   */
  public DependencyGraph pollDependencyGraph() {
    if (isGraphBuilt()) {
      return createDependencyGraph();
    }
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
      for (Job job : _activeJobs) {
        job.cancel(true);
      }
      _activeJobs.clear();
    }
  }

  /**
   * If there are runnable tasks but not as many active jobs as the requested number then additional threads
   * will be started. This is called when the number of background threads is changed.
   */
  protected void startBackgroundBuild() {
    if (_runQueue.isEmpty()) {
      s_logger.info("No pending runnable tasks for background building");
    } else {
      final Iterator<ResolveTask> itr = _runQueue.iterator();
      while (itr.hasNext() && startBackgroundConstructionJob()) {
        itr.next();
      }
    }
  }

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
      s_logger.info("Building dependency graph");
      do {
        final Job job = createConstructionJob();
        synchronized (_activeJobs) {
          _activeJobs.add(job);
        }
        job.run();
        synchronized (_activeJobs) {
          if (_activeJobs.isEmpty()) {
            // We're done and there are no other jobs
            break;
          } else {
            // There are other jobs still running ...
            if (!_runQueue.isEmpty()) {
              // ... and stuff in the queue
              continue;
            }
          }
        }
        // ... but nothing in the queue for us so take a nap
        s_logger.info("Waiting for background threads");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("Interrupted during graph building", e);
        }
      } while (true);
      if (!isGraphBuilt()) {
        throw new CancellationException("Dependency graph building incomplete");
      }
    }
    return createDependencyGraph();
  }

  // DON'T CHECK IN WITH =true
  private static final boolean DEBUG_DUMP_DEPENDENCY_GRAPH = false;

  protected DependencyGraph createDependencyGraph() {
    final DependencyGraph graph = new DependencyGraph(getCalculationConfigurationName());
    s_logger.debug("Converting internal representation to dependency graph");
    for (DependencyNode node : _graphNodes) {
      graph.addDependencyNode(node);
    }
    for (ValueSpecification valueSpecification : _terminalOutputs.values()) {
      graph.addTerminalOutputValue(valueSpecification);
    }
    //graph.dumpStructureASCII(System.out);
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

  /**
   * Returns a map of the originally requested value requirements to the value specifications that were put into the
   * graph as terminal outputs. Any unsatisfied requirements will be absent from the map.
   * 
   * @return the map of requirements to value specifications, not {@code null}
   */
  public Map<ValueRequirement, ValueSpecification> getValueRequirementMapping() {
    return new HashMap<ValueRequirement, ValueSpecification>(_terminalOutputs);
  }

  /**
   * Stores an exception that should be reported to the user.
   * 
   * @param t exception to store, not {@code null}
   */
  protected void postException(final Throwable t) {
    _exceptions.add(t);
  }

  /**
   * Returns the set of exceptions that may have prevented graph construction.
   * 
   * @return the set of exceptions that were thrown by the building process, or {@code null} for none
   */
  public Collection<Throwable> getExceptions() {
    if (_exceptions.isEmpty()) {
      return null;
    } else {
      return Collections.unmodifiableCollection(_exceptions);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + _objectId;
  }

  private ResolveTask getOrCreateTaskResolving(final ValueRequirement valueRequirement, final ResolveTask parentTask) {
    final ResolveTask newTask = new ResolveTask(valueRequirement, parentTask);
    ConcurrentMap<ResolveTask, ResolveTask> tasks = _requirements.get(valueRequirement);
    if (tasks == null) {
      tasks = new ConcurrentHashMap<ResolveTask, ResolveTask>();
      tasks.put(newTask, newTask);
      tasks = _requirements.putIfAbsent(valueRequirement, tasks);
      if (tasks == null) {
        addToRunQueue(newTask);
        return newTask;
      }
    }
    final ResolveTask existingTask = tasks.putIfAbsent(newTask, newTask);
    if (existingTask == null) {
      addToRunQueue(newTask);
      return newTask;
    } else {
      return existingTask;
    }
  }

  private Set<ResolveTask> getTasksResolving(final ValueRequirement valueRequirement) {
    final ConcurrentMap<ResolveTask, ResolveTask> tasks = _requirements.get(valueRequirement);
    if (tasks == null) {
      return Collections.emptySet();
    } else {
      return tasks.keySet();
    }
  }

  public Map<ResolveTask, ResolvedValueProducer> getTasksProducing(final ValueSpecification valueSpecification) {
    final Map<ResolveTask, ResolvedValueProducer> tasks = _specifications.get(valueSpecification);
    if (tasks == null) {
      return Collections.emptyMap();
    } else {
      return tasks;
    }
  }

  public ResolvedValueProducer declareTaskProducing(final ValueSpecification valueSpecification, final ResolveTask task, final ResolvedValueProducer producer) {
    ConcurrentMap<ResolveTask, ResolvedValueProducer> tasks = _specifications.get(valueSpecification);
    if (tasks == null) {
      tasks = new ConcurrentHashMap<ResolveTask, ResolvedValueProducer>();
      tasks.put(task, producer);
      tasks = _specifications.putIfAbsent(valueSpecification, tasks);
      if (tasks == null) {
        return producer;
      }
    }
    final ResolvedValueProducer existing = tasks.putIfAbsent(task, producer);
    if (existing == null) {
      return producer;
    } else {
      return existing;
    }
  }

}
