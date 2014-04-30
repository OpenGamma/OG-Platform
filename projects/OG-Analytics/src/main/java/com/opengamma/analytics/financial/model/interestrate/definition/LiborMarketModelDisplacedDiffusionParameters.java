/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import java.util.Arrays;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Parameters related to a multi-factor Libor Market Model with separable
 * displaced diffusion dynamic.  The equations underlying the Libor Market
 * Model in the probability space with numeraire $P(.,t_{j+1})$ are
 * $$
 * \begin{equation*}
 * dL_t^j = \alpha(t) (L+a_{j}) \gamma_{j} . dW_t^{j+1}
 * \end{equation*}
 * $$
 * with $\alpha(t) = \exp(a t)$. The $\gamma_j$ are m-dimensional vectors.
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

  /**
   * Initialises the mean reversion and number of parameters to zero, and other parameters to empty arrays.
   */
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
  public LiborMarketModelDisplacedDiffusionParameters(final double[] iborTime, final double[] accrualFactor, final double[] displacement, final double[][] volatility, final double meanReversion) {
    ArgumentChecker.notNull(iborTime, "LMM Libor times");
    ArgumentChecker.notNull(accrualFactor, "LMM accrual factors");
    ArgumentChecker.notNull(displacement, "LMM displacements");
    ArgumentChecker.notNull(volatility, "LMM volatility");
    _nbPeriod = accrualFactor.length;
    ArgumentChecker.isTrue(iborTime.length == _nbPeriod + 1, "LMM data: Dimension");
    ArgumentChecker.isTrue(_nbPeriod == displacement.length, "LMM data: Dimension");
    ArgumentChecker.isTrue(_nbPeriod == volatility.length, "LMM data: Dimension");
    _iborTime = iborTime;
    _accrualFactor = accrualFactor;
    _displacement = displacement;
    _volatility = volatility;
    _meanReversion = meanReversion;
    _nbFactor = volatility[0].length;
  }

  /**
   * Create a new copy of the object with the same data. All the arrays are cloned.
   * @return The LMM parameters.
   */
  public LiborMarketModelDisplacedDiffusionParameters copy() {
    final double[][] vol = new double[_volatility.length][];
    for (int loopperiod = 0; loopperiod < _volatility.length; loopperiod++) {
      vol[loopperiod] = _volatility[loopperiod].clone();
    }
    return new LiborMarketModelDisplacedDiffusionParameters(_iborTime.clone(), _accrualFactor.clone(), _displacement.clone(), vol, _meanReversion);
  }

  /**
   * Create model parameters adapted to a specific annuity.
   * @param modelDate The pricing date, not null
   * @param annuity The annuity to be used for the model construction, not null
   * @param dayCount The Ibor day count, not null
   * @param displacement The displacement (common to all Ibors).
   * @param meanReversion The mean reversion.
   * @param volatilityFunction The volatility function. For a given time to Ibor period start date it provides the volatilities (or weights) of the different factors, not null
   * @return A Libor Market Model parameter set.
   */
  public static LiborMarketModelDisplacedDiffusionParameters from(final ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity,
      final DayCount dayCount, final double displacement, final double meanReversion, final Function1D<Double, Double[]> volatilityFunction) {
    ArgumentChecker.notNull(modelDate, "modelDate");
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(dayCount, "dayCount");
    ArgumentChecker.notNull(volatilityFunction, "volatilityFunction");
    final int nbPeriod = annuity.getNumberOfPayments();
    final ZonedDateTime[] iborDate = new ZonedDateTime[nbPeriod + 1];
    final double[] iborTime = new double[nbPeriod + 1];
    final double[] accrualFactor = new double[nbPeriod];
    final double[] d = new double[nbPeriod];
    final Double[] tmp = volatilityFunction.evaluate(0.0);
    final double[][] vol = new double[nbPeriod][tmp.length];
    iborDate[0] = annuity.getNthPayment(0).getAccrualStartDate();
    iborTime[0] = TimeCalculator.getTimeBetween(modelDate, iborDate[0]);
    for (int loopcf = 0; loopcf < nbPeriod; loopcf++) {
      iborDate[loopcf + 1] = annuity.getNthPayment(loopcf).getPaymentDate();
      iborTime[loopcf + 1] = TimeCalculator.getTimeBetween(modelDate, iborDate[loopcf + 1]);
      accrualFactor[loopcf] = dayCount.getDayCountFraction(iborDate[loopcf], iborDate[loopcf + 1], annuity.getCalendar());
      d[loopcf] = displacement;
      //TODO: better conversion to double[]
      final Double[] tmp2 = volatilityFunction.evaluate(iborTime[loopcf]);
      for (int looptmp = 0; looptmp < tmp2.length; looptmp++) {
        vol[loopcf][looptmp] = tmp2[looptmp];
      }
    }
    return new LiborMarketModelDisplacedDiffusionParameters(iborTime, accrualFactor, d, vol, meanReversion);
  }

  /**
   * Creates model parameters adapted to a specific physical delivery fixed-float swaption.
   * @param swaption The swaption, not null
   * @param displacement The displacement
   * @param meanReversion The mean reversion
   * @param volatilityFunction The volatility function. For a given time to Ibor period start date it provides the volatilities (or weights) of the different factors, not null
   * @return A Libor Market Model parameter set.
   */
  public static LiborMarketModelDisplacedDiffusionParameters from(final SwaptionPhysicalFixedIbor swaption, final double displacement,
      final double meanReversion, final Function1D<Double, Double[]> volatilityFunction) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(volatilityFunction, "volatilityFunction");
    final int nbPeriod = swaption.getUnderlyingSwap().getSecondLeg().getNumberOfPayments();
    final double[] iborTime = new double[nbPeriod + 1];
    final double[] accrualFactor = new double[nbPeriod];
    final double[] d = new double[nbPeriod];
    final Double[] tmp = volatilityFunction.evaluate(0.0);
    final double[][] vol = new double[nbPeriod][tmp.length];
    iborTime[0] = swaption.getSettlementTime();
    for (int loopcf = 0; loopcf < nbPeriod; loopcf++) {
      iborTime[loopcf + 1] = swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcf).getPaymentTime();
      accrualFactor[loopcf] = swaption.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcf).getPaymentYearFraction();
      d[loopcf] = displacement;
      //TODO: better conversion to double[]
      final Double[] tmp2 = volatilityFunction.evaluate(iborTime[loopcf]);
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
   * Gets the time tolerance.
   * @return The time tolerance
   */
  public double getTimeTolerance() {
    return TIME_TOLERANCE;
  }

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
          throw new IllegalArgumentException("Instrument time incompatible with LMM");
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
  public final void setVolatility(final double[][] volatility, final int startIndex) {
    ArgumentChecker.notNull(volatility, "LMM volatility");
    ArgumentChecker.isTrue(volatility[0].length == _nbFactor, "LMM: incorrect number of factors");
    for (int loopperiod = 0; loopperiod < volatility.length; loopperiod++) {
      System.arraycopy(volatility[loopperiod], 0, _volatility[startIndex + loopperiod], 0, volatility[loopperiod].length);
    }
  }

  /**
   * Change the model displacement in a block to a given displacement vector.
   * @param displacement The change displacement.
   * @param startIndex The start index for the block to change.
   */
  public final void setDisplacement(final double[] displacement, final int startIndex) {
    ArgumentChecker.notNull(displacement, "LMM displacement");
    System.arraycopy(displacement, 0, _displacement, startIndex, displacement.length);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_accrualFactor);
    result = prime * result + Arrays.hashCode(_displacement);
    result = prime * result + Arrays.hashCode(_iborTime);
    long temp;
    temp = Double.doubleToLongBits(_meanReversion);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _nbFactor;
    result = prime * result + _nbPeriod;
    result = prime * result + Arrays.hashCode(_volatility);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof LiborMarketModelDisplacedDiffusionParameters)) {
      return false;
    }
    final LiborMarketModelDisplacedDiffusionParameters other = (LiborMarketModelDisplacedDiffusionParameters) obj;
    if (_nbFactor != other._nbFactor) {
      return false;
    }
    if (Double.doubleToLongBits(_meanReversion) != Double.doubleToLongBits(other._meanReversion)) {
      return false;
    }
    if (_nbPeriod != other._nbPeriod) {
      return false;
    }
    if (!Arrays.equals(_accrualFactor, other._accrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_displacement, other._displacement)) {
      return false;
    }
    if (!Arrays.equals(_iborTime, other._iborTime)) {
      return false;
    }
    if (!Arrays.deepEquals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
