/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.performance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A very simple timer
 */
public class SimpleTimer {
  private final Logger _timerLog = LoggerFactory.getLogger(SimpleTimer.class);
  private long _startTime;
  private long _stopTime;
  private long _elapsedTime;
  private long _totalTime;  
  private boolean _isRunning;
  private boolean _hasBeenStarted;
  private boolean _hasBeenStopped;

  public void startTimer() {
    if (_isRunning) {
      _timerLog.error("Method startTimer() requested but the timer is already running.");
    }     
    _isRunning = true;
    _hasBeenStarted = true;    
    _startTime = getCurrentTime();
  }
   
  public void stopTimer() {
    if (!_isRunning) {
      _timerLog.error("Method stopTimer() requested but the timer is not running.");
    }
    _isRunning = false;
    _hasBeenStopped = true;    
    _stopTime = getCurrentTime();
  }

  public long elapsedTime() {
    if (!_hasBeenStarted) {
      _timerLog.error("Method elapsedTime() requested but the timer has not been started OR has been reset.");
    }
    _elapsedTime = getCurrentTime() - _startTime;
    return _elapsedTime;
  }
  
  public long totalTime() {
    if (!_hasBeenStarted || !_hasBeenStopped) {
      _timerLog.error("Method totalTime() requested but the timer has not been started and stopped.");
    }    
    _totalTime = _stopTime - _startTime;
    return _totalTime; 
  }  
  
  private long getCurrentTime() {
    long time;
    time = System.nanoTime();
    return time;
  }
}

