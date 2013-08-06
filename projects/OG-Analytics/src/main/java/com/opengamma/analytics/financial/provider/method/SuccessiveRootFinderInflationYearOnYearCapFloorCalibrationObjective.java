/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationYearOnYearCapFloorParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationYearOnYearParameters;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueBlackSmileInflationYearOnYearCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProvider;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.util.money.Currency;

/**
 * Specific objective function for cap floor year on year in price index model  model calibration.
 */
public class SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective extends SuccessiveRootFinderCalibrationObjectivewithInflation {

  /**
   * The pricing method used to price the cap/floor.
   */
  private static final PresentValueBlackSmileInflationYearOnYearCalculator PVIC = PresentValueBlackSmileInflationYearOnYearCalculator.getInstance();
  /**
   * The cap floor year on year in price index model parameters before calibration. The calibration is done on the last volatility.
   */
  private final InflationYearOnYearCapFloorParameters _inflationCapYearOnYearParameters;
  /**
   * The currency for which the cap floor year on year in price index model  parameters are valid.
   */
  private final Currency _ccyInflationcapYearOnYear;
  /**
   * The inflation year on year parameters and curves bundle.
   */
  private BlackSmileCapInflationYearOnYearProvider _inflationCapYearOnYearProvider;

  /**
   * The expiry index for the calibration.
   */
  private int _expiryIndex;

  /**
   * The strike index for the calibration.
   */
  private int _strikeIndex;

  /**
   * The initial volatilities before calibration.
   */
  private final double[][] _volatilityInit;

  /**
   * Constructor of the objective function with the  year on year cap/floor parameters. The parameters range and accuracy are set at some default value 
   * (minimum: 1.0E-6; maximum: 1.0, function value accuracy: 1.0E-4; parameter absolute accuracy: 1.0E-9).
   * @param parameters The Year on Year cap/floor parameters.
   * @param ccy The currency for which the Hull-White parameters are valid.
   */
  public SuccessiveRootFinderInflationYearOnYearCapFloorCalibrationObjective(final InflationYearOnYearCapFloorParameters parameters, final Currency ccy) {
    super(new FXMatrix(ccy), ccy);
    _inflationCapYearOnYearParameters = parameters;
    _ccyInflationcapYearOnYear = ccy;
    setMinimumParameter(1.0E-6);
    setMaximumParameter(1.0);
    setFunctionValueAccuracy(1.0E-4);
    setVariableAbsoluteAccuracy(1.0E-9);
    _volatilityInit = new double[parameters.getNumberOfExpiryTimes()][parameters.getNumberOfStrikes()];
    for (int loopperiod = 0; loopperiod < parameters.getNumberOfExpiryTimes(); loopperiod++) {
      for (int loopfact = 0; loopfact < parameters.getNumberOfStrikes(); loopfact++) {
        _volatilityInit[loopperiod][loopfact] = parameters.getVolatility()[loopperiod][loopfact];
      }
    }
  }

  /**
   * Sets the year on year cap/floor curve bundle using the Hull-White parameters and a given set of curves.
   * @param inflation The multi-curves provider.
   */
  @Override
  public void setInflation(final InflationProviderInterface inflation) {
    _inflationCapYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(inflation, new BlackSmileCapInflationYearOnYearParameters(_inflationCapYearOnYearParameters));
  }

  /**
   * Gets the inflation year on year cap/floor data.
   * @return The inflation year on year cap/floor data.
   */
  public InflationYearOnYearCapFloorParameters getInflationCapYearOnYearParameters() {
    return _inflationCapYearOnYearParameters;
  }

  /**
   * Sets the inflation year on year cap/floor curve bundle.
   * @return The inflation year on year cap/floor curve bundle.
   */
  public BlackSmileCapInflationYearOnYearProvider getInflationCapYearOnYearProvider() {
    return _inflationCapYearOnYearProvider;
  }

  @Override
  public void setInstrument(final InstrumentDerivative instrument) {
    super.setInstrument(instrument);
  }

  /**
   * Gets the expiry index.
   * @return The expiry index.
   */
  public int getExpiryIndex() {
    return _expiryIndex;
  }

  /**
   * Sets the expiry index.
   * @param index The expiry index.
   */
  public void setExpiryIndex(final int index) {
    _expiryIndex = index;
  }

  /**
   * Gets the strike  index.
   * @return The strike index.
   */
  public int getStrikeIndex() {
    return _strikeIndex;
  }

  /**
   * Sets the strike  index.
   * @param index The strike index.
   */
  public void setStrikeIndex(final int index) {
    _strikeIndex = index;
  }

  @Override
  public Double evaluate(final Double x) {

    // setting the volatility in the volatility matrix
    _inflationCapYearOnYearParameters.setVolatility(x, _expiryIndex, _strikeIndex);
    // creating the new volatility surface using the new volatility matrix
    final Interpolator2D interpolator = _inflationCapYearOnYearProvider.getBlackParameters().getVolatilitySurface().getInterpolator();
    final BlackSmileCapInflationYearOnYearParameters blackSmileCapInflationYearOnYearParameters = new BlackSmileCapInflationYearOnYearParameters(_inflationCapYearOnYearParameters, interpolator);
    final BlackSmileCapInflationYearOnYearProvider blackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationYearOnYearProvider(_inflationCapYearOnYearProvider.getInflationProvider(),
        blackSmileCapInflationYearOnYearParameters);

    return _inflationCapYearOnYearProvider.getMulticurveProvider().getFxRates().convert(getInstrument().accept(PVIC, blackSmileCapInflationYearOnYearProvider), _ccyInflationcapYearOnYear).getAmount()
        - getPrice();
  }
}
