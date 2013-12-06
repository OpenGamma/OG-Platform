/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test for continuous integration that has no tags.
 */
@Test // has no test group
public class UntaggedTest {

  public void test_notVeryMuch() {
    assertEquals("Bamboo", "Bamboo");
  }

}
