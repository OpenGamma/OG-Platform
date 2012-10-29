/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.List;

/**
 * List of blackListed schemes in the historical timeseries snapshot
 */
public interface SchemeBlackList {

  String getName();
  
  List<String> getSchemeBlackList();
}
