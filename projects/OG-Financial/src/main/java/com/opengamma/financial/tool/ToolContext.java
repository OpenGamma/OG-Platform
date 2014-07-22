/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.tool;

import java.io.Closeable;
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

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.ReflectionUtils;

/**
 * A standard context that is used to provide components to tools.
 * <p>
 * This is populated and passed to tools that need component services. Each component is optional, although typically all are provided.
 */
@BeanDefinition
public class ToolContext extends DirectBean implements Closeable {

  /**
   * The manager that created this context. This is used by the {@link #close()} method.
   */
  @PropertyDefinition(set = "manual")
  private Object _contextManager;

  /**
   * The config master.
   */
  @PropertyDefinition
  private ConfigMaster _configMaster;
  /**
   * The exchange master.
   */
  @PropertyDefinition
  private ExchangeMaster _exchangeMaster;
  /**
   * The holiday master.
   */
  @PropertyDefinition
  private HolidayMaster _holidayMaster;
  /**
   * The region master.
   */
  @PropertyDefinition
  private RegionMaster _regionMaster;
  /**
   * The security master.
   */
  @PropertyDefinition
  private SecurityMaster _securityMaster;
  /**
   * The position master.
   */
  @PropertyDefinition
  private PositionMaster _positionMaster;
  /**
   * The portfolio master.
   */
  @PropertyDefinition
  private PortfolioMaster _portfolioMaster;
  /**
   * The legal entity master.
   */
  @PropertyDefinition
  private LegalEntityMaster _legalEntityMaster;
  /**
   * The historical time-series master.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  /**
   * The snapshot master.
   */
  @PropertyDefinition
  private MarketDataSnapshotMaster _marketDataSnapshotMaster;

  /**
   * The convention master.
   */
  @PropertyDefinition
  private ConventionMaster _conventionMaster;

  /**
   * The config source.
   */
  @PropertyDefinition
  private ConfigSource _configSource;
  /**
   * The exchange source.
   */
  @PropertyDefinition
  private ExchangeSource _exchangeSource;
  /**
   * The holiday source.
   */
  @PropertyDefinition
  private HolidaySource _holidaySource;
  /**
   * The region source.
   */
  @PropertyDefinition
  private RegionSource _regionSource;
  /**
   * The security source.
   */
  @PropertyDefinition
  private SecuritySource _securitySource;
  /**
   * The position source.
   */
  @PropertyDefinition
  private PositionSource _positionSource;
  /**
   * The organization source.
   */
  @PropertyDefinition
  private LegalEntitySource _legalEntitySource;
  /**
   * The historical time-series source.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The snapshot source.
   */
  @PropertyDefinition
  private MarketDataSnapshotSource _marketDataSnapshotSource;
  /**
   * The convention bundle source.
   */
  @PropertyDefinition
  private ConventionBundleSource _conventionBundleSource;
  /**
   * The convention source.
   */
  @PropertyDefinition
  private ConventionSource _conventionSource;
  /**
   * The security provider.
   */
  @PropertyDefinition
  private SecurityProvider _securityProvider;
  /**
   * The security loader.
   */
  @PropertyDefinition
  private SecurityLoader _securityLoader;
  /**
   * The time-series provider.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesProvider _historicalTimeSeriesProvider;
  /**
   * The time-series loader.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesLoader _historicalTimeSeriesLoader;

  /**
   * The view processor.
   */
  @PropertyDefinition
  private ViewProcessor _viewProcessor;

  /**
   * The available outputs provider.
   */
  @PropertyDefinition
  private AvailableOutputsProvider _avaliableOutputsProvider;
  
  /**
   * The function configuration source
   */
  @PropertyDefinition
  private FunctionConfigurationSource _functionConfigSource;

  /**
   * Creates an instance.
   */
  public ToolContext() {
  }

  //-------------------------------------------------------------------------
  /**
   * Closes the context, freeing any underlying resources. This calls the manager instance if present.
   */
  @Override
  public void close() {
    Object manager = getContextManager();
    ReflectionUtils.close(manager);
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the tool context, used to free any underlying resources.
   * <p>
   * The method {@link ReflectionUtils#isCloseable(Class)} must return true for the object. Call {@link #close()} to close the manager.
   * 
   * @param contextManager the context manager.
   */
  public void setContextManager(Object contextManager) {
    if (ReflectionUtils.isCloseable(contextManager.getClass()) == false) {
      throw new IllegalArgumentException("Object is not closeable: " + contextManager);
    }
    _contextManager = contextManager;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ToolContext}.
   * @return the meta-bean, not null
   */
  public static ToolContext.Meta meta() {
    return ToolContext.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ToolContext.Meta.INSTANCE);
  }

  @Override
  public ToolContext.Meta metaBean() {
    return ToolContext.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the manager that created this context. This is used by the {@link #close()} method.
   * @return the value of the property
   */
  public Object getContextManager() {
    return _contextManager;
  }

  /**
   * Gets the the {@code contextManager} property.
   * @return the property, not null
   */
  public final Property<Object> contextManager() {
    return metaBean().contextManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the value of the property
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master.
   * @param configMaster  the new value of the property
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange master.
   * @return the value of the property
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  /**
   * Sets the exchange master.
   * @param exchangeMaster  the new value of the property
   */
  public void setExchangeMaster(ExchangeMaster exchangeMaster) {
    this._exchangeMaster = exchangeMaster;
  }

  /**
   * Gets the the {@code exchangeMaster} property.
   * @return the property, not null
   */
  public final Property<ExchangeMaster> exchangeMaster() {
    return metaBean().exchangeMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday master.
   * @return the value of the property
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Sets the holiday master.
   * @param holidayMaster  the new value of the property
   */
  public void setHolidayMaster(HolidayMaster holidayMaster) {
    this._holidayMaster = holidayMaster;
  }

  /**
   * Gets the the {@code holidayMaster} property.
   * @return the property, not null
   */
  public final Property<HolidayMaster> holidayMaster() {
    return metaBean().holidayMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region master.
   * @return the value of the property
   */
  public RegionMaster getRegionMaster() {
    return _regionMaster;
  }

  /**
   * Sets the region master.
   * @param regionMaster  the new value of the property
   */
  public void setRegionMaster(RegionMaster regionMaster) {
    this._regionMaster = regionMaster;
  }

  /**
   * Gets the the {@code regionMaster} property.
   * @return the property, not null
   */
  public final Property<RegionMaster> regionMaster() {
    return metaBean().regionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the value of the property
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the new value of the property
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    this._securityMaster = securityMaster;
  }

  /**
   * Gets the the {@code securityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> securityMaster() {
    return metaBean().securityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the value of the property
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the new value of the property
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    this._positionMaster = positionMaster;
  }

  /**
   * Gets the the {@code positionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> positionMaster() {
    return metaBean().positionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * @return the value of the property
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the portfolio master.
   * @param portfolioMaster  the new value of the property
   */
  public void setPortfolioMaster(PortfolioMaster portfolioMaster) {
    this._portfolioMaster = portfolioMaster;
  }

  /**
   * Gets the the {@code portfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> portfolioMaster() {
    return metaBean().portfolioMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the legal entity master.
   * @return the value of the property
   */
  public LegalEntityMaster getLegalEntityMaster() {
    return _legalEntityMaster;
  }

  /**
   * Sets the legal entity master.
   * @param legalEntityMaster  the new value of the property
   */
  public void setLegalEntityMaster(LegalEntityMaster legalEntityMaster) {
    this._legalEntityMaster = legalEntityMaster;
  }

  /**
   * Gets the the {@code legalEntityMaster} property.
   * @return the property, not null
   */
  public final Property<LegalEntityMaster> legalEntityMaster() {
    return metaBean().legalEntityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the historical time-series master.
   * @return the value of the property
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Sets the historical time-series master.
   * @param historicalTimeSeriesMaster  the new value of the property
   */
  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    this._historicalTimeSeriesMaster = historicalTimeSeriesMaster;
  }

  /**
   * Gets the the {@code historicalTimeSeriesMaster} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
    return metaBean().historicalTimeSeriesMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshot master.
   * @return the value of the property
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _marketDataSnapshotMaster;
  }

  /**
   * Sets the snapshot master.
   * @param marketDataSnapshotMaster  the new value of the property
   */
  public void setMarketDataSnapshotMaster(MarketDataSnapshotMaster marketDataSnapshotMaster) {
    this._marketDataSnapshotMaster = marketDataSnapshotMaster;
  }

  /**
   * Gets the the {@code marketDataSnapshotMaster} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
    return metaBean().marketDataSnapshotMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention master.
   * @return the value of the property
   */
  public ConventionMaster getConventionMaster() {
    return _conventionMaster;
  }

  /**
   * Sets the convention master.
   * @param conventionMaster  the new value of the property
   */
  public void setConventionMaster(ConventionMaster conventionMaster) {
    this._conventionMaster = conventionMaster;
  }

  /**
   * Gets the the {@code conventionMaster} property.
   * @return the property, not null
   */
  public final Property<ConventionMaster> conventionMaster() {
    return metaBean().conventionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source.
   * @return the value of the property
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source.
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
   * Gets the exchange source.
   * @return the value of the property
   */
  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  /**
   * Sets the exchange source.
   * @param exchangeSource  the new value of the property
   */
  public void setExchangeSource(ExchangeSource exchangeSource) {
    this._exchangeSource = exchangeSource;
  }

  /**
   * Gets the the {@code exchangeSource} property.
   * @return the property, not null
   */
  public final Property<ExchangeSource> exchangeSource() {
    return metaBean().exchangeSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday source.
   * @return the value of the property
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Sets the holiday source.
   * @param holidaySource  the new value of the property
   */
  public void setHolidaySource(HolidaySource holidaySource) {
    this._holidaySource = holidaySource;
  }

  /**
   * Gets the the {@code holidaySource} property.
   * @return the property, not null
   */
  public final Property<HolidaySource> holidaySource() {
    return metaBean().holidaySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region source.
   * @return the value of the property
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  /**
   * Sets the region source.
   * @param regionSource  the new value of the property
   */
  public void setRegionSource(RegionSource regionSource) {
    this._regionSource = regionSource;
  }

  /**
   * Gets the the {@code regionSource} property.
   * @return the property, not null
   */
  public final Property<RegionSource> regionSource() {
    return metaBean().regionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security source.
   * @return the value of the property
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the security source.
   * @param securitySource  the new value of the property
   */
  public void setSecuritySource(SecuritySource securitySource) {
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
   * @return the value of the property
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Sets the position source.
   * @param positionSource  the new value of the property
   */
  public void setPositionSource(PositionSource positionSource) {
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
   * Gets the organization source.
   * @return the value of the property
   */
  public LegalEntitySource getLegalEntitySource() {
    return _legalEntitySource;
  }

  /**
   * Sets the organization source.
   * @param legalEntitySource  the new value of the property
   */
  public void setLegalEntitySource(LegalEntitySource legalEntitySource) {
    this._legalEntitySource = legalEntitySource;
  }

  /**
   * Gets the the {@code legalEntitySource} property.
   * @return the property, not null
   */
  public final Property<LegalEntitySource> legalEntitySource() {
    return metaBean().legalEntitySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the historical time-series source.
   * @return the value of the property
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  /**
   * Sets the historical time-series source.
   * @param historicalTimeSeriesSource  the new value of the property
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    this._historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  /**
   * Gets the the {@code historicalTimeSeriesSource} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
    return metaBean().historicalTimeSeriesSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshot source.
   * @return the value of the property
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource() {
    return _marketDataSnapshotSource;
  }

  /**
   * Sets the snapshot source.
   * @param marketDataSnapshotSource  the new value of the property
   */
  public void setMarketDataSnapshotSource(MarketDataSnapshotSource marketDataSnapshotSource) {
    this._marketDataSnapshotSource = marketDataSnapshotSource;
  }

  /**
   * Gets the the {@code marketDataSnapshotSource} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotSource> marketDataSnapshotSource() {
    return metaBean().marketDataSnapshotSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention bundle source.
   * @return the value of the property
   */
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  /**
   * Sets the convention bundle source.
   * @param conventionBundleSource  the new value of the property
   */
  public void setConventionBundleSource(ConventionBundleSource conventionBundleSource) {
    this._conventionBundleSource = conventionBundleSource;
  }

  /**
   * Gets the the {@code conventionBundleSource} property.
   * @return the property, not null
   */
  public final Property<ConventionBundleSource> conventionBundleSource() {
    return metaBean().conventionBundleSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention source.
   * @return the value of the property
   */
  public ConventionSource getConventionSource() {
    return _conventionSource;
  }

  /**
   * Sets the convention source.
   * @param conventionSource  the new value of the property
   */
  public void setConventionSource(ConventionSource conventionSource) {
    this._conventionSource = conventionSource;
  }

  /**
   * Gets the the {@code conventionSource} property.
   * @return the property, not null
   */
  public final Property<ConventionSource> conventionSource() {
    return metaBean().conventionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security provider.
   * @return the value of the property
   */
  public SecurityProvider getSecurityProvider() {
    return _securityProvider;
  }

  /**
   * Sets the security provider.
   * @param securityProvider  the new value of the property
   */
  public void setSecurityProvider(SecurityProvider securityProvider) {
    this._securityProvider = securityProvider;
  }

  /**
   * Gets the the {@code securityProvider} property.
   * @return the property, not null
   */
  public final Property<SecurityProvider> securityProvider() {
    return metaBean().securityProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security loader.
   * @return the value of the property
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

  /**
   * Sets the security loader.
   * @param securityLoader  the new value of the property
   */
  public void setSecurityLoader(SecurityLoader securityLoader) {
    this._securityLoader = securityLoader;
  }

  /**
   * Gets the the {@code securityLoader} property.
   * @return the property, not null
   */
  public final Property<SecurityLoader> securityLoader() {
    return metaBean().securityLoader().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series provider.
   * @return the value of the property
   */
  public HistoricalTimeSeriesProvider getHistoricalTimeSeriesProvider() {
    return _historicalTimeSeriesProvider;
  }

  /**
   * Sets the time-series provider.
   * @param historicalTimeSeriesProvider  the new value of the property
   */
  public void setHistoricalTimeSeriesProvider(HistoricalTimeSeriesProvider historicalTimeSeriesProvider) {
    this._historicalTimeSeriesProvider = historicalTimeSeriesProvider;
  }

  /**
   * Gets the the {@code historicalTimeSeriesProvider} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesProvider> historicalTimeSeriesProvider() {
    return metaBean().historicalTimeSeriesProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series loader.
   * @return the value of the property
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader() {
    return _historicalTimeSeriesLoader;
  }

  /**
   * Sets the time-series loader.
   * @param historicalTimeSeriesLoader  the new value of the property
   */
  public void setHistoricalTimeSeriesLoader(HistoricalTimeSeriesLoader historicalTimeSeriesLoader) {
    this._historicalTimeSeriesLoader = historicalTimeSeriesLoader;
  }

  /**
   * Gets the the {@code historicalTimeSeriesLoader} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesLoader> historicalTimeSeriesLoader() {
    return metaBean().historicalTimeSeriesLoader().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view processor.
   * @return the value of the property
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Sets the view processor.
   * @param viewProcessor  the new value of the property
   */
  public void setViewProcessor(ViewProcessor viewProcessor) {
    this._viewProcessor = viewProcessor;
  }

  /**
   * Gets the the {@code viewProcessor} property.
   * @return the property, not null
   */
  public final Property<ViewProcessor> viewProcessor() {
    return metaBean().viewProcessor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the available outputs provider.
   * @return the value of the property
   */
  public AvailableOutputsProvider getAvaliableOutputsProvider() {
    return _avaliableOutputsProvider;
  }

  /**
   * Sets the available outputs provider.
   * @param avaliableOutputsProvider  the new value of the property
   */
  public void setAvaliableOutputsProvider(AvailableOutputsProvider avaliableOutputsProvider) {
    this._avaliableOutputsProvider = avaliableOutputsProvider;
  }

  /**
   * Gets the the {@code avaliableOutputsProvider} property.
   * @return the property, not null
   */
  public final Property<AvailableOutputsProvider> avaliableOutputsProvider() {
    return metaBean().avaliableOutputsProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the function configuration source
   * @return the value of the property
   */
  public FunctionConfigurationSource getFunctionConfigSource() {
    return _functionConfigSource;
  }

  /**
   * Sets the function configuration source
   * @param functionConfigSource  the new value of the property
   */
  public void setFunctionConfigSource(FunctionConfigurationSource functionConfigSource) {
    this._functionConfigSource = functionConfigSource;
  }

  /**
   * Gets the the {@code functionConfigSource} property.
   * @return the property, not null
   */
  public final Property<FunctionConfigurationSource> functionConfigSource() {
    return metaBean().functionConfigSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ToolContext clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ToolContext other = (ToolContext) obj;
      return JodaBeanUtils.equal(getContextManager(), other.getContextManager()) &&
          JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getExchangeMaster(), other.getExchangeMaster()) &&
          JodaBeanUtils.equal(getHolidayMaster(), other.getHolidayMaster()) &&
          JodaBeanUtils.equal(getRegionMaster(), other.getRegionMaster()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getPortfolioMaster(), other.getPortfolioMaster()) &&
          JodaBeanUtils.equal(getLegalEntityMaster(), other.getLegalEntityMaster()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesMaster(), other.getHistoricalTimeSeriesMaster()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotMaster(), other.getMarketDataSnapshotMaster()) &&
          JodaBeanUtils.equal(getConventionMaster(), other.getConventionMaster()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(getExchangeSource(), other.getExchangeSource()) &&
          JodaBeanUtils.equal(getHolidaySource(), other.getHolidaySource()) &&
          JodaBeanUtils.equal(getRegionSource(), other.getRegionSource()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getLegalEntitySource(), other.getLegalEntitySource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotSource(), other.getMarketDataSnapshotSource()) &&
          JodaBeanUtils.equal(getConventionBundleSource(), other.getConventionBundleSource()) &&
          JodaBeanUtils.equal(getConventionSource(), other.getConventionSource()) &&
          JodaBeanUtils.equal(getSecurityProvider(), other.getSecurityProvider()) &&
          JodaBeanUtils.equal(getSecurityLoader(), other.getSecurityLoader()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesProvider(), other.getHistoricalTimeSeriesProvider()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesLoader(), other.getHistoricalTimeSeriesLoader()) &&
          JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getAvaliableOutputsProvider(), other.getAvaliableOutputsProvider()) &&
          JodaBeanUtils.equal(getFunctionConfigSource(), other.getFunctionConfigSource());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getContextManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPortfolioMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLegalEntityMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidaySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLegalEntitySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionBundleSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityLoader());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesLoader());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAvaliableOutputsProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFunctionConfigSource());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(992);
    buf.append("ToolContext{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("contextManager").append('=').append(JodaBeanUtils.toString(getContextManager())).append(',').append(' ');
    buf.append("configMaster").append('=').append(JodaBeanUtils.toString(getConfigMaster())).append(',').append(' ');
    buf.append("exchangeMaster").append('=').append(JodaBeanUtils.toString(getExchangeMaster())).append(',').append(' ');
    buf.append("holidayMaster").append('=').append(JodaBeanUtils.toString(getHolidayMaster())).append(',').append(' ');
    buf.append("regionMaster").append('=').append(JodaBeanUtils.toString(getRegionMaster())).append(',').append(' ');
    buf.append("securityMaster").append('=').append(JodaBeanUtils.toString(getSecurityMaster())).append(',').append(' ');
    buf.append("positionMaster").append('=').append(JodaBeanUtils.toString(getPositionMaster())).append(',').append(' ');
    buf.append("portfolioMaster").append('=').append(JodaBeanUtils.toString(getPortfolioMaster())).append(',').append(' ');
    buf.append("legalEntityMaster").append('=').append(JodaBeanUtils.toString(getLegalEntityMaster())).append(',').append(' ');
    buf.append("historicalTimeSeriesMaster").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesMaster())).append(',').append(' ');
    buf.append("marketDataSnapshotMaster").append('=').append(JodaBeanUtils.toString(getMarketDataSnapshotMaster())).append(',').append(' ');
    buf.append("conventionMaster").append('=').append(JodaBeanUtils.toString(getConventionMaster())).append(',').append(' ');
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
    buf.append("exchangeSource").append('=').append(JodaBeanUtils.toString(getExchangeSource())).append(',').append(' ');
    buf.append("holidaySource").append('=').append(JodaBeanUtils.toString(getHolidaySource())).append(',').append(' ');
    buf.append("regionSource").append('=').append(JodaBeanUtils.toString(getRegionSource())).append(',').append(' ');
    buf.append("securitySource").append('=').append(JodaBeanUtils.toString(getSecuritySource())).append(',').append(' ');
    buf.append("positionSource").append('=').append(JodaBeanUtils.toString(getPositionSource())).append(',').append(' ');
    buf.append("legalEntitySource").append('=').append(JodaBeanUtils.toString(getLegalEntitySource())).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesSource())).append(',').append(' ');
    buf.append("marketDataSnapshotSource").append('=').append(JodaBeanUtils.toString(getMarketDataSnapshotSource())).append(',').append(' ');
    buf.append("conventionBundleSource").append('=').append(JodaBeanUtils.toString(getConventionBundleSource())).append(',').append(' ');
    buf.append("conventionSource").append('=').append(JodaBeanUtils.toString(getConventionSource())).append(',').append(' ');
    buf.append("securityProvider").append('=').append(JodaBeanUtils.toString(getSecurityProvider())).append(',').append(' ');
    buf.append("securityLoader").append('=').append(JodaBeanUtils.toString(getSecurityLoader())).append(',').append(' ');
    buf.append("historicalTimeSeriesProvider").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesProvider())).append(',').append(' ');
    buf.append("historicalTimeSeriesLoader").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesLoader())).append(',').append(' ');
    buf.append("viewProcessor").append('=').append(JodaBeanUtils.toString(getViewProcessor())).append(',').append(' ');
    buf.append("avaliableOutputsProvider").append('=').append(JodaBeanUtils.toString(getAvaliableOutputsProvider())).append(',').append(' ');
    buf.append("functionConfigSource").append('=').append(JodaBeanUtils.toString(getFunctionConfigSource())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ToolContext}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code contextManager} property.
     */
    private final MetaProperty<Object> _contextManager = DirectMetaProperty.ofReadWrite(
        this, "contextManager", ToolContext.class, Object.class);
    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", ToolContext.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code exchangeMaster} property.
     */
    private final MetaProperty<ExchangeMaster> _exchangeMaster = DirectMetaProperty.ofReadWrite(
        this, "exchangeMaster", ToolContext.class, ExchangeMaster.class);
    /**
     * The meta-property for the {@code holidayMaster} property.
     */
    private final MetaProperty<HolidayMaster> _holidayMaster = DirectMetaProperty.ofReadWrite(
        this, "holidayMaster", ToolContext.class, HolidayMaster.class);
    /**
     * The meta-property for the {@code regionMaster} property.
     */
    private final MetaProperty<RegionMaster> _regionMaster = DirectMetaProperty.ofReadWrite(
        this, "regionMaster", ToolContext.class, RegionMaster.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", ToolContext.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", ToolContext.class, PositionMaster.class);
    /**
     * The meta-property for the {@code portfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _portfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "portfolioMaster", ToolContext.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code legalEntityMaster} property.
     */
    private final MetaProperty<LegalEntityMaster> _legalEntityMaster = DirectMetaProperty.ofReadWrite(
        this, "legalEntityMaster", ToolContext.class, LegalEntityMaster.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     */
    private final MetaProperty<HistoricalTimeSeriesMaster> _historicalTimeSeriesMaster = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesMaster", ToolContext.class, HistoricalTimeSeriesMaster.class);
    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _marketDataSnapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotMaster", ToolContext.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code conventionMaster} property.
     */
    private final MetaProperty<ConventionMaster> _conventionMaster = DirectMetaProperty.ofReadWrite(
        this, "conventionMaster", ToolContext.class, ConventionMaster.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", ToolContext.class, ConfigSource.class);
    /**
     * The meta-property for the {@code exchangeSource} property.
     */
    private final MetaProperty<ExchangeSource> _exchangeSource = DirectMetaProperty.ofReadWrite(
        this, "exchangeSource", ToolContext.class, ExchangeSource.class);
    /**
     * The meta-property for the {@code holidaySource} property.
     */
    private final MetaProperty<HolidaySource> _holidaySource = DirectMetaProperty.ofReadWrite(
        this, "holidaySource", ToolContext.class, HolidaySource.class);
    /**
     * The meta-property for the {@code regionSource} property.
     */
    private final MetaProperty<RegionSource> _regionSource = DirectMetaProperty.ofReadWrite(
        this, "regionSource", ToolContext.class, RegionSource.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", ToolContext.class, SecuritySource.class);
    /**
     * The meta-property for the {@code positionSource} property.
     */
    private final MetaProperty<PositionSource> _positionSource = DirectMetaProperty.ofReadWrite(
        this, "positionSource", ToolContext.class, PositionSource.class);
    /**
     * The meta-property for the {@code legalEntitySource} property.
     */
    private final MetaProperty<LegalEntitySource> _legalEntitySource = DirectMetaProperty.ofReadWrite(
        this, "legalEntitySource", ToolContext.class, LegalEntitySource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", ToolContext.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     */
    private final MetaProperty<MarketDataSnapshotSource> _marketDataSnapshotSource = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotSource", ToolContext.class, MarketDataSnapshotSource.class);
    /**
     * The meta-property for the {@code conventionBundleSource} property.
     */
    private final MetaProperty<ConventionBundleSource> _conventionBundleSource = DirectMetaProperty.ofReadWrite(
        this, "conventionBundleSource", ToolContext.class, ConventionBundleSource.class);
    /**
     * The meta-property for the {@code conventionSource} property.
     */
    private final MetaProperty<ConventionSource> _conventionSource = DirectMetaProperty.ofReadWrite(
        this, "conventionSource", ToolContext.class, ConventionSource.class);
    /**
     * The meta-property for the {@code securityProvider} property.
     */
    private final MetaProperty<SecurityProvider> _securityProvider = DirectMetaProperty.ofReadWrite(
        this, "securityProvider", ToolContext.class, SecurityProvider.class);
    /**
     * The meta-property for the {@code securityLoader} property.
     */
    private final MetaProperty<SecurityLoader> _securityLoader = DirectMetaProperty.ofReadWrite(
        this, "securityLoader", ToolContext.class, SecurityLoader.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesProvider} property.
     */
    private final MetaProperty<HistoricalTimeSeriesProvider> _historicalTimeSeriesProvider = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesProvider", ToolContext.class, HistoricalTimeSeriesProvider.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesLoader} property.
     */
    private final MetaProperty<HistoricalTimeSeriesLoader> _historicalTimeSeriesLoader = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesLoader", ToolContext.class, HistoricalTimeSeriesLoader.class);
    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", ToolContext.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code avaliableOutputsProvider} property.
     */
    private final MetaProperty<AvailableOutputsProvider> _avaliableOutputsProvider = DirectMetaProperty.ofReadWrite(
        this, "avaliableOutputsProvider", ToolContext.class, AvailableOutputsProvider.class);
    /**
     * The meta-property for the {@code functionConfigSource} property.
     */
    private final MetaProperty<FunctionConfigurationSource> _functionConfigSource = DirectMetaProperty.ofReadWrite(
        this, "functionConfigSource", ToolContext.class, FunctionConfigurationSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "contextManager",
        "configMaster",
        "exchangeMaster",
        "holidayMaster",
        "regionMaster",
        "securityMaster",
        "positionMaster",
        "portfolioMaster",
        "legalEntityMaster",
        "historicalTimeSeriesMaster",
        "marketDataSnapshotMaster",
        "conventionMaster",
        "configSource",
        "exchangeSource",
        "holidaySource",
        "regionSource",
        "securitySource",
        "positionSource",
        "legalEntitySource",
        "historicalTimeSeriesSource",
        "marketDataSnapshotSource",
        "conventionBundleSource",
        "conventionSource",
        "securityProvider",
        "securityLoader",
        "historicalTimeSeriesProvider",
        "historicalTimeSeriesLoader",
        "viewProcessor",
        "avaliableOutputsProvider",
        "functionConfigSource");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 295862014:  // contextManager
          return _contextManager;
        case 10395716:  // configMaster
          return _configMaster;
        case -652001691:  // exchangeMaster
          return _exchangeMaster;
        case 246258906:  // holidayMaster
          return _holidayMaster;
        case -1820969354:  // regionMaster
          return _regionMaster;
        case -887218750:  // securityMaster
          return _securityMaster;
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -772274742:  // portfolioMaster
          return _portfolioMaster;
        case -1944474242:  // legalEntityMaster
          return _legalEntityMaster;
        case 173967376:  // historicalTimeSeriesMaster
          return _historicalTimeSeriesMaster;
        case 2090650860:  // marketDataSnapshotMaster
          return _marketDataSnapshotMaster;
        case 41113907:  // conventionMaster
          return _conventionMaster;
        case 195157501:  // configSource
          return _configSource;
        case -467239906:  // exchangeSource
          return _exchangeSource;
        case 431020691:  // holidaySource
          return _holidaySource;
        case -1636207569:  // regionSource
          return _regionSource;
        case -702456965:  // securitySource
          return _securitySource;
        case -1655657820:  // positionSource
          return _positionSource;
        case -1759712457:  // legalEntitySource
          return _legalEntitySource;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case -2019554651:  // marketDataSnapshotSource
          return _marketDataSnapshotSource;
        case -1281578674:  // conventionBundleSource
          return _conventionBundleSource;
        case 225875692:  // conventionSource
          return _conventionSource;
        case 809869649:  // securityProvider
          return _securityProvider;
        case -903470221:  // securityLoader
          return _securityLoader;
        case -1592479713:  // historicalTimeSeriesProvider
          return _historicalTimeSeriesProvider;
        case 157715905:  // historicalTimeSeriesLoader
          return _historicalTimeSeriesLoader;
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case -1252442368:  // avaliableOutputsProvider
          return _avaliableOutputsProvider;
        case -2085248523:  // functionConfigSource
          return _functionConfigSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ToolContext> builder() {
      return new DirectBeanBuilder<ToolContext>(new ToolContext());
    }

    @Override
    public Class<? extends ToolContext> beanType() {
      return ToolContext.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code contextManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Object> contextManager() {
      return _contextManager;
    }

    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    /**
     * The meta-property for the {@code exchangeMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeMaster> exchangeMaster() {
      return _exchangeMaster;
    }

    /**
     * The meta-property for the {@code holidayMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayMaster> holidayMaster() {
      return _holidayMaster;
    }

    /**
     * The meta-property for the {@code regionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionMaster> regionMaster() {
      return _regionMaster;
    }

    /**
     * The meta-property for the {@code securityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> securityMaster() {
      return _securityMaster;
    }

    /**
     * The meta-property for the {@code positionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> positionMaster() {
      return _positionMaster;
    }

    /**
     * The meta-property for the {@code portfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> portfolioMaster() {
      return _portfolioMaster;
    }

    /**
     * The meta-property for the {@code legalEntityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LegalEntityMaster> legalEntityMaster() {
      return _legalEntityMaster;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
      return _historicalTimeSeriesMaster;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
      return _marketDataSnapshotMaster;
    }

    /**
     * The meta-property for the {@code conventionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionMaster> conventionMaster() {
      return _conventionMaster;
    }

    /**
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    /**
     * The meta-property for the {@code exchangeSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeSource> exchangeSource() {
      return _exchangeSource;
    }

    /**
     * The meta-property for the {@code holidaySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidaySource> holidaySource() {
      return _holidaySource;
    }

    /**
     * The meta-property for the {@code regionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionSource> regionSource() {
      return _regionSource;
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
     * The meta-property for the {@code legalEntitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LegalEntitySource> legalEntitySource() {
      return _legalEntitySource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotSource> marketDataSnapshotSource() {
      return _marketDataSnapshotSource;
    }

    /**
     * The meta-property for the {@code conventionBundleSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionBundleSource> conventionBundleSource() {
      return _conventionBundleSource;
    }

    /**
     * The meta-property for the {@code conventionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionSource> conventionSource() {
      return _conventionSource;
    }

    /**
     * The meta-property for the {@code securityProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityProvider> securityProvider() {
      return _securityProvider;
    }

    /**
     * The meta-property for the {@code securityLoader} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityLoader> securityLoader() {
      return _securityLoader;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesProvider> historicalTimeSeriesProvider() {
      return _historicalTimeSeriesProvider;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesLoader} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesLoader> historicalTimeSeriesLoader() {
      return _historicalTimeSeriesLoader;
    }

    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code avaliableOutputsProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<AvailableOutputsProvider> avaliableOutputsProvider() {
      return _avaliableOutputsProvider;
    }

    /**
     * The meta-property for the {@code functionConfigSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FunctionConfigurationSource> functionConfigSource() {
      return _functionConfigSource;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 295862014:  // contextManager
          return ((ToolContext) bean).getContextManager();
        case 10395716:  // configMaster
          return ((ToolContext) bean).getConfigMaster();
        case -652001691:  // exchangeMaster
          return ((ToolContext) bean).getExchangeMaster();
        case 246258906:  // holidayMaster
          return ((ToolContext) bean).getHolidayMaster();
        case -1820969354:  // regionMaster
          return ((ToolContext) bean).getRegionMaster();
        case -887218750:  // securityMaster
          return ((ToolContext) bean).getSecurityMaster();
        case -1840419605:  // positionMaster
          return ((ToolContext) bean).getPositionMaster();
        case -772274742:  // portfolioMaster
          return ((ToolContext) bean).getPortfolioMaster();
        case -1944474242:  // legalEntityMaster
          return ((ToolContext) bean).getLegalEntityMaster();
        case 173967376:  // historicalTimeSeriesMaster
          return ((ToolContext) bean).getHistoricalTimeSeriesMaster();
        case 2090650860:  // marketDataSnapshotMaster
          return ((ToolContext) bean).getMarketDataSnapshotMaster();
        case 41113907:  // conventionMaster
          return ((ToolContext) bean).getConventionMaster();
        case 195157501:  // configSource
          return ((ToolContext) bean).getConfigSource();
        case -467239906:  // exchangeSource
          return ((ToolContext) bean).getExchangeSource();
        case 431020691:  // holidaySource
          return ((ToolContext) bean).getHolidaySource();
        case -1636207569:  // regionSource
          return ((ToolContext) bean).getRegionSource();
        case -702456965:  // securitySource
          return ((ToolContext) bean).getSecuritySource();
        case -1655657820:  // positionSource
          return ((ToolContext) bean).getPositionSource();
        case -1759712457:  // legalEntitySource
          return ((ToolContext) bean).getLegalEntitySource();
        case 358729161:  // historicalTimeSeriesSource
          return ((ToolContext) bean).getHistoricalTimeSeriesSource();
        case -2019554651:  // marketDataSnapshotSource
          return ((ToolContext) bean).getMarketDataSnapshotSource();
        case -1281578674:  // conventionBundleSource
          return ((ToolContext) bean).getConventionBundleSource();
        case 225875692:  // conventionSource
          return ((ToolContext) bean).getConventionSource();
        case 809869649:  // securityProvider
          return ((ToolContext) bean).getSecurityProvider();
        case -903470221:  // securityLoader
          return ((ToolContext) bean).getSecurityLoader();
        case -1592479713:  // historicalTimeSeriesProvider
          return ((ToolContext) bean).getHistoricalTimeSeriesProvider();
        case 157715905:  // historicalTimeSeriesLoader
          return ((ToolContext) bean).getHistoricalTimeSeriesLoader();
        case -1697555603:  // viewProcessor
          return ((ToolContext) bean).getViewProcessor();
        case -1252442368:  // avaliableOutputsProvider
          return ((ToolContext) bean).getAvaliableOutputsProvider();
        case -2085248523:  // functionConfigSource
          return ((ToolContext) bean).getFunctionConfigSource();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 295862014:  // contextManager
          ((ToolContext) bean).setContextManager((Object) newValue);
          return;
        case 10395716:  // configMaster
          ((ToolContext) bean).setConfigMaster((ConfigMaster) newValue);
          return;
        case -652001691:  // exchangeMaster
          ((ToolContext) bean).setExchangeMaster((ExchangeMaster) newValue);
          return;
        case 246258906:  // holidayMaster
          ((ToolContext) bean).setHolidayMaster((HolidayMaster) newValue);
          return;
        case -1820969354:  // regionMaster
          ((ToolContext) bean).setRegionMaster((RegionMaster) newValue);
          return;
        case -887218750:  // securityMaster
          ((ToolContext) bean).setSecurityMaster((SecurityMaster) newValue);
          return;
        case -1840419605:  // positionMaster
          ((ToolContext) bean).setPositionMaster((PositionMaster) newValue);
          return;
        case -772274742:  // portfolioMaster
          ((ToolContext) bean).setPortfolioMaster((PortfolioMaster) newValue);
          return;
        case -1944474242:  // legalEntityMaster
          ((ToolContext) bean).setLegalEntityMaster((LegalEntityMaster) newValue);
          return;
        case 173967376:  // historicalTimeSeriesMaster
          ((ToolContext) bean).setHistoricalTimeSeriesMaster((HistoricalTimeSeriesMaster) newValue);
          return;
        case 2090650860:  // marketDataSnapshotMaster
          ((ToolContext) bean).setMarketDataSnapshotMaster((MarketDataSnapshotMaster) newValue);
          return;
        case 41113907:  // conventionMaster
          ((ToolContext) bean).setConventionMaster((ConventionMaster) newValue);
          return;
        case 195157501:  // configSource
          ((ToolContext) bean).setConfigSource((ConfigSource) newValue);
          return;
        case -467239906:  // exchangeSource
          ((ToolContext) bean).setExchangeSource((ExchangeSource) newValue);
          return;
        case 431020691:  // holidaySource
          ((ToolContext) bean).setHolidaySource((HolidaySource) newValue);
          return;
        case -1636207569:  // regionSource
          ((ToolContext) bean).setRegionSource((RegionSource) newValue);
          return;
        case -702456965:  // securitySource
          ((ToolContext) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case -1655657820:  // positionSource
          ((ToolContext) bean).setPositionSource((PositionSource) newValue);
          return;
        case -1759712457:  // legalEntitySource
          ((ToolContext) bean).setLegalEntitySource((LegalEntitySource) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((ToolContext) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case -2019554651:  // marketDataSnapshotSource
          ((ToolContext) bean).setMarketDataSnapshotSource((MarketDataSnapshotSource) newValue);
          return;
        case -1281578674:  // conventionBundleSource
          ((ToolContext) bean).setConventionBundleSource((ConventionBundleSource) newValue);
          return;
        case 225875692:  // conventionSource
          ((ToolContext) bean).setConventionSource((ConventionSource) newValue);
          return;
        case 809869649:  // securityProvider
          ((ToolContext) bean).setSecurityProvider((SecurityProvider) newValue);
          return;
        case -903470221:  // securityLoader
          ((ToolContext) bean).setSecurityLoader((SecurityLoader) newValue);
          return;
        case -1592479713:  // historicalTimeSeriesProvider
          ((ToolContext) bean).setHistoricalTimeSeriesProvider((HistoricalTimeSeriesProvider) newValue);
          return;
        case 157715905:  // historicalTimeSeriesLoader
          ((ToolContext) bean).setHistoricalTimeSeriesLoader((HistoricalTimeSeriesLoader) newValue);
          return;
        case -1697555603:  // viewProcessor
          ((ToolContext) bean).setViewProcessor((ViewProcessor) newValue);
          return;
        case -1252442368:  // avaliableOutputsProvider
          ((ToolContext) bean).setAvaliableOutputsProvider((AvailableOutputsProvider) newValue);
          return;
        case -2085248523:  // functionConfigSource
          ((ToolContext) bean).setFunctionConfigSource((FunctionConfigurationSource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
