/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Iterator;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.function.BinaryOperator;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ComputationTargetTypeMap} class
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetTypeMapTest {

  public void testEmpty() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    assertNull(map.getDirect(ComputationTargetType.NULL));
    assertNull(map.get(ComputationTargetType.NULL));
    assertNull(map.getDirect(ComputationTargetType.POSITION));
    assertNull(map.get(ComputationTargetType.POSITION));
    assertNull(map.getDirect(ComputationTargetType.SECURITY));
    assertNull(map.get(ComputationTargetType.SECURITY));
  }

  public void testSimple() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.POSITION, "Pos");
    map.put(ComputationTargetType.SECURITY, "Sec");
    assertNull(map.getDirect(ComputationTargetType.NULL));
    assertNull(map.get(ComputationTargetType.NULL));
    assertEquals(map.getDirect(ComputationTargetType.POSITION), "Pos");
    assertEquals(map.get(ComputationTargetType.POSITION), "Pos");
    assertEquals(map.getDirect(ComputationTargetType.SECURITY), "Sec");
    assertEquals(map.get(ComputationTargetType.SECURITY), "Sec");
    assertEquals(map.getDirect(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)), "Sec");
    assertEquals(map.get(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)), "Sec");
    assertEquals(map.getDirect(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)), "Pos");
    assertEquals(map.get(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)), "Pos");
    assertNull(map.getDirect(ComputationTargetType.of(MockSecurity.class)));
    assertEquals(map.get(ComputationTargetType.of(MockSecurity.class)), "Sec");
    assertEquals(map.getDirect(ComputationTargetType.of(MockSecurity.class)), "Sec");
  }

  public void testNull() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.NULL, "NULL");
    map.put(ComputationTargetType.SECURITY, "Sec");
    assertEquals(map.getDirect(ComputationTargetType.NULL), "NULL");
    assertEquals(map.get(ComputationTargetType.NULL), "NULL");
  }

  public void testNested() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY), "Sec");
    assertNull(map.getDirect(ComputationTargetType.NULL));
    assertNull(map.get(ComputationTargetType.NULL));
    assertNull(map.getDirect(ComputationTargetType.POSITION));
    assertNull(map.get(ComputationTargetType.POSITION));
    assertEquals(map.getDirect(ComputationTargetType.SECURITY), "Sec");
    assertEquals(map.get(ComputationTargetType.SECURITY), "Sec");
    assertEquals(map.getDirect(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)), "Sec");
    assertEquals(map.get(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY)), "Sec");
    assertNull(map.getDirect(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)));
    assertNull(map.get(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)));
    assertEquals(map.getDirect(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)), "Sec");
    assertEquals(map.get(ComputationTargetType.PRIMITIVE.or(ComputationTargetType.SECURITY)), "Sec");
    assertNull(map.getDirect(ComputationTargetType.of(MockSecurity.class)));
    assertEquals(map.get(ComputationTargetType.of(MockSecurity.class)), "Sec");
    assertEquals(map.getDirect(ComputationTargetType.of(MockSecurity.class)), "Sec");
  }

  public void testMultiple() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE), "Pos|Trade");
    assertNull(map.getDirect(ComputationTargetType.NULL));
    assertNull(map.get(ComputationTargetType.NULL));
    assertNull(map.getDirect(ComputationTargetType.SECURITY));
    assertNull(map.get(ComputationTargetType.SECURITY));
    assertEquals(map.getDirect(ComputationTargetType.POSITION), "Pos|Trade");
    assertEquals(map.get(ComputationTargetType.POSITION), "Pos|Trade");
    assertEquals(map.getDirect(ComputationTargetType.TRADE), "Pos|Trade");
    assertEquals(map.get(ComputationTargetType.TRADE), "Pos|Trade");
    assertEquals(map.getDirect(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)), "Pos|Trade");
    assertEquals(map.get(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION)), "Pos|Trade");
    assertEquals(map.getDirect(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)), "Pos|Trade");
    assertEquals(map.get(ComputationTargetType.TRADE.or(ComputationTargetType.POSITION)), "Pos|Trade");
    assertNull(map.getDirect(ComputationTargetType.of(MockSecurity.class)));
    assertNull(map.get(ComputationTargetType.of(MockSecurity.class)));
  }

  public void testHierarchy() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.SECURITY, "SEC");
    map.put(ComputationTargetType.of(MockSecurity.class), "MOCK");
    final Class<? extends MockSecurity> c = new MockSecurity(0) {
      private static final long serialVersionUID = 1L;
    }.getClass();
    assertNull(map.getDirect(ComputationTargetType.of(c)));
    assertEquals(map.get(ComputationTargetType.of(c)), "MOCK");
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testCollision_direct() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE), "Pos|Trade");
    map.put(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION), "Pos");
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testCollision_cache() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.SECURITY, "Sec");
    assertNotNull(map.get(ComputationTargetType.of(MockSecurity.class)));
    map.put(ComputationTargetType.of(MockSecurity.class), "MSec");
  }

  public void testCollision_folding() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>(new BinaryOperator<String>() {
      @Override
      public String apply(final String a, final String b) {
        return a + b;
      }
    });
    map.put(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE), "A");
    map.put(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION), "B");
    assertEquals(map.get(ComputationTargetType.TRADE), "A");
    assertEquals(map.get(ComputationTargetType.POSITION), "AB");
  }

  public void testChained() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.SECURITY, "A");
    map.put(ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE.or(ComputationTargetType.of(MockSecurity.class))), "B",
        new BinaryOperator<String>() {
          @Override
          public String apply(final String a, final String b) {
            return a + b;
          }
        });
    assertEquals(map.get(ComputationTargetType.SECURITY), "A");
    assertEquals(map.get(ComputationTargetType.TRADE), "B");
    assertEquals(map.get(ComputationTargetType.of(MockSecurity.class)), "AB");
  }

  public void testValues_noNull() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.POSITION, "Pos");
    map.put(ComputationTargetType.SECURITY, "Sec");
    map.get(ComputationTargetType.SECURITY);
    map.get(ComputationTargetType.PORTFOLIO_NODE);
    int flags = 0;
    for (String str : map.values()) {
      if ("Pos".equals(str) && ((flags & 1) == 0)) {
        flags |= 1;
      } else if ("Sec".equals(str) && ((flags & 2) == 0)) {
        flags |= 2;
      } else {
        fail("str = " + str + ", flags = " + flags);
      }
    }
    assertEquals(flags, 3);
  }

  public void testValues_withNull() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.NULL, "Null");
    map.put(ComputationTargetType.POSITION, "Pos");
    map.get(ComputationTargetType.SECURITY);
    map.get(ComputationTargetType.PORTFOLIO_NODE);
    int flags = 0;
    for (String str : map.values()) {
      if ("Pos".equals(str) && ((flags & 1) == 0)) {
        flags |= 1;
      } else if ("Null".equals(str) && ((flags & 2) == 0)) {
        flags |= 2;
      } else {
        fail("str = " + str + ", flags = " + flags);
      }
    }
    assertEquals(flags, 3);
  }

  public void testValues_withRemove() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.NULL, "Null");
    map.put(ComputationTargetType.SECURITY, "Sec");
    map.get(ComputationTargetType.POSITION);
    map.get(ComputationTargetType.PORTFOLIO_NODE);
    int count = 0;
    final Iterator<String> itr = map.values().iterator();
    while (itr.hasNext()) {
      final String str = itr.next();
      assertTrue(str.equals("Null") || str.equals("Sec"));
      count++;
      itr.remove();
    }
    assertEquals(count, 2);
    assertEquals(map.get(ComputationTargetType.NULL), null);
    assertEquals(map.get(ComputationTargetType.SECURITY), null);
  }

  public void testEntries_noNull() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.POSITION, "Pos");
    map.put(ComputationTargetType.SECURITY, "Sec");
    map.get(ComputationTargetType.PORTFOLIO_NODE);
    int flags = 0;
    for (Map.Entry<ComputationTargetType, String> e : map.entries()) {
      if ("Pos".equals(e.getValue()) && ((flags & 1) == 0)) {
        assertEquals(e.getKey(), ComputationTargetType.POSITION);
        flags |= 1;
        e.setValue("Foo");
      } else if ("Sec".equals(e.getValue()) && ((flags & 2) == 0)) {
        assertEquals(e.getKey(), ComputationTargetType.SECURITY);
        flags |= 2;
        e.setValue("Bar");
      } else {
        fail("e = " + e + ", flags = " + flags);
      }
    }
    assertEquals(flags, 3);
    assertEquals(map.get(ComputationTargetType.POSITION), "Foo");
    assertEquals(map.get(ComputationTargetType.SECURITY), "Bar");
  }

  public void testEntries_withNull() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.NULL, "Null");
    map.put(ComputationTargetType.POSITION, "Pos");
    map.get(ComputationTargetType.SECURITY);
    map.get(ComputationTargetType.PORTFOLIO_NODE);
    int flags = 0;
    for (Map.Entry<ComputationTargetType, String> e : map.entries()) {
      if ("Pos".equals(e.getValue()) && ((flags & 1) == 0)) {
        assertEquals(e.getKey(), ComputationTargetType.POSITION);
        flags |= 1;
        e.setValue("Foo");
      } else if ("Null".equals(e.getValue()) && ((flags & 2) == 0)) {
        assertEquals(e.getKey(), ComputationTargetType.NULL);
        flags |= 2;
        e.setValue("Bar");
      } else {
        fail("e = " + e + ", flags = " + flags);
      }
    }
    assertEquals(flags, 3);
    assertEquals(map.get(ComputationTargetType.NULL), "Bar");
    assertEquals(map.get(ComputationTargetType.POSITION), "Foo");
  }

  public void testEntries_withRemove() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.NULL, "Null");
    map.put(ComputationTargetType.SECURITY, "Sec");
    map.get(ComputationTargetType.POSITION);
    map.get(ComputationTargetType.PORTFOLIO_NODE);
    int count = 0;
    final Iterator<Map.Entry<ComputationTargetType, String>> itr = map.entries().iterator();
    while (itr.hasNext()) {
      final Map.Entry<ComputationTargetType, String> str = itr.next();
      assertTrue(str.getValue().equals("Null") || str.getValue().equals("Sec"));
      count++;
      itr.remove();
    }
    assertEquals(count, 2);
    assertEquals(map.get(ComputationTargetType.NULL), null);
    assertEquals(map.get(ComputationTargetType.SECURITY), null);
  }

}
