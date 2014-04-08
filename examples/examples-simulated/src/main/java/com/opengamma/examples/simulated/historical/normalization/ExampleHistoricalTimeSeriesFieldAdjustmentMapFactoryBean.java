/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.historical.normalization;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.examples.simulated.historical.SimulatedHistoricalDataGenerator;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.util.spring.SpringFactoryBean;

/**
 * Spring factory bean for {@code HistoricalTimeSeriesFieldAdjustmentMap}.
 */
public class ExampleHistoricalTimeSeriesFieldAdjustmentMapFactoryBean extends SpringFactoryBean<HistoricalTimeSeriesFieldAdjustmentMap> {

  private static final String LAST_PRICE = "CLOSE";

  public ExampleHistoricalTimeSeriesFieldAdjustmentMapFactoryBean() {
    super(HistoricalTimeSeriesFieldAdjustmentMap.class);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesFieldAdjustmentMap createObject() {
    final HistoricalTimeSeriesFieldAdjustmentMap fieldAdjustmentMap = new HistoricalTimeSeriesFieldAdjustmentMap(SimulatedHistoricalDataGenerator.OG_DATA_SOURCE);
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, LAST_PRICE, new SyntheticHistoricalDataNormalizer());
    return fieldAdjustmentMap;
  }

}
