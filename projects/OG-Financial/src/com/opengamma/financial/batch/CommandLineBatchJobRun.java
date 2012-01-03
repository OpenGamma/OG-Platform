/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static com.opengamma.util.functional.Functional.merge;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.marketdata.DummyOverrideOperationCompiler;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.live.DefaultLiveMarketDataSourceRegistry;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.SingleMarketDataProviderResolver;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.AbstractCalculationNode;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.LocalCalculationNode;
import com.opengamma.engine.view.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.permission.PermissiveViewPermissionProvider;
import com.opengamma.financial.batch.marketdata.BatchMarketDataProvider;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.memory.InMemoryViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.tuple.Pair;

/**
 * A batch run started from the command line. 
 * A CommandLineBatchJob can have several runs, one for each day
 * the batch is to be run. This is useful for example if a client wants to run
 * a historical restatement for 2 years because they have fixed a bug in their
 * pricing models.
 */
public class CommandLineBatchJobRun extends BatchJobRun {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineBatchJobRun.class);

  /**
   * The job this run belongs to.
   */
  private final CommandLineBatchJob _job;
  /**
   * The view processor
   */
  private ViewProcessor _viewProcessor;
  /**
   * The compiled view definition
   */
  private CompiledViewDefinition _compiledViewDefinition;
  /**
   * What day's market data snapshot to use. 99.9% of the time will be the same as
   * _observationDate.
   */
  private final LocalDate _snapshotObservationDate;
  /**
   * Valuation time for purposes of calculating all risk figures.
   * Often referred to as 'T' in mathematical formulas.
   * Not null.
   */
  private final Instant _valuationTime;
  /** 
   * Historical time used for loading entities out of Config DB.
   */
  private final Instant _configDbTime;
  /**
   * Historical time used for loading entities out of PositionMaster,
   * SecurityMaster, etc.
   */
  private final Instant _staticDataTime;

  private final Map<ComputationTargetSpecification, ComputationTarget> _spec2Target = new HashMap<ComputationTargetSpecification, ComputationTarget>();

  // --------------------------------------------------------------------------
  // Variables initialized during the batch run
  // --------------------------------------------------------------------------
  
  /**
   * The ViewDefinition (more precisely, a representation of it in the config DB)
   */
  private ConfigDocument<ViewDefinition> _viewDefinitionConfig;
  /**
   * Whether the run failed due to unexpected exception
   */
  private boolean _failed;

  /**
   * Creates a simple instance based on the creation time, useful for test cases.
   * 
   * @param job  the command line data, not null
   */
  public CommandLineBatchJobRun(CommandLineBatchJob job) {
    this(job,
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate());
  }

  /**
   * Creates an batch job.
   * 
   * @param job  the command line data, not null
   * @param observationDate  the observation date, not null
   * @param snapshotObservationDate  the snapshot observation date
   * @param configDbDate  the config database date, not null
   * @param staticDataDate  the static database date, not null
   */
  public CommandLineBatchJobRun(CommandLineBatchJob job, 
      LocalDate observationDate, 
      LocalDate snapshotObservationDate,
      LocalDate configDbDate,
      LocalDate staticDataDate) {
    super(new BatchId(observationDate, job.getParameters().getObservationTime()));
    
    ArgumentChecker.notNull(job, "Batch job");
    ArgumentChecker.notNull(configDbDate, "Config DB date");
    ArgumentChecker.notNull(staticDataDate, "Static data date");

    _job = job;
    _snapshotObservationDate = snapshotObservationDate;
    
    _configDbTime = ZonedDateTime.of(
        configDbDate,
        job.getParameters().getConfigDbTimeObject(),
        job.getParameters().getTimeZoneObject()).toInstant();
    
    _staticDataTime = ZonedDateTime.of(
        staticDataDate,
        job.getParameters().getStaticDataTimeObject(),
        job.getParameters().getTimeZoneObject()).toInstant();
    
    _valuationTime = ZonedDateTime.of(
        observationDate, 
        job.getParameters().getValuationTimeObject(), 
        job.getParameters().getTimeZoneObject()).toInstant();
  }

  //-------------------------------------------------------------------------
  public ConfigDocument<ViewDefinition> getViewDefinitionConfig() {
    return _viewDefinitionConfig;
  }

  public void setViewDefinitionConfig(ConfigDocument<ViewDefinition> viewDefinitionConfig) {
    _viewDefinitionConfig = viewDefinitionConfig;
  }

  public String getViewOid() {
    return _viewDefinitionConfig.getUniqueId().getValue();
  }

  public String getViewVersion() {
    return _viewDefinitionConfig.getUniqueId().getVersion();
  }

  public Instant getConfigDbTime() {
    return _configDbTime;
  }

  public Instant getStaticDataTime() {
    return _staticDataTime;
  }

  public boolean isFailed() {
    return _failed;
  }

  public void setFailed(boolean failed) {
    _failed = failed;
  }

  public CommandLineBatchJob getJob() {
    return _job;
  }

  //-------------------------------------------------------------------------
  @Override
  public SnapshotId getSnapshotId() {
    return new SnapshotId(_snapshotObservationDate, getJob().getParameters().getSnapshotObservationTime());
  }

  @Override
  public Instant getValuationTime() {
    return _valuationTime;
  }

  @Override
  public String getRunReason() {
    return getJob().getParameters().getRunReason();
  }

  @Override
  public String getSnapshotObservationTime() {
    return getJob().getParameters().getSnapshotObservationTime();
  }

  @Override
  public Map<String, String> getJobLevelParameters() {
    return getJob().getParameters().getParameters();
  }

  @Override
  public RunCreationMode getRunCreationMode() {
    return getJob().getRunCreationMode();
  }

  @Override
  public String getOpenGammaVersion() {
    return getJob().getOpenGammaVersion();
  }

  @Override
  public Instant getCreationTime() {
    return getJob().getCreationTime().toInstant();
  }

  @Override
  public Map<String, String> getRunLevelParameters() {
    Map<String, String> parameters = super.getRunLevelParameters();
    parameters.put("configDbInstant", getConfigDbTime().toString());
    parameters.put("staticDataInstant", getStaticDataTime().toString());
    return parameters;
  }

  //-------------------------------------------------------------------------
  public MarketDataProvider createSnapshotProvider() {    
    // Initialize provider with values from batch DB
    Set<LiveDataValue> liveDataValues;
    try {
      liveDataValues = getJob().getBatchMaster().getSnapshotValues(getSnapshotId());
    } catch (IllegalArgumentException e) {
      if (getJob().getHistoricalMarketDataProvider() != null) {
        // if there is a historical data provider, that provider
        // may potentially provide all market data to run the batch,
        // so no pre-existing snapshot is required
        s_logger.info("Auto-creating snapshot " + getSnapshotId());
        getJob().getBatchMaster().createLiveDataSnapshot(getSnapshotId());
        liveDataValues = Collections.emptySet();
      } else {
        throw e;
      }
    }

    InMemoryLKVMarketDataProvider batchDbProvider = new InMemoryLKVMarketDataProvider();
    for (LiveDataValue value : liveDataValues) {
      ValueRequirement valueRequirement = new ValueRequirement(value.getFieldName(), value.getComputationTargetSpecification());
      batchDbProvider.addValue(valueRequirement, value.getValue());
    }
    
    return new BatchMarketDataProvider(this, getJob().getBatchMaster(), batchDbProvider, getJob().getHistoricalMarketDataProvider());
  }

  public void createViewDefinition() {
    if (getJob().getConfigSource() == null) {
      throw new IllegalStateException("Config Source not given.");
    }
    
    String viewName = getJob().getParameters().getViewName();
    _viewDefinitionConfig = getJob().getConfigSource().getDocumentByName(
        ViewDefinition.class, 
        viewName, 
        getConfigDbTime().toInstant());
    
    if (_viewDefinitionConfig == null) {
      throw new IllegalStateException("Could not find view definition " + viewName + " at " +
          getConfigDbTime().toInstant() + " in config db");
    }
  }

  public void createViewProcessor() {
    // REVIEW jonathan 2011-03-30 -- This implementation is way too tightly-coupled to the engine and does not map to
    // the engine API well at all. See [PLAT-1156]. 
    
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    MarketDataProvider snapshotProvider = createSnapshotProvider();
    MarketDataProviderResolver providerResolver = new SingleMarketDataProviderResolver(snapshotProvider);

    VersionCorrection vc = VersionCorrection.of(getStaticDataTime(), getOriginalCreationTime());
    SecuritySource securitySource = getJob().getSecuritySource();
    if (securitySource == null) {
      securitySource = new MasterSecuritySource(getJob().getSecurityMaster(), vc);
    }
    PositionSource positionSource = getJob().getPositionSource();
    if (positionSource == null) {
      positionSource = new MasterPositionSource(getJob().getPortfolioMaster(), getJob().getPositionMaster(), vc);
    }
    
    FunctionExecutionContext functionExecutionContext = getJob().getFunctionExecutionContext().clone();
    functionExecutionContext.setSecuritySource(securitySource);
    
    CompiledFunctionService functionCompilationService = getJob().getFunctionCompilationService().clone();
    functionCompilationService.getFunctionCompilationContext().setSecuritySource(securitySource);
    
    functionCompilationService.initialize();

    DefaultComputationTargetResolver targetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    InMemoryViewComputationCacheSource computationCache = new InMemoryViewComputationCacheSource(OpenGammaFudgeContext.getInstance());

    ViewProcessorQueryReceiver viewProcessorQueryReceiver = new ViewProcessorQueryReceiver();
    ViewProcessorQuerySender viewProcessorQuerySender = new ViewProcessorQuerySender(InMemoryRequestConduit.create(viewProcessorQueryReceiver));
    AbstractCalculationNode localNode = new LocalCalculationNode(computationCache, functionCompilationService, functionExecutionContext, targetResolver, viewProcessorQuerySender, Executors
        .newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
    JobDispatcher jobDispatcher = new JobDispatcher(new LocalNodeJobInvoker(localNode));

    DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory = getJob().getBatchMaster().createDependencyGraphExecutorFactory(this);
    
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), cacheManager);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionCompilationService);

    InMemoryViewDefinitionRepository viewDefinitionRepository = new InMemoryViewDefinitionRepository();
    viewDefinitionRepository.addViewDefinition(new AddViewDefinitionRequest(_viewDefinitionConfig.getValue()));
    
    DefaultLiveMarketDataSourceRegistry liveMarketDataSourceRegistry = new DefaultLiveMarketDataSourceRegistry();
    
    ViewProcessor viewProcessor = new ViewProcessorImpl(
        UniqueId.of("Vp", "Batch"),
        viewDefinitionRepository,
        liveMarketDataSourceRegistry,
        securitySource,
        positionSource,
        computationTargetResolver,
        functionCompilationService,
        functionResolver,
        providerResolver,
        computationCache,
        jobDispatcher,
        viewProcessorQueryReceiver,
        dependencyGraphExecutorFactory,
        new DiscardingGraphStatisticsGathererProvider(),
        new PermissiveViewPermissionProvider(),
        new DummyOverrideOperationCompiler());
        
    setViewProcessor(viewProcessor);

    // REVIEW jonathan 2011-03-30
    // Total hack to restore the old functionality for now. There is no longer the concept of initialising a view as
    // this is now done automatically as required for the next computation cycle. The compiled view is only required
    // to initialise the database, so this logic probably belongs in the engine, as part of any 'batch' components, and
    // can run before (and delay) the computation cycle.
    
    ViewCompilationServices compilationServices = new ViewCompilationServices(snapshotProvider.getAvailabilityProvider(), functionResolver,
        functionCompilationService.getFunctionCompilationContext(), computationTargetResolver, functionCompilationService.getExecutorService(), securitySource, positionSource);
    CompiledViewDefinition compiledViewDefinition = ViewDefinitionCompiler.compile(getViewDefinition(), compilationServices, getValuationTime(), VersionCorrection.LATEST);
    setCompiledViewDefinition(compiledViewDefinition);
  }

  public void setViewProcessor(ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }

  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  @Override
  public Collection<String> getCalculationConfigurations() {
    Collection<String> calcConfNames = new HashSet<String>();
    for (ViewCalculationConfiguration calcConf : getViewDefinition().getAllCalculationConfigurations()) {
      calcConfNames.add(calcConf.getName());      
    }
    return calcConfNames;
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getAllOutputs() {
    Map<ValueSpecification, Set<ValueRequirement>> outputs = new HashMap<ValueSpecification, Set<ValueRequirement>>();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledViewDefinition().getCompiledCalculationConfigurations()) {
      merge(outputs, compiledCalcConfig.getTerminalOutputSpecifications());
    }
    return outputs;
  }

  @Override
  public Set<String> getAllOutputValueNames() {
    // REVIEW jonathan 2011-05-04 -- restoring previous behaviour, but this ignores ValueProperties
    Set<String> valueRequirementNames = new HashSet<String>();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledViewDefinition().getCompiledCalculationConfigurations()) {
      for (Pair<String, ValueProperties> output : compiledCalcConfig.getTerminalOutputValues()) {
        valueRequirementNames.add(output.getFirst());
      }
    }
    return valueRequirementNames;
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllComputationTargets() {
    return _spec2Target.keySet();
  }

  @Override
  public ComputationTarget resolve(ComputationTargetSpecification spec) {
    return _spec2Target.get(spec);
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinitionConfig.getValue();
  }

  public CompiledViewDefinition getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }

  public void setCompiledViewDefinition(CompiledViewDefinition compiledViewDefinition) {
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");
    _compiledViewDefinition = compiledViewDefinition;
    _spec2Target.clear();
    for (ComputationTarget target : getCompiledViewDefinition().getComputationTargets()) {
      _spec2Target.put(target.toSpecification(), target);
    }
  }

}
