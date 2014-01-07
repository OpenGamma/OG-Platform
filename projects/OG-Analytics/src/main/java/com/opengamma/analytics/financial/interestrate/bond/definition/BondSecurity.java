/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Describes a generic single currency bond issue.
 * @param <N> The notional type (usually FixedPayment or CouponInflationZeroCoupon).
 * @param <C> The coupon type.
 */
public abstract class BondSecurity<N extends Payment, C extends Coupon> implements InstrumentDerivative {
  /**
   * The nominal payments. For bullet bond, it is restricted to a single payment.
   */
  private final Annuity<N> _nominal;
  /**
   * The bond coupons. The coupons notional should be in line with the bond nominal. The discounting curve should be the same for the nominal and the coupons.
   */
  private final Annuity<C> _coupon;
  /**
   * The time (in years) to settlement date. Used for dirty/clean price computation.
   */
  private final double _settlementTime;
  /**
   * The bond issuer name.
   */
  private final String _issuerName;
  /**
   * The bond issuer.
   */
  private final LegalEntity _issuer;
  /**
   * The name of the curve used for settlement amount discounting.
   */
  private final String _discountingCurveName;

  /**
   * Bond constructor from the bond nominal and coupon.
   * @param nominal The notional payments.
   * @param coupon The bond coupons.
   * @param settlementTime The time (in years) to settlement date.
   * @param discountingCurveName The name of the curve used for settlement amount discounting.
   * @param issuer The bond issuer name.
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final String discountingCurveName, final String issuer) {
    this(nominal, coupon, settlementTime, discountingCurveName, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Bond constructor from the bond nominal and coupon.
   * @param nominal The notional payments.
   * @param coupon The bond coupons.
   * @param settlementTime The time (in years) to settlement date.
   * @param discountingCurveName The name of the curve used for settlement amount discounting.
   * @param issuer The bond issuer name.
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final String discountingCurveName, final LegalEntity issuer) {
    ArgumentChecker.notNull(nominal, "Nominal");
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(discountingCurveName, "Repo curve name");
    ArgumentChecker.notNull(issuer, "Issuer");
    _nominal = nominal;
    _coupon = coupon;
    _settlementTime = settlementTime;
    _discountingCurveName = discountingCurveName;
    _issuer = issuer;
    _issuerName = issuer.getShortName();
  }

  /**
   * Bond constructor from the bond nominal and coupon.
   * @param nominal The notional payments.
   * @param coupon The bond coupons.
   * @param settlementTime The time (in years) to settlement date.
   * @param issuer The bond issuer name.
   */
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final String issuer) {
    this(nominal, coupon, settlementTime, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Bond constructor from the bond nominal and coupon.
   * @param nominal The notional payments.
   * @param coupon The bond coupons.
   * @param settlementTime The time (in years) to settlement date.
   * @param issuer The bond issuer.
   */
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final LegalEntity issuer) {
    ArgumentChecker.notNull(nominal, "Nominal");
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(issuer, "Issuer");
    _nominal = nominal;
    _coupon = coupon;
    _settlementTime = settlementTime;
    _discountingCurveName = null;
    _issuerName = issuer.getShortName();
    _issuer = issuer;
  }

  /**
   * Gets the nominal payments.
   * @return The nominal payments.
   */
  public Annuity<N> getNominal() {
    return _nominal;
  }

  /**
   * Gets the coupons.
   * @return The coupons.
   */
  public Annuity<C> getCoupon() {
    return _coupon;
  }

  /**
   * Gets the settlement time.
   * @return The settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the bond currency.
   * @return The bond currency.
   */
  public Currency getCurrency() {
    return _nominal.getCurrency();
  }

  /**
   * Gets the name of the curve used for settlement amount discounting.
   * @return The curve name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getRepoCurveName() {
    if (_discountingCurveName == null) {
      throw new IllegalStateException("Repo curve name was not set");
    }
    return _discountingCurveName;
  }

  /**
   * Gets the issuer name.
   * @return The issuer name.
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
   * Gets the bond issuer name and currency.
   * @return The name/currency.
   * @deprecated This information is no longer used in the curve providers.
   */
  @Deprecated
  public Pair<String, Currency> getIssuerCcy() {
    return Pairs.of(_issuerName, _nominal.getCurrency());
  }

  /**
   * Gets the name of the curve used for discounting.
   * @return The curve name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getDiscountingCurveName() {
    return getNominal().getDiscountCurve();
  }

  @Override
  public String toString() {
    String result = "Bond Security:";
    result += "\nNominal: " + _nominal.toString();
    result += "\nCoupon: " + _coupon.toString();
    return result;
  }

  //REVIEW emcleod 17-08-2013 why is the settlement time and issuer not used?
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coupon.hashCode();
    result = prime * result + _nominal.hashCode();
    return result;
  }

  //REVIEW emcleod 17-08-2013 why is the settlement time and issuer not used?
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
    final BondSecurity<?, ?> other = (BondSecurity<?, ?>) obj;
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    return true;
  }

}
