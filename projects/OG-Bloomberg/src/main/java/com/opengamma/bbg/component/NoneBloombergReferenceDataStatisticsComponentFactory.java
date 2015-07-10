/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.component.ComponentRepository;

/**
 * Component factory for the Null Bloomberg Reference Data Statistics.
 */
public class NoneBloombergReferenceDataStatisticsComponentFactory extends DefaultBloombergReferenceDataStatisticsComponentFactory {

  @Override
  protected BloombergReferenceDataStatistics initReferenceDataStatistics(ComponentRepository repo) {
    return NullBloombergReferenceDataStatistics.INSTANCE;
  }

}
