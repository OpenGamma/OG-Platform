/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.batch.BatchMaster;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.rest.JerseyRestResourceFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.target.ComputationTargetTypeProvider;
import com.opengamma.engine.target.DefaultComputationTargetTypeProvider;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.web.WebAboutResource;
import com.opengamma.web.WebHomeResource;
import com.opengamma.web.analytics.rest.LiveMarketDataProviderNamesResource;
import com.opengamma.web.analytics.rest.LiveMarketDataSpecificationNamesResource;
import com.opengamma.web.config.WebConfigsResource;
import com.opengamma.web.exchange.WebExchangesResource;
import com.opengamma.web.historicaltimeseries.WebAllHistoricalTimeSeriesResource;
import com.opengamma.web.holiday.WebHolidaysResource;
import com.opengamma.web.marketdatasnapshot.WebMarketDataSnapshotsResource;
import com.opengamma.web.orgs.WebOrganizationsResource;
import com.opengamma.web.portfolio.WebPortfoliosResource;
import com.opengamma.web.position.WebPositionsResource;
import com.opengamma.web.region.WebRegionsResource;
import com.opengamma.web.security.WebSecuritiesResource;
import com.opengamma.web.target.WebComputationTargetTypeResource;
import com.opengamma.web.valuerequirementname.WebValueRequirementNamesResource;

/**
 * Component factory for the main website.
 */
@BeanDefinition
public class WebsiteBasicsComponentFactory extends AbstractComponentFactory {

  /**
   * The config master.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigMaster _configMaster;
  /**
   * The exchange master.
   */
  @PropertyDefinition(validate = "notNull")
  private ExchangeMaster _exchangeMaster;
  /**
   * The holiday master.
   */
  @PropertyDefinition(validate = "notNull")
  private HolidayMaster _holidayMaster;
  /**
   * The underlying master.
   */
  @PropertyDefinition(validate = "notNull")
  private RegionMaster _regionMaster;
  /**
   * The security master.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _securityMaster;
  /**
   * The security source.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySource _securitySource;
  /**
   * The security loader.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityLoader _securityLoader;
  /**
   * The position master.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionMaster _positionMaster;
  /**
   * The portfolio master.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioMaster _portfolioMaster;
  /**
   * The batch master.
   */
  @PropertyDefinition(validate = "notNull")
  private BatchMaster _batchMaster;
  /**
   * The time-series master.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  /**
   * The time-series source.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The time-series loader.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesLoader _historicalTimeSeriesLoader;
  /**
   * The scheduler.
   */
  @PropertyDefinition(validate = "notNull")
  private ScheduledExecutorService _scheduler;
  /**
   * The available computation target types.
   */
  @PropertyDefinition(validate = "notNull")
  private ComputationTargetTypeProvider _targetTypes = new DefaultComputationTargetTypeProvider();
  /**
   * The organization master.
   */
  @PropertyDefinition(validate = "notNull")
  private OrganizationMaster _organizationMaster;
  /**
   * The market data snapshot master.
   */
  @PropertyDefinition(validate = "notNull")
  private MarketDataSnapshotMaster _marketDataSnapshotMaster;
  /**
   * For obtaining the live market data provider names. Either this or marketDataSpecificationRepository must be set.
   */
  @PropertyDefinition
  private LiveMarketDataProviderFactory _liveMarketDataProviderFactory;
  /**
   * For looking up market data provider specifications by name. Either this or liveMarketDataProviderFactory must be set.
   * 
   * @deprecated  use liveMarketDataProviderFactory
   */
  @PropertyDefinition
  @Deprecated
  private NamedMarketDataSpecificationRepository _marketDataSpecificationRepository;
  /**
   * The view processor.
   */
  @PropertyDefinition(validate = "notNull")
  private ViewProcessor _viewProcessor;
  /**
   * The computation target resolver.
   */
  @PropertyDefinition(validate = "notNull")
  private ComputationTargetResolver _computationTargetResolver;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    initBasics(repo);
    initMasters(repo);
    initValueRequirementNames(repo, configuration);
  }

  protected void initBasics(ComponentRepository repo) {
    repo.getRestComponents().publishResource(new WebHomeResource());
    repo.getRestComponents().publishResource(new WebAboutResource());
  }

  protected void initMasters(ComponentRepository repo) {
    if (getLiveMarketDataProviderFactory() == null && getMarketDataSpecificationRepository() == null) {
      throw new OpenGammaRuntimeException("Neither " + marketDataSpecificationRepository().name() + " nor " + liveMarketDataProviderFactory().name() + " were specified");
    }
    JerseyRestResourceFactory resource;
    resource = new JerseyRestResourceFactory(WebConfigsResource.class, getConfigMaster());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebExchangesResource.class, getExchangeMaster());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebHolidaysResource.class, getHolidayMaster());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebRegionsResource.class, getRegionMaster());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebSecuritiesResource.class, getSecurityMaster(), getSecurityLoader(), getHistoricalTimeSeriesMaster(), getOrganizationMaster());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebPositionsResource.class, getPositionMaster(), getSecurityLoader(), getSecuritySource(), getHistoricalTimeSeriesSource());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebPortfoliosResource.class, getPortfolioMaster(), getPositionMaster(), getSecuritySource(), getScheduler());
    repo.getRestComponents().publishResource(resource);
    final MasterConfigSource configSource = new MasterConfigSource(getConfigMaster());
    resource = new JerseyRestResourceFactory(WebAllHistoricalTimeSeriesResource.class, getHistoricalTimeSeriesMaster(), getHistoricalTimeSeriesLoader(), configSource);
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebComputationTargetTypeResource.class, getTargetTypes());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebOrganizationsResource.class, getOrganizationMaster());
    repo.getRestComponents().publishResource(resource);
    resource = new JerseyRestResourceFactory(WebMarketDataSnapshotsResource.class, 
        getMarketDataSnapshotMaster(), getConfigMaster(), getLiveMarketDataProviderFactory(), getMarketDataSpecificationRepository(),
        configSource, getComputationTargetResolver(), getViewProcessor(), getHistoricalTimeSeriesSource());
    repo.getRestComponents().publishResource(resource);
  }
  
  protected void initValueRequirementNames(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    String valueRequirementNameClasses = configuration.get(WebValueRequirementNamesResource.VALUE_REQUIREMENT_NAME_CLASSES);
    configuration.remove(WebValueRequirementNamesResource.VALUE_REQUIREMENT_NAME_CLASSES);

    if (valueRequirementNameClasses == null) {
      repo.getRestComponents().publishResource(new WebValueRequirementNamesResource());
    } else if (valueRequirementNameClasses.contains(",")) {
      repo.getRestComponents().publishResource(
          new WebValueRequirementNamesResource(valueRequirementNameClasses.split(",")));
    } else {
      repo.getRestComponents().publishResource(new WebValueRequirementNamesResource(new String[] {valueRequirementNameClasses}));
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code WebsiteBasicsComponentFactory}.
   * @return the meta-bean, not null
   */
  public static WebsiteBasicsComponentFactory.Meta meta() {
    return WebsiteBasicsComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(WebsiteBasicsComponentFactory.Meta.INSTANCE);
  }

  @Override
  public WebsiteBasicsComponentFactory.Meta metaBean() {
    return WebsiteBasicsComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the value of the property, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master.
   * @param configMaster  the new value of the property, not null
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    JodaBeanUtils.notNull(configMaster, "configMaster");
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
   * @return the value of the property, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  /**
   * Sets the exchange master.
   * @param exchangeMaster  the new value of the property, not null
   */
  public void setExchangeMaster(ExchangeMaster exchangeMaster) {
    JodaBeanUtils.notNull(exchangeMaster, "exchangeMaster");
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
   * @return the value of the property, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Sets the holiday master.
   * @param holidayMaster  the new value of the property, not null
   */
  public void setHolidayMaster(HolidayMaster holidayMaster) {
    JodaBeanUtils.notNull(holidayMaster, "holidayMaster");
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
   * Gets the underlying master.
   * @return the value of the property, not null
   */
  public RegionMaster getRegionMaster() {
    return _regionMaster;
  }

  /**
   * Sets the underlying master.
   * @param regionMaster  the new value of the property, not null
   */
  public void setRegionMaster(RegionMaster regionMaster) {
    JodaBeanUtils.notNull(regionMaster, "regionMaster");
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
   * @return the value of the property, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the new value of the property, not null
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    JodaBeanUtils.notNull(securityMaster, "securityMaster");
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
   * Gets the security loader.
   * @return the value of the property, not null
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

  /**
   * Sets the security loader.
   * @param securityLoader  the new value of the property, not null
   */
  public void setSecurityLoader(SecurityLoader securityLoader) {
    JodaBeanUtils.notNull(securityLoader, "securityLoader");
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
   * Gets the position master.
   * @return the value of the property, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the new value of the property, not null
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    JodaBeanUtils.notNull(positionMaster, "positionMaster");
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
   * @return the value of the property, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the portfolio master.
   * @param portfolioMaster  the new value of the property, not null
   */
  public void setPortfolioMaster(PortfolioMaster portfolioMaster) {
    JodaBeanUtils.notNull(portfolioMaster, "portfolioMaster");
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
   * Gets the batch master.
   * @return the value of the property, not null
   */
  public BatchMaster getBatchMaster() {
    return _batchMaster;
  }

  /**
   * Sets the batch master.
   * @param batchMaster  the new value of the property, not null
   */
  public void setBatchMaster(BatchMaster batchMaster) {
    JodaBeanUtils.notNull(batchMaster, "batchMaster");
    this._batchMaster = batchMaster;
  }

  /**
   * Gets the the {@code batchMaster} property.
   * @return the property, not null
   */
  public final Property<BatchMaster> batchMaster() {
    return metaBean().batchMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series master.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Sets the time-series master.
   * @param historicalTimeSeriesMaster  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    JodaBeanUtils.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
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
   * Gets the time-series source.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  /**
   * Sets the time-series source.
   * @param historicalTimeSeriesSource  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    JodaBeanUtils.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");
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
   * Gets the time-series loader.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader() {
    return _historicalTimeSeriesLoader;
  }

  /**
   * Sets the time-series loader.
   * @param historicalTimeSeriesLoader  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesLoader(HistoricalTimeSeriesLoader historicalTimeSeriesLoader) {
    JodaBeanUtils.notNull(historicalTimeSeriesLoader, "historicalTimeSeriesLoader");
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
   * Gets the scheduler.
   * @return the value of the property, not null
   */
  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }

  /**
   * Sets the scheduler.
   * @param scheduler  the new value of the property, not null
   */
  public void setScheduler(ScheduledExecutorService scheduler) {
    JodaBeanUtils.notNull(scheduler, "scheduler");
    this._scheduler = scheduler;
  }

  /**
   * Gets the the {@code scheduler} property.
   * @return the property, not null
   */
  public final Property<ScheduledExecutorService> scheduler() {
    return metaBean().scheduler().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the available computation target types.
   * @return the value of the property, not null
   */
  public ComputationTargetTypeProvider getTargetTypes() {
    return _targetTypes;
  }

  /**
   * Sets the available computation target types.
   * @param targetTypes  the new value of the property, not null
   */
  public void setTargetTypes(ComputationTargetTypeProvider targetTypes) {
    JodaBeanUtils.notNull(targetTypes, "targetTypes");
    this._targetTypes = targetTypes;
  }

  /**
   * Gets the the {@code targetTypes} property.
   * @return the property, not null
   */
  public final Property<ComputationTargetTypeProvider> targetTypes() {
    return metaBean().targetTypes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the organization master.
   * @return the value of the property, not null
   */
  public OrganizationMaster getOrganizationMaster() {
    return _organizationMaster;
  }

  /**
   * Sets the organization master.
   * @param organizationMaster  the new value of the property, not null
   */
  public void setOrganizationMaster(OrganizationMaster organizationMaster) {
    JodaBeanUtils.notNull(organizationMaster, "organizationMaster");
    this._organizationMaster = organizationMaster;
  }

  /**
   * Gets the the {@code organizationMaster} property.
   * @return the property, not null
   */
  public final Property<OrganizationMaster> organizationMaster() {
    return metaBean().organizationMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data snapshot master.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _marketDataSnapshotMaster;
  }

  /**
   * Sets the market data snapshot master.
   * @param marketDataSnapshotMaster  the new value of the property, not null
   */
  public void setMarketDataSnapshotMaster(MarketDataSnapshotMaster marketDataSnapshotMaster) {
    JodaBeanUtils.notNull(marketDataSnapshotMaster, "marketDataSnapshotMaster");
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
   * Gets for obtaining the live market data provider names. Either this or marketDataSpecificationRepository must be set.
   * @return the value of the property
   */
  public LiveMarketDataProviderFactory getLiveMarketDataProviderFactory() {
    return _liveMarketDataProviderFactory;
  }

  /**
   * Sets for obtaining the live market data provider names. Either this or marketDataSpecificationRepository must be set.
   * @param liveMarketDataProviderFactory  the new value of the property
   */
  public void setLiveMarketDataProviderFactory(LiveMarketDataProviderFactory liveMarketDataProviderFactory) {
    this._liveMarketDataProviderFactory = liveMarketDataProviderFactory;
  }

  /**
   * Gets the the {@code liveMarketDataProviderFactory} property.
   * @return the property, not null
   */
  public final Property<LiveMarketDataProviderFactory> liveMarketDataProviderFactory() {
    return metaBean().liveMarketDataProviderFactory().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets for looking up market data provider specifications by name. Either this or liveMarketDataProviderFactory must be set.
   * 
   * @deprecated  use liveMarketDataProviderFactory
   * @return the value of the property
   */
  @Deprecated
  public NamedMarketDataSpecificationRepository getMarketDataSpecificationRepository() {
    return _marketDataSpecificationRepository;
  }

  /**
   * Sets for looking up market data provider specifications by name. Either this or liveMarketDataProviderFactory must be set.
   * 
   * @deprecated  use liveMarketDataProviderFactory
   * @param marketDataSpecificationRepository  the new value of the property
   */
  @Deprecated
  public void setMarketDataSpecificationRepository(NamedMarketDataSpecificationRepository marketDataSpecificationRepository) {
    this._marketDataSpecificationRepository = marketDataSpecificationRepository;
  }

  /**
   * Gets the the {@code marketDataSpecificationRepository} property.
   * 
   * @deprecated  use liveMarketDataProviderFactory
   * @return the property, not null
   */
  @Deprecated
  public final Property<NamedMarketDataSpecificationRepository> marketDataSpecificationRepository() {
    return metaBean().marketDataSpecificationRepository().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view processor.
   * @return the value of the property, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Sets the view processor.
   * @param viewProcessor  the new value of the property, not null
   */
  public void setViewProcessor(ViewProcessor viewProcessor) {
    JodaBeanUtils.notNull(viewProcessor, "viewProcessor");
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
   * Gets the computation target resolver.
   * @return the value of the property, not null
   */
  public ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  /**
   * Sets the computation target resolver.
   * @param computationTargetResolver  the new value of the property, not null
   */
  public void setComputationTargetResolver(ComputationTargetResolver computationTargetResolver) {
    JodaBeanUtils.notNull(computationTargetResolver, "computationTargetResolver");
    this._computationTargetResolver = computationTargetResolver;
  }

  /**
   * Gets the the {@code computationTargetResolver} property.
   * @return the property, not null
   */
  public final Property<ComputationTargetResolver> computationTargetResolver() {
    return metaBean().computationTargetResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public WebsiteBasicsComponentFactory clone() {
    return (WebsiteBasicsComponentFactory) super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      WebsiteBasicsComponentFactory other = (WebsiteBasicsComponentFactory) obj;
      return JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getExchangeMaster(), other.getExchangeMaster()) &&
          JodaBeanUtils.equal(getHolidayMaster(), other.getHolidayMaster()) &&
          JodaBeanUtils.equal(getRegionMaster(), other.getRegionMaster()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getSecurityLoader(), other.getSecurityLoader()) &&
          JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getPortfolioMaster(), other.getPortfolioMaster()) &&
          JodaBeanUtils.equal(getBatchMaster(), other.getBatchMaster()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesMaster(), other.getHistoricalTimeSeriesMaster()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesLoader(), other.getHistoricalTimeSeriesLoader()) &&
          JodaBeanUtils.equal(getScheduler(), other.getScheduler()) &&
          JodaBeanUtils.equal(getTargetTypes(), other.getTargetTypes()) &&
          JodaBeanUtils.equal(getOrganizationMaster(), other.getOrganizationMaster()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotMaster(), other.getMarketDataSnapshotMaster()) &&
          JodaBeanUtils.equal(getLiveMarketDataProviderFactory(), other.getLiveMarketDataProviderFactory()) &&
          JodaBeanUtils.equal(getMarketDataSpecificationRepository(), other.getMarketDataSpecificationRepository()) &&
          JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getComputationTargetResolver(), other.getComputationTargetResolver()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityLoader());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPortfolioMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBatchMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesLoader());
    hash += hash * 31 + JodaBeanUtils.hashCode(getScheduler());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTargetTypes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getOrganizationMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLiveMarketDataProviderFactory());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSpecificationRepository());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComputationTargetResolver());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(704);
    buf.append("WebsiteBasicsComponentFactory{");
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
    buf.append("configMaster").append('=').append(getConfigMaster()).append(',').append(' ');
    buf.append("exchangeMaster").append('=').append(getExchangeMaster()).append(',').append(' ');
    buf.append("holidayMaster").append('=').append(getHolidayMaster()).append(',').append(' ');
    buf.append("regionMaster").append('=').append(getRegionMaster()).append(',').append(' ');
    buf.append("securityMaster").append('=').append(getSecurityMaster()).append(',').append(' ');
    buf.append("securitySource").append('=').append(getSecuritySource()).append(',').append(' ');
    buf.append("securityLoader").append('=').append(getSecurityLoader()).append(',').append(' ');
    buf.append("positionMaster").append('=').append(getPositionMaster()).append(',').append(' ');
    buf.append("portfolioMaster").append('=').append(getPortfolioMaster()).append(',').append(' ');
    buf.append("batchMaster").append('=').append(getBatchMaster()).append(',').append(' ');
    buf.append("historicalTimeSeriesMaster").append('=').append(getHistoricalTimeSeriesMaster()).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(getHistoricalTimeSeriesSource()).append(',').append(' ');
    buf.append("historicalTimeSeriesLoader").append('=').append(getHistoricalTimeSeriesLoader()).append(',').append(' ');
    buf.append("scheduler").append('=').append(getScheduler()).append(',').append(' ');
    buf.append("targetTypes").append('=').append(getTargetTypes()).append(',').append(' ');
    buf.append("organizationMaster").append('=').append(getOrganizationMaster()).append(',').append(' ');
    buf.append("marketDataSnapshotMaster").append('=').append(getMarketDataSnapshotMaster()).append(',').append(' ');
    buf.append("liveMarketDataProviderFactory").append('=').append(getLiveMarketDataProviderFactory()).append(',').append(' ');
    buf.append("marketDataSpecificationRepository").append('=').append(getMarketDataSpecificationRepository()).append(',').append(' ');
    buf.append("viewProcessor").append('=').append(getViewProcessor()).append(',').append(' ');
    buf.append("computationTargetResolver").append('=').append(getComputationTargetResolver()).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code WebsiteBasicsComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", WebsiteBasicsComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code exchangeMaster} property.
     */
    private final MetaProperty<ExchangeMaster> _exchangeMaster = DirectMetaProperty.ofReadWrite(
        this, "exchangeMaster", WebsiteBasicsComponentFactory.class, ExchangeMaster.class);
    /**
     * The meta-property for the {@code holidayMaster} property.
     */
    private final MetaProperty<HolidayMaster> _holidayMaster = DirectMetaProperty.ofReadWrite(
        this, "holidayMaster", WebsiteBasicsComponentFactory.class, HolidayMaster.class);
    /**
     * The meta-property for the {@code regionMaster} property.
     */
    private final MetaProperty<RegionMaster> _regionMaster = DirectMetaProperty.ofReadWrite(
        this, "regionMaster", WebsiteBasicsComponentFactory.class, RegionMaster.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", WebsiteBasicsComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", WebsiteBasicsComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code securityLoader} property.
     */
    private final MetaProperty<SecurityLoader> _securityLoader = DirectMetaProperty.ofReadWrite(
        this, "securityLoader", WebsiteBasicsComponentFactory.class, SecurityLoader.class);
    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", WebsiteBasicsComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code portfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _portfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "portfolioMaster", WebsiteBasicsComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code batchMaster} property.
     */
    private final MetaProperty<BatchMaster> _batchMaster = DirectMetaProperty.ofReadWrite(
        this, "batchMaster", WebsiteBasicsComponentFactory.class, BatchMaster.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     */
    private final MetaProperty<HistoricalTimeSeriesMaster> _historicalTimeSeriesMaster = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesMaster", WebsiteBasicsComponentFactory.class, HistoricalTimeSeriesMaster.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", WebsiteBasicsComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesLoader} property.
     */
    private final MetaProperty<HistoricalTimeSeriesLoader> _historicalTimeSeriesLoader = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesLoader", WebsiteBasicsComponentFactory.class, HistoricalTimeSeriesLoader.class);
    /**
     * The meta-property for the {@code scheduler} property.
     */
    private final MetaProperty<ScheduledExecutorService> _scheduler = DirectMetaProperty.ofReadWrite(
        this, "scheduler", WebsiteBasicsComponentFactory.class, ScheduledExecutorService.class);
    /**
     * The meta-property for the {@code targetTypes} property.
     */
    private final MetaProperty<ComputationTargetTypeProvider> _targetTypes = DirectMetaProperty.ofReadWrite(
        this, "targetTypes", WebsiteBasicsComponentFactory.class, ComputationTargetTypeProvider.class);
    /**
     * The meta-property for the {@code organizationMaster} property.
     */
    private final MetaProperty<OrganizationMaster> _organizationMaster = DirectMetaProperty.ofReadWrite(
        this, "organizationMaster", WebsiteBasicsComponentFactory.class, OrganizationMaster.class);
    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _marketDataSnapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotMaster", WebsiteBasicsComponentFactory.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code liveMarketDataProviderFactory} property.
     */
    private final MetaProperty<LiveMarketDataProviderFactory> _liveMarketDataProviderFactory = DirectMetaProperty.ofReadWrite(
        this, "liveMarketDataProviderFactory", WebsiteBasicsComponentFactory.class, LiveMarketDataProviderFactory.class);
    /**
     * The meta-property for the {@code marketDataSpecificationRepository} property.
     */
    private final MetaProperty<NamedMarketDataSpecificationRepository> _marketDataSpecificationRepository = DirectMetaProperty.ofReadWrite(
        this, "marketDataSpecificationRepository", WebsiteBasicsComponentFactory.class, NamedMarketDataSpecificationRepository.class);
    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", WebsiteBasicsComponentFactory.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code computationTargetResolver} property.
     */
    private final MetaProperty<ComputationTargetResolver> _computationTargetResolver = DirectMetaProperty.ofReadWrite(
        this, "computationTargetResolver", WebsiteBasicsComponentFactory.class, ComputationTargetResolver.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "configMaster",
        "exchangeMaster",
        "holidayMaster",
        "regionMaster",
        "securityMaster",
        "securitySource",
        "securityLoader",
        "positionMaster",
        "portfolioMaster",
        "batchMaster",
        "historicalTimeSeriesMaster",
        "historicalTimeSeriesSource",
        "historicalTimeSeriesLoader",
        "scheduler",
        "targetTypes",
        "organizationMaster",
        "marketDataSnapshotMaster",
        "liveMarketDataProviderFactory",
        "marketDataSpecificationRepository",
        "viewProcessor",
        "computationTargetResolver");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
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
        case -702456965:  // securitySource
          return _securitySource;
        case -903470221:  // securityLoader
          return _securityLoader;
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -772274742:  // portfolioMaster
          return _portfolioMaster;
        case -252634564:  // batchMaster
          return _batchMaster;
        case 173967376:  // historicalTimeSeriesMaster
          return _historicalTimeSeriesMaster;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case 157715905:  // historicalTimeSeriesLoader
          return _historicalTimeSeriesLoader;
        case -160710469:  // scheduler
          return _scheduler;
        case -2094577304:  // targetTypes
          return _targetTypes;
        case -1158737547:  // organizationMaster
          return _organizationMaster;
        case 2090650860:  // marketDataSnapshotMaster
          return _marketDataSnapshotMaster;
        case -301472921:  // liveMarketDataProviderFactory
          return _liveMarketDataProviderFactory;
        case 1743800263:  // marketDataSpecificationRepository
          return _marketDataSpecificationRepository;
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case 1562222174:  // computationTargetResolver
          return _computationTargetResolver;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends WebsiteBasicsComponentFactory> builder() {
      return new DirectBeanBuilder<WebsiteBasicsComponentFactory>(new WebsiteBasicsComponentFactory());
    }

    @Override
    public Class<? extends WebsiteBasicsComponentFactory> beanType() {
      return WebsiteBasicsComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
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
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code securityLoader} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityLoader> securityLoader() {
      return _securityLoader;
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
     * The meta-property for the {@code batchMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BatchMaster> batchMaster() {
      return _batchMaster;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
      return _historicalTimeSeriesMaster;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesLoader} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesLoader> historicalTimeSeriesLoader() {
      return _historicalTimeSeriesLoader;
    }

    /**
     * The meta-property for the {@code scheduler} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ScheduledExecutorService> scheduler() {
      return _scheduler;
    }

    /**
     * The meta-property for the {@code targetTypes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetTypeProvider> targetTypes() {
      return _targetTypes;
    }

    /**
     * The meta-property for the {@code organizationMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<OrganizationMaster> organizationMaster() {
      return _organizationMaster;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
      return _marketDataSnapshotMaster;
    }

    /**
     * The meta-property for the {@code liveMarketDataProviderFactory} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LiveMarketDataProviderFactory> liveMarketDataProviderFactory() {
      return _liveMarketDataProviderFactory;
    }

    /**
     * The meta-property for the {@code marketDataSpecificationRepository} property.
     * @deprecated  use liveMarketDataProviderFactory
     * @return the meta-property, not null
     */
    @Deprecated
    public final MetaProperty<NamedMarketDataSpecificationRepository> marketDataSpecificationRepository() {
      return _marketDataSpecificationRepository;
    }

    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code computationTargetResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetResolver> computationTargetResolver() {
      return _computationTargetResolver;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 10395716:  // configMaster
          return ((WebsiteBasicsComponentFactory) bean).getConfigMaster();
        case -652001691:  // exchangeMaster
          return ((WebsiteBasicsComponentFactory) bean).getExchangeMaster();
        case 246258906:  // holidayMaster
          return ((WebsiteBasicsComponentFactory) bean).getHolidayMaster();
        case -1820969354:  // regionMaster
          return ((WebsiteBasicsComponentFactory) bean).getRegionMaster();
        case -887218750:  // securityMaster
          return ((WebsiteBasicsComponentFactory) bean).getSecurityMaster();
        case -702456965:  // securitySource
          return ((WebsiteBasicsComponentFactory) bean).getSecuritySource();
        case -903470221:  // securityLoader
          return ((WebsiteBasicsComponentFactory) bean).getSecurityLoader();
        case -1840419605:  // positionMaster
          return ((WebsiteBasicsComponentFactory) bean).getPositionMaster();
        case -772274742:  // portfolioMaster
          return ((WebsiteBasicsComponentFactory) bean).getPortfolioMaster();
        case -252634564:  // batchMaster
          return ((WebsiteBasicsComponentFactory) bean).getBatchMaster();
        case 173967376:  // historicalTimeSeriesMaster
          return ((WebsiteBasicsComponentFactory) bean).getHistoricalTimeSeriesMaster();
        case 358729161:  // historicalTimeSeriesSource
          return ((WebsiteBasicsComponentFactory) bean).getHistoricalTimeSeriesSource();
        case 157715905:  // historicalTimeSeriesLoader
          return ((WebsiteBasicsComponentFactory) bean).getHistoricalTimeSeriesLoader();
        case -160710469:  // scheduler
          return ((WebsiteBasicsComponentFactory) bean).getScheduler();
        case -2094577304:  // targetTypes
          return ((WebsiteBasicsComponentFactory) bean).getTargetTypes();
        case -1158737547:  // organizationMaster
          return ((WebsiteBasicsComponentFactory) bean).getOrganizationMaster();
        case 2090650860:  // marketDataSnapshotMaster
          return ((WebsiteBasicsComponentFactory) bean).getMarketDataSnapshotMaster();
        case -301472921:  // liveMarketDataProviderFactory
          return ((WebsiteBasicsComponentFactory) bean).getLiveMarketDataProviderFactory();
        case 1743800263:  // marketDataSpecificationRepository
          return ((WebsiteBasicsComponentFactory) bean).getMarketDataSpecificationRepository();
        case -1697555603:  // viewProcessor
          return ((WebsiteBasicsComponentFactory) bean).getViewProcessor();
        case 1562222174:  // computationTargetResolver
          return ((WebsiteBasicsComponentFactory) bean).getComputationTargetResolver();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 10395716:  // configMaster
          ((WebsiteBasicsComponentFactory) bean).setConfigMaster((ConfigMaster) newValue);
          return;
        case -652001691:  // exchangeMaster
          ((WebsiteBasicsComponentFactory) bean).setExchangeMaster((ExchangeMaster) newValue);
          return;
        case 246258906:  // holidayMaster
          ((WebsiteBasicsComponentFactory) bean).setHolidayMaster((HolidayMaster) newValue);
          return;
        case -1820969354:  // regionMaster
          ((WebsiteBasicsComponentFactory) bean).setRegionMaster((RegionMaster) newValue);
          return;
        case -887218750:  // securityMaster
          ((WebsiteBasicsComponentFactory) bean).setSecurityMaster((SecurityMaster) newValue);
          return;
        case -702456965:  // securitySource
          ((WebsiteBasicsComponentFactory) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case -903470221:  // securityLoader
          ((WebsiteBasicsComponentFactory) bean).setSecurityLoader((SecurityLoader) newValue);
          return;
        case -1840419605:  // positionMaster
          ((WebsiteBasicsComponentFactory) bean).setPositionMaster((PositionMaster) newValue);
          return;
        case -772274742:  // portfolioMaster
          ((WebsiteBasicsComponentFactory) bean).setPortfolioMaster((PortfolioMaster) newValue);
          return;
        case -252634564:  // batchMaster
          ((WebsiteBasicsComponentFactory) bean).setBatchMaster((BatchMaster) newValue);
          return;
        case 173967376:  // historicalTimeSeriesMaster
          ((WebsiteBasicsComponentFactory) bean).setHistoricalTimeSeriesMaster((HistoricalTimeSeriesMaster) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((WebsiteBasicsComponentFactory) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case 157715905:  // historicalTimeSeriesLoader
          ((WebsiteBasicsComponentFactory) bean).setHistoricalTimeSeriesLoader((HistoricalTimeSeriesLoader) newValue);
          return;
        case -160710469:  // scheduler
          ((WebsiteBasicsComponentFactory) bean).setScheduler((ScheduledExecutorService) newValue);
          return;
        case -2094577304:  // targetTypes
          ((WebsiteBasicsComponentFactory) bean).setTargetTypes((ComputationTargetTypeProvider) newValue);
          return;
        case -1158737547:  // organizationMaster
          ((WebsiteBasicsComponentFactory) bean).setOrganizationMaster((OrganizationMaster) newValue);
          return;
        case 2090650860:  // marketDataSnapshotMaster
          ((WebsiteBasicsComponentFactory) bean).setMarketDataSnapshotMaster((MarketDataSnapshotMaster) newValue);
          return;
        case -301472921:  // liveMarketDataProviderFactory
          ((WebsiteBasicsComponentFactory) bean).setLiveMarketDataProviderFactory((LiveMarketDataProviderFactory) newValue);
          return;
        case 1743800263:  // marketDataSpecificationRepository
          ((WebsiteBasicsComponentFactory) bean).setMarketDataSpecificationRepository((NamedMarketDataSpecificationRepository) newValue);
          return;
        case -1697555603:  // viewProcessor
          ((WebsiteBasicsComponentFactory) bean).setViewProcessor((ViewProcessor) newValue);
          return;
        case 1562222174:  // computationTargetResolver
          ((WebsiteBasicsComponentFactory) bean).setComputationTargetResolver((ComputationTargetResolver) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._configMaster, "configMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._exchangeMaster, "exchangeMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._holidayMaster, "holidayMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._regionMaster, "regionMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._securityMaster, "securityMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._securitySource, "securitySource");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._securityLoader, "securityLoader");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._positionMaster, "positionMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._portfolioMaster, "portfolioMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._batchMaster, "batchMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._historicalTimeSeriesSource, "historicalTimeSeriesSource");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._historicalTimeSeriesLoader, "historicalTimeSeriesLoader");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._scheduler, "scheduler");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._targetTypes, "targetTypes");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._organizationMaster, "organizationMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._marketDataSnapshotMaster, "marketDataSnapshotMaster");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._viewProcessor, "viewProcessor");
      JodaBeanUtils.notNull(((WebsiteBasicsComponentFactory) bean)._computationTargetResolver, "computationTargetResolver");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
