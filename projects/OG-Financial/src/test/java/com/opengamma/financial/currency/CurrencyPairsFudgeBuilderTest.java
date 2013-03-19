/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyPairsFudgeBuilderTest {

  /**
   * Writes an instance of {@link CurrencyPairs} to a Fudge message and reads it back.
   */
  @Test
  public void roundTrip() {
    CurrencyPairs pairs1 = CurrencyPairs.of(ImmutableSet.of(
        CurrencyPair.parse("EUR/USD"),
        CurrencyPair.parse("GBP/USD"),
        CurrencyPair.parse("USD/CAD")));
    CurrencyPairsFudgeBuilder builder = new CurrencyPairsFudgeBuilder();
    MutableFudgeMsg msg = builder.buildMessage(new FudgeSerializer(FudgeContext.GLOBAL_DEFAULT), pairs1);
    AssertJUnit.assertNotNull(msg);
    CurrencyPairs pairs2 = builder.buildObject(new FudgeDeserializer(FudgeContext.GLOBAL_DEFAULT), msg);
    AssertJUnit.assertEquals(pairs1, pairs2);
  }

}
