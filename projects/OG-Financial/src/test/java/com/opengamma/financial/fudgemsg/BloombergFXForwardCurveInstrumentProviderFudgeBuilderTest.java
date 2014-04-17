/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fxforwardcurve.BloombergFXForwardCurveInstrumentProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test the Fudge builder for {@link BloombergFXForwardCurveInstrumentProviderFudgeBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class BloombergFXForwardCurveInstrumentProviderFudgeBuilderTest extends FinancialTestBase {

  /**
   * Tests cycling the object.
   */
  @Test
  public void test() {
    final String prefix = "EUR";
    final String postfix = "Curncy";
    final String spotPrefix = "EU";
    final String dataFieldName = "PRICE";
    final FudgeSerializer fudgeSerializationContext = new FudgeSerializer(getFudgeContext());
    BloombergFXForwardCurveInstrumentProvider provider = new BloombergFXForwardCurveInstrumentProvider(prefix, postfix, spotPrefix, dataFieldName);
    MutableFudgeMsg message = fudgeSerializationContext.newMessage();
    fudgeSerializationContext.addToMessageWithClassHeaders(message, "test", null, provider, BloombergFXForwardCurveInstrumentProvider.class);
    assertEquals(provider, cycleObject(BloombergFXForwardCurveInstrumentProvider.class, provider));
    FudgeMsg submessage = (FudgeMsg) (message.getByName("test").getValue());
    assertFalse(submessage.getBoolean("useSpotRateFromGraph"));
    provider = new BloombergFXForwardCurveInstrumentProvider(spotPrefix, postfix, dataFieldName);
    message = fudgeSerializationContext.newMessage();
    fudgeSerializationContext.addToMessageWithClassHeaders(message, "test", null, provider, BloombergFXForwardCurveInstrumentProvider.class);
    assertEquals(provider, cycleObject(BloombergFXForwardCurveInstrumentProvider.class, provider));
    submessage = (FudgeMsg) (message.getByName("test").getValue());
    assertTrue(submessage.getBoolean("useSpotRateFromGraph"));
  }
}
