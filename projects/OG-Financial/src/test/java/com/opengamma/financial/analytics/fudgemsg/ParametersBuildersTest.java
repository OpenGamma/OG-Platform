/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.parameters.G2ppParameters;
import com.opengamma.financial.analytics.parameters.HullWhiteOneFactorParameters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ParametersBuildersTest extends AnalyticsTestBase {

  @Test
  public void testHullWhite() {
    final Currency currency = Currency.AUD;
    final ExternalId meanReversionId = ExternalId.of("Test", "MR");
    final ExternalId initialVolatilityId = ExternalId.of("Test", "IV");
    final SortedMap<Tenor, ExternalId> volatilityParameterIds = new TreeMap<>();
    for (int i = 1; i < 10; i++) {
      volatilityParameterIds.put(Tenor.ofMonths(i), ExternalId.of("Test", "V" + i));
    }
    final HullWhiteOneFactorParameters object = new HullWhiteOneFactorParameters(currency, meanReversionId, initialVolatilityId, volatilityParameterIds);
    object.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(object, cycleObject(HullWhiteOneFactorParameters.class, object));
  }

  @Test
  public void testG2pp() {
    final Currency currency = Currency.AUD;
    final ExternalId firstMeanReversionId = ExternalId.of("Test", "MR1");
    final ExternalId secondMeanReversionId = ExternalId.of("Test", "MR2");
    final ExternalId firstInitialVolatilityId = ExternalId.of("Test", "IV1");
    final ExternalId secondInitialVolatilityId = ExternalId.of("Test", "IV2");
    final SortedMap<Tenor, Pair<ExternalId, ExternalId>> volatilityParameterIds = new TreeMap<>();
    for (int i = 1; i < 10; i++) {
      volatilityParameterIds.put(Tenor.ofMonths(i), Pairs.of(ExternalId.of("Test", "1V" + i), ExternalId.of("Test", "2V" + i)));
    }
    final ExternalId correlationId = ExternalId.of("Test", "rho");
    final G2ppParameters object = new G2ppParameters(currency, firstMeanReversionId, secondMeanReversionId, firstInitialVolatilityId,
        secondInitialVolatilityId, volatilityParameterIds, correlationId);
    object.setUniqueId(UniqueId.of("Test", "123"));
    assertEquals(object, cycleObject(G2ppParameters.class, object));
  }
}
