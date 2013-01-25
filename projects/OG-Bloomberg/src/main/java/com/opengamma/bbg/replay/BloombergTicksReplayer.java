/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * 
 */
public class BloombergTicksReplayer implements Lifecycle {

  /** Logger. */
  private static Logger s_logger = LoggerFactory.getLogger(BloombergTicksReplayer.class);

  private static final int DEFAULT_QUEUE_SIZE = 1000;
  private final BloombergTickReceiver _bloombergTickReceiver;
  private final String _rootDir;
  private final BlockingQueue<FudgeMsg> _ticksQueue = new ArrayBlockingQueue<FudgeMsg>(DEFAULT_QUEUE_SIZE);
  private final Mode _mode;
  private final ZonedDateTime _startTime;
  private final ZonedDateTime _endTime;
  private final boolean _infiniteLoop;
  private final Set<String> _securities;
  
  private Thread _tickPlayerThread;
  private TerminatableJob _ticksPlayerJob;
  private Thread _ticksLoaderThread;
  private TerminatableJob _ticksLoaderJob;
  
  //exception thrown during replay
  private Throwable _exception;

  public BloombergTicksReplayer(Mode mode, String rootDir, BloombergTickReceiver bloombergTickReceiver, ZonedDateTime startTime, ZonedDateTime endTime) {
    this(mode, rootDir, bloombergTickReceiver, startTime, endTime, false, Collections.<String>emptySet());   
  }
  
  public BloombergTicksReplayer(Mode mode, String rootDir, BloombergTickReceiver bloombergTickReceiver, ZonedDateTime startTime, ZonedDateTime endTime, boolean infiniteLoop) {
    this(mode, rootDir, bloombergTickReceiver, startTime, endTime, infiniteLoop, Collections.<String>emptySet());   
  }
  
  public BloombergTicksReplayer(Mode mode, String rootDir, BloombergTickReceiver bloombergTickReceiver, ZonedDateTime startTime, ZonedDateTime endTime, Set<String> securities) {
    this(mode, rootDir, bloombergTickReceiver, startTime, endTime, false, securities);
  }

  public BloombergTicksReplayer(Mode mode, String rootDir, BloombergTickReceiver bloombergTickReceiver, ZonedDateTime startTime, ZonedDateTime endTime, boolean infiniteLoop, Set<String> securities) {
    ArgumentChecker.notNull(rootDir, "rootDir");
    ArgumentChecker.notNull(bloombergTickReceiver, "tickHandler");
    ArgumentChecker.notNull(mode, "mode");
    ArgumentChecker.notNull(startTime, "startTime");
    ArgumentChecker.notNull(endTime, "endTime");
    ArgumentChecker.notNull(securities, "securities");
    _mode = mode;
    _rootDir = rootDir;
    _bloombergTickReceiver = bloombergTickReceiver;
    _startTime = startTime;
    _endTime = endTime;
    _infiniteLoop = infiniteLoop;
    _securities = securities;
  }

  @Override
  public synchronized boolean isRunning() {
    s_logger.debug("isLoaderRunning {}", isLoaderRunning());
    s_logger.debug("isPlayerRunning {}", isPlayerRunning());
    return isLoaderRunning() && isPlayerRunning();
  }

  /**
   * @return true if the player is running
   */
  public boolean isPlayerRunning() {
    return _tickPlayerThread != null && _tickPlayerThread.isAlive();
  }

  /**
   * @return true if the loader is running
   */
  public boolean isLoaderRunning() {
    return _ticksLoaderThread != null && _ticksLoaderThread.isAlive();
  }

  @Override
  public synchronized void start() {
    startLoader();
    //Wait for some ticks to process
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("interrupted from sleeping");
    }
    startPlayer();
  }

  /**
   * 
   */
  private void startLoader() {
    s_logger.info("starting ticksLoader-job");
    TicksLoaderJob ticksLoaderJob = new TicksLoaderJob(_rootDir, _securities, _ticksQueue, _startTime, _endTime, _infiniteLoop);
    _ticksLoaderJob = ticksLoaderJob;
    Thread thread = new Thread(_ticksLoaderJob, "TicksLoader");
//    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        _exception = e;
        s_logger.warn(e.getMessage(), e);
      }
    });
    thread.start();
    _ticksLoaderThread = thread;
  }

  /**
   * 
   */
  private void startPlayer() {
    s_logger.info("starting ticksPlayer-job");
    TicksPlayerJob ticksPlayer = new TicksPlayerJob(_ticksQueue, _bloombergTickReceiver, _mode, _ticksLoaderThread);
    _ticksPlayerJob = ticksPlayer;
    Thread thread = new Thread(_ticksPlayerJob, "TicksPlayer");
//    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        _exception = e;
        s_logger.warn(e.getMessage(), e);
      }
    });
    thread.start();
    
    _tickPlayerThread = thread;
  }

  @Override
  public synchronized void stop() {
    if (isLoaderRunning()) {
      stopTerminatableJob(_ticksLoaderJob, _ticksLoaderThread);
    }
    if (isPlayerRunning()) {
      stopTerminatableJob(_ticksPlayerJob, _tickPlayerThread);
    }
  }

  private void stopTerminatableJob(TerminatableJob terminatableJob,
      Thread thread) {
    s_logger.debug("stopping {}", thread);
    if (thread != null && thread.isAlive()) {
      if (terminatableJob != null) {
        terminatableJob.terminate();
      }
      try {
        thread.join(1000); //wait for 1sec
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.warn("Interrupted waiting for {} thread to finish", thread);
      }
    }
    
  }

  protected Throwable getException() {
    return _exception;
  }

  /**
   * An enumeration describing how to replay the ticks.
   */
  public enum Mode {
    /**
     * Replay at the original latency.
     */
    ORIGINAL_LATENCY,
    /**
     * Replay as fast as possible.
     */
    AS_FAST_AS_POSSIBLE
  }

}
