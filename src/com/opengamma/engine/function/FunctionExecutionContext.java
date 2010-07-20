/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import javax.time.calendar.Clock;

import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calcnode.ViewProcessorQuery;

/**
 * Holds values that will be provided to a {@link FunctionInvoker} during invocation.
 */
public class FunctionExecutionContext extends AbstractFunctionContext {
  /**
   * The name under which an instance of {@link ViewProcessor} should be bound.
   */
  public static final String VIEW_PROCESSOR_QUERY_NAME = "viewProcessorQuery";
  /**
   * The name under which the epoch time indicating the snapshot time will be bound.
   */
  public static final String SNAPSHOT_EPOCH_TIME_NAME = "snapshotEpochTime";
  /**
   * The name under which a JSR-310 Clock providing the snapshot time will be bound.
   */
  public static final String SNAPSHOT_CLOCK_NAME = "snapshotClock";
  /**
   * The name under which an instance of {@link SecuritySource} should be bound.
   */
  public static final String SECURITY_SOURCE_NAME = "securitySource";
  

  public ViewProcessorQuery getViewProcessorQuery() {
    return (ViewProcessorQuery) get(VIEW_PROCESSOR_QUERY_NAME);
  }
  
  public void setViewProcessorQuery(ViewProcessorQuery viewProcessorQuery) {
    put(VIEW_PROCESSOR_QUERY_NAME, viewProcessorQuery);
  }
  
  public Long getSnapshotEpochTime() {
    return (Long) get(SNAPSHOT_EPOCH_TIME_NAME);
  }
  
  public void setSnapshotEpochTime(Long snapshotEpochTime) {
    put(SNAPSHOT_EPOCH_TIME_NAME, snapshotEpochTime);
  }

  public Clock getSnapshotClock() {
    return (Clock) get(SNAPSHOT_CLOCK_NAME);
  }
  
  public void setSnapshotClock(Clock snapshotClock) {
    put(SNAPSHOT_CLOCK_NAME, snapshotClock);
  }
  
  public void setSecuritySource(SecuritySource securitySource) {
    put(SECURITY_SOURCE_NAME, securitySource);
  }
  
  public SecuritySource getSecuritySource() {
    return (SecuritySource) get(SECURITY_SOURCE_NAME);
  }
  
}
