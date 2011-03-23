/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.Collections;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.financial.model.forward.definition.StandardForwardDataBundle;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 *
 */
public class CostOfCarryForwardModelTest {
  private static final double R = 0.05;
  private static final double D = 0.01;
  private static final double SPOT = 110;
  private static final double STORAGE = 5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final ForwardModel<StandardForwardDataBundle> MODEL = new CostOfCarryForwardModel();
  private static final ForwardDefinition DEFINITION = new ForwardDefinition(EXPIRY);
  private static final StandardForwardDataBundle DATA = new StandardForwardDataBundle(D, new YieldCurve(ConstantDoublesCurve.from(R)), SPOT, DATE, STORAGE);
  private static final Set<Greek> GREEKS = Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getGreeks(null, DATA, GREEKS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getGreeks(DEFINITION, null, GREEKS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
    assertEquals((SPOT + STORAGE) * Math.exp(0.03), result.get(Greek.FAIR_PRICE), 1e-9);
  }
}
