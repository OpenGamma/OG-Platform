/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

/**
 * Test DevBundleBuilder
 */
public class DevBundleBuilderTest {

  @Test
  public void test_builder() throws Exception {
       
    BundleManager bundleManager = new BundleManager();
    
    Bundle testBundle = new Bundle("A.css");
    for (int i = 1; i <= 100; i++) {
      testBundle.addChildNode(new Fragment(new File("A" + i + ".css")));
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
    assertEquals(testBundle.getAllFragment(), bundle.getAllFragment());
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
