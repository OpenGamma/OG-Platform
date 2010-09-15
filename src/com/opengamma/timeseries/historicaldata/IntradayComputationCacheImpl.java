/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.historicaldata;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.Duration;
import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.historicaldata.IntradayComputationCache;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.timeseries.DataPointDocument;
import com.opengamma.timeseries.DateTimeTimeSeriesMaster;
import com.opengamma.timeseries.TimeSeriesDocument;
import com.opengamma.timeseries.TimeSeriesSearchRequest;
import com.opengamma.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;

/**
 * This implementation uses a {@link DateTimeTimeSeriesMaster} (i.e., a relational DB)
 * to store intraday time series. 
 * <p>
 * Right now you need to create one cache per view. There probably should be just
 * one globally.
 */
public class IntradayComputationCacheImpl implements IntradayComputationCache, ComputationResultListener, Lifecycle {
  
  private static final Logger s_logger = LoggerFactory.getLogger(IntradayComputationCacheImpl.class);
  
  /**
   * Used to load/store time series
   */
  private final DateTimeTimeSeriesMaster _timeSeriesMaster;
  
  /**
   * The user the cache runs as. Needed for View permission checks
   */
  private final UserPrincipal _user;
  
  /**
   * Used to kick off cache DB update operations (e.g., for 1-minute bars once a minute)
   */
  private final Timer _timer;
  
  /**
   * What resolutions are in use, e.g., "Store historical values at 1, 5, and 15 minute resolutions."
   */
  private final ConcurrentHashMap<Duration, ResolutionRecord> _resolution2ResolutionRecord = new ConcurrentHashMap<Duration, ResolutionRecord>();
  
  /**
   * Are we running?
   */
  private boolean _running; // = false
  
  private final class ResolutionRecord {
    
    /**
     * E.g., 1 minute
     */
    private final Duration _duration;
    
    /**
     * E.g., 24*60 (-> store last 24 hours)
     */
    private final int _numPoints;
    
    /**
     * E.g., 30 seconds ago
     */
    private Instant _timeOfLastDbWrite;

    /**
     * E.g., the last result might be just 2 seconds old
     * - the db is NOT automatically updated after a result is
     * received, instead this happens at a predefined interval,
     * for example 1 minute 
     */
    private ViewComputationResultModel _lastResult;
    
    private SaveTask _saveTask; 
    
    private ResolutionRecord(
        Duration duration,
        int numPoints) {
      
      ArgumentChecker.notNull(duration, "duration");
      if (numPoints <= 0) {
        throw new IllegalArgumentException("Num points must be positive");
      }
      
      _duration = duration;
      _numPoints = numPoints;
      _timeOfLastDbWrite = Instant.EPOCH;
    }
    
    private Instant getFirstDateToRetain() {
      return _timeOfLastDbWrite.minus(_duration.multipliedBy(_numPoints));      
    }
    
    private synchronized void start() {
      if (_saveTask == null) {
        _saveTask = new SaveTask();
        _timer.scheduleAtFixedRate(_saveTask, 0, _duration.toMillisLong());
      }
    }
    
    private synchronized void stop() {
      if (_saveTask != null) {
        _saveTask.cancel();
        _saveTask = null;
      }
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  public IntradayComputationCacheImpl(
      DateTimeTimeSeriesMaster timeSeriesMaster, 
      UserPrincipal user) {
    this(timeSeriesMaster, user, new Timer("IntradayComputationCache"));
  }
  
  public IntradayComputationCacheImpl(
      DateTimeTimeSeriesMaster timeSeriesMaster, 
      UserPrincipal user,
      Timer timer) {
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(timer, "timer");
    _timeSeriesMaster = timeSeriesMaster;    
    _user = user;
    _timer = timer;
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public void addResolution(Duration resolution, int numPoints) {
    ResolutionRecord record = new ResolutionRecord(resolution, numPoints);
    ResolutionRecord previousValue = _resolution2ResolutionRecord.putIfAbsent(resolution, record);
    if (previousValue == null && isRunning()) {
      record.start();      
    }
  }

  @Override
  public void removeResolution(Duration resolution) {
    ResolutionRecord record = _resolution2ResolutionRecord.remove(resolution);
    if (record != null) {
      record.stop();
    }
  }
  
  // --------------------------------------------------------------------------

  @Override
  public DateTimeDoubleTimeSeries getValue(String calcConf, ValueSpecification specification, Duration resolution) {
    ResolutionRecord record = _resolution2ResolutionRecord.get(resolution);
    if (record == null) {
      throw new IllegalArgumentException("Resolution " + resolution + " has not been set up");
    }
    
    IdentifierBundle identifiers = getIdentifierBundle(specification);
    String fieldName = getFieldName(specification);
    
    TimeSeriesSearchRequest<Date> searchRequest = new TimeSeriesSearchRequest<Date>();
    searchRequest.setIdentifiers(identifiers.getIdentifiers());
    searchRequest.setDataSource(getDataSource());
    searchRequest.setDataProvider(calcConf);
    searchRequest.setDataField(fieldName);
    
    TimeSeriesSearchResult<Date> searchResult = _timeSeriesMaster.searchTimeSeries(searchRequest);
    if (searchResult.getDocuments().isEmpty()) {
      return null;
    } else if (searchResult.getDocuments().size() > 1) {
      throw new RuntimeException("Should only have returned 1 result for " + calcConf + "/" + specification);
    }
    
    TimeSeriesDocument<Date> dbTimeSeries = searchResult.getDocuments().get(0);
    
    // the last point of the series is not stored in db but floats in real time.
    // for example, if you have 1-minute bars in the db, and the engine
    // is able to re-compute every 5 seconds, the last point of the time series
    // will change 12 times a minute although the db is written only once a minute.
    
    MutableDateTimeDoubleTimeSeries timeSeries = dbTimeSeries.getTimeSeries().toMutableDateTimeDoubleTimeSeries();
    
    Date latestDbTime = timeSeries.getLatestTime();
    Date latestTime = new Date(record._lastResult.getResultTimestamp().toEpochMillisLong());
    
    if (!latestDbTime.equals(latestTime)) {
      double value = 55;
      timeSeries.putDataPoint(latestTime, value);
    }
    return timeSeries;
  }

  @Override
  public UserPrincipal getUser() {
    return _user;
  }
  
  // --------------------------------------------------------------------------

  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    
    for (ResolutionRecord resolution : _resolution2ResolutionRecord.values()) {
      synchronized (resolution) {
        resolution._lastResult = resultModel;
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  private String getDataSource() {
    return "IntradayCache";
  }
  
  private String getFieldName(ValueSpecification spec) {
    // ugly and error-prone if new fields are added to ValueSpecification
    return spec.getFunctionUniqueId() + "/" + spec.getRequirementSpecification().getValueName();
  }
  
  private IdentifierBundle getIdentifierBundle(ValueSpecification specification) {
    return new IdentifierBundle(specification.getRequirementSpecification().getTargetSpecification().getIdentifier());
  }
  
  // --------------------------------------------------------------------------
  
  private class SaveTask extends TimerTask {
    private ResolutionRecord _resolution;
    
    @Override
    public void run() {
      try {
        synchronized (_resolution) {
          save(_resolution);
        }
      } catch (RuntimeException e) {
        s_logger.error("Updating intraday time series for " + _resolution + " failed", e);
      }
    }
  }
  
  private void save(ResolutionRecord resolution) {
    Instant now = Instant.nowSystemClock();
    resolution._timeOfLastDbWrite = now;
    
    for (String calcConf : resolution._lastResult.getCalculationConfigurationNames()) {
      ViewCalculationResultModel result = resolution._lastResult.getCalculationResult(calcConf);
      
      for (ComputationTargetSpecification spec : result.getAllTargets()) {
        Map<String, ComputedValue> values = result.getValues(spec);
        
        for (ComputedValue value : values.values()) {
          
          if (!(value.getValue() instanceof Double)) {
            s_logger.warn(value + " is not a double");
            continue;
          }
          
          ValueSpecification valueSpecification = value.getSpecification();
          
          IdentifierBundle identifiers = getIdentifierBundle(valueSpecification);
          String fieldName = getFieldName(valueSpecification);

          UniqueIdentifier seriesUid = _timeSeriesMaster.resolveIdentifier(
              identifiers,
              getDataSource(),
              calcConf,
              fieldName);
          
          if (seriesUid == null) {
            TimeSeriesDocument<Date> timeSeries = new TimeSeriesDocument<Date>();
            timeSeries.setIdentifiers(identifiers);
            timeSeries.setDataSource(getDataSource());
            timeSeries.setDataProvider(calcConf);
            timeSeries.setDataField(fieldName);
            timeSeries = _timeSeriesMaster.addTimeSeries(timeSeries);
            seriesUid = timeSeries.getUniqueIdentifier();            
          }
          
          DataPointDocument<Date> dataPoint = new DataPointDocument<Date>();
          dataPoint.setTimeSeriesId(seriesUid);
          dataPoint.setDate(new Date(now.toEpochMillisLong()));
          dataPoint.setValue((Double) value.getValue());
          
          _timeSeriesMaster.addDataPoint(dataPoint);
          _timeSeriesMaster.removeDataPoints(seriesUid, new Date(resolution.getFirstDateToRetain().toEpochMillisLong()));
        }
        
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public synchronized boolean isRunning() {
    return _running;
  }

  @Override
  public synchronized void start() {
    _running = true;
    
    for (ResolutionRecord resolution : _resolution2ResolutionRecord.values()) {
      resolution.start();      
    }
  }

  @Override
  public synchronized void stop() {
    _running = false;
    
    for (ResolutionRecord resolution : _resolution2ResolutionRecord.values()) {
      resolution.stop();      
    }
  }
  
}
