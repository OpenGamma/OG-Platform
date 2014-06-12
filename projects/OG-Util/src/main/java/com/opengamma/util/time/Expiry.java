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
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * An indication of when something expires.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class Expiry implements ImmutableBean, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The expiry date-time.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final ZonedDateTime _expiry;
  /**
   * The accuracy of the expiry.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final ExpiryAccuracy _accuracy;

  //-------------------------------------------------------------------------
  /**
   * Creates an expiry with no specific accuracy.
   * 
   * @param expiry  the expiry date-time
   */
  public Expiry(final ZonedDateTime expiry) {
    this(expiry, ExpiryAccuracy.DAY_MONTH_YEAR);
  }

  /**
   * Creates an expiry with an accuracy.
   * 
   * @param expiry  the expiry date-time, not-null
   * @param accuracy  the accuracy
   */
  @ImmutableConstructor
  public Expiry(final ZonedDateTime expiry, final ExpiryAccuracy accuracy) {
    ArgumentChecker.notNull(expiry, "expiry");
    ArgumentChecker.notNull(accuracy, "accuracy");
    _expiry = expiry;
    _accuracy = accuracy;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the expiry date-time.
   * 
   * @return the date-time
   */
  public ZonedDateTime getExpiry() {
    return _expiry;
  }

  /**
   * Gets the accuracy of the expiry.
   * 
   * @return the accuracy
   */
  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }

  /**
   * Converts the expiry date-time to an instant.
   * 
   * @return the instant of the expiry, not null
   */
  public Instant toInstant() {
    return _expiry.toInstant();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Expiry)) {
      return false;
    }
    final Expiry other = (Expiry) obj;
    // Can't be the same if different accuracies encoded
    if (!getAccuracy().equals(other.getAccuracy())) {
      return false;
    }
    // Only compare to the accuracy agreed
    return equalsToAccuracy(getAccuracy(), getExpiry(), other.getExpiry());
  }

  /**
   * Compares two expiry dates for equality to the given level of accuracy only.
   *
   * @param accuracy  the accuracy to compare to, not null
   * @param expiry1  the first date/time to compare, not null
   * @param expiry2  the second date/time to compare, not null
   * @return true if the two dates/times are equal to the requested accuracy
   */
  public static boolean equalsToAccuracy(final ExpiryAccuracy accuracy, final ZonedDateTime expiry1, final ZonedDateTime expiry2) {
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return (expiry1.getMinute() == expiry2.getMinute()) && (expiry1.getHour() == expiry2.getHour()) && (expiry1.getDayOfMonth() == expiry2.getDayOfMonth())
            && (expiry1.getMonth() == expiry2.getMonth()) && (expiry1.getYear() == expiry2.getYear());
      case HOUR_DAY_MONTH_YEAR:
        return (expiry1.getHour() == expiry2.getHour()) && (expiry1.getDayOfMonth() == expiry2.getDayOfMonth()) && (expiry1.getMonth() == expiry2.getMonth())
            && (expiry1.getYear() == expiry2.getYear());
      case DAY_MONTH_YEAR:
        return (expiry1.getDayOfMonth() == expiry2.getDayOfMonth()) && (expiry1.getMonth() == expiry2.getMonth()) && (expiry1.getYear() == expiry2.getYear());
      case MONTH_YEAR:
        return (expiry1.getMonth() == expiry2.getMonth()) && (expiry1.getYear() == expiry2.getYear());
      case YEAR:
        return (expiry1.getYear() == expiry2.getYear());
      default:
        throw new IllegalArgumentException("accuracy");
    }
  }

  @Override
  public int hashCode() {
    return (_accuracy != null ? _accuracy.hashCode() : 0) ^ _expiry.hashCode();
  }

  @Override
  public String toString() {
    if (_accuracy != null) {
      return "Expiry[" + _expiry + " accuracy " + _accuracy + "]";
    } else {
      return "Expiry[" + _expiry + "]";
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Expiry}.
   * @return the meta-bean, not null
   */
  public static Expiry.Meta meta() {
    return Expiry.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Expiry.Meta.INSTANCE);
  }

  @Override
  public Expiry.Meta metaBean() {
    return Expiry.Meta.INSTANCE;
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
  public Expiry clone() {
    return this;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Expiry}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code expiry} property.
     */
    private final MetaProperty<ZonedDateTime> _expiry = DirectMetaProperty.ofImmutable(
        this, "expiry", Expiry.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code accuracy} property.
     */
    private final MetaProperty<ExpiryAccuracy> _accuracy = DirectMetaProperty.ofImmutable(
        this, "accuracy", Expiry.class, ExpiryAccuracy.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "expiry",
        "accuracy");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1289159373:  // expiry
          return _expiry;
        case -2131707655:  // accuracy
          return _accuracy;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public Expiry.Builder builder() {
      return new Expiry.Builder();
    }

    @Override
    public Class<? extends Expiry> beanType() {
      return Expiry.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code expiry} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ZonedDateTime> expiry() {
      return _expiry;
    }

    /**
     * The meta-property for the {@code accuracy} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExpiryAccuracy> accuracy() {
      return _accuracy;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1289159373:  // expiry
          return ((Expiry) bean).getExpiry();
        case -2131707655:  // accuracy
          return ((Expiry) bean).getAccuracy();
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
   * The bean-builder for {@code Expiry}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<Expiry> {

    private ZonedDateTime _expiry;
    private ExpiryAccuracy _accuracy;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1289159373:  // expiry
          return _expiry;
        case -2131707655:  // accuracy
          return _accuracy;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1289159373:  // expiry
          this._expiry = (ZonedDateTime) newValue;
          break;
        case -2131707655:  // accuracy
          this._accuracy = (ExpiryAccuracy) newValue;
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
    public Expiry build() {
      return new Expiry(
          _expiry,
          _accuracy);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("Expiry.Builder{");
      buf.append("expiry").append('=').append(JodaBeanUtils.toString(_expiry)).append(',').append(' ');
      buf.append("accuracy").append('=').append(JodaBeanUtils.toString(_accuracy));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
