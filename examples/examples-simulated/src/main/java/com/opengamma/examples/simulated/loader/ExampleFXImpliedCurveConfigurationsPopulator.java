/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Populates the config master with example FX implied curve configurations,
 * curve definitions and curve node id mappers.
 */
public class ExampleFXImpliedCurveConfigurationsPopulator {
  /** The suffix for curve node id mapper names */
  private static final String CURVE_NODE_ID_MAPPER_SUFFIX = " / USD";
  /** The suffix for curve names */
  private static final String CURVE_NAME_SUFFIX = "FX";
  /** The suffix for curve constructions configurations */
  private static final String CURVE_CONSTRUCTION_CONFIG_SUFFIX = " FX Implied Curve";
  /** The FX forward ticker suffix */
  private static final String TICKER_SUFFIX = "FXFORWARD";
  /** The FX forward node tenors */
  private static final Tenor[] TENORS = new Tenor[] {Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.THREE_WEEKS,
    Tenor.ONE_MONTH, Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS,
    Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.SIX_YEARS, Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS,
    Tenor.NINE_YEARS, Tenor.TEN_YEARS };
  /** The FX forward node tenor strings */
  private static final String[] TENOR_STRINGS = new String[] {"1W", "2W", "3W", "1M", "3M",
    "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y" };
  /** The exogenous USD curve configuration */
  private static final List<String> EXOGENOUS_CONFIGURATION = Arrays.asList("Default USD Curves");
  /** The FX forward convention id */
  private static final ExternalId CONVENTION_ID = ExternalId.of("CONVENTION", "FX Forward");

  /**
   * Populates a config master with FX implied curve configurations, curve definitions
   * and curve node id mappers.
   * @param configMaster The config master, not null
   */
  public static void populateConfigAndConventionMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    for (final String currency : new String[] {"EUR", "CHF", "JPY", "GBP" }) {
      ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveConstructionConfigurationForCurrency(currency)));
      ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveNodeIdMapperForCurrency(currency)));
      ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveDefinitionForCurrency(currency)));
    }
  }

  /**
   * Creates an FX implied curve configuration for a currency. The configuration
   * contains one discounting curve type referencing a curve called "[CURRENCY_STRING]FX" and
   * is called "[CURRENCY_STRING] FX Implied Curve". The exogenous curve construction 
   * configuration is called "Default USD Curves" and is populated in 
   * {@link ExampleCurveConfigurationsPopulator}. 
   * @param currency The currency
   * @return The configuration
   */
  private static CurveConstructionConfiguration makeCurveConstructionConfigurationForCurrency(final String currency) {
    final String name = currency + CURVE_CONSTRUCTION_CONFIG_SUFFIX;
    final String curveName = currency + CURVE_NAME_SUFFIX;
    final DiscountingCurveTypeConfiguration curveType = new DiscountingCurveTypeConfiguration(currency);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(curveName, Arrays.asList(curveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    return new CurveConstructionConfiguration(name, groups, EXOGENOUS_CONFIGURATION);
  }

  /**
   * Creates a curve node id mapper for FX forwards for a currency / USD pair. 
   * @param currency The currency
   * @return The curve node id mapper
   */
  private static CurveNodeIdMapper makeCurveNodeIdMapperForCurrency(final String currency) {
    final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds = new HashMap<>();
    for (int i = 0; i < TENORS.length; i++) {
      final ExternalId id = ExternalSchemes.syntheticSecurityId(currency + "USD" + TENOR_STRINGS[i] + TICKER_SUFFIX);
      ExternalId underlyingId;
      if (currency.equals("EUR") || currency.equals("CHF") || currency.equals("GBP")) {
        underlyingId = ExternalSchemes.syntheticSecurityId(currency + "USD");
      } else {
        underlyingId = ExternalSchemes.syntheticSecurityId("USD" + currency);
      }
      final StaticCurveInstrumentProvider curveInstrumentProvider = new StaticCurvePointsInstrumentProvider(id, MarketDataRequirementNames.MARKET_VALUE,
          DataFieldType.POINTS, underlyingId, MarketDataRequirementNames.MARKET_VALUE);
      fxForwardNodeIds.put(TENORS[i], curveInstrumentProvider);
    }
    return CurveNodeIdMapper.builder().name(currency + CURVE_NODE_ID_MAPPER_SUFFIX).fxForwardNodeIds(fxForwardNodeIds).build();
  }

  /**
   * Creates a curve definition containing only FX forwards for a currency / USD pair.
   * @param currency The currency
   * @return The curve definition
   */
  private static CurveDefinition makeCurveDefinitionForCurrency(final String currency) {
    final String curveName = currency + CURVE_NAME_SUFFIX;
    final Set<CurveNode> nodes = new LinkedHashSet<>();
    final String curveNodeIdMapper = currency + CURVE_NODE_ID_MAPPER_SUFFIX;
    for (final Tenor tenor : TENORS) {
      final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), tenor, CONVENTION_ID, Currency.of(currency),
          Currency.USD, curveNodeIdMapper);
      nodes.add(node);
    }
    return new InterpolatedCurveDefinition(curveName, nodes, Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  }

  /**
   * Creates a config item from a curve construction configuration object.
   * @param curveConfig The curve construction configuration
   * @return The config item
   */
  private static ConfigItem<CurveConstructionConfiguration> makeConfig(final CurveConstructionConfiguration curveConfig) {
    final ConfigItem<CurveConstructionConfiguration> config = ConfigItem.of(curveConfig);
    config.setName(curveConfig.getName());
    return config;
  }

  /**
   * Creates a config item from a curve node id mapper object.
   * @param curveNodeIdMapper The curve node id mapper
   * @return The config item
   */
  private static ConfigItem<CurveNodeIdMapper> makeConfig(final CurveNodeIdMapper curveNodeIdMapper) {
    final ConfigItem<CurveNodeIdMapper> config = ConfigItem.of(curveNodeIdMapper);
    config.setName(curveNodeIdMapper.getName());
    return config;
  }

  /**
   * Creates a config item from a curve definition object.
   * @param curveDefinition The curve definition
   * @return The config item
   */
  private static ConfigItem<CurveDefinition> makeConfig(final CurveDefinition curveDefinition) {
    final ConfigItem<CurveDefinition> config = ConfigItem.of(curveDefinition);
    config.setName(curveDefinition.getName());
    return config;
  }

}
