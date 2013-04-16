/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleMapperTest {

  public void testMapper() {
    ExternalId idA1 = ExternalId.of("TEST_SCHEME_A", "1");
    ExternalId idB1 = ExternalId.of("TEST_SCHEME_B", "1");
    ExternalId idC1 = ExternalId.of("TEST_SCHEME_C", "1");
    ExternalId idD1 = ExternalId.of("TEST_SCHEME_D", "1");
    ExternalId idA2 = ExternalId.of("TEST_SCHEME_A", "2");
    ExternalId idB2 = ExternalId.of("TEST_SCHEME_B", "2");
    
    // first some bundles with overlapping ids.
    ExternalIdBundle bundleA1B1 = ExternalIdBundle.of(idA1, idB1); 
    ExternalIdBundle bundleB1C1 = ExternalIdBundle.of(idB1, idC1);  
    ExternalIdBundle bundleA1B1C1 = ExternalIdBundle.of(idA1, idB1, idC1);
    ExternalIdBundle bundleA1B1C1D1 = ExternalIdBundle.of(idA1, idB1, idC1, idD1);
    ExternalIdBundle bundleA2B2 = ExternalIdBundle.of(idA2, idB2);
    final String testSchemeName = "TEST_SCHEME_1";
    ExternalIdBundleMapper<String> mapper = new ExternalIdBundleMapper<String>(testSchemeName);
    String obj = "TEST1";
    UniqueId uniqueId1 = mapper.add(bundleA1B1, obj);
    // check the uniqueId is what we expect.
    assertEquals(testSchemeName, uniqueId1.getScheme());
    assertEquals("1", uniqueId1.getValue());
    // check the uniqueId works to retrieve
    assertEquals(obj, mapper.get(uniqueId1));
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
    UniqueId uniqueId2 = mapper.add(bundleB1C1, obj);
    // should give us back the same id.  Check they're equal and that they both still work for retrieval.
    assertEquals(uniqueId1, uniqueId2);
    assertEquals(obj, mapper.get(uniqueId1));
    assertEquals(obj, mapper.get(uniqueId2));
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
    UniqueId uniqueId3 = mapper.add(bundleA2B2, obj2);
    // check the uniqueId is what we expect.
    assertEquals(testSchemeName, uniqueId3.getScheme());
    assertEquals("2", uniqueId3.getValue());
  }

}
