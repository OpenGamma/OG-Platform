/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.infrastructure;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;

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
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.ganglia.GangliaReporter;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;

/**
 * Component Factory to setup the metrics server.
 * <p>
 * This class is designed to allow protected methods to be overridden.
 */
@BeanDefinition
public class MetricsRepositoryComponentFactory extends AbstractComponentFactory {

  /**
   * The registry name.
   */
  @PropertyDefinition(validate = "notEmpty")
  private String _registryName;
  /**
   * Whether to publish over JMX.
   */
  @PropertyDefinition
  private boolean _jmxPublish = true;
  /**
   * Whether to publish over SLF4J.
   */
  @PropertyDefinition
  private boolean _slf4jPublish = true;
  /**
   * Whether to publish over Ganglia.
   */
  @PropertyDefinition
  private boolean _gangliaPublish;
  /**
   * The Ganglia address.
   */
  @PropertyDefinition
  private String _gangliaAddress;
  /**
   * The Ganglia port.
   */
  @PropertyDefinition
  private Integer _gangliaPort;
  /**
   * The Ganglia addressing mode.
   */
  @PropertyDefinition
  private String _gangliaAddressingMode;
  /**
   * The Ganglia time to live.
   */
  @PropertyDefinition
  private Integer _gangliaTtl = 1;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    MetricRegistry summaryRegistry = new MetricRegistry();
    MetricRegistry detailedRegistry = new MetricRegistry();
    if (isJmxPublish()) {
      initJmxPublish(repo, summaryRegistry, detailedRegistry);
    }
    if (isSlf4jPublish()) {
      initSlf4jPublish(repo, summaryRegistry, detailedRegistry);
    }
    if (isGangliaPublish()) {
      initGangliaPublish(repo, summaryRegistry, detailedRegistry);
    }
    OpenGammaMetricRegistry.setSummaryRegistry(summaryRegistry);
    OpenGammaMetricRegistry.setDetailedRegistry(detailedRegistry);

    // Register the registries so that where possible we can avoid
    // using the static singleton
    repo.registerComponent(MetricRegistry.class, "summary", summaryRegistry);
    repo.registerComponent(MetricRegistry.class, "detailed", detailedRegistry);
  }

  /**
   * Initialize publishing by JMX.
   * 
   * @param repo  the component repository, not null
   * @param summaryRegistry  the summary metrics registry, not null
   * @param detailedRegistry  the detailed metrics registry, not null
   */
  protected void initJmxPublish(ComponentRepository repo, MetricRegistry summaryRegistry, MetricRegistry detailedRegistry) {
    repo.registerLifecycle(new JmxReporterLifecycle(repo, summaryRegistry, detailedRegistry));
  }

  /**
   * Initialize publishing by SLF4J.
   * 
   * @param repo  the component repository, not null
   * @param summaryRegistry  the summary metrics registry, not null
   * @param detailedRegistry  the detailed metrics registry, not null
   */
  protected void initSlf4jPublish(ComponentRepository repo, MetricRegistry summaryRegistry, MetricRegistry detailedRegistry) {
    Slf4jReporter logReporter = Slf4jReporter.forRegistry(summaryRegistry)
        .outputTo(LoggerFactory.getLogger(OpenGammaMetricRegistry.class))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    logReporter.start(1, TimeUnit.MINUTES);
    logReporter = Slf4jReporter.forRegistry(detailedRegistry)
        .outputTo(LoggerFactory.getLogger(OpenGammaMetricRegistry.class))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    logReporter.start(1, TimeUnit.MINUTES);
  }

  /**
   * Initialize publishing by Ganglia.
   * <p>
   * Only the summary registry is published.
   * 
   * @param repo  the component repository, not null
   * @param summaryRegistry  the summary metrics registry, not null
   * @param detailedRegistry  the detailed metrics registry, not null
   */
  protected void initGangliaPublish(ComponentRepository repo, MetricRegistry summaryRegistry, MetricRegistry detailedRegistry) throws IOException {
    ArgumentChecker.notNull(getGangliaAddress(), "gangliaAddress");
    ArgumentChecker.notNull(getGangliaPort(), "gangliaPort");
    ArgumentChecker.notNull(getGangliaAddressingMode(), "gangliaAddressingMode");
    ArgumentChecker.notNull(getGangliaTtl(), "gangliaTtl");
    GMetric ganglia = new GMetric(getGangliaAddress(), getGangliaPort(), UDPAddressingMode.valueOf(getGangliaAddressingMode()), getGangliaTtl(), true);
    GangliaReporter gangliaReporter = GangliaReporter.forRegistry(summaryRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build(ganglia);
    repo.registerLifecycle(new GangliaReporterLifecycle(gangliaReporter));
  }

  //-------------------------------------------------------------------------
  /**
   * Lifecycle for JMX reporter.
   * This delays registering the reporter with the MBean server until necessary.
   */
  static final class JmxReporterLifecycle implements Lifecycle {
    private final ComponentRepository _repo;
    private final MetricRegistry _summaryRegistry;
    private final MetricRegistry _detailedRegistry;
    private volatile JmxReporter _summaryReporter;
    private volatile JmxReporter _detailedReporter;
    JmxReporterLifecycle(ComponentRepository repo, MetricRegistry summaryRegistry, MetricRegistry detailedRegistry) {
      _repo = repo;
      _summaryRegistry = summaryRegistry;
      _detailedRegistry = detailedRegistry;
    }
    @Override
    public void start() {
      MBeanServer mbeanServer = _repo.findInstance(MBeanServer.class);
      if (mbeanServer != null) {
        _summaryReporter = JmxReporter.forRegistry(_summaryRegistry).registerWith(mbeanServer).build();
        _detailedReporter = JmxReporter.forRegistry(_detailedRegistry).registerWith(mbeanServer).build();
      } else {
        // fallback to default MBeanServer
        _summaryReporter = JmxReporter.forRegistry(_summaryRegistry).build();
        _detailedReporter = JmxReporter.forRegistry(_detailedRegistry).build();
      }
      _summaryReporter.start();
      _detailedReporter.start();
    }
    @Override
    public void stop() {
      if (_summaryReporter != null) {
        _summaryReporter.stop();
        _summaryReporter = null;
      }
      if (_detailedReporter != null) {
        _detailedReporter.stop();
        _detailedReporter = null;
      }
    }
    @Override
    public boolean isRunning() {
      return _summaryReporter != null || _detailedReporter != null;
    }
  }

  /**
   * Lifecycle for Ganglia reporter.
   * This delays registering the reporter with Ganglia until necessary.
   */
  static final class GangliaReporterLifecycle implements Lifecycle {
    private volatile GangliaReporter _gangliaReporter;
    public GangliaReporterLifecycle(GangliaReporter gangliaReporter) {
      _gangliaReporter = gangliaReporter;
    }
    @Override
    public void start() {
      if (_gangliaReporter != null) {
        _gangliaReporter.start(1, TimeUnit.MINUTES);
      }
    }
    @Override
    public void stop() {
      if (_gangliaReporter != null) {
        _gangliaReporter.stop();
        _gangliaReporter = null;
      }
    }
    @Override
    public boolean isRunning() {
      return (_gangliaReporter != null);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MetricsRepositoryComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MetricsRepositoryComponentFactory.Meta meta() {
    return MetricsRepositoryComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MetricsRepositoryComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MetricsRepositoryComponentFactory.Meta metaBean() {
    return MetricsRepositoryComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the registry name.
   * @return the value of the property, not empty
   */
  public String getRegistryName() {
    return _registryName;
  }

  /**
   * Sets the registry name.
   * @param registryName  the new value of the property, not empty
   */
  public void setRegistryName(String registryName) {
    JodaBeanUtils.notEmpty(registryName, "registryName");
    this._registryName = registryName;
  }

  /**
   * Gets the the {@code registryName} property.
   * @return the property, not null
   */
  public final Property<String> registryName() {
    return metaBean().registryName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to publish over JMX.
   * @return the value of the property
   */
  public boolean isJmxPublish() {
    return _jmxPublish;
  }

  /**
   * Sets whether to publish over JMX.
   * @param jmxPublish  the new value of the property
   */
  public void setJmxPublish(boolean jmxPublish) {
    this._jmxPublish = jmxPublish;
  }

  /**
   * Gets the the {@code jmxPublish} property.
   * @return the property, not null
   */
  public final Property<Boolean> jmxPublish() {
    return metaBean().jmxPublish().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to publish over SLF4J.
   * @return the value of the property
   */
  public boolean isSlf4jPublish() {
    return _slf4jPublish;
  }

  /**
   * Sets whether to publish over SLF4J.
   * @param slf4jPublish  the new value of the property
   */
  public void setSlf4jPublish(boolean slf4jPublish) {
    this._slf4jPublish = slf4jPublish;
  }

  /**
   * Gets the the {@code slf4jPublish} property.
   * @return the property, not null
   */
  public final Property<Boolean> slf4jPublish() {
    return metaBean().slf4jPublish().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to publish over Ganglia.
   * @return the value of the property
   */
  public boolean isGangliaPublish() {
    return _gangliaPublish;
  }

  /**
   * Sets whether to publish over Ganglia.
   * @param gangliaPublish  the new value of the property
   */
  public void setGangliaPublish(boolean gangliaPublish) {
    this._gangliaPublish = gangliaPublish;
  }

  /**
   * Gets the the {@code gangliaPublish} property.
   * @return the property, not null
   */
  public final Property<Boolean> gangliaPublish() {
    return metaBean().gangliaPublish().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Ganglia address.
   * @return the value of the property
   */
  public String getGangliaAddress() {
    return _gangliaAddress;
  }

  /**
   * Sets the Ganglia address.
   * @param gangliaAddress  the new value of the property
   */
  public void setGangliaAddress(String gangliaAddress) {
    this._gangliaAddress = gangliaAddress;
  }

  /**
   * Gets the the {@code gangliaAddress} property.
   * @return the property, not null
   */
  public final Property<String> gangliaAddress() {
    return metaBean().gangliaAddress().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Ganglia port.
   * @return the value of the property
   */
  public Integer getGangliaPort() {
    return _gangliaPort;
  }

  /**
   * Sets the Ganglia port.
   * @param gangliaPort  the new value of the property
   */
  public void setGangliaPort(Integer gangliaPort) {
    this._gangliaPort = gangliaPort;
  }

  /**
   * Gets the the {@code gangliaPort} property.
   * @return the property, not null
   */
  public final Property<Integer> gangliaPort() {
    return metaBean().gangliaPort().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Ganglia addressing mode.
   * @return the value of the property
   */
  public String getGangliaAddressingMode() {
    return _gangliaAddressingMode;
  }

  /**
   * Sets the Ganglia addressing mode.
   * @param gangliaAddressingMode  the new value of the property
   */
  public void setGangliaAddressingMode(String gangliaAddressingMode) {
    this._gangliaAddressingMode = gangliaAddressingMode;
  }

  /**
   * Gets the the {@code gangliaAddressingMode} property.
   * @return the property, not null
   */
  public final Property<String> gangliaAddressingMode() {
    return metaBean().gangliaAddressingMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Ganglia time to live.
   * @return the value of the property
   */
  public Integer getGangliaTtl() {
    return _gangliaTtl;
  }

  /**
   * Sets the Ganglia time to live.
   * @param gangliaTtl  the new value of the property
   */
  public void setGangliaTtl(Integer gangliaTtl) {
    this._gangliaTtl = gangliaTtl;
  }

  /**
   * Gets the the {@code gangliaTtl} property.
   * @return the property, not null
   */
  public final Property<Integer> gangliaTtl() {
    return metaBean().gangliaTtl().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MetricsRepositoryComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MetricsRepositoryComponentFactory other = (MetricsRepositoryComponentFactory) obj;
      return JodaBeanUtils.equal(getRegistryName(), other.getRegistryName()) &&
          (isJmxPublish() == other.isJmxPublish()) &&
          (isSlf4jPublish() == other.isSlf4jPublish()) &&
          (isGangliaPublish() == other.isGangliaPublish()) &&
          JodaBeanUtils.equal(getGangliaAddress(), other.getGangliaAddress()) &&
          JodaBeanUtils.equal(getGangliaPort(), other.getGangliaPort()) &&
          JodaBeanUtils.equal(getGangliaAddressingMode(), other.getGangliaAddressingMode()) &&
          JodaBeanUtils.equal(getGangliaTtl(), other.getGangliaTtl()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegistryName());
    hash += hash * 31 + JodaBeanUtils.hashCode(isJmxPublish());
    hash += hash * 31 + JodaBeanUtils.hashCode(isSlf4jPublish());
    hash += hash * 31 + JodaBeanUtils.hashCode(isGangliaPublish());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGangliaAddress());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGangliaPort());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGangliaAddressingMode());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGangliaTtl());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("MetricsRepositoryComponentFactory{");
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
    buf.append("registryName").append('=').append(JodaBeanUtils.toString(getRegistryName())).append(',').append(' ');
    buf.append("jmxPublish").append('=').append(JodaBeanUtils.toString(isJmxPublish())).append(',').append(' ');
    buf.append("slf4jPublish").append('=').append(JodaBeanUtils.toString(isSlf4jPublish())).append(',').append(' ');
    buf.append("gangliaPublish").append('=').append(JodaBeanUtils.toString(isGangliaPublish())).append(',').append(' ');
    buf.append("gangliaAddress").append('=').append(JodaBeanUtils.toString(getGangliaAddress())).append(',').append(' ');
    buf.append("gangliaPort").append('=').append(JodaBeanUtils.toString(getGangliaPort())).append(',').append(' ');
    buf.append("gangliaAddressingMode").append('=').append(JodaBeanUtils.toString(getGangliaAddressingMode())).append(',').append(' ');
    buf.append("gangliaTtl").append('=').append(JodaBeanUtils.toString(getGangliaTtl())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MetricsRepositoryComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code registryName} property.
     */
    private final MetaProperty<String> _registryName = DirectMetaProperty.ofReadWrite(
        this, "registryName", MetricsRepositoryComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code jmxPublish} property.
     */
    private final MetaProperty<Boolean> _jmxPublish = DirectMetaProperty.ofReadWrite(
        this, "jmxPublish", MetricsRepositoryComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code slf4jPublish} property.
     */
    private final MetaProperty<Boolean> _slf4jPublish = DirectMetaProperty.ofReadWrite(
        this, "slf4jPublish", MetricsRepositoryComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code gangliaPublish} property.
     */
    private final MetaProperty<Boolean> _gangliaPublish = DirectMetaProperty.ofReadWrite(
        this, "gangliaPublish", MetricsRepositoryComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code gangliaAddress} property.
     */
    private final MetaProperty<String> _gangliaAddress = DirectMetaProperty.ofReadWrite(
        this, "gangliaAddress", MetricsRepositoryComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code gangliaPort} property.
     */
    private final MetaProperty<Integer> _gangliaPort = DirectMetaProperty.ofReadWrite(
        this, "gangliaPort", MetricsRepositoryComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code gangliaAddressingMode} property.
     */
    private final MetaProperty<String> _gangliaAddressingMode = DirectMetaProperty.ofReadWrite(
        this, "gangliaAddressingMode", MetricsRepositoryComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code gangliaTtl} property.
     */
    private final MetaProperty<Integer> _gangliaTtl = DirectMetaProperty.ofReadWrite(
        this, "gangliaTtl", MetricsRepositoryComponentFactory.class, Integer.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "registryName",
        "jmxPublish",
        "slf4jPublish",
        "gangliaPublish",
        "gangliaAddress",
        "gangliaPort",
        "gangliaAddressingMode",
        "gangliaTtl");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1329285016:  // registryName
          return _registryName;
        case 1313494970:  // jmxPublish
          return _jmxPublish;
        case 283122412:  // slf4jPublish
          return _slf4jPublish;
        case 113968702:  // gangliaPublish
          return _gangliaPublish;
        case -798358237:  // gangliaAddress
          return _gangliaAddress;
        case 44258738:  // gangliaPort
          return _gangliaPort;
        case -772603358:  // gangliaAddressingMode
          return _gangliaAddressingMode;
        case 555621019:  // gangliaTtl
          return _gangliaTtl;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MetricsRepositoryComponentFactory> builder() {
      return new DirectBeanBuilder<MetricsRepositoryComponentFactory>(new MetricsRepositoryComponentFactory());
    }

    @Override
    public Class<? extends MetricsRepositoryComponentFactory> beanType() {
      return MetricsRepositoryComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code registryName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> registryName() {
      return _registryName;
    }

    /**
     * The meta-property for the {@code jmxPublish} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> jmxPublish() {
      return _jmxPublish;
    }

    /**
     * The meta-property for the {@code slf4jPublish} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> slf4jPublish() {
      return _slf4jPublish;
    }

    /**
     * The meta-property for the {@code gangliaPublish} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> gangliaPublish() {
      return _gangliaPublish;
    }

    /**
     * The meta-property for the {@code gangliaAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> gangliaAddress() {
      return _gangliaAddress;
    }

    /**
     * The meta-property for the {@code gangliaPort} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> gangliaPort() {
      return _gangliaPort;
    }

    /**
     * The meta-property for the {@code gangliaAddressingMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> gangliaAddressingMode() {
      return _gangliaAddressingMode;
    }

    /**
     * The meta-property for the {@code gangliaTtl} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> gangliaTtl() {
      return _gangliaTtl;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1329285016:  // registryName
          return ((MetricsRepositoryComponentFactory) bean).getRegistryName();
        case 1313494970:  // jmxPublish
          return ((MetricsRepositoryComponentFactory) bean).isJmxPublish();
        case 283122412:  // slf4jPublish
          return ((MetricsRepositoryComponentFactory) bean).isSlf4jPublish();
        case 113968702:  // gangliaPublish
          return ((MetricsRepositoryComponentFactory) bean).isGangliaPublish();
        case -798358237:  // gangliaAddress
          return ((MetricsRepositoryComponentFactory) bean).getGangliaAddress();
        case 44258738:  // gangliaPort
          return ((MetricsRepositoryComponentFactory) bean).getGangliaPort();
        case -772603358:  // gangliaAddressingMode
          return ((MetricsRepositoryComponentFactory) bean).getGangliaAddressingMode();
        case 555621019:  // gangliaTtl
          return ((MetricsRepositoryComponentFactory) bean).getGangliaTtl();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1329285016:  // registryName
          ((MetricsRepositoryComponentFactory) bean).setRegistryName((String) newValue);
          return;
        case 1313494970:  // jmxPublish
          ((MetricsRepositoryComponentFactory) bean).setJmxPublish((Boolean) newValue);
          return;
        case 283122412:  // slf4jPublish
          ((MetricsRepositoryComponentFactory) bean).setSlf4jPublish((Boolean) newValue);
          return;
        case 113968702:  // gangliaPublish
          ((MetricsRepositoryComponentFactory) bean).setGangliaPublish((Boolean) newValue);
          return;
        case -798358237:  // gangliaAddress
          ((MetricsRepositoryComponentFactory) bean).setGangliaAddress((String) newValue);
          return;
        case 44258738:  // gangliaPort
          ((MetricsRepositoryComponentFactory) bean).setGangliaPort((Integer) newValue);
          return;
        case -772603358:  // gangliaAddressingMode
          ((MetricsRepositoryComponentFactory) bean).setGangliaAddressingMode((String) newValue);
          return;
        case 555621019:  // gangliaTtl
          ((MetricsRepositoryComponentFactory) bean).setGangliaTtl((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notEmpty(((MetricsRepositoryComponentFactory) bean)._registryName, "registryName");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
