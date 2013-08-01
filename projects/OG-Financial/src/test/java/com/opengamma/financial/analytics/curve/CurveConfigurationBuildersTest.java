/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;

/**
 *
 */
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
  private static final IssuerCurveTypeConfiguration ISSUER_CONFIG = new IssuerCurveTypeConfiguration(BOND_ISSUER_NAME, BOND_CODE);
  private static final ExternalId OVERNIGHT_CONVENTION = ExternalId.of("Test", "USD Overnight");
  private static final OvernightCurveTypeConfiguration OVERNIGHT_CONFIG = new OvernightCurveTypeConfiguration(OVERNIGHT_CONVENTION);
  private static final InflationCurveTypeConfiguration INFLATION_CONFIG = new InflationCurveTypeConfiguration("US", ExternalId.of("Test", "USCPI"));
  private static final CurveGroupConfiguration GROUP1;
  private static final CurveGroupConfiguration GROUP2;
  private static final CurveGroupConfiguration GROUP3;
  private static final CurveConstructionConfiguration CONSTRUCTION;

  static {
    final Map<String, List<CurveTypeConfiguration>> group1Map = new HashMap<>();
    group1Map.put(DISCOUNTING_NAME, Arrays.asList(DISCOUNTING_CONFIG, OVERNIGHT_CONFIG));
    GROUP1 = new CurveGroupConfiguration(1, group1Map);
    final Map<String, List<CurveTypeConfiguration>> group2Map = new HashMap<>();
    group2Map.put(LIBOR_3M_NAME, Arrays.asList((CurveTypeConfiguration) LIBOR_3M_CONFIG));
    group2Map.put(LIBOR_6M_NAME, Arrays.asList((CurveTypeConfiguration) LIBOR_6M_CONFIG));
    GROUP2 = new CurveGroupConfiguration(2, group2Map);
    final Map<String, List<CurveTypeConfiguration>> group3Map = new HashMap<>();
    group3Map.put(BOND_CURVE_NAME, Arrays.asList((CurveTypeConfiguration) ISSUER_CONFIG));
    GROUP3 = new CurveGroupConfiguration(3, group3Map);
    CONSTRUCTION = new CurveConstructionConfiguration("Config", Arrays.asList(GROUP1, GROUP2, GROUP3), null);
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
