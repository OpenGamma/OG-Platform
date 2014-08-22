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

/**
 * Container for the relevant details for pricing a fixed swap leg, with the entries
 * <ul>
 * <li>Start accrual date</li>
 * <li>End accrual date</li>
 * <li>Payment time</li>
 * <li>Payment date</li>
 * <li>Payment year fraction</li>
 * <li>Payment amount (non discounted)</li>
 * <li>Discount factor</li>
 * <li>Notional</li>
 * <li>Rate</li>
 * <li>Discounted payment amount</li>
 * <ul>
 * There is an entry for each coupon in a fixed leg.
 */
@BeanDefinition
public class FixedLegCashFlows implements ImmutableBean, SwapLegCashFlows {

  /**
   * The details of fixed cash flows.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<FixedCashFlowDetails> _cashFlowDetails;

  /**
   * All arrays must be the same length.
   * @param startAccrualDates The start accrual dates, not null
   * @param endAccrualDates The end accrual dates, not null
   * @param discountFactors The discount factors, not null
   * @param paymentTimes The payment times, not null
   * @param paymentDates The payment dates, not null
   * @param paymentFractions The payment year fractions, not null
   * @param paymentAmounts The payment amounts, not null
   * @param notionals The notionals, not null
   * @param fixedRates The fixed rates, not null
   */
  public FixedLegCashFlows(List<LocalDate> startAccrualDates,
                           List<LocalDate> endAccrualDates,
                           List<Double> discountFactors,
                           List<Double> paymentTimes,
                           List<LocalDate> paymentDates,
                           List<Double> paymentFractions,
                           List<CurrencyAmount> paymentAmounts,
                           List<CurrencyAmount> notionals,
                           List<Double> fixedRates) {

    ArgumentChecker.notNull(startAccrualDates, "startAccrualDates");
    ArgumentChecker.notNull(endAccrualDates, "endAccrualDates");
    ArgumentChecker.notNull(discountFactors, "discountFactors");
    ArgumentChecker.notNull(paymentTimes, "paymentTimes");
    ArgumentChecker.notNull(paymentDates, "paymentDates");
    ArgumentChecker.notNull(paymentFractions, "paymentFractions");
    ArgumentChecker.notNull(paymentAmounts, "paymentAmounts");
    ArgumentChecker.notNull(notionals, "notionals");
    ArgumentChecker.notNull(fixedRates, "fixedRates");
    
    int n = startAccrualDates.size();
    ArgumentChecker.isTrue(n == endAccrualDates.size(), "Must have same number of start and end accrual dates");
    ArgumentChecker.isTrue(n == discountFactors.size(), "Must have same number of start accrual dates and discount factors");
    ArgumentChecker.isTrue(n == paymentTimes.size(), "Must have same number of start accrual dates and payment times");
    ArgumentChecker.isTrue(n == paymentDates.size(), "Must have same number of start accrual dates and payment dates");
    ArgumentChecker.isTrue(n == paymentFractions.size(), "Must have same number of start accrual dates and payment year fractions");
    ArgumentChecker.isTrue(n == paymentAmounts.size(), "Must have same number of start accrual dates and payment amounts");
    ArgumentChecker.isTrue(n == notionals.size(), "Must have same number of start accrual dates and notionals");
    ArgumentChecker.isTrue(n == fixedRates.size(), "Must have same number of start accrual dates and fixed rates");
    
    List<FixedCashFlowDetails> cashFlows = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      AbstractCashFlowDetails.Builder builder = FixedCashFlowDetails.builder()
          .rate(fixedRates.get(i))
          .projectedAmount(paymentAmounts.get(i))
          .presentValue(paymentAmounts.get(i).multipliedBy(discountFactors.get(i)))
          .accrualStartDate(startAccrualDates.get(i))
          .accrualEndDate(endAccrualDates.get(i))
          .accrualFactor(paymentTimes.get(i))
          .paymentDate(paymentDates.get(i))
          .df(discountFactors.get(i))
          .notional(notionals.get(i));
      cashFlows.add((FixedCashFlowDetails) builder.build());
    }
    
    _cashFlowDetails = cashFlows;
  }
  
  /**
   * Returns the notional of the cash flow.
   * @return the notional of the cash flow.
   */
  public List<CurrencyAmount> getNotionals() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<CurrencyAmount> notionals = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      notionals.add(cashFlowDetails.get(i).getNotional());
    }
    return notionals;
  }

  /**
   * Returns the accrual start dates of the cash flow.
   * @return the accrual start dates of the cash flow.
   */
  public List<LocalDate> getAccrualStart() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
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
  public List<LocalDate> getAccrualEnd() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
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
  public List<Double> getPaymentFractions() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> accrualFactor = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      accrualFactor.add(cashFlowDetails.get(i).getAccrualFactor());
    }
    return accrualFactor;
  }

  /**
   * Returns the fixed rate of the cash flow.
   * @return the fixed rate of the cash flow.
   */
  public List<Double> getFixedRates() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> fixedRates = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      fixedRates.add(cashFlowDetails.get(i).getRate());
    }
    return fixedRates;
  }

  /**
   * Gets the discounted payment amounts.
   * @return the discounted cashflows
   */
  public List<CurrencyAmount> getDiscountedPaymentAmounts() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<CurrencyAmount> cashflows = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      cashflows.add(cashFlowDetails.get(i).getPresentValue());
    }
    return cashflows;
  }

  /**
   * Gets the total number of cash-flows.
   * @return The total number of cash-flows
   */
  public int getNumberOfCashFlows() {
    return getCashFlowDetails().size();
  }
  
  /**
   * Returns the discount factors used to discount the cash flows.
   * @return the discount factors used to discount the cash flows.
   */
  public List<Double> getDiscountFactors() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<Double> df = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      df.add(cashFlowDetails.get(i).getDf());
    }
    return df;
  }
  
  /**
   * Returns the payment amount, or projected amount of the cash flow.
   * @return the payment amount, or projected amount of the cash flow.
   */
  public List<CurrencyAmount> getPaymentAmounts() {
    List<FixedCashFlowDetails> cashFlowDetails = getCashFlowDetails();
    
    List<CurrencyAmount> paymentAmount = new ArrayList<>();
    for (int i = 0; i < cashFlowDetails.size(); i++) {
      paymentAmount.add(cashFlowDetails.get(i).getProjectedAmount());
    }
    return paymentAmount;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedLegCashFlows}.
   * @return the meta-bean, not null
   */
  public static FixedLegCashFlows.Meta meta() {
    return FixedLegCashFlows.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedLegCashFlows.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FixedLegCashFlows.Builder builder() {
    return new FixedLegCashFlows.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected FixedLegCashFlows(FixedLegCashFlows.Builder builder) {
    JodaBeanUtils.notNull(builder._cashFlowDetails, "cashFlowDetails");
    this._cashFlowDetails = ImmutableList.copyOf(builder._cashFlowDetails);
  }

  @Override
  public FixedLegCashFlows.Meta metaBean() {
    return FixedLegCashFlows.Meta.INSTANCE;
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
   * Gets the details of fixed cash flows.
   * @return the value of the property, not null
   */
  public List<FixedCashFlowDetails> getCashFlowDetails() {
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
      FixedLegCashFlows other = (FixedLegCashFlows) obj;
      return JodaBeanUtils.equal(getCashFlowDetails(), other.getCashFlowDetails());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCashFlowDetails());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("FixedLegCashFlows{");
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
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedLegCashFlows}.
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
    private final MetaProperty<List<FixedCashFlowDetails>> _cashFlowDetails = DirectMetaProperty.ofImmutable(
        this, "cashFlowDetails", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "cashFlowDetails");

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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FixedLegCashFlows.Builder builder() {
      return new FixedLegCashFlows.Builder();
    }

    @Override
    public Class<? extends FixedLegCashFlows> beanType() {
      return FixedLegCashFlows.class;
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
    public final MetaProperty<List<FixedCashFlowDetails>> cashFlowDetails() {
      return _cashFlowDetails;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1294419967:  // cashFlowDetails
          return ((FixedLegCashFlows) bean).getCashFlowDetails();
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
   * The bean-builder for {@code FixedLegCashFlows}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FixedLegCashFlows> {

    private List<FixedCashFlowDetails> _cashFlowDetails = new ArrayList<FixedCashFlowDetails>();

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FixedLegCashFlows beanToCopy) {
      this._cashFlowDetails = new ArrayList<FixedCashFlowDetails>(beanToCopy.getCashFlowDetails());
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
          this._cashFlowDetails = (List<FixedCashFlowDetails>) newValue;
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
    public FixedLegCashFlows build() {
      return new FixedLegCashFlows(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code cashFlowDetails} property in the builder.
     * @param cashFlowDetails  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder cashFlowDetails(List<FixedCashFlowDetails> cashFlowDetails) {
      JodaBeanUtils.notNull(cashFlowDetails, "cashFlowDetails");
      this._cashFlowDetails = cashFlowDetails;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("FixedLegCashFlows.Builder{");
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
