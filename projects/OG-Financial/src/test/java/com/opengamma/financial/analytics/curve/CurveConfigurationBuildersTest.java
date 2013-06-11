/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class CurveConfigurationBuildersTest extends AnalyticsTestBase {

  @Test
  public void testDiscountingCurveTypeConfiguration() {
    final String name = "USD Discounting";
    final String code = "USD";
    final DiscountingCurveTypeConfiguration config = new DiscountingCurveTypeConfiguration(name, code);
    config.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    assertEquals(config, cycleObject(DiscountingCurveTypeConfiguration.class, config));
  }

  @Test
  public void testIndexCurveTypeConfiguration() {
    final String name = "USD Forward3M";
    final String conventionName = "USD 3m Libor";
    final String indexType = "Ibor";
    final IndexCurveTypeConfiguration config = new IndexCurveTypeConfiguration(name, conventionName, indexType);
    config.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "234"));
    assertEquals(config, cycleObject(IndexCurveTypeConfiguration.class, config));
  }

  @Test
  public void testIssuerCurveTypeConfiguration() {
    final String name = "OG Bond Curve";
    final String issuerName = "OG";
    final String underlyingCode = "USD";
    final IssuerCurveTypeConfiguration config = new IssuerCurveTypeConfiguration(name, issuerName, underlyingCode);
    config.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "345"));
    assertEquals(config, cycleObject(IssuerCurveTypeConfiguration.class, config));
  }

}
