/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.credit;

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
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.SecurityDescription;
import com.opengamma.util.money.Currency;

/**
 * A security for Credit Default Swap Index Definitions. These will then be used as
 * the "underlying" security for individual Credit Default Swap Index securities.
 */
@BeanDefinition
@SecurityDescription(type = IndexCDSDefinitionSecurity.SECURITY_TYPE, description = "Index CDS definition")
public class IndexCDSDefinitionSecurity extends FinancialSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * The security type.
   */
  public static final String SECURITY_TYPE = "INDEX_CDS_DEFINITION";
  /**
   * the start date
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _startDate;

  /**
   * The version number.
   */
  @PropertyDefinition(validate = "notNull")
  private String _version;
  /**
   * The series number.
   */
  @PropertyDefinition(validate = "notNull")
  private String _series;
  /**
   * The family
   */
  @PropertyDefinition(validate = "notNull")
  private String _family;
  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The recovery rate for the index.
   */
  @PropertyDefinition(validate = "notNull")
  private Double _recoveryRate;
  /**
   * The coupon frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _couponFrequency;
  /**
   * The terms.
   */
  @PropertyDefinition(validate = "notNull")
  private CDSIndexTerms _terms;
  /**
   * The index components.
   */
  @PropertyDefinition(validate = "notNull")
  private CDSIndexComponentBundle _components;
  /**
   * The coupon (fractional i.e. 100 bps = 0.01)
   */
  @PropertyDefinition(validate = "notNull")
  private Double _coupon;
  /**
   * The holiday calendars.
   */
  @PropertyDefinition(validate = "notNull")
  private Set<ExternalId> _calendars;
  /**
   * The business day convention.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _businessDayConvention;

  /**
   * Creates an instance
   */
  IndexCDSDefinitionSecurity() { //For builder
    super(SECURITY_TYPE);
  }

  /**
   * Index CDS definition
   *
   * @param ids identifiers representing this cds index definition, used by cds inidicies to reference this definition
   * @param startDate the start date, not null
   * @param version  the version, not null
   * @param series  the series, not null
   * @param family  the family, not null
   * @param currency  the currency, not null
   * @param recoveryRate the recovery rate for the index, not null
   * @param couponFrequency the coupon frequency, not null
   * @param coupon the coupon, not null
   * @param terms the terms, not null
   * @param components the components, not null
   * @param calendars the holiday calendars, not null
   * @param businessDayConvention the business day convention, not null
   */
  public IndexCDSDefinitionSecurity(
      ExternalIdBundle ids,
      LocalDate startDate,
      String version,
      String series,
      String family,
      Currency currency,
      Double recoveryRate,
      Frequency couponFrequency,
      Double coupon,
      CDSIndexTerms terms,
      CDSIndexComponentBundle components,
      Set<ExternalId> calendars,
      BusinessDayConvention businessDayConvention) {
    super(SECURITY_TYPE);
    setExternalIdBundle(ids);
    setStartDate(startDate);
    setVersion(version);
    setSeries(series);
    setFamily(family);
    setCurrency(currency);
    setRecoveryRate(recoveryRate);
    setCouponFrequency(couponFrequency);
    setCoupon(coupon);
    setTerms(terms);
    setComponents(components);
    setCalendars(calendars);
    setBusinessDayConvention(businessDayConvention);
  }

  /**
   * Index CDS definition
   *
   * @param ids identifiers representing this cds index definition, used by cds inidicies to reference this definition
   * @param name the descriptive name for this cds index definition
   * @param startDate the start date, not null
   * @param version  the version, not null
   * @param series  the series, not null
   * @param family  the family, not null
   * @param currency  the currency, not null
   * @param recoveryRate the recovery rate for the index, not null
   * @param couponFrequency the coupon frequency, not null
   * @param coupon the coupon, not null
   * @param terms the terms, not null
   * @param components the components, not null
   * @param calendars the holiday calendars, not null
   * @param businessDayConvention the business day convention, not null
   */
  public IndexCDSDefinitionSecurity(
      ExternalIdBundle ids,
      String name,
      LocalDate startDate,
      String version,
      String series,
      String family,
      Currency currency,
      Double recoveryRate,
      Frequency couponFrequency,
      Double coupon,
      CDSIndexTerms terms,
      CDSIndexComponentBundle components,
      Set<ExternalId> calendars,
      BusinessDayConvention businessDayConvention) {
    super(SECURITY_TYPE);
    setExternalIdBundle(ids);
    setName(name);
    setStartDate(startDate);
    setVersion(version);
    setSeries(series);
    setFamily(family);
    setCurrency(currency);
    setRecoveryRate(recoveryRate);
    setCouponFrequency(couponFrequency);
    setCoupon(coupon);
    setTerms(terms);
    setComponents(components);
    setCalendars(calendars);
    setBusinessDayConvention(businessDayConvention);
  }

  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitIndexCDSDefinitionSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IndexCDSDefinitionSecurity}.
   * @return the meta-bean, not null
   */
  public static IndexCDSDefinitionSecurity.Meta meta() {
    return IndexCDSDefinitionSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(IndexCDSDefinitionSecurity.Meta.INSTANCE);
  }

  @Override
  public IndexCDSDefinitionSecurity.Meta metaBean() {
    return IndexCDSDefinitionSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date
   * @return the value of the property, not null
   */
  public LocalDate getStartDate() {
    return _startDate;
  }

  /**
   * Sets the start date
   * @param startDate  the new value of the property, not null
   */
  public void setStartDate(LocalDate startDate) {
    JodaBeanUtils.notNull(startDate, "startDate");
    this._startDate = startDate;
  }

  /**
   * Gets the the {@code startDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> startDate() {
    return metaBean().startDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the version number.
   * @return the value of the property, not null
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Sets the version number.
   * @param version  the new value of the property, not null
   */
  public void setVersion(String version) {
    JodaBeanUtils.notNull(version, "version");
    this._version = version;
  }

  /**
   * Gets the the {@code version} property.
   * @return the property, not null
   */
  public final Property<String> version() {
    return metaBean().version().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the series number.
   * @return the value of the property, not null
   */
  public String getSeries() {
    return _series;
  }

  /**
   * Sets the series number.
   * @param series  the new value of the property, not null
   */
  public void setSeries(String series) {
    JodaBeanUtils.notNull(series, "series");
    this._series = series;
  }

  /**
   * Gets the the {@code series} property.
   * @return the property, not null
   */
  public final Property<String> series() {
    return metaBean().series().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the family
   * @return the value of the property, not null
   */
  public String getFamily() {
    return _family;
  }

  /**
   * Sets the family
   * @param family  the new value of the property, not null
   */
  public void setFamily(String family) {
    JodaBeanUtils.notNull(family, "family");
    this._family = family;
  }

  /**
   * Gets the the {@code family} property.
   * @return the property, not null
   */
  public final Property<String> family() {
    return metaBean().family().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the recovery rate for the index.
   * @return the value of the property, not null
   */
  public Double getRecoveryRate() {
    return _recoveryRate;
  }

  /**
   * Sets the recovery rate for the index.
   * @param recoveryRate  the new value of the property, not null
   */
  public void setRecoveryRate(Double recoveryRate) {
    JodaBeanUtils.notNull(recoveryRate, "recoveryRate");
    this._recoveryRate = recoveryRate;
  }

  /**
   * Gets the the {@code recoveryRate} property.
   * @return the property, not null
   */
  public final Property<Double> recoveryRate() {
    return metaBean().recoveryRate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon frequency.
   * @return the value of the property, not null
   */
  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  /**
   * Sets the coupon frequency.
   * @param couponFrequency  the new value of the property, not null
   */
  public void setCouponFrequency(Frequency couponFrequency) {
    JodaBeanUtils.notNull(couponFrequency, "couponFrequency");
    this._couponFrequency = couponFrequency;
  }

  /**
   * Gets the the {@code couponFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> couponFrequency() {
    return metaBean().couponFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the terms.
   * @return the value of the property, not null
   */
  public CDSIndexTerms getTerms() {
    return _terms;
  }

  /**
   * Sets the terms.
   * @param terms  the new value of the property, not null
   */
  public void setTerms(CDSIndexTerms terms) {
    JodaBeanUtils.notNull(terms, "terms");
    this._terms = terms;
  }

  /**
   * Gets the the {@code terms} property.
   * @return the property, not null
   */
  public final Property<CDSIndexTerms> terms() {
    return metaBean().terms().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the index components.
   * @return the value of the property, not null
   */
  public CDSIndexComponentBundle getComponents() {
    return _components;
  }

  /**
   * Sets the index components.
   * @param components  the new value of the property, not null
   */
  public void setComponents(CDSIndexComponentBundle components) {
    JodaBeanUtils.notNull(components, "components");
    this._components = components;
  }

  /**
   * Gets the the {@code components} property.
   * @return the property, not null
   */
  public final Property<CDSIndexComponentBundle> components() {
    return metaBean().components().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon (fractional i.e. 100 bps = 0.01)
   * @return the value of the property, not null
   */
  public Double getCoupon() {
    return _coupon;
  }

  /**
   * Sets the coupon (fractional i.e. 100 bps = 0.01)
   * @param coupon  the new value of the property, not null
   */
  public void setCoupon(Double coupon) {
    JodaBeanUtils.notNull(coupon, "coupon");
    this._coupon = coupon;
  }

  /**
   * Gets the the {@code coupon} property.
   * @return the property, not null
   */
  public final Property<Double> coupon() {
    return metaBean().coupon().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday calendars.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getCalendars() {
    return _calendars;
  }

  /**
   * Sets the holiday calendars.
   * @param calendars  the new value of the property, not null
   */
  public void setCalendars(Set<ExternalId> calendars) {
    JodaBeanUtils.notNull(calendars, "calendars");
    this._calendars = calendars;
  }

  /**
   * Gets the the {@code calendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> calendars() {
    return metaBean().calendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the business day convention.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Sets the business day convention.
   * @param businessDayConvention  the new value of the property, not null
   */
  public void setBusinessDayConvention(BusinessDayConvention businessDayConvention) {
    JodaBeanUtils.notNull(businessDayConvention, "businessDayConvention");
    this._businessDayConvention = businessDayConvention;
  }

  /**
   * Gets the the {@code businessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> businessDayConvention() {
    return metaBean().businessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public IndexCDSDefinitionSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      IndexCDSDefinitionSecurity other = (IndexCDSDefinitionSecurity) obj;
      return JodaBeanUtils.equal(getStartDate(), other.getStartDate()) &&
          JodaBeanUtils.equal(getVersion(), other.getVersion()) &&
          JodaBeanUtils.equal(getSeries(), other.getSeries()) &&
          JodaBeanUtils.equal(getFamily(), other.getFamily()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getRecoveryRate(), other.getRecoveryRate()) &&
          JodaBeanUtils.equal(getCouponFrequency(), other.getCouponFrequency()) &&
          JodaBeanUtils.equal(getTerms(), other.getTerms()) &&
          JodaBeanUtils.equal(getComponents(), other.getComponents()) &&
          JodaBeanUtils.equal(getCoupon(), other.getCoupon()) &&
          JodaBeanUtils.equal(getCalendars(), other.getCalendars()) &&
          JodaBeanUtils.equal(getBusinessDayConvention(), other.getBusinessDayConvention()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getStartDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getVersion());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSeries());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFamily());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRecoveryRate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCouponFrequency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTerms());
    hash = hash * 31 + JodaBeanUtils.hashCode(getComponents());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCoupon());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCalendars());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBusinessDayConvention());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(416);
    buf.append("IndexCDSDefinitionSecurity{");
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
    buf.append("startDate").append('=').append(JodaBeanUtils.toString(getStartDate())).append(',').append(' ');
    buf.append("version").append('=').append(JodaBeanUtils.toString(getVersion())).append(',').append(' ');
    buf.append("series").append('=').append(JodaBeanUtils.toString(getSeries())).append(',').append(' ');
    buf.append("family").append('=').append(JodaBeanUtils.toString(getFamily())).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("recoveryRate").append('=').append(JodaBeanUtils.toString(getRecoveryRate())).append(',').append(' ');
    buf.append("couponFrequency").append('=').append(JodaBeanUtils.toString(getCouponFrequency())).append(',').append(' ');
    buf.append("terms").append('=').append(JodaBeanUtils.toString(getTerms())).append(',').append(' ');
    buf.append("components").append('=').append(JodaBeanUtils.toString(getComponents())).append(',').append(' ');
    buf.append("coupon").append('=').append(JodaBeanUtils.toString(getCoupon())).append(',').append(' ');
    buf.append("calendars").append('=').append(JodaBeanUtils.toString(getCalendars())).append(',').append(' ');
    buf.append("businessDayConvention").append('=').append(JodaBeanUtils.toString(getBusinessDayConvention())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IndexCDSDefinitionSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code startDate} property.
     */
    private final MetaProperty<LocalDate> _startDate = DirectMetaProperty.ofReadWrite(
        this, "startDate", IndexCDSDefinitionSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code version} property.
     */
    private final MetaProperty<String> _version = DirectMetaProperty.ofReadWrite(
        this, "version", IndexCDSDefinitionSecurity.class, String.class);
    /**
     * The meta-property for the {@code series} property.
     */
    private final MetaProperty<String> _series = DirectMetaProperty.ofReadWrite(
        this, "series", IndexCDSDefinitionSecurity.class, String.class);
    /**
     * The meta-property for the {@code family} property.
     */
    private final MetaProperty<String> _family = DirectMetaProperty.ofReadWrite(
        this, "family", IndexCDSDefinitionSecurity.class, String.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", IndexCDSDefinitionSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code recoveryRate} property.
     */
    private final MetaProperty<Double> _recoveryRate = DirectMetaProperty.ofReadWrite(
        this, "recoveryRate", IndexCDSDefinitionSecurity.class, Double.class);
    /**
     * The meta-property for the {@code couponFrequency} property.
     */
    private final MetaProperty<Frequency> _couponFrequency = DirectMetaProperty.ofReadWrite(
        this, "couponFrequency", IndexCDSDefinitionSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code terms} property.
     */
    private final MetaProperty<CDSIndexTerms> _terms = DirectMetaProperty.ofReadWrite(
        this, "terms", IndexCDSDefinitionSecurity.class, CDSIndexTerms.class);
    /**
     * The meta-property for the {@code components} property.
     */
    private final MetaProperty<CDSIndexComponentBundle> _components = DirectMetaProperty.ofReadWrite(
        this, "components", IndexCDSDefinitionSecurity.class, CDSIndexComponentBundle.class);
    /**
     * The meta-property for the {@code coupon} property.
     */
    private final MetaProperty<Double> _coupon = DirectMetaProperty.ofReadWrite(
        this, "coupon", IndexCDSDefinitionSecurity.class, Double.class);
    /**
     * The meta-property for the {@code calendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _calendars = DirectMetaProperty.ofReadWrite(
        this, "calendars", IndexCDSDefinitionSecurity.class, (Class) Set.class);
    /**
     * The meta-property for the {@code businessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _businessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "businessDayConvention", IndexCDSDefinitionSecurity.class, BusinessDayConvention.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "startDate",
        "version",
        "series",
        "family",
        "currency",
        "recoveryRate",
        "couponFrequency",
        "terms",
        "components",
        "coupon",
        "calendars",
        "businessDayConvention");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2129778896:  // startDate
          return _startDate;
        case 351608024:  // version
          return _version;
        case -905838985:  // series
          return _series;
        case -1281860764:  // family
          return _family;
        case 575402001:  // currency
          return _currency;
        case 2002873877:  // recoveryRate
          return _recoveryRate;
        case 144480214:  // couponFrequency
          return _couponFrequency;
        case 110250375:  // terms
          return _terms;
        case -447446250:  // components
          return _components;
        case -1354573786:  // coupon
          return _coupon;
        case -1233097483:  // calendars
          return _calendars;
        case -1002835891:  // businessDayConvention
          return _businessDayConvention;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends IndexCDSDefinitionSecurity> builder() {
      return new DirectBeanBuilder<IndexCDSDefinitionSecurity>(new IndexCDSDefinitionSecurity());
    }

    @Override
    public Class<? extends IndexCDSDefinitionSecurity> beanType() {
      return IndexCDSDefinitionSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code startDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> startDate() {
      return _startDate;
    }

    /**
     * The meta-property for the {@code version} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> version() {
      return _version;
    }

    /**
     * The meta-property for the {@code series} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> series() {
      return _series;
    }

    /**
     * The meta-property for the {@code family} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> family() {
      return _family;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code recoveryRate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> recoveryRate() {
      return _recoveryRate;
    }

    /**
     * The meta-property for the {@code couponFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> couponFrequency() {
      return _couponFrequency;
    }

    /**
     * The meta-property for the {@code terms} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CDSIndexTerms> terms() {
      return _terms;
    }

    /**
     * The meta-property for the {@code components} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CDSIndexComponentBundle> components() {
      return _components;
    }

    /**
     * The meta-property for the {@code coupon} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> coupon() {
      return _coupon;
    }

    /**
     * The meta-property for the {@code calendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> calendars() {
      return _calendars;
    }

    /**
     * The meta-property for the {@code businessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> businessDayConvention() {
      return _businessDayConvention;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2129778896:  // startDate
          return ((IndexCDSDefinitionSecurity) bean).getStartDate();
        case 351608024:  // version
          return ((IndexCDSDefinitionSecurity) bean).getVersion();
        case -905838985:  // series
          return ((IndexCDSDefinitionSecurity) bean).getSeries();
        case -1281860764:  // family
          return ((IndexCDSDefinitionSecurity) bean).getFamily();
        case 575402001:  // currency
          return ((IndexCDSDefinitionSecurity) bean).getCurrency();
        case 2002873877:  // recoveryRate
          return ((IndexCDSDefinitionSecurity) bean).getRecoveryRate();
        case 144480214:  // couponFrequency
          return ((IndexCDSDefinitionSecurity) bean).getCouponFrequency();
        case 110250375:  // terms
          return ((IndexCDSDefinitionSecurity) bean).getTerms();
        case -447446250:  // components
          return ((IndexCDSDefinitionSecurity) bean).getComponents();
        case -1354573786:  // coupon
          return ((IndexCDSDefinitionSecurity) bean).getCoupon();
        case -1233097483:  // calendars
          return ((IndexCDSDefinitionSecurity) bean).getCalendars();
        case -1002835891:  // businessDayConvention
          return ((IndexCDSDefinitionSecurity) bean).getBusinessDayConvention();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2129778896:  // startDate
          ((IndexCDSDefinitionSecurity) bean).setStartDate((LocalDate) newValue);
          return;
        case 351608024:  // version
          ((IndexCDSDefinitionSecurity) bean).setVersion((String) newValue);
          return;
        case -905838985:  // series
          ((IndexCDSDefinitionSecurity) bean).setSeries((String) newValue);
          return;
        case -1281860764:  // family
          ((IndexCDSDefinitionSecurity) bean).setFamily((String) newValue);
          return;
        case 575402001:  // currency
          ((IndexCDSDefinitionSecurity) bean).setCurrency((Currency) newValue);
          return;
        case 2002873877:  // recoveryRate
          ((IndexCDSDefinitionSecurity) bean).setRecoveryRate((Double) newValue);
          return;
        case 144480214:  // couponFrequency
          ((IndexCDSDefinitionSecurity) bean).setCouponFrequency((Frequency) newValue);
          return;
        case 110250375:  // terms
          ((IndexCDSDefinitionSecurity) bean).setTerms((CDSIndexTerms) newValue);
          return;
        case -447446250:  // components
          ((IndexCDSDefinitionSecurity) bean).setComponents((CDSIndexComponentBundle) newValue);
          return;
        case -1354573786:  // coupon
          ((IndexCDSDefinitionSecurity) bean).setCoupon((Double) newValue);
          return;
        case -1233097483:  // calendars
          ((IndexCDSDefinitionSecurity) bean).setCalendars((Set<ExternalId>) newValue);
          return;
        case -1002835891:  // businessDayConvention
          ((IndexCDSDefinitionSecurity) bean).setBusinessDayConvention((BusinessDayConvention) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._startDate, "startDate");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._version, "version");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._series, "series");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._family, "family");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._recoveryRate, "recoveryRate");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._couponFrequency, "couponFrequency");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._terms, "terms");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._components, "components");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._coupon, "coupon");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._calendars, "calendars");
      JodaBeanUtils.notNull(((IndexCDSDefinitionSecurity) bean)._businessDayConvention, "businessDayConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}



