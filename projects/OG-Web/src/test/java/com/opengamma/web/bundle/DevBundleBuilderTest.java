/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.net.URI;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test DevBundleBuilder.
 */
@Test(groups = TestGroup.UNIT)
public class DevBundleBuilderTest {

  public void test_builder() throws Exception {
    BundleManager bundleManager = new BundleManager();
    
    Bundle testBundle = new Bundle("A.css");
    for (int i = 1; i <= 100; i++) {
      URI uri = new URI("A" + i + ".css");
      testBundle.addChildNode(new Fragment(uri, "/" + uri.toString()));
    }
    bundleManager.addBundle(testBundle);
    
    DevBundleBuilder devBundleBuilder = new DevBundleBuilder(bundleManager);
    BundleManager devBundleManager = devBundleBuilder.getDevBundleManager();
    
    assertNotNull(devBundleManager);
    
    Bundle bundle = devBundleManager.getBundle("A.css");
    assertEquals("A.css", bundle.getId());
    List<BundleNode> childNodes = bundle.getChildNodes();
    assertTrue(childNodes.size() <= DevBundleBuilder.MAX_IMPORTS);
    for (BundleNode bundleNode : childNodes) {
      assertBundleNode(bundleNode);
    }
    assertEquals(testBundle.getAllFragments(), bundle.getAllFragments());
  }

  private void assertBundleNode(BundleNode bundleNode) {
    if (bundleNode instanceof Bundle) {
      Bundle testBundle = (Bundle) bundleNode;
      List<BundleNode> childNodes = testBundle.getChildNodes();
      assertTrue(childNodes.size() <= DevBundleBuilder.MAX_IMPORTS);
      for (BundleNode childNode : childNodes) {
        assertBundleNode(childNode);
      }
    }
  }

}
