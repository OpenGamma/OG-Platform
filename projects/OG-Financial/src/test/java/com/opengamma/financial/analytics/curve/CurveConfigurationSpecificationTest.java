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

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurveConfigurationSpecificationTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final CashSecurity cash = new CashSecurity(Currency.USD, ExternalId.of("Test", "US"),
        DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DayCountFactory.INSTANCE.getDayCount("30/360"), 0.01, 10000);
    cash.setUniqueId(UniqueId.of("Test", "Cash"));
    final ComputationTargetSpecification security = ComputationTargetSpecification.of(cash);
    final ComputationTargetSpecification country = ComputationTargetSpecification.of(UniqueId.of("Test", "US"));
    final ComputationTargetSpecification currency = ComputationTargetSpecification.of(Currency.USD);
    final Set<CurveConfigurationSpecification> unsorted = new LinkedHashSet<>();
    final CurveConfigurationSpecification countryConfig = new CurveConfigurationSpecification(country, 1);
    final CurveConfigurationSpecification currencyConfig = new CurveConfigurationSpecification(currency, 2);
    unsorted.add(countryConfig);
    unsorted.add(currencyConfig);
    final CurveConfigurationSpecification securityConfig = new CurveConfigurationSpecification(security, 0);
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
