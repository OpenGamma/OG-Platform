/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Sum;


/**
 * Tests the sum function
 */
public class SumTest {

  /* indices to look up */
  int [] sparseIndex = {1,3,5,6,8};
  int [] sparseIndexTooHigh = {1,3,5,57,6,8};
  int [] sparseIndexTooLow = {1,3,5,-57,6,8};
  int [] nullIndex = null;
  int rangeLow = 3;
  int rangeHigh = 6;
  int rangeTooHigh = 37;
  int rangeTooLow = -3;

  /* the data */
  // doubles
  double [] nullD = null;
  double [] rowD = {1,2,3,4,5,6,7,8,9,10};
  // floats
  float [] nullF = null;
  float [] rowF = {1,2,3,4,5,6,7,8,9,10};
  // longs
  long [] nullL = null;
  long [] rowL = {1,2,3,4,5,6,7,8,9,10};
  // doubles
  int [] nullI = null;
  int [] rowI = {1,2,3,4,5,6,7,8,9,10};


  /* test doubles */
    // null in
  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testNullInputD(){
    Sum.overAllIndices(nullD);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void test2NullInputD(){
    Sum.overIndices(rowD,nullIndex);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testNull3InputD(){
    Sum.overRange(nullD,rangeLow,rangeHigh);
  }

    // invalid logic
  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeIndexHighD(){
    Sum.overIndices(rowD,sparseIndexTooHigh);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeIndexLowD(){
    Sum.overIndices(rowD,sparseIndexTooLow);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeValueHighD(){
    Sum.overRange(rowD,rangeLow,rangeTooHigh);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeValueLowD(){
    Sum.overRange(rowD,rangeTooLow,rangeHigh);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testBackwardsRangeD(){
    Sum.overRange(rowD,rangeHigh,rangeLow);
  }

    // answers
  @Test
  public void testSumOverAllIndicesD(){
    assertTrue(Sum.overAllIndices(rowD)==55);
  }

  @Test
  public void testSumOverIndicesD(){
    assertTrue(Sum.overIndices(rowD,sparseIndex)==28);
  }

  @Test
  public void testSumOverRangeD(){
    assertTrue(Sum.overRange(rowD,rangeLow,rangeHigh)==22);
  }

  /* test floats */
  // null in
@Test (expectedExceptions = IllegalArgumentException.class)
public void testNullInputF(){
  Sum.overAllIndices(nullF);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void test2NullInputF(){
  Sum.overIndices(rowF,nullIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testNull3InputF(){
  Sum.overRange(nullF,rangeLow,rangeHigh);
}

  // invalid logic
@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexHighF(){
  Sum.overIndices(rowF,sparseIndexTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexLowF(){
  Sum.overIndices(rowF,sparseIndexTooLow);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueHighF(){
  Sum.overRange(rowF,rangeLow,rangeTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueLowF(){
  Sum.overRange(rowF,rangeTooLow,rangeHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testBackwardsRangeF(){
  Sum.overRange(rowF,rangeHigh,rangeLow);
}

  // answers
@Test
public void testSumOverAllIndicesF(){
  assertTrue(Sum.overAllIndices(rowF)==55);
}

@Test
public void testSumOverIndicesF(){
  assertTrue(Sum.overIndices(rowF,sparseIndex)==28);
}

@Test
public void testSumOverRangeF(){
  assertTrue(Sum.overRange(rowF,rangeLow,rangeHigh)==22);
}

/* test longs */
// null in
@Test (expectedExceptions = IllegalArgumentException.class)
public void testNullInputL(){
Sum.overAllIndices(nullL);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void test2NullInputL(){
Sum.overIndices(rowL,nullIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testNull3InputL(){
Sum.overRange(nullL,rangeLow,rangeHigh);
}

// invalid logic
@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexHighL(){
Sum.overIndices(rowL,sparseIndexTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexLowL(){
Sum.overIndices(rowL,sparseIndexTooLow);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueHighL(){
Sum.overRange(rowL,rangeLow,rangeTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueLowL(){
Sum.overRange(rowL,rangeTooLow,rangeHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testBackwardsRangeL(){
Sum.overRange(rowL,rangeHigh,rangeLow);
}

// answers
@Test
public void testSumOverAllIndicesL(){
assertTrue(Sum.overAllIndices(rowL)==55);
}

@Test
public void testSumOverIndicesL(){
assertTrue(Sum.overIndices(rowL,sparseIndex)==28);
}

@Test
public void testSumOverRangeL(){
assertTrue(Sum.overRange(rowL,rangeLow,rangeHigh)==22);
}



/* test ints */
// null in
@Test (expectedExceptions = IllegalArgumentException.class)
public void testNullInputI(){
Sum.overAllIndices(nullI);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void test2NullInputI(){
Sum.overIndices(rowI,nullIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testNull3InputI(){
Sum.overRange(nullI,rangeLow,rangeHigh);
}

// invalid logic
@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexHighI(){
Sum.overIndices(rowI,sparseIndexTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexLowI(){
Sum.overIndices(rowI,sparseIndexTooLow);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueHighI(){
Sum.overRange(rowI,rangeLow,rangeTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueLowI(){
Sum.overRange(rowI,rangeTooLow,rangeHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testBackwardsRangeI(){
Sum.overRange(rowI,rangeHigh,rangeLow);
}

// answers
@Test
public void testSumOverAllIndicesI(){
assertTrue(Sum.overAllIndices(rowI)==55);
}

@Test
public void testSumOverIndicesI(){
assertTrue(Sum.overIndices(rowI,sparseIndex)==28);
}

@Test
public void testSumOverRangeI(){
assertTrue(Sum.overRange(rowI,rangeLow,rangeHigh)==22);
}
}
