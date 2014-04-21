/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Describes a generic single currency bond issue.
 * @param <N> The notional type (usually FixedPayment or CouponInflationZeroCoupon).
 * @param <C> The coupon type.
 */
public abstract class BondSecurityDefinition<N extends PaymentDefinition, C extends CouponDefinition> implements
    InstrumentDefinition<BondSecurity<? extends Payment, ? extends Coupon>> {
  /**
   * The notional payments. For bullet bond, it is restricted to a single payment.
   */
  private final AnnuityDefinition<N> _nominal;
  /**
   * The bond coupons. The coupons notional should be in line with the bond nominal.
   */
  private final AnnuityDefinition<C> _coupon;
  /**
   * Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   */
  private final int _exCouponDays;
  /**
   * Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   */
  private final int _settlementDays;
  /**
   * The calendar used to compute the standard settlement date.
   */
  private final Calendar _calendar;
  /**
   * The bond issuer name.
   */
  private final String _issuerName;
  /**
   * The issuer.
   */
  private final LegalEntity _issuer;
  /**
   * The bond repo type.
   */
  private final String _repoType;

  /**
   * Bond constructor from all the bond details. The repo type is set to an empty string and the legal entity
   * only contains the issuer name.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param issuer The issuer name.
   */
  public BondSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final String issuer) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, new LegalEntity(null, issuer, null, null, null), "");
  }

  /**
   * Bond constructor from all the bond details. The repo type is set to an empty string.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param issuer The issuer name.
   */
  public BondSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final LegalEntity issuer) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, "");
  }

  /**
   * Bond constructor from all the bond details. The legal entity only contains the issuer name.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   */
  public BondSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final String issuer, final String repoType) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, new LegalEntity(null, issuer, null, null, null), repoType);
  }

  /**
   * Bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   */
  public BondSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final LegalEntity issuer, final String repoType) {
    ArgumentChecker.notNull(nominal, "Nominal");
    ArgumentChecker.notNull(coupon, "Coupons");
    ArgumentChecker.isTrue(nominal.getCurrency().equals(coupon.getCurrency()), "Currency of nominal {} and coupons {} should be the same", nominal.getCurrency(),
        coupon.getCurrency());
    ArgumentChecker.isTrue(nominal.getNthPayment(nominal.getNumberOfPayments() - 1).getReferenceAmount() > 0, "Notional should be positive");
    ArgumentChecker.isTrue(coupon.getNthPayment(coupon.getNumberOfPayments() - 1).getReferenceAmount() > 0, "Coupon notional should be positive");
    ArgumentChecker.notNull(issuer, "issuer");
    ArgumentChecker.notNull(repoType, "repo type");
    _nominal = nominal;
    _coupon = coupon;
    // TODO: check that the coupon and nominal correspond in term of remaining notional (in the case of amortization)
    _exCouponDays = exCouponDays;
    _settlementDays = settlementDays;
    _calendar = calendar;
    _issuerName = issuer.getShortName();
    _issuer = issuer;
    _repoType = repoType;
  }

  /**
   * Gets the nominal.
   * @return The nominal.
   */
  public AnnuityDefinition<N> getNominal() {
    return _nominal;
  }

  /**
   * Gets the coupons.
   * @return The coupons.
   */
  public AnnuityDefinition<C> getCoupons() {
    return _coupon;
  }

  /**
   * Gets the number of ex-coupon days.
   * @return The ex-coupon days.
   */
  public int getExCouponDays() {
    return _exCouponDays;
  }

  /**
   * Gets the number of days to standard settlement.
   * @return The days to settlement.
   */
  public int getSettlementDays() {
    return _settlementDays;
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
   * Gets the _repoType name.
   * @return the _repoType
   */
  public String getRepoType() {
    return _repoType;
  }

  /**
   * Gets the currency.
   * @return The bond currency.
   */
  public Currency getCurrency() {
    return _nominal.getCurrency();
  }

  /**
   * Gets the calendar used to compute the standard settlement date.
   * @return The calendar.
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  @Override
  public String toString() {
    String result = "Bond : \n";
    result += "Notional: " + _nominal.toString();
    result += "\nCoupons: \n" + _coupon.toString() + "\n";
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coupon.hashCode();
    result = prime * result + _exCouponDays;
    result = prime * result + _issuer.hashCode();
    result = prime * result + _nominal.hashCode();
    result = prime * result + _repoType.hashCode();
    result = prime * result + _settlementDays;
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
    final BondSecurityDefinition<?, ?> other = (BondSecurityDefinition<?, ?>) obj;
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuer, other._issuer)) {
      return false;
    }
    if (_exCouponDays != other._exCouponDays) {
      return false;
    }
    if (!ObjectUtils.equals(_repoType, other._repoType)) {
      return false;
    }
    if (_settlementDays != other._settlementDays) {
      return false;
    }
    return true;
  }

}
