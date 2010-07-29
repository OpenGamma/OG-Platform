/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForwardRateAgreement implements InterestRateDerivative {
  private final double _settlement;
  private final double _maturity;
  private final double _strike;
  private final double _fixingDate;
  private final double _forwardYearFraction;
  private final double _discountingYearFraction;
  private final String _liborCurveName;
  private final String _fundingCurveName;

  /**
   * Set up for a textbook like FRA - the fixed date is the same as settlement date, and year fraction is ACT/ACT between settlement and maturity
   * @param settlement date (in years from today) at which the FRA is cash settled 
   * @param maturity date (in years from today) at which the  reference rate expires 
   * @param strike the agreed fixed payment of the FRA 
   * @param fundingCurveName The name of the curve used for discounting real payments, i.e. taking the PV of the payment made at the settlement date
   * @param liborCurveName The name of the curve used to calculate the reference rate 
   */
  public ForwardRateAgreement(final double settlement, final double maturity, final double strike, final String fundingCurveName, final String liborCurveName) {
    checkInputs(settlement, maturity, strike, fundingCurveName, liborCurveName);
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
    _strike = strike;
    _settlement = settlement;
    _maturity = maturity;
    _fixingDate = settlement;
    _forwardYearFraction = maturity - settlement;
    _discountingYearFraction = _forwardYearFraction;
  }

  /**
   * Set up for real world FRA
   * @param settlement date (in years from today) at which the FRA is cash settled 
   * @param maturity date (in years from today) at which the reference rate expires 
   * @param fixingDate date (in years from today) at which the reference rate is determined (normally 2 days before the settlement date)
   * @param forwardYearFraction year fraction, $\alpha$ used to determine the non-discounted payment, i.e. the hypothetical payment of (F-k) made at the maturity date  
   * @param discountingYearFraction the year fraction used to discount the hypothetical payment at maturity, to an actual payment at settlement (these year fractions will often be the same) 
   * @param strike the agreed fixed payment of the FRA 
   * @param fundingCurveName The name of the curve used for discounting real payments, i.e. taking the PV of the payment made at the settlement date
   * @param liborCurveName The name of the curve used to calculate the reference rate 
   */
  public ForwardRateAgreement(final double settlement, final double maturity, final double fixingDate, final double forwardYearFraction, final double discountingYearFraction, final double strike,
      final String fundingCurveName, final String liborCurveName) {
    checkInputs(settlement, maturity, strike, fundingCurveName, liborCurveName);
    ArgumentChecker.notNegative(fixingDate, "fixing Date");
    ArgumentChecker.notNegative(forwardYearFraction, "forward year fraction");
    ArgumentChecker.notNegative(discountingYearFraction, "dicounting year fraction");
    Validate.isTrue(fixingDate <= settlement, "must have fixing date before or equal to settlement date");

    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
    _strike = strike;
    _settlement = settlement;
    _maturity = maturity;
    _fixingDate = fixingDate;
    _forwardYearFraction = forwardYearFraction;
    _discountingYearFraction = discountingYearFraction;
  }

  private void checkInputs(final double settlementDate, final double maturity, final double strike, final String fundingCurveName, final String liborCurveName) {
    ArgumentChecker.notNegative(settlementDate, "settlement date");
    ArgumentChecker.notNegative(maturity, "maturity");
    ArgumentChecker.notNegative(strike, "strike");
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    if (settlementDate >= maturity) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
  }

  public double getSettlementDate() {
    return _settlement;
  }

  public double getMaturity() {
    return _maturity;
  }

  public String getLiborCurveName() {
    return _liborCurveName;
  }

  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  public double getStrike() {
    return _strike;
  }

  public double getFixingDate() {
    return _fixingDate;
  }

  public double getForwardYearFraction() {
    return _forwardYearFraction;
  }

  public double getDiscountingYearFraction() {
    return _discountingYearFraction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_discountingYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingDate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forwardYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_fundingCurveName == null) ? 0 : _fundingCurveName.hashCode());
    result = prime * result + ((_liborCurveName == null) ? 0 : _liborCurveName.hashCode());
    temp = Double.doubleToLongBits(_maturity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForwardRateAgreement other = (ForwardRateAgreement) obj;
    if (Double.doubleToLongBits(_discountingYearFraction) != Double.doubleToLongBits(other._discountingYearFraction)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingDate) != Double.doubleToLongBits(other._fixingDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_forwardYearFraction) != Double.doubleToLongBits(other._forwardYearFraction)) {
      return false;
    }
    if (_fundingCurveName == null) {
      if (other._fundingCurveName != null) {
        return false;
      }
    } else if (!_fundingCurveName.equals(other._fundingCurveName)) {
      return false;
    }
    if (_liborCurveName == null) {
      if (other._liborCurveName != null) {
        return false;
      }
    } else if (!_liborCurveName.equals(other._liborCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_maturity) != Double.doubleToLongBits(other._maturity)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlement) != Double.doubleToLongBits(other._settlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitForwardRateAgreement(this, curves);
  }

}
