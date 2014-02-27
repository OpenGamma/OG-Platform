package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Test {@link VolatilityCubeDataFudgeBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDataFudgeBuilderTest extends FinancialTestBase {

  /**
   * Tests a cycle for a populated cube.
   */
  @Test
  public void testCycle() {
    VolatilityCubeData<Double, Double, Double> simpleData = getSimpleData();
    VolatilityCubeData<Double, Double, Double> cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    assertEquals(cycledObject, simpleData);
    simpleData = getSimpleDataWithLabels();
    cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    assertEquals(cycledObject, simpleData);
  }

  /**
   * Tests a cycle for an empty cube.
   */
  @Test
  public void testCycleEmptyCube() {
    final VolatilityCubeData<Double, Double, Double> simpleData = getEmptyCube();
    final VolatilityCubeData<Double, Double, Double> cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    assertEquals(cycledObject, simpleData);
  }

  /**
   * Constructs a cube with default labels.
   * @return A cube
   */
  private static VolatilityCubeData<Double, Double, Double> getSimpleData() {
    final Double[] xs = new Double[] {1., 1., 1., 1., 1., 1., 2., 2., 2., 2., 3., 3., 3., 3., 3., 3., 4., 4. };
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 6., 4., 5., 4., 5., 5., 6., 7., 5., 6., 7., 8., 9. };
    final Double[] zs = new Double[] {14., 15., 16., 24., 25., 26., 14., 15., 24., 25., 15., 16., 17., 25., 26., 27., 18., 28. };
    final double[] vols = new double[] {10., 11., 12., 13., 14., 15., 16., 17., 18., 210., 211., 212., 213., 214., 215., 216., 217., 218. };
    final Map<Triple<Double, Double, Double>, Double> values = new HashMap<>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Triple.of(xs[i], ys[i], zs[i]), vols[i]);
    }
    final String definition = "def";
    final String specification = "spec";
    return new VolatilityCubeData<>(definition, specification, values);
  }

  /**
   * Constructs a cube.
   * @return A cube
   */
  private static VolatilityCubeData<Double, Double, Double> getSimpleDataWithLabels() {
    final Double[] xs = new Double[] {1., 1., 1., 1., 1., 1., 2., 2., 2., 2., 3., 3., 3., 3., 3., 3., 4., 4. };
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 6., 4., 5., 4., 5., 5., 6., 7., 5., 6., 7., 8., 9. };
    final Double[] zs = new Double[] {14., 15., 16., 24., 25., 26., 14., 15., 24., 25., 15., 16., 17., 25., 26., 27., 18., 28. };
    final double[] vols = new double[] {10., 11., 12., 13., 14., 15., 16., 17., 18., 210., 211., 212., 213., 214., 215., 216., 217., 218. };
    final Map<Triple<Double, Double, Double>, Double> values = new HashMap<>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Triple.of(xs[i], ys[i], zs[i]), vols[i]);
    }
    final String definition = "def";
    final String specification = "spec";
    return new VolatilityCubeData<>(definition, specification, "X", "Y", "Z", values);
  }

  /**
   * Constructs an empty cube.
   * @return An empty cube
   */
  private static VolatilityCubeData<Double, Double, Double> getEmptyCube() {
    final Map<Triple<Double, Double, Double>, Double> map = Collections.emptyMap();
    return new VolatilityCubeData<>("test",
        "test",
        map);
  }

}
