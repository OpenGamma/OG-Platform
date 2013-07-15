/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.UUID;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test GUIDGenerator.
 */
@Test(groups = TestGroup.UNIT)
public class GUIDGeneratorTest {

  public void generatorAlwaysGeneratesSomething() {
    UUID uuid = GUIDGenerator.generate();
    assertNotNull(uuid);
    // System.out.println("Generated UUID " + uuid);
  }

}
