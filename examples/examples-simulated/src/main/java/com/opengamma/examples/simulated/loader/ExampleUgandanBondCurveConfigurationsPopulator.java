/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
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
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Creates a curve construction configuration, interpolated curve definition and curve node id mapper
 * for Ugandan bonds to be used for pricing the example Ugandan TRS portfolio. The ISINs used 
 * follow the form "UG0000000XXX" where XX is equal to the number of months until bond maturity. 
 * The bond curve contains only bond nodes from 6m to 10y in 6m increments and uses the bond yield
 * to construct the curve.
 */
public class ExampleUgandanBondCurveConfigurationsPopulator {
  private static final Tenor ZERO = Tenor.of(Period.ZERO);
  private static final String CURVE_CONSTRUCTION_CONFIG_NAME = "UG Government Bond Configuration";
  private static final String CURVE_NAME = "UG Government Bond";
  private static final String CURVE_NODE_ID_MAPPER_NAME = "UG Government Bond ISIN";
  private static final Currency CURRENCY = Currency.of("UGX");

  /**
   * Populates a config master with curve configurations, curve definitions
   * and curve node id mappers.
   * @param configMaster The config master, not null
   */
  public static void populateConfigAndConventionMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveConstructionConfiguration()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveNodeIdMapper()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveDefinition()));
  }

  /**
   * @return The configuration
   */
  private static CurveConstructionConfiguration makeCurveConstructionConfiguration() {
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration(CURRENCY.getCode());
    final Set<Object> keys = Sets.<Object>newHashSet("UGANDA");
    final Set<LegalEntityFilter<LegalEntity>> filters = new HashSet<>();
    filters.add(new LegalEntityShortName());
    final IssuerCurveTypeConfiguration issuerCurveType = new IssuerCurveTypeConfiguration(keys, filters);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(CURVE_NAME, Arrays.asList(discountingCurveType, issuerCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    return new CurveConstructionConfiguration(CURVE_CONSTRUCTION_CONFIG_NAME, groups, Collections.<String>emptyList());
  }

  private static CurveDefinition makeCurveDefinition() {
    final Set<CurveNode> curveNodes = new LinkedHashSet<>();
    for (int i = 0; i < 20; i++) {
      final int months = (int) ((i + 1) / 2. * 12);
      curveNodes.add(new BondNode(Tenor.ofMonths(months), CURVE_NODE_ID_MAPPER_NAME));
    }
    final CurveDefinition curveDefinition = new InterpolatedCurveDefinition(CURVE_NAME, curveNodes,
        Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    return curveDefinition;
  }

  private static CurveNodeIdMapper makeCurveNodeIdMapper() {
    final Map<Tenor, CurveInstrumentProvider> bondNodes = new HashMap<>();
    for (int i = 0; i < 20; i++) {
      final int months = (int) ((i + 1) / 2. * 12);
      final Tenor tenor = Tenor.ofMonths(months);
      String suffix;
      if (months < 10) {
        suffix = "00" + Integer.toString(months);
      } else if (months < 100) {
        suffix = "0" + Integer.toString(months);
      } else {
        suffix = Integer.toString(months);
      }
      final ExternalId isin = ExternalSchemes.isinSecurityId("UG0000000" + suffix);
      final CurveInstrumentProvider instrumentProvider = new StaticCurveInstrumentProvider(isin, MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT);
      bondNodes.put(tenor, instrumentProvider);
    }
    final CurveNodeIdMapper curveNodeIdMapper = CurveNodeIdMapper.builder()
        .name(CURVE_NODE_ID_MAPPER_NAME)
        .bondNodeIds(bondNodes)
        .build();
    return curveNodeIdMapper;
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
