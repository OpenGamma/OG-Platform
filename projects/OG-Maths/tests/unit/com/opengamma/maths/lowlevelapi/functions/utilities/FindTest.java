/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Find;

/**
 * Tests the find function
 */
public class FindTest {
  int _valueI = 5;
  int[] _dataLinearI = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  int[] _dataLinearEQI = {4 };
  int[] _dataLinearNEI = {0, 1, 2, 3, 5, 6, 7, 8, 9 };
  int[] _dataLinearLTI = {0, 1, 2, 3 };
  int[] _dataLinearLEI = {0, 1, 2, 3, 4 };
  int[] _dataLinearGTI = {5, 6, 7, 8, 9 };
  int[] _dataLinearGEI = {4, 5, 6, 7, 8, 9 };

  int[] _dataRepeatI = {1, 2, 3, 4, 5, 5, 5, 8, 9, 10 };
  int[] _dataRepeatEQI = {4, 5, 6 };
  int[] _dataRepeatNEI = {0, 1, 2, 3, 7, 8, 9 };
  int[] _dataRepeatLTI = {0, 1, 2, 3 };
  int[] _dataRepeatLEI = {0, 1, 2, 3, 4, 5, 6 };
  int[] _dataRepeatGTI = {7, 8, 9 };
  int[] _dataRepeatGEI = {4, 5, 6, 7, 8, 9 };

  long _valueL = 5;
  long[] _dataLinearL = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  long[] _dataRepeatL = {1, 2, 3, 4, 5, 5, 5, 8, 9, 10 };

  float _valueF = 5;
  float[] _dataLinearF = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  float[] _dataRepeatF = {1, 2, 3, 4, 5, 5, 5+1e6f, 8, 9, 10 };
  int[] _dataRepeatEQF = {4, 5};
  int[] _dataRepeatNEF = {0, 1, 2, 3, 6, 7, 8, 9 };
  int[] _dataRepeatLTF = {0, 1, 2, 3 };
  int[] _dataRepeatLEF = {0, 1, 2, 3, 4, 5 };
  int[] _dataRepeatGTF = {6, 7, 8, 9 };
  int[] _dataRepeatGEF = {4, 5, 6, 7, 8, 9 };

  double _valueD = 5;
  double[] _dataLinearD = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  double[] _dataRepeatD = {1, 2, 3, 4, 5, 5, 5+1e6f, 8, 9, 10 };
  int[] _dataRepeatEQD = {4, 5};
  int[] _dataRepeatNED = {0, 1, 2, 3, 6, 7, 8, 9 };
  int[] _dataRepeatLTD = {0, 1, 2, 3 };
  int[] _dataRepeatLED = {0, 1, 2, 3, 4, 5 };
  int[] _dataRepeatGTD = {6, 7, 8, 9 };
  int[] _dataRepeatGED = {4, 5, 6, 7, 8, 9 };

  boolean[] _dataMixedBoolean = {false,true,false,true,false};
  int [] _dataMixedFalsePositions={0,2,4};
  int [] _dataMixedTruePositions={1,3};  
  
  // Test Ints
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidIntVector() {
    int[] tmp = null;
    Find.indexes(tmp, Find.condition.eq, _valueI);
  }

  @Test
  public void testLinearFindEQI() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearI, Find.condition.eq, _valueI), _dataLinearEQI));
  }

  @Test
  public void testLinearFindNEI() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearI, Find.condition.ne, _valueI), _dataLinearNEI));
  }

  @Test
  public void testLinearFindLTI() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearI, Find.condition.lt, _valueI), _dataLinearLTI));
  }

  @Test
  public void testLinearFindLEI() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearI, Find.condition.le, _valueI), _dataLinearLEI));
  }

  @Test
  public void testLinearFindGTI() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearI, Find.condition.gt, _valueI), _dataLinearGTI));
  }

  @Test
  public void testLinearFindGEI() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearI, Find.condition.ge, _valueI), _dataLinearGEI));
  }

  @Test
  public void testRepeatFindEQI() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatI, Find.condition.eq, _valueI), _dataRepeatEQI));
  }

  @Test
  public void testRepeatFindNEI() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatI, Find.condition.ne, _valueI), _dataRepeatNEI));
  }

  @Test
  public void testRepeatFindLTI() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatI, Find.condition.lt, _valueI), _dataRepeatLTI));
  }

  @Test
  public void testRepeatFindLEI() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatI, Find.condition.le, _valueI), _dataRepeatLEI));
  }

  @Test
  public void testRepeatFindGTI() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatI, Find.condition.gt, _valueI), _dataRepeatGTI));
  }

  @Test
  public void testRepeatFindGEI() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatI, Find.condition.ge, _valueI), _dataRepeatGEI));
  }


  // Test Longs
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidLongVector() {
    long[] tmp = null;
    Find.indexes(tmp, Find.condition.eq, _valueL);
  }

  @Test
  public void testLinearFindEQL() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearL, Find.condition.eq, _valueL), _dataLinearEQI));
  }

  @Test
  public void testLinearFindNEL() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearL, Find.condition.ne, _valueL), _dataLinearNEI));
  }

  @Test
  public void testLinearFindLTL() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearL, Find.condition.lt, _valueL), _dataLinearLTI));
  }

  @Test
  public void testLinearFindLEL() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearL, Find.condition.le, _valueL), _dataLinearLEI));
  }

  @Test
  public void testLinearFindGTL() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearL, Find.condition.gt, _valueL), _dataLinearGTI));
  }

  @Test
  public void testLinearFindGEL() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearL, Find.condition.ge, _valueL), _dataLinearGEI));
  }

  @Test
  public void testRepeatFindEQL() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatL, Find.condition.eq, _valueL), _dataRepeatEQI));
  }

  @Test
  public void testRepeatFindNEL() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatL, Find.condition.ne, _valueL), _dataRepeatNEI));
  }

  @Test
  public void testRepeatFindLTL() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatL, Find.condition.lt, _valueL), _dataRepeatLTI));
  }

  @Test
  public void testRepeatFindLEL() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatL, Find.condition.le, _valueL), _dataRepeatLEI));
  }

  @Test
  public void testRepeatFindGTL() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatL, Find.condition.gt, _valueL), _dataRepeatGTI));
  }

  @Test
  public void testRepeatFindGEL() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatL, Find.condition.ge, _valueL), _dataRepeatGEI));
  }



  // Test Floats
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidFloatVector() {
    float[] tmp = null;
    Find.indexes(tmp, Find.condition.eq, _valueF);
  }

  @Test
  public void testLinearFindEQF() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearF, Find.condition.eq, _valueF), _dataLinearEQI));
  }

  @Test
  public void testLinearFindNEF() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearF, Find.condition.ne, _valueF), _dataLinearNEI));
  }

  @Test
  public void testLinearFindLTF() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearF, Find.condition.lt, _valueF), _dataLinearLTI));
  }

  @Test
  public void testLinearFindLEF() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearF, Find.condition.le, _valueF), _dataLinearLEI));
  }

  @Test
  public void testLinearFindGTF() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearF, Find.condition.gt, _valueF), _dataLinearGTI));
  }

  @Test
  public void testLinearFindGEF() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearF, Find.condition.ge, _valueF), _dataLinearGEI));
  }

  @Test
  public void testRepeatFindEQF() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatF, Find.condition.eq, _valueF), _dataRepeatEQF));
  }

  @Test
  public void testRepeatFindNEF() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatF, Find.condition.ne, _valueF), _dataRepeatNEF));
  }

  @Test
  public void testRepeatFindLTF() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatF, Find.condition.lt, _valueF), _dataRepeatLTF));
  }

  @Test
  public void testRepeatFindLEF() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatF, Find.condition.le, _valueF), _dataRepeatLEF));
  }

  @Test
  public void testRepeatFindGTF() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatF, Find.condition.gt, _valueF), _dataRepeatGTF));
  }

  @Test
  public void testRepeatFindGEF() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatF, Find.condition.ge, _valueF), _dataRepeatGEF));
  }


  // Test Double
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDoubleVector() {
    double[] tmp = null;
    Find.indexes(tmp, Find.condition.eq, _valueD);
  }

  @Test
  public void testLinearFindEQD() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearD, Find.condition.eq, _valueD), _dataLinearEQI));
  }

  @Test
  public void testLinearFindNED() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearD, Find.condition.ne, _valueD), _dataLinearNEI));
  }

  @Test
  public void testLinearFindLTD() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearD, Find.condition.lt, _valueD), _dataLinearLTI));
  }

  @Test
  public void testLinearFindLED() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearD, Find.condition.le, _valueD), _dataLinearLEI));
  }

  @Test
  public void testLinearFindGTD() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearD, Find.condition.gt, _valueD), _dataLinearGTI));
  }

  @Test
  public void testLinearFindGED() {
    assertTrue(Arrays.equals(Find.indexes(_dataLinearD, Find.condition.ge, _valueD), _dataLinearGEI));
  }

  @Test
  public void testRepeatFindEQD() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatD, Find.condition.eq, _valueD), _dataRepeatEQD));
  }

  @Test
  public void testRepeatFindNED() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatD, Find.condition.ne, _valueD), _dataRepeatNED));
  }

  @Test
  public void testRepeatFindLTD() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatD, Find.condition.lt, _valueD), _dataRepeatLTD));
  }

  @Test
  public void testRepeatFindLED() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatD, Find.condition.le, _valueD), _dataRepeatLED));
  }

  @Test
  public void testRepeatFindGTD() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatD, Find.condition.gt, _valueD), _dataRepeatGTD));
  }

  @Test
  public void testRepeatFindGED() {
    assertTrue(Arrays.equals(Find.indexes(_dataRepeatD, Find.condition.ge, _valueD), _dataRepeatGED));
  }

  // Test Bools
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidBoolVector() {
    boolean[] tmp = null;
    Find.indexes(tmp, Find.booleanCondition.eq, true);
  }

  @Test
  public void testLinearFindEQTrue() {
    assertTrue(Arrays.equals(Find.indexes(_dataMixedBoolean, Find.booleanCondition.eq, true), _dataMixedTruePositions));
  }

  @Test
  public void testLinearFindEQFalse() {
    assertTrue(Arrays.equals(Find.indexes(_dataMixedBoolean, Find.booleanCondition.eq, false), _dataMixedFalsePositions));
  }

  @Test
  public void testLinearFindNEQTrue() {
    assertTrue(Arrays.equals(Find.indexes(_dataMixedBoolean, Find.booleanCondition.ne, true), _dataMixedFalsePositions));
  }

  @Test
  public void testLinearFindNEQFalse() {
    assertTrue(Arrays.equals(Find.indexes(_dataMixedBoolean, Find.booleanCondition.ne, false), _dataMixedTruePositions));
  }  
  
}
