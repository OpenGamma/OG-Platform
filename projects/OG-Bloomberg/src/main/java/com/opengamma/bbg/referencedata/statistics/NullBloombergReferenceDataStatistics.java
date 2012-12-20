/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.Set;

/**
 * Bloomberg statistics implementation that acts as a sink, taking no action.
 */
public final class NullBloombergReferenceDataStatistics implements BloombergReferenceDataStatistics {

  /**
   * The singleton instance.
   */
  public static final NullBloombergReferenceDataStatistics INSTANCE = new NullBloombergReferenceDataStatistics();

  /**
   * Restricted constructor.
   */
  private NullBloombergReferenceDataStatistics() {
    // do nothing
  }

  @Override
  public void recordStatistics(Set<String> securities, Set<String> fields) {
    // do nothing
  }

}
