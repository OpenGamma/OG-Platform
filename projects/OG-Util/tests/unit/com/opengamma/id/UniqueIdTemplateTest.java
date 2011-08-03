/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Tests {@link UniqueIdTemplate}.
 * The use of '/' at the end of the prefix is purely to aid readability here;
 * no assumptions are made about the format of the prefix.
 */
@Test
public class UniqueIdTemplateTest {

  private UniqueId uniqueId(String scheme, String value) {
    return UniqueId.of(scheme, value);
  }

  public void testSchemeAndValuePrefixTemplate() {
    final String scheme = "testScheme";
    UniqueIdTemplate template = new UniqueIdTemplate(scheme, "testPrefix/");
    UniqueId generated = template.uniqueId("someValueContent");
    assertEquals(UniqueId.of(scheme, "testPrefix/someValueContent"), generated);
    assertTrue(template.conforms(generated));
    assertFalse(template.conforms(uniqueId(scheme, "contentOnly")));
    assertEquals("someValueContent", template.extractValueContent(generated));
  }

  public void testSchemeOnlyTemplate() {
    final String scheme = "testScheme";
    UniqueIdTemplate template = new UniqueIdTemplate(scheme);
    UniqueId generated = template.uniqueId("someValueContent");
    assertEquals(uniqueId(scheme, "someValueContent"), generated);
    assertTrue(template.conforms(generated));
    assertEquals("someValueContent", template.extractValueContent(generated));
  }

  public void testConforms() {
    final String scheme = "scheme";
    UniqueIdTemplate template = new UniqueIdTemplate(scheme, "prefix/");
    assertFalse(template.conforms(uniqueId(scheme, "prefix")));
    assertTrue(template.conforms(uniqueId(scheme, "prefix/")));
    assertTrue(template.conforms(uniqueId(scheme, "prefix/c")));
    assertTrue(template.conforms(uniqueId(scheme, "prefix/content")));
    assertFalse(template.conforms(uniqueId("anotherScheme", "prefix/content")));
  }

  public void testExtractValueContent() {
    final String scheme = "scheme";
    UniqueIdTemplate template = new UniqueIdTemplate(scheme, "prefix/");
    assertEquals("", template.extractValueContent(uniqueId(scheme, "prefix/")));
    assertEquals("abc", template.extractValueContent(uniqueId(scheme, "prefix/abc")));
  }

}
