/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.util.ArgumentChecker;

/**
 * Data required to build inflation curves in the Multiple Curve on MulticurveProviderDiscount framework.
 */
public class InflationDiscountBuildingData {

  /**
   * The list of instruments. Not null.
   */
  private final InstrumentDerivative[] _instruments;
  /**
   * The generator of the MarketDiscount.
   */
  private final GeneratorInflationProviderDiscount _generator;
  /**
   * The number of instruments. The size of _instruments.
   */
  private final int _nbInstruments;

  /**
   * Constructor without FX Matrix and Currency map (used for one currency yield curve bundle.).
   * @param instruments The list of instruments.
   * @param generator The market generator.
   */
  public InflationDiscountBuildingData(final InstrumentDerivative[] instruments, final GeneratorInflationProviderDiscount generator) {
    ArgumentChecker.notNull(instruments, "Instruments");
    ArgumentChecker.notNull(generator, "Market generator");
    _instruments = instruments;
    _generator = generator;
    _nbInstruments = instruments.length;
  }

  /**
   * Gets the instruments to be used for the curve construction.
   * @return The instruments.
   */
  public InstrumentDerivative[] getInstruments() {
    InstrumentDerivative[] instruments = new InstrumentDerivative[_nbInstruments];
    System.arraycopy(_instruments, 0, instruments, 0, _nbInstruments);
    return instruments;
  }

  /**
   * Gets the know curves.
   * @return The known curves.
   */
  public InflationProviderDiscount getKnownData() {
    return _generator.getKnownData();
  }

  /**
   * Gets the building function.
   * @return The building function.
   */
  public GeneratorInflationProviderDiscount getGeneratorMarket() {
    return _generator;
  }

  /**
   * Gets the number of instruments used in the calibration.
   * @return The number of instruments.
   */
  public int getNumberOfInstruments() {
    return _nbInstruments;
  }

  /**
   * Get one instrument used for calibration from its index.
   * @param i The instrument index.
   * @return The instrument.
   */
  public InstrumentDerivative getInstrument(final int i) {
    return _instruments[i];
  }

}
