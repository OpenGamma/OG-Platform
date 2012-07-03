/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.convention;

import com.opengamma.financial.convention.AUConventions;
import com.opengamma.financial.convention.CAConventions;
import com.opengamma.financial.convention.CHConventions;
import com.opengamma.financial.convention.DKConventions;
import com.opengamma.financial.convention.EUConventions;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.JPConventions;
import com.opengamma.financial.convention.NZConventions;
import com.opengamma.financial.convention.USConventions;

/**
 * In-memory convention bundle master for Synthetic tickers.
 */
public class SyntheticInMemoryConventionBundleMaster extends InMemoryConventionBundleMaster {

  @Override
  protected void init() {

    USConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticUSConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticUSConventions.addCAPMConvention(this);
    
    SyntheticGBConventions.addFixedIncomeInstrumentConventions(this);
    
    AUConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticAUConventions.addFixedIncomeInstrumentConventions(this);
    
    CAConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticCAConventions.addFixedIncomeInstrumentConventions(this);
    
    CHConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticCHConventions.addFixedIncomeInstrumentConventions(this);
    
    DKConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticDKConventions.addFixedIncomeInstrumentConventions(this);
    
    EUConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticEUConventions.addFixedIncomeInstrumentConventions(this);
    
    JPConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticJPConventions.addFixedIncomeInstrumentConventions(this);
    
    NZConventions.addFixedIncomeInstrumentConventions(this);
    SyntheticNZConventions.addFixedIncomeInstrumentConventions(this);
  }

}
