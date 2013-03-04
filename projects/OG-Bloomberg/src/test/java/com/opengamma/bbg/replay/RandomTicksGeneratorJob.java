/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static com.opengamma.bbg.replay.BloombergTick.RECEIVED_TS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.SECURITY_KEY;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A job to generate random ticks.
 */
public class RandomTicksGeneratorJob extends TerminatableJob {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(RandomTicksGeneratorJob.class);
  /**
   * The maximum message size.
   */
  private static final int MAX_MESSAGE_SIZE = 5;
  /**
   * A random seed.
   */
  public static final long RANDOM_SEED = 100L;

  /**
   * The list of required securities.
   */
  private List<String> _securities;
  /**
   * The queue of messages.
   */
  private BlockingQueue<FudgeMsg> _writerQueue;
  /**
   * The random number generator.
   */
  private Random _valueGenerator = new Random(RANDOM_SEED);
  
  /**
   * Message size generator
   */
  private Random _messageSizeGenerator = new Random();
  
  /**
   * Creates a job for a list of securities.
   * 
   * @param securities  the securities to, not null 
   * @param writerQueue  the queue to use, not null
   */
  public RandomTicksGeneratorJob(List<String> securities, BlockingQueue<FudgeMsg> writerQueue) {
    super();
    _securities = securities;
    _writerQueue = writerQueue;
  }

  @Override
  public void terminate() {
    s_logger.debug("terminating ticksGeneratorJob");
    super.terminate();
  }

  @Override
  protected void runOneCycle() {
    s_logger.debug("queueSize {} ", _writerQueue.size());
    for (String security : _securities) {
      int msgSize = _messageSizeGenerator.nextInt(MAX_MESSAGE_SIZE);
      for (int i = 0; i < msgSize; i++) {
        try {
          MutableFudgeMsg msg = getRandomMessage();
          Instant instant = Clock.systemUTC().instant();
          long epochMillis = instant.toEpochMilli();
          msg.add(RECEIVED_TS_KEY, epochMillis);
          msg.add(SECURITY_KEY, security);
          s_logger.debug("generating {}", msg);
          _writerQueue.put(msg);
        } catch (InterruptedException e) {
          Thread.interrupted();
          s_logger.warn("interrupted exception while putting ticks message on queue");
        }
      }
    }
    
  }

  private MutableFudgeMsg getRandomMessage() {
    return BloombergTestUtils.makeRandomStandardTick(_valueGenerator, OpenGammaFudgeContext.getInstance());
  }

}
