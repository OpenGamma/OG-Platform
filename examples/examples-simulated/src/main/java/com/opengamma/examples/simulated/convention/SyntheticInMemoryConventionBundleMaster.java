/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import com.opengamma.financial.convention.InMemoryConventionBundleMaster;

/**
 * In-memory convention bundle master for Synthetic tickers.
 */
public class SyntheticInMemoryConventionBundleMaster extends InMemoryConventionBundleMaster {

  @Override
  protected void init() {

    SyntheticUSConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticUSConventions.addCAPMConvention(this);
    SyntheticGBConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticAUConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticCAConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticCHConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticDKConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticEUConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticJPConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticNZConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticUSConventions.addTreasuryBondConvention(this);
  }

}
