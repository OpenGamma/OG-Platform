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
public class SuccessFunctionResultTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void serializeResultWithCurrencyAmount() {
    FunctionResult<CurrencyAmount> success = FunctionResultGenerator.success(CurrencyAmount.of(Currency.AUD, 12345));
    assertEncodeDecodeCycle(ResultContainer.class, ResultContainer.of(success));
  }

  @Test
  public void serializeResultWithMapStringString() {
    Map<String, String> map = ImmutableMap.of("one", "1", "two", "2");
    FunctionResult<Map<String, String>> success = FunctionResultGenerator.success(map);
    assertEncodeDecodeCycle(ResultContainer.class, ResultContainer.of(success));
  }
}
