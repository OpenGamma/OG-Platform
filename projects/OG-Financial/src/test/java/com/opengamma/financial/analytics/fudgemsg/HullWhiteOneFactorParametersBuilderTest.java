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

import com.opengamma.financial.analytics.parameters.HullWhiteOneFactorParameters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class HullWhiteOneFactorParametersBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
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
}
