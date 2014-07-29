/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SuccessResultFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void serializeResultWithCurrencyAmount() {
    Result<CurrencyAmount> success = Result.success(CurrencyAmount.of(Currency.AUD, 12345));
    assertEncodeDecodeCycle(ResultContainer.class, ResultContainer.of(success));
  }

  @Test(enabled=false)
  public void serializeResultWithMapStringString() {
    Map<String, String> map = ImmutableMap.of("one", "1", "two", "2");
    Result<Map<String, String>> success = Result.success(map);
    assertEncodeDecodeCycle(ResultContainer.class, ResultContainer.of(success));
  }

}
