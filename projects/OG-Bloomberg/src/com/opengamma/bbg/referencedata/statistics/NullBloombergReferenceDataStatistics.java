/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.Set;

/**
 * A {@link BloombergReferenceDataStatistics} which does nothing
 */
public final class NullBloombergReferenceDataStatistics implements BloombergReferenceDataStatistics {

  /**
   * The singleton instance
   */
  public static final NullBloombergReferenceDataStatistics INSTANCE = new NullBloombergReferenceDataStatistics();

  private NullBloombergReferenceDataStatistics() {
  }

  public void gotFields(Set<String> securities, Set<String> fields) {

  }
}
