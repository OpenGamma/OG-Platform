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
   * Records the fact that some Bloomberg fields were read.
   * 
   * @param identifiers  the identifiers, such as securities, not null
   * @param fields  the fields read, not null
   */
  void recordStatistics(Set<String> identifiers, Set<String> fields);

}
