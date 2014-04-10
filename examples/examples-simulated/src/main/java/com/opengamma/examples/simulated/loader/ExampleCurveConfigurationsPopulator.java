/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.Arrays;
import java.util.Collections;
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
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Creates curve construction configurations, interpolated curve definitions and 
 * curve node id mappers for a two-curve configurations. Currently, only USD curves
 * are created.
 */
public class ExampleCurveConfigurationsPopulator {
  /** Zero period */
  private static final Tenor ZERO = Tenor.of(Period.ZERO);
  /** The curve construction configuration prefix */
  private static final String CURVE_CONSTRUCTION_CONFIG_PREFIX = "Default ";
  /** The curve construction configuration suffix */
  private static final String CURVE_CONSTRUCTION_CONFIG_SUFFIX = " Curves";
  /** The discounting curve name suffix */
  private static final String DISCOUNTING_CURVE_NAME_SUFFIX = " Discounting";
  /** The ibor curve name suffix */
  private static final String IBOR_CURVE_NAME_SUFFIX = " Forward Ibor";
  /** The curve node id mapper suffix */
  private static final String NODE_MAPPER_SUFFIX = " Node Ids";
  /** A map of (currency string, tenor) pairs to ibor security ids */
  private static final Map<Pair<String, Tenor>, ExternalId> IBOR_SECURITY_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to deposit convention ids */
  private static final Map<String, ExternalId> DEPOSIT_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to overnight convention ids */
  private static final Map<String, ExternalId> OVERNIGHT_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to fixed leg convention ids */
  private static final Map<String, ExternalId> FIXED_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of (currency string, tenor) pairs to ibor leg convention ids */
  private static final Map<Pair<String, Tenor>, ExternalId> IBOR_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to OIS leg convention ids */
  private static final Map<String, ExternalId> FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to OIS leg convention ids */
  private static final Map<String, ExternalId> OIS_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to ibor tenors */
  private static final Map<String, Tenor> IBOR_TENOR_FOR_CURRENCY = new HashMap<>();

  static {
    IBOR_SECURITY_FOR_CURRENCY.put(Pairs.of("USD", Tenor.THREE_MONTHS), ExternalSchemes.syntheticSecurityId("USDLIBORP3M"));
    DEPOSIT_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD Deposit"));
    OVERNIGHT_CONVENTION_FOR_CURRENCY.put("USD", ExternalSchemes.syntheticSecurityId("USDFF"));
    FIXED_LEG_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD IRS Fixed Leg"));
    IBOR_LEG_CONVENTION_FOR_CURRENCY.put(Pairs.of("USD", Tenor.THREE_MONTHS), ExternalId.of("CONVENTION", "USD 3M IRS Ibor Leg"));
    FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD OIS Fixed Leg"));
    OIS_LEG_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD OIS Overnight Leg"));
    IBOR_TENOR_FOR_CURRENCY.put("USD", Tenor.THREE_MONTHS);
  }

  /**
   * Populates a config master with curve configurations, curve definitions
   * and curve node id mappers.
   * @param configMaster The config master, not null
   */
  public static void populateConfigAndConventionMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    for (final String currency : new String[] {"USD" }) {
      final Tenor tenor = IBOR_TENOR_FOR_CURRENCY.get(currency);
      ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveConstructionConfigurationForCurrency(currency, tenor)));
      final List<CurveNodeIdMapper> curveNodeIdMappers = makeCurveNodeIdMappersForCurrency(currency, tenor);
      for (final CurveNodeIdMapper curveNodeIdMapper : curveNodeIdMappers) {
        ConfigMasterUtils.storeByName(configMaster, makeConfig(curveNodeIdMapper));
      }
      final List<CurveDefinition> curveDefinitions = makeCurveDefinitionsForCurrency(currency, tenor);
      for (final CurveDefinition curveDefinition : curveDefinitions) {
        ConfigMasterUtils.storeByName(configMaster, makeConfig(curveDefinition));
      }
    }
  }

  /**
   * Creates a curve configuration for a currency and tenor. The configuration
   * contains one discounting curve type referencing a curve called "[CURRENCY_STRING] Discounting",
   * an overnight curve type referencing the discounting curve and one ibor curve type 
   * referencing a curve called "[CURRENCY_STRING] [TENOR] Forward Ibor", and is called 
   * "Default [CURRENCY_STRING] Curves". 
   * @param currency The currency
   * @return The configuration
   */
  private static CurveConstructionConfiguration makeCurveConstructionConfigurationForCurrency(final String currency, final Tenor tenor) {
    final String name = CURVE_CONSTRUCTION_CONFIG_PREFIX + currency + CURVE_CONSTRUCTION_CONFIG_SUFFIX;
    final String discountingCurveName = currency + DISCOUNTING_CURVE_NAME_SUFFIX;
    final String forwardIborCurveName = currency + " " + tenor.toFormattedString().substring(1) + IBOR_CURVE_NAME_SUFFIX;
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration(currency);
    final OvernightCurveTypeConfiguration overnightCurveType = new OvernightCurveTypeConfiguration(OVERNIGHT_CONVENTION_FOR_CURRENCY.get(currency));
    final IborCurveTypeConfiguration forwardIborCurveType = new IborCurveTypeConfiguration(IBOR_SECURITY_FOR_CURRENCY.get(Pairs.of(currency, tenor)), tenor);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(discountingCurveName, Arrays.asList(discountingCurveType, overnightCurveType));
    curveTypes.put(forwardIborCurveName, Arrays.asList(forwardIborCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    return new CurveConstructionConfiguration(name, groups, Collections.<String>emptyList());
  }

  /**
   * Creates a discounting and forward ibor interpolated curve definition for a currency, tenor pair.
   * The discounting curve contains a two-day deposit node and (1m, 2m, 3m, 4m, 5m, 6m, 9m, 1y, 2y, 
   * 3y, 4y, 5y, 10y) OIS nodes. The forward ibor curve contains an ibor node of the appropriate tenor
   * and ((1y, 2y, 3y, 4y, 5y, 6y, 7y, 8y, 9y, 10y, 15y, 20y, 25y, 30y) fixed / ibor swap nodes.
   * The interpolator used is double quadratic with linear extrapolation on both ends.
   * @param currency The currency
   * @param tenor The ibor tenor
   * @return A discounting and forward ibor curve definition
   */
  private static List<CurveDefinition> makeCurveDefinitionsForCurrency(final String currency, final Tenor tenor) {
    final String discountingCurveName = currency + DISCOUNTING_CURVE_NAME_SUFFIX;
    final Set<CurveNode> discountingCurveNodes = new LinkedHashSet<>();
    final String discountingCurveNodeIdMapperName = currency + NODE_MAPPER_SUFFIX;
    discountingCurveNodes.add(new CashNode(ZERO, Tenor.TWO_DAYS, DEPOSIT_CONVENTION_FOR_CURRENCY.get(currency),
        discountingCurveNodeIdMapperName));
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 9 }) {
      discountingCurveNodes.add(new SwapNode(ZERO, Tenor.ofMonths(i), FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency),
          OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency), discountingCurveNodeIdMapperName));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 10 }) {
      discountingCurveNodes.add(new SwapNode(ZERO, Tenor.ofYears(i), FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency),
          OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency), discountingCurveNodeIdMapperName));
    }
    final CurveDefinition discountingCurveDefinition = new InterpolatedCurveDefinition(discountingCurveName, discountingCurveNodes,
        Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final String forwardIborCurveName = currency + " " + tenor.toFormattedString().substring(1) + IBOR_CURVE_NAME_SUFFIX;
    final Set<CurveNode> iborCurveNodes = new LinkedHashSet<>();
    final String iborCurveNodeIdMapperName = currency + " " + tenor.toFormattedString().substring(1) + NODE_MAPPER_SUFFIX;
    final Pair<String, Tenor> currencyTenorPair = Pairs.of(currency, tenor);
    iborCurveNodes.add(new CashNode(ZERO, tenor, IBOR_SECURITY_FOR_CURRENCY.get(currencyTenorPair), iborCurveNodeIdMapperName));
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30 }) {
      iborCurveNodes.add(new SwapNode(ZERO, Tenor.ofYears(i), FIXED_LEG_CONVENTION_FOR_CURRENCY.get(currency),
          IBOR_LEG_CONVENTION_FOR_CURRENCY.get(currencyTenorPair), iborCurveNodeIdMapperName));
    }
    final CurveDefinition iborCurveDefinition = new InterpolatedCurveDefinition(forwardIborCurveName, iborCurveNodes, Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    return Arrays.asList(discountingCurveDefinition, iborCurveDefinition);
  }

  /**
   * Creates two curve node id mappers; one containing deposit and OIS tickers, the other
   * containing ibor and swap tickers. 
   * @param currency The currency
   * @param tenor The ibor tenor
   * @return Curve node id mappers
   */
  private static List<CurveNodeIdMapper> makeCurveNodeIdMappersForCurrency(final String currency, final Tenor tenor) {
    final String discountingCurveNodeIdMapperName = currency + NODE_MAPPER_SUFFIX;
    final Map<Tenor, CurveInstrumentProvider> depositNodes = new HashMap<>();
    depositNodes.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "CASHP2D"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    final Map<Tenor, CurveInstrumentProvider> oisNodes = new HashMap<>();
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 9 }) {
      final Tenor nodeTenor = Tenor.ofMonths(i);
      oisNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "OIS_SWAP" + nodeTenor.toFormattedString())));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 10 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      oisNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "OIS_SWAP" + nodeTenor.toFormattedString())));
    }
    final CurveNodeIdMapper discountingCurveNodeIdMapper = CurveNodeIdMapper.builder()
        .name(discountingCurveNodeIdMapperName)
        .cashNodeIds(depositNodes)
        .swapNodeIds(oisNodes)
        .build();
    final String iborCurveNodeIdMapperName = currency + " " + tenor.toFormattedString().substring(1) + NODE_MAPPER_SUFFIX;
    final Map<Tenor, CurveInstrumentProvider> iborNodes = new HashMap<>();
    iborNodes.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "LIBOR" + tenor.toFormattedString()),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    final Map<Tenor, CurveInstrumentProvider> swapNodes = new HashMap<>();
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      swapNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "SWAP" + nodeTenor.toFormattedString())));
    }
    final CurveNodeIdMapper iborCurveNodeIdMapper = CurveNodeIdMapper.builder()
        .name(iborCurveNodeIdMapperName)
        .cashNodeIds(iborNodes)
        .swapNodeIds(swapNodes)
        .build();
    return Arrays.asList(discountingCurveNodeIdMapper, iborCurveNodeIdMapper);
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
