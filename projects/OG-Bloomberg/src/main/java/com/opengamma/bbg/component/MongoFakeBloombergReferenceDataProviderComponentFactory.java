/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;
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

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.config.BloombergFieldOverride;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.cache.EHValueCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.InMemoryInvalidFieldCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.MongoDBInvalidFieldCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.MongoDBValueCachingReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.DataReferenceDataProviderResource;
import com.opengamma.bbg.referencedata.impl.PatchableReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.RemoteReferenceDataProvider;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.mongo.MongoConnector;

/**
 * Component factory for the reference data provider backed by a pre-populated MongoDB rather than BBG.
 */
@BeanDefinition
public class MongoFakeBloombergReferenceDataProviderComponentFactory extends AbstractComponentFactory {

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
   * The Mongo connector.
   * If a Mongo connector is specified, then it is used for caching.
   */
  @PropertyDefinition
  private MongoConnector _mongoConnector;
  /**
   * The cache manager.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;
  
  /**
   * A config source. If specified, overrides will be pulled from
   * here.
   */
  @PropertyDefinition
  private ConfigSource _configSource;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    final ReferenceDataProvider provider = initReferenceDataProvider(repo);
    final ComponentInfo info = new ComponentInfo(ReferenceDataProvider.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteReferenceDataProvider.class);
    repo.registerComponent(info, provider);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataReferenceDataProviderResource(provider));
    }
  }

  /**
   * Creates the provider.
   * 
   * @param repo  the repository, not null
   * @return the provider, not null
   */
  @SuppressWarnings("unchecked")
  protected ReferenceDataProvider initReferenceDataProvider(ComponentRepository repo) {
    ReferenceDataProvider underlying = mock(ReferenceDataProvider.class);
    when(underlying.getReferenceData(any(ReferenceDataProviderGetRequest.class))).thenReturn(new ReferenceDataProviderGetResult(Collections.<ReferenceData>emptyList()));
    when(underlying.getReferenceData(any(Iterable.class), any(Iterable.class))).thenReturn(new HashMap<String, FudgeMsg>());
    when(underlying.getReferenceDataIgnoreCache(any(Iterable.class), any(Iterable.class))).thenReturn(new HashMap<String, FudgeMsg>());
    when(underlying.getReferenceDataValue(anyString(), anyString())).thenReturn(null);
    when(underlying.getReferenceDataValues(any(Iterable.class), anyString())).thenReturn(new HashMap<String, String>());
    when(underlying.getReferenceDataValues(anyString(), any(Iterable.class))).thenReturn(new HashMap<String, String>());
    
    ReferenceDataProvider effectiveProvider = underlying;
    if (getConfigSource() != null) {
      effectiveProvider = applyFieldOverrides(effectiveProvider);
    }
    
    MongoConnector mongoConnector = getMongoConnector();
    CacheManager cacheManager = getCacheManager();
    if (mongoConnector != null) {
      MongoDBInvalidFieldCachingReferenceDataProvider fieldCached = new MongoDBInvalidFieldCachingReferenceDataProvider(effectiveProvider, mongoConnector);
      return new MongoDBValueCachingReferenceDataProvider(fieldCached, mongoConnector);
      
    } else if (cacheManager != null) {
      ReferenceDataProvider fieldCached = new InMemoryInvalidFieldCachingReferenceDataProvider(effectiveProvider);  // TODO: EHcached version
      return new EHValueCachingReferenceDataProvider(fieldCached, cacheManager);
      
    } else {
      return new InMemoryInvalidFieldCachingReferenceDataProvider(effectiveProvider);
    }
  }

  /**
   * Loads overrides from the config source and applies them to the passed 
   * reference data provider via a wrapper (a {@link PatchableReferenceDataProvider}).
   * @param underlying the provider to patch
   * @return a patched provider
   */
  private PatchableReferenceDataProvider applyFieldOverrides(ReferenceDataProvider underlying) {
    Collection<ConfigItem<BloombergFieldOverride>> overrideItems = getConfigSource().getAll(BloombergFieldOverride.class, VersionCorrection.LATEST);
    
    PatchableReferenceDataProvider patchableReferenceDataProvider = new PatchableReferenceDataProvider(underlying);
    
    for (ConfigItem<BloombergFieldOverride> configItem : overrideItems) {
      BloombergFieldOverride fieldOverride = configItem.getValue();
      
      patchableReferenceDataProvider.setPatch(fieldOverride.getBloombergId(), fieldOverride.getFieldName(), fieldOverride.getOverrideValue());
      
    }
    return patchableReferenceDataProvider;


  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MongoFakeBloombergReferenceDataProviderComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MongoFakeBloombergReferenceDataProviderComponentFactory.Meta meta() {
    return MongoFakeBloombergReferenceDataProviderComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MongoFakeBloombergReferenceDataProviderComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MongoFakeBloombergReferenceDataProviderComponentFactory.Meta metaBean() {
    return MongoFakeBloombergReferenceDataProviderComponentFactory.Meta.INSTANCE;
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
   * Gets the Mongo connector.
   * If a Mongo connector is specified, then it is used for caching.
   * @return the value of the property
   */
  public MongoConnector getMongoConnector() {
    return _mongoConnector;
  }

  /**
   * Sets the Mongo connector.
   * If a Mongo connector is specified, then it is used for caching.
   * @param mongoConnector  the new value of the property
   */
  public void setMongoConnector(MongoConnector mongoConnector) {
    this._mongoConnector = mongoConnector;
  }

  /**
   * Gets the the {@code mongoConnector} property.
   * If a Mongo connector is specified, then it is used for caching.
   * @return the property, not null
   */
  public final Property<MongoConnector> mongoConnector() {
    return metaBean().mongoConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a config source. If specified, overrides will be pulled from
   * here.
   * @return the value of the property
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets a config source. If specified, overrides will be pulled from
   * here.
   * @param configSource  the new value of the property
   */
  public void setConfigSource(ConfigSource configSource) {
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * here.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MongoFakeBloombergReferenceDataProviderComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MongoFakeBloombergReferenceDataProviderComponentFactory other = (MongoFakeBloombergReferenceDataProviderComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getMongoConnector(), other.getMongoConnector()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMongoConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("MongoFakeBloombergReferenceDataProviderComponentFactory{");
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
    buf.append("mongoConnector").append('=').append(JodaBeanUtils.toString(getMongoConnector())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MongoFakeBloombergReferenceDataProviderComponentFactory}.
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
        this, "classifier", MongoFakeBloombergReferenceDataProviderComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", MongoFakeBloombergReferenceDataProviderComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code mongoConnector} property.
     */
    private final MetaProperty<MongoConnector> _mongoConnector = DirectMetaProperty.ofReadWrite(
        this, "mongoConnector", MongoFakeBloombergReferenceDataProviderComponentFactory.class, MongoConnector.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", MongoFakeBloombergReferenceDataProviderComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", MongoFakeBloombergReferenceDataProviderComponentFactory.class, ConfigSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "mongoConnector",
        "cacheManager",
        "configSource");

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
        case 224118201:  // mongoConnector
          return _mongoConnector;
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 195157501:  // configSource
          return _configSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MongoFakeBloombergReferenceDataProviderComponentFactory> builder() {
      return new DirectBeanBuilder<MongoFakeBloombergReferenceDataProviderComponentFactory>(new MongoFakeBloombergReferenceDataProviderComponentFactory());
    }

    @Override
    public Class<? extends MongoFakeBloombergReferenceDataProviderComponentFactory> beanType() {
      return MongoFakeBloombergReferenceDataProviderComponentFactory.class;
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
     * The meta-property for the {@code mongoConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MongoConnector> mongoConnector() {
      return _mongoConnector;
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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).isPublishRest();
        case 224118201:  // mongoConnector
          return ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).getMongoConnector();
        case -1452875317:  // cacheManager
          return ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).getCacheManager();
        case 195157501:  // configSource
          return ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).getConfigSource();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case 224118201:  // mongoConnector
          ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).setMongoConnector((MongoConnector) newValue);
          return;
        case -1452875317:  // cacheManager
          ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 195157501:  // configSource
          ((MongoFakeBloombergReferenceDataProviderComponentFactory) bean).setConfigSource((ConfigSource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((MongoFakeBloombergReferenceDataProviderComponentFactory) bean)._classifier, "classifier");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
