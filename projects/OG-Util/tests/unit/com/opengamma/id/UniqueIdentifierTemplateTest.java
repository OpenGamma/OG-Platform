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
 * Tests {@link UniqueIdentifierTemplate}. The use of '/' at the end of the prefix is purely to aid readability here;
 * no assumptions are made about the format of the prefix.
 */
@Test
public class UniqueIdentifierTemplateTest {
  
  private UniqueIdentifier uid(String scheme, String value) {
    return UniqueIdentifier.of(scheme, value);
  }
  
  public void testSchemeAndValuePrefixTemplate() {
    final String scheme = "testScheme";
    UniqueIdentifierTemplate template = new UniqueIdentifierTemplate(scheme, "testPrefix/");
    UniqueIdentifier generated = template.uid("someValueContent");
    assertEquals(UniqueIdentifier.of(scheme, "testPrefix/someValueContent"), generated);
    assertTrue(template.conforms(generated));
    assertFalse(template.conforms(uid(scheme, "contentOnly")));
    assertEquals("someValueContent", template.extractValueContent(generated));
  }
  
  public void testSchemeOnlyTemplate() {
    final String scheme = "testScheme";
    UniqueIdentifierTemplate template = new UniqueIdentifierTemplate(scheme);
    UniqueIdentifier generated = template.uid("someValueContent");
    assertEquals(uid(scheme, "someValueContent"), generated);
    assertTrue(template.conforms(generated));
    assertEquals("someValueContent", template.extractValueContent(generated));
  }
  
  public void testConforms() {
    final String scheme = "scheme";
    UniqueIdentifierTemplate template = new UniqueIdentifierTemplate(scheme, "prefix/");
    assertFalse(template.conforms(uid(scheme, "prefix")));
    assertTrue(template.conforms(uid(scheme, "prefix/")));
    assertTrue(template.conforms(uid(scheme, "prefix/c")));
    assertTrue(template.conforms(uid(scheme, "prefix/content")));
    assertFalse(template.conforms(uid("anotherScheme", "prefix/content")));
  }
  
  public void testExtractValueContent() {
    final String scheme = "scheme";
    UniqueIdentifierTemplate template = new UniqueIdentifierTemplate(scheme, "prefix/");
    assertEquals("", template.extractValueContent(uid(scheme, "prefix/")));
    assertEquals("abc", template.extractValueContent(uid(scheme, "prefix/abc")));
  }

}
