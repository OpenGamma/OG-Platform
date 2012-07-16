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
import com.opengamma.util.ArgumentChecker;

/**
 * Data bundle with the data required to build curves in the Multiple Yield Curve framework. The data is based on generators.
 */
public class MultipleYieldCurveFinderGeneratorDataBundle {

  /**
   * The list of instruments. Not null.
   */
  private final List<InstrumentDerivative> _instruments;
  /**
   * The bundle with the already build data (FX, curves, HW parameters, ...). Not null.
   */
  private final YieldCurveBundle _knownData;
  /**
   * The generator based function to build yield curve bundle from parameters.
   */
  private final GeneratorCurveBuildingFunction _buildingFunction;
  /**
   * The number of instruments. The size of _instruments.
   */
  private final int _nbInstruments;

  /**
   * Constructor without FX Matrix and Currency map (used for one currency yield curve bundle.).
   * @param instruments The list of instruments.
   * @param knownData The bundle with the already build data.
   * @param curveGenerators The map of String/Curve generators.
   */
  public MultipleYieldCurveFinderGeneratorDataBundle(List<InstrumentDerivative> instruments, YieldCurveBundle knownData, LinkedHashMap<String, GeneratorCurve> curveGenerators) {
    ArgumentChecker.notNull(instruments, "Instruments");
    ArgumentChecker.notNull(knownData, "Known data");
    ArgumentChecker.notNull(curveGenerators, "Curve generators");
    _instruments = instruments;
    _knownData = knownData;
    _buildingFunction = new GeneratorCurveBuildingFunction(curveGenerators);
    _nbInstruments = instruments.size();
  }

  //  /**
  //   * Constructor.
  //   * @param instruments The list of instruments.
  //   * @param knownCurves The known curves.
  //   * @param curveGenerators The map of String/Curve generators.
  //   * @param fxMatrix The exchange rates.
  //   * @param curveCurrency The map of String/Currencies.
  //   */
  //  public MultipleYieldCurveFinderGeneratorDataBundle(List<InstrumentDerivative> instruments, YieldCurveBundle knownCurves, LinkedHashMap<String, GeneratorCurve> curveGenerators,
  //      final FXMatrix fxMatrix, final Map<String, Currency> curveCurrency) {
  //    ArgumentChecker.notNull(instruments, "Instruments");
  //    ArgumentChecker.notNull(curveGenerators, "Curve generators");
  //    _instruments = instruments;
  //    _knownData = knownCurves;
  //    _buildingFunction = new GeneratorCurveBuildingFunction(curveGenerators, fxMatrix, curveCurrency);
  //    _nbInstruments = instruments.size();
  //  }

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
  public YieldCurveBundle getKnownData() {
    return _knownData;
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
