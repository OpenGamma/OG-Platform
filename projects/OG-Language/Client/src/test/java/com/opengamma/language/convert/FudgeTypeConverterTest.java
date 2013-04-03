/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FudgeTypeConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeTypeConverterTest extends AbstractConverterTest {

  private final FudgeTypeConverter _converter = new FudgeTypeConverter (OpenGammaFudgeContext.getInstance ());

  public void testToCurrency() {
    final JavaTypeInfo<Currency> target = JavaTypeInfo.builder(Currency.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, "USD", target, Currency.USD);
    assertInvalidConversion(_converter, "", target);
    assertConversionCount(1, _converter, target);
  }

  public void testToString() {
    final JavaTypeInfo<String> target = JavaTypeInfo.builder(String.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, Currency.USD, target, "USD");
    assertInvalidConversion(_converter, 42, target);
    assertConversionCount(1, _converter, target);
  }
  
  private Security createSecurityObject() {
    return new EquitySecurity("X", "XC", "CN", Currency.USD);
  }

  private FudgeMsg createSecurityMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(_converter.getFudgeContext());
    final Security security = createSecurityObject();
    return FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(security), security.getClass(), Security.class);
  }

  public void testToSecurity () {
    final JavaTypeInfo<Security> target = JavaTypeInfo.builder(Security.class).get ();
    assertEquals(_converter.canConvertTo(target),true);
    assertValidConversion(_converter, createSecurityMessage(), target, createSecurityObject());
    assertInvalidConversion(_converter, "", target);
    assertConversionCount(1, _converter, target);
  }

  public void testToFudgeMsg() {
    final JavaTypeInfo<FudgeMsg> target = JavaTypeInfo.builder(FudgeMsg.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, createSecurityObject(), target, createSecurityMessage());
    assertInvalidConversion(_converter, "", target);
    assertConversionCount(1, _converter, target);
  }

}
