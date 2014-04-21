/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveSelectorFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void roundTrip() {
    Set<String> names = Sets.newHashSet("name1", "name2");
    Set<Currency> currencies = Sets.newHashSet(Currency.AUD, Currency.CAD);
    HashSet<String> calcConfigNames = Sets.newHashSet("ccn1", "ccn2");
    YieldCurveSelector selector =
        new YieldCurveSelector(calcConfigNames, names, currencies, Pattern.compile("\\d.*"), Pattern.compile("\\w.*"));
    assertEncodeDecodeCycle(YieldCurveSelector.class, selector);
  }

}
