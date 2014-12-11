/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

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
import org.springframework.beans.factory.FactoryBean;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.currency.ConfigDBCurrencyMatrixSource;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyMatrixResolver;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsResolver;
import com.opengamma.financial.currency.VersionedCurrencyPairsSource;
import com.opengamma.financial.target.ConfigTargetResolver;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetResolver;
import com.opengamma.financial.temptarget.TempTargetSource;

/**
 * Component factory for the target resolver.
 */
@BeanDefinition
public class TargetResolverComponentFactory extends AbstractComponentFactory implements FactoryBean<ComputationTargetResolver> {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The security source.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySource _securitySource;
  /**
   * The position source.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionSource _positionSource;
  /**
   * The temporary targets.
   */
  @PropertyDefinition
  private TempTargetSource _tempTargets;
  /**
   * The cache manager. If set a caching target resolver will be created, omit to not cache.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;
  /**
   * The configuration source.
   */
  @PropertyDefinition
  private ConfigSource _configSource;
  /**
   * The currency matrix source, if omitted a default will wrap the config source.
   */
  @PropertyDefinition
  private CurrencyMatrixSource _currencyMatrixSource;
  /**
   * The currency pairs source, if omitted a default will wrap the config source.
   */
  @PropertyDefinition
  private VersionedCurrencyPairsSource _currencyPairsSource;

  protected ComputationTargetResolver createTargetResolver() {
    final DefaultComputationTargetResolver resolver = new DefaultComputationTargetResolver(getSecuritySource(), getPositionSource());
    initDefaultResolvers(resolver);
    return resolver;
  }

  protected CurrencyMatrixSource createCurrencyMatrixSource() {
    if (getConfigSource() != null) {
      return new ConfigDBCurrencyMatrixSource(getConfigSource());
    } else {
      return null;
    }
  }

  protected CurrencyMatrixSource getOrCreateCurrencyMatrixSource() {
    if (getCurrencyMatrixSource() == null) {
      setCurrencyMatrixSource(createCurrencyMatrixSource());
    }
    return getCurrencyMatrixSource();
  }

  protected VersionedCurrencyPairsSource createCurrencyPairsSource() {
    if (getConfigSource() != null) {
      return new ConfigDBCurrencyPairsSource(getConfigSource());
    } else {
      return null;
    }
  }

  protected VersionedCurrencyPairsSource getOrCreateCurrencyPairsSource() {
    if (getCurrencyPairsSource() == null) {
      setCurrencyPairsSource(createCurrencyPairsSource());
    }
    return getCurrencyPairsSource();
  }

  protected void initDefaultResolvers(final DefaultComputationTargetResolver resolver) {
    if (getConfigSource() != null) {
      // Resolvers for types which don't have wrappers to abstract their provision away from the config source
      ConfigTargetResolver.initResolver(resolver, ViewDefinition.class, getConfigSource());
      ConfigTargetResolver.initResolver(resolver, YieldCurveDefinition.class, getConfigSource());
    }
    if (getOrCreateCurrencyMatrixSource() != null) {
      resolver.addResolver(CurrencyMatrixResolver.TYPE, new CurrencyMatrixResolver(getOrCreateCurrencyMatrixSource()));
    }
    if (getOrCreateCurrencyPairsSource() != null) {
      resolver.addResolver(CurrencyPairs.TYPE, new CurrencyPairsResolver(getOrCreateCurrencyPairsSource()));
    }
    if (getTempTargets() != null) {
      resolver.addResolver(TempTarget.TYPE, new TempTargetResolver(getTempTargets()));
    }
    resolver.addResolver(CurrencyPair.TYPE);
  }

  protected CachingComputationTargetResolver createCachedTargetResolver(final ComputationTargetResolver underlying) {
    return new DefaultCachingComputationTargetResolver(underlying, getCacheManager());
  }

  private ComputationTargetResolver initTargetResolver() {
    ComputationTargetResolver resolver = createTargetResolver();
    if (getCacheManager() != null) {
      resolver = createCachedTargetResolver(resolver);
    }
    return resolver;
  }

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    repo.registerComponent(new ComponentInfo(ComputationTargetResolver.class, getClassifier()), initTargetResolver());
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TargetResolverComponentFactory}.
   * @return the meta-bean, not null
   */
  public static TargetResolverComponentFactory.Meta meta() {
    return TargetResolverComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TargetResolverComponentFactory.Meta.INSTANCE);
  }

  @Override
  public TargetResolverComponentFactory.Meta metaBean() {
    return TargetResolverComponentFactory.Meta.INSTANCE;
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
   * Gets the security source.
   * @return the value of the property, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the security source.
   * @param securitySource  the new value of the property, not null
   */
  public void setSecuritySource(SecuritySource securitySource) {
    JodaBeanUtils.notNull(securitySource, "securitySource");
    this._securitySource = securitySource;
  }

  /**
   * Gets the the {@code securitySource} property.
   * @return the property, not null
   */
  public final Property<SecuritySource> securitySource() {
    return metaBean().securitySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position source.
   * @return the value of the property, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Sets the position source.
   * @param positionSource  the new value of the property, not null
   */
  public void setPositionSource(PositionSource positionSource) {
    JodaBeanUtils.notNull(positionSource, "positionSource");
    this._positionSource = positionSource;
  }

  /**
   * Gets the the {@code positionSource} property.
   * @return the property, not null
   */
  public final Property<PositionSource> positionSource() {
    return metaBean().positionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the temporary targets.
   * @return the value of the property
   */
  public TempTargetSource getTempTargets() {
    return _tempTargets;
  }

  /**
   * Sets the temporary targets.
   * @param tempTargets  the new value of the property
   */
  public void setTempTargets(TempTargetSource tempTargets) {
    this._tempTargets = tempTargets;
  }

  /**
   * Gets the the {@code tempTargets} property.
   * @return the property, not null
   */
  public final Property<TempTargetSource> tempTargets() {
    return metaBean().tempTargets().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager. If set a caching target resolver will be created, omit to not cache.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager. If set a caching target resolver will be created, omit to not cache.
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
   * Gets the configuration source.
   * @return the value of the property
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the configuration source.
   * @param configSource  the new value of the property
   */
  public void setConfigSource(ConfigSource configSource) {
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency matrix source, if omitted a default will wrap the config source.
   * @return the value of the property
   */
  public CurrencyMatrixSource getCurrencyMatrixSource() {
    return _currencyMatrixSource;
  }

  /**
   * Sets the currency matrix source, if omitted a default will wrap the config source.
   * @param currencyMatrixSource  the new value of the property
   */
  public void setCurrencyMatrixSource(CurrencyMatrixSource currencyMatrixSource) {
    this._currencyMatrixSource = currencyMatrixSource;
  }

  /**
   * Gets the the {@code currencyMatrixSource} property.
   * @return the property, not null
   */
  public final Property<CurrencyMatrixSource> currencyMatrixSource() {
    return metaBean().currencyMatrixSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency pairs source, if omitted a default will wrap the config source.
   * @return the value of the property
   */
  public VersionedCurrencyPairsSource getCurrencyPairsSource() {
    return _currencyPairsSource;
  }

  /**
   * Sets the currency pairs source, if omitted a default will wrap the config source.
   * @param currencyPairsSource  the new value of the property
   */
  public void setCurrencyPairsSource(VersionedCurrencyPairsSource currencyPairsSource) {
    this._currencyPairsSource = currencyPairsSource;
  }

  /**
   * Gets the the {@code currencyPairsSource} property.
   * @return the property, not null
   */
  public final Property<VersionedCurrencyPairsSource> currencyPairsSource() {
    return metaBean().currencyPairsSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public TargetResolverComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TargetResolverComponentFactory other = (TargetResolverComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getTempTargets(), other.getTempTargets()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(getCurrencyMatrixSource(), other.getCurrencyMatrixSource()) &&
          JodaBeanUtils.equal(getCurrencyPairsSource(), other.getCurrencyPairsSource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTempTargets());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrencyMatrixSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrencyPairsSource());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("TargetResolverComponentFactory{");
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
    buf.append("securitySource").append('=').append(JodaBeanUtils.toString(getSecuritySource())).append(',').append(' ');
    buf.append("positionSource").append('=').append(JodaBeanUtils.toString(getPositionSource())).append(',').append(' ');
    buf.append("tempTargets").append('=').append(JodaBeanUtils.toString(getTempTargets())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
    buf.append("currencyMatrixSource").append('=').append(JodaBeanUtils.toString(getCurrencyMatrixSource())).append(',').append(' ');
    buf.append("currencyPairsSource").append('=').append(JodaBeanUtils.toString(getCurrencyPairsSource())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TargetResolverComponentFactory}.
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
        this, "classifier", TargetResolverComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", TargetResolverComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code positionSource} property.
     */
    private final MetaProperty<PositionSource> _positionSource = DirectMetaProperty.ofReadWrite(
        this, "positionSource", TargetResolverComponentFactory.class, PositionSource.class);
    /**
     * The meta-property for the {@code tempTargets} property.
     */
    private final MetaProperty<TempTargetSource> _tempTargets = DirectMetaProperty.ofReadWrite(
        this, "tempTargets", TargetResolverComponentFactory.class, TempTargetSource.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", TargetResolverComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", TargetResolverComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code currencyMatrixSource} property.
     */
    private final MetaProperty<CurrencyMatrixSource> _currencyMatrixSource = DirectMetaProperty.ofReadWrite(
        this, "currencyMatrixSource", TargetResolverComponentFactory.class, CurrencyMatrixSource.class);
    /**
     * The meta-property for the {@code currencyPairsSource} property.
     */
    private final MetaProperty<VersionedCurrencyPairsSource> _currencyPairsSource = DirectMetaProperty.ofReadWrite(
        this, "currencyPairsSource", TargetResolverComponentFactory.class, VersionedCurrencyPairsSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "securitySource",
        "positionSource",
        "tempTargets",
        "cacheManager",
        "configSource",
        "currencyMatrixSource",
        "currencyPairsSource");

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
        case -702456965:  // securitySource
          return _securitySource;
        case -1655657820:  // positionSource
          return _positionSource;
        case 1942609550:  // tempTargets
          return _tempTargets;
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 195157501:  // configSource
          return _configSource;
        case 615188973:  // currencyMatrixSource
          return _currencyMatrixSource;
        case -1615906429:  // currencyPairsSource
          return _currencyPairsSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TargetResolverComponentFactory> builder() {
      return new DirectBeanBuilder<TargetResolverComponentFactory>(new TargetResolverComponentFactory());
    }

    @Override
    public Class<? extends TargetResolverComponentFactory> beanType() {
      return TargetResolverComponentFactory.class;
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
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code positionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionSource> positionSource() {
      return _positionSource;
    }

    /**
     * The meta-property for the {@code tempTargets} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<TempTargetSource> tempTargets() {
      return _tempTargets;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    /**
     * The meta-property for the {@code currencyMatrixSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyMatrixSource> currencyMatrixSource() {
      return _currencyMatrixSource;
    }

    /**
     * The meta-property for the {@code currencyPairsSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VersionedCurrencyPairsSource> currencyPairsSource() {
      return _currencyPairsSource;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((TargetResolverComponentFactory) bean).getClassifier();
        case -702456965:  // securitySource
          return ((TargetResolverComponentFactory) bean).getSecuritySource();
        case -1655657820:  // positionSource
          return ((TargetResolverComponentFactory) bean).getPositionSource();
        case 1942609550:  // tempTargets
          return ((TargetResolverComponentFactory) bean).getTempTargets();
        case -1452875317:  // cacheManager
          return ((TargetResolverComponentFactory) bean).getCacheManager();
        case 195157501:  // configSource
          return ((TargetResolverComponentFactory) bean).getConfigSource();
        case 615188973:  // currencyMatrixSource
          return ((TargetResolverComponentFactory) bean).getCurrencyMatrixSource();
        case -1615906429:  // currencyPairsSource
          return ((TargetResolverComponentFactory) bean).getCurrencyPairsSource();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((TargetResolverComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -702456965:  // securitySource
          ((TargetResolverComponentFactory) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case -1655657820:  // positionSource
          ((TargetResolverComponentFactory) bean).setPositionSource((PositionSource) newValue);
          return;
        case 1942609550:  // tempTargets
          ((TargetResolverComponentFactory) bean).setTempTargets((TempTargetSource) newValue);
          return;
        case -1452875317:  // cacheManager
          ((TargetResolverComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 195157501:  // configSource
          ((TargetResolverComponentFactory) bean).setConfigSource((ConfigSource) newValue);
          return;
        case 615188973:  // currencyMatrixSource
          ((TargetResolverComponentFactory) bean).setCurrencyMatrixSource((CurrencyMatrixSource) newValue);
          return;
        case -1615906429:  // currencyPairsSource
          ((TargetResolverComponentFactory) bean).setCurrencyPairsSource((VersionedCurrencyPairsSource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((TargetResolverComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((TargetResolverComponentFactory) bean)._securitySource, "securitySource");
      JodaBeanUtils.notNull(((TargetResolverComponentFactory) bean)._positionSource, "positionSource");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

  // FactoryBean

  @Override
  public ComputationTargetResolver getObject() throws Exception {
    return initTargetResolver();
  }

  @Override
  public Class<?> getObjectType() {
    return ComputationTargetResolver.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
