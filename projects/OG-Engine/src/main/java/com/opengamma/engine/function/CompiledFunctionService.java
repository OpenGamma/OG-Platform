/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Combines a function repository and compiler to give access to compiled functions.
 */
public class CompiledFunctionService implements Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(CompiledFunctionService.class);

  private final FunctionRepositoryFactory _functionRepositoryFactory;
  private FunctionRepository _rawFunctionRepository;
  private FunctionRepository _initializedFunctionRepository;
  private final FunctionRepositoryCompiler _functionRepositoryCompiler;
  private final FunctionCompilationContext _functionCompilationContext;
  private final Set<FunctionDefinition> _reinitializingFunctionDefinitions = new HashSet<FunctionDefinition>();
  private final Set<ObjectId> _reinitializingFunctionRequirements = new HashSet<ObjectId>();

  /**
   * A pool executor for general use by the engine. This should be used for tasks that should saturate the available processors.
   * <p>
   * This is not null unless the service has been shutdown.
   */
  private volatile PoolExecutor _executorService;

  private final FunctionReinitializer _reinitializer = new FunctionReinitializer() {

    @Override
    public synchronized void reinitializeFunction(final FunctionDefinition function, final ObjectId identifier) {
      s_logger.debug("Re-initialize function {} on change to {}", function, identifier);
      ArgumentChecker.notNull(function, "function");
      _reinitializingFunctionDefinitions.add(function);
      _reinitializingFunctionRequirements.add(identifier);
    }

    @Override
    public synchronized void reinitializeFunction(final FunctionDefinition function, final Collection<ObjectId> identifiers) {
      s_logger.debug("Re-initialize function {} on changes to {}", function, identifiers);
      ArgumentChecker.notNull(function, "function");
      _reinitializingFunctionDefinitions.add(function);
      _reinitializingFunctionRequirements.addAll(identifiers);
    }

  };

  private static PoolExecutor createExecutorService() {
    return new PoolExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 1), "CFS");
  }

  /**
   * Creates a new instance.
   * <p>
   * This method will use a static function repository - it will reinitialize functions, but cannot respond to addition or removal of functions. Use a constructor that takes
   * {@link FunctionConfigurationSource} instead.
   * 
   * @param functionRepository the static function repository, not null
   * @param functionRepositoryCompiler the repository compiler service, not null
   * @param functionCompilationContext the function context, not null
   * @deprecated Use {@link #CompiledFunctionService(FunctionConfigurationSource,FunctionRepositoryCompiler,FunctionCompilationContext)} instead
   */
  @Deprecated
  public CompiledFunctionService(final FunctionRepository functionRepository, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext) {
    this(functionRepository, functionRepositoryCompiler, functionCompilationContext, createExecutorService());
  }

  /**
   * Creates a new instance.
   * <p>
   * This method will use a static function repository - it will reinitialize functions, but cannot respond to addition or removal of functions. Use a constructor that takes
   * {@link FunctionConfigurationSource} instead.
   * 
   * @param functionRepository the static function repository, not null
   * @param functionRepositoryCompiler the repository compiler service, not null
   * @param functionCompilationContext the function context, not null
   * @param executorService the executor service for general processor heavy operations, not null
   * @deprecated Use {@link #CompiledFunctionService(FunctionConfigurationSource,FunctionRepositoryCompiler,FunctionCompilationContext,PoolExecutor)} instead
   */
  @Deprecated
  public CompiledFunctionService(final FunctionRepository functionRepository, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext, PoolExecutor executorService) {
    this(FunctionRepositoryFactory.constructRepositoryFactory(functionRepository), functionRepositoryCompiler, functionCompilationContext, executorService);
  }

  /**
   * Creates a new instance.
   * 
   * @param functions the source of function configuration, not null
   * @param functionRepositoryCompiler the repository compiler service, not null
   * @param functionCompilationContext the function context, not null
   */
  public CompiledFunctionService(final FunctionConfigurationSource functions, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext) {
    this(functions, functionRepositoryCompiler, functionCompilationContext, createExecutorService());
  }

  /**
   * Creates a new instance.
   * 
   * @param functions the source of function configuration, not null
   * @param functionRepositoryCompiler the repository compiler service, not null
   * @param functionCompilationContext the function context, not null
   * @param executorService the executor service for general processor heavy operations, not null
   */
  public CompiledFunctionService(final FunctionConfigurationSource functions, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext, PoolExecutor executorService) {
    this(FunctionRepositoryFactory.constructRepositoryFactory(functions), functionRepositoryCompiler, functionCompilationContext, executorService);
  }

  /**
   * Creates a new instance.
   * 
   * @param functions the source of function repository to use, not null
   * @param functionRepositoryCompiler the repository compiler service, not null
   * @param functionCompilationContext the function context, not null
   */
  public CompiledFunctionService(final FunctionRepositoryFactory functions, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext) {
    this(functions, functionRepositoryCompiler, functionCompilationContext, createExecutorService());
  }

  /**
   * Creates a new instance.
   * 
   * @param functions the source of function repository to use, not null
   * @param functionRepositoryCompiler the repository compiler service, not null
   * @param functionCompilationContext the function context, not null
   * @param executorService the executor service for general processor heavy operations, not null
   */
  public CompiledFunctionService(final FunctionRepositoryFactory functions, final FunctionRepositoryCompiler functionRepositoryCompiler,
      final FunctionCompilationContext functionCompilationContext, PoolExecutor executorService) {
    ArgumentChecker.notNull(functions, "functions");
    ArgumentChecker.notNull(functionRepositoryCompiler, "functionRepositoryCompiler");
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    ArgumentChecker.notNull(executorService, "executorService");
    _functionRepositoryFactory = functions;
    _functionRepositoryCompiler = functionRepositoryCompiler;
    _functionCompilationContext = functionCompilationContext;
    _executorService = executorService;
  }

  private static final class StaticFunctionRepository implements FunctionRepository {

    private final Map<String, FunctionDefinition> _functions;

    private StaticFunctionRepository(final FunctionRepository functionRepo) {
      if (functionRepo != null) {
        final Collection<FunctionDefinition> functions = functionRepo.getAllFunctions();
        _functions = Maps.newHashMapWithExpectedSize(functions.size());
        for (FunctionDefinition function : functions) {
          _functions.put(function.getUniqueId(), function);
        }
      } else {
        _functions = new HashMap<String, FunctionDefinition>();
      }
    }

    private void remove(final FunctionDefinition function) {
      _functions.remove(function.getUniqueId());
    }

    private void add(final FunctionDefinition function) {
      _functions.put(function.getUniqueId(), function);
    }

    @Override
    public Collection<FunctionDefinition> getAllFunctions() {
      return _functions.values();
    }

    @Override
    public FunctionDefinition getFunction(final String uniqueId) {
      return _functions.get(uniqueId);
    }

  }

  protected void initializeImpl(final long initId, final Collection<FunctionDefinition> functions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Initializing {} function definitions", functions.size());
    _reinitializingFunctionDefinitions.clear();
    _reinitializingFunctionRequirements.clear();
    final StaticFunctionRepository initialized = new StaticFunctionRepository(_initializedFunctionRepository);
    final PoolExecutor.Service<FunctionDefinition> jobs = getExecutorService().createService(new CompletionListener<FunctionDefinition>() {

      @Override
      public void success(final FunctionDefinition function) {
        if (function != null) {
          synchronized (initialized) {
            initialized.add(function);
          }
        }
      }

      @Override
      public void failure(final Throwable error) {
        s_logger.warn("Couldn't initialize function", error);
        // Don't take any further action - the error has been logged and the function is not in the "initialized" set
      }

    });
    getFunctionCompilationContext().setFunctionReinitializer(_reinitializer);
    getFunctionCompilationContext().setFunctionInitId(initId);
    synchronized (initialized) {
      for (final FunctionDefinition definition : functions) {
        initialized.remove(definition);
      }
    }
    for (final FunctionDefinition definition : functions) {
      jobs.execute(new Callable<FunctionDefinition>() {
        @Override
        public FunctionDefinition call() {
          try {
            definition.init(getFunctionCompilationContext());
            return definition;
          } catch (final UnsupportedOperationException e) {
            s_logger.warn("Function {}, is not supported in this configuration - {}", definition.getUniqueId(), e.getMessage());
            s_logger.info("Caught exception", e);
            return null;
          } catch (final Exception e) {
            s_logger.error("Couldn't initialize function {}", definition.getUniqueId());
            throw new OpenGammaRuntimeException("Couldn't initialize " + definition.getShortName(), e);
          }
        }
      });
    }
    try {
      jobs.join();
    } catch (final InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("Interrupted while initializing function definitions.");
      throw new OpenGammaRuntimeException("Interrupted while initializing function definitions. ViewProcessor not safe to use.");
    }
    _initializedFunctionRepository = initialized;
    getFunctionCompilationContext().setFunctionReinitializer(null);
    timer.finished();
  }

  /**
   * Initializes all functions.
   * 
   * @return the set of object identifiers that should trigger re-initialization
   */
  public Set<ObjectId> initialize() {
    return initialize(System.currentTimeMillis());
  }

  /**
   * Initializes all functions.
   * 
   * @param initId the initialization identifier
   * @return the set of object identifiers that should trigger re-initialization
   */
  public synchronized Set<ObjectId> initialize(final long initId) {
    s_logger.info("Initializing all function definitions to {}", initId);
    _rawFunctionRepository = getFunctionRepositoryFactory().constructRepository(Instant.ofEpochMilli(initId));
    _initializedFunctionRepository = null;
    initializeImpl(initId, _rawFunctionRepository.getAllFunctions());
    return _reinitializingFunctionRequirements;
  }

  private void reinitializeImpl(final long initId) {
    final FunctionRepository newFunctionRepository = getFunctionRepositoryFactory().constructRepository(Instant.ofEpochMilli(initId));
    if (newFunctionRepository == _rawFunctionRepository) {
      // Same repository; just reinitialization of some functions
      final Set<FunctionDefinition> reinitialize = _reinitializingFunctionDefinitions;
      if (reinitialize.isEmpty()) {
        s_logger.warn("No functions registered for re-initialization");
        getFunctionCompilationContext().setFunctionInitId(initId);
      } else {
        initializeImpl(initId, new ArrayList<FunctionDefinition>(reinitialize));
      }
    } else {
      // Different repository; full initialization
      _rawFunctionRepository = newFunctionRepository;
      _initializedFunctionRepository = null;
      initializeImpl(initId, newFunctionRepository.getAllFunctions());
    }
  }

  public synchronized void reinitializeIfNeeded(final long initId) {
    if (getFunctionCompilationContext().getFunctionInitId() != initId) {
      s_logger.info("Re-initializing function definitions - was {} required {}", getFunctionCompilationContext().getFunctionInitId(), initId);
      reinitializeImpl(initId);
    }
  }

  /**
   * Re-initializes functions that requested re-initialization during their previous initialization.
   * 
   * @return the set of unique identifiers requested by any initialized functions that should trigger re-initialization
   */
  public synchronized Set<ObjectId> reinitialize() {
    long initId = System.currentTimeMillis();
    while (getFunctionCompilationContext().getFunctionInitId() == initId) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
      initId = System.currentTimeMillis();
    }
    s_logger.info("Re-initializing all function definitions to {}", initId);
    reinitializeImpl(initId);
    return _reinitializingFunctionRequirements;
  }

  /**
   * Returns the source of the underlying (raw) function repository. The repository sourced from here will be used during initialization operations to populate the {@link #getFunctionRepository} and
   * {@link #getInitializedFunctionRepository} values.
   * 
   * @return the source of the underlying function repository, not null
   */
  public FunctionRepositoryFactory getFunctionRepositoryFactory() {
    return _functionRepositoryFactory;
  }

  /**
   * Returns the underlying (raw) function repository. Definitions in the repository may or may not be properly initialized. If functions are needed that can be reliably used, use
   * {@link #getInitializedFunctionRepository} instead.
   * 
   * @return the function repository, not null
   */
  public synchronized FunctionRepository getFunctionRepository() {
    return _rawFunctionRepository;
  }

  /**
   * Returns a repository of initialized functions. This may be a subset of the underlying (raw) repository if one or more threw exceptions during their {@link FunctionDefinition#init} calls.
   * 
   * @return the function repository, not null
   */
  public synchronized FunctionRepository getInitializedFunctionRepository() {
    return _initializedFunctionRepository;
  }

  public FunctionRepositoryCompiler getFunctionRepositoryCompiler() {
    return _functionRepositoryCompiler;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  public CompiledFunctionRepository compileFunctionRepository(final long timestamp) {
    final FunctionCompilationContext context = getFunctionCompilationContext();
    // TODO: [PLAT-2761] Temporary hack until the correct version/correction is passed in
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST));
    return getFunctionRepositoryCompiler().compile(getInitializedFunctionRepository(), context, getExecutorService(), Instant.ofEpochMilli(timestamp));
  }

  public CompiledFunctionRepository compileFunctionRepository(final Instant timestamp) {
    final FunctionCompilationContext context = getFunctionCompilationContext();
    // TODO: [PLAT-2761] Temporary hack until the correct version/correction is passed in
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST));
    return getFunctionRepositoryCompiler().compile(getInitializedFunctionRepository(), context, getExecutorService(), timestamp);
  }

  public PoolExecutor getExecutorService() {
    return _executorService;
  }

  @Override
  public CompiledFunctionService clone() {
    return new CompiledFunctionService(getFunctionRepositoryFactory(), getFunctionRepositoryCompiler(), getFunctionCompilationContext().clone());
  }

  // Lifecycle

  @Override
  public synchronized void start() {
    if (_executorService == null) {
      _executorService = createExecutorService();
    }
  }

  @Override
  public void stop() {
    final PoolExecutor executor;
    synchronized (this) {
      executor = _executorService;
      _executorService = null;
    }
    if (executor != null) {
      executor.stop();
    }
  }

  @Override
  public boolean isRunning() {
    return _executorService != null;
  }

}
