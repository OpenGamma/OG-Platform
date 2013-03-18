/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.text;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link CompositeStringUtil} class.
 */
@Test(groups = TestGroup.UNIT)
public class CompositeStringUtilTest {

  private static void cycle(final CompositeStringUtil util, final String composite, final String... components) {
    assertEquals(util.create(components), composite);
    assertEquals(util.parse(composite), components);
  }

  public void testDefaultEscapeLast() {
    final CompositeStringUtil util = new CompositeStringUtil(2, true);
    cycle(util, "Foo_Bar", "Foo", "Bar");
    cycle(util, "Foo_$$$_", "Foo", "$_");
    cycle(util, "$$$__Bar", "$_", "Bar");
    cycle(util, "$$$__$$$_", "$_", "$_");
  }

  public void testDefaultNoEscapeLast() {
    final CompositeStringUtil util = new CompositeStringUtil(2, false);
    cycle(util, "Foo_Bar", "Foo", "Bar");
    cycle(util, "Foo_$_", "Foo", "$_");
    cycle(util, "$$$__Bar", "$_", "Bar");
    cycle(util, "$$$__$_", "$_", "$_");
  }

  public void testCustomEscapeLast() {
    final CompositeStringUtil util = new CompositeStringUtil(2, true);
    util.setEscapeCharacters('!', '%');
    util.setSeparators('+', '-');
    cycle(util, "%-+!+", "%-", "!+");
    cycle(util, "%-+%%%-", "%-", "%-");
    cycle(util, "!!!++!+", "!+", "!+");
    cycle(util, "!!!++%%%-", "!+", "%-");
  }

  public void testCustomNoEscapeLast() {
    final CompositeStringUtil util = new CompositeStringUtil(2, false);
    util.setEscapeCharacters('!');
    util.setSeparators('+');
    cycle(util, "%-+!+", "%-", "!+");
    cycle(util, "%-+%-", "%-", "%-");
    cycle(util, "!!!++!+", "!+", "!+");
    cycle(util, "!!!++%-", "!+", "%-");
  }

}
