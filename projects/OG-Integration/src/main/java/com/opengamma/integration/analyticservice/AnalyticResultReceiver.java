/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import java.util.List;

import com.opengamma.engine.view.ViewResultEntry;

/**
 * 
 */
public interface AnalyticResultReceiver {
  
  void analyticReceived(List<ViewResultEntry> allResults);

}
