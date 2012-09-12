/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.tool;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;

/**
 * Exposes elements of a Bloomberg market data server in a tool context.
 */
public interface BloombergToolContext {

  /**
   * Gets the Bloomberg reference data provider.
   * 
   * @return the Bloomberg reference data provider
   */
  ReferenceDataProvider getBloombergReferenceDataProvider();

}
