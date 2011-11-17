/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.google.common.collect.ImmutableSet;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class CurrencyPairsFudgeBuilderTest {

  /**
   * Writes an instance of {@link CurrencyPairs} to a Fudge message and reads it back.
   */
  @Test
  public void roundTrip() {
    CurrencyPairs pairs1 = new CurrencyPairs(ImmutableSet.of(
        CurrencyPair.of("EUR/USD"),
        CurrencyPair.of("GBP/USD"),
        CurrencyPair.of("USD/CAD")));
    CurrencyPairsFudgeBuilder builder = new CurrencyPairsFudgeBuilder();
    MutableFudgeMsg msg = builder.buildMessage(new FudgeSerializer(FudgeContext.GLOBAL_DEFAULT), pairs1);
    AssertJUnit.assertNotNull(msg);
    CurrencyPairs pairs2 = builder.buildObject(new FudgeDeserializer(FudgeContext.GLOBAL_DEFAULT), msg);
    AssertJUnit.assertEquals(pairs1, pairs2);
  }
}
