/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.building;

import java.util.LinkedHashMap;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Data bundle with the data required to build curves in the Multiple Yield Curve framework. The data is based on generators.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated. Use classes such as
 * {@link MulticurveDiscountBuildingRepository}.
 */
@Deprecated
public class MultipleYieldCurveFinderGeneratorDataBundle {

  /**
   * The list of instruments. Not null.
   */
  private final InstrumentDerivative[] _instruments;
  /**
   * The bundle with the already build data (FX, curves, HW parameters, ...). Not null.
   */
  private final YieldCurveBundle _knownData;
  /**
   * The generator based function to build yield curve bundle from parameters.
   */
  private final CurveBuildingGeneratorFunction _buildingFunction;
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
  public MultipleYieldCurveFinderGeneratorDataBundle(final InstrumentDerivative[] instruments, final YieldCurveBundle knownData, final LinkedHashMap<String, GeneratorYDCurve> curveGenerators) {
    ArgumentChecker.notNull(instruments, "Instruments");
    ArgumentChecker.notNull(knownData, "Known data");
    ArgumentChecker.notNull(curveGenerators, "Curve generators");
    _instruments = instruments;
    _knownData = knownData; // TODO: do we need to store it; it is in building function.
    _buildingFunction = new CurveBuildingGeneratorFunction(curveGenerators, knownData);
    _nbInstruments = instruments.length;
  }

  /**
   * Gets the instruments to be used for the curve construction.
   * @return The instruments.
   */
  public InstrumentDerivative[] getInstruments() {
    final InstrumentDerivative[] instruments = new InstrumentDerivative[_nbInstruments];
    System.arraycopy(_instruments, 0, instruments, 0, _nbInstruments);
    return instruments;
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
  public CurveBuildingGeneratorFunction getBuildingFunction() {
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
    return _instruments[i];
  }

}
