/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.tool.marketdata;

import static java.lang.String.format;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.marketdatasnapshot.MarketDataSnapshotterImpl;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Saves snapshots.
 */
@BeanDefinition
public final class MarketDataSnapshotSaver implements ImmutableBean {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSnapshotSaver.class);
  /**
   * The view processor.
   */
  @PropertyDefinition(validate = "notNull")
  private final ViewProcessor _viewProcessor;
  /**
   * The config master.
   */
  @PropertyDefinition(validate = "notNull")
  private final ConfigMaster _configMaster;
  /**
   * The market data snapshot master.
   */
  @PropertyDefinition(validate = "notNull")
  private final MarketDataSnapshotMaster _marketDataSnapshotMaster;
  /**
   * The market data snapshotter.
   */
  @PropertyDefinition(validate = "notNull")
  private final MarketDataSnapshotter _snapshotter;
  /**
   * The maximum time to wait, in milliseconds, for market data to populate the snapshot.
   */
  @PropertyDefinition
  private final Long _marketDataTimeoutMillis;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance.
   *
   * @param computationTargetResolver  the resolver, not null
   * @param historicalTimeSeriesSource  the source, not null
   * @param viewProcessor  the view processor, not null
   * @param configMaster  the master, not null
   * @param marketDataSnapshotMaster  the master, not null
   * @param volatilityCubeDefinitionSource  the source, not null
   * @param mode  the mode in which to capture the snapshot (STRUCTURED or FLATTENED), not null
   * @param marketDataTimeoutMillis  the maximum time to wait, in milliseconds, for market data, null to use a default value
   * @return the saver, not null
   */
  public static MarketDataSnapshotSaver of(final ComputationTargetResolver computationTargetResolver, final HistoricalTimeSeriesSource historicalTimeSeriesSource,
      final ViewProcessor viewProcessor, final ConfigMaster configMaster, final MarketDataSnapshotMaster marketDataSnapshotMaster,
      final VolatilityCubeDefinitionSource volatilityCubeDefinitionSource, final MarketDataSnapshotter.Mode mode, final Long marketDataTimeoutMillis) {
    ArgumentChecker.notNull(computationTargetResolver, "computationTargetResolver");
    ArgumentChecker.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");

    final MarketDataSnapshotterImpl snapshotter = new MarketDataSnapshotterImpl(computationTargetResolver, historicalTimeSeriesSource, mode);
    return new MarketDataSnapshotSaver(viewProcessor, configMaster, marketDataSnapshotMaster, snapshotter, marketDataTimeoutMillis);
  }

  /**
   * Obtains an instance.
   *
   * @param snapshotter  the snapshotter, not null
   * @param viewProcessor  the view processor, not null
   * @param configMaster  the master, not null
   * @param marketDataSnapshotMaster  the master, not null
   * @param marketDataTimeoutMillis  the maximum time to wait, in milliseconds, for market data, null to use a default value
   * @return the saver, not null
   */
  public static MarketDataSnapshotSaver of(final MarketDataSnapshotter snapshotter, final ViewProcessor viewProcessor,
      final ConfigMaster configMaster, final MarketDataSnapshotMaster marketDataSnapshotMaster, final Long marketDataTimeoutMillis) {
    return new MarketDataSnapshotSaver(viewProcessor, configMaster, marketDataSnapshotMaster, snapshotter, marketDataTimeoutMillis);
  }

  //-------------------------------------------------------------------------
  public MarketDataSnapshotDocument createSnapshot(final String name, final String viewDefinitionName, final Instant valuationInstant,
      final List<MarketDataSpecification> marketDataSpecs) throws InterruptedException {
    final ViewCycleExecutionOptions cycleExecutionOptions = ViewCycleExecutionOptions.builder().setValuationTime(valuationInstant).setMarketDataSpecifications(marketDataSpecs).create();
    final ArbitraryViewCycleExecutionSequence executionSequence = ArbitraryViewCycleExecutionSequence.single(cycleExecutionOptions);
    final ViewExecutionOptions viewExecutionOptions = new ExecutionOptions(executionSequence, EnumSet.of(ViewExecutionFlags.AWAIT_MARKET_DATA), null, getMarketDataTimeoutMillis(), null);

    final Set<ConfigDocument> viewDefinitions = Sets.newHashSet();
    final ConfigSearchRequest<ViewDefinition> request = new ConfigSearchRequest<ViewDefinition>(ViewDefinition.class);
    request.setName(viewDefinitionName);
    Iterables.addAll(viewDefinitions, ConfigSearchIterator.iterable(getConfigMaster(), request));
    if (viewDefinitions.isEmpty()) {
      endWithError("Unable to resolve any view definitions with name '%s'", viewDefinitionName);
    }
    if (viewDefinitions.size() > 1) {
      endWithError("Multiple view definitions resolved when searching for string '%s': %s", viewDefinitionName, viewDefinitions);
    }
    final UniqueId viewDefinitionId = Iterables.getOnlyElement(viewDefinitions).getValue().getUniqueId();
    final ManageableMarketDataSnapshot snapshot = createSnapshotFromNewProcess(getSnapshotter(), name, getViewProcessor(), viewDefinitionId, viewExecutionOptions);
    return getMarketDataSnapshotMaster().add(new MarketDataSnapshotDocument(snapshot));
  }

  public MarketDataSnapshotDocument createSnapshot(final String name, final UniqueId viewProcessId) throws InterruptedException {
    final ManageableMarketDataSnapshot snapshot = createSnapshotFromExistingProcess(getSnapshotter(), name, getViewProcessor(), viewProcessId);
    return getMarketDataSnapshotMaster().add(new MarketDataSnapshotDocument(snapshot));
  }

  //-------------------------------------------------------------------------
  private static ManageableMarketDataSnapshot createSnapshotFromNewProcess(final MarketDataSnapshotter marketDataSnapshotter, final String name,
      final ViewProcessor viewProcessor, final UniqueId viewDefinitionId, final ViewExecutionOptions viewExecutionOptions) throws InterruptedException {
    final ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    final SnapshotResultListener resultListener = new SnapshotResultListener();
    viewClient.setResultListener(resultListener);
    viewClient.setViewCycleAccessSupported(true);
    viewClient.attachToViewProcess(viewDefinitionId, viewExecutionOptions);
    try {
      return takeSnapshot(marketDataSnapshotter, name, viewClient, resultListener);
    } finally {
      viewClient.shutdown();
    }
  }

  private static ManageableMarketDataSnapshot createSnapshotFromExistingProcess(final MarketDataSnapshotter marketDataSnapshotter, final String name,
      final ViewProcessor viewProcessor, final UniqueId viewProcessId) throws InterruptedException {
    final ViewClient viewClient = viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    final SnapshotResultListener resultListener = new SnapshotResultListener();
    viewClient.setResultListener(resultListener);
    viewClient.setViewCycleAccessSupported(true);
    viewClient.attachToViewProcess(viewProcessId);
    try {
      return takeSnapshot(marketDataSnapshotter, name, viewClient, resultListener);
    } finally {
      viewClient.shutdown();
    }
  }

  private static ManageableMarketDataSnapshot takeSnapshot(final MarketDataSnapshotter marketDataSnapshotter, final String name,
      final ViewClient viewClient, final SnapshotResultListener resultListener) throws InterruptedException {
    resultListener.await();
    viewClient.setResultListener(null);
    if (!resultListener.isSuccess()) {
      throw new OpenGammaRuntimeException("Failed to capture a cycle to snapshot");
    }
    EngineResourceReference<? extends ViewCycle> cycleReference = null;
    try {
      cycleReference = viewClient.createLatestCycleReference();
      if (cycleReference == null) {
        throw new OpenGammaRuntimeException("Unable to obtain a view cycle reference to snapshot");
      }
      final ViewCycle viewCycle = cycleReference.get();
      return takeSnapshot(marketDataSnapshotter, name, viewClient, viewCycle);
    } finally {
      if (cycleReference != null) {
        cycleReference.release();
      }
    }
  }

  private static ManageableMarketDataSnapshot takeSnapshot(final MarketDataSnapshotter marketDataSnapshotter, String name, final ViewClient viewClient, final ViewCycle viewCycle) {
    s_logger.debug("Taking snapshot");
    final StructuredMarketDataSnapshot snapshot = marketDataSnapshotter.createSnapshot(viewClient, viewCycle);
    s_logger.debug("Snapshot complete");
    final ManageableMarketDataSnapshot manageableMarketDataSnapshot = new ManageableMarketDataSnapshot(snapshot);
    if (name == null) {
      name = snapshot.getBasisViewName() + "/" + viewCycle.getExecutionOptions().getValuationTime();
    }
    manageableMarketDataSnapshot.setName(name);
    return manageableMarketDataSnapshot;
  }

  private void endWithError(final String message, final Object... messageArgs) {
    s_logger.error(message, messageArgs);
    throw new OpenGammaRuntimeException(format(message, messageArgs));
  }

  private static class SnapshotResultListener extends AbstractViewResultListener {

    private final CountDownLatch _latch = new CountDownLatch(1);
    private boolean _success;

    public void await() throws InterruptedException {
      _latch.await();
    }

    public boolean isSuccess() {
      return _success;
    }

    @Override
    public UserPrincipal getUser() {
      String ipAddress;
      try {
        ipAddress = InetAddress.getLocalHost().getHostAddress();
      } catch (final UnknownHostException e) {
        ipAddress = "unknown";
      }
      return new UserPrincipal("MarketDataSnapshotterTool", ipAddress);
    }

    @Override
    public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
      s_logger.error(exception.getMessage() + "\n\n" + (exception.getCause() == null ? "" : exception.getCause().getMessage()));
      _latch.countDown();
    }

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      s_logger.info("cycle completed");
      _success = true;
      _latch.countDown();
    }

    @Override
    public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
      s_logger.error(exception.getMessage() + "\n\n" + (exception.getCause() == null ? "" : exception.getCause().getMessage()));
      _latch.countDown();
    }

    @Override
    public void processCompleted() {
      _latch.countDown();
    }

    @Override
    public void processTerminated(final boolean executionInterrupted) {
      _latch.countDown();
    }

  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MarketDataSnapshotSaver}.
   * @return the meta-bean, not null
   */
  public static MarketDataSnapshotSaver.Meta meta() {
    return MarketDataSnapshotSaver.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MarketDataSnapshotSaver.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static MarketDataSnapshotSaver.Builder builder() {
    return new MarketDataSnapshotSaver.Builder();
  }

  private MarketDataSnapshotSaver(
      ViewProcessor viewProcessor,
      ConfigMaster configMaster,
      MarketDataSnapshotMaster marketDataSnapshotMaster,
      MarketDataSnapshotter snapshotter,
      Long marketDataTimeoutMillis) {
    JodaBeanUtils.notNull(viewProcessor, "viewProcessor");
    JodaBeanUtils.notNull(configMaster, "configMaster");
    JodaBeanUtils.notNull(marketDataSnapshotMaster, "marketDataSnapshotMaster");
    JodaBeanUtils.notNull(snapshotter, "snapshotter");
    this._viewProcessor = viewProcessor;
    this._configMaster = configMaster;
    this._marketDataSnapshotMaster = marketDataSnapshotMaster;
    this._snapshotter = snapshotter;
    this._marketDataTimeoutMillis = marketDataTimeoutMillis;
  }

  @Override
  public MarketDataSnapshotSaver.Meta metaBean() {
    return MarketDataSnapshotSaver.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view processor.
   * @return the value of the property, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the value of the property, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data snapshot master.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _marketDataSnapshotMaster;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data snapshotter.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotter getSnapshotter() {
    return _snapshotter;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum time to wait, in milliseconds, for market data to populate the snapshot.
   * @return the value of the property
   */
  public Long getMarketDataTimeoutMillis() {
    return _marketDataTimeoutMillis;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public MarketDataSnapshotSaver clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MarketDataSnapshotSaver other = (MarketDataSnapshotSaver) obj;
      return JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotMaster(), other.getMarketDataSnapshotMaster()) &&
          JodaBeanUtils.equal(getSnapshotter(), other.getSnapshotter()) &&
          JodaBeanUtils.equal(getMarketDataTimeoutMillis(), other.getMarketDataTimeoutMillis());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSnapshotter());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataTimeoutMillis());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("MarketDataSnapshotSaver{");
    buf.append("viewProcessor").append('=').append(getViewProcessor()).append(',').append(' ');
    buf.append("configMaster").append('=').append(getConfigMaster()).append(',').append(' ');
    buf.append("marketDataSnapshotMaster").append('=').append(getMarketDataSnapshotMaster()).append(',').append(' ');
    buf.append("snapshotter").append('=').append(getSnapshotter()).append(',').append(' ');
    buf.append("marketDataTimeoutMillis").append('=').append(JodaBeanUtils.toString(getMarketDataTimeoutMillis()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MarketDataSnapshotSaver}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofImmutable(
        this, "viewProcessor", MarketDataSnapshotSaver.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofImmutable(
        this, "configMaster", MarketDataSnapshotSaver.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _marketDataSnapshotMaster = DirectMetaProperty.ofImmutable(
        this, "marketDataSnapshotMaster", MarketDataSnapshotSaver.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code snapshotter} property.
     */
    private final MetaProperty<MarketDataSnapshotter> _snapshotter = DirectMetaProperty.ofImmutable(
        this, "snapshotter", MarketDataSnapshotSaver.class, MarketDataSnapshotter.class);
    /**
     * The meta-property for the {@code marketDataTimeoutMillis} property.
     */
    private final MetaProperty<Long> _marketDataTimeoutMillis = DirectMetaProperty.ofImmutable(
        this, "marketDataTimeoutMillis", MarketDataSnapshotSaver.class, Long.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "viewProcessor",
        "configMaster",
        "marketDataSnapshotMaster",
        "snapshotter",
        "marketDataTimeoutMillis");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case 10395716:  // configMaster
          return _configMaster;
        case 2090650860:  // marketDataSnapshotMaster
          return _marketDataSnapshotMaster;
        case -168565795:  // snapshotter
          return _snapshotter;
        case -617269599:  // marketDataTimeoutMillis
          return _marketDataTimeoutMillis;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public MarketDataSnapshotSaver.Builder builder() {
      return new MarketDataSnapshotSaver.Builder();
    }

    @Override
    public Class<? extends MarketDataSnapshotSaver> beanType() {
      return MarketDataSnapshotSaver.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
      return _marketDataSnapshotMaster;
    }

    /**
     * The meta-property for the {@code snapshotter} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataSnapshotter> snapshotter() {
      return _snapshotter;
    }

    /**
     * The meta-property for the {@code marketDataTimeoutMillis} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Long> marketDataTimeoutMillis() {
      return _marketDataTimeoutMillis;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          return ((MarketDataSnapshotSaver) bean).getViewProcessor();
        case 10395716:  // configMaster
          return ((MarketDataSnapshotSaver) bean).getConfigMaster();
        case 2090650860:  // marketDataSnapshotMaster
          return ((MarketDataSnapshotSaver) bean).getMarketDataSnapshotMaster();
        case -168565795:  // snapshotter
          return ((MarketDataSnapshotSaver) bean).getSnapshotter();
        case -617269599:  // marketDataTimeoutMillis
          return ((MarketDataSnapshotSaver) bean).getMarketDataTimeoutMillis();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code MarketDataSnapshotSaver}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<MarketDataSnapshotSaver> {

    private ViewProcessor _viewProcessor;
    private ConfigMaster _configMaster;
    private MarketDataSnapshotMaster _marketDataSnapshotMaster;
    private MarketDataSnapshotter _snapshotter;
    private Long _marketDataTimeoutMillis;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(MarketDataSnapshotSaver beanToCopy) {
      this._viewProcessor = beanToCopy.getViewProcessor();
      this._configMaster = beanToCopy.getConfigMaster();
      this._marketDataSnapshotMaster = beanToCopy.getMarketDataSnapshotMaster();
      this._snapshotter = beanToCopy.getSnapshotter();
      this._marketDataTimeoutMillis = beanToCopy.getMarketDataTimeoutMillis();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case 10395716:  // configMaster
          return _configMaster;
        case 2090650860:  // marketDataSnapshotMaster
          return _marketDataSnapshotMaster;
        case -168565795:  // snapshotter
          return _snapshotter;
        case -617269599:  // marketDataTimeoutMillis
          return _marketDataTimeoutMillis;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          this._viewProcessor = (ViewProcessor) newValue;
          break;
        case 10395716:  // configMaster
          this._configMaster = (ConfigMaster) newValue;
          break;
        case 2090650860:  // marketDataSnapshotMaster
          this._marketDataSnapshotMaster = (MarketDataSnapshotMaster) newValue;
          break;
        case -168565795:  // snapshotter
          this._snapshotter = (MarketDataSnapshotter) newValue;
          break;
        case -617269599:  // marketDataTimeoutMillis
          this._marketDataTimeoutMillis = (Long) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public MarketDataSnapshotSaver build() {
      return new MarketDataSnapshotSaver(
          _viewProcessor,
          _configMaster,
          _marketDataSnapshotMaster,
          _snapshotter,
          _marketDataTimeoutMillis);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code viewProcessor} property in the builder.
     * @param viewProcessor  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder viewProcessor(ViewProcessor viewProcessor) {
      JodaBeanUtils.notNull(viewProcessor, "viewProcessor");
      this._viewProcessor = viewProcessor;
      return this;
    }

    /**
     * Sets the {@code configMaster} property in the builder.
     * @param configMaster  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder configMaster(ConfigMaster configMaster) {
      JodaBeanUtils.notNull(configMaster, "configMaster");
      this._configMaster = configMaster;
      return this;
    }

    /**
     * Sets the {@code marketDataSnapshotMaster} property in the builder.
     * @param marketDataSnapshotMaster  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder marketDataSnapshotMaster(MarketDataSnapshotMaster marketDataSnapshotMaster) {
      JodaBeanUtils.notNull(marketDataSnapshotMaster, "marketDataSnapshotMaster");
      this._marketDataSnapshotMaster = marketDataSnapshotMaster;
      return this;
    }

    /**
     * Sets the {@code snapshotter} property in the builder.
     * @param snapshotter  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder snapshotter(MarketDataSnapshotter snapshotter) {
      JodaBeanUtils.notNull(snapshotter, "snapshotter");
      this._snapshotter = snapshotter;
      return this;
    }

    /**
     * Sets the {@code marketDataTimeoutMillis} property in the builder.
     * @param marketDataTimeoutMillis  the new value
     * @return this, for chaining, not null
     */
    public Builder marketDataTimeoutMillis(Long marketDataTimeoutMillis) {
      this._marketDataTimeoutMillis = marketDataTimeoutMillis;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(192);
      buf.append("MarketDataSnapshotSaver.Builder{");
      buf.append("viewProcessor").append('=').append(JodaBeanUtils.toString(_viewProcessor)).append(',').append(' ');
      buf.append("configMaster").append('=').append(JodaBeanUtils.toString(_configMaster)).append(',').append(' ');
      buf.append("marketDataSnapshotMaster").append('=').append(JodaBeanUtils.toString(_marketDataSnapshotMaster)).append(',').append(' ');
      buf.append("snapshotter").append('=').append(JodaBeanUtils.toString(_snapshotter)).append(',').append(' ');
      buf.append("marketDataTimeoutMillis").append('=').append(JodaBeanUtils.toString(_marketDataTimeoutMillis));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
