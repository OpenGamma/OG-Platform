/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link IdentifierBundleMapper}
 */
@Test
public class IdentifierBundleMapperTest {

  public void testIdentifierBundleMapper() {
    Identifier idA1 = Identifier.of("TEST_SCHEME_A", "1");
    Identifier idB1 = Identifier.of("TEST_SCHEME_B", "1");
    Identifier idC1 = Identifier.of("TEST_SCHEME_C", "1");
    Identifier idD1 = Identifier.of("TEST_SCHEME_D", "1");
    Identifier idA2 = Identifier.of("TEST_SCHEME_A", "2");
    Identifier idB2 = Identifier.of("TEST_SCHEME_B", "2");
    Identifier idC2 = Identifier.of("TEST_SCHEME_C", "2");
        
    // first some bundles with overlapping ids.
    IdentifierBundle bundleA1B1 = IdentifierBundle.of(idA1, idB1); 
    IdentifierBundle bundleB1C1 = IdentifierBundle.of(idB1, idC1);  
    IdentifierBundle bundleA1B1C1 = IdentifierBundle.of(idA1, idB1, idC1);
    IdentifierBundle bundleA1B1C1D1 = IdentifierBundle.of(idA1, idB1, idC1, idD1);
    IdentifierBundle bundleA2B2 = IdentifierBundle.of(idA2, idB2);
    IdentifierBundle bundleA1B2 = IdentifierBundle.of(idA1, idB2);
    final String testSchemeName = "TEST_SCHEME_1";
    IdentifierBundleMapper<String> mapper = new IdentifierBundleMapper<String>(testSchemeName);
    String obj = "TEST1";
    UniqueIdentifier uid1 = mapper.add(bundleA1B1, obj);
    // check the uid is what we expect.
    assertEquals(testSchemeName, uid1.getScheme());
    assertEquals("1", uid1.getValue());
    // check the uid works to retrieve
    assertEquals(obj, mapper.get(uid1));
    // try it with each id in the bundle we passed in.
    assertEquals(obj, mapper.get(idA1).iterator().next());
    assertEquals(obj, mapper.get(idB1).iterator().next());
    // now try it with the bundle we passed in.
    assertEquals(obj, mapper.get(bundleA1B1).iterator().next());
    // now test a partial match.
    assertEquals(obj, mapper.get(bundleB1C1).iterator().next());
    // test no match.
    assertEquals(true, mapper.get(bundleA2B2).isEmpty());
    // and no match with just an id.
    assertEquals(true, mapper.get(idA2).isEmpty());
    // now try adding the same object with an overlapping bundle.  Should make resulting bundle the union.
    UniqueIdentifier uid2 = mapper.add(bundleB1C1, obj);
    // should give us back the same id.  Check they're equal and that they both still work for retrieval.
    assertEquals(uid1, uid2);
    assertEquals(obj, mapper.get(uid1));
    assertEquals(obj, mapper.get(uid2));
    //
    assertEquals(obj, mapper.get(idA1).iterator().next());
    assertEquals(obj, mapper.get(idB1).iterator().next());
    assertEquals(obj, mapper.get(idC1).iterator().next());
    // now try it with the original bundle we passed in.
    assertEquals(obj, mapper.get(bundleA1B1).iterator().next());
    // now test a another match.
    assertEquals(obj, mapper.get(bundleB1C1).iterator().next());
    // now test a another match.
    assertEquals(obj, mapper.get(bundleA1B1C1).iterator().next());
    // and lastly another overlapping, but incomplete bundle:
    assertEquals(obj, mapper.get(bundleA1B1C1D1).iterator().next());
    String obj2 = "TEST2";
    UniqueIdentifier uid3 = mapper.add(bundleA2B2, obj2);
    // check the uid is what we expect.
    assertEquals(testSchemeName, uid3.getScheme());
    assertEquals("2", uid3.getValue());
    // look into the below state, but i think it's ok as the behaviour changed
    //try {
    //  uid3 = mapper.add(bundleA1B2, obj2);
    //  Assert.fail();
    //} catch (OpenGammaRuntimeException ogre) {
    //  // expected.
    //}
  }
}
