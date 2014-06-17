/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Description of the stub period handling for annuity definitions.
 */
@BeanDefinition
public final class StubCalculationMethod implements ImmutableBean {

  /**
   * The stub type.
   */
  @PropertyDefinition(validate = "notNull")
  private final StubType _type;

  /**
   * The first stub rate. Setting this will override any interpolation. This is an optional field.
   */
  @PropertyDefinition
  private final Double _firstStubRate;
  
  /**
   * The last stub rate. Setting this will override any interpolation. This is an optional field.
   */
  @PropertyDefinition
  private final Double _lastStubRate;

  /**
   * The date at which the first stub period ends and regular coupon periods begin. This is an optional field, unless stub type is BOTH.
   */
  @PropertyDefinition
  private final LocalDate _firstStubEndDate;
  
  /**
   * The date at which the regular coupon periods ends and last stub period begins. This is an optional field, unless stub type is BOTH.
   */
  @PropertyDefinition
  private final LocalDate _lastStubEndDate;

  /**
   *  The External Id which corresponds to the first index rate. This is an optional field.
   */
  @PropertyDefinition
  private final ExternalId _firstStubStartReferenceRateId;
  
  /**
   *  The External Id which corresponds to the end index rate for the first stub. This is an optional field.
   */
  @PropertyDefinition
  private final ExternalId _firstStubEndReferenceRateId;
  
  /**
   *  The External Id which corresponds to the start index rate for the last stub. This is an optional field.
   */
  @PropertyDefinition
  private final ExternalId _lastStubStartReferenceRateId;
  
  /**
   *  The External Id which corresponds to the end index rate for the last stub. This is an optional field.
   */
  @PropertyDefinition
  private final ExternalId _lastStubEndReferenceRateId;  
  
  /**
   * Returns whether the first stub rate has been set.
   * @return whether the first stub rate has been set.
   */
  public boolean hasFirstStubRate() {
    return _firstStubRate != null && !_firstStubRate.isNaN();
  }
  
  /**
   * Returns whether the last stub rate has been set.
   * @return whether the last stub rate has been set.
   */
  public boolean hasLastStubRate() {
    return _lastStubRate != null && !_lastStubRate.isNaN();
  }
  
  /**
   * Returns whether the first stub has a reference rate set for the start.
   * @return whether the first stub start reference rate id has been set.
   */
  public boolean hasFirstStubStartReferenceRateId() {
    return _firstStubStartReferenceRateId != null;
  }
  
  /**
   * Returns whether the first stub has a reference rate set for the end.
   * @return whether the first stub end reference rate id has been set.
   */
  public boolean hasFirstStubEndReferenceRateId() {
    return _firstStubEndReferenceRateId != null;
  }
  
  /**
   * Returns whether the last stub has a reference rate set for the start.
   * @return whether the last stub start reference rate id has been set.
   */
  public boolean hasLastStubStartReferenceRateId() {
    return _lastStubStartReferenceRateId != null;
  }
  
  /**
   * Returns whether the last stub has a reference rate set for the end.
   * @return whether the last stub end reference rate id has been set.
   */
  public boolean hasLastStubEndReferenceRateId() {
    return _lastStubEndReferenceRateId != null;
  }

  /**
   * Validate inputs.
   * 
   * @return this stub calculation method
   */
  public StubCalculationMethod validate() {
    switch (getType()) {
      case BOTH:
        ArgumentChecker.notNull(getFirstStubEndDate(), "Dual stub must have a first stub period end date");
        ArgumentChecker.notNull(getLastStubEndDate(), "Dual stub must have a last stub period end date");
        
        if (hasFirstStubStartReferenceRateId() && !hasFirstStubEndReferenceRateId()) {
          throw new OpenGammaRuntimeException("Dual stub has a first stub start reference rate identifier without a stub end reference rate");
        } else if (hasFirstStubEndReferenceRateId() && !hasFirstStubStartReferenceRateId()) {
          throw new OpenGammaRuntimeException("Dual stub has a first stub end reference rate identifier without a stub start reference rate");
        }
        
        if (hasLastStubStartReferenceRateId() && !hasLastStubEndReferenceRateId()) {
          throw new OpenGammaRuntimeException("Dual stub has a last stub start reference rate identifier without a stub end reference rate");
        } else if (hasLastStubEndReferenceRateId() && !hasLastStubStartReferenceRateId()) {
          throw new OpenGammaRuntimeException("Dual stub has a last stub end reference rate identifier without a stub start reference rate");
        }
        
        break;
        
      case SHORT_START:
        if (hasFirstStubStartReferenceRateId() && !hasFirstStubEndReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a first stub start reference rate identifier without a stub end reference rate");
        }
        if (hasFirstStubEndReferenceRateId() && !hasFirstStubStartReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a first stub end reference rate identifier without a stub start reference rate");
        }
        break;
        
      case LONG_START:
        if (hasFirstStubStartReferenceRateId() && !hasFirstStubEndReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a first stub start reference rate identifier without a stub end reference rate");
        }
        if (hasFirstStubEndReferenceRateId() && !hasFirstStubStartReferenceRateId()) {
          throw new OpenGammaRuntimeException("Dual stub has a first stub end reference rate identifier without a stub start reference rate");
        }
        break;
        
      case SHORT_END:
        if (hasLastStubStartReferenceRateId() && !hasLastStubEndReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a last stub start reference rate identifier without a stub end reference rate");
        }
        if (hasLastStubEndReferenceRateId() && !hasLastStubStartReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a last stub end reference rate identifier without a stub start reference rate");
        }
        break;
        
      case LONG_END:
        if (hasLastStubStartReferenceRateId() && !hasLastStubEndReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a last stub start reference rate identifier without a stub end reference rate");
        }
        if (hasLastStubEndReferenceRateId() && !hasLastStubStartReferenceRateId()) {
          throw new OpenGammaRuntimeException("Stub has a last stub end reference rate identifier without a stub start reference rate");
        }
        break;        
    }
    return this;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code StubCalculationMethod}.
   * @return the meta-bean, not null
   */
  public static StubCalculationMethod.Meta meta() {
    return StubCalculationMethod.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(StubCalculationMethod.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static StubCalculationMethod.Builder builder() {
    return new StubCalculationMethod.Builder();
  }

  private StubCalculationMethod(
      StubType type,
      Double firstStubRate,
      Double lastStubRate,
      LocalDate firstStubEndDate,
      LocalDate lastStubEndDate,
      ExternalId firstStubStartReferenceRateId,
      ExternalId firstStubEndReferenceRateId,
      ExternalId lastStubStartReferenceRateId,
      ExternalId lastStubEndReferenceRateId) {
    JodaBeanUtils.notNull(type, "type");
    this._type = type;
    this._firstStubRate = firstStubRate;
    this._lastStubRate = lastStubRate;
    this._firstStubEndDate = firstStubEndDate;
    this._lastStubEndDate = lastStubEndDate;
    this._firstStubStartReferenceRateId = firstStubStartReferenceRateId;
    this._firstStubEndReferenceRateId = firstStubEndReferenceRateId;
    this._lastStubStartReferenceRateId = lastStubStartReferenceRateId;
    this._lastStubEndReferenceRateId = lastStubEndReferenceRateId;
  }

  @Override
  public StubCalculationMethod.Meta metaBean() {
    return StubCalculationMethod.Meta.INSTANCE;
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
   * Gets the stub type.
   * @return the value of the property, not null
   */
  public StubType getType() {
    return _type;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first stub rate. Setting this will override any interpolation. This is an optional field.
   * @return the value of the property
   */
  public Double getFirstStubRate() {
    return _firstStubRate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last stub rate. Setting this will override any interpolation. This is an optional field.
   * @return the value of the property
   */
  public Double getLastStubRate() {
    return _lastStubRate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date at which the first stub period ends and regular coupon periods begin. This is an optional field, unless stub type is BOTH.
   * @return the value of the property
   */
  public LocalDate getFirstStubEndDate() {
    return _firstStubEndDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date at which the regular coupon periods ends and last stub period begins. This is an optional field, unless stub type is BOTH.
   * @return the value of the property
   */
  public LocalDate getLastStubEndDate() {
    return _lastStubEndDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the External Id which corresponds to the first index rate. This is an optional field.
   * @return the value of the property
   */
  public ExternalId getFirstStubStartReferenceRateId() {
    return _firstStubStartReferenceRateId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the External Id which corresponds to the end index rate for the first stub. This is an optional field.
   * @return the value of the property
   */
  public ExternalId getFirstStubEndReferenceRateId() {
    return _firstStubEndReferenceRateId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the External Id which corresponds to the start index rate for the last stub. This is an optional field.
   * @return the value of the property
   */
  public ExternalId getLastStubStartReferenceRateId() {
    return _lastStubStartReferenceRateId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the External Id which corresponds to the end index rate for the last stub. This is an optional field.
   * @return the value of the property
   */
  public ExternalId getLastStubEndReferenceRateId() {
    return _lastStubEndReferenceRateId;
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
  public StubCalculationMethod clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      StubCalculationMethod other = (StubCalculationMethod) obj;
      return JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getFirstStubRate(), other.getFirstStubRate()) &&
          JodaBeanUtils.equal(getLastStubRate(), other.getLastStubRate()) &&
          JodaBeanUtils.equal(getFirstStubEndDate(), other.getFirstStubEndDate()) &&
          JodaBeanUtils.equal(getLastStubEndDate(), other.getLastStubEndDate()) &&
          JodaBeanUtils.equal(getFirstStubStartReferenceRateId(), other.getFirstStubStartReferenceRateId()) &&
          JodaBeanUtils.equal(getFirstStubEndReferenceRateId(), other.getFirstStubEndReferenceRateId()) &&
          JodaBeanUtils.equal(getLastStubStartReferenceRateId(), other.getLastStubStartReferenceRateId()) &&
          JodaBeanUtils.equal(getLastStubEndReferenceRateId(), other.getLastStubEndReferenceRateId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFirstStubRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLastStubRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFirstStubEndDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLastStubEndDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFirstStubStartReferenceRateId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFirstStubEndReferenceRateId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLastStubStartReferenceRateId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLastStubEndReferenceRateId());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("StubCalculationMethod{");
    buf.append("type").append('=').append(getType()).append(',').append(' ');
    buf.append("firstStubRate").append('=').append(getFirstStubRate()).append(',').append(' ');
    buf.append("lastStubRate").append('=').append(getLastStubRate()).append(',').append(' ');
    buf.append("firstStubEndDate").append('=').append(getFirstStubEndDate()).append(',').append(' ');
    buf.append("lastStubEndDate").append('=').append(getLastStubEndDate()).append(',').append(' ');
    buf.append("firstStubStartReferenceRateId").append('=').append(getFirstStubStartReferenceRateId()).append(',').append(' ');
    buf.append("firstStubEndReferenceRateId").append('=').append(getFirstStubEndReferenceRateId()).append(',').append(' ');
    buf.append("lastStubStartReferenceRateId").append('=').append(getLastStubStartReferenceRateId()).append(',').append(' ');
    buf.append("lastStubEndReferenceRateId").append('=').append(JodaBeanUtils.toString(getLastStubEndReferenceRateId()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code StubCalculationMethod}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<StubType> _type = DirectMetaProperty.ofImmutable(
        this, "type", StubCalculationMethod.class, StubType.class);
    /**
     * The meta-property for the {@code firstStubRate} property.
     */
    private final MetaProperty<Double> _firstStubRate = DirectMetaProperty.ofImmutable(
        this, "firstStubRate", StubCalculationMethod.class, Double.class);
    /**
     * The meta-property for the {@code lastStubRate} property.
     */
    private final MetaProperty<Double> _lastStubRate = DirectMetaProperty.ofImmutable(
        this, "lastStubRate", StubCalculationMethod.class, Double.class);
    /**
     * The meta-property for the {@code firstStubEndDate} property.
     */
    private final MetaProperty<LocalDate> _firstStubEndDate = DirectMetaProperty.ofImmutable(
        this, "firstStubEndDate", StubCalculationMethod.class, LocalDate.class);
    /**
     * The meta-property for the {@code lastStubEndDate} property.
     */
    private final MetaProperty<LocalDate> _lastStubEndDate = DirectMetaProperty.ofImmutable(
        this, "lastStubEndDate", StubCalculationMethod.class, LocalDate.class);
    /**
     * The meta-property for the {@code firstStubStartReferenceRateId} property.
     */
    private final MetaProperty<ExternalId> _firstStubStartReferenceRateId = DirectMetaProperty.ofImmutable(
        this, "firstStubStartReferenceRateId", StubCalculationMethod.class, ExternalId.class);
    /**
     * The meta-property for the {@code firstStubEndReferenceRateId} property.
     */
    private final MetaProperty<ExternalId> _firstStubEndReferenceRateId = DirectMetaProperty.ofImmutable(
        this, "firstStubEndReferenceRateId", StubCalculationMethod.class, ExternalId.class);
    /**
     * The meta-property for the {@code lastStubStartReferenceRateId} property.
     */
    private final MetaProperty<ExternalId> _lastStubStartReferenceRateId = DirectMetaProperty.ofImmutable(
        this, "lastStubStartReferenceRateId", StubCalculationMethod.class, ExternalId.class);
    /**
     * The meta-property for the {@code lastStubEndReferenceRateId} property.
     */
    private final MetaProperty<ExternalId> _lastStubEndReferenceRateId = DirectMetaProperty.ofImmutable(
        this, "lastStubEndReferenceRateId", StubCalculationMethod.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "type",
        "firstStubRate",
        "lastStubRate",
        "firstStubEndDate",
        "lastStubEndDate",
        "firstStubStartReferenceRateId",
        "firstStubEndReferenceRateId",
        "lastStubStartReferenceRateId",
        "lastStubEndReferenceRateId");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          return _type;
        case -579843714:  // firstStubRate
          return _firstStubRate;
        case 153142116:  // lastStubRate
          return _lastStubRate;
        case 1938251211:  // firstStubEndDate
          return _firstStubEndDate;
        case -1589587419:  // lastStubEndDate
          return _lastStubEndDate;
        case -80328414:  // firstStubStartReferenceRateId
          return _firstStubStartReferenceRateId;
        case -336301303:  // firstStubEndReferenceRateId
          return _firstStubEndReferenceRateId;
        case -1989003512:  // lastStubStartReferenceRateId
          return _lastStubStartReferenceRateId;
        case 1409196655:  // lastStubEndReferenceRateId
          return _lastStubEndReferenceRateId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public StubCalculationMethod.Builder builder() {
      return new StubCalculationMethod.Builder();
    }

    @Override
    public Class<? extends StubCalculationMethod> beanType() {
      return StubCalculationMethod.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public MetaProperty<StubType> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code firstStubRate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> firstStubRate() {
      return _firstStubRate;
    }

    /**
     * The meta-property for the {@code lastStubRate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> lastStubRate() {
      return _lastStubRate;
    }

    /**
     * The meta-property for the {@code firstStubEndDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> firstStubEndDate() {
      return _firstStubEndDate;
    }

    /**
     * The meta-property for the {@code lastStubEndDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> lastStubEndDate() {
      return _lastStubEndDate;
    }

    /**
     * The meta-property for the {@code firstStubStartReferenceRateId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExternalId> firstStubStartReferenceRateId() {
      return _firstStubStartReferenceRateId;
    }

    /**
     * The meta-property for the {@code firstStubEndReferenceRateId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExternalId> firstStubEndReferenceRateId() {
      return _firstStubEndReferenceRateId;
    }

    /**
     * The meta-property for the {@code lastStubStartReferenceRateId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExternalId> lastStubStartReferenceRateId() {
      return _lastStubStartReferenceRateId;
    }

    /**
     * The meta-property for the {@code lastStubEndReferenceRateId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExternalId> lastStubEndReferenceRateId() {
      return _lastStubEndReferenceRateId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          return ((StubCalculationMethod) bean).getType();
        case -579843714:  // firstStubRate
          return ((StubCalculationMethod) bean).getFirstStubRate();
        case 153142116:  // lastStubRate
          return ((StubCalculationMethod) bean).getLastStubRate();
        case 1938251211:  // firstStubEndDate
          return ((StubCalculationMethod) bean).getFirstStubEndDate();
        case -1589587419:  // lastStubEndDate
          return ((StubCalculationMethod) bean).getLastStubEndDate();
        case -80328414:  // firstStubStartReferenceRateId
          return ((StubCalculationMethod) bean).getFirstStubStartReferenceRateId();
        case -336301303:  // firstStubEndReferenceRateId
          return ((StubCalculationMethod) bean).getFirstStubEndReferenceRateId();
        case -1989003512:  // lastStubStartReferenceRateId
          return ((StubCalculationMethod) bean).getLastStubStartReferenceRateId();
        case 1409196655:  // lastStubEndReferenceRateId
          return ((StubCalculationMethod) bean).getLastStubEndReferenceRateId();
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
   * The bean-builder for {@code StubCalculationMethod}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<StubCalculationMethod> {

    private StubType _type;
    private Double _firstStubRate;
    private Double _lastStubRate;
    private LocalDate _firstStubEndDate;
    private LocalDate _lastStubEndDate;
    private ExternalId _firstStubStartReferenceRateId;
    private ExternalId _firstStubEndReferenceRateId;
    private ExternalId _lastStubStartReferenceRateId;
    private ExternalId _lastStubEndReferenceRateId;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(StubCalculationMethod beanToCopy) {
      this._type = beanToCopy.getType();
      this._firstStubRate = beanToCopy.getFirstStubRate();
      this._lastStubRate = beanToCopy.getLastStubRate();
      this._firstStubEndDate = beanToCopy.getFirstStubEndDate();
      this._lastStubEndDate = beanToCopy.getLastStubEndDate();
      this._firstStubStartReferenceRateId = beanToCopy.getFirstStubStartReferenceRateId();
      this._firstStubEndReferenceRateId = beanToCopy.getFirstStubEndReferenceRateId();
      this._lastStubStartReferenceRateId = beanToCopy.getLastStubStartReferenceRateId();
      this._lastStubEndReferenceRateId = beanToCopy.getLastStubEndReferenceRateId();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          return _type;
        case -579843714:  // firstStubRate
          return _firstStubRate;
        case 153142116:  // lastStubRate
          return _lastStubRate;
        case 1938251211:  // firstStubEndDate
          return _firstStubEndDate;
        case -1589587419:  // lastStubEndDate
          return _lastStubEndDate;
        case -80328414:  // firstStubStartReferenceRateId
          return _firstStubStartReferenceRateId;
        case -336301303:  // firstStubEndReferenceRateId
          return _firstStubEndReferenceRateId;
        case -1989003512:  // lastStubStartReferenceRateId
          return _lastStubStartReferenceRateId;
        case 1409196655:  // lastStubEndReferenceRateId
          return _lastStubEndReferenceRateId;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          this._type = (StubType) newValue;
          break;
        case -579843714:  // firstStubRate
          this._firstStubRate = (Double) newValue;
          break;
        case 153142116:  // lastStubRate
          this._lastStubRate = (Double) newValue;
          break;
        case 1938251211:  // firstStubEndDate
          this._firstStubEndDate = (LocalDate) newValue;
          break;
        case -1589587419:  // lastStubEndDate
          this._lastStubEndDate = (LocalDate) newValue;
          break;
        case -80328414:  // firstStubStartReferenceRateId
          this._firstStubStartReferenceRateId = (ExternalId) newValue;
          break;
        case -336301303:  // firstStubEndReferenceRateId
          this._firstStubEndReferenceRateId = (ExternalId) newValue;
          break;
        case -1989003512:  // lastStubStartReferenceRateId
          this._lastStubStartReferenceRateId = (ExternalId) newValue;
          break;
        case 1409196655:  // lastStubEndReferenceRateId
          this._lastStubEndReferenceRateId = (ExternalId) newValue;
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
    public StubCalculationMethod build() {
      return new StubCalculationMethod(
          _type,
          _firstStubRate,
          _lastStubRate,
          _firstStubEndDate,
          _lastStubEndDate,
          _firstStubStartReferenceRateId,
          _firstStubEndReferenceRateId,
          _lastStubStartReferenceRateId,
          _lastStubEndReferenceRateId);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code type} property in the builder.
     * @param type  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder type(StubType type) {
      JodaBeanUtils.notNull(type, "type");
      this._type = type;
      return this;
    }

    /**
     * Sets the {@code firstStubRate} property in the builder.
     * @param firstStubRate  the new value
     * @return this, for chaining, not null
     */
    public Builder firstStubRate(Double firstStubRate) {
      this._firstStubRate = firstStubRate;
      return this;
    }

    /**
     * Sets the {@code lastStubRate} property in the builder.
     * @param lastStubRate  the new value
     * @return this, for chaining, not null
     */
    public Builder lastStubRate(Double lastStubRate) {
      this._lastStubRate = lastStubRate;
      return this;
    }

    /**
     * Sets the {@code firstStubEndDate} property in the builder.
     * @param firstStubEndDate  the new value
     * @return this, for chaining, not null
     */
    public Builder firstStubEndDate(LocalDate firstStubEndDate) {
      this._firstStubEndDate = firstStubEndDate;
      return this;
    }

    /**
     * Sets the {@code lastStubEndDate} property in the builder.
     * @param lastStubEndDate  the new value
     * @return this, for chaining, not null
     */
    public Builder lastStubEndDate(LocalDate lastStubEndDate) {
      this._lastStubEndDate = lastStubEndDate;
      return this;
    }

    /**
     * Sets the {@code firstStubStartReferenceRateId} property in the builder.
     * @param firstStubStartReferenceRateId  the new value
     * @return this, for chaining, not null
     */
    public Builder firstStubStartReferenceRateId(ExternalId firstStubStartReferenceRateId) {
      this._firstStubStartReferenceRateId = firstStubStartReferenceRateId;
      return this;
    }

    /**
     * Sets the {@code firstStubEndReferenceRateId} property in the builder.
     * @param firstStubEndReferenceRateId  the new value
     * @return this, for chaining, not null
     */
    public Builder firstStubEndReferenceRateId(ExternalId firstStubEndReferenceRateId) {
      this._firstStubEndReferenceRateId = firstStubEndReferenceRateId;
      return this;
    }

    /**
     * Sets the {@code lastStubStartReferenceRateId} property in the builder.
     * @param lastStubStartReferenceRateId  the new value
     * @return this, for chaining, not null
     */
    public Builder lastStubStartReferenceRateId(ExternalId lastStubStartReferenceRateId) {
      this._lastStubStartReferenceRateId = lastStubStartReferenceRateId;
      return this;
    }

    /**
     * Sets the {@code lastStubEndReferenceRateId} property in the builder.
     * @param lastStubEndReferenceRateId  the new value
     * @return this, for chaining, not null
     */
    public Builder lastStubEndReferenceRateId(ExternalId lastStubEndReferenceRateId) {
      this._lastStubEndReferenceRateId = lastStubEndReferenceRateId;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(320);
      buf.append("StubCalculationMethod.Builder{");
      buf.append("type").append('=').append(JodaBeanUtils.toString(_type)).append(',').append(' ');
      buf.append("firstStubRate").append('=').append(JodaBeanUtils.toString(_firstStubRate)).append(',').append(' ');
      buf.append("lastStubRate").append('=').append(JodaBeanUtils.toString(_lastStubRate)).append(',').append(' ');
      buf.append("firstStubEndDate").append('=').append(JodaBeanUtils.toString(_firstStubEndDate)).append(',').append(' ');
      buf.append("lastStubEndDate").append('=').append(JodaBeanUtils.toString(_lastStubEndDate)).append(',').append(' ');
      buf.append("firstStubStartReferenceRateId").append('=').append(JodaBeanUtils.toString(_firstStubStartReferenceRateId)).append(',').append(' ');
      buf.append("firstStubEndReferenceRateId").append('=').append(JodaBeanUtils.toString(_firstStubEndReferenceRateId)).append(',').append(' ');
      buf.append("lastStubStartReferenceRateId").append('=').append(JodaBeanUtils.toString(_lastStubStartReferenceRateId)).append(',').append(' ');
      buf.append("lastStubEndReferenceRateId").append('=').append(JodaBeanUtils.toString(_lastStubEndReferenceRateId));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
