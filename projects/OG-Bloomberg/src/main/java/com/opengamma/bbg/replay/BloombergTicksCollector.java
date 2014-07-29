/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static com.opengamma.bbg.replay.BloombergTick.BUID_KEY;
import static com.opengamma.bbg.replay.BloombergTick.FIELDS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.RECEIVED_TS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.SECURITY_KEY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.bbg.util.SessionOptionsUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Collects ticks from Bloomberg for later replay.
 */
public class BloombergTicksCollector implements Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergTicksCollector.class);

  private static final FudgeContext s_fudgeContext = new FudgeContext();

  private static final String DEFAULT_TRACK_FILE = "/watchList.txt";
  private static final int DEFAULT_SESSION_SIZE = 4;
  private static final StorageMode DEFAULT_STORAGE_MODE = StorageMode.SINGLE;

  /**
   * Default ticks root directory
   */
  public static final String DEFAULT_ROOT_DIR = "/tickData";

  private BloombergConnector _bloombergConnector;
  private List<Session> _sessionList = Lists.newArrayList();
      
  private List<String> _options = Lists.newArrayList();
  private AtomicBoolean _bbgSessionStarted = new AtomicBoolean();
  private List<SubscriptionList> _subscriptionsList = Lists.newArrayList();
  
  private ReferenceDataProvider _refDataProvider;
  private String _rootDir;
  private BloombergTickWriter _ticksWriterJob;
  private Thread _ticksWriterThread;
  private BlockingQueue<FudgeMsg> _allTicksQueue = new LinkedBlockingQueue<FudgeMsg>();
  private String _trackFile;
  private int _bbgSessions;
  
  private StorageMode _storageMode;
  
  public BloombergTicksCollector(BloombergConnector sessionOptions, ReferenceDataProvider refDataProvider) {
    this(sessionOptions, refDataProvider, DEFAULT_ROOT_DIR, DEFAULT_TRACK_FILE, DEFAULT_SESSION_SIZE, DEFAULT_STORAGE_MODE);
  }
  
  public BloombergTicksCollector(BloombergConnector sessionOptions, ReferenceDataProvider refDataProvider, String rootDir) {
    this(sessionOptions, refDataProvider, rootDir, DEFAULT_TRACK_FILE, DEFAULT_SESSION_SIZE, DEFAULT_STORAGE_MODE);
  }
  
  /**
   * @param sessionOptions the bloomberg session options, not null.
   * @param refDataProvider the reference data provider, not null
   * @param rootDir the base directory to write ticks to, not null
   * @param trackFile the watchList file containing identifiers, not null
   * @param bbgSessions the number of bloomberg sessions to create, must be positive
   * @param storageMode the storage mode, not null
   */
  public BloombergTicksCollector(BloombergConnector sessionOptions, ReferenceDataProvider refDataProvider, 
      String rootDir, String trackFile, int bbgSessions, StorageMode storageMode) {
    
    ArgumentChecker.notNull(sessionOptions, "SessionOptions");
    ArgumentChecker.notNull(refDataProvider, "BloombergRefDataProvider");
    ArgumentChecker.notNull(rootDir, "RootDir");
    ArgumentChecker.notNull(trackFile, "trackFile");
    ArgumentChecker.notNull(storageMode, "storageMode");
    if (bbgSessions < 1) {
      throw new IllegalArgumentException("Bloomberg sessions must be greater than zero");
    }
    _storageMode = storageMode;
    _bloombergConnector = sessionOptions;
    _refDataProvider = refDataProvider;
    _rootDir = rootDir;
    _trackFile = trackFile;
    _bbgSessions = bbgSessions;
    checkRootDir();
  }

  //-------------------------------------------------------------------------
  private void checkRootDir() {
    File file = new File(_rootDir);
    if (!file.isDirectory()) { 
      s_logger.warn("{} root directory does not exist", _rootDir);
      throw new IllegalArgumentException(_rootDir + " is not a directory");
    }
    if (!file.canWrite()) {
      throw new IllegalArgumentException(" cannot write to " + _rootDir);
    }
    if (!file.canRead()) {
      throw new IllegalArgumentException(" cannot read from " + _rootDir);
    }
    if (!file.canExecute()) {
      throw new IllegalArgumentException(" cannot execute " + _rootDir);
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return (_bbgSessionStarted.get() == true || (_ticksWriterThread != null && _ticksWriterThread.isAlive()));
  }

  @Override
  public synchronized void start() {
    s_logger.info("starting bloombergTickCollector");
    if (isRunning()) {
      s_logger.info("BloombergTickStorage already started");
      return;
    }
    
    Map<String, String> ticker2Buid = BloombergDataUtils.getBUID(_refDataProvider, loadSecurities());
    doSnapshot(ticker2Buid);
    
    //setup writer thread
    BloombergTickWriter tickWriter = new BloombergTickWriter(s_fudgeContext, _allTicksQueue, ticker2Buid, _rootDir, _storageMode);
    Thread thread = new Thread(tickWriter, "TicksWriter");
    thread.start();
    _ticksWriterJob = tickWriter;
    _ticksWriterThread = thread;
        
    createSubscriptions(ticker2Buid.keySet());
    boolean sessionCreated = false;
    try {
      sessionCreated = createSession();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Session cannot be open to Bloomberg", e);
    }
    _bbgSessionStarted.set(sessionCreated);
  }

  private Set<String> loadSecurities() {
    Set<String> bloombergKeys = Sets.newHashSet();
    try {
      for (ExternalId identifier : BloombergDataUtils.identifierLoader(new FileReader(_trackFile))) {
        bloombergKeys.add(BloombergDomainIdentifierResolver.toBloombergKey(identifier));
      }
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException(_trackFile + " cannot be found", ex);
    }
    return bloombergKeys;
  }
  
  private void doSnapshot(Map<String, String> ticker2Buid) {
    ReferenceDataProvider refDataProvider = _refDataProvider;
    Map<String, FudgeMsg> refDataValues = refDataProvider.getReferenceDataIgnoreCache(ticker2Buid.keySet(), BloombergDataUtils.STANDARD_FIELDS_SET);
    
    for (String bloombergKey : ticker2Buid.keySet()) {
      FudgeMsg result = refDataValues.get(bloombergKey);
      if (result == null) {
        throw new OpenGammaRuntimeException("Result for " + bloombergKey + " was not found");
      }
      
      MutableFudgeMsg tickMsg = s_fudgeContext.newMessage();
      Instant instant = Clock.systemUTC().instant();
      long epochMillis = instant.toEpochMilli();
      tickMsg.add(RECEIVED_TS_KEY, epochMillis);
      tickMsg.add(SECURITY_KEY, bloombergKey);
      tickMsg.add(FIELDS_KEY, result);
      tickMsg.add(BUID_KEY, ticker2Buid.get(bloombergKey));
      try {
        _allTicksQueue.put(tickMsg);
      } catch (InterruptedException ex) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to do snaphot for " + bloombergKey, ex);
      }
    }
  }

  /**
   * @param bloombergKeys
   */
  private void createSubscriptions(Set<String> bloombergKeys) {
    s_logger.debug("creating subscriptions list for {} securities", bloombergKeys.size());
    for (int i = 0; i < _bbgSessions; i++) {
      _subscriptionsList.add(new SubscriptionList());
    }
    int counter = 0;
    for (String bloombergKey : bloombergKeys) {
      int index = counter % _bbgSessions;
      SubscriptionList subscriptions = _subscriptionsList.get(index);
      subscriptions.add(new Subscription(bloombergKey, BloombergDataUtils.STANDARD_FIELDS_LIST, _options,
          new CorrelationID(bloombergKey)));
      counter++;
    }
    
  }

  @Override
  public synchronized void stop() {
    s_logger.info("Stopping marketdata storage serivce");
    stopBloombergSession();
    stopTicksWriterThreads();
  }

  /**
   * 
   */
  public void stopBloombergSession() {
    s_logger.info("stopping bloomberg session");
    if (_bbgSessionStarted.get()) {
      for (Session session : _sessionList) {
        try {
          session.stop();
        } catch (InterruptedException e) {
          Thread.interrupted();
          s_logger.warn("Interrupted while waiting for session to stop", e);
        }
      }
      _sessionList = null;
      _bbgSessionStarted.set(false);
    }
  }

  /**
   * 
   */
  public void stopTicksWriterThreads() {
    s_logger.info("Stopping ticks writer thread");
    if (_ticksWriterThread != null && _ticksWriterThread.isAlive()) {
      if (_ticksWriterJob != null) {
        try {
          _allTicksQueue.put(BloombergTickReplayUtils.getTerminateMessage());
        } catch (InterruptedException e) {
          Thread.interrupted();
          s_logger.warn("interrupted from waiting to put terminate message on queue");
        }
      }
      try {
        _ticksWriterThread.join();
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.warn("Interrupted while waiting for ticks writer thread to terminate", e);
      }
    }
    _ticksWriterJob = null;
    _ticksWriterThread = null;
  }

  private boolean createSession() throws Exception {
    for (Session session : _sessionList) {
      if (session != null) {
        session.stop();
      }
    }
    s_logger.info("Connecting to {} ", SessionOptionsUtils.toString(_bloombergConnector.getSessionOptions()));
    BloombergTickCollectorHandler handler = new BloombergTickCollectorHandler(_allTicksQueue, this);
    for (int i = 0; i < _bbgSessions; i++) {
      Session session = new Session(_bloombergConnector.getSessionOptions(), handler);
      if (!session.start()) {
        s_logger.info("Failed to start session");
        return false;
      }
      if (!session.openService("//blp/mktdata")) {
        s_logger.info("Failed to open service //blp/mktdata");
        session.stop();
        return false;
      }
      _sessionList.add(session);
    }
    s_logger.info("Connected successfully\n");
    
    s_logger.info("Subscribing ...");
    int index = 0;
    for (Session session : _sessionList) {
      session.subscribe(_subscriptionsList.get(index));
      index++;
    }
    return true;
  }

  public boolean isTickWriterAlive() {
    return _ticksWriterThread != null && _ticksWriterThread.isAlive();
  }

}
