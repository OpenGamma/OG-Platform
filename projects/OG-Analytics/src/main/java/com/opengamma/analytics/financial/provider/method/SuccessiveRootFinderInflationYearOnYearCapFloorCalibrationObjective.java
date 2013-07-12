/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueBlackSmileInflationYearOnYearCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProvider;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Specific objective function for Hull-White model calibration with cap/floor.
 */
public class SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective extends SuccessiveRootFinderCalibrationObjectivewithInflation {

  /**
   * The pricing method used to price the cap/floor.
   */
  private static final PresentValueBlackSmileInflationYearOnYearCalculator PVIC = PresentValueBlackSmileInflationYearOnYearCalculator.getInstance();
  /**
   * The Hull-White parameters before calibration. The calibration is done on the last volatility.
   */
  private final BlackSmileCapInflationYearOnYearParameters _inflationCapYearOnYearParameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyInflationcapYearOnYear;
  /**
   * The Hull-White parameters and curves bundle.
   */
  private BlackSmileCapInflationYearOnYearProvider _inflationCapYearOnYearProvider;

  /**
   * Constructor of the objective function with the Hull-White parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Hull-White parameters.
   * @param ccy The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  public SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(final BlackSmileCapInflationYearOnYearParameters parameters, final Currency ccy) {
    super(new FXMatrix(ccy), ccy);
    _inflationCapYearOnYearParameters = parameters;
    _ccyInflationcapYearOnYear = ccy;
    setMinimumParameter(1.0E-6);
    setMaximumParameter(1.0);
    setFunctionValueAccuracy(1.0E-4);
    setVariableAbsoluteAccuracy(1.0E-9);
  }

  /**
   * Sets the Hull-White curve bundle using the Hull-White parameters and a given set of curves.
   * @param inflation The multi-curves provider.
   */
  @Override
  public void setInflation(InflationProviderInterface inflation) {
    _inflationCapYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(inflation, _inflationCapYearOnYearParameters);
  }

  /**
   * Gets the inflation year on year cap/floor data.
   * @return The inflation year on year cap/floor data.
   */
  public BlackSmileCapInflationYearOnYearParameters getInflationCapYearOnYearParameters() {
    return _inflationCapYearOnYearParameters;
  }

  /**
   * Sets the inflation year on year cap/floor curve bundle.
   * @return The inflation year on year cap/floor curve bundle.
   */
  public BlackSmileCapInflationYearOnYearProvider getInflationCapYearOnYearProvider() {
    return _inflationCapYearOnYearProvider;
  }

  /**
   * Sets the calibration time for the next calibration.
   * @param calibrationTime The calibration time.
   */
  public void setNextCalibrationTime(double calibrationTime) {
  }

  @Override
  public void setInstrument(InstrumentDerivative instrument) {
    super.setInstrument(instrument);
  }

  @Override
  public Double evaluate(Double x) {
    return _inflationCapYearOnYearProvider.getMulticurveProvider().getFxRates().convert(getInstrument().accept(PVIC, _inflationCapYearOnYearProvider), _ccyInflationcapYearOnYear).getAmount()
        - getPrice();
  }
}
