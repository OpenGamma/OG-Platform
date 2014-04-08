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
<<<<<<< HEAD
<<<<<<< HEAD

  private static final String LAST_PRICE = "CLOSE";
=======
  /** Value that the {@link MarketDataRequirementNames#MARKET_VALUE} field is adjusted into */
  private static final String LAST_PRICE = "CLOSE";
  /** Value that the {@link MarketDataRequirementNames#YIELD_YIELD_TO_MATURITY_MID} field is adjusted into */
  private static final String LAST_YIELD = "YIELD_CLOSE";
>>>>>>> 7decf75... [PLAT-6345] Adding more outputs for equity and bond TRS
=======
  /** Value that the {@link MarketDataRequirementName#MARKET_VALUE} field is adjusted into */
  private static final String LAST_PRICE = "CLOSE";
  /** Value that the {@link MarketDataRequirementName#YIELD_YIELD_TO_MATURITY_MID} field is adjusted into */
  private static final String LAST_YIELD = "YIELD_CLOSE";
>>>>>>> a8c2f08... Revert "Revert "[PLAT-5345] Adding bond TRS analytics to examples-simulated""

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
