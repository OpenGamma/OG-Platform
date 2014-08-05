/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ReferenceAmountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void testReferenceAmountEncoding() {
    ReferenceAmount<Pair<String, Currency>> amount = new ReferenceAmount<>();
    amount.add(Pairs.of("a", Currency.of("EUR")), 1d);
    amount.add(Pairs.of("b", Currency.of("GBP")), 2d);
    assertEncodeDecodeCycle(ReferenceAmount.class, amount);
  }

}
