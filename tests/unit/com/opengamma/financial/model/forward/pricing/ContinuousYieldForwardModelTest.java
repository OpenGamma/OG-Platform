/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.forward.definition.ContinuousYieldForwardDataBundle;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.util.SetUtils;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * @author emcleod
 *
 */
public class ContinuousYieldForwardModelTest {
  private static final double R = 0.05;
  private static final double D = 0.01;
  private static final double SPOT = 110;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final ForwardModel<ContinuousYieldForwardDataBundle> MODEL = new ContinuousYieldForwardModel();
  private static final ForwardDefinition DEFINITION = new ForwardDefinition(EXPIRY);
  private static final ContinuousYieldForwardDataBundle DATA = new ContinuousYieldForwardDataBundle(D, new ConstantInterestRateDiscountCurve(R), SPOT, DATE);
  private static final Set<Greek> GREEKS = SetUtils.asSet(Greek.FAIR_PRICE, Greek.DELTA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getGreeks(null, DATA, GREEKS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getGreeks(DEFINITION, null, GREEKS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullGreekSet() {
    MODEL.getGreeks(DEFINITION, DATA, null);
  }

  @Test
  public void testRequiredGreeks() {
    assertEquals(new GreekResultCollection(), MODEL.getGreeks(DEFINITION, DATA, Collections.<Greek> emptySet()));
    assertEquals(new GreekResultCollection(), MODEL.getGreeks(DEFINITION, DATA, SetUtils.asSet(Greek.DELTA)));
  }

  @Test
  public void test() {
    final GreekResultCollection result = MODEL.getGreeks(DEFINITION, DATA, GREEKS);
    assertEquals(result.size(), 1);
    assertEquals(result.keySet().iterator().next(), Greek.FAIR_PRICE);
    assertEquals((Double) result.entrySet().iterator().next().getValue().getResult(), SPOT * Math.exp(0.03), 1e-9);
  }
}
