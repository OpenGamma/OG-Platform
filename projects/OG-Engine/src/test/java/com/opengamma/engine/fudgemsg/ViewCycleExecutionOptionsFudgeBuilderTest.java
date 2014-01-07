/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.marketdata.manipulator.CompositeMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.YieldCurveSelector;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ViewCycleExecutionOptionsFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void rountTrip() {
    Map<DistinctMarketDataSelector, FunctionParameters> selectors2params = Maps.newHashMap();
    Map<String, String> params = ImmutableMap.of("foo", "bar");
    DistinctMarketDataSelector selector = YieldCurveSelector.of(YieldCurveKey.of(Currency.AUD, "curveKey"));
    selectors2params.put(selector, new SimpleFunctionParameters(params));
    ViewCycleExecutionOptions options =
        ViewCycleExecutionOptions
            .builder()
            .setValuationTime(Instant.now())
            .setMarketDataSelector(CompositeMarketDataSelector.of(selectors2params.keySet()))
            .setFunctionParameters(selectors2params)
            .setResolverVersionCorrection(VersionCorrection.LATEST)
            .create();
    assertEncodeDecodeCycle(ViewCycleExecutionOptions.class, options);
  }
}
