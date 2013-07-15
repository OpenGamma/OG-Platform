package com.opengamma.core.marketdatasnapshot;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Test {@link VolatilityCubeData}.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDataTest {

  @Test
  public void smilesAsExpected() {
    final VolatilityCubeData data = getSimpleData();
    checkSimpleData(data);
  }

  @Test
  public void nullSmilesAsExpected() {
    final VolatilityCubeData data = getNullData();
    checkNulldata(data);
  }

  private static VolatilityCubeData getSimpleData() {
    final VolatilityCubeData data = new VolatilityCubeData();
    final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    dataPoints.put(new VolatilityPoint(Tenor.DAY, Tenor.DAY, 0), 0.0);
    dataPoints.put(new VolatilityPoint(Tenor.DAY, Tenor.DAY, 1), 1.0);
    dataPoints.put(new VolatilityPoint(Tenor.DAY, Tenor.DAY, 2), 2.0);

    dataPoints.put(new VolatilityPoint(Tenor.WORKING_WEEK, Tenor.WORKING_WEEK, 0), 50.0);
    dataPoints.put(new VolatilityPoint(Tenor.WORKING_WEEK, Tenor.WORKING_WEEK, 1), 51.0);
    dataPoints.put(new VolatilityPoint(Tenor.WORKING_WEEK, Tenor.WORKING_WEEK, 2), 52.0);

    dataPoints.put(new VolatilityPoint(Tenor.DAY, Tenor.WORKING_WEEK, 0), 150.0);
    dataPoints.put(new VolatilityPoint(Tenor.DAY, Tenor.WORKING_WEEK, 1), 151.0);
    dataPoints.put(new VolatilityPoint(Tenor.DAY, Tenor.WORKING_WEEK, 2), 152.0);

    data.setDataPoints(dataPoints);

    final SnapshotDataBundle bundle = new SnapshotDataBundle();
    bundle.setDataPoint(ExternalId.of("Test", "Test"), 0.0);
    data.setOtherData(bundle);
    return data;
  }

  private static void checkSimpleData(final VolatilityCubeData data) {
    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = data.getSmiles();
    assertEquals(2, smiles.size());

    assertTrue(smiles.containsKey(Tenor.DAY));
    final Map<Tenor, Pair<double[], double[]>> dayMap = smiles.get(Tenor.DAY);
    assertEquals(2, dayMap.size());
    assertTrue(dayMap.containsKey(Tenor.DAY));
    Pair<double[], double[]> smile = dayMap.get(Tenor.DAY);
    double[] expectedStrikes = new double[] {0.0, 1.0, 2.0 };
    double[] expectedVols = new double[] {0.0, 1.0, 2.0 };
    assertMatches(smile, expectedStrikes, expectedVols);

    smile = dayMap.get(Tenor.WORKING_WEEK);

    expectedStrikes = new double[] {0.0, 1.0, 2.0 };
    expectedVols = new double[] {150.0, 151.0, 152.0 };
    assertMatches(smile, expectedStrikes, expectedVols);

    assertTrue(smiles.containsKey(Tenor.WORKING_WEEK));

    smile = smiles.get(Tenor.WORKING_WEEK).get(Tenor.WORKING_WEEK);

    expectedStrikes = new double[] {0.0, 1.0, 2.0 };
    expectedVols = new double[] {50.0, 51.0, 52.0 };
    assertMatches(smile, expectedStrikes, expectedVols);

    assertEquals(1, data.getOtherData().size());
  }

  private static void checkNulldata(final VolatilityCubeData data) {
    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = data.getSmiles();
    assertEquals(null, smiles);
  }

  private static VolatilityCubeData getNullData() {
    final VolatilityCubeData data = new VolatilityCubeData();
    final HashMap<VolatilityPoint, Double> dataPoints = null;

    data.setDataPoints(dataPoints);
    return data;
  }

  private static void assertMatches(final Pair<double[], double[]> smile, final double[] expectedStrikes, final double[] expectedVols) {
    assertTrue(Arrays.equals(smile.getFirst(), expectedStrikes));
    assertTrue(Arrays.equals(smile.getSecond(), expectedVols));
  }

}
