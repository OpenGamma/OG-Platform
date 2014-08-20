/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;

/**
 * Container for the relevant details for pricing a floating swap leg, with the entries
 * <ul>
 * <li>accrualStartDates</li>
 * <li>accrualEndDates</li>
 * <li>accrualYearFractions</li>
 * <li>fixingStart</li>
 * <li>fixingEnd</li>
 * <li>fixingYearFractions</li>
 * <li>forwardRates</li>
 * <li>fixedRates</li>
 * <li>paymentDates</li>
 * <li>paymentTimes</li>
 * <li>paymentDiscountFactors</li>
 * <li>paymentAmounts</li>
 * <li>projectedAmounts</li>
 * <li>notionals</li>
 * <li>spreads</li>
 * <li>gearings</li>
 * <li>indexTenors</li>
 * </ul>
 */
@BeanDefinition
public class FloatingLegCashFlows implements ImmutableBean, SwapLegCashFlows {

  @PropertyDefinition(validate = "notNull")
  private final List<FloatingCashFlowDetails> _cashFlowDetails;

  /**
   * @param startAccrualDates The start accrual dates, not null
   * @param endAccrualDates The end accrual dates, not null
   * @param accrualYearFractions The accrual year fractions, not null
   * @param fixingStart The fixing start dates, not null
   * @param fixingEnd The fixing end dates, not null
   * @param fixingYearFractions The fixing year fractions, not null
   * @param forwardRates The forward rates, not null
   * @param fixedRates The fixed rates, not null
   * @param paymentDates The payment dates, not null
   * @param paymentTimes The payment times, not null
   * @param paymentDiscountFactors The payment discount factors, not null
   * @param projectedAmounts The projected amounts, not null
   * @param notionals The notionals, not null
   * @param spreads The spreads, not null
   * @param gearings The gearings, not null
   * @param indexTenors The index tenors, not null
   */
  public FloatingLegCashFlows(List<LocalDate> startAccrualDates,
                              List<LocalDate> endAccrualDates,
                              List<Double> accrualYearFractions,
                              List<LocalDate> fixingStart,
                              List<LocalDate> fixingEnd,
                              List<Double> fixingYearFractions,
                              List<Double> forwardRates,
                              List<Double> fixedRates,
                              List<LocalDate> paymentDates,
                              List<Double> paymentTimes,
                              List<Double> paymentDiscountFactors,
                              List<CurrencyAmount> projectedAmounts,
                              List<CurrencyAmount> notionals,
                              List<Double> spreads,
                              List<Double> gearings,
                              List<Tenor> indexTenors) {

    ArgumentChecker.notNull(startAccrualDates, "startAccrualDates");
    ArgumentChecker.notNull(endAccrualDates, "endAccrualDates");
    ArgumentChecker.notNull(accrualYearFractions, "accrualYearFractions");
    ArgumentChecker.notNull(fixingStart, "fixingStart");
    ArgumentChecker.notNull(fixingEnd, "fixingEnd");
    ArgumentChecker.notNull(fixingYearFractions, "fixingYearFractions");
    ArgumentChecker.notNull(forwardRates, "forwardRates");
    ArgumentChecker.notNull(fixedRates, "fixedRates");
    ArgumentChecker.notNull(paymentDates, "paymentDates");
    ArgumentChecker.notNull(paymentTimes, "paymentTimes");
    ArgumentChecker.notNull(paymentDiscountFactors, "paymentDiscountFactors");
    ArgumentChecker.notNull(projectedAmounts, "projectedAmounts");
    ArgumentChecker.notNull(notionals, "notionals");
    ArgumentChecker.notNull(spreads, "spreads");
    ArgumentChecker.notNull(gearings, "gearings");
    ArgumentChecker.notNull(indexTenors, "indexTenors");

    int n = notionals.size();
    ArgumentChecker.isTrue(n == startAccrualDates.size(), "number of accrual start dates must equal number of notionals");
    ArgumentChecker.isTrue(n == endAccrualDates.size(), "number of accrual end dates must equal number of notionals");
    ArgumentChecker.isTrue(n == accrualYearFractions.size(), "number of accrual year fractions must equal number of notionals");
    ArgumentChecker.isTrue(n == fixingStart.size(), "number of fixing start dates must equal number of notionals");
    ArgumentChecker.isTrue(n == fixingEnd.size(), "number of fixing end dates must equal number of notionals");
    ArgumentChecker.isTrue(n == fixingYearFractions.size(), "number of fixing year fractions must equal number of notionals");
    ArgumentChecker.isTrue(n == forwardRates.size(), "number of forward rates must equal number of notionals");
    ArgumentChecker.isTrue(n == fixedRates.size(), "number of fixed rates must equal number of notionals");
    ArgumentChecker.isTrue(n == paymentDates.size(), "number of payment dates must equal number of notionals");
    ArgumentChecker.isTrue(n == paymentDiscountFactors.size(), "number of payment discount factors must equal number of notionals");
    ArgumentChecker.isTrue(n == projectedAmounts.size(), "number of projected amounts must equal number of notionals");
    ArgumentChecker.isTrue(n == spreads.size(), "number of spreads must equal number of notionals");
    ArgumentChecker.isTrue(n == gearings.size(), "number of gearings must equal number of notionals");
    ArgumentChecker.isTrue(n == indexTenors.size(), "number of index tenors must equal number of notionals");
    
    List<FloatingCashFlowDetails> cashFlows = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      AbstractCashFlowDetails.Builder builder = FloatingCashFlowDetails.builder()
          .fixingStartDate(fixingStart.get(i))
          .fixingEndDate(fixingEnd.get(i))
          .fixingYearFrac(fixingYearFractions.get(i))
          .forwardRate(forwardRates.get(i))
          .spread(spreads.get(i))
          .gearing(gearings.get(i))
          .projectedAmount(projectedAmounts.get(i))
          .presentValue(projectedAmounts.get(i).multipliedBy(paymentDiscountFactors.get(i)))
          .accrualStartDate(startAccrualDates.get(i))
          .accrualEndDate(endAccrualDates.get(i))
          .accrualFactor(paymentTimes.get(i))
          .paymentDate(paymentDates.get(i))
          .df(paymentDiscountFactors.get(i))
          .notional(notionals.get(i));
      cashFlows.add((FloatingCashFlowDetails) builder.build());
    }
    
    _cashFlowDetails = cashFlows;
  }

  /**
   * Gets the number of cash-flows.
   * @return the number of cash-flows
   */
  @DerivedProperty
  public int getNumberOfCashFlows() {
    return _cashFlowDetails.size();
  }
  
  /**
   * Gets the discounted payment amounts.
   * @return the discounted cashflows
   */
  @DerivedProperty
  public List<CurrencyAmount> getPaymentAmounts() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<CurrencyAmount> cashflows = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      cashflows.add(cashFlowDetails.get(i).getProjectedAmount());
    }
    return cashflows;
  }

  /**
   * Gets the discounted payment amounts.
   * @return the discounted cashflows
   */
  @DerivedProperty
  public List<CurrencyAmount> getDiscountedPaymentAmounts() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<CurrencyAmount> cashflows = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      cashflows.add(cashFlowDetails.get(i).getPresentValue());
    }
    return cashflows;
  }

  /**
   * Returns the notionals of the cash flows.
   * @return the notionals of the cash flows.
   */
  @DerivedProperty
  public List<CurrencyAmount> getNotionals() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();

    List<CurrencyAmount> cashflows = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      cashflows.add(cashFlowDetails.get(i).getNotional());
    }
    return cashflows;
  }

  /**
   * Returns the accrual start dates of the cash flow.
   * @return the accrual start dates of the cash flow.
   */
  @DerivedProperty
  public List<LocalDate> getAccrualStart() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<LocalDate> accrualStart = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      accrualStart.add(cashFlowDetails.get(i).getAccrualStartDate());
    }
    return accrualStart;
  }

  /**
   * Returns the accrual end dates of the cash flow.
   * @return the accrual end dates of the cash flow.
   */
  @DerivedProperty
  public List<LocalDate> getAccrualEnd() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<LocalDate> accrualEnd = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      accrualEnd.add(cashFlowDetails.get(i).getAccrualEndDate());
    }
    return accrualEnd;
  }

  /**
   * Returns the payment fraction, or accrual factor, of the cash flow.
   * @return the payment fraction, or accrual factor, of the cash flow.
   */
  @DerivedProperty
  public List<Double> getAccrualYearFractions() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> accrualFactor = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      accrualFactor.add(cashFlowDetails.get(i).getAccrualFactor());
    }
    return accrualFactor;
  }

  /**
   * Returns the payment dates of the cash flow.
   * @return the payment dates of the cash flow.
   */
  @DerivedProperty
  public List<LocalDate> getPaymentDates() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<LocalDate> paymentDates = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      paymentDates.add(cashFlowDetails.get(i).getPaymentDate());
    }
    return paymentDates;
  }

  /**
   * Returns the fixed rate of the cash flow.
   * @return the fixed rate of the cash flow.
   */
  @DerivedProperty
  public List<Double> getFixedRates() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> fixedRates = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      fixedRates.add(cashFlowDetails.get(i).getFixedRate());
    }
    return fixedRates;
  }
  
  /**
   * Returns the forward rate of the cash flow.
   * @return the forward rate of the cash flow.
   */
  @DerivedProperty
  public List<Double> getForwardRates() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> forwardRates = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      forwardRates.add(cashFlowDetails.get(i).getForwardRate());
    }
    return forwardRates;
  }
  
  /**
   * Returns the discount factors used to discount the cash flows.
   * @return the discount factors used to discount the cash flows.
   */
  @DerivedProperty
  public List<Double> getPaymentDiscountFactors() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> df = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      df.add(cashFlowDetails.get(i).getDf());
    }
    return df;
  }

  /**
   * Returns the fixing start dates of the cash flow.
   * @return the fixing start dates of the cash flow.
   */
  @DerivedProperty
  public List<LocalDate> getFixingStart() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<LocalDate> fixingStart = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      fixingStart.add(cashFlowDetails.get(i).getFixingStartDate());
    }
    return fixingStart;
  }

  /**
   * Returns the fixing end dates of the cash flow.
   * @return the fixing end dates of the cash flow.
   */
  @DerivedProperty
  public List<LocalDate> getFixingEnd() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<LocalDate> fixingEnd = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      fixingEnd.add(cashFlowDetails.get(i).getFixingEndDate());
    }
    return fixingEnd;
  }
  
  /**
   * Returns the fixing year fractions used to compute the forward rate.
   * @return the fixing year fractions used to compute the forward rate.
   */
  @DerivedProperty
  public List<Double> getFixingYearFractions() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> fixingYearFractions = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      fixingYearFractions.add(cashFlowDetails.get(i).getFixingYearFrac());
    }
    return fixingYearFractions;
  }

  /**
   * Returns the spreads of the cash flows.
   * @return the spreads of the cash flows.
   */
  @DerivedProperty
  public List<Double> getSpreads() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> spreads = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      spreads.add(cashFlowDetails.get(i).getSpread());
    }
    return spreads;
  }

  /**
   * Returns the gearing of the cash flows.
   * @return the gearing of the cash flows.
   */
  @DerivedProperty
  public List<Double> getGearings() {
    List<FloatingCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> gearings = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      gearings.add(cashFlowDetails.get(i).getGearing());
    }
    return gearings;
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FloatingLegCashFlows}.
   * @return the meta-bean, not null
   */
  public static FloatingLegCashFlows.Meta meta() {
    return FloatingLegCashFlows.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FloatingLegCashFlows.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FloatingLegCashFlows.Builder builder() {
    return new FloatingLegCashFlows.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected FloatingLegCashFlows(FloatingLegCashFlows.Builder builder) {
    JodaBeanUtils.notNull(builder._cashFlowDetails, "cashFlowDetails");
    this._cashFlowDetails = ImmutableList.copyOf(builder._cashFlowDetails);
  }

  @Override
  public FloatingLegCashFlows.Meta metaBean() {
    return FloatingLegCashFlows.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cashFlowDetails.
   * @return the value of the property, not null
   */
  public List<FloatingCashFlowDetails> getCashFlowDetails() {
    return _cashFlowDetails;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FloatingLegCashFlows other = (FloatingLegCashFlows) obj;
      return JodaBeanUtils.equal(getCashFlowDetails(), other.getCashFlowDetails()) &&
          (getNumberOfCashFlows() == other.getNumberOfCashFlows()) &&
          JodaBeanUtils.equal(getPaymentAmounts(), other.getPaymentAmounts()) &&
          JodaBeanUtils.equal(getDiscountedPaymentAmounts(), other.getDiscountedPaymentAmounts()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getAccrualStart(), other.getAccrualStart()) &&
          JodaBeanUtils.equal(getAccrualEnd(), other.getAccrualEnd()) &&
          JodaBeanUtils.equal(getAccrualYearFractions(), other.getAccrualYearFractions()) &&
          JodaBeanUtils.equal(getPaymentDates(), other.getPaymentDates()) &&
          JodaBeanUtils.equal(getFixedRates(), other.getFixedRates()) &&
          JodaBeanUtils.equal(getForwardRates(), other.getForwardRates()) &&
          JodaBeanUtils.equal(getPaymentDiscountFactors(), other.getPaymentDiscountFactors()) &&
          JodaBeanUtils.equal(getFixingStart(), other.getFixingStart()) &&
          JodaBeanUtils.equal(getFixingEnd(), other.getFixingEnd()) &&
          JodaBeanUtils.equal(getFixingYearFractions(), other.getFixingYearFractions()) &&
          JodaBeanUtils.equal(getSpreads(), other.getSpreads()) &&
          JodaBeanUtils.equal(getGearings(), other.getGearings());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCashFlowDetails());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNumberOfCashFlows());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDiscountedPaymentAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualEnd());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualYearFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixedRates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getForwardRates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDiscountFactors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingEnd());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingYearFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSpreads());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGearings());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(576);
    buf.append("FloatingLegCashFlows{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("cashFlowDetails").append('=').append(JodaBeanUtils.toString(getCashFlowDetails())).append(',').append(' ');
    buf.append("numberOfCashFlows").append('=').append(JodaBeanUtils.toString(getNumberOfCashFlows())).append(',').append(' ');
    buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(getPaymentAmounts())).append(',').append(' ');
    buf.append("discountedPaymentAmounts").append('=').append(JodaBeanUtils.toString(getDiscountedPaymentAmounts())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("accrualStart").append('=').append(JodaBeanUtils.toString(getAccrualStart())).append(',').append(' ');
    buf.append("accrualEnd").append('=').append(JodaBeanUtils.toString(getAccrualEnd())).append(',').append(' ');
    buf.append("accrualYearFractions").append('=').append(JodaBeanUtils.toString(getAccrualYearFractions())).append(',').append(' ');
    buf.append("paymentDates").append('=').append(JodaBeanUtils.toString(getPaymentDates())).append(',').append(' ');
    buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(getFixedRates())).append(',').append(' ');
    buf.append("forwardRates").append('=').append(JodaBeanUtils.toString(getForwardRates())).append(',').append(' ');
    buf.append("paymentDiscountFactors").append('=').append(JodaBeanUtils.toString(getPaymentDiscountFactors())).append(',').append(' ');
    buf.append("fixingStart").append('=').append(JodaBeanUtils.toString(getFixingStart())).append(',').append(' ');
    buf.append("fixingEnd").append('=').append(JodaBeanUtils.toString(getFixingEnd())).append(',').append(' ');
    buf.append("fixingYearFractions").append('=').append(JodaBeanUtils.toString(getFixingYearFractions())).append(',').append(' ');
    buf.append("spreads").append('=').append(JodaBeanUtils.toString(getSpreads())).append(',').append(' ');
    buf.append("gearings").append('=').append(JodaBeanUtils.toString(getGearings())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FloatingLegCashFlows}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code cashFlowDetails} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<FloatingCashFlowDetails>> _cashFlowDetails = DirectMetaProperty.ofImmutable(
        this, "cashFlowDetails", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfCashFlows", FloatingLegCashFlows.class, Integer.TYPE);
    /**
     * The meta-property for the {@code paymentAmounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _paymentAmounts = DirectMetaProperty.ofDerived(
        this, "paymentAmounts", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _discountedPaymentAmounts = DirectMetaProperty.ofDerived(
        this, "discountedPaymentAmounts", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _notionals = DirectMetaProperty.ofDerived(
        this, "notionals", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code accrualStart} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _accrualStart = DirectMetaProperty.ofDerived(
        this, "accrualStart", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code accrualEnd} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _accrualEnd = DirectMetaProperty.ofDerived(
        this, "accrualEnd", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code accrualYearFractions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _accrualYearFractions = DirectMetaProperty.ofDerived(
        this, "accrualYearFractions", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentDates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _paymentDates = DirectMetaProperty.ofDerived(
        this, "paymentDates", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code fixedRates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _fixedRates = DirectMetaProperty.ofDerived(
        this, "fixedRates", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code forwardRates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _forwardRates = DirectMetaProperty.ofDerived(
        this, "forwardRates", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentDiscountFactors} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _paymentDiscountFactors = DirectMetaProperty.ofDerived(
        this, "paymentDiscountFactors", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code fixingStart} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _fixingStart = DirectMetaProperty.ofDerived(
        this, "fixingStart", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code fixingEnd} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _fixingEnd = DirectMetaProperty.ofDerived(
        this, "fixingEnd", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code fixingYearFractions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _fixingYearFractions = DirectMetaProperty.ofDerived(
        this, "fixingYearFractions", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code spreads} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _spreads = DirectMetaProperty.ofDerived(
        this, "spreads", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code gearings} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _gearings = DirectMetaProperty.ofDerived(
        this, "gearings", FloatingLegCashFlows.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "cashFlowDetails",
        "numberOfCashFlows",
        "paymentAmounts",
        "discountedPaymentAmounts",
        "notionals",
        "accrualStart",
        "accrualEnd",
        "accrualYearFractions",
        "paymentDates",
        "fixedRates",
        "forwardRates",
        "paymentDiscountFactors",
        "fixingStart",
        "fixingEnd",
        "fixingYearFractions",
        "spreads",
        "gearings");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1294419967:  // cashFlowDetails
          return _cashFlowDetails;
        case -338982286:  // numberOfCashFlows
          return _numberOfCashFlows;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 178231285:  // discountedPaymentAmounts
          return _discountedPaymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case 1071260659:  // accrualStart
          return _accrualStart;
        case 1846909100:  // accrualEnd
          return _accrualEnd;
        case 1516259717:  // accrualYearFractions
          return _accrualYearFractions;
        case -522438625:  // paymentDates
          return _paymentDates;
        case 1695350911:  // fixedRates
          return _fixedRates;
        case -291258418:  // forwardRates
          return _forwardRates;
        case -650014307:  // paymentDiscountFactors
          return _paymentDiscountFactors;
        case 270958773:  // fixingStart
          return _fixingStart;
        case 871775726:  // fixingEnd
          return _fixingEnd;
        case 309118023:  // fixingYearFractions
          return _fixingYearFractions;
        case -1996407456:  // spreads
          return _spreads;
        case 1449942752:  // gearings
          return _gearings;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FloatingLegCashFlows.Builder builder() {
      return new FloatingLegCashFlows.Builder();
    }

    @Override
    public Class<? extends FloatingLegCashFlows> beanType() {
      return FloatingLegCashFlows.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code cashFlowDetails} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<FloatingCashFlowDetails>> cashFlowDetails() {
      return _cashFlowDetails;
    }

    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numberOfCashFlows() {
      return _numberOfCashFlows;
    }

    /**
     * The meta-property for the {@code paymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurrencyAmount>> paymentAmounts() {
      return _paymentAmounts;
    }

    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurrencyAmount>> discountedPaymentAmounts() {
      return _discountedPaymentAmounts;
    }

    /**
     * The meta-property for the {@code notionals} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurrencyAmount>> notionals() {
      return _notionals;
    }

    /**
     * The meta-property for the {@code accrualStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> accrualStart() {
      return _accrualStart;
    }

    /**
     * The meta-property for the {@code accrualEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> accrualEnd() {
      return _accrualEnd;
    }

    /**
     * The meta-property for the {@code accrualYearFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> accrualYearFractions() {
      return _accrualYearFractions;
    }

    /**
     * The meta-property for the {@code paymentDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> paymentDates() {
      return _paymentDates;
    }

    /**
     * The meta-property for the {@code fixedRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> fixedRates() {
      return _fixedRates;
    }

    /**
     * The meta-property for the {@code forwardRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> forwardRates() {
      return _forwardRates;
    }

    /**
     * The meta-property for the {@code paymentDiscountFactors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> paymentDiscountFactors() {
      return _paymentDiscountFactors;
    }

    /**
     * The meta-property for the {@code fixingStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> fixingStart() {
      return _fixingStart;
    }

    /**
     * The meta-property for the {@code fixingEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> fixingEnd() {
      return _fixingEnd;
    }

    /**
     * The meta-property for the {@code fixingYearFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> fixingYearFractions() {
      return _fixingYearFractions;
    }

    /**
     * The meta-property for the {@code spreads} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> spreads() {
      return _spreads;
    }

    /**
     * The meta-property for the {@code gearings} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> gearings() {
      return _gearings;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1294419967:  // cashFlowDetails
          return ((FloatingLegCashFlows) bean).getCashFlowDetails();
        case -338982286:  // numberOfCashFlows
          return ((FloatingLegCashFlows) bean).getNumberOfCashFlows();
        case -1875448267:  // paymentAmounts
          return ((FloatingLegCashFlows) bean).getPaymentAmounts();
        case 178231285:  // discountedPaymentAmounts
          return ((FloatingLegCashFlows) bean).getDiscountedPaymentAmounts();
        case 1910080819:  // notionals
          return ((FloatingLegCashFlows) bean).getNotionals();
        case 1071260659:  // accrualStart
          return ((FloatingLegCashFlows) bean).getAccrualStart();
        case 1846909100:  // accrualEnd
          return ((FloatingLegCashFlows) bean).getAccrualEnd();
        case 1516259717:  // accrualYearFractions
          return ((FloatingLegCashFlows) bean).getAccrualYearFractions();
        case -522438625:  // paymentDates
          return ((FloatingLegCashFlows) bean).getPaymentDates();
        case 1695350911:  // fixedRates
          return ((FloatingLegCashFlows) bean).getFixedRates();
        case -291258418:  // forwardRates
          return ((FloatingLegCashFlows) bean).getForwardRates();
        case -650014307:  // paymentDiscountFactors
          return ((FloatingLegCashFlows) bean).getPaymentDiscountFactors();
        case 270958773:  // fixingStart
          return ((FloatingLegCashFlows) bean).getFixingStart();
        case 871775726:  // fixingEnd
          return ((FloatingLegCashFlows) bean).getFixingEnd();
        case 309118023:  // fixingYearFractions
          return ((FloatingLegCashFlows) bean).getFixingYearFractions();
        case -1996407456:  // spreads
          return ((FloatingLegCashFlows) bean).getSpreads();
        case 1449942752:  // gearings
          return ((FloatingLegCashFlows) bean).getGearings();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FloatingLegCashFlows}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FloatingLegCashFlows> {

    private List<FloatingCashFlowDetails> _cashFlowDetails = new ArrayList<FloatingCashFlowDetails>();

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FloatingLegCashFlows beanToCopy) {
      this._cashFlowDetails = new ArrayList<FloatingCashFlowDetails>(beanToCopy.getCashFlowDetails());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1294419967:  // cashFlowDetails
          return _cashFlowDetails;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1294419967:  // cashFlowDetails
          this._cashFlowDetails = (List<FloatingCashFlowDetails>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FloatingLegCashFlows build() {
      return new FloatingLegCashFlows(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code cashFlowDetails} property in the builder.
     * @param cashFlowDetails  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder cashFlowDetails(List<FloatingCashFlowDetails> cashFlowDetails) {
      JodaBeanUtils.notNull(cashFlowDetails, "cashFlowDetails");
      this._cashFlowDetails = cashFlowDetails;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("FloatingLegCashFlows.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("cashFlowDetails").append('=').append(JodaBeanUtils.toString(_cashFlowDetails)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

