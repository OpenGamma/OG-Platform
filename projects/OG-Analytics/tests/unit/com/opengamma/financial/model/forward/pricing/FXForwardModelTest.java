/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.forward.definition.FXForwardDataBundle;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 *
 */
public class FXForwardModelTest {
  private static final double R1 = 0.05;
  private static final double R2 = 0.07;
  private static final double SPOT = 1.4;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final ForwardModel<FXForwardDataBundle> MODEL = new FXForwardModel();
  private static final ForwardDefinition DEFINITION = new ForwardDefinition(EXPIRY);
  private static final FXForwardDataBundle DATA = new FXForwardDataBundle(new YieldCurve(ConstantDoublesCurve.from(R1)), new YieldCurve(ConstantDoublesCurve.from(R2)), SPOT, DATE);
  private static final Set<Greek> GREEKS = Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA);

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
    assertEquals(new GreekResultCollection(), MODEL.getGreeks(DEFINITION, DATA, Sets.newHashSet(Greek.DELTA)));
  }

  @Test
  public void test() {
    final GreekResultCollection result = MODEL.getGreeks(DEFINITION, DATA, GREEKS);
    assertEquals(1, result.size());
    assertTrue(result.contains(Greek.FAIR_PRICE));
    assertEquals(SPOT * Math.exp(-0.01), result.get(Greek.FAIR_PRICE), 1e-9);
  }
}
