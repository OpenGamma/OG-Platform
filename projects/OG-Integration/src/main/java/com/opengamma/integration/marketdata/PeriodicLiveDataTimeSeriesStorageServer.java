/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata;

import static org.threeten.bp.temporal.ChronoUnit.HOURS;
import static org.threeten.bp.temporal.ChronoUnit.MINUTES;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.Clock;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import au.com.bytecode.opencsv.CSVParser;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Will subscribe to a set of live data elements and periodically write those
 * values out to the Historical Time Series system.
 * <p/>
 * The current implementation writes everything subscribed hourly to an
 * observation time with the name of the hour on which it fires <b>IN UTC</b>.
 */
public class PeriodicLiveDataTimeSeriesStorageServer implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(PeriodicLiveDataTimeSeriesStorageServer.class);
  private final UserPrincipal _liveDataUser;
  private final LiveDataClient _liveDataClient;
  private final HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  private final HistoricalTimeSeriesMasterUtils _htsUtils;
  private final String _dataSource;
  private final String _dataProvider;
  private final boolean _writeToDatabase;
  private String _initializationFileName;
  
  private final ScheduledExecutorService _timerExecutor;
  private final ExecutorService _storageExecutor;
  private final ConcurrentMap<LiveDataSpecification, FudgeMsg> _allValues = new ConcurrentHashMap<LiveDataSpecification, FudgeMsg>();
  
  public PeriodicLiveDataTimeSeriesStorageServer(
      String userName,
      LiveDataClient liveDataClient,
      HistoricalTimeSeriesMaster htsMaster,
      String dataSource,
      String dataProvider,
      boolean writeToDatabase) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    if (userName == null) {
      userName = System.getProperty("user.name");
    }
    _liveDataUser = UserPrincipal.getLocalUser(userName);
    _liveDataClient = liveDataClient;
    _historicalTimeSeriesMaster = htsMaster;
    _htsUtils = new HistoricalTimeSeriesMasterUtils(htsMaster);
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _writeToDatabase = writeToDatabase;
    _timerExecutor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
      private final AtomicInteger _threadCount = new AtomicInteger(0);

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "PeriodicLiveDataTimeSeriesStorageServer-Timer-" + _threadCount.getAndIncrement());
        t.setDaemon(false);
        return t;
      }
      
    });
    
    _storageExecutor = new ThreadPoolExecutor(3, 10, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
      private final AtomicInteger _threadCount = new AtomicInteger(0);

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "PeriodicLiveDataTimeSeriesStorageServer-Storage-" + _threadCount.getAndIncrement());
        t.setDaemon(false);
        return t;
      }
    }, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  /**
   * Gets the liveDataClient.
   * @return the liveDataClient
   */
  public LiveDataClient getLiveDataClient() {
    return _liveDataClient;
  }

  /**
   * Gets the historicalTimeSeriesMaster.
   * @return the historicalTimeSeriesMaster
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Gets the liveDataUser.
   * @return the liveDataUser
   */
  public UserPrincipal getLiveDataUser() {
    return _liveDataUser;
  }

  /**
   * Gets the dataSource.
   * @return the dataSource
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Gets the dataProvider.
   * @return the dataProvider
   */
  public String getDataProvider() {
    return _dataProvider;
  }

  /**
   * Gets the initializationFileName.
   * @return the initializationFileName
   */
  public String getInitializationFileName() {
    return _initializationFileName;
  }

  /**
   * Sets the initializationFileName.
   * If specified, this file (in CSV format with no header row) will be used
   * to setup subscriptions on initialization.
   * @param initializationFileName  the initializationFileName
   */
  public void setInitializationFileName(String initializationFileName) {
    _initializationFileName = initializationFileName;
  }

  /**
   * Gets the writeToDatabase.
   * @return the writeToDatabase
   */
  public boolean isWriteToDatabase() {
    return _writeToDatabase;
  }

  public void addSubscription(ExternalId id, String normalizationSet) {
    LiveDataSpecification ldSpec = new LiveDataSpecification(normalizationSet, id);
    s_logger.warn("Subscribing to {}", ldSpec);
    getLiveDataClient().subscribe(getLiveDataUser(), ldSpec, new LiveDataListener() {

      @Override
      public void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult) {
        s_logger.warn("Subscription result of {}", subscriptionResult);
      }

      @Override
      public void subscriptionResultsReceived(Collection<LiveDataSubscriptionResponse> subscriptionResults) {
        s_logger.warn("Sub result {}", subscriptionResults);
      }

      @Override
      public void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification) {
        s_logger.warn("Subscription stopped to {}", fullyQualifiedSpecification);
      }

      @Override
      public void valueUpdate(LiveDataValueUpdate valueUpdate) {
        _allValues.put(valueUpdate.getSpecification(), valueUpdate.getFields());
      }
       
    });
    
  }
  

  /**
   * The task that will snapshot the state of the market and create the storage
   * tasks.
   */
  private class SnapshotTask implements Runnable {

    @Override
    public void run() {
      for (Map.Entry<LiveDataSpecification, FudgeMsg> entry : _allValues.entrySet()) {
        OffsetDateTime atTheHour = OffsetDateTime.now(Clock.systemUTC()).truncatedTo(MINUTES);
        if (atTheHour.getMinute() >= 55) {
          // Assume we got triggered early.
          atTheHour = atTheHour.withMinute(0).plusHours(1);
        } else {
          atTheHour = atTheHour.withMinute(0);
        }
        String observationTimeName = atTheHour.toOffsetTime().toString();
        try {
          _storageExecutor.execute(new StorageTask(entry.getKey(), entry.getValue(), atTheHour.toLocalDate(), observationTimeName));
        } catch (Exception e) {
          s_logger.error("Unable to submit a storage task to store {} {}", entry.getKey(), entry.getValue());
        }
      }
    }
    
  }
  
  /**
   * The task that will actually write values to the DB.
   */
  private class StorageTask implements Runnable {
    private final LiveDataSpecification _liveDataSpecification;
    private final FudgeMsg _values;
    private final LocalDate _date;
    private final String _observationTimeName;
    
    public StorageTask(LiveDataSpecification ldSpec, FudgeMsg values, LocalDate date, String observationTimeName) {
      _liveDataSpecification = ldSpec;
      _values = values;
      _date = date;
      _observationTimeName = observationTimeName;
    }

    @Override
    public void run() {
      String description = _liveDataSpecification.getIdentifiers().getExternalIds().iterator().next().toString();
      for (FudgeField field : _values.getAllFields()) {
        if (isWriteToDatabase()) {
          // TODO kirk 2012-07-19 -- The first time this starts it should be able to identify
          // the underlying identifiers for the HTS entries for each TS and cache those in RAM
          // to avoid DB thrashing.
          _htsUtils.writeTimeSeriesPoint(
              description,
              getDataSource(),
              getDataProvider(),
              field.getName(),
              _observationTimeName,
              _liveDataSpecification.getIdentifiers(),
              _date,
              (Double) field.getValue());
        } else {
          s_logger.error("Would write {} {} {} {} {} {} {} {}",
              new Object[] {description, getDataSource(), getDataProvider(), field.getName(), _observationTimeName,
                            _liveDataSpecification.getIdentifiers().toString(), _date, field.getValue()});
        }
      }
    }
    
  }

  @Override
  public void start() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime nextHour = now.truncatedTo(HOURS).plusHours(1);
    Duration delay = Duration.between(now.atOffset(ZoneOffset.UTC), nextHour.atOffset(ZoneOffset.UTC));
    Duration oneHour = Duration.ofHours(1);
    s_logger.warn("Now {} Next {} Delay {} {}", new Object[] {now, nextHour, delay, delay.toMillis() });
    _timerExecutor.scheduleAtFixedRate(new SnapshotTask(), delay.toMillis(), oneHour.toMillis(), TimeUnit.MILLISECONDS);
    if (getInitializationFileName() != null) {
      initializeFromFile(getInitializationFileName());
    }
  }

  /**
   * @param initializationFileName The name of the file to load in CSV format.
   */
  public void initializeFromFile(String initializationFileName) {
    File f = new File(initializationFileName);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(f);
      initializeFromStream(fis);
    } catch (IOException ioe) {
      s_logger.error("Unable to load subscriptions from file", ioe);
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException ioe) {
        s_logger.warn("Unable to close initialization file", ioe);
      }
    }
  }

  /**
   * @param is The input stream to load
   */
  public void initializeFromStream(InputStream is) throws IOException {
    CSVParser parser = new CSVParser();
    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    String line = r.readLine();
    while (line != null) {
      String[] fields = parser.parseLine(line);
      if (fields.length != 3) {
        s_logger.warn("Line {} not in proper format.", line);
      } else {
        String scheme = fields[0];
        String id = fields[1];
        String normalization = fields[2];
        addSubscription(ExternalId.of(scheme, id), normalization);
        try {
          Thread.sleep(200L);
        } catch (InterruptedException ex) {
          // TODO Auto-generated catch block
          ex.printStackTrace();
        }
      }
      line = r.readLine();
    }
  }

  @Override
  public void stop() {
    _timerExecutor.shutdown();
    _storageExecutor.shutdown();
  }

  @Override
  public boolean isRunning() {
    return !_storageExecutor.isTerminated();
  }

}
