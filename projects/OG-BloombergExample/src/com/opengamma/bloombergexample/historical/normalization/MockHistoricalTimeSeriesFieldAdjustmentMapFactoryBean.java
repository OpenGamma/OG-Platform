/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.historical.normalization;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.bloombergexample.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;
import com.opengamma.util.spring.SpringFactoryBean;

/**
 * Spring factory bean for {@code HistoricalTimeSeriesFieldAdjustmentMap}.
 */
public class MockHistoricalTimeSeriesFieldAdjustmentMapFactoryBean extends SpringFactoryBean<HistoricalTimeSeriesFieldAdjustmentMap> {

  private static final String YLD_TO_MATURITY_MID = "YTM_MID";
  private static final String VOLUME = "VOL";
  private static final String LAST_PRICE = "CLOSE";

  public MockHistoricalTimeSeriesFieldAdjustmentMapFactoryBean() {
    super(HistoricalTimeSeriesFieldAdjustmentMap.class);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesFieldAdjustmentMap createObject() {
    HistoricalTimeSeriesFieldAdjustmentMap fieldAdjustmentMap = new HistoricalTimeSeriesFieldAdjustmentMap(SimulatedHistoricalDataGenerator.OG_DATA_SOURCE);
    HistoricalTimeSeriesAdjuster mockNormalizer = new MockHistoricalTimeSeriesNormalizer();
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.MARKET_VALUE, null, LAST_PRICE, new SyntheticMarketDataNormalizer());
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.VOLUME, null, VOLUME, mockNormalizer);
    fieldAdjustmentMap.addFieldAdjustment(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, null, YLD_TO_MATURITY_MID, mockNormalizer);
    return fieldAdjustmentMap;
  }

}
