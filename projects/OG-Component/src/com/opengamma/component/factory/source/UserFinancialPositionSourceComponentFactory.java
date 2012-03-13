/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.DataPositionSourceResource;
import com.opengamma.core.position.impl.DelegatingPositionSource;
import com.opengamma.core.position.impl.EHCachingPositionSource;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;

/**
 * Component factory for the position source.
 */
@BeanDefinition
public class UserFinancialPositionSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The cache manager.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;
  /**
   * The classifier that the factory should publish under (underlying master).
   */
  @PropertyDefinition
  private String _underlyingClassifier;
  /**
   * The portfolio master (underlying master).
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioMaster _underlyingPortfolioMaster;
  /**
   * The position master (underlying master).
   */
  @PropertyDefinition(validate = "notNull")
  private PositionMaster _underlyingPositionMaster;
  /**
   * The classifier that the factory should publish under (user master).
   */
  @PropertyDefinition
  private String _userClassifier;
  /**
   * The portfolio master (user master).
   */
  @PropertyDefinition
  private PortfolioMaster _userPortfolioMaster;
  /**
   * The position master (user master).
   */
  @PropertyDefinition
  private PositionMaster _userPositionMaster;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    PositionSource source = initUnderlying(repo, configuration);
    
    // add user level if requested
    PositionSource userSource = initUser(repo, configuration);
    Map<String, PositionSource> map = new HashMap<String, PositionSource>();
    if (userSource != null) {
      String scheme = repo.getInfo(getUserPortfolioMaster()).getAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME);
      map.put(scheme, userSource);
      source = new DelegatingPositionSource(source, map);
    }
    
    // register
    ComponentInfo info = new ComponentInfo(PositionSource.class, getClassifier());
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataPositionSourceResource(source));
    }
  }

  protected PositionSource initUnderlying(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    PositionSource source = new MasterPositionSource(getUnderlyingPortfolioMaster(), getUnderlyingPositionMaster());
    if (getCacheManager() != null) {
      source = new EHCachingPositionSource(source, getCacheManager());
    }
    if (getUnderlyingClassifier() != null) {
      ComponentInfo info = new ComponentInfo(PositionSource.class, getUnderlyingClassifier());
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataPositionSourceResource(source));
      }
    }
    return source;
  }

  protected PositionSource initUser(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    if (getUserPortfolioMaster() == null || getUserPositionMaster() == null) {
      return null;
    }
    PositionSource source = new MasterPositionSource(getUserPortfolioMaster(), getUserPositionMaster());
    if (getUserClassifier() != null) {
      ComponentInfo info = new ComponentInfo(PositionSource.class, getUserClassifier());
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataPositionSourceResource(source));
      }
    }
    return source;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserFinancialPositionSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static UserFinancialPositionSourceComponentFactory.Meta meta() {
    return UserFinancialPositionSourceComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(UserFinancialPositionSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public UserFinancialPositionSourceComponentFactory.Meta metaBean() {
    return UserFinancialPositionSourceComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -614707837:  // publishRest
        return isPublishRest();
      case -1452875317:  // cacheManager
        return getCacheManager();
      case 1705602398:  // underlyingClassifier
        return getUnderlyingClassifier();
      case -337956691:  // underlyingPortfolioMaster
        return getUnderlyingPortfolioMaster();
      case -440936024:  // underlyingPositionMaster
        return getUnderlyingPositionMaster();
      case 473030732:  // userClassifier
        return getUserClassifier();
      case 686514815:  // userPortfolioMaster
        return getUserPortfolioMaster();
      case 1808868758:  // userPositionMaster
        return getUserPositionMaster();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
      case -1452875317:  // cacheManager
        setCacheManager((CacheManager) newValue);
        return;
      case 1705602398:  // underlyingClassifier
        setUnderlyingClassifier((String) newValue);
        return;
      case -337956691:  // underlyingPortfolioMaster
        setUnderlyingPortfolioMaster((PortfolioMaster) newValue);
        return;
      case -440936024:  // underlyingPositionMaster
        setUnderlyingPositionMaster((PositionMaster) newValue);
        return;
      case 473030732:  // userClassifier
        setUserClassifier((String) newValue);
        return;
      case 686514815:  // userPortfolioMaster
        setUserPortfolioMaster((PortfolioMaster) newValue);
        return;
      case 1808868758:  // userPositionMaster
        setUserPositionMaster((PositionMaster) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_underlyingPortfolioMaster, "underlyingPortfolioMaster");
    JodaBeanUtils.notNull(_underlyingPositionMaster, "underlyingPositionMaster");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserFinancialPositionSourceComponentFactory other = (UserFinancialPositionSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getUnderlyingClassifier(), other.getUnderlyingClassifier()) &&
          JodaBeanUtils.equal(getUnderlyingPortfolioMaster(), other.getUnderlyingPortfolioMaster()) &&
          JodaBeanUtils.equal(getUnderlyingPositionMaster(), other.getUnderlyingPositionMaster()) &&
          JodaBeanUtils.equal(getUserClassifier(), other.getUserClassifier()) &&
          JodaBeanUtils.equal(getUserPortfolioMaster(), other.getUserPortfolioMaster()) &&
          JodaBeanUtils.equal(getUserPositionMaster(), other.getUserPositionMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlyingClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlyingPortfolioMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlyingPositionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserPortfolioMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserPositionMaster());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under (underlying master).
   * @return the value of the property
   */
  public String getUnderlyingClassifier() {
    return _underlyingClassifier;
  }

  /**
   * Sets the classifier that the factory should publish under (underlying master).
   * @param underlyingClassifier  the new value of the property
   */
  public void setUnderlyingClassifier(String underlyingClassifier) {
    this._underlyingClassifier = underlyingClassifier;
  }

  /**
   * Gets the the {@code underlyingClassifier} property.
   * @return the property, not null
   */
  public final Property<String> underlyingClassifier() {
    return metaBean().underlyingClassifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio master (underlying master).
   * @return the value of the property, not null
   */
  public PortfolioMaster getUnderlyingPortfolioMaster() {
    return _underlyingPortfolioMaster;
  }

  /**
   * Sets the portfolio master (underlying master).
   * @param underlyingPortfolioMaster  the new value of the property, not null
   */
  public void setUnderlyingPortfolioMaster(PortfolioMaster underlyingPortfolioMaster) {
    JodaBeanUtils.notNull(underlyingPortfolioMaster, "underlyingPortfolioMaster");
    this._underlyingPortfolioMaster = underlyingPortfolioMaster;
  }

  /**
   * Gets the the {@code underlyingPortfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> underlyingPortfolioMaster() {
    return metaBean().underlyingPortfolioMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position master (underlying master).
   * @return the value of the property, not null
   */
  public PositionMaster getUnderlyingPositionMaster() {
    return _underlyingPositionMaster;
  }

  /**
   * Sets the position master (underlying master).
   * @param underlyingPositionMaster  the new value of the property, not null
   */
  public void setUnderlyingPositionMaster(PositionMaster underlyingPositionMaster) {
    JodaBeanUtils.notNull(underlyingPositionMaster, "underlyingPositionMaster");
    this._underlyingPositionMaster = underlyingPositionMaster;
  }

  /**
   * Gets the the {@code underlyingPositionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> underlyingPositionMaster() {
    return metaBean().underlyingPositionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under (user master).
   * @return the value of the property
   */
  public String getUserClassifier() {
    return _userClassifier;
  }

  /**
   * Sets the classifier that the factory should publish under (user master).
   * @param userClassifier  the new value of the property
   */
  public void setUserClassifier(String userClassifier) {
    this._userClassifier = userClassifier;
  }

  /**
   * Gets the the {@code userClassifier} property.
   * @return the property, not null
   */
  public final Property<String> userClassifier() {
    return metaBean().userClassifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio master (user master).
   * @return the value of the property
   */
  public PortfolioMaster getUserPortfolioMaster() {
    return _userPortfolioMaster;
  }

  /**
   * Sets the portfolio master (user master).
   * @param userPortfolioMaster  the new value of the property
   */
  public void setUserPortfolioMaster(PortfolioMaster userPortfolioMaster) {
    this._userPortfolioMaster = userPortfolioMaster;
  }

  /**
   * Gets the the {@code userPortfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> userPortfolioMaster() {
    return metaBean().userPortfolioMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position master (user master).
   * @return the value of the property
   */
  public PositionMaster getUserPositionMaster() {
    return _userPositionMaster;
  }

  /**
   * Sets the position master (user master).
   * @param userPositionMaster  the new value of the property
   */
  public void setUserPositionMaster(PositionMaster userPositionMaster) {
    this._userPositionMaster = userPositionMaster;
  }

  /**
   * Gets the the {@code userPositionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> userPositionMaster() {
    return metaBean().userPositionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserFinancialPositionSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", UserFinancialPositionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", UserFinancialPositionSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", UserFinancialPositionSourceComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code underlyingClassifier} property.
     */
    private final MetaProperty<String> _underlyingClassifier = DirectMetaProperty.ofReadWrite(
        this, "underlyingClassifier", UserFinancialPositionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code underlyingPortfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _underlyingPortfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "underlyingPortfolioMaster", UserFinancialPositionSourceComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code underlyingPositionMaster} property.
     */
    private final MetaProperty<PositionMaster> _underlyingPositionMaster = DirectMetaProperty.ofReadWrite(
        this, "underlyingPositionMaster", UserFinancialPositionSourceComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code userClassifier} property.
     */
    private final MetaProperty<String> _userClassifier = DirectMetaProperty.ofReadWrite(
        this, "userClassifier", UserFinancialPositionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code userPortfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _userPortfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "userPortfolioMaster", UserFinancialPositionSourceComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code userPositionMaster} property.
     */
    private final MetaProperty<PositionMaster> _userPositionMaster = DirectMetaProperty.ofReadWrite(
        this, "userPositionMaster", UserFinancialPositionSourceComponentFactory.class, PositionMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "cacheManager",
        "underlyingClassifier",
        "underlyingPortfolioMaster",
        "underlyingPositionMaster",
        "userClassifier",
        "userPortfolioMaster",
        "userPositionMaster");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 1705602398:  // underlyingClassifier
          return _underlyingClassifier;
        case -337956691:  // underlyingPortfolioMaster
          return _underlyingPortfolioMaster;
        case -440936024:  // underlyingPositionMaster
          return _underlyingPositionMaster;
        case 473030732:  // userClassifier
          return _userClassifier;
        case 686514815:  // userPortfolioMaster
          return _userPortfolioMaster;
        case 1808868758:  // userPositionMaster
          return _userPositionMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserFinancialPositionSourceComponentFactory> builder() {
      return new DirectBeanBuilder<UserFinancialPositionSourceComponentFactory>(new UserFinancialPositionSourceComponentFactory());
    }

    @Override
    public Class<? extends UserFinancialPositionSourceComponentFactory> beanType() {
      return UserFinancialPositionSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code underlyingClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> underlyingClassifier() {
      return _underlyingClassifier;
    }

    /**
     * The meta-property for the {@code underlyingPortfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> underlyingPortfolioMaster() {
      return _underlyingPortfolioMaster;
    }

    /**
     * The meta-property for the {@code underlyingPositionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> underlyingPositionMaster() {
      return _underlyingPositionMaster;
    }

    /**
     * The meta-property for the {@code userClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userClassifier() {
      return _userClassifier;
    }

    /**
     * The meta-property for the {@code userPortfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> userPortfolioMaster() {
      return _userPortfolioMaster;
    }

    /**
     * The meta-property for the {@code userPositionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> userPositionMaster() {
      return _userPositionMaster;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
