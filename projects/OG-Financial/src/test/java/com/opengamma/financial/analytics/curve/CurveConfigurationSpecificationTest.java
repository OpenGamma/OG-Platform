/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurveConfigurationSpecificationTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final ExternalId security = ExternalId.of("Test", "Cash");
    final ExternalId country = ExternalId.of("Test", "US");
    final ExternalId currency = ExternalId.of(Currency.OBJECT_SCHEME, "USD");
    final CurveConfigurationSpecification countryConfig = new CurveConfigurationSpecification(country, 1);
    final CurveConfigurationSpecification currencyConfig = new CurveConfigurationSpecification(currency, 2);
    final CurveConfigurationSpecification securityConfig = new CurveConfigurationSpecification(security, 0);
    final Set<CurveConfigurationSpecification> unsorted = new LinkedHashSet<>();
    unsorted.add(countryConfig);
    unsorted.add(currencyConfig);
    unsorted.add(securityConfig);
    final Set<CurveConfigurationSpecification> sorted = new TreeSet<>(new CurveConfigurationSpecificationComparator());
    sorted.addAll(unsorted);
    final Iterator<CurveConfigurationSpecification> iterator = sorted.iterator();
    assertEquals(3, sorted.size());
    assertEquals(securityConfig, iterator.next());
    assertEquals(countryConfig, iterator.next());
    assertEquals(currencyConfig, iterator.next());
    assertEquals(securityConfig, cycleObject(CurveConfigurationSpecification.class, securityConfig));
    assertEquals(countryConfig, cycleObject(CurveConfigurationSpecification.class, countryConfig));
    assertEquals(currencyConfig, cycleObject(CurveConfigurationSpecification.class, currencyConfig));
  }
}
