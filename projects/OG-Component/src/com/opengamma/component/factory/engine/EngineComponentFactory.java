/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

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

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calcnode.CalcNodeSocketConfiguration;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.batch.AdHocBatchDbManager;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuilderService;
import com.opengamma.financial.user.FinancialUserManager;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Component factory for the config source.
 */
@BeanDefinition
public class EngineComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The description of the Engine.
   */
  @PropertyDefinition(validate = "notNull")
  private String _description;

  /**
   * The security master.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _securityMaster;
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
   * The snapshot master.
   */
  @PropertyDefinition(validate = "notNull")
  private MarketDataSnapshotMaster _marketDataSnapshotMaster;
  /**
   * The time-series master.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;

  /**
   * The config source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;
  /**
   * The exchange source.
   */
  @PropertyDefinition(validate = "notNull")
  private ExchangeSource _exchangeSource;
  /**
   * The holiday source.
   */
  @PropertyDefinition(validate = "notNull")
  private HolidaySource _holidaySource;
  /**
   * The region source.
   */
  @PropertyDefinition(validate = "notNull")
  private RegionSource _regionSource;
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
   * The snapshot source.
   */
  @PropertyDefinition(validate = "notNull")
  private MarketDataSnapshotSource _marketDataSnapshotSource;
  /**
   * The time-series source.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The currency matrix source.
   */
  @PropertyDefinition
  private CurrencyMatrixSource _currencyMatrixSource;
  /**
   * The currency pairs source.
   */
  @PropertyDefinition
  private CurrencyPairsSource _currencyPairsSource;
  /**
   * The yield curve definition source.
   */
  @PropertyDefinition
  private InterpolatedYieldCurveDefinitionSource _interpolatedYieldCurveDefinitionSource;
  /**
   * The yield curve specification source.
   */
  @PropertyDefinition
  private InterpolatedYieldCurveSpecificationBuilder _interpolatedYieldCurveSpecificationBuilder;
  /**
   * The volitility cube source.
   */
  @PropertyDefinition
  private VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  /**
   * The user manager.
   */
  @PropertyDefinition
  private FinancialUserManager _userManager;
  /**
   * The convention bundle source.
   */
  @PropertyDefinition
  private AdHocBatchDbManager _adHocBatchDbManager;
  /**
   * The socket configuration.
   */
  @PropertyDefinition
  private CalcNodeSocketConfiguration _calcNodeSocketConfiguration;

  /**
   * The repository configuration source.
   */
  @PropertyDefinition(validate = "notNull")
  private RepositoryConfigurationSource _repositoryConfigurationSource;
  /**
   * The available outputs.
   */
  @PropertyDefinition(validate = "notNull")
  private AvailableOutputsProvider _availableOutputs;
  /**
   * The dependency graph builder.
   */
  @PropertyDefinition(validate = "notNull")
  private DependencyGraphBuilderService _dependencyGraphBuilder;
  /**
   * The view processor.
   */
  @PropertyDefinition(validate = "notNull")
  private ViewProcessor _viewProcessor;

  /**
   * The view processor id.
   */
  @PropertyDefinition(validate = "notNull")
  private String _viewProcessorId;
  /**
   * The JMS broker URI.
   */
  @PropertyDefinition(validate = "notNull")
  private String _jmsBrokerUri;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code EngineComponentFactory}.
   * @return the meta-bean, not null
   */
  public static EngineComponentFactory.Meta meta() {
    return EngineComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(EngineComponentFactory.Meta.INSTANCE);
  }

  @Override
  public EngineComponentFactory.Meta metaBean() {
    return EngineComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -1724546052:  // description
        return getDescription();
      case -887218750:  // securityMaster
        return getSecurityMaster();
      case -1840419605:  // positionMaster
        return getPositionMaster();
      case -772274742:  // portfolioMaster
        return getPortfolioMaster();
      case 2090650860:  // marketDataSnapshotMaster
        return getMarketDataSnapshotMaster();
      case 173967376:  // historicalTimeSeriesMaster
        return getHistoricalTimeSeriesMaster();
      case 195157501:  // configSource
        return getConfigSource();
      case -467239906:  // exchangeSource
        return getExchangeSource();
      case 431020691:  // holidaySource
        return getHolidaySource();
      case -1636207569:  // regionSource
        return getRegionSource();
      case -702456965:  // securitySource
        return getSecuritySource();
      case -1655657820:  // positionSource
        return getPositionSource();
      case -2019554651:  // marketDataSnapshotSource
        return getMarketDataSnapshotSource();
      case 358729161:  // historicalTimeSeriesSource
        return getHistoricalTimeSeriesSource();
      case 615188973:  // currencyMatrixSource
        return getCurrencyMatrixSource();
      case -1615906429:  // currencyPairsSource
        return getCurrencyPairsSource();
      case -582658381:  // interpolatedYieldCurveDefinitionSource
        return getInterpolatedYieldCurveDefinitionSource();
      case -461125123:  // interpolatedYieldCurveSpecificationBuilder
        return getInterpolatedYieldCurveSpecificationBuilder();
      case 1540542824:  // volatilityCubeDefinitionSource
        return getVolatilityCubeDefinitionSource();
      case 533393762:  // userManager
        return getUserManager();
      case -1721311058:  // adHocBatchDbManager
        return getAdHocBatchDbManager();
      case 644418764:  // calcNodeSocketConfiguration
        return getCalcNodeSocketConfiguration();
      case 416791815:  // repositoryConfigurationSource
        return getRepositoryConfigurationSource();
      case 602280073:  // availableOutputs
        return getAvailableOutputs();
      case 713351512:  // dependencyGraphBuilder
        return getDependencyGraphBuilder();
      case -1697555603:  // viewProcessor
        return getViewProcessor();
      case 736640360:  // viewProcessorId
        return getViewProcessorId();
      case 2047189283:  // jmsBrokerUri
        return getJmsBrokerUri();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -1724546052:  // description
        setDescription((String) newValue);
        return;
      case -887218750:  // securityMaster
        setSecurityMaster((SecurityMaster) newValue);
        return;
      case -1840419605:  // positionMaster
        setPositionMaster((PositionMaster) newValue);
        return;
      case -772274742:  // portfolioMaster
        setPortfolioMaster((PortfolioMaster) newValue);
        return;
      case 2090650860:  // marketDataSnapshotMaster
        setMarketDataSnapshotMaster((MarketDataSnapshotMaster) newValue);
        return;
      case 173967376:  // historicalTimeSeriesMaster
        setHistoricalTimeSeriesMaster((HistoricalTimeSeriesMaster) newValue);
        return;
      case 195157501:  // configSource
        setConfigSource((ConfigSource) newValue);
        return;
      case -467239906:  // exchangeSource
        setExchangeSource((ExchangeSource) newValue);
        return;
      case 431020691:  // holidaySource
        setHolidaySource((HolidaySource) newValue);
        return;
      case -1636207569:  // regionSource
        setRegionSource((RegionSource) newValue);
        return;
      case -702456965:  // securitySource
        setSecuritySource((SecuritySource) newValue);
        return;
      case -1655657820:  // positionSource
        setPositionSource((PositionSource) newValue);
        return;
      case -2019554651:  // marketDataSnapshotSource
        setMarketDataSnapshotSource((MarketDataSnapshotSource) newValue);
        return;
      case 358729161:  // historicalTimeSeriesSource
        setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
        return;
      case 615188973:  // currencyMatrixSource
        setCurrencyMatrixSource((CurrencyMatrixSource) newValue);
        return;
      case -1615906429:  // currencyPairsSource
        setCurrencyPairsSource((CurrencyPairsSource) newValue);
        return;
      case -582658381:  // interpolatedYieldCurveDefinitionSource
        setInterpolatedYieldCurveDefinitionSource((InterpolatedYieldCurveDefinitionSource) newValue);
        return;
      case -461125123:  // interpolatedYieldCurveSpecificationBuilder
        setInterpolatedYieldCurveSpecificationBuilder((InterpolatedYieldCurveSpecificationBuilder) newValue);
        return;
      case 1540542824:  // volatilityCubeDefinitionSource
        setVolatilityCubeDefinitionSource((VolatilityCubeDefinitionSource) newValue);
        return;
      case 533393762:  // userManager
        setUserManager((FinancialUserManager) newValue);
        return;
      case -1721311058:  // adHocBatchDbManager
        setAdHocBatchDbManager((AdHocBatchDbManager) newValue);
        return;
      case 644418764:  // calcNodeSocketConfiguration
        setCalcNodeSocketConfiguration((CalcNodeSocketConfiguration) newValue);
        return;
      case 416791815:  // repositoryConfigurationSource
        setRepositoryConfigurationSource((RepositoryConfigurationSource) newValue);
        return;
      case 602280073:  // availableOutputs
        setAvailableOutputs((AvailableOutputsProvider) newValue);
        return;
      case 713351512:  // dependencyGraphBuilder
        setDependencyGraphBuilder((DependencyGraphBuilderService) newValue);
        return;
      case -1697555603:  // viewProcessor
        setViewProcessor((ViewProcessor) newValue);
        return;
      case 736640360:  // viewProcessorId
        setViewProcessorId((String) newValue);
        return;
      case 2047189283:  // jmsBrokerUri
        setJmsBrokerUri((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_description, "description");
    JodaBeanUtils.notNull(_securityMaster, "securityMaster");
    JodaBeanUtils.notNull(_positionMaster, "positionMaster");
    JodaBeanUtils.notNull(_portfolioMaster, "portfolioMaster");
    JodaBeanUtils.notNull(_marketDataSnapshotMaster, "marketDataSnapshotMaster");
    JodaBeanUtils.notNull(_historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    JodaBeanUtils.notNull(_configSource, "configSource");
    JodaBeanUtils.notNull(_exchangeSource, "exchangeSource");
    JodaBeanUtils.notNull(_holidaySource, "holidaySource");
    JodaBeanUtils.notNull(_regionSource, "regionSource");
    JodaBeanUtils.notNull(_securitySource, "securitySource");
    JodaBeanUtils.notNull(_positionSource, "positionSource");
    JodaBeanUtils.notNull(_marketDataSnapshotSource, "marketDataSnapshotSource");
    JodaBeanUtils.notNull(_historicalTimeSeriesSource, "historicalTimeSeriesSource");
    JodaBeanUtils.notNull(_repositoryConfigurationSource, "repositoryConfigurationSource");
    JodaBeanUtils.notNull(_availableOutputs, "availableOutputs");
    JodaBeanUtils.notNull(_dependencyGraphBuilder, "dependencyGraphBuilder");
    JodaBeanUtils.notNull(_viewProcessor, "viewProcessor");
    JodaBeanUtils.notNull(_viewProcessorId, "viewProcessorId");
    JodaBeanUtils.notNull(_jmsBrokerUri, "jmsBrokerUri");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      EngineComponentFactory other = (EngineComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getDescription(), other.getDescription()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getPortfolioMaster(), other.getPortfolioMaster()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotMaster(), other.getMarketDataSnapshotMaster()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesMaster(), other.getHistoricalTimeSeriesMaster()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(getExchangeSource(), other.getExchangeSource()) &&
          JodaBeanUtils.equal(getHolidaySource(), other.getHolidaySource()) &&
          JodaBeanUtils.equal(getRegionSource(), other.getRegionSource()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotSource(), other.getMarketDataSnapshotSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getCurrencyMatrixSource(), other.getCurrencyMatrixSource()) &&
          JodaBeanUtils.equal(getCurrencyPairsSource(), other.getCurrencyPairsSource()) &&
          JodaBeanUtils.equal(getInterpolatedYieldCurveDefinitionSource(), other.getInterpolatedYieldCurveDefinitionSource()) &&
          JodaBeanUtils.equal(getInterpolatedYieldCurveSpecificationBuilder(), other.getInterpolatedYieldCurveSpecificationBuilder()) &&
          JodaBeanUtils.equal(getVolatilityCubeDefinitionSource(), other.getVolatilityCubeDefinitionSource()) &&
          JodaBeanUtils.equal(getUserManager(), other.getUserManager()) &&
          JodaBeanUtils.equal(getAdHocBatchDbManager(), other.getAdHocBatchDbManager()) &&
          JodaBeanUtils.equal(getCalcNodeSocketConfiguration(), other.getCalcNodeSocketConfiguration()) &&
          JodaBeanUtils.equal(getRepositoryConfigurationSource(), other.getRepositoryConfigurationSource()) &&
          JodaBeanUtils.equal(getAvailableOutputs(), other.getAvailableOutputs()) &&
          JodaBeanUtils.equal(getDependencyGraphBuilder(), other.getDependencyGraphBuilder()) &&
          JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getViewProcessorId(), other.getViewProcessorId()) &&
          JodaBeanUtils.equal(getJmsBrokerUri(), other.getJmsBrokerUri()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDescription());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPortfolioMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidaySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrencyMatrixSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrencyPairsSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterpolatedYieldCurveDefinitionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterpolatedYieldCurveSpecificationBuilder());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVolatilityCubeDefinitionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAdHocBatchDbManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalcNodeSocketConfiguration());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRepositoryConfigurationSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAvailableOutputs());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDependencyGraphBuilder());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewProcessorId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsBrokerUri());
    return hash ^ super.hashCode();
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
   * Gets the description of the Engine.
   * @return the value of the property, not null
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Sets the description of the Engine.
   * @param description  the new value of the property, not null
   */
  public void setDescription(String description) {
    JodaBeanUtils.notNull(description, "description");
    this._description = description;
  }

  /**
   * Gets the the {@code description} property.
   * @return the property, not null
   */
  public final Property<String> description() {
    return metaBean().description().createProperty(this);
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
   * Gets the snapshot master.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _marketDataSnapshotMaster;
  }

  /**
   * Sets the snapshot master.
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
   * Gets the config source.
   * @return the value of the property, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source.
   * @param configSource  the new value of the property, not null
   */
  public void setConfigSource(ConfigSource configSource) {
    JodaBeanUtils.notNull(configSource, "configSource");
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
   * @return the value of the property, not null
   */
  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  /**
   * Sets the exchange source.
   * @param exchangeSource  the new value of the property, not null
   */
  public void setExchangeSource(ExchangeSource exchangeSource) {
    JodaBeanUtils.notNull(exchangeSource, "exchangeSource");
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
   * @return the value of the property, not null
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Sets the holiday source.
   * @param holidaySource  the new value of the property, not null
   */
  public void setHolidaySource(HolidaySource holidaySource) {
    JodaBeanUtils.notNull(holidaySource, "holidaySource");
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
   * @return the value of the property, not null
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  /**
   * Sets the region source.
   * @param regionSource  the new value of the property, not null
   */
  public void setRegionSource(RegionSource regionSource) {
    JodaBeanUtils.notNull(regionSource, "regionSource");
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
   * Gets the snapshot source.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource() {
    return _marketDataSnapshotSource;
  }

  /**
   * Sets the snapshot source.
   * @param marketDataSnapshotSource  the new value of the property, not null
   */
  public void setMarketDataSnapshotSource(MarketDataSnapshotSource marketDataSnapshotSource) {
    JodaBeanUtils.notNull(marketDataSnapshotSource, "marketDataSnapshotSource");
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
   * Gets the currency matrix source.
   * @return the value of the property
   */
  public CurrencyMatrixSource getCurrencyMatrixSource() {
    return _currencyMatrixSource;
  }

  /**
   * Sets the currency matrix source.
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
   * Gets the currency pairs source.
   * @return the value of the property
   */
  public CurrencyPairsSource getCurrencyPairsSource() {
    return _currencyPairsSource;
  }

  /**
   * Sets the currency pairs source.
   * @param currencyPairsSource  the new value of the property
   */
  public void setCurrencyPairsSource(CurrencyPairsSource currencyPairsSource) {
    this._currencyPairsSource = currencyPairsSource;
  }

  /**
   * Gets the the {@code currencyPairsSource} property.
   * @return the property, not null
   */
  public final Property<CurrencyPairsSource> currencyPairsSource() {
    return metaBean().currencyPairsSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield curve definition source.
   * @return the value of the property
   */
  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource() {
    return _interpolatedYieldCurveDefinitionSource;
  }

  /**
   * Sets the yield curve definition source.
   * @param interpolatedYieldCurveDefinitionSource  the new value of the property
   */
  public void setInterpolatedYieldCurveDefinitionSource(InterpolatedYieldCurveDefinitionSource interpolatedYieldCurveDefinitionSource) {
    this._interpolatedYieldCurveDefinitionSource = interpolatedYieldCurveDefinitionSource;
  }

  /**
   * Gets the the {@code interpolatedYieldCurveDefinitionSource} property.
   * @return the property, not null
   */
  public final Property<InterpolatedYieldCurveDefinitionSource> interpolatedYieldCurveDefinitionSource() {
    return metaBean().interpolatedYieldCurveDefinitionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield curve specification source.
   * @return the value of the property
   */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder() {
    return _interpolatedYieldCurveSpecificationBuilder;
  }

  /**
   * Sets the yield curve specification source.
   * @param interpolatedYieldCurveSpecificationBuilder  the new value of the property
   */
  public void setInterpolatedYieldCurveSpecificationBuilder(InterpolatedYieldCurveSpecificationBuilder interpolatedYieldCurveSpecificationBuilder) {
    this._interpolatedYieldCurveSpecificationBuilder = interpolatedYieldCurveSpecificationBuilder;
  }

  /**
   * Gets the the {@code interpolatedYieldCurveSpecificationBuilder} property.
   * @return the property, not null
   */
  public final Property<InterpolatedYieldCurveSpecificationBuilder> interpolatedYieldCurveSpecificationBuilder() {
    return metaBean().interpolatedYieldCurveSpecificationBuilder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the volitility cube source.
   * @return the value of the property
   */
  public VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource() {
    return _volatilityCubeDefinitionSource;
  }

  /**
   * Sets the volitility cube source.
   * @param volatilityCubeDefinitionSource  the new value of the property
   */
  public void setVolatilityCubeDefinitionSource(VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    this._volatilityCubeDefinitionSource = volatilityCubeDefinitionSource;
  }

  /**
   * Gets the the {@code volatilityCubeDefinitionSource} property.
   * @return the property, not null
   */
  public final Property<VolatilityCubeDefinitionSource> volatilityCubeDefinitionSource() {
    return metaBean().volatilityCubeDefinitionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user manager.
   * @return the value of the property
   */
  public FinancialUserManager getUserManager() {
    return _userManager;
  }

  /**
   * Sets the user manager.
   * @param userManager  the new value of the property
   */
  public void setUserManager(FinancialUserManager userManager) {
    this._userManager = userManager;
  }

  /**
   * Gets the the {@code userManager} property.
   * @return the property, not null
   */
  public final Property<FinancialUserManager> userManager() {
    return metaBean().userManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention bundle source.
   * @return the value of the property
   */
  public AdHocBatchDbManager getAdHocBatchDbManager() {
    return _adHocBatchDbManager;
  }

  /**
   * Sets the convention bundle source.
   * @param adHocBatchDbManager  the new value of the property
   */
  public void setAdHocBatchDbManager(AdHocBatchDbManager adHocBatchDbManager) {
    this._adHocBatchDbManager = adHocBatchDbManager;
  }

  /**
   * Gets the the {@code adHocBatchDbManager} property.
   * @return the property, not null
   */
  public final Property<AdHocBatchDbManager> adHocBatchDbManager() {
    return metaBean().adHocBatchDbManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the socket configuration.
   * @return the value of the property
   */
  public CalcNodeSocketConfiguration getCalcNodeSocketConfiguration() {
    return _calcNodeSocketConfiguration;
  }

  /**
   * Sets the socket configuration.
   * @param calcNodeSocketConfiguration  the new value of the property
   */
  public void setCalcNodeSocketConfiguration(CalcNodeSocketConfiguration calcNodeSocketConfiguration) {
    this._calcNodeSocketConfiguration = calcNodeSocketConfiguration;
  }

  /**
   * Gets the the {@code calcNodeSocketConfiguration} property.
   * @return the property, not null
   */
  public final Property<CalcNodeSocketConfiguration> calcNodeSocketConfiguration() {
    return metaBean().calcNodeSocketConfiguration().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the repository configuration source.
   * @return the value of the property, not null
   */
  public RepositoryConfigurationSource getRepositoryConfigurationSource() {
    return _repositoryConfigurationSource;
  }

  /**
   * Sets the repository configuration source.
   * @param repositoryConfigurationSource  the new value of the property, not null
   */
  public void setRepositoryConfigurationSource(RepositoryConfigurationSource repositoryConfigurationSource) {
    JodaBeanUtils.notNull(repositoryConfigurationSource, "repositoryConfigurationSource");
    this._repositoryConfigurationSource = repositoryConfigurationSource;
  }

  /**
   * Gets the the {@code repositoryConfigurationSource} property.
   * @return the property, not null
   */
  public final Property<RepositoryConfigurationSource> repositoryConfigurationSource() {
    return metaBean().repositoryConfigurationSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the available outputs.
   * @return the value of the property, not null
   */
  public AvailableOutputsProvider getAvailableOutputs() {
    return _availableOutputs;
  }

  /**
   * Sets the available outputs.
   * @param availableOutputs  the new value of the property, not null
   */
  public void setAvailableOutputs(AvailableOutputsProvider availableOutputs) {
    JodaBeanUtils.notNull(availableOutputs, "availableOutputs");
    this._availableOutputs = availableOutputs;
  }

  /**
   * Gets the the {@code availableOutputs} property.
   * @return the property, not null
   */
  public final Property<AvailableOutputsProvider> availableOutputs() {
    return metaBean().availableOutputs().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the dependency graph builder.
   * @return the value of the property, not null
   */
  public DependencyGraphBuilderService getDependencyGraphBuilder() {
    return _dependencyGraphBuilder;
  }

  /**
   * Sets the dependency graph builder.
   * @param dependencyGraphBuilder  the new value of the property, not null
   */
  public void setDependencyGraphBuilder(DependencyGraphBuilderService dependencyGraphBuilder) {
    JodaBeanUtils.notNull(dependencyGraphBuilder, "dependencyGraphBuilder");
    this._dependencyGraphBuilder = dependencyGraphBuilder;
  }

  /**
   * Gets the the {@code dependencyGraphBuilder} property.
   * @return the property, not null
   */
  public final Property<DependencyGraphBuilderService> dependencyGraphBuilder() {
    return metaBean().dependencyGraphBuilder().createProperty(this);
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
   * Gets the view processor id.
   * @return the value of the property, not null
   */
  public String getViewProcessorId() {
    return _viewProcessorId;
  }

  /**
   * Sets the view processor id.
   * @param viewProcessorId  the new value of the property, not null
   */
  public void setViewProcessorId(String viewProcessorId) {
    JodaBeanUtils.notNull(viewProcessorId, "viewProcessorId");
    this._viewProcessorId = viewProcessorId;
  }

  /**
   * Gets the the {@code viewProcessorId} property.
   * @return the property, not null
   */
  public final Property<String> viewProcessorId() {
    return metaBean().viewProcessorId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS broker URI.
   * @return the value of the property, not null
   */
  public String getJmsBrokerUri() {
    return _jmsBrokerUri;
  }

  /**
   * Sets the JMS broker URI.
   * @param jmsBrokerUri  the new value of the property, not null
   */
  public void setJmsBrokerUri(String jmsBrokerUri) {
    JodaBeanUtils.notNull(jmsBrokerUri, "jmsBrokerUri");
    this._jmsBrokerUri = jmsBrokerUri;
  }

  /**
   * Gets the the {@code jmsBrokerUri} property.
   * @return the property, not null
   */
  public final Property<String> jmsBrokerUri() {
    return metaBean().jmsBrokerUri().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code EngineComponentFactory}.
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
        this, "classifier", EngineComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code description} property.
     */
    private final MetaProperty<String> _description = DirectMetaProperty.ofReadWrite(
        this, "description", EngineComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", EngineComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", EngineComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code portfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _portfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "portfolioMaster", EngineComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _marketDataSnapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotMaster", EngineComponentFactory.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     */
    private final MetaProperty<HistoricalTimeSeriesMaster> _historicalTimeSeriesMaster = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesMaster", EngineComponentFactory.class, HistoricalTimeSeriesMaster.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", EngineComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code exchangeSource} property.
     */
    private final MetaProperty<ExchangeSource> _exchangeSource = DirectMetaProperty.ofReadWrite(
        this, "exchangeSource", EngineComponentFactory.class, ExchangeSource.class);
    /**
     * The meta-property for the {@code holidaySource} property.
     */
    private final MetaProperty<HolidaySource> _holidaySource = DirectMetaProperty.ofReadWrite(
        this, "holidaySource", EngineComponentFactory.class, HolidaySource.class);
    /**
     * The meta-property for the {@code regionSource} property.
     */
    private final MetaProperty<RegionSource> _regionSource = DirectMetaProperty.ofReadWrite(
        this, "regionSource", EngineComponentFactory.class, RegionSource.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", EngineComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code positionSource} property.
     */
    private final MetaProperty<PositionSource> _positionSource = DirectMetaProperty.ofReadWrite(
        this, "positionSource", EngineComponentFactory.class, PositionSource.class);
    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     */
    private final MetaProperty<MarketDataSnapshotSource> _marketDataSnapshotSource = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotSource", EngineComponentFactory.class, MarketDataSnapshotSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", EngineComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code currencyMatrixSource} property.
     */
    private final MetaProperty<CurrencyMatrixSource> _currencyMatrixSource = DirectMetaProperty.ofReadWrite(
        this, "currencyMatrixSource", EngineComponentFactory.class, CurrencyMatrixSource.class);
    /**
     * The meta-property for the {@code currencyPairsSource} property.
     */
    private final MetaProperty<CurrencyPairsSource> _currencyPairsSource = DirectMetaProperty.ofReadWrite(
        this, "currencyPairsSource", EngineComponentFactory.class, CurrencyPairsSource.class);
    /**
     * The meta-property for the {@code interpolatedYieldCurveDefinitionSource} property.
     */
    private final MetaProperty<InterpolatedYieldCurveDefinitionSource> _interpolatedYieldCurveDefinitionSource = DirectMetaProperty.ofReadWrite(
        this, "interpolatedYieldCurveDefinitionSource", EngineComponentFactory.class, InterpolatedYieldCurveDefinitionSource.class);
    /**
     * The meta-property for the {@code interpolatedYieldCurveSpecificationBuilder} property.
     */
    private final MetaProperty<InterpolatedYieldCurveSpecificationBuilder> _interpolatedYieldCurveSpecificationBuilder = DirectMetaProperty.ofReadWrite(
        this, "interpolatedYieldCurveSpecificationBuilder", EngineComponentFactory.class, InterpolatedYieldCurveSpecificationBuilder.class);
    /**
     * The meta-property for the {@code volatilityCubeDefinitionSource} property.
     */
    private final MetaProperty<VolatilityCubeDefinitionSource> _volatilityCubeDefinitionSource = DirectMetaProperty.ofReadWrite(
        this, "volatilityCubeDefinitionSource", EngineComponentFactory.class, VolatilityCubeDefinitionSource.class);
    /**
     * The meta-property for the {@code userManager} property.
     */
    private final MetaProperty<FinancialUserManager> _userManager = DirectMetaProperty.ofReadWrite(
        this, "userManager", EngineComponentFactory.class, FinancialUserManager.class);
    /**
     * The meta-property for the {@code adHocBatchDbManager} property.
     */
    private final MetaProperty<AdHocBatchDbManager> _adHocBatchDbManager = DirectMetaProperty.ofReadWrite(
        this, "adHocBatchDbManager", EngineComponentFactory.class, AdHocBatchDbManager.class);
    /**
     * The meta-property for the {@code calcNodeSocketConfiguration} property.
     */
    private final MetaProperty<CalcNodeSocketConfiguration> _calcNodeSocketConfiguration = DirectMetaProperty.ofReadWrite(
        this, "calcNodeSocketConfiguration", EngineComponentFactory.class, CalcNodeSocketConfiguration.class);
    /**
     * The meta-property for the {@code repositoryConfigurationSource} property.
     */
    private final MetaProperty<RepositoryConfigurationSource> _repositoryConfigurationSource = DirectMetaProperty.ofReadWrite(
        this, "repositoryConfigurationSource", EngineComponentFactory.class, RepositoryConfigurationSource.class);
    /**
     * The meta-property for the {@code availableOutputs} property.
     */
    private final MetaProperty<AvailableOutputsProvider> _availableOutputs = DirectMetaProperty.ofReadWrite(
        this, "availableOutputs", EngineComponentFactory.class, AvailableOutputsProvider.class);
    /**
     * The meta-property for the {@code dependencyGraphBuilder} property.
     */
    private final MetaProperty<DependencyGraphBuilderService> _dependencyGraphBuilder = DirectMetaProperty.ofReadWrite(
        this, "dependencyGraphBuilder", EngineComponentFactory.class, DependencyGraphBuilderService.class);
    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", EngineComponentFactory.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code viewProcessorId} property.
     */
    private final MetaProperty<String> _viewProcessorId = DirectMetaProperty.ofReadWrite(
        this, "viewProcessorId", EngineComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code jmsBrokerUri} property.
     */
    private final MetaProperty<String> _jmsBrokerUri = DirectMetaProperty.ofReadWrite(
        this, "jmsBrokerUri", EngineComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "description",
        "securityMaster",
        "positionMaster",
        "portfolioMaster",
        "marketDataSnapshotMaster",
        "historicalTimeSeriesMaster",
        "configSource",
        "exchangeSource",
        "holidaySource",
        "regionSource",
        "securitySource",
        "positionSource",
        "marketDataSnapshotSource",
        "historicalTimeSeriesSource",
        "currencyMatrixSource",
        "currencyPairsSource",
        "interpolatedYieldCurveDefinitionSource",
        "interpolatedYieldCurveSpecificationBuilder",
        "volatilityCubeDefinitionSource",
        "userManager",
        "adHocBatchDbManager",
        "calcNodeSocketConfiguration",
        "repositoryConfigurationSource",
        "availableOutputs",
        "dependencyGraphBuilder",
        "viewProcessor",
        "viewProcessorId",
        "jmsBrokerUri");

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
        case -1724546052:  // description
          return _description;
        case -887218750:  // securityMaster
          return _securityMaster;
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -772274742:  // portfolioMaster
          return _portfolioMaster;
        case 2090650860:  // marketDataSnapshotMaster
          return _marketDataSnapshotMaster;
        case 173967376:  // historicalTimeSeriesMaster
          return _historicalTimeSeriesMaster;
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
        case -2019554651:  // marketDataSnapshotSource
          return _marketDataSnapshotSource;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case 615188973:  // currencyMatrixSource
          return _currencyMatrixSource;
        case -1615906429:  // currencyPairsSource
          return _currencyPairsSource;
        case -582658381:  // interpolatedYieldCurveDefinitionSource
          return _interpolatedYieldCurveDefinitionSource;
        case -461125123:  // interpolatedYieldCurveSpecificationBuilder
          return _interpolatedYieldCurveSpecificationBuilder;
        case 1540542824:  // volatilityCubeDefinitionSource
          return _volatilityCubeDefinitionSource;
        case 533393762:  // userManager
          return _userManager;
        case -1721311058:  // adHocBatchDbManager
          return _adHocBatchDbManager;
        case 644418764:  // calcNodeSocketConfiguration
          return _calcNodeSocketConfiguration;
        case 416791815:  // repositoryConfigurationSource
          return _repositoryConfigurationSource;
        case 602280073:  // availableOutputs
          return _availableOutputs;
        case 713351512:  // dependencyGraphBuilder
          return _dependencyGraphBuilder;
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case 736640360:  // viewProcessorId
          return _viewProcessorId;
        case 2047189283:  // jmsBrokerUri
          return _jmsBrokerUri;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends EngineComponentFactory> builder() {
      return new DirectBeanBuilder<EngineComponentFactory>(new EngineComponentFactory());
    }

    @Override
    public Class<? extends EngineComponentFactory> beanType() {
      return EngineComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
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
     * The meta-property for the {@code description} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> description() {
      return _description;
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
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
      return _marketDataSnapshotMaster;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
      return _historicalTimeSeriesMaster;
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
     * The meta-property for the {@code marketDataSnapshotSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotSource> marketDataSnapshotSource() {
      return _marketDataSnapshotSource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
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
    public final MetaProperty<CurrencyPairsSource> currencyPairsSource() {
      return _currencyPairsSource;
    }

    /**
     * The meta-property for the {@code interpolatedYieldCurveDefinitionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterpolatedYieldCurveDefinitionSource> interpolatedYieldCurveDefinitionSource() {
      return _interpolatedYieldCurveDefinitionSource;
    }

    /**
     * The meta-property for the {@code interpolatedYieldCurveSpecificationBuilder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterpolatedYieldCurveSpecificationBuilder> interpolatedYieldCurveSpecificationBuilder() {
      return _interpolatedYieldCurveSpecificationBuilder;
    }

    /**
     * The meta-property for the {@code volatilityCubeDefinitionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VolatilityCubeDefinitionSource> volatilityCubeDefinitionSource() {
      return _volatilityCubeDefinitionSource;
    }

    /**
     * The meta-property for the {@code userManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FinancialUserManager> userManager() {
      return _userManager;
    }

    /**
     * The meta-property for the {@code adHocBatchDbManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<AdHocBatchDbManager> adHocBatchDbManager() {
      return _adHocBatchDbManager;
    }

    /**
     * The meta-property for the {@code calcNodeSocketConfiguration} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CalcNodeSocketConfiguration> calcNodeSocketConfiguration() {
      return _calcNodeSocketConfiguration;
    }

    /**
     * The meta-property for the {@code repositoryConfigurationSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RepositoryConfigurationSource> repositoryConfigurationSource() {
      return _repositoryConfigurationSource;
    }

    /**
     * The meta-property for the {@code availableOutputs} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<AvailableOutputsProvider> availableOutputs() {
      return _availableOutputs;
    }

    /**
     * The meta-property for the {@code dependencyGraphBuilder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DependencyGraphBuilderService> dependencyGraphBuilder() {
      return _dependencyGraphBuilder;
    }

    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code viewProcessorId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> viewProcessorId() {
      return _viewProcessorId;
    }

    /**
     * The meta-property for the {@code jmsBrokerUri} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsBrokerUri() {
      return _jmsBrokerUri;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
