/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.types.ParameterizedTypeImpl;
import com.opengamma.util.types.VariantType;

/**
 * Gets the region or sub-fields of the region of an {@link LegalEntity}.
 */
@BeanDefinition
public class LegalEntityRegion implements LegalEntityFilter<LegalEntity>, Bean {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * True if the name is to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useName;

  /**
   * True if the countries are to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useCountry;

  /**
   * The set of countries to be used as a filter.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Set<Country> _countries;

  /**
   * True if the currencies are to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useCurrency;

  /**
   * The set of currencies to be used as a filter.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Set<Currency> _currencies;

  /**
   * For the builder.
   */
  public LegalEntityRegion() {
    setUseName(false);
    setUseCountry(false);
    setCountries(Collections.<Country>emptySet());
    setUseCurrency(false);
    setCurrencies(Collections.<Currency>emptySet());
  }

  /**
   * @param useName True if the name of the region is to be used as a filter
   * @param useCountries True if the countries are to be used as a filter
   * @param countries A set of countries to be used as a filter, not null. Can be empty
   * @param useCurrencies True if the countries are to be used as a filter
   * @param currencies A set of currencies to be used as a filter, not null. Can be empty
   */
  public LegalEntityRegion(final boolean useName, final boolean useCountries, final Set<Country> countries, final boolean useCurrencies, final Set<Currency> currencies) {
    setUseName(useName);
    setUseCountry(useCountries);
    setCountries(countries);
    setUseCurrency(useCurrencies);
    setCurrencies(currencies);
  }

  @Override
  public Object getFilteredData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "legal entity");
    final Region region = legalEntity.getRegion();
    if (region == null) {
      throw new IllegalStateException("Region for this legal entity " + legalEntity + " was null");
    }
    if (!(_useName || _useCountry || _useCurrency)) {
      return region;
    }
    final Set<Object> selections = new HashSet<>();
    if (_useName) {
      selections.add(region.getName());
    }
    if (_useCountry) {
      final Set<Country> countries = region.getCountries();
      if (_countries.isEmpty()) {
        selections.addAll(countries);
      } else if (countries.containsAll(_countries)) {
        selections.addAll(_countries);
      }
    }
    if (_useCurrency) {
      final Set<Currency> currencies = region.getCurrencies();
      if (_currencies.isEmpty()) {
        selections.addAll(currencies);
      } else if (currencies.containsAll(_currencies)) {
        selections.addAll(_currencies);
      }
    }
    return selections;
  }

  @Override
  public Type getFilteredDataType() {
    if (!(_useName || _useCountry || _useCurrency)) {
      return Region.class;
    }
    Type setMember = null;
    if (_useName) {
      // Set gets a string
      setMember = VariantType.either(setMember, String.class);
    }
    if (_useCountry) {
      // Set might contain Country instances
      setMember = VariantType.either(setMember, Country.class);
    }
    if (_useCurrency) {
      // Set might contain Currency instances
      setMember = VariantType.either(setMember, Currency.class);
    }
    return ParameterizedTypeImpl.of(Set.class, setMember);
  }

  /**
   * Sets the countries to be used as a filter. This also sets the {@link LegalEntityRegion#_useCountry} field to true.
   * 
   * @param countries The new value of the property, not null
   */
  public void setCountries(final Set<Country> countries) {
    JodaBeanUtils.notNull(countries, "countries");
    if (!countries.isEmpty()) {
      setUseCountry(true);
    }
    this._countries = countries;
  }

  /**
   * Sets the currencies to be used as a filter. This also sets the {@link LegalEntityRegion#_useCountry} field to true.
   * 
   * @param currencies The new value of the property, not null
   */
  public void setCurrencies(final Set<Currency> currencies) {
    JodaBeanUtils.notNull(currencies, "currencies");
    if (!currencies.isEmpty()) {
      setUseCurrency(true);
    }
    this._currencies = currencies;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LegalEntityRegion}.
   * @return the meta-bean, not null
   */
  public static LegalEntityRegion.Meta meta() {
    return LegalEntityRegion.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LegalEntityRegion.Meta.INSTANCE);
  }

  @Override
  public LegalEntityRegion.Meta metaBean() {
    return LegalEntityRegion.Meta.INSTANCE;
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
   * Gets true if the name is to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseName() {
    return _useName;
  }

  /**
   * Sets true if the name is to be used as a filter.
   * @param useName  the new value of the property
   */
  public void setUseName(boolean useName) {
    this._useName = useName;
  }

  /**
   * Gets the the {@code useName} property.
   * @return the property, not null
   */
  public final Property<Boolean> useName() {
    return metaBean().useName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets true if the countries are to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseCountry() {
    return _useCountry;
  }

  /**
   * Sets true if the countries are to be used as a filter.
   * @param useCountry  the new value of the property
   */
  public void setUseCountry(boolean useCountry) {
    this._useCountry = useCountry;
  }

  /**
   * Gets the the {@code useCountry} property.
   * @return the property, not null
   */
  public final Property<Boolean> useCountry() {
    return metaBean().useCountry().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of countries to be used as a filter.
   * @return the value of the property, not null
   */
  public Set<Country> getCountries() {
    return _countries;
  }

  /**
   * Gets the the {@code countries} property.
   * @return the property, not null
   */
  public final Property<Set<Country>> countries() {
    return metaBean().countries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets true if the currencies are to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseCurrency() {
    return _useCurrency;
  }

  /**
   * Sets true if the currencies are to be used as a filter.
   * @param useCurrency  the new value of the property
   */
  public void setUseCurrency(boolean useCurrency) {
    this._useCurrency = useCurrency;
  }

  /**
   * Gets the the {@code useCurrency} property.
   * @return the property, not null
   */
  public final Property<Boolean> useCurrency() {
    return metaBean().useCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of currencies to be used as a filter.
   * @return the value of the property, not null
   */
  public Set<Currency> getCurrencies() {
    return _currencies;
  }

  /**
   * Gets the the {@code currencies} property.
   * @return the property, not null
   */
  public final Property<Set<Currency>> currencies() {
    return metaBean().currencies().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public LegalEntityRegion clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LegalEntityRegion other = (LegalEntityRegion) obj;
      return (isUseName() == other.isUseName()) &&
          (isUseCountry() == other.isUseCountry()) &&
          JodaBeanUtils.equal(getCountries(), other.getCountries()) &&
          (isUseCurrency() == other.isUseCurrency()) &&
          JodaBeanUtils.equal(getCurrencies(), other.getCurrencies());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseName());
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseCountry());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCountries());
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrencies());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("LegalEntityRegion{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("useName").append('=').append(JodaBeanUtils.toString(isUseName())).append(',').append(' ');
    buf.append("useCountry").append('=').append(JodaBeanUtils.toString(isUseCountry())).append(',').append(' ');
    buf.append("countries").append('=').append(JodaBeanUtils.toString(getCountries())).append(',').append(' ');
    buf.append("useCurrency").append('=').append(JodaBeanUtils.toString(isUseCurrency())).append(',').append(' ');
    buf.append("currencies").append('=').append(JodaBeanUtils.toString(getCurrencies())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LegalEntityRegion}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code useName} property.
     */
    private final MetaProperty<Boolean> _useName = DirectMetaProperty.ofReadWrite(
        this, "useName", LegalEntityRegion.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code useCountry} property.
     */
    private final MetaProperty<Boolean> _useCountry = DirectMetaProperty.ofReadWrite(
        this, "useCountry", LegalEntityRegion.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code countries} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Country>> _countries = DirectMetaProperty.ofReadWrite(
        this, "countries", LegalEntityRegion.class, (Class) Set.class);
    /**
     * The meta-property for the {@code useCurrency} property.
     */
    private final MetaProperty<Boolean> _useCurrency = DirectMetaProperty.ofReadWrite(
        this, "useCurrency", LegalEntityRegion.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code currencies} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Currency>> _currencies = DirectMetaProperty.ofReadWrite(
        this, "currencies", LegalEntityRegion.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "useName",
        "useCountry",
        "countries",
        "useCurrency",
        "currencies");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -148203342:  // useName
          return _useName;
        case -663407601:  // useCountry
          return _useCountry;
        case 1352637108:  // countries
          return _countries;
        case 1856611000:  // useCurrency
          return _useCurrency;
        case -1089470353:  // currencies
          return _currencies;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends LegalEntityRegion> builder() {
      return new DirectBeanBuilder<LegalEntityRegion>(new LegalEntityRegion());
    }

    @Override
    public Class<? extends LegalEntityRegion> beanType() {
      return LegalEntityRegion.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code useName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useName() {
      return _useName;
    }

    /**
     * The meta-property for the {@code useCountry} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useCountry() {
      return _useCountry;
    }

    /**
     * The meta-property for the {@code countries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<Country>> countries() {
      return _countries;
    }

    /**
     * The meta-property for the {@code useCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useCurrency() {
      return _useCurrency;
    }

    /**
     * The meta-property for the {@code currencies} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<Currency>> currencies() {
      return _currencies;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -148203342:  // useName
          return ((LegalEntityRegion) bean).isUseName();
        case -663407601:  // useCountry
          return ((LegalEntityRegion) bean).isUseCountry();
        case 1352637108:  // countries
          return ((LegalEntityRegion) bean).getCountries();
        case 1856611000:  // useCurrency
          return ((LegalEntityRegion) bean).isUseCurrency();
        case -1089470353:  // currencies
          return ((LegalEntityRegion) bean).getCurrencies();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -148203342:  // useName
          ((LegalEntityRegion) bean).setUseName((Boolean) newValue);
          return;
        case -663407601:  // useCountry
          ((LegalEntityRegion) bean).setUseCountry((Boolean) newValue);
          return;
        case 1352637108:  // countries
          ((LegalEntityRegion) bean).setCountries((Set<Country>) newValue);
          return;
        case 1856611000:  // useCurrency
          ((LegalEntityRegion) bean).setUseCurrency((Boolean) newValue);
          return;
        case -1089470353:  // currencies
          ((LegalEntityRegion) bean).setCurrencies((Set<Currency>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((LegalEntityRegion) bean)._countries, "countries");
      JodaBeanUtils.notNull(((LegalEntityRegion) bean)._currencies, "currencies");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
