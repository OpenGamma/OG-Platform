/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.provider;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.impl.DataHistoricalTimeSeriesProviderResource;
import com.opengamma.provider.historicaltimeseries.impl.DelegatingHistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.impl.RemoteHistoricalTimeSeriesProvider;

/**
 * Component factory for combining local time-series providers.
 */
@BeanDefinition
public class DelegatingHistoricalTimeSeriesProviderComponentFactory extends AbstractComponentFactory {

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
   * The first provider to use.
   */
  @PropertyDefinition(validate = "notNull")
  private ComponentInfo _provider1;
  /**
   * The second provider to use.
   */
  @PropertyDefinition
  private ComponentInfo _provider2;
  /**
   * The third provider to use.
   */
  @PropertyDefinition
  private ComponentInfo _provider3;
  /**
   * The fourth provider to use.
   */
  @PropertyDefinition
  private ComponentInfo _provider4;
  /**
   * The fifth provider to use.
   */
  @PropertyDefinition
  private ComponentInfo _provider5;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    Map<String, HistoricalTimeSeriesProvider> map = Maps.newHashMap();
    List<String> acceptedTypes = Lists.newArrayList();
    
    int maxLevel = buildProviders(repo, getProvider1(), map, acceptedTypes, 1);
    if (getProvider2() != null) {
      maxLevel = buildProviders(repo, getProvider2(), map, acceptedTypes, maxLevel);
    }
    if (getProvider3() != null) {
      maxLevel = buildProviders(repo, getProvider3(), map, acceptedTypes, maxLevel);
    }
    if (getProvider4() != null) {
      maxLevel = buildProviders(repo, getProvider4(), map, acceptedTypes, maxLevel);
    }
    if (getProvider5() != null) {
      maxLevel = buildProviders(repo, getProvider5(), map, acceptedTypes, maxLevel);
    }
    
    final HistoricalTimeSeriesProvider provider = new DelegatingHistoricalTimeSeriesProvider(map);
    final ComponentInfo info = new ComponentInfo(HistoricalTimeSeriesProvider.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, (maxLevel + 1));
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteHistoricalTimeSeriesProvider.class);
    info.addAttribute(ComponentInfoAttributes.ACCEPTED_TYPES, Joiner.on(',').join(acceptedTypes));
    repo.registerComponent(info, provider);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataHistoricalTimeSeriesProviderResource(provider));
    }
  }

  private int buildProviders(ComponentRepository repo, ComponentInfo info, Map<String, HistoricalTimeSeriesProvider> map, List<String> acceptedTypes, int maxLevel) {
    if (info.getType() != HistoricalTimeSeriesProvider.class) {
      throw new IllegalArgumentException("Component info must be a HistoricalTimeSeriesProvider");
    }
    if (info.getAttribute(ComponentInfoAttributes.ACCEPTED_TYPES) == null) {
      throw new IllegalArgumentException("Component info must specify acceptedTypes: " + info.getClassifier());
    }
    List<String> accepteds = Arrays.asList(StringUtils.split(info.getAttribute(ComponentInfoAttributes.ACCEPTED_TYPES), ","));
    for (String accepted : accepteds) {
      if (map.containsKey(accepted) == false) {
        map.put(accepted, (HistoricalTimeSeriesProvider) repo.getInstance(info));
        acceptedTypes.add(accepted);
      }
    }
    String level = info.getAttribute(ComponentInfoAttributes.LEVEL);
    return Math.max(maxLevel, NumberUtils.toInt(level, 1));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DelegatingHistoricalTimeSeriesProviderComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DelegatingHistoricalTimeSeriesProviderComponentFactory.Meta meta() {
    return DelegatingHistoricalTimeSeriesProviderComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DelegatingHistoricalTimeSeriesProviderComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DelegatingHistoricalTimeSeriesProviderComponentFactory.Meta metaBean() {
    return DelegatingHistoricalTimeSeriesProviderComponentFactory.Meta.INSTANCE;
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
   * Gets the first provider to use.
   * @return the value of the property, not null
   */
  public ComponentInfo getProvider1() {
    return _provider1;
  }

  /**
   * Sets the first provider to use.
   * @param provider1  the new value of the property, not null
   */
  public void setProvider1(ComponentInfo provider1) {
    JodaBeanUtils.notNull(provider1, "provider1");
    this._provider1 = provider1;
  }

  /**
   * Gets the the {@code provider1} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> provider1() {
    return metaBean().provider1().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the second provider to use.
   * @return the value of the property
   */
  public ComponentInfo getProvider2() {
    return _provider2;
  }

  /**
   * Sets the second provider to use.
   * @param provider2  the new value of the property
   */
  public void setProvider2(ComponentInfo provider2) {
    this._provider2 = provider2;
  }

  /**
   * Gets the the {@code provider2} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> provider2() {
    return metaBean().provider2().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the third provider to use.
   * @return the value of the property
   */
  public ComponentInfo getProvider3() {
    return _provider3;
  }

  /**
   * Sets the third provider to use.
   * @param provider3  the new value of the property
   */
  public void setProvider3(ComponentInfo provider3) {
    this._provider3 = provider3;
  }

  /**
   * Gets the the {@code provider3} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> provider3() {
    return metaBean().provider3().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fourth provider to use.
   * @return the value of the property
   */
  public ComponentInfo getProvider4() {
    return _provider4;
  }

  /**
   * Sets the fourth provider to use.
   * @param provider4  the new value of the property
   */
  public void setProvider4(ComponentInfo provider4) {
    this._provider4 = provider4;
  }

  /**
   * Gets the the {@code provider4} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> provider4() {
    return metaBean().provider4().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fifth provider to use.
   * @return the value of the property
   */
  public ComponentInfo getProvider5() {
    return _provider5;
  }

  /**
   * Sets the fifth provider to use.
   * @param provider5  the new value of the property
   */
  public void setProvider5(ComponentInfo provider5) {
    this._provider5 = provider5;
  }

  /**
   * Gets the the {@code provider5} property.
   * @return the property, not null
   */
  public final Property<ComponentInfo> provider5() {
    return metaBean().provider5().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DelegatingHistoricalTimeSeriesProviderComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DelegatingHistoricalTimeSeriesProviderComponentFactory other = (DelegatingHistoricalTimeSeriesProviderComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getProvider1(), other.getProvider1()) &&
          JodaBeanUtils.equal(getProvider2(), other.getProvider2()) &&
          JodaBeanUtils.equal(getProvider3(), other.getProvider3()) &&
          JodaBeanUtils.equal(getProvider4(), other.getProvider4()) &&
          JodaBeanUtils.equal(getProvider5(), other.getProvider5()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProvider1());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProvider2());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProvider3());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProvider4());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProvider5());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("DelegatingHistoricalTimeSeriesProviderComponentFactory{");
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
    buf.append("provider1").append('=').append(JodaBeanUtils.toString(getProvider1())).append(',').append(' ');
    buf.append("provider2").append('=').append(JodaBeanUtils.toString(getProvider2())).append(',').append(' ');
    buf.append("provider3").append('=').append(JodaBeanUtils.toString(getProvider3())).append(',').append(' ');
    buf.append("provider4").append('=').append(JodaBeanUtils.toString(getProvider4())).append(',').append(' ');
    buf.append("provider5").append('=').append(JodaBeanUtils.toString(getProvider5())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DelegatingHistoricalTimeSeriesProviderComponentFactory}.
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
        this, "classifier", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code provider1} property.
     */
    private final MetaProperty<ComponentInfo> _provider1 = DirectMetaProperty.ofReadWrite(
        this, "provider1", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code provider2} property.
     */
    private final MetaProperty<ComponentInfo> _provider2 = DirectMetaProperty.ofReadWrite(
        this, "provider2", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code provider3} property.
     */
    private final MetaProperty<ComponentInfo> _provider3 = DirectMetaProperty.ofReadWrite(
        this, "provider3", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code provider4} property.
     */
    private final MetaProperty<ComponentInfo> _provider4 = DirectMetaProperty.ofReadWrite(
        this, "provider4", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-property for the {@code provider5} property.
     */
    private final MetaProperty<ComponentInfo> _provider5 = DirectMetaProperty.ofReadWrite(
        this, "provider5", DelegatingHistoricalTimeSeriesProviderComponentFactory.class, ComponentInfo.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "provider1",
        "provider2",
        "provider3",
        "provider4",
        "provider5");

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
        case -547571616:  // provider1
          return _provider1;
        case -547571615:  // provider2
          return _provider2;
        case -547571614:  // provider3
          return _provider3;
        case -547571613:  // provider4
          return _provider4;
        case -547571612:  // provider5
          return _provider5;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DelegatingHistoricalTimeSeriesProviderComponentFactory> builder() {
      return new DirectBeanBuilder<DelegatingHistoricalTimeSeriesProviderComponentFactory>(new DelegatingHistoricalTimeSeriesProviderComponentFactory());
    }

    @Override
    public Class<? extends DelegatingHistoricalTimeSeriesProviderComponentFactory> beanType() {
      return DelegatingHistoricalTimeSeriesProviderComponentFactory.class;
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
     * The meta-property for the {@code provider1} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> provider1() {
      return _provider1;
    }

    /**
     * The meta-property for the {@code provider2} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> provider2() {
      return _provider2;
    }

    /**
     * The meta-property for the {@code provider3} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> provider3() {
      return _provider3;
    }

    /**
     * The meta-property for the {@code provider4} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> provider4() {
      return _provider4;
    }

    /**
     * The meta-property for the {@code provider5} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentInfo> provider5() {
      return _provider5;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).isPublishRest();
        case -547571616:  // provider1
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).getProvider1();
        case -547571615:  // provider2
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).getProvider2();
        case -547571614:  // provider3
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).getProvider3();
        case -547571613:  // provider4
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).getProvider4();
        case -547571612:  // provider5
          return ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).getProvider5();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -547571616:  // provider1
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setProvider1((ComponentInfo) newValue);
          return;
        case -547571615:  // provider2
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setProvider2((ComponentInfo) newValue);
          return;
        case -547571614:  // provider3
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setProvider3((ComponentInfo) newValue);
          return;
        case -547571613:  // provider4
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setProvider4((ComponentInfo) newValue);
          return;
        case -547571612:  // provider5
          ((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean).setProvider5((ComponentInfo) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((DelegatingHistoricalTimeSeriesProviderComponentFactory) bean)._provider1, "provider1");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
