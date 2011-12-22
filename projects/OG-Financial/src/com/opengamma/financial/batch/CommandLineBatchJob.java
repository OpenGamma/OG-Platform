/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.CalendricalParseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.historical.HistoricalMarketDataProvider;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.VersionUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * A command line batch job holding all necessary configuration.
 */
public class CommandLineBatchJob {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineBatchJob.class);

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
   * Used to create the PositionSource if none is explicitly specified. Use this
   * for more control over the version and correction dates of data.
   */
  private PortfolioMaster _portfolioMaster;

  /**
   * Used to load Positions (needed for building the dependency graph). If not
   * specified will be constructed from the PositionMaster.
   */
  private PositionSource _positionSource;

  /**
   * Used to write stuff to the batch database.
   */
  private BatchRunMaster _batchMaster;

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
  private HistoricalMarketDataProvider _historicalMarketDataProvider;
  
  /**
   * This is used to determine the name of the property file from
   * which to read system version. 
   */
  private String _systemVersionPropertyFile = "og-financial";
  
  // --------------------------------------------------------------------------
  // Variables initialized from command line input
  // --------------------------------------------------------------------------
  
  /**
   * Most variables from command line input end up in this object.
   */
  private BatchJobParameters _parameters = new BatchJobParameters();

  /**
   * @see RunCreationMode
   */
  private RunCreationMode _runCreationMode = RunCreationMode.AUTO; 
  
  /**
   * The batch may be run for multiple days in sequence, therefore we need multiple runs
   */
  private final List<CommandLineBatchJobRun> _runs = new ArrayList<CommandLineBatchJobRun>();
  
  // --------------------------------------------------------------------------

  public CommandLineBatchJob() {
    _user = UserPrincipal.getLocalUser();
    // TODO: TIMEZONE
    _creationTime = ZonedDateTime.now();  // used later to obtain local date/time and zone
  }

  // --------------------------------------------------------------------------

  public String getOpenGammaVersion() {
    return VersionUtils.getVersion(getSystemVersionPropertyFile());
  }
  
  public String getSystemVersionPropertyFile() {
    return _systemVersionPropertyFile;
  }

  public void setSystemVersionPropertyFile(String systemVersionPropertyFile) {
    _systemVersionPropertyFile = systemVersionPropertyFile;
  }

  public BatchJobParameters getParameters() {
    return _parameters;
  }
  
  public void setParameters(BatchJobParameters parameters) {
    _parameters = parameters;
  }

  public RunCreationMode getRunCreationMode() {
    return _runCreationMode;
  }

  public void setRunCreationMode(RunCreationMode runCreationMode) {
    _runCreationMode = runCreationMode;
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

  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  public void setPortfolioMaster(PortfolioMaster portfolioMaster) {
    _portfolioMaster = portfolioMaster;
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

  public BatchRunMaster getBatchMaster() {
    return _batchMaster;
  }

  public void setBatchMaster(BatchRunMaster batchMaster) {
    _batchMaster = batchMaster;
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
  
  public HistoricalMarketDataProvider getHistoricalMarketDataProvider() {
    return _historicalMarketDataProvider;
  }

  public void setHistoricalMarketDataProvider(HistoricalMarketDataProvider historicalMarketDataProvider) {
    _historicalMarketDataProvider = historicalMarketDataProvider;
  }

  public UserPrincipal getUser() {
    return _user;
  }

  public ZonedDateTime getCreationTime() {
    return _creationTime;
  }

  public List<CommandLineBatchJobRun> getRuns() {
    return Collections.unmodifiableList(_runs);
  }

  public void addRun(CommandLineBatchJobRun run) {
    _runs.add(run);
  }

  // --------------------------------------------------------------------------

  public static Options getOptions() {
    Options options = new Options();

    options.addOption("reason", true, "Run reason. Default - Manual run started on {yyyy-MM-ddTHH:mm:ssZZ} by {user.name}.");

    options.addOption("observationTime", true, "Observation time - for example, LDN_CLOSE. Default - " + BatchJobParameters.AD_HOC_OBSERVATION_TIME + ".");
    options.addOption("observationDate", true, "Observation date (= run date). yyyyMMdd - for example, 20100621. Default - system clock date.");
    options.addOption("valuationTime", true, "Valuation time. HH:mm[:ss] - for example, 16:22:09. Default - system clock.");

    options.addOption("view", true, "View name in configuration database. You must specify this.");
    options.addOption("viewTime", true, "Time at which view should be loaded. HH:mm[:ss]. Default - system clock.");

    options.addOption("snapshotObservationTime", true, "Observation time of LiveData snapshot to use - for example, LDN_CLOSE. Default - same as observationTime.");
    options.addOption("snapshotObservationDate", true, "Observation date of LiveData snapshot to use. yyyyMMdd. Default - same as observationDate");

    options.addOption("runCreationMode", true, "One of auto, create_new, create_new_overwrite, reuse_existing (case insensitive)." +
        " Specifies whether to create a new run in the database." +
        " See documentation of RunCreationMode Java enum to find out more. Default - auto.");

    options.addOption("configDbTime", true, "Time at which documents should be loaded from the configuration database. HH:mm[:ss]. Default - system clock.");
    options.addOption("staticDataTime", true, "Time at which documents should be loaded from position master, security master, etc. HH:mm[:ss]. Default - system clock.");

    options.addOption("dateRangeStart", true, "First valuation date (inclusive). If daterangestart and daterangeend are given, "
        + "observationDate and snapshotObservationDate are calculated from the range and must not be given explicitly." 
        + " By default, the batch will be run for those dates within the range which are not weekends or holidays.");
    options.addOption("dateRangeEnd", true, "Last valuation date (inclusive).");
    options.addOption("snapshotDateRange", false, "An option that can be used in conjunction with dateRangeStart and dateRangeEnd. "
        + "If given, the batch will be run for those dates for which there is a market data snapshot in the batch database. "
        + "If there is no snapshot, that date is simply ignored. This can be useful if you want to run the batch for " 
        + "a specific set of historical dates.");
    
    options.addOption("timeZone", true, "Time zone in which times on the command line are given. Default - system time zone.");
    
    options.addOption("springXml", true, "Name (relative to current working directory) of Spring XML which contains definition of bean batchJob. " +
        "If specified, all configuration " +
        "is assumed to come exclusively from the command line. No configuration is read from the config DB and " +
        "as a result, no {name of config} needs to be given.");
    
    return options;
  }

  private Collection<LocalDate> getDates(String dateRangeStart, String dateRangeEnd) {
    ArgumentChecker.notNull(dateRangeStart, "Date range start");
    ArgumentChecker.notNull(dateRangeStart, "Date range end");

    LocalDate startDate = BatchJobParameters.parseDate(dateRangeStart);
    LocalDate endDate = BatchJobParameters.parseDate(dateRangeEnd);

    Collection<LocalDate> dates = new ArrayList<LocalDate>();

    int difference = DateUtils.getDaysBetween(startDate, true, endDate, true);
    for (int i = 0; i < difference; i++) {
      LocalDate date = startDate.plusDays(i);
      dates.add(date);
    }

    return dates;
  }
  
  private CommandLineBatchJobRun createRun(CommandLine line, LocalDate runDate) {
    
    // default snapshot observation date = run date, but this can be overridden
    // on the command line
    LocalDate snapshotObservationDate = runDate;
    String option = line.getOptionValue("snapshotObservationDate");
    if (option != null) {
      snapshotObservationDate = BatchJobParameters.parseDate(option); 
    } 
    
    LocalDate configDbDate = runDate;
    option = line.getOptionValue("configDbDate");
    if (option != null) {
      configDbDate = BatchJobParameters.parseDate(option); 
    }
    
    LocalDate staticDataDate = runDate;
    option = line.getOptionValue("staticDataDate");
    if (option != null) {
      staticDataDate = BatchJobParameters.parseDate(option); 
    }
    
    CommandLineBatchJobRun run = new CommandLineBatchJobRun(this, 
        runDate, 
        snapshotObservationDate,
        configDbDate,
        staticDataDate);
    return run;
  }

  public void initialize(CommandLine line, BatchJobParameters parameters) throws CalendricalParseException, OpenGammaRuntimeException {
    getParameters().initializeDefaults(this);
    
    if (parameters != null) {
      getParameters().initialize(parameters);
    }
    
    Map<String, String> optionsMap = new HashMap<String, String>();
    for (Option option : line.getOptions()) {
      optionsMap.put(option.getOpt(), option.getValue());
    }
    
    getParameters().initialize(optionsMap);
    getParameters().validate();

    if (line.hasOption("runCreationMode")) {
      String creationMode = line.getOptionValue("runCreationMode");
      if (creationMode.equalsIgnoreCase("auto")) {
        setRunCreationMode(RunCreationMode.AUTO);
      } else if (creationMode.equalsIgnoreCase("create_new")) {
        setRunCreationMode(RunCreationMode.CREATE_NEW);
      } else if (creationMode.equalsIgnoreCase("create_new_overwrite")) {
        setRunCreationMode(RunCreationMode.CREATE_NEW_OVERWRITE);
      } else if (creationMode.equalsIgnoreCase("reuse_existing")) {
        setRunCreationMode(RunCreationMode.REUSE_EXISTING);
      } else {
        throw new OpenGammaRuntimeException("Unrecognized runCreationMode. " +
            "Should be one of AUTO, ALWAYS, NEVER. " +
            "Was " + creationMode);
      }
    } 

    String dateRangeStart = line.getOptionValue("dateRangeStart");
    String dateRangeEnd = line.getOptionValue("dateRangeEnd");

    if (dateRangeStart != null && dateRangeEnd != null) {
      // multiple runs, on many different dates
      Collection<LocalDate> runDates = getDates(dateRangeStart, dateRangeEnd);

      for (LocalDate runDate : runDates) {
        CommandLineBatchJobRun run = createRun(line, runDate);

        String whyNotRunReason = null;
        
        if (line.hasOption("snapshotDateRange")) {
          try {
            _batchMaster.getSnapshotValues(run.getSnapshotId());
          } catch (IllegalArgumentException e) {
            whyNotRunReason = "there is no market data snapshot for this day";
          }
        } else {
          if (runDate.getDayOfWeek() == DayOfWeek.SATURDAY || runDate.getDayOfWeek() == DayOfWeek.SUNDAY) { 
            whyNotRunReason = "this day is a weekend"; 
          } else if (getHolidaySource() != null && getHolidayCurrency() != null) {
            boolean isHoliday = getHolidaySource().isHoliday(runDate, getHolidayCurrency());
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
      
      LocalDate runDate = getCreationTime().toLocalDate();
      String observationDateStr = line.getOptionValue("observationDate");
      if (observationDateStr != null) {
        runDate = BatchJobParameters.parseDate(observationDateStr);
      } 
      
      CommandLineBatchJobRun run = createRun(line, runDate);
      addRun(run);
    }
  }

  public void execute() {
    for (CommandLineBatchJobRun run : _runs) {
      try {
        s_logger.info("Running {}", run);
  
        run.createViewDefinition();
        run.createViewProcessor();
        
        _batchMaster.startBatch(run);
        
        ViewClient client = run.getViewProcessor().createViewClient(UserPrincipal.getLocalUser());
        HistoricalMarketDataSpecification marketDataSpec = getHistoricalMarketDataProvider() == null ? MarketData.historical(run.getSnapshotObservationDate(), null, null) : MarketData.historical(
            run.getSnapshotObservationDate(), getHistoricalMarketDataProvider().getTimeSeriesResolverKey(), getHistoricalMarketDataProvider().getTimeSeriesFieldResolverKey());
        ViewCycleExecutionOptions cycleExecutionOptions = new ViewCycleExecutionOptions(run.getValuationTime(), marketDataSpec);
        client.attachToViewProcess(run.getViewDefinition().getUniqueId(), ExecutionOptions.batch(ArbitraryViewCycleExecutionSequence.single(cycleExecutionOptions), null), true);
        client.waitForCompletion();

        _batchMaster.endBatch(run);
        
        s_logger.info("Completed {}", run);
      
      } catch (Exception e) {
        run.setFailed(true);
        s_logger.error("Failed to run " + run, e);                        
      }
    }
  }

}
