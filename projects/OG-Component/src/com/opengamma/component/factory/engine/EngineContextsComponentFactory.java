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

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.marketdata.MarketDataELCompiler;

/**
 * Component factory for the config source.
 */
@BeanDefinition
public class EngineContextsComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The config source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;
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
   * The region source.
   */
  @PropertyDefinition(validate = "notNull")
  private RegionSource _regionSource;
  /**
   * The convention bundle source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConventionBundleSource _conventionBundleSource;
  /**
   * The yield curve definition source.
   */
  @PropertyDefinition(validate = "notNull")
  private InterpolatedYieldCurveDefinitionSource _interpolatedYieldCurveDefinitionSource;
  /**
   * The yield curve specification source.
   */
  @PropertyDefinition(validate = "notNull")
  private InterpolatedYieldCurveSpecificationBuilder _interpolatedYieldCurveSpecificationBuilder;
  /**
   * The volitility cube source.
   */
  @PropertyDefinition(validate = "notNull")
  private VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  /**
   * The currency matrix source.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyMatrixSource _currencyMatrixSource;
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

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    initFunctionCompilationContext(repo, configuration);
    OverrideOperationCompiler ooc = initOverrideOperationCompiler(repo, configuration);
    initFunctionExecutionContext(repo, configuration, ooc);
  }

  protected void initFunctionCompilationContext(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, getConfigSource());
    OpenGammaCompilationContext.setRegionSource(context, getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, getConventionBundleSource());
    OpenGammaCompilationContext.setInterpolatedYieldCurveDefinitionSource(context, getInterpolatedYieldCurveDefinitionSource());
    OpenGammaCompilationContext.setInterpolatedYieldCurveSpecificationBuilder(context, getInterpolatedYieldCurveSpecificationBuilder());
    OpenGammaCompilationContext.setVolatilityCubeDefinitionSource(context, getVolatilityCubeDefinitionSource());
    OpenGammaCompilationContext.setCurrencyMatrixSource(context, getCurrencyMatrixSource());
    OpenGammaCompilationContext.setHolidaySource(context, getHolidaySource());
    OpenGammaCompilationContext.setExchangeSource(context, getExchangeSource());
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    
    ComponentInfo info = new ComponentInfo(FunctionCompilationContext.class, getClassifier());
    repo.registerComponent(info, context);
  }

  protected OverrideOperationCompiler initOverrideOperationCompiler(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    OverrideOperationCompiler ooc = new MarketDataELCompiler(getSecuritySource());
    
    ComponentInfo info = new ComponentInfo(OverrideOperationCompiler.class, getClassifier());
    repo.registerComponent(info, ooc);
    return ooc;
  }

  protected void initFunctionExecutionContext(ComponentRepository repo, LinkedHashMap<String, String> configuration, OverrideOperationCompiler ooc) {
    final FunctionExecutionContext context = new FunctionExecutionContext();
    OpenGammaExecutionContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    OpenGammaExecutionContext.setRegionSource(context, getRegionSource());
    OpenGammaExecutionContext.setExchangeSource(context, getExchangeSource());
    OpenGammaExecutionContext.setHolidaySource(context, getHolidaySource());
    OpenGammaExecutionContext.setConventionBundleSource(context, getConventionBundleSource());
    OpenGammaExecutionContext.setConfigSource(context, getConfigSource());
    OpenGammaExecutionContext.setOverrideOperationCompiler(context, ooc);
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    
    ComponentInfo info = new ComponentInfo(FunctionExecutionContext.class, getClassifier());
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

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case 195157501:  // configSource
        return getConfigSource();
      case -702456965:  // securitySource
        return getSecuritySource();
      case -1655657820:  // positionSource
        return getPositionSource();
      case -1636207569:  // regionSource
        return getRegionSource();
      case -1281578674:  // conventionBundleSource
        return getConventionBundleSource();
      case -582658381:  // interpolatedYieldCurveDefinitionSource
        return getInterpolatedYieldCurveDefinitionSource();
      case -461125123:  // interpolatedYieldCurveSpecificationBuilder
        return getInterpolatedYieldCurveSpecificationBuilder();
      case 1540542824:  // volatilityCubeDefinitionSource
        return getVolatilityCubeDefinitionSource();
      case 615188973:  // currencyMatrixSource
        return getCurrencyMatrixSource();
      case 431020691:  // holidaySource
        return getHolidaySource();
      case -467239906:  // exchangeSource
        return getExchangeSource();
      case 358729161:  // historicalTimeSeriesSource
        return getHistoricalTimeSeriesSource();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case 195157501:  // configSource
        setConfigSource((ConfigSource) newValue);
        return;
      case -702456965:  // securitySource
        setSecuritySource((SecuritySource) newValue);
        return;
      case -1655657820:  // positionSource
        setPositionSource((PositionSource) newValue);
        return;
      case -1636207569:  // regionSource
        setRegionSource((RegionSource) newValue);
        return;
      case -1281578674:  // conventionBundleSource
        setConventionBundleSource((ConventionBundleSource) newValue);
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
      case 615188973:  // currencyMatrixSource
        setCurrencyMatrixSource((CurrencyMatrixSource) newValue);
        return;
      case 431020691:  // holidaySource
        setHolidaySource((HolidaySource) newValue);
        return;
      case -467239906:  // exchangeSource
        setExchangeSource((ExchangeSource) newValue);
        return;
      case 358729161:  // historicalTimeSeriesSource
        setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_configSource, "configSource");
    JodaBeanUtils.notNull(_securitySource, "securitySource");
    JodaBeanUtils.notNull(_positionSource, "positionSource");
    JodaBeanUtils.notNull(_regionSource, "regionSource");
    JodaBeanUtils.notNull(_conventionBundleSource, "conventionBundleSource");
    JodaBeanUtils.notNull(_interpolatedYieldCurveDefinitionSource, "interpolatedYieldCurveDefinitionSource");
    JodaBeanUtils.notNull(_interpolatedYieldCurveSpecificationBuilder, "interpolatedYieldCurveSpecificationBuilder");
    JodaBeanUtils.notNull(_volatilityCubeDefinitionSource, "volatilityCubeDefinitionSource");
    JodaBeanUtils.notNull(_currencyMatrixSource, "currencyMatrixSource");
    JodaBeanUtils.notNull(_holidaySource, "holidaySource");
    JodaBeanUtils.notNull(_exchangeSource, "exchangeSource");
    JodaBeanUtils.notNull(_historicalTimeSeriesSource, "historicalTimeSeriesSource");
    super.validate();
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
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getRegionSource(), other.getRegionSource()) &&
          JodaBeanUtils.equal(getConventionBundleSource(), other.getConventionBundleSource()) &&
          JodaBeanUtils.equal(getInterpolatedYieldCurveDefinitionSource(), other.getInterpolatedYieldCurveDefinitionSource()) &&
          JodaBeanUtils.equal(getInterpolatedYieldCurveSpecificationBuilder(), other.getInterpolatedYieldCurveSpecificationBuilder()) &&
          JodaBeanUtils.equal(getVolatilityCubeDefinitionSource(), other.getVolatilityCubeDefinitionSource()) &&
          JodaBeanUtils.equal(getCurrencyMatrixSource(), other.getCurrencyMatrixSource()) &&
          JodaBeanUtils.equal(getHolidaySource(), other.getHolidaySource()) &&
          JodaBeanUtils.equal(getExchangeSource(), other.getExchangeSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionBundleSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterpolatedYieldCurveDefinitionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterpolatedYieldCurveSpecificationBuilder());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVolatilityCubeDefinitionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrencyMatrixSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidaySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
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
   * @return the value of the property, not null
   */
  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource() {
    return _interpolatedYieldCurveDefinitionSource;
  }

  /**
   * Sets the yield curve definition source.
   * @param interpolatedYieldCurveDefinitionSource  the new value of the property, not null
   */
  public void setInterpolatedYieldCurveDefinitionSource(InterpolatedYieldCurveDefinitionSource interpolatedYieldCurveDefinitionSource) {
    JodaBeanUtils.notNull(interpolatedYieldCurveDefinitionSource, "interpolatedYieldCurveDefinitionSource");
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
   * @return the value of the property, not null
   */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder() {
    return _interpolatedYieldCurveSpecificationBuilder;
  }

  /**
   * Sets the yield curve specification source.
   * @param interpolatedYieldCurveSpecificationBuilder  the new value of the property, not null
   */
  public void setInterpolatedYieldCurveSpecificationBuilder(InterpolatedYieldCurveSpecificationBuilder interpolatedYieldCurveSpecificationBuilder) {
    JodaBeanUtils.notNull(interpolatedYieldCurveSpecificationBuilder, "interpolatedYieldCurveSpecificationBuilder");
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
   * @return the value of the property, not null
   */
  public VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource() {
    return _volatilityCubeDefinitionSource;
  }

  /**
   * Sets the volitility cube source.
   * @param volatilityCubeDefinitionSource  the new value of the property, not null
   */
  public void setVolatilityCubeDefinitionSource(VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    JodaBeanUtils.notNull(volatilityCubeDefinitionSource, "volatilityCubeDefinitionSource");
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
   * Gets the currency matrix source.
   * @return the value of the property, not null
   */
  public CurrencyMatrixSource getCurrencyMatrixSource() {
    return _currencyMatrixSource;
  }

  /**
   * Sets the currency matrix source.
   * @param currencyMatrixSource  the new value of the property, not null
   */
  public void setCurrencyMatrixSource(CurrencyMatrixSource currencyMatrixSource) {
    JodaBeanUtils.notNull(currencyMatrixSource, "currencyMatrixSource");
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
     * The meta-property for the {@code regionSource} property.
     */
    private final MetaProperty<RegionSource> _regionSource = DirectMetaProperty.ofReadWrite(
        this, "regionSource", EngineContextsComponentFactory.class, RegionSource.class);
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
     * The meta-property for the {@code currencyMatrixSource} property.
     */
    private final MetaProperty<CurrencyMatrixSource> _currencyMatrixSource = DirectMetaProperty.ofReadWrite(
        this, "currencyMatrixSource", EngineContextsComponentFactory.class, CurrencyMatrixSource.class);
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
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "configSource",
        "securitySource",
        "positionSource",
        "regionSource",
        "conventionBundleSource",
        "interpolatedYieldCurveDefinitionSource",
        "interpolatedYieldCurveSpecificationBuilder",
        "volatilityCubeDefinitionSource",
        "currencyMatrixSource",
        "holidaySource",
        "exchangeSource",
        "historicalTimeSeriesSource");

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
        case -702456965:  // securitySource
          return _securitySource;
        case -1655657820:  // positionSource
          return _positionSource;
        case -1636207569:  // regionSource
          return _regionSource;
        case -1281578674:  // conventionBundleSource
          return _conventionBundleSource;
        case -582658381:  // interpolatedYieldCurveDefinitionSource
          return _interpolatedYieldCurveDefinitionSource;
        case -461125123:  // interpolatedYieldCurveSpecificationBuilder
          return _interpolatedYieldCurveSpecificationBuilder;
        case 1540542824:  // volatilityCubeDefinitionSource
          return _volatilityCubeDefinitionSource;
        case 615188973:  // currencyMatrixSource
          return _currencyMatrixSource;
        case 431020691:  // holidaySource
          return _holidaySource;
        case -467239906:  // exchangeSource
          return _exchangeSource;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
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
     * The meta-property for the {@code regionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionSource> regionSource() {
      return _regionSource;
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
     * The meta-property for the {@code currencyMatrixSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyMatrixSource> currencyMatrixSource() {
      return _currencyMatrixSource;
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

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
