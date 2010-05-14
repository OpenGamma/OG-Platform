/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import javax.time.calendar.Clock;

import com.opengamma.engine.historicaldata.HistoricalDataProvider;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.calcnode.ViewProcessorQuery;

/**
 * Holds values that will be provided to a {@link FunctionInvoker} during invocation.
 *
 */
public class FunctionExecutionContext extends AbstractFunctionContext {
  public static final String HISTORICAL_DATA_PROVIDER_NAME = "historicalDataProvider";
  public static final String SECURITY_MASTER_NAME = "securityMaster";
  public static final String VIEW_PROCESSOR_QUERY_NAME = "viewProcessorQuery";
  public static final String SNAPSHOT_EPOCH_TIME_NAME = "snapshotEpochTime";
  public static final String SNAPSHOT_CLOCK_NAME = "snapshotClock";

  public HistoricalDataProvider getHistoricalDataProvider() {
    return (HistoricalDataProvider) get(HISTORICAL_DATA_PROVIDER_NAME);
  }
  
  public void setHistoricalDataProvider(HistoricalDataProvider historicalDataProvider) {
    put(HISTORICAL_DATA_PROVIDER_NAME, historicalDataProvider);
  }
  
  public SecurityMaster getSecurityMaster() {
    return (SecurityMaster) get(SECURITY_MASTER_NAME);
  }
  
  public void setSecurityMaster(SecurityMaster secMaster) {
    put(SECURITY_MASTER_NAME, secMaster);
  }
  
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
}
