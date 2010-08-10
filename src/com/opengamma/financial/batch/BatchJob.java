/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.fudgemsg.FudgeContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.engine.view.cache.MapViewComputationCacheSource;
import com.opengamma.engine.view.calc.BatchExecutorFactory;
import com.opengamma.engine.view.calcnode.CalculationNodeRequestReceiver;
import com.opengamma.engine.view.calcnode.FudgeJobRequestSender;
import com.opengamma.engine.view.calcnode.JobRequestSender;
import com.opengamma.engine.view.calcnode.ResultWriterFactory;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.financial.position.master.MasterPositionSource;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.security.HistoricallyFixedSecurityMaster;
import com.opengamma.financial.security.SecurityMaster;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchJob implements Job {
  
  /**
   * Used as a default "observation time" for ad hoc batches, i.e., batches that are
   * started manually by users and whose results should NOT flow to downstream
   * systems.  
   */
  public static final String AD_HOC_OBSERVATION_TIME = "AD_HOC_RUN";
  
  // --------------------------------------------------------------------------
  // Variables automatically initialized at construction time
  // --------------------------------------------------------------------------
  
  /** yyyyMMddHHmmss[Z] */
  private final DateTimeFormatter _dateTimeFormatter;
  
  /** yyyyMMdd */
  private final DateTimeFormatter _dateFormatter;
  
  /** 
   * User from the operating system - System.getProperty("user.name").
   * 
   * Not null.
   */
  private final UserPrincipal _user;
  
  // --------------------------------------------------------------------------
  // Variables YOU should set - whether by using Spring property-based injection,
  // or manually by calling setters in tests
  // --------------------------------------------------------------------------
  
  /**
   * Used to load a ViewDefinition (whose name will be given on the command line)
   */
  private MongoDBConnectionSettings _configDbConnectionSettings;
  
  /**
   * Used to load Functions (needed for building the dependency graph)
   */
  private FunctionRepository _functionRepository;

  /**
   * Used to create the SecuritySource if none is explicitly specified. Use this
   * for more control over the version and correction dates of data.
   */
  private SecurityMaster _securityMaster;

  /**
   * Used to load Securities (needed for building the dependency grapth). If not
   * specified will be constructed from the SecurityMaster.
   */
  private SecuritySource _securitySource;

  /**
   * Used to create the PositionSource if none is explicitly specified. Use this
   * for more control over the version and correction dates of data.
   */
  private PositionMaster _positionMaster;

  /**
   * Used to load Positions (needed for building the dependency graph). If not
   * specified will be constructed from the PositionMaster.
   */
  private PositionSource _positionSource;

  /**
   * Used to write stuff to the batch database
   */
  private BatchDbManager _batchDbManager;
  
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

  // --------------------------------------------------------------------------
  // Variables initialized from command line input
  // --------------------------------------------------------------------------
  
  /** 
   * Why the batch is being run. Would typically tell whether the run is an automatic/manual
   * run, and if manual, who started it and maybe why.
   * 
   * Not null.
   */
  private String _runReason;
  
  /** 
   * A label for the run. Examples: LDN_CLOSE, AD_HOC_RUN. The exact time of LDN_CLOSE could vary
   * daily due to this time being set by the head trader.
   * So one day it might be 16:32, the next 16:46, etc. 
   * 
   * Not null.
   */
  private String _observationTime;
  
  /**
   * What day's batch this is. 
   * 
   * Not null.
   */
  private LocalDate _observationDate;
  
  /**
   * Valuation time for purposes of calculating all risk figures. Often referred to 'T'
   * in mathematical formulas.
   * 
   * Not null.
   */
  private OffsetDateTime _valuationTime;
  
  /**
   * This view name references the OpenGamma configuration database.
   * The view will define the portfolio of trades the batch should be run for.
   * 
   * Not null.
   */
  private String _viewName;
  
  /**
   * Sometimes you might want to run the batch against a historical
   * view.
   * 
   * Not null.
   */
  private OffsetDateTime _viewDateTime;
  
  /**
   * The batch will run against a defined set of market data.
   * 
   * This variable tells which set exactly. The contents are 
   * similar to {@link #_observationTime}.
   * 
   * Not null.
   */
  private String _snapshotObservationTime;

  /**
   * What day's market data snapshot to use.
   * 
   * Not null.
   */
  private LocalDate _snapshotObservationDate;
  
  /**
   * If true, a new run is always created - no existing results are used. 
   * If false, the system first checks if there is already a run 
   * in the database for the same view (including same version), with the same 
   * observation date and time. If there is, that run is reused. This 
   * means that the system checks what risk figures
   * are already in the database and will try to calculate any
   * missing risk.   
   */
  private boolean _forceNewRun; // = false
  
  /**
   * Historical time used for loading entities out of PositionMaster.
   * 
   * Not null.
   */
  private OffsetDateTime _positionMasterTime;
  
  // --------------------------------------------------------------------------
  // Variables initialized during the batch run
  // --------------------------------------------------------------------------
  
  /**
   * Used to load a ViewDefinition
   */
  private ConfigMaster<ViewDefinition> _configDb;
  
  /** 
   * Object ID of ViewDefinition loaded from config DB
   */
  private String _viewOid = "UNDEFINED";
  
  /**
   * Version of ViewDefinition loaded from config DB
   */
  private int _viewVersion = -1;

  /**
   * To initialize _view, you need a ViewDefinition, but also
   * many other properties - positions loaded from a position master, 
   * securities from security master, etc.
   */
  private View _view;
  
  /**
   * Set by _batchDbManager
   */
  private Instant _creationTime;
  
  /**
   * Set by _batchDbManager
   */
  private Object _dbHandle;


  //--------------------------------------------------------------------------

  
  public BatchJob() {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyyMMddHHmmss[Z]");
    _dateTimeFormatter = builder.toFormatter();
    
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyyMMdd");
    _dateFormatter = builder.toFormatter();
    
    _user = UserPrincipal.getLocalUser();
  }
  
  private OffsetDateTime parseDateTime(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    try {
      // try first to parse as if time zone explicitly provided, e.g., 20100621162200+0000
      return _dateTimeFormatter.parse(dateTime, OffsetDateTime.rule());
    } catch (CalendricalParseException e) {
      // try to parse as if no time zone provided, e.g. 20100621162200. Use the system time zone. 
      LocalDateTime localDateTime = _dateTimeFormatter.parse(dateTime, LocalDateTime.rule());
      return OffsetDateTime.of(localDateTime, ZonedDateTime.nowSystemClock().toOffsetDateTime().getOffset());
    }
  }
  
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
  }
  
  public String getRunReason() {
    return _runReason;
  }

  public void setRunReason(String runReason) {
    _runReason = runReason;
  }

  public String getObservationTime() {
    return _observationTime;
  }

  public void setObservationTime(String observationTime) {
    _observationTime = observationTime;
  }

  public LocalDate getObservationDate() {
    return _observationDate;
  }

  public void setObservationDate(String observationDate) {
    if (observationDate == null) {
      _observationDate = null;
    } else { 
      _observationDate = _dateFormatter.parse(observationDate, LocalDate.rule());
    }
  }

  public OffsetDateTime getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(String valuationTime) {
    _valuationTime = parseDateTime(valuationTime);
  }

  public String getViewName() {
    return _viewName;
  }

  public void setViewName(String viewName) {
    _viewName = viewName;
  }

  public OffsetDateTime getViewDateTime() {
    return _viewDateTime;
  }

  public void setViewDateTime(String viewDateTime) {
    _viewDateTime = parseDateTime(viewDateTime);
  }

  public LocalDate getSnapshotObservationDate() {
    return _snapshotObservationDate;
  }

  public void setSnapshotObservationDate(String snapshotObservationDate) {
    if (snapshotObservationDate == null) {
      _snapshotObservationDate = null;
    } else {
      _snapshotObservationDate = _dateFormatter.parse(snapshotObservationDate, LocalDate.rule());
    }
  }
  
  public String getSnapshotObservationTime() {
    return _snapshotObservationTime;
  }
  
  public void setSnapshotObservationTime(String snapshotObservationTime) {
    _snapshotObservationTime = snapshotObservationTime;
  }
  
  public SnapshotId getSnapshotId() {
    return new SnapshotId(getSnapshotObservationDate(), getSnapshotObservationTime());
  }

  public boolean isForceNewRun() {
    return _forceNewRun;
  }

  public void setForceNewRun(boolean forceNewRun) {
    _forceNewRun = forceNewRun;
  }
  
  public String getOpenGammaVersion() {
    return "0.1";
  }
  
  public String getOpenGammaVersionHash() {
    return "undefined";
  }
  
  public View getView() {
    return _view;
  }
  
  public void setView(View view) {
    _view = view;
  }
  
  public Collection<ViewCalculationConfiguration> getCalculationConfigurations() {
    return getView().getDefinition().getAllCalculationConfigurations();
  }
  
  public String getViewOid() {
    return _viewOid; 
  }
  
  public int getViewVersion() {
    return _viewVersion;    
  }
  
  public Instant getCreationTime() {
    return _creationTime;
  }

  public void setCreationTime(Instant creationTime) {
    _creationTime = creationTime;
  }

  public Object getDbHandle() {
    return _dbHandle;
  }

  public void setDbHandle(Object dbHandle) {
    _dbHandle = dbHandle;
  }
  
  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  // --------------------------------------------------------------------------
  
  public MongoDBConnectionSettings getConfigDbConnectionSettings() {
    return _configDbConnectionSettings;
  }

  public void setConfigDbConnectionSettings(MongoDBConnectionSettings configDbConnectionSettings) {
    _configDbConnectionSettings = configDbConnectionSettings;
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public void setFunctionRepository(FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  public void setSecurityMaster(SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  public void setPositionMaster(PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }
  
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public BatchDbManager getBatchDbManager() {
    return _batchDbManager;
  }
  
  public void setBatchDbManager(BatchDbManager batchDbManager) {
    _batchDbManager = batchDbManager;
  }

  // --------------------------------------------------------------------------
  
  public UserPrincipal getUser() {
    return _user;
  }
  
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Run reason", getRunReason()).toString(); 
  }

  
  // --------------------------------------------------------------------------
  
  public InMemoryLKVSnapshotProvider getSnapshotProvider() {
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    
    Set<LiveDataValue> liveDataValues = _batchDbManager.getSnapshotValues(getSnapshotId());
    
    for (LiveDataValue value : liveDataValues) {
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement(
          value.getFieldName(), 
          value.getComputationTargetSpecification()));
      ComputedValue computedValue = new ComputedValue(valueSpec, value.getValue());
      snapshotProvider.addValue(computedValue);
    }
    
    snapshotProvider.snapshot(getValuationTime().toInstant().toEpochMillisLong());
    return snapshotProvider;
  }
  
  /**
   * @return Historical time used for loading entities out of PositionMaster.
   * and SecurityMaster. 
   */
  public OffsetDateTime getPositionMasterTime() {
    return _positionMasterTime; 
  }
  
  public void setPositionMasterTime(OffsetDateTime positionMasterTime) {
    _positionMasterTime = positionMasterTime;
  }
  
  public void setPositionMasterTime(String positionMasterTime) {
    _positionMasterTime = parseDateTime(positionMasterTime);
  }

  /**
   * Some static data masters may have the capability for
   * their users to modify the historical record. For the purposes of batch,
   * we want the data returned from the masters to be 100% fixed.
   * Therefore, we also have the concept of "as viewed at" time,
   * which ensures that the historical data will always be the same.
   * 
   * @return At the moment, the "as viewed at" time
   * is simply the (original) creation time of the batch. This ensures
   * that even if the batch is restarted, data will not change. 
   */
  public Instant getPositionMasterAsViewedAtTime() {
    return getCreationTime();        
  }
  
  public OffsetDateTime getSecurityMasterTime() {
    return getPositionMasterTime(); // assume this for now
  }
  
  public Instant getSecurityMasterAsViewedAtTime() {
    return getPositionMasterAsViewedAtTime(); // assume this for now
  }
  
  public void initView() {
    
    if (getViewName() == null) {
      throw new IllegalStateException("Please specify view name.");
    }
    
    if (_viewDateTime == null) {
      _viewDateTime = _valuationTime;      
    }
    
    if (_configDbConnectionSettings == null) {
      throw new IllegalStateException("Config DB connection settings not given.");            
    }
    _configDb = new MongoDBConfigMaster<ViewDefinition>(ViewDefinition.class, 
        getConfigDbConnectionSettings());

    ConfigDocument<ViewDefinition> viewDefinitionDoc = getViewByNameWithTime();
    if (viewDefinitionDoc == null) {
      throw new IllegalStateException("Config DB does not contain ViewDefinition with name " + getViewName() + " at " + _viewDateTime);      
    }
    _viewOid = viewDefinitionDoc.getOid();
    _viewVersion = viewDefinitionDoc.getVersion();
    
    InMemoryLKVSnapshotProvider snapshotProvider = getSnapshotProvider();
    
    SecuritySource securitySource = getSecuritySource();
    if (securitySource == null) {
      new HistoricallyFixedSecurityMaster(getSecurityMaster(), getSecurityMasterTime(), getSecurityMasterAsViewedAtTime());
    }
    
    PositionSource positionSource = getPositionSource();
    if (positionSource == null) {
      positionSource = new MasterPositionSource(getPositionMaster(), getPositionMasterTime(), getPositionMasterAsViewedAtTime());
    }
      
    DefaultComputationTargetResolver targetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    MapViewComputationCacheSource cacheFactory = new MapViewComputationCacheSource(getFudgeContext());
    
    FunctionExecutionContext executionContext = new FunctionExecutionContext(); 
    executionContext.setSecuritySource(securitySource);
    
    FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    compilationContext.setSecuritySource(securitySource);
    
    ResultWriterFactory resultWriterFactory = getBatchDbManager().createResultWriterFactory(this);
    
    ViewProcessorQueryReceiver viewProcessorQueryReceiver = new ViewProcessorQueryReceiver();
    ViewProcessorQuerySender viewProcessorQuerySender = new ViewProcessorQuerySender(InMemoryRequestConduit.create(viewProcessorQueryReceiver));
    CalculationNodeRequestReceiver calcRequestReceiver = new CalculationNodeRequestReceiver(cacheFactory, 
        getFunctionRepository(), 
        executionContext, 
        targetResolver, 
        viewProcessorQuerySender,
        resultWriterFactory,
        InetAddressUtils.getLocalHostName());
    JobRequestSender calcRequestSender = new FudgeJobRequestSender(InMemoryRequestConduit.create(calcRequestReceiver),
        resultWriterFactory,
        null);
    
    ThreadFactory threadFactory = new NamedThreadPoolFactory("BatchJob-" + System.currentTimeMillis(), true);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    
    ViewProcessingContext vpc = new ViewProcessingContext(new PermissiveLiveDataEntitlementChecker(), snapshotProvider, snapshotProvider, getFunctionRepository(), new DefaultFunctionResolver(
        getFunctionRepository()), positionSource, securitySource, cacheFactory, calcRequestSender, viewProcessorQueryReceiver, compilationContext, executor, new BatchExecutorFactory(),
        resultWriterFactory);
    
    _view = new View(viewDefinitionDoc.getValue(), vpc);
    _view.setPopulateResultModel(false);
    _view.init();
  }

  /**
   * 
   */
  private ConfigDocument<ViewDefinition> getViewByNameWithTime() {
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    searchRequest.setName(getViewName());
    searchRequest.setEffectiveTime(_viewDateTime.toInstant());
    ConfigSearchResult<ViewDefinition> searchResult = _configDb.search(searchRequest);
    List<ConfigDocument<ViewDefinition>> documents = searchResult.getDocuments();
    return documents.get(0);
  }
  
  public void init() {
    ZonedDateTime now = ZonedDateTime.nowSystemClock();
    
    if (_runReason == null) {
      _runReason = "Manual run started on " + 
        ZonedDateTime.nowSystemClock().toString() + " by " + 
        getUser().getUserName();                   
    }
    
    if (_observationTime == null) {
      _observationTime = AD_HOC_OBSERVATION_TIME;      
    }

    if (_observationDate == null) {
      _observationDate = LocalDate.of(now);      
    }
    
    if (_valuationTime == null) {
      _valuationTime = OffsetDateTime.of(_observationDate, now, now.getOffset()); 
    }
    
    if (_snapshotObservationDate == null) {
      _snapshotObservationDate = _observationDate;
    }
    
    if (_snapshotObservationTime == null) {
      _snapshotObservationTime = _observationTime;
    }
    
    if (_positionMasterTime == null) {
      _positionMasterTime = _valuationTime;
    }
  }
  
  public Options getOptions() {
    Options options = new Options();

    options.addOption("reason", "reason", true, 
        "Run reason. Default - Manual run started on {yyyy-MM-ddTHH:mm:ssZZ} by {user.name}.");
    
    options.addOption("observationtime", "observationtime", true, 
        "Observation time - for example, LDN_CLOSE. Default - " + AD_HOC_OBSERVATION_TIME + ".");
    options.addOption("observationdate", "observationdate", true, 
        "Observation date. yyyyMMdd - for example, 20100621. Default - system clock date.");
    options.addOption("valuationtime", "valuationtime", true, 
        "Valuation time. yyyyMMddHHmmss[Z] - for example, 20100621162200+0000. If no time zone (e.g., +0000) " +
        " is given, the system time zone is used. Default - system clock on observation date.");
    
    options.addOption("view", "view", true, 
        "View name in configuration database. You must specify this.");
    options.addOption("viewdatetime", "viewdatetime", true, 
        "Instant at which view should be loaded. yyyyMMddHHmmss[Z]. Default - same as valuationtime.");
    
    options.addOption("snapshotobservationtime", "snapshotobservationtime", true, 
        "Observation time of LiveData snapshot to use - for example, LDN_CLOSE. Default - same as observationtime.");
    options.addOption("snapshotobservationdate", "snapshotobservationdate", true, 
        "Observation date of LiveData snapshot to use. yyyyMMdd. Default - same as observationdate");
    
    options.addOption("forcenewrun", "forcenewrun", false, "If specified, a new run is always created " +
        "- no existing results are used. If not specified, the system first checks if there is already a run " + 
        "in the database for the given view (including the same version) with the same observation date and time. " +
        "If there is, that run is reused.");
    
    options.addOption("positionmastertime", "positionmastertime", true, 
      "Instant at which positions should be loaded. yyyyMMddHHmmss[Z]. Default - same as valuationtime.");
    
    return options;
  }
  
  public void parse(String[] args) throws CalendricalParseException, OpenGammaRuntimeException {
    CommandLine line;
    try {
      CommandLineParser parser = new PosixParser();
      line = parser.parse(getOptions(), args);
    } catch (ParseException e) {
      throw new OpenGammaRuntimeException("Could not parse command line", e);
    }

    setRunReason(line.getOptionValue("reason"));
    setObservationTime(line.getOptionValue("observationtime"));
    setObservationDate(line.getOptionValue("observationdate"));
    setValuationTime(line.getOptionValue("valuationtime"));
    setViewName(line.getOptionValue("view"));
    setViewDateTime(line.getOptionValue("viewdatetime"));
    setSnapshotObservationTime(line.getOptionValue("snapshotobservationtime"));
    setSnapshotObservationDate(line.getOptionValue("snapshotobservationdate"));
    setForceNewRun(line.hasOption("forcenewrun"));
    setPositionMasterTime(line.getOptionValue("positionmastertime"));
  }
  
  public void execute() {
    if (getView() == null) {
      initView();
    }

    _batchDbManager.startBatch(this);
    
    getView().runOneCycle(getValuationTime().toInstant().toEpochMillisLong());
    
    _batchDbManager.endBatch(this);
  }
  
  public static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.financial.batch.BatchJob [args]", options);
  }
  
  public static void main(String[] args) { // CSIGNORE
    BatchJob job = new BatchJob();
    
    try {
      job.parse(args);
      job.init();
      job.execute();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      usage(job.getOptions());
      System.exit(-1);
    }
    
    System.exit(0);
  }
  

}
