/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.legalentity.Account;
import com.opengamma.core.legalentity.Capability;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.Obligation;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.RootPortfolio;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of {@code LegalEntity}.
 * <p>
 * This is the simplest possible implementation of the {@link LegalEntity} interface.
 * <p>
 * This class is mutable and not thread-safe.
 * It is intended to be used in the engine via the read-only {@code LegalEntity} interface.
 */
@BeanDefinition
public class SimpleLegalEntity extends DirectBean
    implements LegalEntity, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the legal entity.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private UniqueId _uniqueId;
  /**
   * The bundle of external identifiers that define the legal entity.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private ExternalIdBundle _externalIdBundle = ExternalIdBundle.EMPTY;
  /**
   * The map of attributes, which can be used for attaching additional application-level information.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private final Map<String, String> _attributes = new HashMap<>();
  /**
   * The map of details, which can be used for attaching additional application-level information.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private final Map<String, String> _details = new HashMap<>();
  /**
   * The name of the legal entity.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private String _name = "";
  /**
   * The ratings.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private List<Rating> _ratings = new ArrayList<>();
  /**
   * The capabilities.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private List<Capability> _capabilities = new ArrayList<>();
  /**
   * The external identifier bundle.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private List<ExternalIdBundle> _issuedSecurities = new ArrayList<>();
  /**
   * The obligations.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private List<Obligation> _obligations = new ArrayList<>();
  /**
   * The root portfolio.
   */
  @PropertyDefinition(overrideGet = true)
  private RootPortfolio _rootPortfolio;
  /**
   * The accounts.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private List<Account> _accounts = new ArrayList<>();

  /**
   * Creates a legal entity.
   */
  protected SimpleLegalEntity() {
  }

  /**
   * Creates a legal entity specifying the values of the main fields.
   *
   * @param name the name of the legal entity, not null
   * @param externalIdBundle the bundle of identifiers that define the legal entity, not null
   */
  public SimpleLegalEntity(String name, ExternalIdBundle externalIdBundle) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(externalIdBundle, "externalIdBundle");
    setName(name);
    setExternalIdBundle(externalIdBundle);
  }

  /**
   * Creates a legal entity specifying the values of the main fields.
   *
   * @param uniqueId the unique identifier, not null
   * @param name the name of the legal entity, not null
   * @param externalIdBundle the bundle of identifiers that define the legal entity, not null
   */
  protected SimpleLegalEntity(UniqueId uniqueId, String name, ExternalIdBundle externalIdBundle) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(externalIdBundle, "externalIdBundle");
    setUniqueId(uniqueId);
    setName(name);
    setExternalIdBundle(externalIdBundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an external identifier to the bundle representing this legal entity.
   *
   * @param legalEntityId the identifier to add, not null
   */
  public void addExternalId(ExternalId legalEntityId) {
    setExternalIdBundle(getExternalIdBundle().withExternalId(legalEntityId));
  }

  @Override
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  @Override
  public void addDetail(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _details.put(key, value);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleLegalEntity}.
   * @return the meta-bean, not null
   */
  public static SimpleLegalEntity.Meta meta() {
    return SimpleLegalEntity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SimpleLegalEntity.Meta.INSTANCE);
  }

  @Override
  public SimpleLegalEntity.Meta metaBean() {
    return SimpleLegalEntity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the legal entity.
   * @return the value of the property
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the legal entity.
   * @param uniqueId  the new value of the property
   */
  @Override
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bundle of external identifiers that define the legal entity.
   * @return the value of the property, not null
   */
  @Override
  public ExternalIdBundle getExternalIdBundle() {
    return _externalIdBundle;
  }

  /**
   * Sets the bundle of external identifiers that define the legal entity.
   * @param externalIdBundle  the new value of the property, not null
   */
  public void setExternalIdBundle(ExternalIdBundle externalIdBundle) {
    JodaBeanUtils.notNull(externalIdBundle, "externalIdBundle");
    this._externalIdBundle = externalIdBundle;
  }

  /**
   * Gets the the {@code externalIdBundle} property.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> externalIdBundle() {
    return metaBean().externalIdBundle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the map of attributes, which can be used for attaching additional application-level information.
   * @return the value of the property, not null
   */
  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the map of attributes, which can be used for attaching additional application-level information.
   * @param attributes  the new value of the property, not null
   */
  @Override
  public void setAttributes(Map<String, String> attributes) {
    JodaBeanUtils.notNull(attributes, "attributes");
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the map of details, which can be used for attaching additional application-level information.
   * @return the value of the property, not null
   */
  @Override
  public Map<String, String> getDetails() {
    return _details;
  }

  /**
   * Sets the map of details, which can be used for attaching additional application-level information.
   * @param details  the new value of the property, not null
   */
  @Override
  public void setDetails(Map<String, String> details) {
    JodaBeanUtils.notNull(details, "details");
    this._details.clear();
    this._details.putAll(details);
  }

  /**
   * Gets the the {@code details} property.
   * @return the property, not null
   */
  public final Property<Map<String, String>> details() {
    return metaBean().details().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the legal entity.
   * @return the value of the property, not null
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the legal entity.
   * @param name  the new value of the property, not null
   */
  public void setName(String name) {
    JodaBeanUtils.notNull(name, "name");
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the ratings.
   * @return the value of the property, not null
   */
  @Override
  public List<Rating> getRatings() {
    return _ratings;
  }

  /**
   * Sets the ratings.
   * @param ratings  the new value of the property, not null
   */
  public void setRatings(List<Rating> ratings) {
    JodaBeanUtils.notNull(ratings, "ratings");
    this._ratings = ratings;
  }

  /**
   * Gets the the {@code ratings} property.
   * @return the property, not null
   */
  public final Property<List<Rating>> ratings() {
    return metaBean().ratings().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the capabilities.
   * @return the value of the property, not null
   */
  @Override
  public List<Capability> getCapabilities() {
    return _capabilities;
  }

  /**
   * Sets the capabilities.
   * @param capabilities  the new value of the property, not null
   */
  public void setCapabilities(List<Capability> capabilities) {
    JodaBeanUtils.notNull(capabilities, "capabilities");
    this._capabilities = capabilities;
  }

  /**
   * Gets the the {@code capabilities} property.
   * @return the property, not null
   */
  public final Property<List<Capability>> capabilities() {
    return metaBean().capabilities().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier bundle.
   * @return the value of the property, not null
   */
  @Override
  public List<ExternalIdBundle> getIssuedSecurities() {
    return _issuedSecurities;
  }

  /**
   * Sets the external identifier bundle.
   * @param issuedSecurities  the new value of the property, not null
   */
  public void setIssuedSecurities(List<ExternalIdBundle> issuedSecurities) {
    JodaBeanUtils.notNull(issuedSecurities, "issuedSecurities");
    this._issuedSecurities = issuedSecurities;
  }

  /**
   * Gets the the {@code issuedSecurities} property.
   * @return the property, not null
   */
  public final Property<List<ExternalIdBundle>> issuedSecurities() {
    return metaBean().issuedSecurities().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the obligations.
   * @return the value of the property, not null
   */
  @Override
  public List<Obligation> getObligations() {
    return _obligations;
  }

  /**
   * Sets the obligations.
   * @param obligations  the new value of the property, not null
   */
  public void setObligations(List<Obligation> obligations) {
    JodaBeanUtils.notNull(obligations, "obligations");
    this._obligations = obligations;
  }

  /**
   * Gets the the {@code obligations} property.
   * @return the property, not null
   */
  public final Property<List<Obligation>> obligations() {
    return metaBean().obligations().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the root portfolio.
   * @return the value of the property
   */
  @Override
  public RootPortfolio getRootPortfolio() {
    return _rootPortfolio;
  }

  /**
   * Sets the root portfolio.
   * @param rootPortfolio  the new value of the property
   */
  public void setRootPortfolio(RootPortfolio rootPortfolio) {
    this._rootPortfolio = rootPortfolio;
  }

  /**
   * Gets the the {@code rootPortfolio} property.
   * @return the property, not null
   */
  public final Property<RootPortfolio> rootPortfolio() {
    return metaBean().rootPortfolio().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the accounts.
   * @return the value of the property, not null
   */
  @Override
  public List<Account> getAccounts() {
    return _accounts;
  }

  /**
   * Sets the accounts.
   * @param accounts  the new value of the property, not null
   */
  public void setAccounts(List<Account> accounts) {
    JodaBeanUtils.notNull(accounts, "accounts");
    this._accounts = accounts;
  }

  /**
   * Gets the the {@code accounts} property.
   * @return the property, not null
   */
  public final Property<List<Account>> accounts() {
    return metaBean().accounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SimpleLegalEntity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleLegalEntity other = (SimpleLegalEntity) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getExternalIdBundle(), other.getExternalIdBundle()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getDetails(), other.getDetails()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getRatings(), other.getRatings()) &&
          JodaBeanUtils.equal(getCapabilities(), other.getCapabilities()) &&
          JodaBeanUtils.equal(getIssuedSecurities(), other.getIssuedSecurities()) &&
          JodaBeanUtils.equal(getObligations(), other.getObligations()) &&
          JodaBeanUtils.equal(getRootPortfolio(), other.getRootPortfolio()) &&
          JodaBeanUtils.equal(getAccounts(), other.getAccounts());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdBundle());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDetails());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRatings());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCapabilities());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIssuedSecurities());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObligations());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRootPortfolio());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccounts());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(384);
    buf.append("SimpleLegalEntity{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("externalIdBundle").append('=').append(JodaBeanUtils.toString(getExternalIdBundle())).append(',').append(' ');
    buf.append("attributes").append('=').append(JodaBeanUtils.toString(getAttributes())).append(',').append(' ');
    buf.append("details").append('=').append(JodaBeanUtils.toString(getDetails())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("ratings").append('=').append(JodaBeanUtils.toString(getRatings())).append(',').append(' ');
    buf.append("capabilities").append('=').append(JodaBeanUtils.toString(getCapabilities())).append(',').append(' ');
    buf.append("issuedSecurities").append('=').append(JodaBeanUtils.toString(getIssuedSecurities())).append(',').append(' ');
    buf.append("obligations").append('=').append(JodaBeanUtils.toString(getObligations())).append(',').append(' ');
    buf.append("rootPortfolio").append('=').append(JodaBeanUtils.toString(getRootPortfolio())).append(',').append(' ');
    buf.append("accounts").append('=').append(JodaBeanUtils.toString(getAccounts())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleLegalEntity}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", SimpleLegalEntity.class, UniqueId.class);
    /**
     * The meta-property for the {@code externalIdBundle} property.
     */
    private final MetaProperty<ExternalIdBundle> _externalIdBundle = DirectMetaProperty.ofReadWrite(
        this, "externalIdBundle", SimpleLegalEntity.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", SimpleLegalEntity.class, (Class) Map.class);
    /**
     * The meta-property for the {@code details} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _details = DirectMetaProperty.ofReadWrite(
        this, "details", SimpleLegalEntity.class, (Class) Map.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", SimpleLegalEntity.class, String.class);
    /**
     * The meta-property for the {@code ratings} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Rating>> _ratings = DirectMetaProperty.ofReadWrite(
        this, "ratings", SimpleLegalEntity.class, (Class) List.class);
    /**
     * The meta-property for the {@code capabilities} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Capability>> _capabilities = DirectMetaProperty.ofReadWrite(
        this, "capabilities", SimpleLegalEntity.class, (Class) List.class);
    /**
     * The meta-property for the {@code issuedSecurities} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ExternalIdBundle>> _issuedSecurities = DirectMetaProperty.ofReadWrite(
        this, "issuedSecurities", SimpleLegalEntity.class, (Class) List.class);
    /**
     * The meta-property for the {@code obligations} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Obligation>> _obligations = DirectMetaProperty.ofReadWrite(
        this, "obligations", SimpleLegalEntity.class, (Class) List.class);
    /**
     * The meta-property for the {@code rootPortfolio} property.
     */
    private final MetaProperty<RootPortfolio> _rootPortfolio = DirectMetaProperty.ofReadWrite(
        this, "rootPortfolio", SimpleLegalEntity.class, RootPortfolio.class);
    /**
     * The meta-property for the {@code accounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Account>> _accounts = DirectMetaProperty.ofReadWrite(
        this, "accounts", SimpleLegalEntity.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "externalIdBundle",
        "attributes",
        "details",
        "name",
        "ratings",
        "capabilities",
        "issuedSecurities",
        "obligations",
        "rootPortfolio",
        "accounts");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case -736922008:  // externalIdBundle
          return _externalIdBundle;
        case 405645655:  // attributes
          return _attributes;
        case 1557721666:  // details
          return _details;
        case 3373707:  // name
          return _name;
        case 983597686:  // ratings
          return _ratings;
        case -1487597642:  // capabilities
          return _capabilities;
        case 1643621609:  // issuedSecurities
          return _issuedSecurities;
        case 809305781:  // obligations
          return _obligations;
        case -2027978106:  // rootPortfolio
          return _rootPortfolio;
        case -2137146394:  // accounts
          return _accounts;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleLegalEntity> builder() {
      return new DirectBeanBuilder<SimpleLegalEntity>(new SimpleLegalEntity());
    }

    @Override
    public Class<? extends SimpleLegalEntity> beanType() {
      return SimpleLegalEntity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code externalIdBundle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> externalIdBundle() {
      return _externalIdBundle;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    /**
     * The meta-property for the {@code details} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> details() {
      return _details;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code ratings} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Rating>> ratings() {
      return _ratings;
    }

    /**
     * The meta-property for the {@code capabilities} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Capability>> capabilities() {
      return _capabilities;
    }

    /**
     * The meta-property for the {@code issuedSecurities} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ExternalIdBundle>> issuedSecurities() {
      return _issuedSecurities;
    }

    /**
     * The meta-property for the {@code obligations} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Obligation>> obligations() {
      return _obligations;
    }

    /**
     * The meta-property for the {@code rootPortfolio} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RootPortfolio> rootPortfolio() {
      return _rootPortfolio;
    }

    /**
     * The meta-property for the {@code accounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Account>> accounts() {
      return _accounts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((SimpleLegalEntity) bean).getUniqueId();
        case -736922008:  // externalIdBundle
          return ((SimpleLegalEntity) bean).getExternalIdBundle();
        case 405645655:  // attributes
          return ((SimpleLegalEntity) bean).getAttributes();
        case 1557721666:  // details
          return ((SimpleLegalEntity) bean).getDetails();
        case 3373707:  // name
          return ((SimpleLegalEntity) bean).getName();
        case 983597686:  // ratings
          return ((SimpleLegalEntity) bean).getRatings();
        case -1487597642:  // capabilities
          return ((SimpleLegalEntity) bean).getCapabilities();
        case 1643621609:  // issuedSecurities
          return ((SimpleLegalEntity) bean).getIssuedSecurities();
        case 809305781:  // obligations
          return ((SimpleLegalEntity) bean).getObligations();
        case -2027978106:  // rootPortfolio
          return ((SimpleLegalEntity) bean).getRootPortfolio();
        case -2137146394:  // accounts
          return ((SimpleLegalEntity) bean).getAccounts();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((SimpleLegalEntity) bean).setUniqueId((UniqueId) newValue);
          return;
        case -736922008:  // externalIdBundle
          ((SimpleLegalEntity) bean).setExternalIdBundle((ExternalIdBundle) newValue);
          return;
        case 405645655:  // attributes
          ((SimpleLegalEntity) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 1557721666:  // details
          ((SimpleLegalEntity) bean).setDetails((Map<String, String>) newValue);
          return;
        case 3373707:  // name
          ((SimpleLegalEntity) bean).setName((String) newValue);
          return;
        case 983597686:  // ratings
          ((SimpleLegalEntity) bean).setRatings((List<Rating>) newValue);
          return;
        case -1487597642:  // capabilities
          ((SimpleLegalEntity) bean).setCapabilities((List<Capability>) newValue);
          return;
        case 1643621609:  // issuedSecurities
          ((SimpleLegalEntity) bean).setIssuedSecurities((List<ExternalIdBundle>) newValue);
          return;
        case 809305781:  // obligations
          ((SimpleLegalEntity) bean).setObligations((List<Obligation>) newValue);
          return;
        case -2027978106:  // rootPortfolio
          ((SimpleLegalEntity) bean).setRootPortfolio((RootPortfolio) newValue);
          return;
        case -2137146394:  // accounts
          ((SimpleLegalEntity) bean).setAccounts((List<Account>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._externalIdBundle, "externalIdBundle");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._attributes, "attributes");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._details, "details");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._name, "name");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._ratings, "ratings");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._capabilities, "capabilities");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._issuedSecurities, "issuedSecurities");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._obligations, "obligations");
      JodaBeanUtils.notNull(((SimpleLegalEntity) bean)._accounts, "accounts");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
