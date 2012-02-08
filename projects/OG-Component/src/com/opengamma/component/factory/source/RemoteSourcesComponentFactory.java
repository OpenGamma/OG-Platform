/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
import com.opengamma.component.ComponentServer;
import com.opengamma.component.RemoteComponentServer;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.JmsChangeManager;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.RemoteExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.RemotePositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.RemoteRegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.currency.rest.RemoteCurrencyMatrixSource;
import com.opengamma.financial.currency.rest.RemoteCurrencyPairsSource;
import com.opengamma.financial.function.rest.RemoteRepositoryConfigurationSource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component factory for accessing remote sources from the local machine.
 */
@BeanDefinition
public class RemoteSourcesComponentFactory extends AbstractComponentFactory {

  /**
   * The remote wrappers.
   */
  private final Map<Class<?>, Class<?>> _remoteWrappers = new HashMap<Class<?>, Class<?>>();
  {
    _remoteWrappers.put(CurrencyMatrixSource.class, RemoteCurrencyMatrixSource.class);
    _remoteWrappers.put(CurrencyPairsSource.class, RemoteCurrencyPairsSource.class);
    _remoteWrappers.put(ExchangeSource.class, RemoteExchangeSource.class);
    _remoteWrappers.put(HistoricalTimeSeriesSource.class, RemoteHistoricalTimeSeriesSource.class);
    _remoteWrappers.put(HolidaySource.class, RemoteHolidaySource.class);
    _remoteWrappers.put(MarketDataSnapshotSource.class, RemoteMarketDataSnapshotSource.class);
    _remoteWrappers.put(RegionSource.class, RemoteRegionSource.class);
    _remoteWrappers.put(RepositoryConfigurationSource.class, RemoteRepositoryConfigurationSource.class);
    _remoteWrappers.put(PositionSource.class, RemotePositionSource.class);
    _remoteWrappers.put(SecuritySource.class, RemoteFinancialSecuritySource.class);
  }

  /**
   * The remote URI.
   */
  @PropertyDefinition(validate = "notNull")
  private URI _baseUri;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The JMS connector.
   */
  @PropertyDefinition
  private JmsConnector _jmsConnector;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    RemoteComponentServer remote = new RemoteComponentServer(_baseUri);
    ComponentServer server = remote.getComponentServer();
    for (ComponentInfo info : server.getComponentInfos()) {
      initComponent(repo, info);
    }
  }

  /**
   * Initialize the remote component.
   * 
   * @param repo  the local repository, not null
   * @param info  the remote information, not null
   */
  protected void initComponent(ComponentRepository repo, ComponentInfo info) {
    URI componentUri = info.getUri();
    Class<?> remoteType = _remoteWrappers.get(info.getType());
    if (remoteType != null) {
      String jmsTopic = info.getAttributes().get(ComponentInfoAttributes.JMS_CHANGE_MANAGER_TOPIC);
      Object target;
      if (jmsTopic != null) {
        JmsChangeManager changeManager = new JmsChangeManager(_jmsConnector, jmsTopic);
        repo.registerLifecycle(changeManager);
        Constructor<?> con = ReflectionUtils.findConstructor(remoteType, URI.class, ChangeManager.class);
        target = ReflectionUtils.newInstance(con, componentUri, changeManager);
      } else {
        Constructor<?> con = ReflectionUtils.findConstructor(remoteType, URI.class);
        target = ReflectionUtils.newInstance(con, componentUri);
      }
      repo.registerComponent(info, target);
      if (isPublishRest()) {
        repo.getRestComponents().republish(info);
      }
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RemoteSourcesComponentFactory}.
   * @return the meta-bean, not null
   */
  public static RemoteSourcesComponentFactory.Meta meta() {
    return RemoteSourcesComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(RemoteSourcesComponentFactory.Meta.INSTANCE);
  }

  @Override
  public RemoteSourcesComponentFactory.Meta metaBean() {
    return RemoteSourcesComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -332625701:  // baseUri
        return getBaseUri();
      case -614707837:  // publishRest
        return isPublishRest();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -332625701:  // baseUri
        setBaseUri((URI) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
      case -1495762275:  // jmsConnector
        setJmsConnector((JmsConnector) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_baseUri, "baseUri");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RemoteSourcesComponentFactory other = (RemoteSourcesComponentFactory) obj;
      return JodaBeanUtils.equal(getBaseUri(), other.getBaseUri()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getBaseUri());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the remote URI.
   * @return the value of the property, not null
   */
  public URI getBaseUri() {
    return _baseUri;
  }

  /**
   * Sets the remote URI.
   * @param baseUri  the new value of the property, not null
   */
  public void setBaseUri(URI baseUri) {
    JodaBeanUtils.notNull(baseUri, "baseUri");
    this._baseUri = baseUri;
  }

  /**
   * Gets the the {@code baseUri} property.
   * @return the property, not null
   */
  public final Property<URI> baseUri() {
    return metaBean().baseUri().createProperty(this);
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
   * Gets the JMS connector.
   * @return the value of the property
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * @param jmsConnector  the new value of the property
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    this._jmsConnector = jmsConnector;
  }

  /**
   * Gets the the {@code jmsConnector} property.
   * @return the property, not null
   */
  public final Property<JmsConnector> jmsConnector() {
    return metaBean().jmsConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RemoteSourcesComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseUri} property.
     */
    private final MetaProperty<URI> _baseUri = DirectMetaProperty.ofReadWrite(
        this, "baseUri", RemoteSourcesComponentFactory.class, URI.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", RemoteSourcesComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", RemoteSourcesComponentFactory.class, JmsConnector.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "baseUri",
        "publishRest",
        "jmsConnector");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -332625701:  // baseUri
          return _baseUri;
        case -614707837:  // publishRest
          return _publishRest;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RemoteSourcesComponentFactory> builder() {
      return new DirectBeanBuilder<RemoteSourcesComponentFactory>(new RemoteSourcesComponentFactory());
    }

    @Override
    public Class<? extends RemoteSourcesComponentFactory> beanType() {
      return RemoteSourcesComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseUri} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<URI> baseUri() {
      return _baseUri;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
