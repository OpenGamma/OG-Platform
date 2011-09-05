/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.TimeCalculator;

/**
 * Parameters related to a multi-factor Libor Market Model with separable displaced diffusion dynamic.
 */
public class LiborMarketModelDisplacedDiffusionParameters {

  /**
   * The times separating the Ibor periods. 
   */
  private final double[] _iborTime;
  /**
   * The accrual factors for the different periods.
   */
  private final double[] _accrualFactor;
  /**
   * The displacements for the different periods.
   */
  private final double[] _displacement;
  /**
   * The volatilities. The dimensions of the volatility is number of periods X number of factors.
   */
  private final double[][] _volatility;
  /**
   * The mean reversion used for the volatility time dependency.
   */
  private final double _meanReversion;
  /**
   * The number of periods.
   */
  private final int _nbPeriod;
  /**
   * The number of factors.
   */
  private final int _nbFactor;

  /**
   * Constructor from the model details.
   * @param iborTime The times separating the Ibor periods. 
   * @param accrualFactor The accrual factors for the different periods.
   * @param displacement The displacements for the different periods.
   * @param volatility The volatilities. The dimensions of the volatility is number of periods X number of factors.
   * @param meanReversion The mean reversion used for the volatility time dependency.
   */
  public LiborMarketModelDisplacedDiffusionParameters(double[] iborTime, double[] accrualFactor, double[] displacement, double[][] volatility, double meanReversion) {
    Validate.notNull(iborTime, "LMM Libor times");
    Validate.notNull(accrualFactor, "LMM accrual factors");
    Validate.notNull(displacement, "LMM displacements");
    Validate.notNull(volatility, "LMM volatility");
    _nbPeriod = accrualFactor.length;
    Validate.isTrue(iborTime.length == _nbPeriod + 1, "LMM data: Dimension");
    Validate.isTrue(_nbPeriod == displacement.length, "LMM data: Dimension");
    Validate.isTrue(_nbPeriod == volatility.length, "LMM data: Dimension");
    _iborTime = iborTime;
    _accrualFactor = accrualFactor;
    _displacement = displacement;
    _volatility = volatility;
    _meanReversion = meanReversion;
    _nbFactor = volatility[0].length;
  }

  /**
   * Create model parameters adapted to a specific swap.
   * @param modelDate The pricing date.
   * @param swap The swap to be used for the model construction.
   * @param dayCount The Ibor day count.
   * @param displacement The displacement (common to all Ibors).
   * @param meanReversion The mean reversion.
   * @param volatilityFunction The volatility function. For a given time to Ibor period start date it provides the volatilities (or weights) of the different factors.
   * @return A Libor Market Model parameter set.
   */
  public static LiborMarketModelDisplacedDiffusionParameters from(ZonedDateTime modelDate, SwapFixedIborDefinition swap, DayCount dayCount, double displacement, double meanReversion,
      Function1D<Double, Double[]> volatilityFunction) {
    int nbPeriod = swap.getIborLeg().getNumberOfPayments();
    ZonedDateTime[] iborDate = new ZonedDateTime[nbPeriod + 1];
    double[] iborTime = new double[nbPeriod + 1];
    double[] accrualFactor = new double[nbPeriod];
    double[] d = new double[nbPeriod];
    Double[] tmp = volatilityFunction.evaluate(0.0);
    double[][] vol = new double[nbPeriod][tmp.length];
    iborDate[0] = swap.getIborLeg().getNthPayment(0).getAccrualStartDate();
    iborTime[0] = TimeCalculator.getTimeBetween(modelDate, iborDate[0]);
    for (int loopcf = 0; loopcf < nbPeriod; loopcf++) {
      iborDate[loopcf + 1] = swap.getIborLeg().getNthPayment(loopcf).getPaymentDate();
      iborTime[loopcf + 1] = TimeCalculator.getTimeBetween(modelDate, iborDate[loopcf + 1]);
      accrualFactor[loopcf] = dayCount.getDayCountFraction(iborDate[loopcf], iborDate[loopcf + 1]);
      d[loopcf] = displacement;
      //TODO: better conversion to double[]
      Double[] tmp2 = volatilityFunction.evaluate(iborTime[loopcf]);
      for (int looptmp = 0; looptmp < tmp2.length; looptmp++) {
        vol[loopcf][looptmp] = tmp2[looptmp];
      }
    }
    return new LiborMarketModelDisplacedDiffusionParameters(iborTime, accrualFactor, d, vol, meanReversion);
  }

  /**
   * Gets the _iborTime field.
   * @return the _iborTime
   */
  public double[] getIborTime() {
    return _iborTime;
  }

  /**
   * Gets the _accrualFactor field.
   * @return the _accrualFactor
   */
  public double[] getAccrualFactor() {
    return _accrualFactor;
  }

  /**
   * Gets the _displacement field.
   * @return the _displacement
   */
  public double[] getDisplacement() {
    return _displacement;
  }

  /**
   * Gets the _volatility field.
   * @return the _volatility
   */
  public double[][] getVolatility() {
    return _volatility;
  }

  /**
   * Gets the _meanReversion field.
   * @return the _meanReversion
   */
  public double getMeanReversion() {
    return _meanReversion;
  }

  /**
   * Gets the _nbPeriod field.
   * @return the _nbPeriod
   */
  public int getNbPeriod() {
    return _nbPeriod;
  }

  /**
   * Gets the _nbFactor field.
   * @return the _nbFactor
   */
  public int getNbFactor() {
    return _nbFactor;
  }

  /**
   * Change the model volatility in a block to a given volatility matrix.
   * @param volatility The changed volatility.
   * @param startIndex The start index for the block to change.
   */
  public final void setVolatility(double[][] volatility, int startIndex) {
    Validate.notNull(volatility, "LMM volatility");
    Validate.isTrue(volatility[0].length == _nbFactor, "LMM: incorrect number of factors");
    for (int loopperiod = 0; loopperiod < volatility.length; loopperiod++) {
      System.arraycopy(volatility[loopperiod], 0, _volatility[startIndex + loopperiod], 0, volatility[loopperiod].length);
    }
  }

}
