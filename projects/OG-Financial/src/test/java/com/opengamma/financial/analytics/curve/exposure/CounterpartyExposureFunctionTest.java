/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for CounterpartyExposureFunction.
 */
@Test(groups = TestGroup.UNIT)
public class CounterpartyExposureFunctionTest {

  private static final ExternalId COUNTERPARTY_ID = ExternalId.of(Counterparty.DEFAULT_SCHEME, "TEST");
  
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(COUNTERPARTY_ID);
  
  private static final ExposureFunction EXPOSURE_FUNCTION = new CounterpartyExposureFunction();
  
  @Test
  public void testCounterparty() {
    FRASecurity security = ExposureFunctionTestHelper.getFRASecurity();
    Trade trade = new SimpleTrade(security, BigDecimal.ONE, COUNTERPARTY, LocalDate.now(), OffsetTime.now());
    assertEquals(COUNTERPARTY_ID, EXPOSURE_FUNCTION.getIds(trade).get(0));
  }
}
