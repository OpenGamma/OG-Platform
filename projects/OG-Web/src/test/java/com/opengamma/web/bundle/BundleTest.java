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
import java.net.URISyntaxException;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/**
 * Test Bundle.
 */
@Test(groups = TestGroup.UNIT)
public class BundleTest {

  static final String JS_BUNDLE_COMMON_JS = "jsBundleCommon.js";
  static final String CSS_UTIL_CSS = "cssUtil.css";
  static final String OG_COMMON_CSS = "ogCommon.css";

  //cssBundleCommon fragments
  static final Fragment BUTTON_CSS = new Fragment(createUri("styles/common/og.common.buttons.css"), "/styles/common/og.common.buttons.css");
  static final Fragment CORE_CSS = new Fragment(createUri("styles/common/og.common.core.css"), "/styles/common/og.common.core.css");

  //cssUtil fragments
  static final Fragment RESET_CSS = new Fragment(createUri("styles/common/util/og.common.reset.css"), "/styles/common/util/og.common.reset.css");
  static final Fragment LINKS_CSS = new Fragment(createUri("styles/common/util/og.common.links.css"), "/styles/common/util/og.common.links.css");

  //jsBundleCommon fragments
  static final Fragment CORE_JS = new Fragment(createUri("scripts/og/common/og.common.core.js"), "/scripts/og/common/og.common.core.js");
  static final Fragment INIT_JS = new Fragment(createUri("scripts/og/common/og.common.init.js"), "/scripts/og/common/og.common.init.js");
  static final Fragment JQUERY_JS = new Fragment(createUri("scripts/og/common/og.common.jquery.rest.js"), "/scripts/og/common/og.common.jquery.rest.js");

  static final Fragment FRAG_A = new Fragment(createUri("A"), "/A");
  private static final Fragment FRAG_B = new Fragment(createUri("B"), "/B");
  private static final Fragment FRAG_C = new Fragment(createUri("C"), "/C");
  private static final Fragment FRAG_D = new Fragment(createUri("D"), "/D");
  
  private static URI createUri(String resource) {
    try {
      return new URI(resource);
    } catch (URISyntaxException e) {
      throw new OpenGammaRuntimeException("Invalid URI for resource " + resource, e);
    }
  }

  public void test_fragments_only() throws Exception {
    Bundle cssBundleCommon = makeCssBundleCommon();    
    List<Fragment> allFragment = cssBundleCommon.getAllFragments();
    assertNotNull(allFragment);
    assertTrue(allFragment.size() == 3);
    assertEquals(FRAG_A, allFragment.get(0));
    assertEquals(BUTTON_CSS, allFragment.get(1));
    assertEquals(CORE_CSS, allFragment.get(2));
  }

  public void test_bundles_only() throws Exception {
    Bundle test = new Bundle();
    test.addChildNode(makeCssBundleCommon());
    test.addChildNode(makeCssUtil());
    List<Fragment> allFragment = test.getAllFragments();
    assertNotNull(allFragment);
    assertTrue(allFragment.size() == 5);
    assertEquals(FRAG_A, allFragment.get(0));
    assertEquals(BUTTON_CSS, allFragment.get(1));
    assertEquals(CORE_CSS, allFragment.get(2));
    assertEquals(RESET_CSS, allFragment.get(3));
    assertEquals(LINKS_CSS, allFragment.get(4));
  }

  public void test_fragments_bundle() throws Exception {
    Bundle test = new Bundle();
    //add fragment
    test.addChildNode(FRAG_B);
    Bundle cssBundleCommon = makeCssBundleCommon();
    //add fragment to bundle
    cssBundleCommon.addChildNode(FRAG_C);
    
    //add bundle
    test.addChildNode(cssBundleCommon);
    //add bundle
    test.addChildNode(makeCssUtil());
    //add fragment
    test.addChildNode(FRAG_D);
    
    List<Fragment> allFragment = test.getAllFragments();
    assertNotNull(allFragment);
    assertTrue(allFragment.size() == 8);
    assertEquals(FRAG_B, allFragment.get(0));
    assertEquals(FRAG_A, allFragment.get(1));
    assertEquals(BUTTON_CSS, allFragment.get(2));
    assertEquals(CORE_CSS, allFragment.get(3));
    assertEquals(FRAG_C, allFragment.get(4));
    assertEquals(RESET_CSS, allFragment.get(5));
    assertEquals(LINKS_CSS, allFragment.get(6));
    assertEquals(FRAG_D, allFragment.get(7));
  }

  static Bundle makeCssBundleCommon() {
    Bundle cssBundleCommon = new Bundle(OG_COMMON_CSS);
    cssBundleCommon.addChildNode(FRAG_A);
    cssBundleCommon.addChildNode(BUTTON_CSS);
    cssBundleCommon.addChildNode(CORE_CSS);
    return cssBundleCommon;
  }

  static Bundle makeCssUtil() {
    Bundle cssUtil = new Bundle(CSS_UTIL_CSS);
    cssUtil.addChildNode(RESET_CSS);
    cssUtil.addChildNode(LINKS_CSS);
    return cssUtil;
  }

  static Bundle makejsBundleCommon() {
    Bundle jsBundleCommon = new Bundle(JS_BUNDLE_COMMON_JS);
    jsBundleCommon.addChildNode(CORE_JS);
    jsBundleCommon.addChildNode(INIT_JS);
    jsBundleCommon.addChildNode(JQUERY_JS);
    return jsBundleCommon;
  }

}
