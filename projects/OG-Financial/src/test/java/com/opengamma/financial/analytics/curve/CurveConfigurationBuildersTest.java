/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class CurveConfigurationBuildersTest extends AnalyticsTestBase {
  private static final String DISCOUNTING_NAME = "USD Discounting";
  private static final String DISCOUNTING_CODE = "USD";
  private static final DiscountingCurveTypeConfiguration DISCOUNTING_CONFIG = new DiscountingCurveTypeConfiguration(DISCOUNTING_NAME, DISCOUNTING_CODE);
  private static final String LIBOR_3M_NAME = "USD Forward3M";
  private static final String LIBOR_3M_CONVENTION_NAME = "USD 3m Libor";
  private static final String LIBOR_INDEX_TYPE = "Ibor";
  private static final IndexCurveTypeConfiguration LIBOR_3M_CONFIG = new IndexCurveTypeConfiguration(LIBOR_3M_NAME, LIBOR_3M_CONVENTION_NAME, LIBOR_INDEX_TYPE);
  private static final String LIBOR_6M_NAME = "USD Forward3M";
  private static final String LIBOR_6M_CONVENTION_NAME = "USD 6m Libor";
  private static final IndexCurveTypeConfiguration LIBOR_6M_CONFIG = new IndexCurveTypeConfiguration(LIBOR_6M_NAME, LIBOR_6M_CONVENTION_NAME, LIBOR_INDEX_TYPE);
  private static final String BOND_CURVE_NAME = "OG Bond Curve";
  private static final String BOND_ISSUER_NAME = "OG";
  private static final String BOND_CODE = "USD";
  private static final IssuerCurveTypeConfiguration ISSUER_CONFIG = new IssuerCurveTypeConfiguration(BOND_CURVE_NAME, BOND_ISSUER_NAME, BOND_CODE);
  private static final CurveGroupConfiguration GROUP1;
  private static final CurveGroupConfiguration GROUP2;
  private static final CurveGroupConfiguration GROUP3;
  private static final CurveConstructionConfiguration CONSTRUCTION;

  static {
    DISCOUNTING_CONFIG.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    LIBOR_3M_CONFIG.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "234"));
    ISSUER_CONFIG.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "345"));
    GROUP1 = new CurveGroupConfiguration(1, Arrays.asList(DISCOUNTING_CONFIG, LIBOR_3M_CONFIG));
    GROUP1.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "456"));
    GROUP2 = new CurveGroupConfiguration(2, Arrays.asList((CurveTypeConfiguration) LIBOR_6M_CONFIG));
    GROUP2.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "567"));
    GROUP3 = new CurveGroupConfiguration(3, Arrays.asList((CurveTypeConfiguration) ISSUER_CONFIG));
    CONSTRUCTION = new CurveConstructionConfiguration(Arrays.asList(GROUP1, GROUP2, GROUP3), null);
    CONSTRUCTION.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "678"));
  }

  @Test
  public void testDiscountingCurveTypeConfiguration() {
    assertEquals(DISCOUNTING_CONFIG, cycleObject(DiscountingCurveTypeConfiguration.class, DISCOUNTING_CONFIG));
  }

  @Test
  public void testIndexCurveTypeConfiguration() {
    assertEquals(LIBOR_3M_CONFIG, cycleObject(IndexCurveTypeConfiguration.class, LIBOR_3M_CONFIG));
  }

  @Test
  public void testIssuerCurveTypeConfiguration() {
    assertEquals(ISSUER_CONFIG, cycleObject(IssuerCurveTypeConfiguration.class, ISSUER_CONFIG));
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
    final CurveConstructionConfiguration construction = new CurveConstructionConfiguration(Arrays.asList(GROUP1, GROUP2, GROUP3), exogenousConfigs);
    construction.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "789"));
    assertEquals(construction, cycleObject(CurveConstructionConfiguration.class, construction));
  }
}
