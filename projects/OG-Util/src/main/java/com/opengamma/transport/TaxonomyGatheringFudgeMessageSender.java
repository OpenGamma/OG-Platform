/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class TaxonomyGatheringFudgeMessageSender implements FudgeMessageSender {
  private static final Logger s_logger = LoggerFactory.getLogger(TaxonomyGatheringFudgeMessageSender.class);
  private static final long DEFAULT_PERIOD = 60 * 1000L; 
  private final FudgeMessageSender _underlying;
  private final FudgeContext _fudgeContext;
  private final File _outputFile;
  private final File _temporaryFile;
  private final long _rewritePeriod;
  private final ConcurrentMap<String, Integer> _taxonomyValues = new ConcurrentHashMap<String, Integer>();
  private final AtomicInteger _nextOrdinal = new AtomicInteger(1);
  private final AtomicReference<CountDownLatch> _waitForNextWrite = new AtomicReference<CountDownLatch>();
  /**
   * The next ordinal the last time that the file was written, to avoid writing when
   * there's been no changes.
   */
  private final AtomicInteger _lastMaxOrdinalWritten = new AtomicInteger(1);
  private final Timer _timer;
  
  public TaxonomyGatheringFudgeMessageSender(FudgeMessageSender underlying, String outputFileName) {
    this(underlying, outputFileName, FudgeContext.GLOBAL_DEFAULT);
  }
  
  public TaxonomyGatheringFudgeMessageSender(FudgeMessageSender underlying, String outputFileName, FudgeContext fudgeContext) {
    this(underlying, outputFileName, FudgeContext.GLOBAL_DEFAULT, DEFAULT_PERIOD);
  }
  
  public TaxonomyGatheringFudgeMessageSender(FudgeMessageSender underlying, String outputFileName, FudgeContext fudgeContext, long fileWritePeriod) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notEmpty(outputFileName, "outputFileName");
    ArgumentChecker.isTrue(fileWritePeriod > 0, "File write period must be positive");
    
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    _rewritePeriod = fileWritePeriod;
    
    File outputFile = new File(outputFileName);
    if (outputFile.exists()) {
      ArgumentChecker.isTrue(outputFile.canRead(), "Must be able to read the output file");
      ArgumentChecker.isTrue(outputFile.canWrite(), "Must be able to write the output file");
    }
    _outputFile = outputFile;
    File temporaryFile = new File(outputFileName + ".tmp");
    if (temporaryFile.exists()) {
      ArgumentChecker.isTrue(temporaryFile.canWrite(), "Must be able to write to a temporary output file");
    }
    _temporaryFile = temporaryFile;
    
    bootstrapTaxonomy();
    
    _timer = new Timer("TaxonomyGatheringFudgeMessageSender", true);
    _timer.schedule(new PropertyWritingTimerTask(), 0, _rewritePeriod);
  }

  /**
   * 
   */
  private void bootstrapTaxonomy() {
    if (!_outputFile.exists()) {
      s_logger.debug("Existing file doesn't exist, so not bootstrapping.");
      return;
    }
    s_logger.info("Bootstrapping taxonomy from {}", _outputFile);
    Properties propsFromFile = null;
    try (InputStream underlyingInputStream = new BufferedInputStream(new FileInputStream(_outputFile))) {
      propsFromFile = new Properties();
      propsFromFile.load(underlyingInputStream);
    } catch (IOException ioe) {
      s_logger.warn("Unable to load existing properties from {}", ioe, new Object[]{_outputFile});
    }
    
    int maxOrdinal = 0; 
    if (propsFromFile != null) {
      for (Map.Entry<Object, Object> entry : propsFromFile.entrySet()) {
        String keyString = (String) entry.getKey();
        String valueString = (String) entry.getValue();
        int ordinal = Integer.parseInt(keyString);
        maxOrdinal = Math.max(maxOrdinal, ordinal);
        _taxonomyValues.put(valueString, ordinal);
      }
    }
    _nextOrdinal.set(maxOrdinal + 1);
    _lastMaxOrdinalWritten.set(_nextOrdinal.get());
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  protected ConcurrentMap<String, Integer> getCurrentTaxonomy() {
    return _taxonomyValues;
  }
  
  protected Timer getTimer() {
    return _timer;
  }

  @Override
  public void send(FudgeMsg message) {
    gatherFieldNames(message);
    _underlying.send(message);
  }
  
  /**
   * @param message
   */
  private void gatherFieldNames(FudgeMsg message) {
    for (FudgeField field : message) {
      if (field.getName() == null) {
        continue;
      }
      if (field.getOrdinal() != null) {
        continue;
      }
      // Yes, we double-check here. That's fine, as we want to avoid collisions
      // that will result in gaps in the taxonomy ordinal. The chances
      // of a collision here is small enough that we're fine if we have a gap,
      // but it wouldn't be if we did .putIfAbsent() without the first containsKey()
      // check.
      if (_taxonomyValues.containsKey(field.getName())) {
        continue;
      }
      _taxonomyValues.putIfAbsent(field.getName(), _nextOrdinal.getAndIncrement());
      if (field.getValue() instanceof FudgeMsg) {
        gatherFieldNames((FudgeMsg) field.getValue());
      }
    }
  }

  /**
   * Wait for the next write to occur that will have flushed the last of the field names collected by
   * the previous call to send.
   */
  protected void waitForNextWrite() throws InterruptedException {
    final int nextOrdinal = _nextOrdinal.get();
    while (_lastMaxOrdinalWritten.get() < nextOrdinal) {
      CountDownLatch latch = new CountDownLatch(1);
      if (_waitForNextWrite.compareAndSet(null, latch)) {
        // Latch available for the writing thread
        if (_lastMaxOrdinalWritten.get() >= nextOrdinal) {
          // Writing thread updated the last max; it may or may not have taken
          // the latch at this point but that doesn't matter. At worse it will
          // waste a "countDown" operation on it.
          return;
        }
      } else {
        latch = _waitForNextWrite.get();
        // If latch is now null then a write must have happened, so don't need to block
        if (latch == null) {
          return;
        }
      }
      latch.await();
    }
  }

  private class PropertyWritingTimerTask extends TimerTask {

    @Override
    public void run() {
      final int nextOrdinal = _nextOrdinal.get();
      if (_lastMaxOrdinalWritten.get() >= nextOrdinal) {
        s_logger.debug("No reason to write taxonomy as no changes since last persist.");
      } else {
        s_logger.info("Writing current taxonomy of {} values to {}", _taxonomyValues.size(), _outputFile);
        Properties props = new Properties();
        for (Map.Entry<String, Integer> taxonomyEntry : _taxonomyValues.entrySet()) {
          props.setProperty(taxonomyEntry.getValue().toString(), taxonomyEntry.getKey());
        }
        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(_temporaryFile);
          props.store(new BufferedOutputStream(fos), "Automatically generated, written " + new Date());
        } catch (IOException ioe) {
          s_logger.warn("Unable to write taxonomy to file {}", ioe, new Object[] {_temporaryFile});
        } finally {
          if (fos != null) {
            try {
              fos.close();
              if (!_temporaryFile.renameTo(_outputFile)) {
                boolean ok = false;
                if (_outputFile.exists()) {
                  _outputFile.delete();
                  ok = _temporaryFile.renameTo(_outputFile);
                }
                if (!ok) {
                  s_logger.warn("Unable to rename temporary file {} to {}", _temporaryFile, _outputFile);
                }
              }
            } catch (IOException ioe) {
              s_logger.warn("Unable to close output file {}", ioe, new Object[] {_temporaryFile});
            }
          }
        }
        _lastMaxOrdinalWritten.set(nextOrdinal);
        final CountDownLatch latch = _waitForNextWrite.getAndSet(null);
        if (latch != null) {
          latch.countDown();
        }
      }
    }
    
  }

}
