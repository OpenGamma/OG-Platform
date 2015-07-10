/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;

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

import com.google.common.collect.ImmutableMap;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklist;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.model.pnl.DefaultPnLRequirementsGatherer;
import com.opengamma.financial.analytics.model.pnl.PnLRequirementsGatherer;
import com.opengamma.financial.analytics.riskfactors.DefaultRiskFactorsConfigurationProvider;
import com.opengamma.financial.analytics.riskfactors.DefaultRiskFactorsGatherer;
import com.opengamma.financial.analytics.riskfactors.RiskFactorsGatherer;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.marketdata.MarketDataELCompiler;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Component factory for the config source.
 */
@BeanDefinition
@SuppressWarnings("deprecation")
public class EngineContextsComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The config source.
   * <p>
   * Where possible, components should not be tightly coupled to the configuration database. An intermediate interface, with an implementation that is backed by a ConfigSource, allows the flexibility
   * to source that data from an external system, or a more efficient storage mechanism, in the future.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;
  /**
   * The config master. This might only be a temporary addition; most services should be written to back onto this if necessary rather than data be accessed directly from the config master. This
   * allows the flexibility to have data stored in another system or more efficient storage specific to that type.
   * <p>
   * This is currently required to replace the functionality previously offered by ViewDefinitionRepository which exposed both user maintained views from the persistent config master and
   * temporary/short-lived views created programatically.
   */
  @PropertyDefinition
  private ConfigMaster _configMaster;
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
   * The target resolver.
   */
  @PropertyDefinition(validate = "notNull")
  private ComputationTargetResolver _targetResolver;
  /**
   * The region source.
   */
  @PropertyDefinition(validate = "notNull")
  private RegionSource _regionSource;
  /**
   * The organization source.
   */
  @PropertyDefinition(validate = "notNull")
  private LegalEntitySource _legalEntitySource;
  /**
   * The convention bundle source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConventionBundleSource _conventionBundleSource;
  /**
   * The yield curve definition source.
   */
  @PropertyDefinition()
  private InterpolatedYieldCurveDefinitionSource _interpolatedYieldCurveDefinitionSource;
  /**
   * The yield curve specification source.
   */
  @PropertyDefinition()
  private InterpolatedYieldCurveSpecificationBuilder _interpolatedYieldCurveSpecificationBuilder;
  /**
   * The volitility cube source.
   */
  @PropertyDefinition()
  private VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  /**
   * The holiday source.
   */
  @PropertyDefinition(validate = "notNull")
  private HolidaySource _holidaySource;
  /**
   * The exchange source.
   */
  @PropertyDefinition(validate = "notNull")
  private ExchangeSource _exchangeSource;
  /**
   * The time-series source.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The time-series resolver.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;
  /**
   * The convention source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConventionSource _conventionSource;
  /**
   * The execution blacklist. View processors will not submit nodes matched by this blacklist for execution.
   */
  @PropertyDefinition
  private FunctionBlacklist _executionBlacklist;
  /**
   * The compilation blacklist. Dependency graph builders will not produce graphs which contain nodes matched by this blacklist.
   */
  @PropertyDefinition
  private FunctionBlacklist _compilationBlacklist;
  /**
   * The temporary target repository.
   */
  @PropertyDefinition
  private TempTargetRepository _tempTargetRepository;

  /**
   * The slave view processor executing functions can make requests to. This might be the view processor that owns the context, but might be a different but compatible one.
   */
  @PropertyDefinition
  private ViewProcessor _viewProcessor;
  /**
   * The permissive behavior flag.
   */
  @PropertyDefinition
  private Boolean _permissive = Boolean.FALSE;
  /**
   * The PnL requirements gatherer.
   */
  @PropertyDefinition
  private PnLRequirementsGatherer _pnlRequirementsGatherer;
  /**
   * The risk factors requirements gatherer.
   */
  @PropertyDefinition
  private RiskFactorsGatherer _riskFactorsGatherer;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    initPnlRequirementsGatherer();
    initThreadLocalServiceContext();
    initFunctionCompilationContext(repo, configuration);
    final OverrideOperationCompiler ooc = initOverrideOperationCompiler(repo, configuration);
    initFunctionExecutionContext(repo, configuration, ooc);
  }

  protected void initPnlRequirementsGatherer() {
    _pnlRequirementsGatherer = new DefaultPnLRequirementsGatherer() {
      {
        addCurveCalculationConfig("USD", "DefaultTwoCurveUSDConfig");
        addFXCurveCalculationConfig("USD", "DefaultTwoCurveUSDConfig");
        addIRFuturesCurveCalculationConfig("USD", "DefaultTwoCurveUSDConfig");
        addFXDiscountingCurveName("USD", "Forward3M");
        addCurveCalculationConfig("EUR", "DefaultTwoCurveEURConfig");
        addFXCurveCalculationConfig("EUR", "DefaultTwoCurveEURConfig");
        addIRFuturesCurveCalculationConfig("EUR", "DefaultTwoCurveEURConfig");
        addFXDiscountingCurveName("EUR", "Forward6M");
        addCurveCalculationConfig("CAD", "DefaultTwoCurveCADConfig");
        addFXCurveCalculationConfig("CAD", "DefaultTwoCurveCADConfig");
        addIRFuturesCurveCalculationConfig("CAD", "DefaultTwoCurveCADConfig");
        addFXDiscountingCurveName("CAD", "Forward3M");
        addCurveCalculationConfig("AUD", "DefaultThreeCurveAUDConfig");
        addFXCurveCalculationConfig("AUD", "DefaultThreeCurveAUDConfig");
        addIRFuturesCurveCalculationConfig("AUD", "DefaultThreeCurveAUDConfig");
        addFXDiscountingCurveName("AUD", "ForwardBasis3M");
        addCurveCalculationConfig("CHF", "DefaultTwoCurveCHFConfig");
        addFXCurveCalculationConfig("CHF", "DefaultTwoCurveCHFConfig");
        addIRFuturesCurveCalculationConfig("CHF", "DefaultTwoCurveCHFConfig");
        addFXDiscountingCurveName("CHF", "Forward6M");
        addCurveCalculationConfig("GBP", "DefaultTwoCurveGBPConfig");
        addFXCurveCalculationConfig("GBP", "DefaultTwoCurveGBPConfig");
        addIRFuturesCurveCalculationConfig("GBP", "DefaultTwoCurveGBPConfig");
        addFXDiscountingCurveName("USD", "Forward3M");
      }
    };
  }

  private void initThreadLocalServiceContext() {
    
    VersionCorrectionProvider vcProvider = new VersionCorrectionProvider() {
        @Override
        public VersionCorrection getPortfolioVersionCorrection() {
          return VersionCorrection.LATEST;
        }
        @Override
        public VersionCorrection getConfigVersionCorrection() {
          return VersionCorrection.LATEST;
        }
      };

    ImmutableMap.Builder<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>builder()
        .put(ConfigSource.class, getConfigSource())
        .put(RegionSource.class, getRegionSource())
        .put(ConventionBundleSource.class, getConventionBundleSource())
        .put(ConventionSource.class, getConventionSource())
        .put(HolidaySource.class, getHolidaySource())
        .put(ExchangeSource.class, getExchangeSource())
        .put(HistoricalTimeSeriesSource.class, getHistoricalTimeSeriesSource())
        .put(HistoricalTimeSeriesResolver.class, getHistoricalTimeSeriesResolver())
        .put(SecuritySource.class, getSecuritySource())
        .put(LegalEntitySource.class, getLegalEntitySource())
        .put(PositionSource.class, getPositionSource());

    if (getInterpolatedYieldCurveDefinitionSource() != null) {
      services.put(InterpolatedYieldCurveDefinitionSource.class, getInterpolatedYieldCurveDefinitionSource());
    }
    if (getInterpolatedYieldCurveSpecificationBuilder() != null) {
      services.put(InterpolatedYieldCurveSpecificationBuilder.class, getInterpolatedYieldCurveSpecificationBuilder());
    }
    if (getVolatilityCubeDefinitionSource() != null) {
      services.put(VolatilityCubeDefinitionSource.class, getVolatilityCubeDefinitionSource());
    }
    services.put(VersionCorrectionProvider.class, vcProvider);

    ThreadLocalServiceContext.init(ServiceContext.of(services.build()));
  }


  protected void initFunctionCompilationContext(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, getConfigSource());
    OpenGammaCompilationContext.setRegionSource(context, getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, getConventionBundleSource());
    OpenGammaCompilationContext.setConventionSource(context, getConventionSource());
    OpenGammaCompilationContext.setInterpolatedYieldCurveDefinitionSource(context, getInterpolatedYieldCurveDefinitionSource());
    OpenGammaCompilationContext.setInterpolatedYieldCurveSpecificationBuilder(context, getInterpolatedYieldCurveSpecificationBuilder());
    OpenGammaCompilationContext.setVolatilityCubeDefinitionSource(context, getVolatilityCubeDefinitionSource());
    OpenGammaCompilationContext.setHolidaySource(context, getHolidaySource());
    OpenGammaCompilationContext.setExchangeSource(context, getExchangeSource());
    OpenGammaCompilationContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    OpenGammaCompilationContext.setHistoricalTimeSeriesResolver(context, getHistoricalTimeSeriesResolver());
    if (getTempTargetRepository() != null) {
      OpenGammaCompilationContext.setTempTargets(context, getTempTargetRepository());
    }
    context.setSecuritySource(getSecuritySource());
    context.setLegalEntitySource(getLegalEntitySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    context.setRawComputationTargetResolver(getTargetResolver());
    if (getCompilationBlacklist() != null) {
      context.setGraphBuildingBlacklist(new DefaultFunctionBlacklistQuery(getCompilationBlacklist()));
    }
    if (getExecutionBlacklist() != null) {
      context.setGraphExecutionBlacklist(new DefaultFunctionBlacklistQuery(getExecutionBlacklist()));
    }
    OpenGammaCompilationContext.setPermissive(context, Boolean.TRUE.equals(getPermissive()));
    OpenGammaCompilationContext.setPnLRequirementsGatherer(context, getPnlRequirementsGatherer());
    if (getRiskFactorsGatherer() == null) {
      if (getSecuritySource() != null) {
        setRiskFactorsGatherer(new DefaultRiskFactorsGatherer(getSecuritySource(), new DefaultRiskFactorsConfigurationProvider()));
      }
    }
    if (getRiskFactorsGatherer() != null) {
      OpenGammaCompilationContext.setRiskFactorsGatherer(context, getRiskFactorsGatherer());
    }
    final ComponentInfo info = new ComponentInfo(FunctionCompilationContext.class, getClassifier());
    repo.registerComponent(info, context);
  }

  protected OverrideOperationCompiler initOverrideOperationCompiler(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final OverrideOperationCompiler ooc = new MarketDataELCompiler();
    final ComponentInfo info = new ComponentInfo(OverrideOperationCompiler.class, getClassifier());
    repo.registerComponent(info, ooc);
    return ooc;
  }

  protected void initFunctionExecutionContext(final ComponentRepository repo, final LinkedHashMap<String, String> configuration, final OverrideOperationCompiler ooc) {
    final FunctionExecutionContext context = new FunctionExecutionContext();
    OpenGammaExecutionContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    OpenGammaExecutionContext.setRegionSource(context, getRegionSource());
    OpenGammaExecutionContext.setExchangeSource(context, getExchangeSource());
    OpenGammaExecutionContext.setHolidaySource(context, getHolidaySource());
    OpenGammaExecutionContext.setLegalEntitySource(context, getLegalEntitySource());
    OpenGammaExecutionContext.setConventionBundleSource(context, getConventionBundleSource());
    OpenGammaExecutionContext.setConventionSource(context, getConventionSource());
    OpenGammaExecutionContext.setLegalEntitySource(context, getLegalEntitySource());
    OpenGammaExecutionContext.setConfigSource(context, getConfigSource());
    if (getConfigMaster() != null) {
      OpenGammaExecutionContext.setConfigMaster(context, getConfigMaster());
    }
    OpenGammaExecutionContext.setOverrideOperationCompiler(context, ooc);
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    if (getViewProcessor() != null) {
      OpenGammaExecutionContext.setViewProcessor(context, getViewProcessor());
    }
    final ComponentInfo info = new ComponentInfo(FunctionExecutionContext.class, getClassifier());
    repo.registerComponent(info, context);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code EngineContextsComponentFactory}.
   * @return the meta-bean, not null
   */
  public static EngineContextsComponentFactory.Meta meta() {
    return EngineContextsComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(EngineContextsComponentFactory.Meta.INSTANCE);
  }

  @Override
  public EngineContextsComponentFactory.Meta metaBean() {
    return EngineContextsComponentFactory.Meta.INSTANCE;
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
   * Gets the config source.
   * <p>
   * Where possible, components should not be tightly coupled to the configuration database. An intermediate interface, with an implementation that is backed by a ConfigSource, allows the flexibility
   * to source that data from an external system, or a more efficient storage mechanism, in the future.
   * @return the value of the property, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source.
   * <p>
   * Where possible, components should not be tightly coupled to the configuration database. An intermediate interface, with an implementation that is backed by a ConfigSource, allows the flexibility
   * to source that data from an external system, or a more efficient storage mechanism, in the future.
   * @param configSource  the new value of the property, not null
   */
  public void setConfigSource(ConfigSource configSource) {
    JodaBeanUtils.notNull(configSource, "configSource");
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * <p>
   * Where possible, components should not be tightly coupled to the configuration database. An intermediate interface, with an implementation that is backed by a ConfigSource, allows the flexibility
   * to source that data from an external system, or a more efficient storage mechanism, in the future.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master. This might only be a temporary addition; most services should be written to back onto this if necessary rather than data be accessed directly from the config master. This
   * allows the flexibility to have data stored in another system or more efficient storage specific to that type.
   * <p>
   * This is currently required to replace the functionality previously offered by ViewDefinitionRepository which exposed both user maintained views from the persistent config master and
   * temporary/short-lived views created programatically.
   * @return the value of the property
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master. This might only be a temporary addition; most services should be written to back onto this if necessary rather than data be accessed directly from the config master. This
   * allows the flexibility to have data stored in another system or more efficient storage specific to that type.
   * <p>
   * This is currently required to replace the functionality previously offered by ViewDefinitionRepository which exposed both user maintained views from the persistent config master and
   * temporary/short-lived views created programatically.
   * @param configMaster  the new value of the property
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * allows the flexibility to have data stored in another system or more efficient storage specific to that type.
   * <p>
   * This is currently required to replace the functionality previously offered by ViewDefinitionRepository which exposed both user maintained views from the persistent config master and
   * temporary/short-lived views created programatically.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
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
   * Gets the target resolver.
   * @return the value of the property, not null
   */
  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  /**
   * Sets the target resolver.
   * @param targetResolver  the new value of the property, not null
   */
  public void setTargetResolver(ComputationTargetResolver targetResolver) {
    JodaBeanUtils.notNull(targetResolver, "targetResolver");
    this._targetResolver = targetResolver;
  }

  /**
   * Gets the the {@code targetResolver} property.
   * @return the property, not null
   */
  public final Property<ComputationTargetResolver> targetResolver() {
    return metaBean().targetResolver().createProperty(this);
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
   * Gets the organization source.
   * @return the value of the property, not null
   */
  public LegalEntitySource getLegalEntitySource() {
    return _legalEntitySource;
  }

  /**
   * Sets the organization source.
   * @param legalEntitySource  the new value of the property, not null
   */
  public void setLegalEntitySource(LegalEntitySource legalEntitySource) {
    JodaBeanUtils.notNull(legalEntitySource, "legalEntitySource");
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
   * Gets the convention bundle source.
   * @return the value of the property, not null
   */
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  /**
   * Sets the convention bundle source.
   * @param conventionBundleSource  the new value of the property, not null
   */
  public void setConventionBundleSource(ConventionBundleSource conventionBundleSource) {
    JodaBeanUtils.notNull(conventionBundleSource, "conventionBundleSource");
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
   * Gets the time-series resolver.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  /**
   * Sets the time-series resolver.
   * @param historicalTimeSeriesResolver  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesResolver(HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    JodaBeanUtils.notNull(historicalTimeSeriesResolver, "historicalTimeSeriesResolver");
    this._historicalTimeSeriesResolver = historicalTimeSeriesResolver;
  }

  /**
   * Gets the the {@code historicalTimeSeriesResolver} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesResolver> historicalTimeSeriesResolver() {
    return metaBean().historicalTimeSeriesResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention source.
   * @return the value of the property, not null
   */
  public ConventionSource getConventionSource() {
    return _conventionSource;
  }

  /**
   * Sets the convention source.
   * @param conventionSource  the new value of the property, not null
   */
  public void setConventionSource(ConventionSource conventionSource) {
    JodaBeanUtils.notNull(conventionSource, "conventionSource");
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
   * Gets the execution blacklist. View processors will not submit nodes matched by this blacklist for execution.
   * @return the value of the property
   */
  public FunctionBlacklist getExecutionBlacklist() {
    return _executionBlacklist;
  }

  /**
   * Sets the execution blacklist. View processors will not submit nodes matched by this blacklist for execution.
   * @param executionBlacklist  the new value of the property
   */
  public void setExecutionBlacklist(FunctionBlacklist executionBlacklist) {
    this._executionBlacklist = executionBlacklist;
  }

  /**
   * Gets the the {@code executionBlacklist} property.
   * @return the property, not null
   */
  public final Property<FunctionBlacklist> executionBlacklist() {
    return metaBean().executionBlacklist().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the compilation blacklist. Dependency graph builders will not produce graphs which contain nodes matched by this blacklist.
   * @return the value of the property
   */
  public FunctionBlacklist getCompilationBlacklist() {
    return _compilationBlacklist;
  }

  /**
   * Sets the compilation blacklist. Dependency graph builders will not produce graphs which contain nodes matched by this blacklist.
   * @param compilationBlacklist  the new value of the property
   */
  public void setCompilationBlacklist(FunctionBlacklist compilationBlacklist) {
    this._compilationBlacklist = compilationBlacklist;
  }

  /**
   * Gets the the {@code compilationBlacklist} property.
   * @return the property, not null
   */
  public final Property<FunctionBlacklist> compilationBlacklist() {
    return metaBean().compilationBlacklist().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the temporary target repository.
   * @return the value of the property
   */
  public TempTargetRepository getTempTargetRepository() {
    return _tempTargetRepository;
  }

  /**
   * Sets the temporary target repository.
   * @param tempTargetRepository  the new value of the property
   */
  public void setTempTargetRepository(TempTargetRepository tempTargetRepository) {
    this._tempTargetRepository = tempTargetRepository;
  }

  /**
   * Gets the the {@code tempTargetRepository} property.
   * @return the property, not null
   */
  public final Property<TempTargetRepository> tempTargetRepository() {
    return metaBean().tempTargetRepository().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the slave view processor executing functions can make requests to. This might be the view processor that owns the context, but might be a different but compatible one.
   * @return the value of the property
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Sets the slave view processor executing functions can make requests to. This might be the view processor that owns the context, but might be a different but compatible one.
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
   * Gets the permissive behavior flag.
   * @return the value of the property
   */
  public Boolean getPermissive() {
    return _permissive;
  }

  /**
   * Sets the permissive behavior flag.
   * @param permissive  the new value of the property
   */
  public void setPermissive(Boolean permissive) {
    this._permissive = permissive;
  }

  /**
   * Gets the the {@code permissive} property.
   * @return the property, not null
   */
  public final Property<Boolean> permissive() {
    return metaBean().permissive().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the PnL requirements gatherer.
   * @return the value of the property
   */
  public PnLRequirementsGatherer getPnlRequirementsGatherer() {
    return _pnlRequirementsGatherer;
  }

  /**
   * Sets the PnL requirements gatherer.
   * @param pnlRequirementsGatherer  the new value of the property
   */
  public void setPnlRequirementsGatherer(PnLRequirementsGatherer pnlRequirementsGatherer) {
    this._pnlRequirementsGatherer = pnlRequirementsGatherer;
  }

  /**
   * Gets the the {@code pnlRequirementsGatherer} property.
   * @return the property, not null
   */
  public final Property<PnLRequirementsGatherer> pnlRequirementsGatherer() {
    return metaBean().pnlRequirementsGatherer().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the risk factors requirements gatherer.
   * @return the value of the property
   */
  public RiskFactorsGatherer getRiskFactorsGatherer() {
    return _riskFactorsGatherer;
  }

  /**
   * Sets the risk factors requirements gatherer.
   * @param riskFactorsGatherer  the new value of the property
   */
  public void setRiskFactorsGatherer(RiskFactorsGatherer riskFactorsGatherer) {
    this._riskFactorsGatherer = riskFactorsGatherer;
  }

  /**
   * Gets the the {@code riskFactorsGatherer} property.
   * @return the property, not null
   */
  public final Property<RiskFactorsGatherer> riskFactorsGatherer() {
    return metaBean().riskFactorsGatherer().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public EngineContextsComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      EngineContextsComponentFactory other = (EngineContextsComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getTargetResolver(), other.getTargetResolver()) &&
          JodaBeanUtils.equal(getRegionSource(), other.getRegionSource()) &&
          JodaBeanUtils.equal(getLegalEntitySource(), other.getLegalEntitySource()) &&
          JodaBeanUtils.equal(getConventionBundleSource(), other.getConventionBundleSource()) &&
          JodaBeanUtils.equal(getInterpolatedYieldCurveDefinitionSource(), other.getInterpolatedYieldCurveDefinitionSource()) &&
          JodaBeanUtils.equal(getInterpolatedYieldCurveSpecificationBuilder(), other.getInterpolatedYieldCurveSpecificationBuilder()) &&
          JodaBeanUtils.equal(getVolatilityCubeDefinitionSource(), other.getVolatilityCubeDefinitionSource()) &&
          JodaBeanUtils.equal(getHolidaySource(), other.getHolidaySource()) &&
          JodaBeanUtils.equal(getExchangeSource(), other.getExchangeSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesResolver(), other.getHistoricalTimeSeriesResolver()) &&
          JodaBeanUtils.equal(getConventionSource(), other.getConventionSource()) &&
          JodaBeanUtils.equal(getExecutionBlacklist(), other.getExecutionBlacklist()) &&
          JodaBeanUtils.equal(getCompilationBlacklist(), other.getCompilationBlacklist()) &&
          JodaBeanUtils.equal(getTempTargetRepository(), other.getTempTargetRepository()) &&
          JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getPermissive(), other.getPermissive()) &&
          JodaBeanUtils.equal(getPnlRequirementsGatherer(), other.getPnlRequirementsGatherer()) &&
          JodaBeanUtils.equal(getRiskFactorsGatherer(), other.getRiskFactorsGatherer()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTargetResolver());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRegionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLegalEntitySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConventionBundleSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getInterpolatedYieldCurveDefinitionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getInterpolatedYieldCurveSpecificationBuilder());
    hash = hash * 31 + JodaBeanUtils.hashCode(getVolatilityCubeDefinitionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHolidaySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExchangeSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesResolver());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConventionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExecutionBlacklist());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCompilationBlacklist());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTempTargetRepository());
    hash = hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPermissive());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPnlRequirementsGatherer());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRiskFactorsGatherer());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(800);
    buf.append("EngineContextsComponentFactory{");
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
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
    buf.append("configMaster").append('=').append(JodaBeanUtils.toString(getConfigMaster())).append(',').append(' ');
    buf.append("securitySource").append('=').append(JodaBeanUtils.toString(getSecuritySource())).append(',').append(' ');
    buf.append("positionSource").append('=').append(JodaBeanUtils.toString(getPositionSource())).append(',').append(' ');
    buf.append("targetResolver").append('=').append(JodaBeanUtils.toString(getTargetResolver())).append(',').append(' ');
    buf.append("regionSource").append('=').append(JodaBeanUtils.toString(getRegionSource())).append(',').append(' ');
    buf.append("legalEntitySource").append('=').append(JodaBeanUtils.toString(getLegalEntitySource())).append(',').append(' ');
    buf.append("conventionBundleSource").append('=').append(JodaBeanUtils.toString(getConventionBundleSource())).append(',').append(' ');
    buf.append("interpolatedYieldCurveDefinitionSource").append('=').append(JodaBeanUtils.toString(getInterpolatedYieldCurveDefinitionSource())).append(',').append(' ');
    buf.append("interpolatedYieldCurveSpecificationBuilder").append('=').append(JodaBeanUtils.toString(getInterpolatedYieldCurveSpecificationBuilder())).append(',').append(' ');
    buf.append("volatilityCubeDefinitionSource").append('=').append(JodaBeanUtils.toString(getVolatilityCubeDefinitionSource())).append(',').append(' ');
    buf.append("holidaySource").append('=').append(JodaBeanUtils.toString(getHolidaySource())).append(',').append(' ');
    buf.append("exchangeSource").append('=').append(JodaBeanUtils.toString(getExchangeSource())).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesSource())).append(',').append(' ');
    buf.append("historicalTimeSeriesResolver").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesResolver())).append(',').append(' ');
    buf.append("conventionSource").append('=').append(JodaBeanUtils.toString(getConventionSource())).append(',').append(' ');
    buf.append("executionBlacklist").append('=').append(JodaBeanUtils.toString(getExecutionBlacklist())).append(',').append(' ');
    buf.append("compilationBlacklist").append('=').append(JodaBeanUtils.toString(getCompilationBlacklist())).append(',').append(' ');
    buf.append("tempTargetRepository").append('=').append(JodaBeanUtils.toString(getTempTargetRepository())).append(',').append(' ');
    buf.append("viewProcessor").append('=').append(JodaBeanUtils.toString(getViewProcessor())).append(',').append(' ');
    buf.append("permissive").append('=').append(JodaBeanUtils.toString(getPermissive())).append(',').append(' ');
    buf.append("pnlRequirementsGatherer").append('=').append(JodaBeanUtils.toString(getPnlRequirementsGatherer())).append(',').append(' ');
    buf.append("riskFactorsGatherer").append('=').append(JodaBeanUtils.toString(getRiskFactorsGatherer())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code EngineContextsComponentFactory}.
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
        this, "classifier", EngineContextsComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", EngineContextsComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", EngineContextsComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", EngineContextsComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code positionSource} property.
     */
    private final MetaProperty<PositionSource> _positionSource = DirectMetaProperty.ofReadWrite(
        this, "positionSource", EngineContextsComponentFactory.class, PositionSource.class);
    /**
     * The meta-property for the {@code targetResolver} property.
     */
    private final MetaProperty<ComputationTargetResolver> _targetResolver = DirectMetaProperty.ofReadWrite(
        this, "targetResolver", EngineContextsComponentFactory.class, ComputationTargetResolver.class);
    /**
     * The meta-property for the {@code regionSource} property.
     */
    private final MetaProperty<RegionSource> _regionSource = DirectMetaProperty.ofReadWrite(
        this, "regionSource", EngineContextsComponentFactory.class, RegionSource.class);
    /**
     * The meta-property for the {@code legalEntitySource} property.
     */
    private final MetaProperty<LegalEntitySource> _legalEntitySource = DirectMetaProperty.ofReadWrite(
        this, "legalEntitySource", EngineContextsComponentFactory.class, LegalEntitySource.class);
    /**
     * The meta-property for the {@code conventionBundleSource} property.
     */
    private final MetaProperty<ConventionBundleSource> _conventionBundleSource = DirectMetaProperty.ofReadWrite(
        this, "conventionBundleSource", EngineContextsComponentFactory.class, ConventionBundleSource.class);
    /**
     * The meta-property for the {@code interpolatedYieldCurveDefinitionSource} property.
     */
    private final MetaProperty<InterpolatedYieldCurveDefinitionSource> _interpolatedYieldCurveDefinitionSource = DirectMetaProperty.ofReadWrite(
        this, "interpolatedYieldCurveDefinitionSource", EngineContextsComponentFactory.class, InterpolatedYieldCurveDefinitionSource.class);
    /**
     * The meta-property for the {@code interpolatedYieldCurveSpecificationBuilder} property.
     */
    private final MetaProperty<InterpolatedYieldCurveSpecificationBuilder> _interpolatedYieldCurveSpecificationBuilder = DirectMetaProperty.ofReadWrite(
        this, "interpolatedYieldCurveSpecificationBuilder", EngineContextsComponentFactory.class, InterpolatedYieldCurveSpecificationBuilder.class);
    /**
     * The meta-property for the {@code volatilityCubeDefinitionSource} property.
     */
    private final MetaProperty<VolatilityCubeDefinitionSource> _volatilityCubeDefinitionSource = DirectMetaProperty.ofReadWrite(
        this, "volatilityCubeDefinitionSource", EngineContextsComponentFactory.class, VolatilityCubeDefinitionSource.class);
    /**
     * The meta-property for the {@code holidaySource} property.
     */
    private final MetaProperty<HolidaySource> _holidaySource = DirectMetaProperty.ofReadWrite(
        this, "holidaySource", EngineContextsComponentFactory.class, HolidaySource.class);
    /**
     * The meta-property for the {@code exchangeSource} property.
     */
    private final MetaProperty<ExchangeSource> _exchangeSource = DirectMetaProperty.ofReadWrite(
        this, "exchangeSource", EngineContextsComponentFactory.class, ExchangeSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", EngineContextsComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesResolver} property.
     */
    private final MetaProperty<HistoricalTimeSeriesResolver> _historicalTimeSeriesResolver = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesResolver", EngineContextsComponentFactory.class, HistoricalTimeSeriesResolver.class);
    /**
     * The meta-property for the {@code conventionSource} property.
     */
    private final MetaProperty<ConventionSource> _conventionSource = DirectMetaProperty.ofReadWrite(
        this, "conventionSource", EngineContextsComponentFactory.class, ConventionSource.class);
    /**
     * The meta-property for the {@code executionBlacklist} property.
     */
    private final MetaProperty<FunctionBlacklist> _executionBlacklist = DirectMetaProperty.ofReadWrite(
        this, "executionBlacklist", EngineContextsComponentFactory.class, FunctionBlacklist.class);
    /**
     * The meta-property for the {@code compilationBlacklist} property.
     */
    private final MetaProperty<FunctionBlacklist> _compilationBlacklist = DirectMetaProperty.ofReadWrite(
        this, "compilationBlacklist", EngineContextsComponentFactory.class, FunctionBlacklist.class);
    /**
     * The meta-property for the {@code tempTargetRepository} property.
     */
    private final MetaProperty<TempTargetRepository> _tempTargetRepository = DirectMetaProperty.ofReadWrite(
        this, "tempTargetRepository", EngineContextsComponentFactory.class, TempTargetRepository.class);
    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", EngineContextsComponentFactory.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code permissive} property.
     */
    private final MetaProperty<Boolean> _permissive = DirectMetaProperty.ofReadWrite(
        this, "permissive", EngineContextsComponentFactory.class, Boolean.class);
    /**
     * The meta-property for the {@code pnlRequirementsGatherer} property.
     */
    private final MetaProperty<PnLRequirementsGatherer> _pnlRequirementsGatherer = DirectMetaProperty.ofReadWrite(
        this, "pnlRequirementsGatherer", EngineContextsComponentFactory.class, PnLRequirementsGatherer.class);
    /**
     * The meta-property for the {@code riskFactorsGatherer} property.
     */
    private final MetaProperty<RiskFactorsGatherer> _riskFactorsGatherer = DirectMetaProperty.ofReadWrite(
        this, "riskFactorsGatherer", EngineContextsComponentFactory.class, RiskFactorsGatherer.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "configSource",
        "configMaster",
        "securitySource",
        "positionSource",
        "targetResolver",
        "regionSource",
        "legalEntitySource",
        "conventionBundleSource",
        "interpolatedYieldCurveDefinitionSource",
        "interpolatedYieldCurveSpecificationBuilder",
        "volatilityCubeDefinitionSource",
        "holidaySource",
        "exchangeSource",
        "historicalTimeSeriesSource",
        "historicalTimeSeriesResolver",
        "conventionSource",
        "executionBlacklist",
        "compilationBlacklist",
        "tempTargetRepository",
        "viewProcessor",
        "permissive",
        "pnlRequirementsGatherer",
        "riskFactorsGatherer");

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
        case 195157501:  // configSource
          return _configSource;
        case 10395716:  // configMaster
          return _configMaster;
        case -702456965:  // securitySource
          return _securitySource;
        case -1655657820:  // positionSource
          return _positionSource;
        case -1933414217:  // targetResolver
          return _targetResolver;
        case -1636207569:  // regionSource
          return _regionSource;
        case -1759712457:  // legalEntitySource
          return _legalEntitySource;
        case -1281578674:  // conventionBundleSource
          return _conventionBundleSource;
        case -582658381:  // interpolatedYieldCurveDefinitionSource
          return _interpolatedYieldCurveDefinitionSource;
        case -461125123:  // interpolatedYieldCurveSpecificationBuilder
          return _interpolatedYieldCurveSpecificationBuilder;
        case 1540542824:  // volatilityCubeDefinitionSource
          return _volatilityCubeDefinitionSource;
        case 431020691:  // holidaySource
          return _holidaySource;
        case -467239906:  // exchangeSource
          return _exchangeSource;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case -946313676:  // historicalTimeSeriesResolver
          return _historicalTimeSeriesResolver;
        case 225875692:  // conventionSource
          return _conventionSource;
        case -557041435:  // executionBlacklist
          return _executionBlacklist;
        case 1210914458:  // compilationBlacklist
          return _compilationBlacklist;
        case 491227055:  // tempTargetRepository
          return _tempTargetRepository;
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case -517618017:  // permissive
          return _permissive;
        case -1266263066:  // pnlRequirementsGatherer
          return _pnlRequirementsGatherer;
        case 861249085:  // riskFactorsGatherer
          return _riskFactorsGatherer;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends EngineContextsComponentFactory> builder() {
      return new DirectBeanBuilder<EngineContextsComponentFactory>(new EngineContextsComponentFactory());
    }

    @Override
    public Class<? extends EngineContextsComponentFactory> beanType() {
      return EngineContextsComponentFactory.class;
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
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
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
     * The meta-property for the {@code targetResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetResolver> targetResolver() {
      return _targetResolver;
    }

    /**
     * The meta-property for the {@code regionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionSource> regionSource() {
      return _regionSource;
    }

    /**
     * The meta-property for the {@code legalEntitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LegalEntitySource> legalEntitySource() {
      return _legalEntitySource;
    }

    /**
     * The meta-property for the {@code conventionBundleSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionBundleSource> conventionBundleSource() {
      return _conventionBundleSource;
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
     * The meta-property for the {@code holidaySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidaySource> holidaySource() {
      return _holidaySource;
    }

    /**
     * The meta-property for the {@code exchangeSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeSource> exchangeSource() {
      return _exchangeSource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesResolver> historicalTimeSeriesResolver() {
      return _historicalTimeSeriesResolver;
    }

    /**
     * The meta-property for the {@code conventionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionSource> conventionSource() {
      return _conventionSource;
    }

    /**
     * The meta-property for the {@code executionBlacklist} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FunctionBlacklist> executionBlacklist() {
      return _executionBlacklist;
    }

    /**
     * The meta-property for the {@code compilationBlacklist} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FunctionBlacklist> compilationBlacklist() {
      return _compilationBlacklist;
    }

    /**
     * The meta-property for the {@code tempTargetRepository} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<TempTargetRepository> tempTargetRepository() {
      return _tempTargetRepository;
    }

    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code permissive} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> permissive() {
      return _permissive;
    }

    /**
     * The meta-property for the {@code pnlRequirementsGatherer} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PnLRequirementsGatherer> pnlRequirementsGatherer() {
      return _pnlRequirementsGatherer;
    }

    /**
     * The meta-property for the {@code riskFactorsGatherer} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RiskFactorsGatherer> riskFactorsGatherer() {
      return _riskFactorsGatherer;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((EngineContextsComponentFactory) bean).getClassifier();
        case 195157501:  // configSource
          return ((EngineContextsComponentFactory) bean).getConfigSource();
        case 10395716:  // configMaster
          return ((EngineContextsComponentFactory) bean).getConfigMaster();
        case -702456965:  // securitySource
          return ((EngineContextsComponentFactory) bean).getSecuritySource();
        case -1655657820:  // positionSource
          return ((EngineContextsComponentFactory) bean).getPositionSource();
        case -1933414217:  // targetResolver
          return ((EngineContextsComponentFactory) bean).getTargetResolver();
        case -1636207569:  // regionSource
          return ((EngineContextsComponentFactory) bean).getRegionSource();
        case -1759712457:  // legalEntitySource
          return ((EngineContextsComponentFactory) bean).getLegalEntitySource();
        case -1281578674:  // conventionBundleSource
          return ((EngineContextsComponentFactory) bean).getConventionBundleSource();
        case -582658381:  // interpolatedYieldCurveDefinitionSource
          return ((EngineContextsComponentFactory) bean).getInterpolatedYieldCurveDefinitionSource();
        case -461125123:  // interpolatedYieldCurveSpecificationBuilder
          return ((EngineContextsComponentFactory) bean).getInterpolatedYieldCurveSpecificationBuilder();
        case 1540542824:  // volatilityCubeDefinitionSource
          return ((EngineContextsComponentFactory) bean).getVolatilityCubeDefinitionSource();
        case 431020691:  // holidaySource
          return ((EngineContextsComponentFactory) bean).getHolidaySource();
        case -467239906:  // exchangeSource
          return ((EngineContextsComponentFactory) bean).getExchangeSource();
        case 358729161:  // historicalTimeSeriesSource
          return ((EngineContextsComponentFactory) bean).getHistoricalTimeSeriesSource();
        case -946313676:  // historicalTimeSeriesResolver
          return ((EngineContextsComponentFactory) bean).getHistoricalTimeSeriesResolver();
        case 225875692:  // conventionSource
          return ((EngineContextsComponentFactory) bean).getConventionSource();
        case -557041435:  // executionBlacklist
          return ((EngineContextsComponentFactory) bean).getExecutionBlacklist();
        case 1210914458:  // compilationBlacklist
          return ((EngineContextsComponentFactory) bean).getCompilationBlacklist();
        case 491227055:  // tempTargetRepository
          return ((EngineContextsComponentFactory) bean).getTempTargetRepository();
        case -1697555603:  // viewProcessor
          return ((EngineContextsComponentFactory) bean).getViewProcessor();
        case -517618017:  // permissive
          return ((EngineContextsComponentFactory) bean).getPermissive();
        case -1266263066:  // pnlRequirementsGatherer
          return ((EngineContextsComponentFactory) bean).getPnlRequirementsGatherer();
        case 861249085:  // riskFactorsGatherer
          return ((EngineContextsComponentFactory) bean).getRiskFactorsGatherer();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((EngineContextsComponentFactory) bean).setClassifier((String) newValue);
          return;
        case 195157501:  // configSource
          ((EngineContextsComponentFactory) bean).setConfigSource((ConfigSource) newValue);
          return;
        case 10395716:  // configMaster
          ((EngineContextsComponentFactory) bean).setConfigMaster((ConfigMaster) newValue);
          return;
        case -702456965:  // securitySource
          ((EngineContextsComponentFactory) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case -1655657820:  // positionSource
          ((EngineContextsComponentFactory) bean).setPositionSource((PositionSource) newValue);
          return;
        case -1933414217:  // targetResolver
          ((EngineContextsComponentFactory) bean).setTargetResolver((ComputationTargetResolver) newValue);
          return;
        case -1636207569:  // regionSource
          ((EngineContextsComponentFactory) bean).setRegionSource((RegionSource) newValue);
          return;
        case -1759712457:  // legalEntitySource
          ((EngineContextsComponentFactory) bean).setLegalEntitySource((LegalEntitySource) newValue);
          return;
        case -1281578674:  // conventionBundleSource
          ((EngineContextsComponentFactory) bean).setConventionBundleSource((ConventionBundleSource) newValue);
          return;
        case -582658381:  // interpolatedYieldCurveDefinitionSource
          ((EngineContextsComponentFactory) bean).setInterpolatedYieldCurveDefinitionSource((InterpolatedYieldCurveDefinitionSource) newValue);
          return;
        case -461125123:  // interpolatedYieldCurveSpecificationBuilder
          ((EngineContextsComponentFactory) bean).setInterpolatedYieldCurveSpecificationBuilder((InterpolatedYieldCurveSpecificationBuilder) newValue);
          return;
        case 1540542824:  // volatilityCubeDefinitionSource
          ((EngineContextsComponentFactory) bean).setVolatilityCubeDefinitionSource((VolatilityCubeDefinitionSource) newValue);
          return;
        case 431020691:  // holidaySource
          ((EngineContextsComponentFactory) bean).setHolidaySource((HolidaySource) newValue);
          return;
        case -467239906:  // exchangeSource
          ((EngineContextsComponentFactory) bean).setExchangeSource((ExchangeSource) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((EngineContextsComponentFactory) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case -946313676:  // historicalTimeSeriesResolver
          ((EngineContextsComponentFactory) bean).setHistoricalTimeSeriesResolver((HistoricalTimeSeriesResolver) newValue);
          return;
        case 225875692:  // conventionSource
          ((EngineContextsComponentFactory) bean).setConventionSource((ConventionSource) newValue);
          return;
        case -557041435:  // executionBlacklist
          ((EngineContextsComponentFactory) bean).setExecutionBlacklist((FunctionBlacklist) newValue);
          return;
        case 1210914458:  // compilationBlacklist
          ((EngineContextsComponentFactory) bean).setCompilationBlacklist((FunctionBlacklist) newValue);
          return;
        case 491227055:  // tempTargetRepository
          ((EngineContextsComponentFactory) bean).setTempTargetRepository((TempTargetRepository) newValue);
          return;
        case -1697555603:  // viewProcessor
          ((EngineContextsComponentFactory) bean).setViewProcessor((ViewProcessor) newValue);
          return;
        case -517618017:  // permissive
          ((EngineContextsComponentFactory) bean).setPermissive((Boolean) newValue);
          return;
        case -1266263066:  // pnlRequirementsGatherer
          ((EngineContextsComponentFactory) bean).setPnlRequirementsGatherer((PnLRequirementsGatherer) newValue);
          return;
        case 861249085:  // riskFactorsGatherer
          ((EngineContextsComponentFactory) bean).setRiskFactorsGatherer((RiskFactorsGatherer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._configSource, "configSource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._securitySource, "securitySource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._positionSource, "positionSource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._targetResolver, "targetResolver");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._regionSource, "regionSource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._legalEntitySource, "legalEntitySource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._conventionBundleSource, "conventionBundleSource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._holidaySource, "holidaySource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._exchangeSource, "exchangeSource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._historicalTimeSeriesSource, "historicalTimeSeriesSource");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._historicalTimeSeriesResolver, "historicalTimeSeriesResolver");
      JodaBeanUtils.notNull(((EngineContextsComponentFactory) bean)._conventionSource, "conventionSource");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
