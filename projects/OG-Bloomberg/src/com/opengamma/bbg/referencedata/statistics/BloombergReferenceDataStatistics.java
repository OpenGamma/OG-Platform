/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.Set;

/**
 * A class which records interesting statistics about usage of bloomberg reference data
 */
public interface BloombergReferenceDataStatistics {
  /**
   * Requests the statistics to record the fact that some fields were read recently
   * @param securities the securities
   * @param fields the fields
   */
  void gotFields(Set<String> securities, Set<String> fields);
}
