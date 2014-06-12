/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Notional that can handle a schedule. Currency must be constant throughout.
 * Expects to be handed a list of dates and the notional (absolute or delta) taking effect in that period.
 * Can be passed a single amount for a constant notional.
 * Dates provided may be business day adjusted during analytics calculations.
 */
@BeanDefinition
public final class InterestRateSwapNotional extends InterestRateNotional {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The dates for which custom notionals take effect.
   */
  @PropertyDefinition
  private List<LocalDate> _dates;
  /**
   * The custom notionals. Possible this should be delta's on top of first notional.
   */
  @PropertyDefinition
  private List<Double> _notionals;
  /**
   * Controls if the custom notionals are delta on the original or absolute values.
   */
  @PropertyDefinition
  private List<Rate.ShiftType> _shiftTypes;

  //@Override
  public <T> T accept(InterestRateSwapNotionalVisitor<LocalDate, T> visitor, LocalDate period) {
    return visitor.visitInterestRateSwapNotional(this, period);
  }

  //@Override
  public <T> T accept(InterestRateSwapNotionalVisitor<Object , T> visitor) {
    return visitor.visitInterestRateSwapNotional(this);
  }

  /**
   * Return the notional amount.
   *
   * @deprecated getInitialAmount() should be used in preference to this.
   * @return the initial notional.
   */
  @Deprecated
  @Override
  public double getAmount() {
    return getInitialAmount();
  }

  /**
   * Return the initial notional amount.
   *
   * @return the initial notional.
   */
  public double getInitialAmount() {
    return super.getAmount();
  }

  /**
  * Get the notional as of a given date
  *
  * @param date the (business day adjusted) date you want the notional for.
  * @return the notional
  */
  public double getAmount(final LocalDate date) {
    if (getDates().size() == 0 || date.isBefore(getDates().get(0))) {  // constant notional or before schedule begins
      return super.getAmount();
    }
    final int index = Collections.binarySearch(_dates, date);
    if (index >= 0) {
      if (_shiftTypes.get(index) == Rate.ShiftType.OUTRIGHT) {
        return _notionals.get(index); // short circuit if we don't need to adjust from previous
      }
      // Recurse back until it hits an outright amount (the initial notional is outright)
      final int previousIndex = index - 1;
      double previousValue;
      if (previousIndex < 0) {
        previousValue = getInitialAmount();
      } else {
        previousValue = getAmount(getDates().get(previousIndex));
      }
      return _shiftTypes.get(index).getRate(previousValue, getNotionals().get(index));
    }
    // if value not explicitly set for this date, take from last notional before this date.
    return getAmount(getDates().get(-(index + 2)));
  }

  /**
   * Create a variable notional schedule.
   *
   * @param ccy the currency
   * @param dates the dates the provided values take effect (unadjusted for business days)
   * @param notionals the notional values (or shifts to the previous notional) that take effect
   * @param types the shift types for each step in the schedule
   * @return the notional schedule
   */
  public static InterestRateSwapNotional of(Currency ccy, final List<LocalDate> dates, final List<Double> notionals, List<Rate.ShiftType> types) {
    ArgumentChecker.noNulls(dates, "dates");
    ArgumentChecker.noNulls(notionals, "notionals");
    ArgumentChecker.noNulls(types, "types");
    ArgumentChecker.isTrue(dates.size() == notionals.size(), "Different numbers of overrides & notionals");
    ArgumentChecker.isTrue(dates.size() == types.size(), "Different numbers of overrides & notionals");
    ArgumentChecker.isTrue(notionals.size() > 0, "Require at least one notional");
    if (notionals.size() == 1) {      // constant notional
      return new InterestRateSwapNotional(ccy, notionals.get(0));
    }
    ArgumentChecker.isTrue(types.get(0) == Rate.ShiftType.OUTRIGHT, "First notional in schedule must be an OUTRIGHT quote");
    return new InterestRateSwapNotional(ccy, dates, notionals, types);
  }

  /**
   * Create a variable notional schedule.
   *
   * @param ccy the currency
   * @param dates the dates the provided values take effect (unadjusted for business days)
   * @param notionals the notional values that take effect
   * @return the notional schedule
   */
  public static InterestRateSwapNotional of(Currency ccy, final List<LocalDate> dates, final List<Double> notionals) {
    List<Rate.ShiftType> types = Lists.newArrayListWithExpectedSize(notionals.size());
    for (int i = 0; i < dates.size(); i++) {
      types.add(Rate.ShiftType.OUTRIGHT);
    }
    return of(ccy, dates, notionals, types);
  }

  /**
   * Create a constant notional
   *
   * @param ccy the currency
   * @param notional the notional value
   * @return the constant notional
   */
  public static InterestRateSwapNotional of(Currency ccy, final double notional) {
    return new InterestRateSwapNotional(ccy, notional);
  }

  private InterestRateSwapNotional(Currency ccy, List<LocalDate> overridePeriods, List<Double> notionals, List<Rate.ShiftType> types) {
    super(ccy, ArgumentChecker.notEmpty(notionals, "notionals").iterator().next());
    ArgumentChecker.isTrue(overridePeriods.size() == notionals.size(), "Different overrides & notionals");
    ArgumentChecker.isTrue(overridePeriods.size() == types.size(), "Different overrides & adjustment types");
    _dates = Lists.newArrayList(overridePeriods);
    _notionals = notionals;
    _shiftTypes = types;
  }

  /**
   * Create a constant notional
   *
   * @param ccy the currency
   * @param notional the notional value
   */
  public InterestRateSwapNotional(final Currency ccy, final double notional) {
    super(ccy, notional);
    _dates = Collections.emptyList();
    _notionals = Collections.emptyList();
    _shiftTypes = Collections.emptyList();
  }

  protected InterestRateSwapNotional() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code InterestRateSwapNotional}.
   * @return the meta-bean, not null
   */
  public static InterestRateSwapNotional.Meta meta() {
    return InterestRateSwapNotional.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(InterestRateSwapNotional.Meta.INSTANCE);
  }

  @Override
  public InterestRateSwapNotional.Meta metaBean() {
    return InterestRateSwapNotional.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the dates for which custom notionals take effect.
   * @return the value of the property
   */
  public List<LocalDate> getDates() {
    return _dates;
  }

  /**
   * Sets the dates for which custom notionals take effect.
   * @param dates  the new value of the property
   */
  public void setDates(List<LocalDate> dates) {
    this._dates = dates;
  }

  /**
   * Gets the the {@code dates} property.
   * @return the property, not null
   */
  public Property<List<LocalDate>> dates() {
    return metaBean().dates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the custom notionals. Possible this should be delta's on top of first notional.
   * @return the value of the property
   */
  public List<Double> getNotionals() {
    return _notionals;
  }

  /**
   * Sets the custom notionals. Possible this should be delta's on top of first notional.
   * @param notionals  the new value of the property
   */
  public void setNotionals(List<Double> notionals) {
    this._notionals = notionals;
  }

  /**
   * Gets the the {@code notionals} property.
   * @return the property, not null
   */
  public Property<List<Double>> notionals() {
    return metaBean().notionals().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets controls if the custom notionals are delta on the original or absolute values.
   * @return the value of the property
   */
  public List<Rate.ShiftType> getShiftTypes() {
    return _shiftTypes;
  }

  /**
   * Sets controls if the custom notionals are delta on the original or absolute values.
   * @param shiftTypes  the new value of the property
   */
  public void setShiftTypes(List<Rate.ShiftType> shiftTypes) {
    this._shiftTypes = shiftTypes;
  }

  /**
   * Gets the the {@code shiftTypes} property.
   * @return the property, not null
   */
  public Property<List<Rate.ShiftType>> shiftTypes() {
    return metaBean().shiftTypes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public InterestRateSwapNotional clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      InterestRateSwapNotional other = (InterestRateSwapNotional) obj;
      return JodaBeanUtils.equal(getDates(), other.getDates()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getShiftTypes(), other.getShiftTypes()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash += hash * 31 + JodaBeanUtils.hashCode(getShiftTypes());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("InterestRateSwapNotional{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("dates").append('=').append(JodaBeanUtils.toString(getDates())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("shiftTypes").append('=').append(JodaBeanUtils.toString(getShiftTypes())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code InterestRateSwapNotional}.
   */
  public static final class Meta extends InterestRateNotional.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _dates = DirectMetaProperty.ofReadWrite(
        this, "dates", InterestRateSwapNotional.class, (Class) List.class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _notionals = DirectMetaProperty.ofReadWrite(
        this, "notionals", InterestRateSwapNotional.class, (Class) List.class);
    /**
     * The meta-property for the {@code shiftTypes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Rate.ShiftType>> _shiftTypes = DirectMetaProperty.ofReadWrite(
        this, "shiftTypes", InterestRateSwapNotional.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "dates",
        "notionals",
        "shiftTypes");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 95356549:  // dates
          return _dates;
        case 1910080819:  // notionals
          return _notionals;
        case 1923906839:  // shiftTypes
          return _shiftTypes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends InterestRateSwapNotional> builder() {
      return new DirectBeanBuilder<InterestRateSwapNotional>(new InterestRateSwapNotional());
    }

    @Override
    public Class<? extends InterestRateSwapNotional> beanType() {
      return InterestRateSwapNotional.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dates} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<LocalDate>> dates() {
      return _dates;
    }

    /**
     * The meta-property for the {@code notionals} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<Double>> notionals() {
      return _notionals;
    }

    /**
     * The meta-property for the {@code shiftTypes} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<Rate.ShiftType>> shiftTypes() {
      return _shiftTypes;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 95356549:  // dates
          return ((InterestRateSwapNotional) bean).getDates();
        case 1910080819:  // notionals
          return ((InterestRateSwapNotional) bean).getNotionals();
        case 1923906839:  // shiftTypes
          return ((InterestRateSwapNotional) bean).getShiftTypes();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 95356549:  // dates
          ((InterestRateSwapNotional) bean).setDates((List<LocalDate>) newValue);
          return;
        case 1910080819:  // notionals
          ((InterestRateSwapNotional) bean).setNotionals((List<Double>) newValue);
          return;
        case 1923906839:  // shiftTypes
          ((InterestRateSwapNotional) bean).setShiftTypes((List<Rate.ShiftType>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
