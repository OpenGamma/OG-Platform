package com.opengamma.core.marketdatasnapshot;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Test {@link VolatilityCubeDataBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDataFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void testCycleSimpleGraph() {
    final VolatilityCubeData simpleData = getSimpleData();

    final VolatilityCubeData cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    assertEquals(cycledObject, simpleData);
  }

  @Test
  public void testCycleNullGraph() {
    final VolatilityCubeData simpleData = getNullData();

    final VolatilityCubeData cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    assertEquals(cycledObject, simpleData);
  }

  private static VolatilityCubeData<Double, Double, Double> getSimpleData() {
    final Double[] xs = new Double[] {1., 1., 1., 1., 1., 1., 2., 2., 2., 2., 3., 3., 3., 3., 3., 3., 4., 4. };
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 6., 4., 5., 4., 5., 5., 6., 7., 5., 6., 7., 8., 9. };
    final Double[] zs = new Double[] {14., 15., 16., 24., 25., 26., 14., 15., 24., 25., 15., 16., 17., 25., 26., 27., 18., 28. };
    final double[] vols = new double[] {10., 11., 12., 13., 14., 15., 16., 17., 18., 210., 211., 212., 213., 214., 215., 216., 217., 218. };
    final Map<Triple<Double, Double, Double>, Double> values = new HashMap<>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Triple.of(xs[i], ys[i], zs[i]), vols[i]);
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    return new VolatilityCubeData<>(name, name, target, xs, ys, zs, values);
  }

  private static VolatilityCubeData<Double, Double, Double> getNullData() {
    Map<Triple<Double, Double, Double>, Double> map = Collections.emptyMap();
    return new VolatilityCubeData<>("test",
        "test",
        Currency.USD,
        new Double[] {},
        new Double[] {},
        new Double[] {},
        map);
  }

}
