/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch;

import java.util.Collection;

import javax.time.calendar.LocalDate;
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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.batch.db.BatchDbManager;
import com.opengamma.engine.batch.db.BatchDbRiskContext;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * The entry point for running OpenGamma batches. 
 */
public class BatchJob implements Job, ComputationResultListener {
  
  /**
   * Used as a default "observation time" for ad hoc batches, i.e., batches that are
   * started manually by users and whose results should NOT flow to downstream
   * systems.  
   */
  public static final String AD_HOC_OBSERVATION_TIME = "AD_HOC_RUN";
  
  // --------------------------------------------------------------------------
  // Variables initialized at construction time
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
  private ZonedDateTime _valuationTime;
  
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
  private ZonedDateTime _viewDateTime;
  
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
   * Used to populate the batch database
   */
  private BatchDbManager _batchDbManager;
  
  // --------------------------------------------------------------------------
  // Variables initialized during the batch run
  // --------------------------------------------------------------------------

  /**
   * View loaded from the configuration database.
   * The view will define the portfolio of trades the batch should be run for.
   */
  private View _view;
  
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

  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(String valuationTime) {
    if (valuationTime == null) {
      _valuationTime = null;
    } else {
      _valuationTime = _dateTimeFormatter.parse(valuationTime, ZonedDateTime.rule()); 
    }
  }

  public String getViewName() {
    return _viewName;
  }

  public void setViewName(String viewName) {
    _viewName = viewName;
  }

  public ZonedDateTime getViewDateTime() {
    return _viewDateTime;
  }

  public void setViewDateTime(String viewDateTime) {
    if (viewDateTime == null) {
      _viewDateTime = null;            
    } else {
      _viewDateTime = _dateTimeFormatter.parse(viewDateTime, ZonedDateTime.rule());
    }
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
  
  public int getViewOid() {
    return 123; // TODO
  }
  
  public int getViewVersion() {
    return 1; // TODO    
  }
  
  public Object getDbHandle() {
    return _dbHandle;
  }

  public void setDbHandle(Object dbHandle) {
    _dbHandle = dbHandle;
  }
  
  public BatchDbManager getBatchDbManager() {
    return _batchDbManager;
  }

  public void setBatchDbManager(BatchDbManager batchDbManager) {
    _batchDbManager = batchDbManager;
  }

  // --------------------------------------------------------------------------
  
  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    BatchDbRiskContext context = getBatchDbManager().createLocalContext(this);
    _batchDbManager.write(context, resultModel);    
  }

  @Override
  public UserPrincipal getUser() {
    return _user;
  }
  
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Run reason", getRunReason()).toString(); 
  }

  
  // --------------------------------------------------------------------------

  
  public void init() throws OpenGammaRuntimeException {
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
      _valuationTime = now; 
    }
    
    if (getViewName() == null && getView() == null) {
      throw new OpenGammaRuntimeException("Please specify view name.");
    }
    if (_viewDateTime == null) {
      _viewDateTime = now;      
    }
    
    if (_snapshotObservationDate == null) {
      _snapshotObservationDate = _observationDate;
    }
    
    if (_snapshotObservationTime == null) {
      _snapshotObservationTime = _observationTime;
    }
    
    getView().addResultListener(this);
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
        "Valuation time. yyyyMMddHHmmss[Z] - for example, 20100621162200. Default - system clock datetime.");
    
    options.addOption("view", "view", true, 
        "View name in configuration database. You must specify this.");
    options.addOption("viewdatetime", "viewdatetime", true, 
        "Instant at which view should be loaded. yyyyMMddHHmmss[Z]. Default - system clock datetime.");
    
    options.addOption("snapshotobservationtime", "snapshotobservationtime", true, 
        "Observation time of LiveData snapshot to use - for example, LDN_CLOSE. Default - same as observationtime.");
    options.addOption("snapshotobservationdate", "snapshotobservationdate", true, 
        "Observation date of LiveData snapshot to use. yyyyMMdd. Default - same as observationdate");
    
    options.addOption("forcenewrun", "forcenewrun", false, "If specified, a new run is always created " +
        "- no existing results are used. If not specified, the system first checks if there is already a run " + 
        "in the database for the given view (including the same version) with the same observation date and time. " +
        "If there is, that run is reused.");
    
    return options;
  }
  
  public void parse(String[] args) throws ParseException, CalendricalParseException, OpenGammaRuntimeException {
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(getOptions(), args);
    
    setRunReason(line.getOptionValue("reason"));
    setObservationTime(line.getOptionValue("observationtime"));
    setObservationDate(line.getOptionValue("observationdate"));
    setValuationTime(line.getOptionValue("valuationtime"));
    setViewName(line.getOptionValue("view"));
    setViewDateTime(line.getOptionValue("viewdatetime"));
    setSnapshotObservationTime(line.getOptionValue("snapshotobservationtime"));
    setSnapshotObservationDate(line.getOptionValue("snapshotobservationdate"));
    setForceNewRun(line.hasOption("forcenewrun"));
  }
  
  public void execute() {
    _batchDbManager.startBatch(this);
    getView().runOneCycle();
    _batchDbManager.endBatch(this);
  }
  
  public static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java com.opengamma.engine.batch.BatchJob [args]", options);
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
