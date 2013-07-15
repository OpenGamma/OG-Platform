/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static com.opengamma.bbg.replay.BloombergTick.BUID_KEY;
import static com.opengamma.bbg.replay.BloombergTick.RECEIVED_TS_KEY;
import static com.opengamma.bbg.replay.BloombergTickWriter.ALL_TICKS_FILENAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.time.DateUtils;

/**
 * 
 *
 * @author yomi
 */
public class TicksLoaderJob extends TerminatableJob {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(TicksLoaderJob.class);

  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  private final String _rootDir;
  private final Set<String> _securities;
  private final BlockingQueue<FudgeMsg> _ticksQueue;
  private final ZonedDateTime _startTime;
  private final long _startTimeInEpochsMillis;
  private final ZonedDateTime _endTime;
  private final long _endTimeInEpochsMillis;
  private final boolean _infiniteLoop;
  private final List<String> _files = new ArrayList<String>();
  
  public TicksLoaderJob(String rootDir, Set<String> securities,
      BlockingQueue<FudgeMsg> ticksQueue, ZonedDateTime startTime, ZonedDateTime endTime, boolean infiniteLoop) {
    ArgumentChecker.notNull(rootDir, "rootDir");
    ArgumentChecker.notNull(securities, "securities");
    ArgumentChecker.notNull(ticksQueue, "ticksQueue");
    ArgumentChecker.notNull(startTime, "startTime");
    ArgumentChecker.notNull(endTime, "endTime");
    _rootDir = rootDir;
    _securities = securities;
    _ticksQueue = ticksQueue;
    _startTime = startTime;
    _startTimeInEpochsMillis = _startTime.toInstant().toEpochMilli();
    _endTime = endTime;
    _endTimeInEpochsMillis = _endTime.toInstant().toEpochMilli();
    _infiniteLoop = infiniteLoop;
  }
  
  @Override
  public void terminate() {
    s_logger.debug("ticksLoader terminating...");
    super.terminate();
  }

  /**
   * 
   */
  private void sendTerminateMessage() {
    try {
      _ticksQueue.put(BloombergTickReplayUtils.getTerminateMessage());
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.warn("interrupted while putting terminate message on queue");
    }
  }

  @Override
  protected void postRunCycle() {
    sendTerminateMessage();
  }

  @Override
  protected void runOneCycle() {
    for (String fullPath : _files) {
      if (fullPath == null) {
        continue;
      }
      if (!loadTicks(fullPath)) {
        // End already reached, so forget any remaining files
        break;
      }
    }
    if (!_infiniteLoop) {
      terminate();
    }
  }
  
  /**
   * @param fullPath
   * @return  <code>true</code> if the end has been reached, <code>false</code> otherwise
   */
  private boolean loadTicks(String fullPath) {
    try {
      FileInputStream fis = new FileInputStream(fullPath);
      FudgeMsgReader reader = s_fudgeContext.createMessageReader(fis);
      try {
        while (reader.hasNext()) {
          FudgeMsg message = reader.nextMessage();
          String buid = message.getString(BUID_KEY);
          if (afterEndTime(message)) {
            return false;
          }
          boolean isRequestedSecurity = _securities.contains(buid) || _securities.isEmpty();
          boolean isRequestedTime = withinRequestedTime(message);
          if (isRequestedSecurity && isRequestedTime) {
            try {
              _ticksQueue.put(message);
            } catch (InterruptedException e) {
              Thread.interrupted();
              s_logger.warn("interrupted waiting to write to ticks queue");
            }
          }
        }
        return true;
      } finally {
        try {
          fis.close();
        } catch (IOException e) {
          s_logger.warn("cannot close {}", fullPath);
        }
      }
    } catch (FileNotFoundException e) {
      s_logger.warn("{} not found", fullPath);
      throw new OpenGammaRuntimeException(fullPath + " not found", e);
    }
    
  }

  /**
   * @param message
   * @return
   */
  private boolean afterEndTime(FudgeMsg message) {
    boolean result = false;
    Long epochMillis = message.getLong(RECEIVED_TS_KEY);
    if (epochMillis != null) {
      result = epochMillis > _endTimeInEpochsMillis;
    }
    return result;
  }

  /**
   * @param message
   * @return
   */
  private boolean withinRequestedTime(FudgeMsg message) {
    boolean result = false;
    Long epochMillis = message.getLong(RECEIVED_TS_KEY);
    if (epochMillis != null) {
      result = epochMillis >= _startTimeInEpochsMillis && epochMillis <= _endTimeInEpochsMillis;
    }
    return result;
  }

  @Override
  protected void preStart() {
    LocalDate startDate = _startTime.toLocalDate();
    LocalDate endDate = _endTime.toLocalDate();

    LocalDate current = endDate;
    List<String> reverseOrder = new ArrayList<String>();
    while (current.isAfter(startDate) || current.equals(startDate)) {
      String fullPath = getFileNameFromDate(current);
      File file = new File(fullPath);
      if (file.exists()) {
        reverseOrder.add(fullPath);
      } else {
        s_logger.warn("{} does not exists ", file);
      }
      current = DateUtils.previousWeekDay(current);
    }
    ListIterator<String> reverseIterator = reverseOrder.listIterator(reverseOrder.size());
    while (reverseIterator.hasPrevious()) {
      _files.add(reverseIterator.previous());
    }
  }

  /**
   * @param startDate
   * @return
   */
  private String getFileNameFromDate(LocalDate date) {
    StringBuilder buf = new StringBuilder();
    buf.append(_rootDir).append(File.separator).append(date.getYear()).append(File.separator);
    int month = date.getMonthValue();
    if (month < 10) {
      buf.append("0").append(month);
    } else {
      buf.append(month);
    }
    buf.append(File.separator);
    int dayOfMonth = date.getDayOfMonth();
    if (dayOfMonth < 10) {
      buf.append("0").append(dayOfMonth);
    } else {
      buf.append(dayOfMonth);
    }
    buf.append(File.separator).append(ALL_TICKS_FILENAME);
    return buf.toString();
  }

}
