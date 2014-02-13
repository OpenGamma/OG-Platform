/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.financial.legalentity.LegalEntitySector;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CurveConfigurationBuildersTest extends AnalyticsTestBase {
  private static final String DISCOUNTING_NAME = "USD Discounting";
  private static final String DISCOUNTING_CODE = "USD";
  private static final DiscountingCurveTypeConfiguration DISCOUNTING_CONFIG = new DiscountingCurveTypeConfiguration(DISCOUNTING_CODE);
  private static final String LIBOR_3M_NAME = "USD Forward3M";
  private static final ExternalId LIBOR_3M_CONVENTION = ExternalId.of("Test", "USD 3m Libor");
  private static final IborCurveTypeConfiguration LIBOR_3M_CONFIG = new IborCurveTypeConfiguration(LIBOR_3M_CONVENTION, Tenor.THREE_MONTHS);
  private static final String LIBOR_6M_NAME = "USD Forward3M";
  private static final ExternalId LIBOR_6M_CONVENTION = ExternalId.of("Test", "USD 6m Libor");
  private static final IborCurveTypeConfiguration LIBOR_6M_CONFIG = new IborCurveTypeConfiguration(LIBOR_6M_CONVENTION, Tenor.SIX_MONTHS);
  private static final String BOND_CURVE_NAME = "OG Bond Curve";
  private static final String BOND_ISSUER_NAME = "OG";
  private static final String BOND_CODE = "USD";
  private static final IssuerCurveTypeConfiguration DEPRECATED_ISSUER_CONFIG = new IssuerCurveTypeConfiguration(BOND_ISSUER_NAME, BOND_CODE);
  private static final IssuerCurveTypeConfiguration ISSUER_CONFIG;
  private static final ExternalId OVERNIGHT_CONVENTION = ExternalId.of("Test", "USD Overnight");
  private static final OvernightCurveTypeConfiguration OVERNIGHT_CONFIG = new OvernightCurveTypeConfiguration(OVERNIGHT_CONVENTION);
  private static final InflationCurveTypeConfiguration INFLATION_CONFIG = new InflationCurveTypeConfiguration("US", ExternalId.of("Test", "USCPI"));
  private static final CurveGroupConfiguration GROUP1;
  private static final CurveGroupConfiguration GROUP2;
  private static final CurveGroupConfiguration GROUP3;
  private static final CurveConstructionConfiguration CONSTRUCTION;

  static {
    final Set<Object> keys = new HashSet<>();
    keys.add("INDUSTRY");
    keys.add("North America");
    keys.add(Country.US);
    keys.add(Currency.USD);
    final Set<LegalEntityFilter<LegalEntity>> filters = new HashSet<>();
    filters.add(new LegalEntitySector(true, false, Collections.<String>emptySet()));
    filters.add(new LegalEntityRegion(true, true, Collections.singleton(Country.US), true, Collections.singleton(Currency.USD)));
    ISSUER_CONFIG = new IssuerCurveTypeConfiguration(keys, filters);
    final Map<String, List<? extends CurveTypeConfiguration>> group1Map = new HashMap<>();
    group1Map.put(DISCOUNTING_NAME, Arrays.asList(DISCOUNTING_CONFIG, OVERNIGHT_CONFIG));
    GROUP1 = new CurveGroupConfiguration(1, group1Map);
    final Map<String, List<? extends CurveTypeConfiguration>> group2Map = new HashMap<>();
    group2Map.put(LIBOR_3M_NAME, Arrays.asList(LIBOR_3M_CONFIG));
    group2Map.put(LIBOR_6M_NAME, Arrays.asList(LIBOR_6M_CONFIG));
    GROUP2 = new CurveGroupConfiguration(2, group2Map);
    final Map<String, List<? extends CurveTypeConfiguration>> group3Map = new HashMap<>();
    group3Map.put(BOND_CURVE_NAME, Arrays.asList(ISSUER_CONFIG));
    GROUP3 = new CurveGroupConfiguration(3, group3Map);
    CONSTRUCTION = new CurveConstructionConfiguration("Config", Arrays.asList(GROUP1, GROUP2, GROUP3), ImmutableList.<String>of());
    CONSTRUCTION.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "678"));
  }

  @Test
  public void testDiscountingCurveTypeConfiguration() {
    assertEquals(DISCOUNTING_CONFIG, cycleObject(DiscountingCurveTypeConfiguration.class, DISCOUNTING_CONFIG));
  }

  @Test
  public void testIborCurveTypeConfiguration() {
    assertEquals(LIBOR_3M_CONFIG, cycleObject(IborCurveTypeConfiguration.class, LIBOR_3M_CONFIG));
  }

  @Test
  public void testOvernightCurveTypeConfiguration() {
    assertEquals(OVERNIGHT_CONFIG, cycleObject(OvernightCurveTypeConfiguration.class, OVERNIGHT_CONFIG));
  }

  @Test
  public void testIssuerCurveTypeConfiguration() {
    final Set<Object> keys = Sets.<Object>newHashSet("OG", Currency.USD);
    final Set<LegalEntityFilter<LegalEntity>> filterSet = new HashSet<>();
    filterSet.add(new LegalEntityRegion(false, false, Collections.<Country>emptySet(), true, Collections.singleton(Currency.USD)));
    filterSet.add(new LegalEntityShortName());
    assertEquals(new IssuerCurveTypeConfiguration(keys, filterSet), cycleObject(IssuerCurveTypeConfiguration.class, DEPRECATED_ISSUER_CONFIG));
    assertEquals(ISSUER_CONFIG, cycleObject(IssuerCurveTypeConfiguration.class, ISSUER_CONFIG));
  }

  @Test
  public void testInflationCurveTypeConfiguration() {
    assertEquals(INFLATION_CONFIG, cycleObject(InflationCurveTypeConfiguration.class, INFLATION_CONFIG));
  }

  @Test
  public void testCurveGroupConfiguration() {
    assertEquals(GROUP1, cycleObject(CurveGroupConfiguration.class, GROUP1));
    assertEquals(GROUP2, cycleObject(CurveGroupConfiguration.class, GROUP2));
    assertEquals(GROUP3, cycleObject(CurveGroupConfiguration.class, GROUP3));
  }

  @Test
  public void testCurveConstructionConfiguration() {
    assertEquals(CONSTRUCTION, cycleObject(CurveConstructionConfiguration.class, CONSTRUCTION));
    final List<String> exogenousConfigs = Arrays.asList("Config1", "Config2");
    final CurveConstructionConfiguration construction = new CurveConstructionConfiguration("Config", Arrays.asList(GROUP1, GROUP2, GROUP3), exogenousConfigs);
    construction.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "789"));
    assertEquals(construction, cycleObject(CurveConstructionConfiguration.class, construction));
  }
}
