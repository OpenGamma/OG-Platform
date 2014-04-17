/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * In-memory convention bundle master for Synthetic tickers.
 */
public class SyntheticInMemoryConventionBundleMaster extends InMemoryConventionBundleMaster {

  @Override
  protected void init() {
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(this);
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
    addDummyTreasuryBondConvention(utils, "UG");
  }

  /**
   * Creates a dummy treasury bond convention for a country.
   * @param utils Utility for storing conventions
   * @param domicile The country name
   */
  private static void addDummyTreasuryBondConvention(final ConventionBundleMasterUtils utils, final String domicile) {
    final String name = domicile + "_TREASURY_BOND_CONVENTION";
    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(SIMPLE_NAME_SCHEME, name)), name, true, true, 0, 3, true);
  }

}
