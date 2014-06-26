/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.util.ArgumentChecker;

/**
 * A range of dates.
 * <p>
 * This holds a range of dates.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class LocalDateRange implements ImmutableBean, Serializable {

  /**
   * A range over the whole time-line.
   */
  public static final LocalDateRange ALL = LocalDateRange.of(LocalDate.MIN, LocalDate.MAX, true);

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The start date, inclusive.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final LocalDate _startDateInclusive;
  /**
   * The end date, inclusive.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final LocalDate _endDateInclusive;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param startDateInclusive  the start date, MIN_DATE treated as unbounded, not null
   * @param endDate  the end date, MAX_DATE treated as unbounded, not null
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the range, not null
   */
  public static LocalDateRange of(LocalDate startDateInclusive, LocalDate endDate, boolean endDateInclusive) {
    ArgumentChecker.notNull(startDateInclusive, "startDate");
    ArgumentChecker.notNull(endDate, "endDate");
    if (endDateInclusive == false && endDate.isBefore(LocalDate.MAX)) {
      endDate = endDate.minusDays(1);
    }
    if (endDate.isBefore(startDateInclusive)) {
      throw new IllegalArgumentException("Start date must be on or after end date");
    }
    return new LocalDateRange(startDateInclusive, endDate);
  }

  /**
   * Creates an instance treating nulls as unbounded.
   * <p>
   * The null value is stored as {@code MIN_DATE} or {@code MAX_DATE} internally,
   * thus there is no special behavior for unbounded.
   * 
   * @param startDateInclusive  the start date, null means unbounded MIN_DATE
   * @param endDate  the end date, null means unbounded MAX_DATE
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the range, not null
   */
  public static LocalDateRange ofNullUnbounded(LocalDate startDateInclusive, LocalDate endDate, boolean endDateInclusive) {
    startDateInclusive = (startDateInclusive != null ? startDateInclusive : LocalDate.MIN);
    endDate = (endDate != null ? endDate : LocalDate.MAX);
    return of(startDateInclusive, endDate, endDateInclusive);
  }

  /**
   * Creates an instance.
   * 
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   */
  @ImmutableConstructor
  private LocalDateRange(LocalDate startDate, LocalDate endDate) {
    _startDateInclusive = startDate;
    _endDateInclusive = endDate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the start date, inclusive.
   * 
   * @return the start date, not null
   */
  public LocalDate getStartDateInclusive() {
    return _startDateInclusive;
  }

  /**
   * Gets the end date, inclusive.
   * 
   * @return the end date, not null
   */
  public LocalDate getEndDateInclusive() {
    return _endDateInclusive;
  }

  /**
   * Gets the end date, exclusive.
   * <p>
   * If the end date (inclusive) is {@code MAX_DATE}, then {@code MAX_DATE} is returned.
   * 
   * @return the end date, not null
   */
  public LocalDate getEndDateExclusive() {
    if (isEndDateMaximum()) {
      return _endDateInclusive;
    }
    return _endDateInclusive.plusDays(1);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the start date is the minimum, typically signalling unbounded.
   * 
   * @return true if maximum
   */
  public boolean isStartDateMinimum() {
    return _startDateInclusive.equals(LocalDate.MIN);
  }

  /**
   * Checks if the end date is the maximum, typically signalling unbounded.
   * 
   * @return true if maximum
   */
  public boolean isEndDateMaximum() {
    return _endDateInclusive.equals(LocalDate.MAX);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this range with the start date adjusted.
   * 
   * @param adjuster  the adjuster to use, not null
   * @return the new range, not null
   */
  public LocalDateRange withStartDate(TemporalAdjuster adjuster) {
    return new LocalDateRange(_startDateInclusive.with(adjuster), _endDateInclusive);
  }

  /**
   * Returns a copy of this range with the end date adjusted.
   * 
   * @param adjuster  the adjuster to use, not null
   * @return the new range, not null
   */
  public LocalDateRange withEndDate(TemporalAdjuster adjuster) {
    return new LocalDateRange(_startDateInclusive, _endDateInclusive.with(adjuster));
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this range with the start date adjusted if it is unbounded.
   * 
   * @param startDateInclusive  the start date to use if currently MIN_DATE, not null
   * @return the new range, not null
   */
  public LocalDateRange resolveUnboundedStartDate(LocalDate startDateInclusive) {
    return isStartDateMinimum() ? LocalDateRange.of(startDateInclusive, _endDateInclusive, true) : this;
  }

  /**
   * Returns a copy of this range with the end date adjusted if it is unbounded.
   * 
   * @param endDate  the end date to use if currently MAX_DATE, not null
   * @param endDateInclusive  whether the end date is inclusive (true) or exclusive (false)
   * @return the new range, not null
   */
  public LocalDateRange resolveUnboundedEndDate(LocalDate endDate, boolean endDateInclusive) {
    return isEndDateMaximum() ? LocalDateRange.of(_startDateInclusive, endDate, endDateInclusive) : this;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "[" + _startDateInclusive + "," + _endDateInclusive + "]";
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LocalDateRange}.
   * @return the meta-bean, not null
   */
  public static LocalDateRange.Meta meta() {
    return LocalDateRange.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LocalDateRange.Meta.INSTANCE);
  }

  @Override
  public LocalDateRange.Meta metaBean() {
    return LocalDateRange.Meta.INSTANCE;
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
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LocalDateRange other = (LocalDateRange) obj;
      return JodaBeanUtils.equal(getStartDateInclusive(), other.getStartDateInclusive()) &&
          JodaBeanUtils.equal(getEndDateInclusive(), other.getEndDateInclusive());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartDateInclusive());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEndDateInclusive());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LocalDateRange}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code startDateInclusive} property.
     */
    private final MetaProperty<LocalDate> _startDateInclusive = DirectMetaProperty.ofImmutable(
        this, "startDateInclusive", LocalDateRange.class, LocalDate.class);
    /**
     * The meta-property for the {@code endDateInclusive} property.
     */
    private final MetaProperty<LocalDate> _endDateInclusive = DirectMetaProperty.ofImmutable(
        this, "endDateInclusive", LocalDateRange.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "startDateInclusive",
        "endDateInclusive");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -353187684:  // startDateInclusive
          return _startDateInclusive;
        case 292475075:  // endDateInclusive
          return _endDateInclusive;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public LocalDateRange.Builder builder() {
      return new LocalDateRange.Builder();
    }

    @Override
    public Class<? extends LocalDateRange> beanType() {
      return LocalDateRange.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code startDateInclusive} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> startDateInclusive() {
      return _startDateInclusive;
    }

    /**
     * The meta-property for the {@code endDateInclusive} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> endDateInclusive() {
      return _endDateInclusive;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -353187684:  // startDateInclusive
          return ((LocalDateRange) bean).getStartDateInclusive();
        case 292475075:  // endDateInclusive
          return ((LocalDateRange) bean).getEndDateInclusive();
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
   * The bean-builder for {@code LocalDateRange}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<LocalDateRange> {

    private LocalDate _startDateInclusive;
    private LocalDate _endDateInclusive;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -353187684:  // startDateInclusive
          return _startDateInclusive;
        case 292475075:  // endDateInclusive
          return _endDateInclusive;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -353187684:  // startDateInclusive
          this._startDateInclusive = (LocalDate) newValue;
          break;
        case 292475075:  // endDateInclusive
          this._endDateInclusive = (LocalDate) newValue;
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
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public LocalDateRange build() {
      return new LocalDateRange(
          _startDateInclusive,
          _endDateInclusive);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("LocalDateRange.Builder{");
      buf.append("startDateInclusive").append('=').append(JodaBeanUtils.toString(_startDateInclusive)).append(',').append(' ');
      buf.append("endDateInclusive").append('=').append(JodaBeanUtils.toString(_endDateInclusive));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
