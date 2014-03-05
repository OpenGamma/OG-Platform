/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantDateCurve.checkAndGetTimes;

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

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;

/**
 * An ISDA compliant date credit curve.
 */
@BeanDefinition
public class ISDACompliantDateCreditCurve
    extends ISDACompliantCreditCurve
    implements ISDACompliantCurveWithDates {

  /**
   * The standard ACT/365 day count.
   */
  private static final DayCount ACT_365 = DayCounts.ACT_365;

  /**
   * The base date.
   */
  @PropertyDefinition(set = "private")
  private LocalDate _baseDate;
  /**
   * The knot dates on the curve.
   */
  @PropertyDefinition(get = "private", set = "private")
  private LocalDate[] _dates;
  /**
   * The day count.
   */
  @PropertyDefinition(get = "private", set = "private")
  private DayCount _dayCount;

  /**
   * Constructor for Joda-Beans.
   */
  protected ISDACompliantDateCreditCurve() {
  }

  /**
   * Builds a credit curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates.
   * The times (year-fractions) between the baseDate and the knot dates is calculated using ACT/365.
   * 
   * @param baseDate  the base date for the curve (i.e. this is time zero), not null
   * @param dates  the knot dates on the curve. These must be ascending with the first date after the baseDate, not null
   * @param rates  the continually compounded zero hazard rates at given knot dates, not null
   */
  public ISDACompliantDateCreditCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a credit curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates.
   * The times (year-fractions) between the baseDate and the knot dates is calculated using the specified day-count-convention.
   * 
   * @param baseDate  the base date for the curve (i.e. this is time zero), not null
   * @param dates  the knot dates on the curve. These must be ascending with the first date after the baseDate, not null
   * @param rates  the continually compounded zero hazard rates at given knot dates, not null
   * @param dayCount  the day-count-convention, not null
   */
  public ISDACompliantDateCreditCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    super(checkAndGetTimes(baseDate, dates, rates, dayCount), rates);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  private ISDACompliantDateCreditCurve(final LocalDate baseDate, final LocalDate[] dates, final DayCount dayCount, final ISDACompliantCreditCurve baseCurve) {
    super(baseCurve);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  //  /**
  //  * Converter from the old HazardRateCurve to ISDACompliantDateCreditCurve. Not this only works if offset = 0.0.
  //  * @param hazardCurve a HazardRateCurve hazard curve
  //  * @return A ISDACompliantDateCreditCurve
  //  */
  //  public static ISDACompliantDateCreditCurve fromHazardRateCurve(final HazardRateCurve hazardCurve) {
  //
  //    ArgumentChecker.isTrue(hazardCurve.getOffset() == 0, "offset not zero - cannot convert");
  //    final LocalDate[] dates = ISDACompliantScheduleGenerator.toLocalDate(hazardCurve.getCurveTenors());
  //    final double[] t = hazardCurve.getTimes();
  //    ISDACompliantCreditCurve temp = new ISDACompliantCreditCurve(t, hazardCurve.getRates());
  //
  //    // back out the missing baseDate (assuming ACT/365 was used)
  //    int days = (int) Math.round(365 * t[0]);
  //    LocalDate baseDate = dates[0].minusDays(days);
  //
  //    return new ISDACompliantDateCreditCurve(baseDate, dates, ACT_365, temp);
  //  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  @Override
  public LocalDate[] getCurveDates() {
    return _dates.clone();
  }

  @Override
  public ISDACompliantDateCreditCurve withRate(final double rate, final int index) {
    final ISDACompliantCreditCurve temp = super.withRate(rate, index);
    return new ISDACompliantDateCreditCurve(_baseDate, _dates, _dayCount, temp);
  }

  @Override
  public double getZeroRate(final LocalDate date) {
    final double t = _dayCount.getDayCountFraction(_baseDate, date);
    return getZeroRate(t);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ISDACompliantDateCreditCurve}.
   * @return the meta-bean, not null
   */
  public static ISDACompliantDateCreditCurve.Meta meta() {
    return ISDACompliantDateCreditCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ISDACompliantDateCreditCurve.Meta.INSTANCE);
  }

  @Override
  public ISDACompliantDateCreditCurve.Meta metaBean() {
    return ISDACompliantDateCreditCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base date.
   * @return the value of the property
   */
  public LocalDate getBaseDate() {
    return _baseDate;
  }

  /**
   * Sets the base date.
   * @param baseDate  the new value of the property
   */
  private void setBaseDate(LocalDate baseDate) {
    this._baseDate = baseDate;
  }

  /**
   * Gets the the {@code baseDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> baseDate() {
    return metaBean().baseDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the knot dates on the curve.
   * @return the value of the property
   */
  private LocalDate[] getDates() {
    return _dates;
  }

  /**
   * Sets the knot dates on the curve.
   * @param dates  the new value of the property
   */
  private void setDates(LocalDate[] dates) {
    this._dates = dates;
  }

  /**
   * Gets the the {@code dates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> dates() {
    return metaBean().dates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count.
   * @return the value of the property
   */
  private DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count.
   * @param dayCount  the new value of the property
   */
  private void setDayCount(DayCount dayCount) {
    this._dayCount = dayCount;
  }

  /**
   * Gets the the {@code dayCount} property.
   * @return the property, not null
   */
  public final Property<DayCount> dayCount() {
    return metaBean().dayCount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDACompliantDateCreditCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ISDACompliantDateCreditCurve other = (ISDACompliantDateCreditCurve) obj;
      return JodaBeanUtils.equal(getBaseDate(), other.getBaseDate()) &&
          JodaBeanUtils.equal(getDates(), other.getDates()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getBaseDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ISDACompliantDateCreditCurve{");
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
    buf.append("baseDate").append('=').append(JodaBeanUtils.toString(getBaseDate())).append(',').append(' ');
    buf.append("dates").append('=').append(JodaBeanUtils.toString(getDates())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ISDACompliantDateCreditCurve}.
   */
  public static class Meta extends ISDACompliantCreditCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseDate} property.
     */
    private final MetaProperty<LocalDate> _baseDate = DirectMetaProperty.ofReadWrite(
        this, "baseDate", ISDACompliantDateCreditCurve.class, LocalDate.class);
    /**
     * The meta-property for the {@code dates} property.
     */
    private final MetaProperty<LocalDate[]> _dates = DirectMetaProperty.ofReadWrite(
        this, "dates", ISDACompliantDateCreditCurve.class, LocalDate[].class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", ISDACompliantDateCreditCurve.class, DayCount.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "baseDate",
        "dates",
        "dayCount");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          return _baseDate;
        case 95356549:  // dates
          return _dates;
        case 1905311443:  // dayCount
          return _dayCount;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ISDACompliantDateCreditCurve> builder() {
      return new DirectBeanBuilder<ISDACompliantDateCreditCurve>(new ISDACompliantDateCreditCurve());
    }

    @Override
    public Class<? extends ISDACompliantDateCreditCurve> beanType() {
      return ISDACompliantDateCreditCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> baseDate() {
      return _baseDate;
    }

    /**
     * The meta-property for the {@code dates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> dates() {
      return _dates;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          return ((ISDACompliantDateCreditCurve) bean).getBaseDate();
        case 95356549:  // dates
          return ((ISDACompliantDateCreditCurve) bean).getDates();
        case 1905311443:  // dayCount
          return ((ISDACompliantDateCreditCurve) bean).getDayCount();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          ((ISDACompliantDateCreditCurve) bean).setBaseDate((LocalDate) newValue);
          return;
        case 95356549:  // dates
          ((ISDACompliantDateCreditCurve) bean).setDates((LocalDate[]) newValue);
          return;
        case 1905311443:  // dayCount
          ((ISDACompliantDateCreditCurve) bean).setDayCount((DayCount) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
