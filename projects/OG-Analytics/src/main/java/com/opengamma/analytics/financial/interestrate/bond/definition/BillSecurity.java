/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Describes a (Treasury) Bill with settlement date.
 */
public class BillSecurity implements InstrumentDerivative {

  /**
   * The bill currency.
   */
  private final Currency _currency;
  /**
   * The bill time to settlement.
   */
  private final double _settlementTime;
  /**
   * The bill end or maturity time.
   */
  private final double _endTime;
  /**
   * The bill nominal.
   */
  private final double _notional;
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * The accrual factor in the bill day count between settlement and maturity.
   */
  private final double _accrualFactor;
  /**
   * The bill issuer name.
   */
  private final String _issuerName;
  /**
   * The bill issuer.
   */
  private final LegalEntity _issuer;
  /**
   * The name of the curve used for the bill cash flows (issuer credit).
   */
  private final String _creditCurveName;
  /**
   * The name of the curve used for settlement amount discounting.
   */
  private final String _discountingCurveName;

  /**
   * Constructor from all details. The legal entity contains only the issuer name.
   * @param currency The bill currency.
   * @param settlementTime The bill time to settlement.
   * @param endTime The bill end or maturity time.
   * @param notional The bill nominal.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param accrualFactor The accrual factor in the bill day count between settlement and maturity.
   * @param issuer The bill issuer name.
   * @param creditCurveName The name of the curve used for the bill cash flows (issuer credit).
   * @param discountingCurveName The name of the curve used for settlement amount discounting.
   * @deprecated Use the constructor that does not take curve names
   */
  @Deprecated
  public BillSecurity(final Currency currency, final double settlementTime, final double endTime, final double notional, final YieldConvention yieldConvention,
      final double accrualFactor, final String issuer, final String creditCurveName, final String discountingCurveName) {
    this(currency, settlementTime, endTime, notional, yieldConvention, accrualFactor, new LegalEntity(null, issuer, null, null, null), creditCurveName, discountingCurveName);
  }

  /**
   * Constructor from all details.
   * @param currency The bill currency.
   * @param settlementTime The bill time to settlement.
   * @param endTime The bill end or maturity time.
   * @param notional The bill nominal.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param accrualFactor The accrual factor in the bill day count between settlement and maturity.
   * @param issuer The bill issuer name.
   * @param creditCurveName The name of the curve used for the bill cash flows (issuer credit).
   * @param discountingCurveName The name of the curve used for settlement amount discounting.
   * @deprecated Use the constructor that does not take curve names
   */
  @Deprecated
  public BillSecurity(final Currency currency, final double settlementTime, final double endTime, final double notional, final YieldConvention yieldConvention,
      final double accrualFactor, final LegalEntity issuer, final String creditCurveName, final String discountingCurveName) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    ArgumentChecker.notNull(issuer, "Issuer");
    ArgumentChecker.notNull(creditCurveName, "Credit curve");
    ArgumentChecker.notNull(discountingCurveName, "Discounting curve");
    ArgumentChecker.isTrue(notional > 0.0, "Notional should be positive");
    ArgumentChecker.isTrue(endTime >= settlementTime, "End time should be after settlement time");
    ArgumentChecker.isTrue(settlementTime >= 0, "Settlement time should be positive");
    _currency = currency;
    _endTime = endTime;
    _settlementTime = settlementTime;
    _notional = notional;
    _yieldConvention = yieldConvention;
    _accrualFactor = accrualFactor;
    _issuerName = issuer.getShortName();
    _issuer = issuer;
    _creditCurveName = creditCurveName;
    _discountingCurveName = discountingCurveName;
  }

  /**
   * Constructor from all details. The legal entity contains only the issuer name.
   * @param currency The bill currency.
   * @param settlementTime The bill time to settlement.
   * @param endTime The bill end or maturity time.
   * @param notional The bill nominal.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param accrualFactor The accrual factor in the bill day count between settlement and maturity.
   * @param issuer The bill issuer name.
   */
  public BillSecurity(final Currency currency, final double settlementTime, final double endTime, final double notional, final YieldConvention yieldConvention, final double accrualFactor,
      final String issuer) {
    this(currency, settlementTime, endTime, notional, yieldConvention, accrualFactor, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Constructor from all details.
   * @param currency The bill currency.
   * @param settlementTime The bill time to settlement.
   * @param endTime The bill end or maturity time.
   * @param notional The bill nominal.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param accrualFactor The accrual factor in the bill day count between settlement and maturity.
   * @param issuer The bill issuer name.
   */
  public BillSecurity(final Currency currency, final double settlementTime, final double endTime, final double notional, final YieldConvention yieldConvention, final double accrualFactor,
      final LegalEntity issuer) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    ArgumentChecker.notNull(issuer, "Issuer");
    ArgumentChecker.isTrue(notional > 0.0, "Notional should be positive");
    ArgumentChecker.isTrue(endTime >= settlementTime, "End time should be after settlement time");
    ArgumentChecker.isTrue(settlementTime >= 0, "Settlement time should be positive");
    _currency = currency;
    _endTime = endTime;
    _settlementTime = settlementTime;
    _notional = notional;
    _yieldConvention = yieldConvention;
    _accrualFactor = accrualFactor;
    _issuerName = issuer.getShortName();
    _issuer = issuer;
    _creditCurveName = null;
    _discountingCurveName = null;
  }

  /**
   * Get the bill currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the bill time to settlement.
   * @return The time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the bill end or maturity time.
   * @return The time.
   */
  public double getEndTime() {
    return _endTime;
  }

  /**
   * Gets the bill notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the yield (to maturity) computation convention.
   * @return The convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the accrual factor in the bill day count between settlement and maturity.
   * @return The accrual factor.
   */
  public double getAccrualFactor() {
    return _accrualFactor;
  }

  /**
   * Gets the bill issuer name.
   * @return The name.
   */
  public String getIssuer() {
    return _issuerName;
  }

  /**
   * Gets the issuer.
   * @return The issuer
   */
  public LegalEntity getIssuerEntity() {
    return _issuer;
  }

  /**
   * Gets the bill issuer name and currency.
   * @return The name/currency.
   * @deprecated This information is no longer used in the curve providers.
   */
  @Deprecated
  public Pair<String, Currency> getIssuerCcy() {
    return Pairs.of(_issuerName, _currency);
  }

  /**
   * Gets the name of the curve used for settlement amount discounting.
   * @return The name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getDiscountingCurveName() {
    if (_discountingCurveName == null) {
      throw new IllegalStateException("Discounting curve name was not set");
    }
    return _discountingCurveName;
  }

  /**
   * Gets the name of the curve used for the bill cash flows (issuer credit).
   * @return The name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getCreditCurveName() {
    if (_creditCurveName == null) {
      throw new IllegalStateException("Credit curve name was not set");
    }
    return _creditCurveName;
  }

  @Override
  public String toString() {
    return "Bill " + _issuerName + " " + _currency + ": settle" + _settlementTime + " - maturity " + _endTime + " - notional " + _notional;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_creditCurveName == null ? 0 : _creditCurveName.hashCode());
    result = prime * result + _currency.hashCode();
    result = prime * result + (_discountingCurveName == null ? 0 : _discountingCurveName.hashCode());
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _issuerName.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _yieldConvention.hashCode();
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BillSecurity other = (BillSecurity) obj;
    if (Double.doubleToLongBits(_accrualFactor) != Double.doubleToLongBits(other._accrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_creditCurveName, other._creditCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (_issuerName == null) {
      if (other._issuerName != null) {
        return false;
      }
    } else if (!_issuerName.equals(other._issuerName)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
