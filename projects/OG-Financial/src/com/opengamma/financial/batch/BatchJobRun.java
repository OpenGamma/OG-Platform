/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Executors;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.builder.ToStringBuilder;
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
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewInternal;
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
import com.opengamma.financial.batch.BatchJob.RunCreationMode;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * A batch for a single day. A single BatchJob can have several runs, one for each day
 * the batch is to be run. This is useful for example if a client wants to run
 * a historical restatement for 2 years because they have fixed a bug in their
 * pricing models.
 */
public class BatchJobRun {
  
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BatchJobRun.class);
  
  // --------------------------------------------------------------------------
  // Variables initialized at construction time
  // --------------------------------------------------------------------------
  
  /**
   * The job this run belongs to.
   */
  private final BatchJob _job;
  
  /**
   * What day's batch this is. 
   */
  private final LocalDate _observationDate;
  
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
  private final ZonedDateTime _valuationTime;
  
  /**
   * Historical time used for loading entities out of Config DB.
   */
  private final ZonedDateTime _configDbTime;
  
  /**
   * Historical time used for loading entities out of PositionMaster,
   * SecurityMaster, etc.
   */
  private final ZonedDateTime _staticDataTime;
  
  // --------------------------------------------------------------------------
  // Variables initialized during the batch run
  // --------------------------------------------------------------------------
  
  /**
   * The ViewDefinition (more precisely, a representation of it in the config DB)
   */
  private ConfigDocument<ViewDefinition> _viewDefinitionConfig;

  /**
   * To initialize _view, you need a ViewDefinition, but also
   * many other properties - positions loaded from a position master, 
   * securities from security master, etc.
   */
  private ViewInternal _view;
  
  /**
   * When the run was first created in database.
   * If the first run attempt, the value is system clock
   * when the BatchJob object was created.
   * If a second, third, ..., run attempt, it's the 
   * system clock on the first run attempt.
   * <p>
   * Set by BatchDbManager. 
   */
  private Instant _originalCreationTime;

  /**
   * A handle to the database entry for this run.
   * <p>
   * Set by BatchDbManager.
   */
  private Object _dbHandle;
  
  /**
   * Whether the run failed due to unexpected exception
   */
  private boolean _failed;

  //--------------------------------------------------------------------------
  
  /**
   * This constructor is useful in tests.
   * 
   * @param job Batch job
   */
  public BatchJobRun(BatchJob job) {
    this(job,
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate());
  }
  
  public BatchJobRun(BatchJob job, 
      LocalDate observationDate, 
      LocalDate snapshotObservationDate,
      LocalDate configDbDate,
      LocalDate staticDataDate) {
    ArgumentChecker.notNull(job, "Batch job");
    ArgumentChecker.notNull(observationDate, "Observation date");
    ArgumentChecker.notNull(snapshotObservationDate, "Snapshot observation date");
    ArgumentChecker.notNull(configDbDate, "Config DB date");
    ArgumentChecker.notNull(staticDataDate, "Static data date");
    
    _job = job;
    _observationDate = observationDate;
    _snapshotObservationDate = snapshotObservationDate;
    
    _valuationTime = ZonedDateTime.of(
        observationDate, 
        job.getParameters().getValuationTimeObject(), 
        job.getParameters().getTimeZoneObject());
    
    _configDbTime = ZonedDateTime.of(
        configDbDate,
        job.getParameters().getConfigDbTimeObject(),
        job.getParameters().getTimeZoneObject());
    
    _staticDataTime = ZonedDateTime.of(
        staticDataDate,
        job.getParameters().getStaticDataTimeObject(),
        job.getParameters().getTimeZoneObject());
  }
  
  //--------------------------------------------------------------------------
  
  public String getRunReason() {
    return getJob().getParameters().getRunReason();
  }

  public String getObservationTime() {
    return getJob().getParameters().getObservationTime();
  }

  public LocalDate getObservationDate() {
    return _observationDate;
  }

  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }
  
  public ZonedDateTime getConfigDbTime() {
    return _configDbTime;
  }

  public ZonedDateTime getStaticDataTime() {
    return _staticDataTime;
  }

  public LocalDate getSnapshotObservationDate() {
    return _snapshotObservationDate;
  }

  public String getSnapshotObservationTime() {
    return getJob().getParameters().getSnapshotObservationTime();
  }

  public SnapshotId getSnapshotId() {
    return new SnapshotId(getSnapshotObservationDate(), getSnapshotObservationTime());
  }

  public Object getDbHandle() {
    return _dbHandle;
  }

  public void setDbHandle(Object dbHandle) {
    _dbHandle = dbHandle;
  }
  
  public Instant getOriginalCreationTime() {
    return _originalCreationTime;
  }

  public void setOriginalCreationTime(Instant originalCreationTime) {
    _originalCreationTime = originalCreationTime;
  }
  
  public boolean isFailed() {
    return _failed;
  }

  public void setFailed(boolean failed) {
    _failed = failed;
  }

  public BatchJob getJob() {
    return _job;
  }

  public UserPrincipal getUser() {
    return getJob().getUser();
  }
  
  public RunCreationMode getRunCreationMode() {
    return getJob().getRunCreationMode();
  }
  
  public String getOpenGammaVersion() {
    return getJob().getOpenGammaVersion();
  }
  
  public Collection<ViewCalculationConfiguration> getCalculationConfigurations() {
    return getView().getDefinition().getAllCalculationConfigurations();
  }
  
  public ConfigDocument<ViewDefinition> getViewDefinitionConfig() {
    return _viewDefinitionConfig;
  }
  
  public void setViewDefinitionConfig(ConfigDocument<ViewDefinition> viewDefinitionConfig) {
    _viewDefinitionConfig = viewDefinitionConfig;
  }

  public ViewInternal getView() {
    return _view;
  }
  
  public void setView(ViewInternal view) {
    _view = view;
  }

  public String getViewOid() {
    return _viewDefinitionConfig.getUniqueId().getValue();
  }

  public String getViewVersion() {
    return _viewDefinitionConfig.getUniqueId().getVersion();
  }
  
  public ZonedDateTime getCreationTime() {
    return getJob().getCreationTime();
  }
  
  public Map<String, String> getParameters() {
    Map<String, String> jobLevelParameters = getJob().getParameters().getParameters();
    
    Map<String, String> allParameters = new HashMap<String, String>(jobLevelParameters);
    allParameters.put("valuationInstant", getValuationTime().toInstant().toString());
    allParameters.put("configDbInstant", getConfigDbTime().toInstant().toString());
    allParameters.put("staticDataInstant", getStaticDataTime().toInstant().toString());
    
    return allParameters;
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

    provider.snapshot(getValuationTime().toInstant().toEpochMillisLong());
    
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

    SecuritySource securitySource = getJob().getSecuritySource();
    if (securitySource == null) {
      securitySource = new MasterSecuritySource(getJob().getSecurityMaster(), getStaticDataTime(), getOriginalCreationTime());
    }
    PositionSource positionSource = getJob().getPositionSource();
    if (positionSource == null) {
      positionSource = new MasterPositionSource(getJob().getPortfolioMaster(), getJob().getPositionMaster(), getStaticDataTime(), getOriginalCreationTime());
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
    _view = view;
  }

  // --------------------------------------------------------------------------
  
  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("Observation date", getObservationDate())
      .append("Observation time", getObservationTime())
      .append("Run reason", getRunReason()).toString(); 
  }

}
