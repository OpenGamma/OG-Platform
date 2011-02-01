/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Executors;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessingContext;
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
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

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
   * What day's market data snapshot to use. 99.9% of the time will be the same as
   * _observationDate.
   */
  private final LocalDate _snapshotObservationDate;
  
  /**
   * Valuation time for purposes of calculating all risk figures. Often referred to as 'T'
   * in mathematical formulas.
   * 
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


  // --------------------------------------------------------------------------
  
  /**
   * This constructor is useful in tests.
   * 
   * @param job Batch job
   */
  public CommandLineBatchJobRun(CommandLineBatchJob job) {
    this(job,
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate());
  }
  
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
  
  // --------------------------------------------------------------------------
  
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
  
  // --------------------------------------------------------------------------
  
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

  // --------------------------------------------------------------------------
  
  public InMemoryLKVSnapshotProvider getSnapshotProvider() {
    InMemoryLKVSnapshotProvider provider;
    if (getJob().getHistoricalDataProvider() != null) {
      provider = new BatchLiveDataSnapshotProvider(this, getJob().getBatchDbManager(), getJob().getHistoricalDataProvider());
    } else {
      provider = new InMemoryLKVSnapshotProvider();
    }
    
    // Initialize provider with values from batch DB
    
    Set<LiveDataValue> liveDataValues;
    try {
      liveDataValues = getJob().getBatchDbManager().getSnapshotValues(getSnapshotId());
    } catch (IllegalArgumentException e) {
      if (getJob().getHistoricalDataProvider() != null) {
        // if there is a historical data provider, that provider
        // may potentially provide all market data to run the batch,
        // so no pre-existing snapshot is required
        s_logger.info("Auto-creating snapshot " + getSnapshotId());
        getJob().getBatchDbManager().createLiveDataSnapshot(getSnapshotId());
        liveDataValues = Collections.emptySet();
      } else {
        throw e;
      }
    }

    for (LiveDataValue value : liveDataValues) {
      ValueRequirement valueRequirement = new ValueRequirement(value.getFieldName(), value.getComputationTargetSpecification());
      provider.addValue(valueRequirement, value.getValue());
    }

    provider.snapshot(getValuationTime().toEpochMillisLong());
    
    return provider;
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

  public void createView() {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    InMemoryLKVSnapshotProvider snapshotProvider = getSnapshotProvider();
    
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

    DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory = getJob().getBatchDbManager().createDependencyGraphExecutorFactory(this);
    
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), cacheManager);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionCompilationService);
    ViewProcessingContext vpc = new ViewProcessingContext(
        new PermissiveLiveDataEntitlementChecker(), snapshotProvider, snapshotProvider,
        functionCompilationService, functionResolver, positionSource, securitySource, computationTargetResolver, computationCache,
        jobDispatcher, viewProcessorQueryReceiver, dependencyGraphExecutorFactory, new DefaultViewPermissionProvider(),
        new DiscardingGraphStatisticsGathererProvider());

    ViewImpl view = new ViewImpl(_viewDefinitionConfig.getValue(), vpc, new Timer("Batch view timer"));
    view.setPopulateResultModel(false);
    view.init(getValuationTime());
    setView(view);
  }

}
