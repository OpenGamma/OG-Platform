/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.List;

/**
 * List of blackListed data fields/schemes in the historical timeseries snapshot
 */
public interface BlackList {
  
  String getName();
  
  List<String> getBlackList();

}
