/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.historical.normalization;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.examples.simulated.historical.SimulatedHistoricalData;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.util.spring.SpringFactoryBean;

/**
 * Spring factory bean for {@code HistoricalTimeSeriesFieldAdjustmentMap}.
 */
public class ExampleHistoricalTimeSeriesFieldAdjustmentMapFactoryBean extends SpringFactoryBean<HistoricalTimeSeriesFieldAdjustmentMap> {
  /** Value that the {@link MarketDataRequirementName#MARKET_VALUE} field is adjusted into */
  private static final String LAST_PRICE = "CLOSE";
  /** Value that the {@link MarketDataRequirementName#YIELD_YIELD_TO_MATURITY_MID} field is adjusted into */
  private static final String LAST_YIELD = "YIELD_CLOSE";

  /**
   * Default constructor.
   */
  public ExampleHistoricalTimeSeriesFieldAdjustmentMapFactoryBean() {
    super(HistoricalTimeSeriesFieldAdjustmentMap.class);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesFieldAdjustmentMap createObject() {
    final HistoricalTimeSeriesFieldAdjustmentMap fieldAdjustmentMap = new HistoricalTimeSeriesFieldAdjustmentMap(SimulatedHistoricalData.OG_DATA_SOURCE);
    final SyntheticHistoricalDataNormalizer adjuster = new SyntheticHistoricalDataNormalizer();
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, LAST_PRICE, adjuster);
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, null, LAST_YIELD, adjuster);
    return fieldAdjustmentMap;
  }

}
