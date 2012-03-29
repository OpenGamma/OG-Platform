/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.View;

/**
 * Tests the view applying class
 */
public class ViewTest {

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
  double [] answerByIndexD = {2,4,6,7,9};
  double [] answerByRangeD = {4,5,6,7};

  // floats
  float [] nullF = null;
  float [] rowF = {1,2,3,4,5,6,7,8,9,10};
  float [] answerByIndexF = {2,4,6,7,9};
  float [] answerByRangeF = {4,5,6,7};

  // longs
  long [] nullL = null;
  long [] rowL = {1,2,3,4,5,6,7,8,9,10};
  long [] answerByIndexL = {2,4,6,7,9};
  long [] answerByRangeL = {4,5,6,7};

  // doubles
  int [] nullI = null;
  int [] rowI = {1,2,3,4,5,6,7,8,9,10};
  int [] answerByIndexI = {2,4,6,7,9};
  int [] answerByRangeI = {4,5,6,7};


  /* test doubles */
    // null in
  @Test (expectedExceptions = IllegalArgumentException.class)
  public void test12NullInputD(){
    View.byIndex(nullD,sparseIndex);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void test21NullInputD(){
    View.byIndex(rowD,nullIndex);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testNull3InputD(){
    View.byRange(nullD,rangeLow,rangeHigh);
  }

    // invalid logic
  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeIndexHighD(){
    View.byIndex(rowD,sparseIndexTooHigh);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeIndexLowD(){
    View.byIndex(rowD,sparseIndexTooLow);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeValueHighD(){
    View.byRange(rowD,rangeLow,rangeTooHigh);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testOutOfRangeValueLowD(){
    View.byRange(rowD,rangeTooLow,rangeHigh);
  }

  @Test (expectedExceptions = IllegalArgumentException.class)
  public void testBackwardsRangeD(){
    View.byRange(rowD,rangeHigh,rangeLow);
  }

  // answers
  @Test
  public void testViewbyIndicesD(){
    assertTrue(Arrays.equals(View.byIndex(rowD,sparseIndex),answerByIndexD));
  }

  @Test
  public void testViewbyRangeD(){
    assertTrue(Arrays.equals(View.byRange(rowD,rangeLow,rangeHigh),answerByRangeD));
  }

  /* test floats */
  // null in
@Test (expectedExceptions = IllegalArgumentException.class)
public void test12NullInputF(){
  View.byIndex(nullF,sparseIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void test21NullInputF(){
  View.byIndex(rowF,nullIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testNull3InputF(){
  View.byRange(nullF,rangeLow,rangeHigh);
}

  // invalid logic
@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexHighF(){
  View.byIndex(rowF,sparseIndexTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexLowF(){
  View.byIndex(rowF,sparseIndexTooLow);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueHighF(){
  View.byRange(rowF,rangeLow,rangeTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueLowF(){
  View.byRange(rowF,rangeTooLow,rangeHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testBackwardsRangeF(){
  View.byRange(rowF,rangeHigh,rangeLow);
}

// answers
@Test
public void testViewbyIndicesF(){
  assertTrue(Arrays.equals(View.byIndex(rowF,sparseIndex),answerByIndexF));
}

@Test
public void testViewbyRangeF(){
  assertTrue(Arrays.equals(View.byRange(rowF,rangeLow,rangeHigh),answerByRangeF));
}


/* test longs */
// null in
@Test (expectedExceptions = IllegalArgumentException.class)
public void test12NullInputL(){
View.byIndex(nullL,sparseIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void test21NullInputL(){
View.byIndex(rowL,nullIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testNull3InputL(){
View.byRange(nullL,rangeLow,rangeHigh);
}

// invalid logic
@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexHighL(){
View.byIndex(rowL,sparseIndexTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexLowL(){
View.byIndex(rowL,sparseIndexTooLow);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueHighL(){
View.byRange(rowL,rangeLow,rangeTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueLowL(){
View.byRange(rowL,rangeTooLow,rangeHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testBackwardsRangeL(){
View.byRange(rowL,rangeHigh,rangeLow);
}

//answers
@Test
public void testViewbyIndicesL(){
assertTrue(Arrays.equals(View.byIndex(rowL,sparseIndex),answerByIndexL));
}

@Test
public void testViewbyRangeL(){
assertTrue(Arrays.equals(View.byRange(rowL,rangeLow,rangeHigh),answerByRangeL));
}

/* test ints */
//null in
@Test (expectedExceptions = IllegalArgumentException.class)
public void test12NullInputI(){
View.byIndex(nullI,sparseIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void test21NullInputI(){
View.byIndex(rowI,nullIndex);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testNull3InputI(){
View.byRange(nullI,rangeLow,rangeHigh);
}

//invalid logic
@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexHighI(){
View.byIndex(rowI,sparseIndexTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeIndexLowI(){
View.byIndex(rowI,sparseIndexTooLow);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueHighI(){
View.byRange(rowI,rangeLow,rangeTooHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testOutOfRangeValueLowI(){
View.byRange(rowI,rangeTooLow,rangeHigh);
}

@Test (expectedExceptions = IllegalArgumentException.class)
public void testBackwardsRangeI(){
View.byRange(rowI,rangeHigh,rangeLow);
}

//answers
@Test
public void testViewbyIndicesI(){
assertTrue(Arrays.equals(View.byIndex(rowI,sparseIndex),answerByIndexI));
}

@Test
public void testViewbyRangeI(){
assertTrue(Arrays.equals(View.byRange(rowI,rangeLow,rangeHigh),answerByRangeI));
}


}

