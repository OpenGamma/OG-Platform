/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.CSTimer;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.util.OverWriteOldestBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.TerminatableJob;

/**
 * The primary job which will recalculate a {@link View}'s output data.
 *
 * @author kirk
 */
public class ViewRecalculationJob extends TerminatableJob {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewRecalculationJob.class);
  private static long s_delay = 0L;
  private final ViewImpl _view;
  private ViewComputationResultModelImpl _previousResult;
  private static boolean _paused;
  private static One2OneChannel _channel = Channel.one2one(new OverWriteOldestBuffer(1));
  
  public ViewRecalculationJob(ViewImpl view) {
    if(view == null) {
      throw new NullPointerException("Must provide a backing view.");
    }
    _view = view;
  }

  /**
   * @return the previousResult
   */
  public ViewComputationResultModelImpl getPreviousResult() {
    return _previousResult;
  }

  /**
   * @param previousResult the previousResult to set
   */
  public void setPreviousResult(ViewComputationResultModelImpl previousResult) {
    _previousResult = previousResult;
  }

  /**
   * @return the view
   */
  public ViewImpl getView() {
    return _view;
  }

  @Override
  protected void runOneCycle() {
    PortfolioEvaluationModel portfolioEvaluationModel = getView().getPortfolioEvaluationModel();
    ViewComputationResultModelImpl result = new ViewComputationResultModelImpl();
    
    SingleComputationCycle cycle = new SingleComputationCycle(
        getView().getDefinition().getName(),
        getView().getProcessingContext(),
        portfolioEvaluationModel,
        result, getView().getDefinition(),
        getView().getComputationExecutorService());
    if(!cycle.prepareInputs()) {
      s_logger.info("Not executing as couldn't snapshot market data. Probably waiting for source data to finish populating.");
      try {
        Thread.sleep(100l);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    } else {
      cycle.executePlans();
      cycle.populateResultModel();
      cycle.releaseResources();
    }
    
    long endTime = System.currentTimeMillis();
    result.setResultTimestamp(endTime);
    long delta = endTime - cycle.getStartTime();
    s_logger.info("Completed one recalculation pass in {}ms", delta);
    getView().recalculationPerformed(result);
    CSTimer csTimer = new CSTimer();
    csTimer.setAlarm(System.currentTimeMillis() + s_delay);
    Guard[] guards = new Guard[] {  csTimer, _channel.in() };
    int selected = new Alternative(guards).fairSelect(new boolean[] { !_paused, true });
    if (selected == 1) {
      _channel.in().read();
    }
    s_logger.info("selected:"+selected);
//    if (s_delay != 0L) {
//      try { Thread.sleep(s_delay); } catch (InterruptedException e) { }
//    }
  }

  public static void setDelay(long delay) {
    s_logger.info("setting delay to "+delay);
    s_delay = delay;
    _channel.out().write(null);
  }
  
  public static void pause(boolean paused) {
    s_logger.info("setting paused to "+paused);
    _paused = paused;
    _channel.out().write(null);
  }
}
