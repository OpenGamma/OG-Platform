/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;

/**
 * 
 */
public class BlackSmileCapInflationYearOnYearProviderDiscount extends BlackSmileCapInflationYearOnYearProvider {

  /**
   * @param inflation The inflation provider.
   * @param parameters The Black parameters.
   */
  public BlackSmileCapInflationYearOnYearProviderDiscount(InflationProviderDiscount inflation, final BlackSmileCapInflationYearOnYearParameters parameters) {
    super(inflation, parameters);
  }

  @Override
  public BlackSmileCapInflationYearOnYearProviderDiscount copy() {
    InflationProviderDiscount inflation = getInflationProvider().copy();
    return new BlackSmileCapInflationYearOnYearProviderDiscount(inflation, getBlackParameters());
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return (InflationProviderDiscount) super.getInflationProvider();
  }

}
