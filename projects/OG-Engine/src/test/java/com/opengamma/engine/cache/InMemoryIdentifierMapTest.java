/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.INTEGRATION)
public class InMemoryIdentifierMapTest extends AbstractIdentifierMapTest {

  @Override
  protected IdentifierMap createIdentifierMap(String testName) {
    return new InMemoryIdentifierMap();
  }

}
