/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.replay.BloombergTicksReplayer.Mode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class TicksPlayerJob extends TerminatableJob {

  /** Logger/ */
  private static final Logger s_logger = LoggerFactory.getLogger(TicksPlayerJob.class);

  private BlockingQueue<FudgeMsg> _ticksQueue;
  private BloombergTickReceiver _tickReceiver;
  private Mode _mode;
  private Thread _ticksLoaderThread;

  public TicksPlayerJob(BlockingQueue<FudgeMsg> ticksQueue, BloombergTickReceiver tickReceiver, Mode mode, Thread ticksLoaderThread) {
    ArgumentChecker.notNull(ticksQueue, "ticksQueue");
    ArgumentChecker.notNull(tickReceiver, "tickReceiver");
    ArgumentChecker.notNull(mode, "mode");
    ArgumentChecker.notNull(ticksLoaderThread, "ticksLoaderThread");
    _ticksQueue = ticksQueue;
    _tickReceiver = tickReceiver;
    _mode = mode;
    _ticksLoaderThread = ticksLoaderThread;
  }

  @Override
  public void terminate() {
    s_logger.debug("ticksPlayer terminating...");
    super.terminate();
  }

  @Override
  protected void runOneCycle() {
    if (!_ticksLoaderThread.isAlive() && _ticksQueue.isEmpty()) {
      terminate();
    } else {
      playNextTick();
    }
  }

  /**
   * @param nextTick
   * 
   */
  private void playNextTick() {
    FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    switch (_mode) {
      case ORIGINAL_LATENCY:
        BloombergTick currentTick = null;;
        try {
          FudgeMsg msg = _ticksQueue.take();
          if (msg != null && BloombergTickReplayUtils.isTerminateMsg(msg)) {
            s_logger.debug("received terminate message");
            terminate();
            return;
          }
          currentTick = BloombergTick.fromFudgeMsg(deserializer, msg);
          long ts1 = System.currentTimeMillis();
          _tickReceiver.tickReceived(currentTick);
          long ts2 = System.currentTimeMillis();
          FudgeMsg nextMsg = _ticksQueue.peek();
          if (nextMsg != null && !BloombergTickReplayUtils.isTerminateMsg(nextMsg)) {
            BloombergTick nextTick = BloombergTick.fromFudgeMsg(deserializer, nextMsg);
            long tickLatency = nextTick.getReceivedTS() - currentTick.getReceivedTS();
            long sleepTime = tickLatency - (ts2 - ts1);
            s_logger.debug("sleeping for {}ms,", sleepTime);
            if (sleepTime > 0) {
              try {
                Thread.sleep(sleepTime);
              } catch (InterruptedException e) {
                Thread.interrupted();
                s_logger.warn("interrupted from keeping time difference between ticks");
              }
            }
          }
        } catch (InterruptedException e1) {
          Thread.interrupted();
          s_logger.warn("interrupted while waiting to read ticks to play");
        }
        break;
      case AS_FAST_AS_POSSIBLE:
        BloombergTick tick = null;
        try {
          FudgeMsg msg = _ticksQueue.take();
          if (msg != null && BloombergTickReplayUtils.isTerminateMsg(msg)) {
            s_logger.debug("received terminate message");
            terminate();
            return;
          }
          tick = BloombergTick.fromFudgeMsg(deserializer, msg);
          _tickReceiver.tickReceived(tick);
        } catch (InterruptedException e) {
          Thread.interrupted();
          s_logger.warn("interrupted while waiting to read ticks to play");
        }
        break;
      default:
        break;
    }
  }
}
