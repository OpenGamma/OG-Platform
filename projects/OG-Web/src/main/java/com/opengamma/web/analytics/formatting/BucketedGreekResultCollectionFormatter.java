/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class BucketedGreekResultCollectionFormatter extends AbstractFormatter<BucketedGreekResultCollection> {

  private static final Logger s_logger = LoggerFactory.getLogger(BucketedGreekResultCollectionFormatter.class);

  /* package */ BucketedGreekResultCollectionFormatter() {
    super(BucketedGreekResultCollection.class);
  }

  @Override
  public Object formatCell(BucketedGreekResultCollection value, ValueSpecification valueSpec, Object inlineKey) {
    if (value.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA) != null) {
      double[] expiries = value.getExpiries();
      double[][] strikes = value.getStrikes();
      double[] uniqueStrikes = strikes[0];
      for (int i = 1; i < strikes.length; i++) {
        if (strikes[i].length != uniqueStrikes.length) {
          s_logger.warn("Did not have a rectangular bucketed vega surface");
          return FORMATTING_ERROR;
        }
      }
      return "Volatility Surface (" + expiries.length + " x " + uniqueStrikes.length + ")";
    } else {
      return FORMATTING_ERROR;
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.SURFACE_DATA;
  }
}
