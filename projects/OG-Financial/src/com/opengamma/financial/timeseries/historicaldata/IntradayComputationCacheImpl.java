/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.historicaldata;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.financial.timeseries.DataPointDocument;
import com.opengamma.financial.timeseries.DateTimeTimeSeriesMaster;
import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesSearchRequest;
import com.opengamma.financial.timeseries.TimeSeriesSearchResult;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MapDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;

/**
 * This implementation uses a {@link DateTimeTimeSeriesMaster} (i.e., a relational DB)
 * to store intraday time series. 
 * <p>
 * You need to create one cache per view processor.
 */
public class IntradayComputationCacheImpl implements IntradayComputationCache, ComputationResultListener, Lifecycle {
  
  private static final Logger s_logger = LoggerFactory.getLogger(IntradayComputationCacheImpl.class);
  
  /**
   * Used to load/store time series. All access to it must be synchronized using _timeSeriesMasterLock.
   */
  private final DateTimeTimeSeriesMaster _timeSeriesMaster;
  
  /**
   * Used to lock the time series storage
   */
  private final Lock _timeSeriesMasterLock = new ReentrantLock();
  
  /**
   * The user the cache runs as. Needed for View permission checks
   */
  private final UserPrincipal _user;
  
  /**
   * Used to kick off DB update operations (e.g., for 1-minute bars once a minute)
   */
  private ScheduledExecutorService _executorService;
  
  /**
   * What resolutions are in use, e.g., "Store historical values at 1, 5, and 15 minute resolutions."
   */
  private final ConcurrentHashMap<Duration, ResolutionRecord> _resolution2ResolutionRecord = new ConcurrentHashMap<Duration, ResolutionRecord>();
  
  /**
   * Are we running?
   */
  private boolean _running; // = false
  
  /**
   * The view processor is used to query whether a given view is running.
   */
  private final ViewProcessor _viewProcessor;
  
  /**
   * This is the part that's fixed to a single view processor.
   */
  private ConcurrentHashMap<String, ViewComputationResultModel> _viewName2LastResult = new ConcurrentHashMap<String, ViewComputationResultModel>();
  
  /**
   * One resolution (e.g., 1 minute) at which history is being stored. There
   * can be multiple resolutions active simultaneously.
   */
  private final class ResolutionRecord {
    
    /**
     * E.g., 1 minute
     */
    private final Duration _duration;
    
    /**
     * E.g., 24*60 (-> store last 24 hours)
     */
    private int _numPoints;
    
    /**
     * Run at e.g. 1 minute interval
     */
    private ScheduledFuture<?> _saveTask; 
    
    private ResolutionRecord(
        Duration duration,
        int numPoints) {
      
      ArgumentChecker.notNull(duration, "duration");
      if (numPoints <= 0) {
        throw new IllegalArgumentException("Num points must be positive");
      }
      
      _duration = duration;
      _numPoints = numPoints;
    }
    
    public synchronized int getNumPoints() {
      return _numPoints;
    }

    public synchronized void setNumPoints(int numPoints) {
      _numPoints = numPoints;
    }

    private synchronized Instant getFirstDateToRetain(Instant now) {
      // e.g., current time = 1000 ms
      // duration = 50 ms
      // retain last 2 points
      // last point to retain must be 950 ms since a new point was written at 1000 ms
      // subtract 30 millis from the exact value to account for system clock wobbliness
      // so in practice retain points at 920 ms or greater
      return now.minus(_duration.multipliedBy(getNumPoints() - 1)).minusMillis(30);      
    }
    
    private void start() { // synchronization on parent object
      if (_saveTask == null) {
        SaveTask saveTask = new SaveTask(this);
        _saveTask = _executorService.scheduleAtFixedRate(saveTask, 0, _duration.toMillisLong(), TimeUnit.MILLISECONDS);
      }
    }
    
    private void stop() { // synchronization on parent object
      if (_saveTask != null) {
        _saveTask.cancel(false);
        _saveTask = null;
      }
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  public IntradayComputationCacheImpl(
      ViewProcessor viewProcessor,
      DateTimeTimeSeriesMaster timeSeriesMaster, 
      UserPrincipal user) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    ArgumentChecker.notNull(user, "user");
    _viewProcessor = viewProcessor;
    _timeSeriesMaster = timeSeriesMaster;    
    _user = user;
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public synchronized void addResolution(Duration resolution, int numPoints) {
    ResolutionRecord record = new ResolutionRecord(resolution, numPoints);
    ResolutionRecord previousValue = _resolution2ResolutionRecord.putIfAbsent(resolution, record);
    
    if (previousValue != null) {
      previousValue.setNumPoints(numPoints);
    } 
    
    if (previousValue == null && isRunning()) {
      record.start();      
    } 
  }

  @Override
  public synchronized void removeResolution(Duration resolution) {
    ResolutionRecord record = _resolution2ResolutionRecord.remove(resolution);
    if (record != null) {
      record.stop();
    }
  }
  
  @Override
  public Map<Duration, Integer> getResolutions() {
    Map<Duration, Integer> resolutions = new HashMap<Duration, Integer>();
    for (ResolutionRecord record : _resolution2ResolutionRecord.values()) {
      resolutions.put(record._duration, record.getNumPoints());
    }
    return resolutions;
  }

  // --------------------------------------------------------------------------

  @Override
  public DateTimeDoubleTimeSeries getValue(String viewName, String calcConf, ValueSpecification specification, Duration resolution) {
    ResolutionRecord record = _resolution2ResolutionRecord.get(resolution);
    if (record == null) {
      throw new IllegalArgumentException("Resolution " + resolution + " has not been set up");
    }
    ViewComputationResultModel lastResult = _viewName2LastResult.get(viewName);
    
    IdentifierBundle identifiers = getIdentifierBundle(specification);
    String fieldName = getFieldName(specification);
    
    TimeSeriesSearchRequest<Date> searchRequest = new TimeSeriesSearchRequest<Date>();
    searchRequest.getIdentifiers().addAll(identifiers.getIdentifiers());
    searchRequest.setDataSource(getDataSource());
    searchRequest.setDataProvider(getDataProvider(viewName, calcConf));
    searchRequest.setDataField(fieldName);
    searchRequest.setLoadTimeSeries(true);
    
    TimeSeriesSearchResult<Date> searchResult;

    _timeSeriesMasterLock.lock();
    try {
      searchResult = _timeSeriesMaster.searchTimeSeries(searchRequest);
    } finally {
      _timeSeriesMasterLock.unlock();
    }
    
    if (searchResult.getDocuments().isEmpty()) {
      return null;
    } else if (searchResult.getDocuments().size() > 1) {
      throw new RuntimeException("Should only have returned 1 result for " + viewName + "/" + calcConf + "/" + specification);
    }
    
    TimeSeriesDocument<Date> dbTimeSeries = searchResult.getDocuments().get(0);
    MutableDateTimeDoubleTimeSeries timeSeries = dbTimeSeries.getTimeSeries().toMutableDateTimeDoubleTimeSeries();
    
    if (lastResult != null) {
      // the last point of the series is not stored in db but floats in real time.
      // for example, if you have 1-minute bars in the db, and the engine
      // is able to re-compute every 5 seconds, the last point of the time series
      // will change 12 times a minute although the db is written only once a minute.
      
      Date latestDbTime = timeSeries.getLatestTime();
      Date latestTime = new Date(lastResult.getResultTimestamp().toEpochMillisLong());
      
      if (latestTime.after(latestDbTime)) {
        ViewTargetResultModel latestResult = lastResult.getTargetResult(specification.getTargetSpecification());
        if (latestResult != null) {
          Map<String, ComputedValue> latestValues = latestResult.getValues(calcConf);
          ComputedValue latestValue = latestValues.get(specification.getValueName());
          if (latestValue.getValue() instanceof Double) {
            timeSeries.putDataPoint(latestTime, (Double) latestValue.getValue());
          }
        } else {
          s_logger.debug("No latest point found {} {}", calcConf, specification);
        }
      }
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
    _viewName2LastResult.put(resultModel.getViewName(), resultModel);
  }
  
  // --------------------------------------------------------------------------
  
  private String getDataSource() {
    return "IntradayCache";
  }
  
  private String getObservationTime() {
    return "Intraday";
  }
  
  private String getDataProvider(String viewName, String calcConf) {
    return viewName + "/" + calcConf;
  }
  
  private String getFieldName(ValueSpecification spec) {
    // ugly and error-prone if new fields are added to ValueSpecification
    // TODO 2010-10-22 Andrew -- should we iterate over the properties of the value spec ?
    return spec.getFunctionUniqueId() + "/" + spec.getValueName();
  }
  
  private IdentifierBundle getIdentifierBundle(ValueSpecification specification) {
    return IdentifierBundle.of(specification.getTargetSpecification().getIdentifier());
  }
  
  // --------------------------------------------------------------------------
  
  private class SaveTask implements Runnable {
    private ResolutionRecord _resolution;
    
    public SaveTask(ResolutionRecord resolution) {
      ArgumentChecker.notNull(resolution, "resolution");
      _resolution = resolution;      
    }
    
    @Override
    public void run() {
      try {
        save(_resolution);
      } catch (RuntimeException e) {
        s_logger.error("Updating intraday time series for " + _resolution + " failed", e);
      }
    }
  }
  
  private void save(ResolutionRecord resolution) {
    for (Iterator<Map.Entry<String, ViewComputationResultModel>> it = _viewName2LastResult.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, ViewComputationResultModel> entry = it.next();
      String viewName = entry.getKey();
      ViewComputationResultModel lastResult = entry.getValue();
      
      View view = _viewProcessor.getView(viewName, getUser());
      if (view == null || !view.isLiveComputationRunning()) {
        s_logger.debug("View {} not running, not writing any history", viewName);
        it.remove(); // help garbage-collect the ViewComputationResultModel
        continue;
      }
      
      save(resolution, lastResult);
    }
  } 
  
  private void save(ResolutionRecord resolution, ViewComputationResultModel lastResult) {
    
    Instant now = Instant.nowSystemClock();

    for (String calcConf : lastResult.getCalculationConfigurationNames()) {
      ViewCalculationResultModel result = lastResult.getCalculationResult(calcConf);
      
      for (ComputationTargetSpecification spec : result.getAllTargets()) {
        Map<String, ComputedValue> values = result.getValues(spec);
        
        for (ComputedValue value : values.values()) {
          
          if (!(value.getValue() instanceof Double)) {
            s_logger.debug(value + " is not a double");
            continue;
          }
          
          ValueSpecification valueSpecification = value.getSpecification();
          
          IdentifierBundle identifiers = getIdentifierBundle(valueSpecification);
          String dataProvider = getDataProvider(lastResult.getViewName(), calcConf);
          String fieldName = getFieldName(valueSpecification);

          UniqueIdentifier seriesUid;
          _timeSeriesMasterLock.lock();
          try {
            seriesUid = _timeSeriesMaster.resolveIdentifier(
                identifiers,
                null,
                getDataSource(),
                dataProvider,
                fieldName);
          } finally {
            _timeSeriesMasterLock.unlock();
          }
          
          if (seriesUid == null) {
            
            TimeSeriesDocument<Date> timeSeries = new TimeSeriesDocument<Date>();
            //REVIEW - Yomi 20101007 may need to store the valid dates range for identifiers
            timeSeries.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
            timeSeries.setDataSource(getDataSource());
            timeSeries.setDataProvider(getDataProvider(lastResult.getViewName(), calcConf));
            timeSeries.setDataField(fieldName);
            timeSeries.setObservationTime(getObservationTime());
            
            MapDateTimeDoubleTimeSeries ts = new MapDateTimeDoubleTimeSeries();
            Date date = new Date(now.toEpochMillisLong());
            ts.putDataPoint(date, (Double) value.getValue());
            timeSeries.setTimeSeries(ts);
            
            _timeSeriesMasterLock.lock();
            try {
              _timeSeriesMaster.addTimeSeries(timeSeries);
            } finally {
              _timeSeriesMasterLock.unlock();
            }
            
          } else {
            
            Date firstDateToRetain = new Date(resolution.getFirstDateToRetain(now).toEpochMillisLong());
            
            DataPointDocument<Date> dataPoint = new DataPointDocument<Date>();
            dataPoint.setTimeSeriesId(seriesUid);
            dataPoint.setDate(new Date(now.toEpochMillisLong()));
            dataPoint.setValue((Double) value.getValue());
            
            _timeSeriesMasterLock.lock();
            try {
              // here's the reason to use _timeSeriesMasterLock. If the lock was not used,
              // you could get an inconsistent view of the time series between add and remove. 
              _timeSeriesMaster.addDataPoint(dataPoint);
              _timeSeriesMaster.removeDataPoints(seriesUid, firstDateToRetain);
            } finally {
              _timeSeriesMasterLock.unlock();
            }
          }
          
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
    
    _executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadPoolFactory("IntradayComputationCache"));
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
    
    _executorService.shutdown();
    try {
      _executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Should not have been interrupted", e);
    }
    _executorService = null;
  }
  
}
