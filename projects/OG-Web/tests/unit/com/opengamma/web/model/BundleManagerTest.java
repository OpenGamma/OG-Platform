/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.model;


import static com.opengamma.web.model.BundleTest.BUTTON_CSS;
import static com.opengamma.web.model.BundleTest.CORE_CSS;
import static com.opengamma.web.model.BundleTest.CORE_JS;
import static com.opengamma.web.model.BundleTest.CSS_UTIL_CSS;
import static com.opengamma.web.model.BundleTest.FRAG_A;
import static com.opengamma.web.model.BundleTest.INIT_JS;
import static com.opengamma.web.model.BundleTest.JQUERY_JS;
import static com.opengamma.web.model.BundleTest.JS_BUNDLE_COMMON_JS;
import static com.opengamma.web.model.BundleTest.LINKS_CSS;
import static com.opengamma.web.model.BundleTest.OG_COMMON_CSS;
import static com.opengamma.web.model.BundleTest.RESET_CSS;
import static com.opengamma.web.model.BundleTest.makeCssBundleCommon;
import static com.opengamma.web.model.BundleTest.makeCssUtil;
import static com.opengamma.web.model.BundleTest.makejsBundleCommon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Test BundleManager
 */
public class BundleManagerTest {

  private final BundleManager _manager = new BundleManager();
  
  @Test
  public void test_addBundle() {
    
    _manager.addBundle(makeCssBundleCommon());
    _manager.addBundle(makeCssUtil());
    _manager.addBundle(makejsBundleCommon());
      
    Bundle bundle = _manager.getBundle(OG_COMMON_CSS);
    assertNotNull(bundle);
    assertEquals(expectedCssBundleFragments(), bundle.getAllFragment());
    
    bundle = _manager.getBundle(CSS_UTIL_CSS);
    assertNotNull(bundle);
    assertEquals(expectedCssUtilFragments(), bundle.getAllFragment());
    
    bundle = _manager.getBundle(JS_BUNDLE_COMMON_JS);
    assertNotNull(bundle);
    assertEquals(expectedJsBundleFragments(), bundle.getAllFragment());
    
  }

  private List<Fragment> expectedJsBundleFragments() {
    List<Fragment> expectedList = Lists.newArrayList(CORE_JS, INIT_JS, JQUERY_JS);
    return expectedList;
  }

  private List<Fragment> expectedCssUtilFragments() {
    List<Fragment> expectedList = Lists.newArrayList(RESET_CSS, LINKS_CSS);
    return expectedList;
  }

  private List<Fragment> expectedCssBundleFragments() {
    List<Fragment> expectedList = Lists.newArrayList(FRAG_A, BUTTON_CSS, CORE_CSS);
    return expectedList;
  }


}
