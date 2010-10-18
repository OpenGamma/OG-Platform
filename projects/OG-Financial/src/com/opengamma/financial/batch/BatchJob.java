/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Executors;

import javax.time.Instant;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.CalendricalParseException;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import net.sf.ehcache.CacheManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.config.MasterConfigSource;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.livedata.HistoricalLiveDataSnapshotProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.SecuritySource;
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
import com.opengamma.financial.Currency;
import com.opengamma.financial.position.master.MasterPositionSource;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.security.master.MasterSecuritySource;
import com.opengamma.financial.security.master.SecurityMaster;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.time.DateUtil;

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchJob {

  private static final Logger s_logger = LoggerFactory.getLogger(BatchJob.class);

  // --------------------------------------------------------------------------

  /** yyyyMMddHHmmss[Z] */
  private static final DateTimeFormatter s_dateTimeFormatter;

  /** yyyyMMdd */
  private static final DateTimeFormatter s_dateFormatter;

  /** HHmmss[Z] */
  private static final DateTimeFormatter s_timeFormatter;

  static {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyyMMddHHmmss[Z]");
    s_dateTimeFormatter = builder.toFormatter();

    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyyMMdd");
    s_dateFormatter = builder.toFormatter();

    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("HHmmss[Z]");
    s_timeFormatter = builder.toFormatter();
  }

  /* package */static OffsetDateTime parseDateTime(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    try {
      // try first to parse as if time zone explicitly provided, e.g., 20100621162200+0000
      return s_dateTimeFormatter.parse(dateTime, OffsetDateTime.rule());
    } catch (CalendricalParseException e) {
      // try to parse as if no time zone provided, e.g. 20100621162200. Use the system time zone.
      LocalDateTime localDateTime = s_dateTimeFormatter.parse(dateTime, LocalDateTime.rule());
      return OffsetDateTime.of(localDateTime, ZonedDateTime.nowSystemClock().toOffsetDateTime().getOffset());
    }
  }

  /* package */static LocalDate parseDate(String date) {
    return s_dateFormatter.parse(date, LocalDate.rule());
  }

  /* package */static OffsetTime parseTime(String time) {
    return s_timeFormatter.parse(time, OffsetTime.rule());
  }

  // --------------------------------------------------------------------------
  // Variables automatically initialized at construction time
  // --------------------------------------------------------------------------

  private final ZonedDateTime _creationTime;

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
  private MasterConfigSource _configSource;

  /**
   * Used to load Functions (needed for building the dependency graph)
   */
  private CompiledFunctionService _functionCompilationService;

  /**
   * Used to create the SecuritySource if none is explicitly specified. Use this
   * for more control over the version and correction dates of data.
   */
  private SecurityMaster _securityMaster;

  /**
   * Used to load Securities (needed for building the dependency graph). If not
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

  /**
   * Stores instances of all the various interfaces required by functions during execution
   */
  private FunctionExecutionContext _functionExecutionContext;

  /**
   * Given a range of days, used to decide which days to run the batch for. Optional.
   * If not given, all days for which there is a snapshot are used.
   */
  private HolidaySource _holidaySource;
  
  /**
   * Given a range of days, used to decide which days to run the batch for. Optional.
   * If not given, all days for which there is a snapshot are used.
   */
  private Currency _holidayCurrency;
  
  /**
   * Used to populate the batch DB with market data in real time while the batch is running.
   * This means you don't need to pre-populate the batch DB with market data.
   * Optional. If not given, you need to pre-populate the
   * batch DB with all necessary market data.   
   */
  private HistoricalLiveDataSnapshotProvider _historicalDataProvider;

  // --------------------------------------------------------------------------
  // Variables initialized from command line input
  // --------------------------------------------------------------------------

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

  /**
   * The batch may be run for multiple days in sequence, therefore we need multiple runs
   */
  private final List<BatchJobRun> _runs = new ArrayList<BatchJobRun>();

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
  
  // --------------------------------------------------------------------------

  public BatchJob() {
    _user = UserPrincipal.getLocalUser();
    _creationTime = ZonedDateTime.nowSystemClock();
  }

  // --------------------------------------------------------------------------

  public String getOpenGammaVersion() {
    return "0.1";
  }

  public String getOpenGammaVersionHash() {
    return "undefined";
  }

  public ViewInternal getView() {
    return _view;
  }

  public void setView(ViewInternal view) {
    _view = view;
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

  public Collection<ViewCalculationConfiguration> getCalculationConfigurations() {
    return getView().getDefinition().getAllCalculationConfigurations();
  }

  public ConfigDocument<ViewDefinition> getViewDefinitionConfig() {
    return _viewDefinitionConfig;
  }

  public void setViewDefinitionConfig(ConfigDocument<ViewDefinition> viewDefinitionConfig) {
    _viewDefinitionConfig = viewDefinitionConfig;
  }

  public String getViewOid() {
    return _viewDefinitionConfig.getConfigId().getValue();
  }

  public int getViewVersion() {
    return _viewDefinitionConfig.getVersionNumber();
  }

  public boolean isForceNewRun() {
    return _forceNewRun;
  }

  public void setForceNewRun(boolean forceNewRun) {
    _forceNewRun = forceNewRun;
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
    return getCreationTime().toInstant();
  }

  public OffsetDateTime getSecurityMasterTime() {
    return getPositionMasterTime(); // assume this for now
  }

  public Instant getSecurityMasterAsViewedAtTime() {
    return getPositionMasterAsViewedAtTime(); // assume this for now
  }

  public MasterConfigSource getConfigSource() {
    return _configSource;
  }

  public void setConfigSource(MasterConfigSource configSource) {
    _configSource = configSource;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public void setFunctionCompilationService(CompiledFunctionService functionCompilationService) {
    _functionCompilationService = functionCompilationService;
  }

  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  public void setSecurityMaster(SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  /* package */SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * This method should only be used in tests since if you use it 
   * (instead of setSecurityMaster) you will not get historically
   * fixed securities.
   * 
   * @param securitySource Security source
   */
  /* package */void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  public void setPositionMaster(PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }

  /* package */PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * This method should only be used in tests since if you use it 
   * (instead of setPositionMaster) you will not get historically
   * fixed positions.
   * 
   * @param positionSource Position source
   */
  /* package */void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public BatchDbManager getBatchDbManager() {
    return _batchDbManager;
  }

  public void setBatchDbManager(BatchDbManager batchDbManager) {
    _batchDbManager = batchDbManager;
  }

  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public void setFunctionExecutionContext(FunctionExecutionContext executionContext) {
    _functionExecutionContext = executionContext;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }
  
  public Currency getHolidayCurrency() {
    return _holidayCurrency;
  }

  public void setHolidayCurrency(Currency holidayCurrency) {
    _holidayCurrency = holidayCurrency;
  }
  
  public HistoricalLiveDataSnapshotProvider getHistoricalDataProvider() {
    return _historicalDataProvider;
  }

  public void setHistoricalDataProvider(HistoricalLiveDataSnapshotProvider historicalDataProvider) {
    _historicalDataProvider = historicalDataProvider;
  }

  public UserPrincipal getUser() {
    return _user;
  }

  public ZonedDateTime getCreationTime() {
    return _creationTime;
  }

  public List<BatchJobRun> getRuns() {
    return Collections.unmodifiableList(_runs);
  }

  public void addRun(BatchJobRun run) {
    _runs.add(run);
  }

  private OffsetDateTime getLastValuationTime() {
    if (getRuns().isEmpty()) {
      throw new IllegalStateException("No runs defined");
    }
    BatchJobRun lastRun = getRuns().get(getRuns().size() - 1);
    return lastRun.getValuationTime();
  }

  // --------------------------------------------------------------------------

  public InMemoryLKVSnapshotProvider getSnapshotProvider(BatchJobRun run) {
    InMemoryLKVSnapshotProvider provider;
    if (_historicalDataProvider != null) {
      provider = new BatchLiveDataSnapshotProvider(run, _batchDbManager, _historicalDataProvider);
    } else {
      provider = new InMemoryLKVSnapshotProvider();
    }
    
    // Initialize provider with values from batch DB
    
    Set<LiveDataValue> liveDataValues = _batchDbManager.getSnapshotValues(run.getSnapshotId());

    for (LiveDataValue value : liveDataValues) {
      ValueRequirement valueRequirement = new ValueRequirement(value.getFieldName(), value.getComputationTargetSpecification());
      provider.addValue(valueRequirement, value.getValue());
    }

    provider.snapshot(run.getValuationTime().toInstant().toEpochMillisLong());
    
    return provider;
  }

  // --------------------------------------------------------------------------

  public void createViewDefinition() {

    if (getViewName() == null) {
      throw new IllegalStateException("Please specify view name.");
    }

    if (_viewDateTime == null) {
      _viewDateTime = getLastValuationTime();
    }
    
    if (_configSource == null) {
      throw new IllegalStateException("Config Source not given.");
    }
    
    _viewDefinitionConfig = getViewByNameWithTime();
    if (_viewDefinitionConfig == null) {
      throw new IllegalStateException("Config DB does not contain ViewDefinition with name " + getViewName() + " at " + _viewDateTime);
    }

    if (_positionMasterTime == null) {
      _positionMasterTime = _viewDateTime;
    }
  }

  public void createView(BatchJobRun run) {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    InMemoryLKVSnapshotProvider snapshotProvider = getSnapshotProvider(run);

    SecuritySource securitySource = getSecuritySource();
    if (securitySource == null) {
      securitySource = new MasterSecuritySource(getSecurityMaster(), getSecurityMasterTime(), getSecurityMasterAsViewedAtTime());
    }
    PositionSource positionSource = getPositionSource();
    if (positionSource == null) {
      positionSource = new MasterPositionSource(getPositionMaster(), getPositionMasterTime(), getPositionMasterAsViewedAtTime());
    }
    
    FunctionExecutionContext functionExecutionContext = getFunctionExecutionContext().clone();
    functionExecutionContext.setSecuritySource(securitySource);
    
    // this needs to be fixed, at the moment because of this line you can't run multiple days in parallel
    getFunctionCompilationService().getFunctionCompilationContext().setSecuritySource(securitySource);

    DefaultComputationTargetResolver targetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    InMemoryViewComputationCacheSource computationCache = new InMemoryViewComputationCacheSource(OpenGammaFudgeContext.getInstance());

    ViewProcessorQueryReceiver viewProcessorQueryReceiver = new ViewProcessorQueryReceiver();
    ViewProcessorQuerySender viewProcessorQuerySender = new ViewProcessorQuerySender(InMemoryRequestConduit.create(viewProcessorQueryReceiver));
    AbstractCalculationNode localNode = new LocalCalculationNode(computationCache, getFunctionCompilationService(), functionExecutionContext, targetResolver, viewProcessorQuerySender, Executors
        .newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
    JobDispatcher jobDispatcher = new JobDispatcher(new LocalNodeJobInvoker(localNode));

    DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory = getBatchDbManager().createDependencyGraphExecutorFactory(run);
    
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), cacheManager);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(getFunctionCompilationService());
    ViewProcessingContext vpc = new ViewProcessingContext(
        new PermissiveLiveDataEntitlementChecker(), snapshotProvider, snapshotProvider,
        getFunctionCompilationService(), functionResolver, positionSource, securitySource, computationTargetResolver, computationCache,
        jobDispatcher, viewProcessorQueryReceiver, dependencyGraphExecutorFactory, new DefaultViewPermissionProvider(),
        new DiscardingGraphStatisticsGathererProvider());

    ViewImpl view = new ViewImpl(_viewDefinitionConfig.getValue(), vpc, new Timer("Batch view timer"));
    view.setPopulateResultModel(false);
    view.init();
    _view = view;
  }

  /**
   * 
   */
  private ConfigDocument<ViewDefinition> getViewByNameWithTime() {
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    searchRequest.setName(getViewName());
    searchRequest.setVersionAsOfInstant(_viewDateTime.toInstant());
    ConfigSearchResult<ViewDefinition> searchResult = _configSource.getMaster(ViewDefinition.class).search(searchRequest);
    return searchResult.getFirstDocument();
  }

  public static Options getOptions() {
    Options options = new Options();

    options.addOption("reason", true, "Run reason. Default - Manual run started on {yyyy-MM-ddTHH:mm:ssZZ} by {user.name}.");

    options.addOption("observationtime", true, "Observation time - for example, LDN_CLOSE. Default - " + BatchJobRun.AD_HOC_OBSERVATION_TIME + ".");
    options.addOption("observationdate", true, "Observation date. yyyyMMdd - for example, 20100621. Default - system clock date.");
    options.addOption("valuationtime", true, "Valuation time. yyyyMMddHHmmss[Z] - for example, 20100621162200+0000. If no time zone (e.g., +0000) "
        + "is given, the system time zone is used. Default - system clock on observation date.");

    options.addOption("view", true, "View name in configuration database. You must specify this.");
    options.addOption("viewdatetime", true, "Instant at which view should be loaded. yyyyMMddHHmmss[Z]. Default - same as valuationtime.");

    options.addOption("snapshotobservationtime", true, "Observation time of LiveData snapshot to use - for example, LDN_CLOSE. Default - same as observationtime.");
    options.addOption("snapshotobservationdate", true, "Observation date of LiveData snapshot to use. yyyyMMdd. Default - same as observationdate");

    options.addOption("forcenewrun", false, "If specified, a new run is always created "
        + "- no existing results are used. If not specified, the system first checks if there is already a run "
        + "in the database for the given view (including the same version) with the same observation date and time. " + "If there is, that run is reused.");

    options.addOption("positionmastertime", true, "Instant at which positions should be loaded. yyyyMMddHHmmss[Z]. Default - same as viewdatetime.");

    options.addOption("daterangestart", true, "First valuation date (inclusive). If daterangestart and daterangeend are given, "
        + "observationdate and snapshotobservationdate are calculated from the range and " + "must not be given explicitly. In addition, valuationtime must be a time, "
        + "HHmmss[Z], instead of a datetime. 1. If holidaySource/holidayCurrency are not given: The batch will be run " 
        + "for those dates within the range for which there is a snapshot in the database. "
        + "If there is no snapshot, that date is simply ignored. " 
        + "2. If holidaySource and holidayCurrency are given: The batch will be run for those dates which are not weekends or holidays.");
    options.addOption("daterangeend", true, "Last valuation date (inclusive). Optional.");

    return options;
  }

  private Set<LocalDate> getDates(String dateRangeStart, String dateRangeEnd) {
    ArgumentChecker.notNull(dateRangeStart, "Date range start");
    ArgumentChecker.notNull(dateRangeStart, "Date range end");

    LocalDate startDate = parseDate(dateRangeStart);
    LocalDate endDate = parseDate(dateRangeEnd);

    Set<LocalDate> dates = new HashSet<LocalDate>();

    int difference = DateUtil.getDaysBetween(startDate, true, endDate, true);
    for (int i = 0; i < difference; i++) {
      LocalDate date = startDate.plusDays(i);
      dates.add(date);
    }

    return dates;
  }

  private BatchJobRun createRun(CommandLine line) {
    BatchJobRun run = new BatchJobRun(this);

    // attributes that do not vary by date
    run.setRunReason(line.getOptionValue("reason"));
    run.setObservationTime(line.getOptionValue("observationtime"));
    run.setSnapshotObservationTime(line.getOptionValue("snapshotobservationtime"));

    return run;
  }

  public void parse(String[] args) throws CalendricalParseException, OpenGammaRuntimeException {
    CommandLine line;
    try {
      CommandLineParser parser = new PosixParser();
      line = parser.parse(getOptions(), args);
    } catch (ParseException e) {
      throw new OpenGammaRuntimeException("Could not parse command line", e);
    }

    setViewName(line.getOptionValue("view"));
    setViewDateTime(line.getOptionValue("viewdatetime"));
    setForceNewRun(line.hasOption("forcenewrun"));
    setPositionMasterTime(line.getOptionValue("positionmastertime"));

    String dateRangeStart = line.getOptionValue("daterangestart");
    String dateRangeEnd = line.getOptionValue("daterangeend");

    if (dateRangeStart != null && dateRangeEnd != null) {
      // multiple runs, on many different dates
      Set<LocalDate> runDates = getDates(dateRangeStart, dateRangeEnd);

      for (LocalDate runDate : runDates) {
        BatchJobRun run = createRun(line);

        run.setObservationDate(runDate);
        run.setSnapshotObservationDate(runDate);

        String valuationTimePartStr = line.getOptionValue("valuationtime");
        OffsetTime valuationTimePart;
        if (valuationTimePartStr == null) {
          valuationTimePart = getCreationTime().toOffsetTime();
        } else {
          valuationTimePart = parseTime(valuationTimePartStr);
        }
        OffsetDateTime valuationTime = OffsetDateTime.of(runDate, valuationTimePart, valuationTimePart.getOffset());
        run.setValuationTime(valuationTime);

        run.init();

        String whyNotRunReason = null;
        
        if (getHolidaySource() == null || getHolidayCurrency() == null) {
          try {
            _batchDbManager.getSnapshotValues(run.getSnapshotId());
          } catch (IllegalArgumentException e) {
            whyNotRunReason = "there is no market data snapshot for this day";
          }
        } else {
          if (runDate.getDayOfWeek() == DayOfWeek.SATURDAY || runDate.getDayOfWeek() == DayOfWeek.SUNDAY) { 
            whyNotRunReason = "this day is a weekend"; 
          } else {
            boolean isHoliday = getHolidaySource().isHoliday(getHolidayCurrency(), runDate);
            if (isHoliday) {
              whyNotRunReason = "this day is a holiday";
            }
          }
        }

        if (whyNotRunReason == null) {
          addRun(run);
        } else {
          s_logger.info("Not running for day {} because {}", runDate, whyNotRunReason);
        }
      }

    } else {
      // a single run, on a single date
      BatchJobRun run = createRun(line);

      run.setObservationDate(line.getOptionValue("observationdate"));
      run.setSnapshotObservationDate(line.getOptionValue("snapshotobservationdate"));
      run.setValuationTime(line.getOptionValue("valuationtime"));

      run.init();
      addRun(run);
    }
  }

  public void execute() {
    for (BatchJobRun run : _runs) {
      try {
        s_logger.info("Running {}", run);
  
        createView(run);
  
        _batchDbManager.startBatch(run);

        getView().runOneCycle(run.getValuationTime().toInstant().toEpochMillisLong());

        _batchDbManager.endBatch(run);
        
        s_logger.info("Completed {}", run);
      
      } catch (Exception e) {
        run.setFailed(true);
        s_logger.error("Failed " + run, e);                        
      }
    }
  }

  public static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.financial.batch.BatchJob [args] {springfile.xml}", getOptions());
  }

  public static void main(String[] args) { // CSIGNORE
    if (args.length == 0) {
      usage();
      System.exit(-1);
    }
    
    String springContextFile = args[args.length - 1];
    ApplicationContext context = new FileSystemXmlApplicationContext(springContextFile);
    BatchJob job = (BatchJob) context.getBean("batchJob");

    try {
      job.parse(args);
    } catch (Exception e) {
      s_logger.error("Failed to parse command line", e);
      usage();
      System.exit(-1);
    }

    try {
      job.createViewDefinition();
    } catch (Exception e) {
      s_logger.error("Failed to run batch", e);
      usage();
      System.exit(-1);
    }
    
    job.execute();

    boolean failed = false;
    for (BatchJobRun run : job._runs) {
      if (run.isFailed()) {
        failed = true;
      }
    }
    
    if (failed) {
      System.exit(-1);
    } else {
      System.exit(0);
    }
  }

}
