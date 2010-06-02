/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.historicaldata.HistoricalDataProvider;
import com.opengamma.engine.security.SecurityMaster;

/**
 * Utility methods to pull standard objects out of a {@link FunctionExecutionContext}.
 *
 * @author pietari
 */
public class OpenGammaExecutionContext {
  
  /**
   * The name under which an instance of {@link HistoricalDataProvider} should be bound.
   */
  public static final String HISTORICAL_DATA_PROVIDER_NAME = "historicalDataProvider";

  /**
   * The name under which an instance of {@link SecurityMaster} should be bound.
   */
  public static final String SECURITY_MASTER_NAME = "securityMaster";
  
  public static HistoricalDataProvider getHistoricalDataProvider(FunctionExecutionContext context) {
    return (HistoricalDataProvider) context.get(HISTORICAL_DATA_PROVIDER_NAME);
  }
  
  public static void setHistoricalDataProvider(FunctionExecutionContext context, 
      HistoricalDataProvider historicalDataProvider) {
    context.put(HISTORICAL_DATA_PROVIDER_NAME, historicalDataProvider);
  }
  
  public static SecurityMaster getSecurityMaster(FunctionExecutionContext context) {
    return (SecurityMaster) context.get(SECURITY_MASTER_NAME);
  }
  
  public static void setSecurityMaster(FunctionExecutionContext context, SecurityMaster secMaster) {
    context.put(SECURITY_MASTER_NAME, secMaster);
  }
  
}
