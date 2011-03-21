/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import java.io.File;
import java.util.List;

import com.opengamma.web.bundle.BundleManager;
import com.opengamma.web.bundle.BundleParser;
import com.opengamma.web.bundle.Fragment;

/**
 * Test BundleParser.
 */
@Test
public class BundleParserTest {

  public void testParser() throws Exception {
    File xmlFile = new File(getClass().getResource("uiResourceConfig.xml").getPath());
    
    BundleParser bundleParser = new BundleParser(xmlFile);
    BundleManager bundleManager = bundleParser.getBundleManager();
    assertNotNull(bundleManager);
    
    List<Fragment> cssBundleCommon = bundleManager.getBundle("cssBundleCommon.css").getAllFragment();
    assertNotNull(cssBundleCommon);
    assertEquals(2, cssBundleCommon.size());
    assertEquals(new Fragment(new File("styles/common/og.common.buttons.css")), cssBundleCommon.get(0));
    assertEquals(new Fragment(new File("styles/common/og.common.core.css")), cssBundleCommon.get(1));
    
    List<Fragment> cssUtil = bundleManager.getBundle("cssUtil.css").getAllFragment();
    assertNotNull(cssUtil);
    assertEquals(2, cssUtil.size());
    assertEquals(new Fragment(new File("styles/common/util/og.common.reset.css")), cssUtil.get(0));
    assertEquals(new Fragment(new File("styles/common/util/og.common.links.css")), cssUtil.get(1));
    
    List<Fragment> jsBundleCommon = bundleManager.getBundle("jsBundleCommon.js").getAllFragment();
    assertNotNull(jsBundleCommon);
    assertEquals(3, jsBundleCommon.size());
    assertEquals(new Fragment(new File("scripts/og/common/og.common.core.js")), jsBundleCommon.get(0));
    assertEquals(new Fragment(new File("scripts/og/common/og.common.init.js")), jsBundleCommon.get(1));
    assertEquals(new Fragment(new File("scripts/og/common/og.common.jquery.rest.js")), jsBundleCommon.get(2));
    
    List<Fragment> cssOgCommon = bundleManager.getBundle("ogCommon.css").getAllFragment();
    assertNotNull(cssOgCommon);
    assertEquals(cssBundleCommon.size() + cssUtil.size(), cssOgCommon.size());
    int i = 0;
    for (Fragment fragment : cssBundleCommon) {
      assertEquals(fragment, cssOgCommon.get(i++));
    }
    for (Fragment fragment : cssUtil) {
      assertEquals(fragment, cssOgCommon.get(i++));
    }
    
    List<Fragment> jsOgCommon = bundleManager.getBundle("ogCommon.js").getAllFragment();
    assertNotNull(jsOgCommon);
    assertEquals(jsBundleCommon.size(), jsOgCommon.size());
    int j = 0;
    for (Fragment fragment : jsBundleCommon) {
      assertEquals(fragment, jsOgCommon.get(j++));
    }    
  }

}
