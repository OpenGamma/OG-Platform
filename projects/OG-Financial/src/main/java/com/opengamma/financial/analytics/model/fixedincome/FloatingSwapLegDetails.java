/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.io.Serializable;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;

/**
 * Container for the relevant details for pricing a floating swap leg, with the
 * entries
 * <p>
 * <li></li>
 */
@Deprecated
@BeanDefinition
public class  FloatingSwapLegDetails extends DirectBean implements Serializable {
  /**
   * The start accrual dates label.
   */
  public static final String START_ACCRUAL_DATES = "Start Accrual Date";
  /**
   * The end accrual dates label.
   */
  public static final String END_ACCRUAL_DATES = "End Accrual Date";
  /**
   * The accrual fraction label.
   */
  public static final String ACCRUAL_YEAR_FRACTION = "Accrual Year Fraction";
  /**
   * The start fixing date label.
   */
  public static final String START_FIXING_DATES = "Start Fixing Date";
  /**
   * The end fixing date label.
   */
  public static final String END_FIXING_DATES = "End Fixing Date";
  /**
   * The fixing fraction label.
   */
  public static final String FIXING_FRACTIONS = "Fixing Year Fraction";
  /**
   * The forward rate. Used when the fixing is in the future.
   */
  public static final String FORWARD_RATE = "Forward Rate";
  /**
   * The fixed rate. Used when the fixing is known.
   */
  public static final String FIXED_RATE = "Fixed Rate";
  /**
   * The payment date.
   */
  public static final String PAYMENT_DATE = "Payment Date";
  /**
   * The payment time.
   */
  public static final String PAYMENT_TIME = "Payment Time";
  /**
   * The payment amount.
   */
  public static final String PAYMENT_AMOUNT = "Payment Amount";
  /**
   * The notional.
   */
  public static final String NOTIONAL = "Notional";
  /**
   * The spread.
   */
  public static final String SPREAD = "Spread";
  /**
   * The gearing.
   */
  public static final String GEARING = "Gearing";
  /**
   * The payment discount factor. Used when the fixing is known
   */
  public static final String PAYMENT_DISCOUNT_FACTOR = "Payment Discount Factor";
  /**
   * The projected amount.
   */
  public static final String PROJECTED_AMOUNT = "Projected Amount";
  /**
   * The index tenor.
   */
  public static final String INDEX_TERM = "Index Tenor";

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * An array of accrual start dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _accrualStart;

  /**
   * An array of accrual end dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _accrualEnd;

  /**
   * An array of accrual year fractions.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _accrualYearFractions;

  /**
   * An array of fixing start dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _fixingStart;

  /**
   * An array of fixing end dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _fixingEnd;

  /**
   * An array of fixing year fractions. May contain null values if there have been fixings as of the valuation date.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _fixingYearFractions;

  /**
   * An array of forward rates.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _forwardRates;

  /**
   * An array of fixed rates. May contain null values if there have been no fixings as of the valuation date.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _fixedRates;

  /**
   * An array of payment dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _paymentDates;

  /**
   * An array of payment times.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _paymentTimes;

  /**
   * An array of payment amounts. May contain nulls if there have been no fixings as of the valuation date.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _paymentAmounts;

  /**
   * An array of notionals.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _notionals;

  /**
   * An array of spreads.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _spreads;

  /**
   * An array of gearings.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _gearings;

  /**
   * An array of payment discount factors.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _paymentDiscountFactors;

  /**
   * An array of projected amounts. May contain nulls if there has been a fixing as of the valuation date.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _projectedAmounts;

  /**
   * An array of index tenors. May contain nulls if there has been a fixing as of the valuation date.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor[] _indexTenors;
  /**
   * The discounted payment amount
   */
  public static final String DISCOUNTED_PAYMENT_AMOUNT = "Discounted Payment Amount";
  /**
   * The discounted projected amount
   */
  public static final String DISCOUNTED_PROJECTED_PAYMENT = "Discounted Projected Payment";

  /**
   * For the builder.
   */
  /* package */FloatingSwapLegDetails() {
    super();
  }

  /**
   * @param accrualStartDates The start accrual dates, not null
   * @param accrualEndDates The end accrual dates, not null
   * @param accrualYearFractions The accrual year fractions, not null
   * @param fixingStart The fixing start dates, not null
   * @param fixingEnd The fixing end dates, not null
   * @param fixingYearFractions The fixing year fractions, not null
   * @param forwardRates The forward rates, not null
   * @param fixedRates The fixed rates, not null
   * @param paymentDates The payment dates, not null
   * @param paymentTimes The payment times, not null
   * @param paymentDiscountFactors The payment discount factors, not null
   * @param paymentAmounts The payment amounts, not null
   * @param projectedAmounts The projected amounts, not null
   * @param notionals The notionals, not null
   * @param spreads The spreads, not null
   * @param gearings The gearings, not null
   * @param indexTenors The index tenors, not null
   */
  public FloatingSwapLegDetails(final LocalDate[] accrualStartDates, final LocalDate[] accrualEndDates, final double[] accrualYearFractions,
      final LocalDate[] fixingStart, final LocalDate[] fixingEnd, final Double[] fixingYearFractions, final Double[] forwardRates,
      final Double[] fixedRates, final LocalDate[] paymentDates, final double[] paymentTimes, final double[] paymentDiscountFactors,
      final CurrencyAmount[] paymentAmounts, final CurrencyAmount[] projectedAmounts, final CurrencyAmount[] notionals, final double[] spreads,
      final double[] gearings, final Tenor[] indexTenors) {
    setAccrualStart(accrualStartDates);
    setAccrualEnd(accrualEndDates);
    setAccrualYearFractions(accrualYearFractions);
    setFixingStart(fixingStart);
    setFixingEnd(fixingEnd);
    setFixingYearFractions(fixingYearFractions);
    setForwardRates(forwardRates);
    setFixedRates(fixedRates);
    setPaymentDates(paymentDates);
    setPaymentTimes(paymentTimes);
    setPaymentDiscountFactors(paymentDiscountFactors);
    setPaymentAmounts(paymentAmounts);
    setProjectedAmounts(projectedAmounts);
    setNotionals(notionals);
    setSpreads(spreads);
    setGearings(gearings);
    setIndexTenors(indexTenors);
    final int n = notionals.length;
    ArgumentChecker.isTrue(n == accrualStartDates.length, "number of accrual start dates must equal number of notionals");
    ArgumentChecker.isTrue(n == accrualEndDates.length, "number of accrual end dates must equal number of notionals");
    ArgumentChecker.isTrue(n == accrualYearFractions.length, "number of accrual year fractions must equal number of notionals");
    ArgumentChecker.isTrue(n == fixingStart.length, "number of fixing start dates must equal number of notionals");
    ArgumentChecker.isTrue(n == fixingEnd.length, "number of fixing end dates must equal number of notionals");
    ArgumentChecker.isTrue(n == fixingYearFractions.length, "number of fixing year fractions must equal number of notionals");
    ArgumentChecker.isTrue(n == forwardRates.length, "number of forward rates must equal number of notionals");
    ArgumentChecker.isTrue(n == fixedRates.length, "number of fixed rates must equal number of notionals");
    ArgumentChecker.isTrue(n == paymentDates.length, "number of payment dates must equal number of notionals");
    ArgumentChecker.isTrue(n == paymentDiscountFactors.length, "number of payment discount factors must equal number of notionals");
    ArgumentChecker.isTrue(n == paymentAmounts.length, "number of payment amounts must equal number of notionals");
    ArgumentChecker.isTrue(n == projectedAmounts.length, "number of projected amounts must equal number of notionals");
    ArgumentChecker.isTrue(n == spreads.length, "number of spreads must equal number of notionals");
    ArgumentChecker.isTrue(n == gearings.length, "number of gearings must equal number of notionals");
    ArgumentChecker.isTrue(n == indexTenors.length, "number of index tenors must equal number of notionals");
  }

  /**
   * Gets the total number of cash-flows.
   * @return The total number of cash-flows
   */
  @DerivedProperty
  public int getNumberOfCashFlows() {
    return getNotionals().length;
  }

  /**
   * Gets the number of fixed cash-flows.
   * @return The number of fixed cash-flows
   */
  @DerivedProperty
  public int getNumberOfFixedCashFlows() {
    return getFixedRates().length;
  }

  /**
   * Gets the number of floating cash-flows.
   * @return The number of floating cash-flows
   */
  @DerivedProperty
  public int getNumberOfFloatingCashFlows() {
    return getForwardRates().length;
  }

  /**
   * Gets the discounted payment amounts.
   * @return the discounted cashflows
   */
  @DerivedProperty
  public CurrencyAmount[] getDiscountedPaymentAmounts() {
    final CurrencyAmount[] cashflows = new CurrencyAmount[getNumberOfCashFlows()];
    for (int i = 0; i < getNumberOfCashFlows(); i++) {
      final CurrencyAmount payment = getPaymentAmounts()[i];
      if (payment == null) {
        continue;
      }
      final double df = getPaymentDiscountFactors()[i];
      cashflows[i] = CurrencyAmount.of(payment.getCurrency(), payment.getAmount() * df);
    }
    return cashflows;
  }

  /**
   * Gets the discounted projected payment amounts.
   * @return the discounted cashflows
   */
  @DerivedProperty
  public CurrencyAmount[] getDiscountedProjectedAmounts() {
    final CurrencyAmount[] cashflows = new CurrencyAmount[getNumberOfCashFlows()];
    for (int i = 0; i < getNumberOfCashFlows(); i++) {
      final CurrencyAmount payment = getProjectedAmounts()[i];
      if (payment == null) {
        continue;
      }
      final double df = getPaymentDiscountFactors()[i];
      cashflows[i] = CurrencyAmount.of(payment.getCurrency(), payment.getAmount() * df);
    }
    return cashflows;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FloatingSwapLegDetails}.
   * @return the meta-bean, not null
   */
  public static FloatingSwapLegDetails.Meta meta() {
    return FloatingSwapLegDetails.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FloatingSwapLegDetails.Meta.INSTANCE);
  }

  @Override
  public FloatingSwapLegDetails.Meta metaBean() {
    return FloatingSwapLegDetails.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual start dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getAccrualStart() {
    return _accrualStart;
  }

  /**
   * Sets an array of accrual start dates.
   * @param accrualStart  the new value of the property, not null
   */
  public void setAccrualStart(LocalDate[] accrualStart) {
    JodaBeanUtils.notNull(accrualStart, "accrualStart");
    this._accrualStart = accrualStart;
  }

  /**
   * Gets the the {@code accrualStart} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> accrualStart() {
    return metaBean().accrualStart().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual end dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getAccrualEnd() {
    return _accrualEnd;
  }

  /**
   * Sets an array of accrual end dates.
   * @param accrualEnd  the new value of the property, not null
   */
  public void setAccrualEnd(LocalDate[] accrualEnd) {
    JodaBeanUtils.notNull(accrualEnd, "accrualEnd");
    this._accrualEnd = accrualEnd;
  }

  /**
   * Gets the the {@code accrualEnd} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> accrualEnd() {
    return metaBean().accrualEnd().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual year fractions.
   * @return the value of the property, not null
   */
  public double[] getAccrualYearFractions() {
    return _accrualYearFractions;
  }

  /**
   * Sets an array of accrual year fractions.
   * @param accrualYearFractions  the new value of the property, not null
   */
  public void setAccrualYearFractions(double[] accrualYearFractions) {
    JodaBeanUtils.notNull(accrualYearFractions, "accrualYearFractions");
    this._accrualYearFractions = accrualYearFractions;
  }

  /**
   * Gets the the {@code accrualYearFractions} property.
   * @return the property, not null
   */
  public final Property<double[]> accrualYearFractions() {
    return metaBean().accrualYearFractions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixing start dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getFixingStart() {
    return _fixingStart;
  }

  /**
   * Sets an array of fixing start dates.
   * @param fixingStart  the new value of the property, not null
   */
  public void setFixingStart(LocalDate[] fixingStart) {
    JodaBeanUtils.notNull(fixingStart, "fixingStart");
    this._fixingStart = fixingStart;
  }

  /**
   * Gets the the {@code fixingStart} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> fixingStart() {
    return metaBean().fixingStart().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixing end dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getFixingEnd() {
    return _fixingEnd;
  }

  /**
   * Sets an array of fixing end dates.
   * @param fixingEnd  the new value of the property, not null
   */
  public void setFixingEnd(LocalDate[] fixingEnd) {
    JodaBeanUtils.notNull(fixingEnd, "fixingEnd");
    this._fixingEnd = fixingEnd;
  }

  /**
   * Gets the the {@code fixingEnd} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> fixingEnd() {
    return metaBean().fixingEnd().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixing year fractions. May contain null values if there have been fixings as of the valuation date.
   * @return the value of the property, not null
   */
  public Double[] getFixingYearFractions() {
    return _fixingYearFractions;
  }

  /**
   * Sets an array of fixing year fractions. May contain null values if there have been fixings as of the valuation date.
   * @param fixingYearFractions  the new value of the property, not null
   */
  public void setFixingYearFractions(Double[] fixingYearFractions) {
    JodaBeanUtils.notNull(fixingYearFractions, "fixingYearFractions");
    this._fixingYearFractions = fixingYearFractions;
  }

  /**
   * Gets the the {@code fixingYearFractions} property.
   * @return the property, not null
   */
  public final Property<Double[]> fixingYearFractions() {
    return metaBean().fixingYearFractions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of forward rates.
   * @return the value of the property, not null
   */
  public Double[] getForwardRates() {
    return _forwardRates;
  }

  /**
   * Sets an array of forward rates.
   * @param forwardRates  the new value of the property, not null
   */
  public void setForwardRates(Double[] forwardRates) {
    JodaBeanUtils.notNull(forwardRates, "forwardRates");
    this._forwardRates = forwardRates;
  }

  /**
   * Gets the the {@code forwardRates} property.
   * @return the property, not null
   */
  public final Property<Double[]> forwardRates() {
    return metaBean().forwardRates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixed rates. May contain null values if there have been no fixings as of the valuation date.
   * @return the value of the property, not null
   */
  public Double[] getFixedRates() {
    return _fixedRates;
  }

  /**
   * Sets an array of fixed rates. May contain null values if there have been no fixings as of the valuation date.
   * @param fixedRates  the new value of the property, not null
   */
  public void setFixedRates(Double[] fixedRates) {
    JodaBeanUtils.notNull(fixedRates, "fixedRates");
    this._fixedRates = fixedRates;
  }

  /**
   * Gets the the {@code fixedRates} property.
   * @return the property, not null
   */
  public final Property<Double[]> fixedRates() {
    return metaBean().fixedRates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getPaymentDates() {
    return _paymentDates;
  }

  /**
   * Sets an array of payment dates.
   * @param paymentDates  the new value of the property, not null
   */
  public void setPaymentDates(LocalDate[] paymentDates) {
    JodaBeanUtils.notNull(paymentDates, "paymentDates");
    this._paymentDates = paymentDates;
  }

  /**
   * Gets the the {@code paymentDates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> paymentDates() {
    return metaBean().paymentDates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment times.
   * @return the value of the property, not null
   */
  public double[] getPaymentTimes() {
    return _paymentTimes;
  }

  /**
   * Sets an array of payment times.
   * @param paymentTimes  the new value of the property, not null
   */
  public void setPaymentTimes(double[] paymentTimes) {
    JodaBeanUtils.notNull(paymentTimes, "paymentTimes");
    this._paymentTimes = paymentTimes;
  }

  /**
   * Gets the the {@code paymentTimes} property.
   * @return the property, not null
   */
  public final Property<double[]> paymentTimes() {
    return metaBean().paymentTimes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment amounts. May contain nulls if there have been no fixings as of the valuation date.
   * @return the value of the property, not null
   */
  public CurrencyAmount[] getPaymentAmounts() {
    return _paymentAmounts;
  }

  /**
   * Sets an array of payment amounts. May contain nulls if there have been no fixings as of the valuation date.
   * @param paymentAmounts  the new value of the property, not null
   */
  public void setPaymentAmounts(CurrencyAmount[] paymentAmounts) {
    JodaBeanUtils.notNull(paymentAmounts, "paymentAmounts");
    this._paymentAmounts = paymentAmounts;
  }

  /**
   * Gets the the {@code paymentAmounts} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> paymentAmounts() {
    return metaBean().paymentAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of notionals.
   * @return the value of the property, not null
   */
  public CurrencyAmount[] getNotionals() {
    return _notionals;
  }

  /**
   * Sets an array of notionals.
   * @param notionals  the new value of the property, not null
   */
  public void setNotionals(CurrencyAmount[] notionals) {
    JodaBeanUtils.notNull(notionals, "notionals");
    this._notionals = notionals;
  }

  /**
   * Gets the the {@code notionals} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> notionals() {
    return metaBean().notionals().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of spreads.
   * @return the value of the property, not null
   */
  public double[] getSpreads() {
    return _spreads;
  }

  /**
   * Sets an array of spreads.
   * @param spreads  the new value of the property, not null
   */
  public void setSpreads(double[] spreads) {
    JodaBeanUtils.notNull(spreads, "spreads");
    this._spreads = spreads;
  }

  /**
   * Gets the the {@code spreads} property.
   * @return the property, not null
   */
  public final Property<double[]> spreads() {
    return metaBean().spreads().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of gearings.
   * @return the value of the property, not null
   */
  public double[] getGearings() {
    return _gearings;
  }

  /**
   * Sets an array of gearings.
   * @param gearings  the new value of the property, not null
   */
  public void setGearings(double[] gearings) {
    JodaBeanUtils.notNull(gearings, "gearings");
    this._gearings = gearings;
  }

  /**
   * Gets the the {@code gearings} property.
   * @return the property, not null
   */
  public final Property<double[]> gearings() {
    return metaBean().gearings().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment discount factors.
   * @return the value of the property, not null
   */
  public double[] getPaymentDiscountFactors() {
    return _paymentDiscountFactors;
  }

  /**
   * Sets an array of payment discount factors.
   * @param paymentDiscountFactors  the new value of the property, not null
   */
  public void setPaymentDiscountFactors(double[] paymentDiscountFactors) {
    JodaBeanUtils.notNull(paymentDiscountFactors, "paymentDiscountFactors");
    this._paymentDiscountFactors = paymentDiscountFactors;
  }

  /**
   * Gets the the {@code paymentDiscountFactors} property.
   * @return the property, not null
   */
  public final Property<double[]> paymentDiscountFactors() {
    return metaBean().paymentDiscountFactors().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of projected amounts. May contain nulls if there has been a fixing as of the valuation date.
   * @return the value of the property, not null
   */
  public CurrencyAmount[] getProjectedAmounts() {
    return _projectedAmounts;
  }

  /**
   * Sets an array of projected amounts. May contain nulls if there has been a fixing as of the valuation date.
   * @param projectedAmounts  the new value of the property, not null
   */
  public void setProjectedAmounts(CurrencyAmount[] projectedAmounts) {
    JodaBeanUtils.notNull(projectedAmounts, "projectedAmounts");
    this._projectedAmounts = projectedAmounts;
  }

  /**
   * Gets the the {@code projectedAmounts} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> projectedAmounts() {
    return metaBean().projectedAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of index tenors. May contain nulls if there has been a fixing as of the valuation date.
   * @return the value of the property, not null
   */
  public Tenor[] getIndexTenors() {
    return _indexTenors;
  }

  /**
   * Sets an array of index tenors. May contain nulls if there has been a fixing as of the valuation date.
   * @param indexTenors  the new value of the property, not null
   */
  public void setIndexTenors(Tenor[] indexTenors) {
    JodaBeanUtils.notNull(indexTenors, "indexTenors");
    this._indexTenors = indexTenors;
  }

  /**
   * Gets the the {@code indexTenors} property.
   * @return the property, not null
   */
  public final Property<Tenor[]> indexTenors() {
    return metaBean().indexTenors().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code numberOfCashFlows} property.
   * @return the property, not null
   */
  public final Property<Integer> numberOfCashFlows() {
    return metaBean().numberOfCashFlows().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code numberOfFixedCashFlows} property.
   * @return the property, not null
   */
  public final Property<Integer> numberOfFixedCashFlows() {
    return metaBean().numberOfFixedCashFlows().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code numberOfFloatingCashFlows} property.
   * @return the property, not null
   */
  public final Property<Integer> numberOfFloatingCashFlows() {
    return metaBean().numberOfFloatingCashFlows().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code discountedPaymentAmounts} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> discountedPaymentAmounts() {
    return metaBean().discountedPaymentAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code discountedProjectedAmounts} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> discountedProjectedAmounts() {
    return metaBean().discountedProjectedAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FloatingSwapLegDetails clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FloatingSwapLegDetails other = (FloatingSwapLegDetails) obj;
      return JodaBeanUtils.equal(getAccrualStart(), other.getAccrualStart()) &&
          JodaBeanUtils.equal(getAccrualEnd(), other.getAccrualEnd()) &&
          JodaBeanUtils.equal(getAccrualYearFractions(), other.getAccrualYearFractions()) &&
          JodaBeanUtils.equal(getFixingStart(), other.getFixingStart()) &&
          JodaBeanUtils.equal(getFixingEnd(), other.getFixingEnd()) &&
          JodaBeanUtils.equal(getFixingYearFractions(), other.getFixingYearFractions()) &&
          JodaBeanUtils.equal(getForwardRates(), other.getForwardRates()) &&
          JodaBeanUtils.equal(getFixedRates(), other.getFixedRates()) &&
          JodaBeanUtils.equal(getPaymentDates(), other.getPaymentDates()) &&
          JodaBeanUtils.equal(getPaymentTimes(), other.getPaymentTimes()) &&
          JodaBeanUtils.equal(getPaymentAmounts(), other.getPaymentAmounts()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getSpreads(), other.getSpreads()) &&
          JodaBeanUtils.equal(getGearings(), other.getGearings()) &&
          JodaBeanUtils.equal(getPaymentDiscountFactors(), other.getPaymentDiscountFactors()) &&
          JodaBeanUtils.equal(getProjectedAmounts(), other.getProjectedAmounts()) &&
          JodaBeanUtils.equal(getIndexTenors(), other.getIndexTenors()) &&
          (getNumberOfCashFlows() == other.getNumberOfCashFlows()) &&
          (getNumberOfFixedCashFlows() == other.getNumberOfFixedCashFlows()) &&
          (getNumberOfFloatingCashFlows() == other.getNumberOfFloatingCashFlows()) &&
          JodaBeanUtils.equal(getDiscountedPaymentAmounts(), other.getDiscountedPaymentAmounts()) &&
          JodaBeanUtils.equal(getDiscountedProjectedAmounts(), other.getDiscountedProjectedAmounts());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualEnd());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualYearFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingEnd());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingYearFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getForwardRates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixedRates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentTimes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSpreads());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGearings());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDiscountFactors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProjectedAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIndexTenors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNumberOfCashFlows());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNumberOfFixedCashFlows());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNumberOfFloatingCashFlows());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDiscountedPaymentAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDiscountedProjectedAmounts());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(736);
    buf.append("FloatingSwapLegDetails{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("accrualStart").append('=').append(JodaBeanUtils.toString(getAccrualStart())).append(',').append(' ');
    buf.append("accrualEnd").append('=').append(JodaBeanUtils.toString(getAccrualEnd())).append(',').append(' ');
    buf.append("accrualYearFractions").append('=').append(JodaBeanUtils.toString(getAccrualYearFractions())).append(',').append(' ');
    buf.append("fixingStart").append('=').append(JodaBeanUtils.toString(getFixingStart())).append(',').append(' ');
    buf.append("fixingEnd").append('=').append(JodaBeanUtils.toString(getFixingEnd())).append(',').append(' ');
    buf.append("fixingYearFractions").append('=').append(JodaBeanUtils.toString(getFixingYearFractions())).append(',').append(' ');
    buf.append("forwardRates").append('=').append(JodaBeanUtils.toString(getForwardRates())).append(',').append(' ');
    buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(getFixedRates())).append(',').append(' ');
    buf.append("paymentDates").append('=').append(JodaBeanUtils.toString(getPaymentDates())).append(',').append(' ');
    buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(getPaymentTimes())).append(',').append(' ');
    buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(getPaymentAmounts())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("spreads").append('=').append(JodaBeanUtils.toString(getSpreads())).append(',').append(' ');
    buf.append("gearings").append('=').append(JodaBeanUtils.toString(getGearings())).append(',').append(' ');
    buf.append("paymentDiscountFactors").append('=').append(JodaBeanUtils.toString(getPaymentDiscountFactors())).append(',').append(' ');
    buf.append("projectedAmounts").append('=').append(JodaBeanUtils.toString(getProjectedAmounts())).append(',').append(' ');
    buf.append("indexTenors").append('=').append(JodaBeanUtils.toString(getIndexTenors())).append(',').append(' ');
    buf.append("numberOfCashFlows").append('=').append(JodaBeanUtils.toString(getNumberOfCashFlows())).append(',').append(' ');
    buf.append("numberOfFixedCashFlows").append('=').append(JodaBeanUtils.toString(getNumberOfFixedCashFlows())).append(',').append(' ');
    buf.append("numberOfFloatingCashFlows").append('=').append(JodaBeanUtils.toString(getNumberOfFloatingCashFlows())).append(',').append(' ');
    buf.append("discountedPaymentAmounts").append('=').append(JodaBeanUtils.toString(getDiscountedPaymentAmounts())).append(',').append(' ');
    buf.append("discountedProjectedAmounts").append('=').append(JodaBeanUtils.toString(getDiscountedProjectedAmounts())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FloatingSwapLegDetails}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code accrualStart} property.
     */
    private final MetaProperty<LocalDate[]> _accrualStart = DirectMetaProperty.ofReadWrite(
        this, "accrualStart", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code accrualEnd} property.
     */
    private final MetaProperty<LocalDate[]> _accrualEnd = DirectMetaProperty.ofReadWrite(
        this, "accrualEnd", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code accrualYearFractions} property.
     */
    private final MetaProperty<double[]> _accrualYearFractions = DirectMetaProperty.ofReadWrite(
        this, "accrualYearFractions", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code fixingStart} property.
     */
    private final MetaProperty<LocalDate[]> _fixingStart = DirectMetaProperty.ofReadWrite(
        this, "fixingStart", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code fixingEnd} property.
     */
    private final MetaProperty<LocalDate[]> _fixingEnd = DirectMetaProperty.ofReadWrite(
        this, "fixingEnd", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code fixingYearFractions} property.
     */
    private final MetaProperty<Double[]> _fixingYearFractions = DirectMetaProperty.ofReadWrite(
        this, "fixingYearFractions", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code forwardRates} property.
     */
    private final MetaProperty<Double[]> _forwardRates = DirectMetaProperty.ofReadWrite(
        this, "forwardRates", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code fixedRates} property.
     */
    private final MetaProperty<Double[]> _fixedRates = DirectMetaProperty.ofReadWrite(
        this, "fixedRates", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code paymentDates} property.
     */
    private final MetaProperty<LocalDate[]> _paymentDates = DirectMetaProperty.ofReadWrite(
        this, "paymentDates", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code paymentTimes} property.
     */
    private final MetaProperty<double[]> _paymentTimes = DirectMetaProperty.ofReadWrite(
        this, "paymentTimes", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code paymentAmounts} property.
     */
    private final MetaProperty<CurrencyAmount[]> _paymentAmounts = DirectMetaProperty.ofReadWrite(
        this, "paymentAmounts", FloatingSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    private final MetaProperty<CurrencyAmount[]> _notionals = DirectMetaProperty.ofReadWrite(
        this, "notionals", FloatingSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code spreads} property.
     */
    private final MetaProperty<double[]> _spreads = DirectMetaProperty.ofReadWrite(
        this, "spreads", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code gearings} property.
     */
    private final MetaProperty<double[]> _gearings = DirectMetaProperty.ofReadWrite(
        this, "gearings", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code paymentDiscountFactors} property.
     */
    private final MetaProperty<double[]> _paymentDiscountFactors = DirectMetaProperty.ofReadWrite(
        this, "paymentDiscountFactors", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code projectedAmounts} property.
     */
    private final MetaProperty<CurrencyAmount[]> _projectedAmounts = DirectMetaProperty.ofReadWrite(
        this, "projectedAmounts", FloatingSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code indexTenors} property.
     */
    private final MetaProperty<Tenor[]> _indexTenors = DirectMetaProperty.ofReadWrite(
        this, "indexTenors", FloatingSwapLegDetails.class, Tenor[].class);
    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfCashFlows", FloatingSwapLegDetails.class, Integer.TYPE);
    /**
     * The meta-property for the {@code numberOfFixedCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfFixedCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfFixedCashFlows", FloatingSwapLegDetails.class, Integer.TYPE);
    /**
     * The meta-property for the {@code numberOfFloatingCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfFloatingCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfFloatingCashFlows", FloatingSwapLegDetails.class, Integer.TYPE);
    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     */
    private final MetaProperty<CurrencyAmount[]> _discountedPaymentAmounts = DirectMetaProperty.ofDerived(
        this, "discountedPaymentAmounts", FloatingSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code discountedProjectedAmounts} property.
     */
    private final MetaProperty<CurrencyAmount[]> _discountedProjectedAmounts = DirectMetaProperty.ofDerived(
        this, "discountedProjectedAmounts", FloatingSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "accrualStart",
        "accrualEnd",
        "accrualYearFractions",
        "fixingStart",
        "fixingEnd",
        "fixingYearFractions",
        "forwardRates",
        "fixedRates",
        "paymentDates",
        "paymentTimes",
        "paymentAmounts",
        "notionals",
        "spreads",
        "gearings",
        "paymentDiscountFactors",
        "projectedAmounts",
        "indexTenors",
        "numberOfCashFlows",
        "numberOfFixedCashFlows",
        "numberOfFloatingCashFlows",
        "discountedPaymentAmounts",
        "discountedProjectedAmounts");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return _accrualStart;
        case 1846909100:  // accrualEnd
          return _accrualEnd;
        case 1516259717:  // accrualYearFractions
          return _accrualYearFractions;
        case 270958773:  // fixingStart
          return _fixingStart;
        case 871775726:  // fixingEnd
          return _fixingEnd;
        case 309118023:  // fixingYearFractions
          return _fixingYearFractions;
        case -291258418:  // forwardRates
          return _forwardRates;
        case 1695350911:  // fixedRates
          return _fixedRates;
        case -522438625:  // paymentDates
          return _paymentDates;
        case -507430688:  // paymentTimes
          return _paymentTimes;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case -1996407456:  // spreads
          return _spreads;
        case 1449942752:  // gearings
          return _gearings;
        case -650014307:  // paymentDiscountFactors
          return _paymentDiscountFactors;
        case -176306557:  // projectedAmounts
          return _projectedAmounts;
        case 1358155045:  // indexTenors
          return _indexTenors;
        case -338982286:  // numberOfCashFlows
          return _numberOfCashFlows;
        case -857546850:  // numberOfFixedCashFlows
          return _numberOfFixedCashFlows;
        case -582457076:  // numberOfFloatingCashFlows
          return _numberOfFloatingCashFlows;
        case 178231285:  // discountedPaymentAmounts
          return _discountedPaymentAmounts;
        case 2019754051:  // discountedProjectedAmounts
          return _discountedProjectedAmounts;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FloatingSwapLegDetails> builder() {
      return new DirectBeanBuilder<FloatingSwapLegDetails>(new FloatingSwapLegDetails());
    }

    @Override
    public Class<? extends FloatingSwapLegDetails> beanType() {
      return FloatingSwapLegDetails.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code accrualStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> accrualStart() {
      return _accrualStart;
    }

    /**
     * The meta-property for the {@code accrualEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> accrualEnd() {
      return _accrualEnd;
    }

    /**
     * The meta-property for the {@code accrualYearFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> accrualYearFractions() {
      return _accrualYearFractions;
    }

    /**
     * The meta-property for the {@code fixingStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> fixingStart() {
      return _fixingStart;
    }

    /**
     * The meta-property for the {@code fixingEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> fixingEnd() {
      return _fixingEnd;
    }

    /**
     * The meta-property for the {@code fixingYearFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> fixingYearFractions() {
      return _fixingYearFractions;
    }

    /**
     * The meta-property for the {@code forwardRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> forwardRates() {
      return _forwardRates;
    }

    /**
     * The meta-property for the {@code fixedRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> fixedRates() {
      return _fixedRates;
    }

    /**
     * The meta-property for the {@code paymentDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> paymentDates() {
      return _paymentDates;
    }

    /**
     * The meta-property for the {@code paymentTimes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> paymentTimes() {
      return _paymentTimes;
    }

    /**
     * The meta-property for the {@code paymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> paymentAmounts() {
      return _paymentAmounts;
    }

    /**
     * The meta-property for the {@code notionals} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> notionals() {
      return _notionals;
    }

    /**
     * The meta-property for the {@code spreads} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> spreads() {
      return _spreads;
    }

    /**
     * The meta-property for the {@code gearings} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> gearings() {
      return _gearings;
    }

    /**
     * The meta-property for the {@code paymentDiscountFactors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> paymentDiscountFactors() {
      return _paymentDiscountFactors;
    }

    /**
     * The meta-property for the {@code projectedAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> projectedAmounts() {
      return _projectedAmounts;
    }

    /**
     * The meta-property for the {@code indexTenors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor[]> indexTenors() {
      return _indexTenors;
    }

    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numberOfCashFlows() {
      return _numberOfCashFlows;
    }

    /**
     * The meta-property for the {@code numberOfFixedCashFlows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numberOfFixedCashFlows() {
      return _numberOfFixedCashFlows;
    }

    /**
     * The meta-property for the {@code numberOfFloatingCashFlows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numberOfFloatingCashFlows() {
      return _numberOfFloatingCashFlows;
    }

    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> discountedPaymentAmounts() {
      return _discountedPaymentAmounts;
    }

    /**
     * The meta-property for the {@code discountedProjectedAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> discountedProjectedAmounts() {
      return _discountedProjectedAmounts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return ((FloatingSwapLegDetails) bean).getAccrualStart();
        case 1846909100:  // accrualEnd
          return ((FloatingSwapLegDetails) bean).getAccrualEnd();
        case 1516259717:  // accrualYearFractions
          return ((FloatingSwapLegDetails) bean).getAccrualYearFractions();
        case 270958773:  // fixingStart
          return ((FloatingSwapLegDetails) bean).getFixingStart();
        case 871775726:  // fixingEnd
          return ((FloatingSwapLegDetails) bean).getFixingEnd();
        case 309118023:  // fixingYearFractions
          return ((FloatingSwapLegDetails) bean).getFixingYearFractions();
        case -291258418:  // forwardRates
          return ((FloatingSwapLegDetails) bean).getForwardRates();
        case 1695350911:  // fixedRates
          return ((FloatingSwapLegDetails) bean).getFixedRates();
        case -522438625:  // paymentDates
          return ((FloatingSwapLegDetails) bean).getPaymentDates();
        case -507430688:  // paymentTimes
          return ((FloatingSwapLegDetails) bean).getPaymentTimes();
        case -1875448267:  // paymentAmounts
          return ((FloatingSwapLegDetails) bean).getPaymentAmounts();
        case 1910080819:  // notionals
          return ((FloatingSwapLegDetails) bean).getNotionals();
        case -1996407456:  // spreads
          return ((FloatingSwapLegDetails) bean).getSpreads();
        case 1449942752:  // gearings
          return ((FloatingSwapLegDetails) bean).getGearings();
        case -650014307:  // paymentDiscountFactors
          return ((FloatingSwapLegDetails) bean).getPaymentDiscountFactors();
        case -176306557:  // projectedAmounts
          return ((FloatingSwapLegDetails) bean).getProjectedAmounts();
        case 1358155045:  // indexTenors
          return ((FloatingSwapLegDetails) bean).getIndexTenors();
        case -338982286:  // numberOfCashFlows
          return ((FloatingSwapLegDetails) bean).getNumberOfCashFlows();
        case -857546850:  // numberOfFixedCashFlows
          return ((FloatingSwapLegDetails) bean).getNumberOfFixedCashFlows();
        case -582457076:  // numberOfFloatingCashFlows
          return ((FloatingSwapLegDetails) bean).getNumberOfFloatingCashFlows();
        case 178231285:  // discountedPaymentAmounts
          return ((FloatingSwapLegDetails) bean).getDiscountedPaymentAmounts();
        case 2019754051:  // discountedProjectedAmounts
          return ((FloatingSwapLegDetails) bean).getDiscountedProjectedAmounts();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          ((FloatingSwapLegDetails) bean).setAccrualStart((LocalDate[]) newValue);
          return;
        case 1846909100:  // accrualEnd
          ((FloatingSwapLegDetails) bean).setAccrualEnd((LocalDate[]) newValue);
          return;
        case 1516259717:  // accrualYearFractions
          ((FloatingSwapLegDetails) bean).setAccrualYearFractions((double[]) newValue);
          return;
        case 270958773:  // fixingStart
          ((FloatingSwapLegDetails) bean).setFixingStart((LocalDate[]) newValue);
          return;
        case 871775726:  // fixingEnd
          ((FloatingSwapLegDetails) bean).setFixingEnd((LocalDate[]) newValue);
          return;
        case 309118023:  // fixingYearFractions
          ((FloatingSwapLegDetails) bean).setFixingYearFractions((Double[]) newValue);
          return;
        case -291258418:  // forwardRates
          ((FloatingSwapLegDetails) bean).setForwardRates((Double[]) newValue);
          return;
        case 1695350911:  // fixedRates
          ((FloatingSwapLegDetails) bean).setFixedRates((Double[]) newValue);
          return;
        case -522438625:  // paymentDates
          ((FloatingSwapLegDetails) bean).setPaymentDates((LocalDate[]) newValue);
          return;
        case -507430688:  // paymentTimes
          ((FloatingSwapLegDetails) bean).setPaymentTimes((double[]) newValue);
          return;
        case -1875448267:  // paymentAmounts
          ((FloatingSwapLegDetails) bean).setPaymentAmounts((CurrencyAmount[]) newValue);
          return;
        case 1910080819:  // notionals
          ((FloatingSwapLegDetails) bean).setNotionals((CurrencyAmount[]) newValue);
          return;
        case -1996407456:  // spreads
          ((FloatingSwapLegDetails) bean).setSpreads((double[]) newValue);
          return;
        case 1449942752:  // gearings
          ((FloatingSwapLegDetails) bean).setGearings((double[]) newValue);
          return;
        case -650014307:  // paymentDiscountFactors
          ((FloatingSwapLegDetails) bean).setPaymentDiscountFactors((double[]) newValue);
          return;
        case -176306557:  // projectedAmounts
          ((FloatingSwapLegDetails) bean).setProjectedAmounts((CurrencyAmount[]) newValue);
          return;
        case 1358155045:  // indexTenors
          ((FloatingSwapLegDetails) bean).setIndexTenors((Tenor[]) newValue);
          return;
        case -338982286:  // numberOfCashFlows
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: numberOfCashFlows");
        case -857546850:  // numberOfFixedCashFlows
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: numberOfFixedCashFlows");
        case -582457076:  // numberOfFloatingCashFlows
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: numberOfFloatingCashFlows");
        case 178231285:  // discountedPaymentAmounts
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: discountedPaymentAmounts");
        case 2019754051:  // discountedProjectedAmounts
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: discountedProjectedAmounts");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._accrualStart, "accrualStart");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._accrualEnd, "accrualEnd");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._accrualYearFractions, "accrualYearFractions");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixingStart, "fixingStart");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixingEnd, "fixingEnd");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixingYearFractions, "fixingYearFractions");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._forwardRates, "forwardRates");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixedRates, "fixedRates");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentDates, "paymentDates");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentTimes, "paymentTimes");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentAmounts, "paymentAmounts");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._notionals, "notionals");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._spreads, "spreads");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._gearings, "gearings");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentDiscountFactors, "paymentDiscountFactors");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._projectedAmounts, "projectedAmounts");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._indexTenors, "indexTenors");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
