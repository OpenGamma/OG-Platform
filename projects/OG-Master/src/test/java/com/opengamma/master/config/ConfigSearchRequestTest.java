/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ConfigSearchRequestTest {

  /**
   * Tests matching a document using a name containing wildcards.
   */
  @Test
  public void wildcardNameMatch() {
    ConfigSearchRequest<Object> searchRequest = new ConfigSearchRequest<>(Object.class);

    searchRequest.setName("foo*bar");
    assertTrue(searchRequest.matches(document("foobar")));
    assertTrue(searchRequest.matches(document("foo bar")));
    assertTrue(searchRequest.matches(document("foo12345bar")));
    assertFalse(searchRequest.matches(document("foo")));
    assertTrue(searchRequest.matches(document("fooBar")));

    searchRequest.setName("*baz*");
    assertTrue(searchRequest.matches(document("baz")));
    assertTrue(searchRequest.matches(document("baz1234")));
    assertTrue(searchRequest.matches(document("1234baz1234")));

    searchRequest.setName("?qux");
    assertTrue(searchRequest.matches(document("1qux")));
    assertFalse(searchRequest.matches(document("qux")));
  }

  /**
   * Tests matching a document using an exact name match.
   */
  @Test
  public void exactNameMatch() {
    ConfigSearchRequest<Object> searchRequest = new ConfigSearchRequest<>(Object.class);

    searchRequest.setName("foo");
    assertTrue(searchRequest.matches(document("foo")));
    assertFalse(searchRequest.matches(document("bar")));
    assertFalse(searchRequest.matches(document("Foo")));
  }

  private static ConfigDocument document(String name) {
    return new ConfigDocument(ConfigItem.of(new Object(), name));
  }
}
