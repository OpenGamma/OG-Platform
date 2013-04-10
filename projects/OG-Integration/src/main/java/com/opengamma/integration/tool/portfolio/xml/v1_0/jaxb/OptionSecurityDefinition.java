/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.beans.BeanDefinition;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.threeten.bp.YearMonth;

import com.opengamma.financial.security.option.OptionType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.conversion.ListedOptionSecurityExtractor;
import com.opengamma.integration.tool.portfolio.xml.v1_0.conversion.ListedSecurityExtractor;

import java.util.Map;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

@BeanDefinition
@XmlRootElement(name = "optionSecurity")
public class OptionSecurityDefinition extends ListedSecurityDefinition {

  public enum ListedOptionType {
    @XmlEnumValue(value = "equityIndexOption")
    EQUITY_INDEX_OPTION,
    @XmlEnumValue(value = "equityDividendOption")
    EQUITY_DIVIDEND_OPTION
  }

  @XmlAttribute(name = "type", required = true)
  @PropertyDefinition
  private ListedOptionType _listedOptionType;

  @XmlElement(name = "optionType", required = true)
  @PropertyDefinition
  private OptionType _optionType;

  @XmlElement(name = "strike", required = true)
  @PropertyDefinition
  private BigDecimal _strike;

  @XmlElement(name = "optionExpiry", required = true)
  @XmlJavaTypeAdapter(DerivativeExpiryDateAdapter.class)
  @PropertyDefinition
  private YearMonth _optionExpiry;

  @XmlElement(name = "exerciseType", required = true)
  @PropertyDefinition
  private ExerciseType _exerciseType;

  public ListedSecurityExtractor getSecurityExtractor() {
    return new ListedOptionSecurityExtractor(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code OptionSecurityDefinition}.
   * @return the meta-bean, not null
   */
  public static OptionSecurityDefinition.Meta meta() {
    return OptionSecurityDefinition.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(OptionSecurityDefinition.Meta.INSTANCE);
  }

  @Override
  public OptionSecurityDefinition.Meta metaBean() {
    return OptionSecurityDefinition.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1985615124:  // listedOptionType
        return getListedOptionType();
      case 1373587791:  // optionType
        return getOptionType();
      case -891985998:  // strike
        return getStrike();
      case 1032553992:  // optionExpiry
        return getOptionExpiry();
      case -466331342:  // exerciseType
        return getExerciseType();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1985615124:  // listedOptionType
        setListedOptionType((ListedOptionType) newValue);
        return;
      case 1373587791:  // optionType
        setOptionType((OptionType) newValue);
        return;
      case -891985998:  // strike
        setStrike((BigDecimal) newValue);
        return;
      case 1032553992:  // optionExpiry
        setOptionExpiry((YearMonth) newValue);
        return;
      case -466331342:  // exerciseType
        setExerciseType((ExerciseType) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      OptionSecurityDefinition other = (OptionSecurityDefinition) obj;
      return JodaBeanUtils.equal(getListedOptionType(), other.getListedOptionType()) &&
          JodaBeanUtils.equal(getOptionType(), other.getOptionType()) &&
          JodaBeanUtils.equal(getStrike(), other.getStrike()) &&
          JodaBeanUtils.equal(getOptionExpiry(), other.getOptionExpiry()) &&
          JodaBeanUtils.equal(getExerciseType(), other.getExerciseType()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getListedOptionType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getOptionType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStrike());
    hash += hash * 31 + JodaBeanUtils.hashCode(getOptionExpiry());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExerciseType());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the listedOptionType.
   * @return the value of the property
   */
  public ListedOptionType getListedOptionType() {
    return _listedOptionType;
  }

  /**
   * Sets the listedOptionType.
   * @param listedOptionType  the new value of the property
   */
  public void setListedOptionType(ListedOptionType listedOptionType) {
    this._listedOptionType = listedOptionType;
  }

  /**
   * Gets the the {@code listedOptionType} property.
   * @return the property, not null
   */
  public final Property<ListedOptionType> listedOptionType() {
    return metaBean().listedOptionType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the optionType.
   * @return the value of the property
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * Sets the optionType.
   * @param optionType  the new value of the property
   */
  public void setOptionType(OptionType optionType) {
    this._optionType = optionType;
  }

  /**
   * Gets the the {@code optionType} property.
   * @return the property, not null
   */
  public final Property<OptionType> optionType() {
    return metaBean().optionType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the strike.
   * @return the value of the property
   */
  public BigDecimal getStrike() {
    return _strike;
  }

  /**
   * Sets the strike.
   * @param strike  the new value of the property
   */
  public void setStrike(BigDecimal strike) {
    this._strike = strike;
  }

  /**
   * Gets the the {@code strike} property.
   * @return the property, not null
   */
  public final Property<BigDecimal> strike() {
    return metaBean().strike().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the optionExpiry.
   * @return the value of the property
   */
  public YearMonth getOptionExpiry() {
    return _optionExpiry;
  }

  /**
   * Sets the optionExpiry.
   * @param optionExpiry  the new value of the property
   */
  public void setOptionExpiry(YearMonth optionExpiry) {
    this._optionExpiry = optionExpiry;
  }

  /**
   * Gets the the {@code optionExpiry} property.
   * @return the property, not null
   */
  public final Property<YearMonth> optionExpiry() {
    return metaBean().optionExpiry().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exerciseType.
   * @return the value of the property
   */
  public ExerciseType getExerciseType() {
    return _exerciseType;
  }

  /**
   * Sets the exerciseType.
   * @param exerciseType  the new value of the property
   */
  public void setExerciseType(ExerciseType exerciseType) {
    this._exerciseType = exerciseType;
  }

  /**
   * Gets the the {@code exerciseType} property.
   * @return the property, not null
   */
  public final Property<ExerciseType> exerciseType() {
    return metaBean().exerciseType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code OptionSecurityDefinition}.
   */
  public static class Meta extends ListedSecurityDefinition.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code listedOptionType} property.
     */
    private final MetaProperty<ListedOptionType> _listedOptionType = DirectMetaProperty.ofReadWrite(
        this, "listedOptionType", OptionSecurityDefinition.class, ListedOptionType.class);
    /**
     * The meta-property for the {@code optionType} property.
     */
    private final MetaProperty<OptionType> _optionType = DirectMetaProperty.ofReadWrite(
        this, "optionType", OptionSecurityDefinition.class, OptionType.class);
    /**
     * The meta-property for the {@code strike} property.
     */
    private final MetaProperty<BigDecimal> _strike = DirectMetaProperty.ofReadWrite(
        this, "strike", OptionSecurityDefinition.class, BigDecimal.class);
    /**
     * The meta-property for the {@code optionExpiry} property.
     */
    private final MetaProperty<YearMonth> _optionExpiry = DirectMetaProperty.ofReadWrite(
        this, "optionExpiry", OptionSecurityDefinition.class, YearMonth.class);
    /**
     * The meta-property for the {@code exerciseType} property.
     */
    private final MetaProperty<ExerciseType> _exerciseType = DirectMetaProperty.ofReadWrite(
        this, "exerciseType", OptionSecurityDefinition.class, ExerciseType.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "listedOptionType",
        "optionType",
        "strike",
        "optionExpiry",
        "exerciseType");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1985615124:  // listedOptionType
          return _listedOptionType;
        case 1373587791:  // optionType
          return _optionType;
        case -891985998:  // strike
          return _strike;
        case 1032553992:  // optionExpiry
          return _optionExpiry;
        case -466331342:  // exerciseType
          return _exerciseType;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends OptionSecurityDefinition> builder() {
      return new DirectBeanBuilder<OptionSecurityDefinition>(new OptionSecurityDefinition());
    }

    @Override
    public Class<? extends OptionSecurityDefinition> beanType() {
      return OptionSecurityDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code listedOptionType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ListedOptionType> listedOptionType() {
      return _listedOptionType;
    }

    /**
     * The meta-property for the {@code optionType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<OptionType> optionType() {
      return _optionType;
    }

    /**
     * The meta-property for the {@code strike} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> strike() {
      return _strike;
    }

    /**
     * The meta-property for the {@code optionExpiry} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<YearMonth> optionExpiry() {
      return _optionExpiry;
    }

    /**
     * The meta-property for the {@code exerciseType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExerciseType> exerciseType() {
      return _exerciseType;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
