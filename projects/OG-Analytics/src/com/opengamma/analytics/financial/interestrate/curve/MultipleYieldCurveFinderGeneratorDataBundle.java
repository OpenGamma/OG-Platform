/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import java.util.LinkedHashMap;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Data bundle with the data required to build curves in the Multiple Yield Curve framework. The data is based on generators.
 */
public class MultipleYieldCurveFinderGeneratorDataBundle {

  private final List<InstrumentDerivative> _instruments;
  private final YieldCurveBundle _knownCurves;
  private final GeneratorCurveBuildingFunction _buildingFunction;
  private final int _nbInstruments;

  //  private final List<String> _names;
  //  private final boolean _useFiniteDifferenceByDefault;

  public MultipleYieldCurveFinderGeneratorDataBundle(List<InstrumentDerivative> instruments, YieldCurveBundle knownCurves, LinkedHashMap<String, GeneratorCurve> curveGenerators) {
    _instruments = instruments;
    _knownCurves = knownCurves;
    _buildingFunction = new GeneratorCurveBuildingFunction(curveGenerators);
    _nbInstruments = instruments.size();
  }

  /**
   * Gets the instruments to be used for the curve construction..
   * @return The instruments.
   */
  public List<InstrumentDerivative> getInstruments() {
    return _instruments;
  }

  /**
   * Gets the know curves.
   * @return The known curves.
   */
  public YieldCurveBundle getKnownCurves() {
    return _knownCurves;
  }

  /**
   * Gets the building function.
   * @return The building function.
   */
  public GeneratorCurveBuildingFunction getBuildingFunction() {
    return _buildingFunction;
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
    return _instruments.get(i);
  }

}
