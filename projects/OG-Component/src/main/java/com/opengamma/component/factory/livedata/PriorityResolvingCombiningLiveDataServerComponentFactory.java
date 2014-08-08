/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.livedata;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.text.StrBuilder;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.LiveDataServerMBean;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.combining.PriorityResolvingCombiningLiveDataServer;
import com.opengamma.livedata.server.distribution.JmsSenderFactory;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataServerTypes;

/**
 * Component factory to create a combining live data server.
 */
@BeanDefinition
public class PriorityResolvingCombiningLiveDataServerComponentFactory extends AbstractStandardLiveDataServerComponentFactory {

  /**
   * The cache manager.
   */
  @PropertyDefinition(validate = "notNull")
  private CacheManager _cacheManager;
  /**
   * The first server.
   */
  @PropertyDefinition(validate = "notNull")
  private ComponentInfo _server1;
  /**
   * The second server.
   */
  @PropertyDefinition
  private ComponentInfo _server2;
  /**
   * The third server.
   */
  @PropertyDefinition
  private ComponentInfo _server3;
  /**
   * The fourth server.
   */
  @PropertyDefinition
  private ComponentInfo _server4;
  /**
   * The fifth server.
   */
  @PropertyDefinition
  private ComponentInfo _server5;
  /**
   * The sixth server.
   */
  @PropertyDefinition
  private ComponentInfo _server6;

  //-------------------------------------------------------------------------
  @Override
  protected StandardLiveDataServer initServer(ComponentRepository repo) {
    List<ComponentInfo> infos = buildInfoList();
    List<StandardLiveDataServer> servers = Lists.newArrayList();
    for (ComponentInfo info : infos) {
      servers.add((StandardLiveDataServer) repo.getInstance(info));
    }
    PriorityResolvingCombiningLiveDataServer combinedServer = new PriorityResolvingCombiningLiveDataServer(servers, getCacheManager());
    configureServerPlugins(repo, combinedServer);
    repo.registerMBean(new LiveDataServerMBean(combinedServer));
    return combinedServer;
  }

  /**
   * Configures any plugins that alter the behavior of the basic server.
   * 
   * @param repo  the repository, not null
   * @param server  the server being configured, not null
   */
  protected void configureServerPlugins(ComponentRepository repo, PriorityResolvingCombiningLiveDataServer server) {
    DistributionSpecificationResolver resolver = server.getDefaultDistributionSpecificationResolver();
    
    LiveDataEntitlementChecker entitlementChecker = new PermissiveLiveDataEntitlementChecker();
    
    JmsSenderFactory senderFactory = new JmsSenderFactory(getJmsConnector());
    
    server.setDistributionSpecificationResolver(resolver);
    server.setEntitlementChecker(entitlementChecker);
    server.setMarketDataSenderFactory(senderFactory);
  }

  @Override
  protected LiveDataMetaData createMetaData(ComponentRepository repo) {
    List<ComponentInfo> infos = buildInfoList();
    Set<ExternalScheme> schemes = Sets.newLinkedHashSet();
    StrBuilder buf = new StrBuilder();
    for (ComponentInfo info : infos) {
      ComponentInfo infoProvider = repo.findInfo(LiveDataMetaDataProvider.class, info.getClassifier());
      if (infoProvider == null) {
        throw new OpenGammaRuntimeException("Unable to find matching LiveDataMetaDataProvider: " + info);
      }
      LiveDataMetaDataProvider provider = (LiveDataMetaDataProvider) repo.getInstance(infoProvider);
      LiveDataMetaData metaData = provider.metaData();
      schemes.addAll(metaData.getSupportedSchemes());
      buf.appendSeparator(", ").append(metaData.getDescription());
    }
    return new LiveDataMetaData(ImmutableList.copyOf(schemes), LiveDataServerTypes.STANDARD, buf.toString());
  }

  /**
   * Builds the list of server infos.
   * 
   * @return the server list, not null
   */
  protected List<ComponentInfo> buildInfoList() {
    List<ComponentInfo> infos = Lists.newArrayList();
    infos.add(getServer1());
    infos.add(getServer2());
    infos.add(getServer3());
    infos.add(getServer4());
    infos.add(getServer5());
    infos.add(getServer6());
    infos.removeAll(Collections.singleton(null));
    return infos;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PriorityResolvingCombiningLiveDataServerComponentFactory}.
   * @return the meta-bean, not null
   */
  public static PriorityResolvingCombiningLiveDataServerComponentFactory.Meta meta() {
    return PriorityResolvingCombiningLiveDataServerComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(PriorityResolvingCombiningLiveDataServerComponentFactory.Meta.INSTANCE);
  }

  @Override
  public PriorityResolvingCombiningLiveDataServerComponentFactory.Meta metaBean() {
    return PriorityResolvingCombiningLiveDataServerComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * @return the value of the property, not null
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager.
   * @param cacheManager  the new value of the property, not null
   */
  public void setCacheManager(CacheManager cacheManager) {
    JodaBeanUtils.notNull(cacheManager, "cacheManager");
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
   * Gets the first server.
   * @return the value of the property, not null
   */
  public ComponentInfo getServer1() {
    return _server1;
  }

  /**
   * Sets the first server.
   * @param server1  the new value of the property, not null
   */
  public void setServer1(ComponentInfo server1) {
    JodaBeanUtils.notNull(server1, "server1");
    this._server1 = server1;
  }

  /**
   * Gets the the {@code server1} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> server1() {
    return metaBean().server1().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the second server.
   * @return the value of the property
   */
  public ComponentInfo getServer2() {
    return _server2;
  }

  /**
   * Sets the second server.
   * @param server2  the new value of the property
   */
  public void setServer2(ComponentInfo server2) {
    this._server2 = server2;
  }

  /**
   * Gets the the {@code server2} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> server2() {
    return metaBean().server2().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the third server.
   * @return the value of the property
   */
  public ComponentInfo getServer3() {
    return _server3;
  }

  /**
   * Sets the third server.
   * @param server3  the new value of the property
   */
  public void setServer3(ComponentInfo server3) {
    this._server3 = server3;
  }

  /**
   * Gets the the {@code server3} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> server3() {
    return metaBean().server3().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fourth server.
   * @return the value of the property
   */
  public ComponentInfo getServer4() {
    return _server4;
  }

  /**
   * Sets the fourth server.
   * @param server4  the new value of the property
   */
  public void setServer4(ComponentInfo server4) {
    this._server4 = server4;
  }

  /**
   * Gets the the {@code server4} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> server4() {
    return metaBean().server4().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fifth server.
   * @return the value of the property
   */
  public ComponentInfo getServer5() {
    return _server5;
  }

  /**
   * Sets the fifth server.
   * @param server5  the new value of the property
   */
  public void setServer5(ComponentInfo server5) {
    this._server5 = server5;
  }

  /**
   * Gets the the {@code server5} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> server5() {
    return metaBean().server5().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sixth server.
   * @return the value of the property
   */
  public ComponentInfo getServer6() {
    return _server6;
  }

  /**
   * Sets the sixth server.
   * @param server6  the new value of the property
   */
  public void setServer6(ComponentInfo server6) {
    this._server6 = server6;
  }

  /**
   * Gets the the {@code server6} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> server6() {
    return metaBean().server6().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public PriorityResolvingCombiningLiveDataServerComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PriorityResolvingCombiningLiveDataServerComponentFactory other = (PriorityResolvingCombiningLiveDataServerComponentFactory) obj;
      return JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getServer1(), other.getServer1()) &&
          JodaBeanUtils.equal(getServer2(), other.getServer2()) &&
          JodaBeanUtils.equal(getServer3(), other.getServer3()) &&
          JodaBeanUtils.equal(getServer4(), other.getServer4()) &&
          JodaBeanUtils.equal(getServer5(), other.getServer5()) &&
          JodaBeanUtils.equal(getServer6(), other.getServer6()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServer1());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServer2());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServer3());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServer4());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServer5());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServer6());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("PriorityResolvingCombiningLiveDataServerComponentFactory{");
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
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("server1").append('=').append(JodaBeanUtils.toString(getServer1())).append(',').append(' ');
    buf.append("server2").append('=').append(JodaBeanUtils.toString(getServer2())).append(',').append(' ');
    buf.append("server3").append('=').append(JodaBeanUtils.toString(getServer3())).append(',').append(' ');
    buf.append("server4").append('=').append(JodaBeanUtils.toString(getServer4())).append(',').append(' ');
    buf.append("server5").append('=').append(JodaBeanUtils.toString(getServer5())).append(',').append(' ');
    buf.append("server6").append('=').append(JodaBeanUtils.toString(getServer6())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PriorityResolvingCombiningLiveDataServerComponentFactory}.
   */
  public static class Meta extends AbstractStandardLiveDataServerComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", PriorityResolvingCombiningLiveDataServerComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code server1} property.
     */
    private final MetaProperty<ComponentInfo> _server1 = DirectMetaProperty.ofReadWrite(
        this, "server1", PriorityResolvingCombiningLiveDataServerComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code server2} property.
     */
    private final MetaProperty<ComponentInfo> _server2 = DirectMetaProperty.ofReadWrite(
        this, "server2", PriorityResolvingCombiningLiveDataServerComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code server3} property.
     */
    private final MetaProperty<ComponentInfo> _server3 = DirectMetaProperty.ofReadWrite(
        this, "server3", PriorityResolvingCombiningLiveDataServerComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code server4} property.
     */
    private final MetaProperty<ComponentInfo> _server4 = DirectMetaProperty.ofReadWrite(
        this, "server4", PriorityResolvingCombiningLiveDataServerComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code server5} property.
     */
    private final MetaProperty<ComponentInfo> _server5 = DirectMetaProperty.ofReadWrite(
        this, "server5", PriorityResolvingCombiningLiveDataServerComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code server6} property.
     */
    private final MetaProperty<ComponentInfo> _server6 = DirectMetaProperty.ofReadWrite(
        this, "server6", PriorityResolvingCombiningLiveDataServerComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "cacheManager",
        "server1",
        "server2",
        "server3",
        "server4",
        "server5",
        "server6");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 1984149838:  // server1
          return _server1;
        case 1984149839:  // server2
          return _server2;
        case 1984149840:  // server3
          return _server3;
        case 1984149841:  // server4
          return _server4;
        case 1984149842:  // server5
          return _server5;
        case 1984149843:  // server6
          return _server6;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PriorityResolvingCombiningLiveDataServerComponentFactory> builder() {
      return new DirectBeanBuilder<PriorityResolvingCombiningLiveDataServerComponentFactory>(new PriorityResolvingCombiningLiveDataServerComponentFactory());
    }

    @Override
    public Class<? extends PriorityResolvingCombiningLiveDataServerComponentFactory> beanType() {
      return PriorityResolvingCombiningLiveDataServerComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code server1} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> server1() {
      return _server1;
    }

    /**
     * The meta-property for the {@code server2} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> server2() {
      return _server2;
    }

    /**
     * The meta-property for the {@code server3} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> server3() {
      return _server3;
    }

    /**
     * The meta-property for the {@code server4} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> server4() {
      return _server4;
    }

    /**
     * The meta-property for the {@code server5} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> server5() {
      return _server5;
    }

    /**
     * The meta-property for the {@code server6} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> server6() {
      return _server6;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1452875317:  // cacheManager
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getCacheManager();
        case 1984149838:  // server1
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getServer1();
        case 1984149839:  // server2
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getServer2();
        case 1984149840:  // server3
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getServer3();
        case 1984149841:  // server4
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getServer4();
        case 1984149842:  // server5
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getServer5();
        case 1984149843:  // server6
          return ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).getServer6();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1452875317:  // cacheManager
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 1984149838:  // server1
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setServer1((ComponentInfo) newValue);
          return;
        case 1984149839:  // server2
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setServer2((ComponentInfo) newValue);
          return;
        case 1984149840:  // server3
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setServer3((ComponentInfo) newValue);
          return;
        case 1984149841:  // server4
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setServer4((ComponentInfo) newValue);
          return;
        case 1984149842:  // server5
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setServer5((ComponentInfo) newValue);
          return;
        case 1984149843:  // server6
          ((PriorityResolvingCombiningLiveDataServerComponentFactory) bean).setServer6((ComponentInfo) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((PriorityResolvingCombiningLiveDataServerComponentFactory) bean)._cacheManager, "cacheManager");
      JodaBeanUtils.notNull(((PriorityResolvingCombiningLiveDataServerComponentFactory) bean)._server1, "server1");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
