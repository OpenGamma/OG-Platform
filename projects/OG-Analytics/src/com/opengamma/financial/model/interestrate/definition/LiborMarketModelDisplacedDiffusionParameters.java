/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.annuity.AnnuityCouponDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.TimeCalculator;

/**
 * Parameters related to a multi-factor Libor Market Model with separable displaced diffusion dynamic.
 * The equations underlying the Libor Market Model in the probability space with numeraire  {@latex.inline $P(.,t_{j+1})$} are
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * dL_t^j = \\alpha(t) (L+a_{j}) \\gamma_{j} . dW_t^{j+1}
 * \\end{equation*}
 * }
 * with {@latex.inline $\\alpha(t) = \\exp(a t)$}. The {@latex.inline $\\gamma_j$} are m-dimensional vectors.
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
   * The time tolerance between the dates given by the model and the dates of the instrument. To avoid rounding problems.
   */
  private static final double TIME_TOLERANCE = 1.0E-3;

  public LiborMarketModelDisplacedDiffusionParameters() {
    _nbPeriod = 0;
    _iborTime = new double[1];
    _accrualFactor = new double[0];
    _displacement = new double[0];
    _volatility = new double[0][0];
    _meanReversion = 0.0;
    _nbFactor = 0;
  }

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
   * @param annuity The annuity to be used for the model construction.    swap The swap 
   * @param dayCount The Ibor day count.
   * @param displacement The displacement (common to all Ibors).
   * @param meanReversion The mean reversion.
   * @param volatilityFunction The volatility function. For a given time to Ibor period start date it provides the volatilities (or weights) of the different factors.
   * @return A Libor Market Model parameter set.
   */
  public static LiborMarketModelDisplacedDiffusionParameters from(ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity, DayCount dayCount, double displacement,
      double meanReversion, Function1D<Double, Double[]> volatilityFunction) { // SwapFixedIborDefinition swap
    int nbPeriod = annuity.getNumberOfPayments();
    ZonedDateTime[] iborDate = new ZonedDateTime[nbPeriod + 1];
    double[] iborTime = new double[nbPeriod + 1];
    double[] accrualFactor = new double[nbPeriod];
    double[] d = new double[nbPeriod];
    Double[] tmp = volatilityFunction.evaluate(0.0);
    double[][] vol = new double[nbPeriod][tmp.length];
    iborDate[0] = annuity.getNthPayment(0).getAccrualStartDate();
    iborTime[0] = TimeCalculator.getTimeBetween(modelDate, iborDate[0]);
    for (int loopcf = 0; loopcf < nbPeriod; loopcf++) {
      iborDate[loopcf + 1] = annuity.getNthPayment(loopcf).getPaymentDate();
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

  public static LiborMarketModelDisplacedDiffusionParameters from(SwaptionPhysicalFixedIbor swaption, double displacement, double meanReversion, Function1D<Double, Double[]> volatilityFunction) {
    int nbPeriod = swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments();
    double[] iborTime = new double[nbPeriod + 1];
    double[] accrualFactor = new double[nbPeriod];
    double[] d = new double[nbPeriod];
    Double[] tmp = volatilityFunction.evaluate(0.0);
    double[][] vol = new double[nbPeriod][tmp.length];
    iborTime[0] = swaption.getSettlementTime();
    for (int loopcf = 0; loopcf < nbPeriod; loopcf++) {
      iborTime[loopcf + 1] = swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcf).getPaymentTime();
      accrualFactor[loopcf] = swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcf).getPaymentYearFraction();
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

  public double getTimeTolerance() {
    return TIME_TOLERANCE;
  }

  //  public void setParameters(double[] iborTime, double[] accrualFactor, double[] displacement, double[][] volatility, double meanReversion) {
  //    Validate.notNull(iborTime, "LMM Libor times");
  //    Validate.notNull(accrualFactor, "LMM accrual factors");
  //    Validate.notNull(displacement, "LMM displacements");
  //    Validate.notNull(volatility, "LMM volatility");
  //    _nbPeriod = accrualFactor.length;
  //    Validate.isTrue(iborTime.length == _nbPeriod + 1, "LMM data: Dimension");
  //    Validate.isTrue(_nbPeriod == displacement.length, "LMM data: Dimension");
  //    Validate.isTrue(_nbPeriod == volatility.length, "LMM data: Dimension");
  //    _iborTime = iborTime;
  //    _accrualFactor = accrualFactor;
  //    _displacement = displacement;
  //    _volatility = volatility;
  //    _meanReversion = meanReversion;
  //    _nbFactor = volatility[0].length;
  //  }

  //  public void setParameters(SwaptionPhysicalFixedIbor swaption, double displacement, double meanReversion, Function1D<Double, Double[]> volatilityFunction) {
  //    int nbPeriod = swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments();
  //    double[] iborTime = new double[nbPeriod + 1];
  //    double[] accrualFactor = new double[nbPeriod];
  //    double[] d = new double[nbPeriod];
  //    Double[] tmp = volatilityFunction.evaluate(0.0);
  //    double[][] vol = new double[nbPeriod][tmp.length];
  //    iborTime[0] = swaption.getSettlementTime();
  //    for (int loopcf = 0; loopcf < nbPeriod; loopcf++) {
  //      iborTime[loopcf + 1] = swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcf).getPaymentTime();
  //      accrualFactor[loopcf] = swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcf).getPaymentYearFraction();
  //      d[loopcf] = displacement;
  //      //TODO: better conversion to double[]
  //      Double[] tmp2 = volatilityFunction.evaluate(iborTime[loopcf]);
  //      for (int looptmp = 0; looptmp < tmp2.length; looptmp++) {
  //        vol[loopcf][looptmp] = tmp2[looptmp];
  //      }
  //    }
  //    setParameters(iborTime, accrualFactor, d, vol, meanReversion);
  //  }

  /**
   * Return the index in the Ibor time list of a given time. The match does not need to be exact (to allow rounding effects and 1 day discrepancy).
   * The allowed difference is set in the TIME_TOLERANCE variable.
   * @param time The time.
   * @return The index.
   */
  public int getTimeIndex(final double time) {
    int index = Arrays.binarySearch(_iborTime, time);
    if (index < 0) {
      if (_iborTime[-index - 1] - time < TIME_TOLERANCE) {
        index = -index - 1;
      } else {
        if (time - _iborTime[-index - 2] < TIME_TOLERANCE) {
          index = -index - 2;
        } else {
          Validate.isTrue(true, "Instrument time incompatible with LMM");
        }
      }
    }
    return index;
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
