/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.sesame.credit.CreditPricingSampleData;
import com.opengamma.sesame.credit.config.RestructuringSettings;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the round trip serialization of the credit config items and standard/legacy cds
 */
@Test(groups = TestGroup.UNIT)
public class CreditSourcesTest {

  @Test
  public void standardCdsRoundTrip() {
    StandardCDSSecurity input = CreditPricingSampleData.createStandardCDSSecurity();
    String xml = JodaBeanSerialization.serializer(true).xmlWriter().write(input);
    StandardCDSSecurity output = (StandardCDSSecurity) JodaBeanSerialization.deserializer().xmlReader().read(xml);
    assertEquals(input, output);
  }

  @Test
  public void legacyCdsRoundTrip() {
    LegacyCDSSecurity input = CreditPricingSampleData.createLegacyCDSSecurity();
    String xml = JodaBeanSerialization.serializer(true).xmlWriter().write(input);
    LegacyCDSSecurity output = (LegacyCDSSecurity) JodaBeanSerialization.deserializer().xmlReader().read(xml);
    assertEquals(input, output);
  }

  @Test
  public void restructuringSettingsRoundTrip() {
    RestructuringSettings input = CreditPricingSampleData.createRestructuringSettings();
    String xml = JodaBeanSerialization.serializer(true).xmlWriter().write(input);
    RestructuringSettings output = (RestructuringSettings) JodaBeanSerialization.deserializer().xmlReader().read(xml);
    assertEquals(input, output);
  }

  @Test
  public void creditCurveDataSnapshotRoundTrip() {
    CreditCurveDataSnapshot input = CreditPricingSampleData.createCreditCurveDataSnapshot();
    String xml = JodaBeanSerialization.serializer(true).xmlWriter().write(input);
    CreditCurveDataSnapshot output = (CreditCurveDataSnapshot) JodaBeanSerialization.deserializer().xmlReader().read(xml);
    assertEquals(input, output);

    FudgeMsg fudge = OpenGammaFudgeContext.getInstance().toFudgeMsg(input).getMessage();
    CreditCurveDataSnapshot fudgeOutput = (CreditCurveDataSnapshot) OpenGammaFudgeContext.getInstance().fromFudgeMsg(fudge);
    assertEquals(input, fudgeOutput);
  }

  @Test
  public void createYieldCurveDataSnapshotRoundTrip() {
    YieldCurveDataSnapshot input = CreditPricingSampleData.createYieldCurveDataSnapshot();
    String xml = JodaBeanSerialization.serializer(true).xmlWriter().write(input);
    YieldCurveDataSnapshot output = (YieldCurveDataSnapshot) JodaBeanSerialization.deserializer().xmlReader().read(xml);
    assertEquals(input, output);

    FudgeMsg fudge = OpenGammaFudgeContext.getInstance().toFudgeMsg(input).getMessage();
    YieldCurveDataSnapshot fudgeOutput = (YieldCurveDataSnapshot) OpenGammaFudgeContext.getInstance().fromFudgeMsg(fudge);
    assertEquals(input, fudgeOutput);
  }

}
