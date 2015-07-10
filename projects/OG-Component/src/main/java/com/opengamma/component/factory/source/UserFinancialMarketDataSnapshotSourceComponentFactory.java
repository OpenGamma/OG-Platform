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

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.DataMarketDataSnapshotSourceResource;
import com.opengamma.core.marketdatasnapshot.impl.DelegatingSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.MasterSnapshotSource;

/**
 * Component factory for the snapshot source.
 * <p>
 * This factory creates snapshot sources for the underlying and user masters
 * as well as a combined source.
 */
@BeanDefinition
public class UserFinancialMarketDataSnapshotSourceComponentFactory extends AbstractComponentFactory {

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
   * The snapshot master (underlying master).
   */
  @PropertyDefinition(validate = "notNull")
  private MarketDataSnapshotMaster _underlyingMarketDataSnapshotMaster;
  /**
   * The classifier that the factory should publish under (user master).
   */
  @PropertyDefinition
  private String _userClassifier;
  /**
   * The snapshot master (user master).
   */
  @PropertyDefinition
  private MarketDataSnapshotMaster _userMarketDataSnapshotMaster;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    MarketDataSnapshotSource source = initUnderlying(repo, configuration);
    
    // add user level if requested
    MarketDataSnapshotSource userSource = initUser(repo, configuration);
    Map<String, MarketDataSnapshotSource> map = new HashMap<String, MarketDataSnapshotSource>();
    if (userSource != null) {
      String scheme = repo.getInfo(getUserMarketDataSnapshotMaster()).getAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME);
      map.put(scheme, userSource);
      source = new DelegatingSnapshotSource(source, map);
    }
    
    // register
    ComponentInfo info = new ComponentInfo(MarketDataSnapshotSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 2);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteMarketDataSnapshotSource.class);
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataMarketDataSnapshotSourceResource(source));
    }
  }

  protected MarketDataSnapshotSource initUnderlying(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    MarketDataSnapshotSource source = new MasterSnapshotSource(getUnderlyingMarketDataSnapshotMaster());
    if (getUnderlyingClassifier() != null) {
      ComponentInfo info = new ComponentInfo(MarketDataSnapshotSource.class, getUnderlyingClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteMarketDataSnapshotSource.class);
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataMarketDataSnapshotSourceResource(source));
      }
    }
    return source;
  }

  protected MarketDataSnapshotSource initUser(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    if (getUserMarketDataSnapshotMaster() == null) {
      return null;
    }
    MarketDataSnapshotSource source = new MasterSnapshotSource(getUserMarketDataSnapshotMaster());
    if (getUserClassifier() != null) {
      ComponentInfo info = new ComponentInfo(MarketDataSnapshotSource.class, getUserClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteMarketDataSnapshotSource.class);
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataMarketDataSnapshotSourceResource(source));
      }
    }
    return source;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserFinancialMarketDataSnapshotSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static UserFinancialMarketDataSnapshotSourceComponentFactory.Meta meta() {
    return UserFinancialMarketDataSnapshotSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserFinancialMarketDataSnapshotSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public UserFinancialMarketDataSnapshotSourceComponentFactory.Meta metaBean() {
    return UserFinancialMarketDataSnapshotSourceComponentFactory.Meta.INSTANCE;
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
   * Gets the snapshot master (underlying master).
   * @return the value of the property, not null
   */
  public MarketDataSnapshotMaster getUnderlyingMarketDataSnapshotMaster() {
    return _underlyingMarketDataSnapshotMaster;
  }

  /**
   * Sets the snapshot master (underlying master).
   * @param underlyingMarketDataSnapshotMaster  the new value of the property, not null
   */
  public void setUnderlyingMarketDataSnapshotMaster(MarketDataSnapshotMaster underlyingMarketDataSnapshotMaster) {
    JodaBeanUtils.notNull(underlyingMarketDataSnapshotMaster, "underlyingMarketDataSnapshotMaster");
    this._underlyingMarketDataSnapshotMaster = underlyingMarketDataSnapshotMaster;
  }

  /**
   * Gets the the {@code underlyingMarketDataSnapshotMaster} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotMaster> underlyingMarketDataSnapshotMaster() {
    return metaBean().underlyingMarketDataSnapshotMaster().createProperty(this);
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
   * Gets the snapshot master (user master).
   * @return the value of the property
   */
  public MarketDataSnapshotMaster getUserMarketDataSnapshotMaster() {
    return _userMarketDataSnapshotMaster;
  }

  /**
   * Sets the snapshot master (user master).
   * @param userMarketDataSnapshotMaster  the new value of the property
   */
  public void setUserMarketDataSnapshotMaster(MarketDataSnapshotMaster userMarketDataSnapshotMaster) {
    this._userMarketDataSnapshotMaster = userMarketDataSnapshotMaster;
  }

  /**
   * Gets the the {@code userMarketDataSnapshotMaster} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotMaster> userMarketDataSnapshotMaster() {
    return metaBean().userMarketDataSnapshotMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserFinancialMarketDataSnapshotSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserFinancialMarketDataSnapshotSourceComponentFactory other = (UserFinancialMarketDataSnapshotSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getUnderlyingClassifier(), other.getUnderlyingClassifier()) &&
          JodaBeanUtils.equal(getUnderlyingMarketDataSnapshotMaster(), other.getUnderlyingMarketDataSnapshotMaster()) &&
          JodaBeanUtils.equal(getUserClassifier(), other.getUserClassifier()) &&
          JodaBeanUtils.equal(getUserMarketDataSnapshotMaster(), other.getUserMarketDataSnapshotMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingMarketDataSnapshotMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserMarketDataSnapshotMaster());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("UserFinancialMarketDataSnapshotSourceComponentFactory{");
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
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("underlyingClassifier").append('=').append(JodaBeanUtils.toString(getUnderlyingClassifier())).append(',').append(' ');
    buf.append("underlyingMarketDataSnapshotMaster").append('=').append(JodaBeanUtils.toString(getUnderlyingMarketDataSnapshotMaster())).append(',').append(' ');
    buf.append("userClassifier").append('=').append(JodaBeanUtils.toString(getUserClassifier())).append(',').append(' ');
    buf.append("userMarketDataSnapshotMaster").append('=').append(JodaBeanUtils.toString(getUserMarketDataSnapshotMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserFinancialMarketDataSnapshotSourceComponentFactory}.
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
        this, "classifier", UserFinancialMarketDataSnapshotSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", UserFinancialMarketDataSnapshotSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", UserFinancialMarketDataSnapshotSourceComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code underlyingClassifier} property.
     */
    private final MetaProperty<String> _underlyingClassifier = DirectMetaProperty.ofReadWrite(
        this, "underlyingClassifier", UserFinancialMarketDataSnapshotSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code underlyingMarketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _underlyingMarketDataSnapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "underlyingMarketDataSnapshotMaster", UserFinancialMarketDataSnapshotSourceComponentFactory.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code userClassifier} property.
     */
    private final MetaProperty<String> _userClassifier = DirectMetaProperty.ofReadWrite(
        this, "userClassifier", UserFinancialMarketDataSnapshotSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code userMarketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _userMarketDataSnapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "userMarketDataSnapshotMaster", UserFinancialMarketDataSnapshotSourceComponentFactory.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "cacheManager",
        "underlyingClassifier",
        "underlyingMarketDataSnapshotMaster",
        "userClassifier",
        "userMarketDataSnapshotMaster");

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
        case 2076717417:  // underlyingMarketDataSnapshotMaster
          return _underlyingMarketDataSnapshotMaster;
        case 473030732:  // userClassifier
          return _userClassifier;
        case 1671293911:  // userMarketDataSnapshotMaster
          return _userMarketDataSnapshotMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserFinancialMarketDataSnapshotSourceComponentFactory> builder() {
      return new DirectBeanBuilder<UserFinancialMarketDataSnapshotSourceComponentFactory>(new UserFinancialMarketDataSnapshotSourceComponentFactory());
    }

    @Override
    public Class<? extends UserFinancialMarketDataSnapshotSourceComponentFactory> beanType() {
      return UserFinancialMarketDataSnapshotSourceComponentFactory.class;
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
     * The meta-property for the {@code underlyingMarketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> underlyingMarketDataSnapshotMaster() {
      return _underlyingMarketDataSnapshotMaster;
    }

    /**
     * The meta-property for the {@code userClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userClassifier() {
      return _userClassifier;
    }

    /**
     * The meta-property for the {@code userMarketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> userMarketDataSnapshotMaster() {
      return _userMarketDataSnapshotMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).isPublishRest();
        case -1452875317:  // cacheManager
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).getCacheManager();
        case 1705602398:  // underlyingClassifier
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).getUnderlyingClassifier();
        case 2076717417:  // underlyingMarketDataSnapshotMaster
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).getUnderlyingMarketDataSnapshotMaster();
        case 473030732:  // userClassifier
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).getUserClassifier();
        case 1671293911:  // userMarketDataSnapshotMaster
          return ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).getUserMarketDataSnapshotMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1452875317:  // cacheManager
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 1705602398:  // underlyingClassifier
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setUnderlyingClassifier((String) newValue);
          return;
        case 2076717417:  // underlyingMarketDataSnapshotMaster
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setUnderlyingMarketDataSnapshotMaster((MarketDataSnapshotMaster) newValue);
          return;
        case 473030732:  // userClassifier
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setUserClassifier((String) newValue);
          return;
        case 1671293911:  // userMarketDataSnapshotMaster
          ((UserFinancialMarketDataSnapshotSourceComponentFactory) bean).setUserMarketDataSnapshotMaster((MarketDataSnapshotMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((UserFinancialMarketDataSnapshotSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((UserFinancialMarketDataSnapshotSourceComponentFactory) bean)._underlyingMarketDataSnapshotMaster, "underlyingMarketDataSnapshotMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
