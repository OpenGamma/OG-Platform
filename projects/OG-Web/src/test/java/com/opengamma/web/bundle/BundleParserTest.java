/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/**
 * Test BundleParser.
 */
@Test(groups = TestGroup.UNIT)
public class BundleParserTest {

  public void testParser() throws Exception {
    InputStream xmlStream = getClass().getResourceAsStream("uiResourceConfig.xml");
    
    UriProvider uriProvider = new UriProvider() {

      @Override
      public URI getUri(String resourceReference) {
        try {
          return new URI(resourceReference);
        } catch (URISyntaxException ex) {
          throw new OpenGammaRuntimeException("Invalid URI for resource " + resourceReference);
        }
      }
      
    };
    BundleParser bundleParser = new BundleParser(uriProvider, "");
    BundleManager bundleManager = bundleParser.parse(xmlStream);
    assertNotNull(bundleManager);
    
    List<Fragment> cssBundleCommon = bundleManager.getBundle("cssBundleCommon.css").getAllFragments();
    assertNotNull(cssBundleCommon);
    assertEquals(2, cssBundleCommon.size());
    assertEquals(new Fragment(new URI("styles/common/og.common.buttons.css"), "/styles/common/og.common.buttons.css"), cssBundleCommon.get(0));
    assertEquals(new Fragment(new URI("styles/common/og.common.core.css"), "/styles/common/og.common.core.css"), cssBundleCommon.get(1));
    
    List<Fragment> cssUtil = bundleManager.getBundle("cssUtil.css").getAllFragments();
    assertNotNull(cssUtil);
    assertEquals(2, cssUtil.size());
    assertEquals(new Fragment(new URI("styles/common/util/og.common.reset.css"), "/styles/common/util/og.common.reset.css"), cssUtil.get(0));
    assertEquals(new Fragment(new URI("styles/common/util/og.common.links.css"), "/styles/common/util/og.common.links.css"), cssUtil.get(1));
    
    List<Fragment> jsBundleCommon = bundleManager.getBundle("jsBundleCommon.js").getAllFragments();
    assertNotNull(jsBundleCommon);
    assertEquals(3, jsBundleCommon.size());
    assertEquals(new Fragment(new URI("scripts/og/common/og.common.core.js"), "/scripts/og/common/og.common.core.js"), jsBundleCommon.get(0));
    assertEquals(new Fragment(new URI("scripts/og/common/og.common.init.js"), "/scripts/og/common/og.common.init.js"), jsBundleCommon.get(1));
    assertEquals(new Fragment(new URI("scripts/og/common/og.common.jquery.rest.js"), "/scripts/og/common/og.common.jquery.rest.js"), jsBundleCommon.get(2));
    
    List<Fragment> cssOgCommon = bundleManager.getBundle("ogCommon.css").getAllFragments();
    assertNotNull(cssOgCommon);
    assertEquals(cssBundleCommon.size() + cssUtil.size(), cssOgCommon.size());
    int i = 0;
    for (Fragment fragment : cssBundleCommon) {
      assertEquals(fragment, cssOgCommon.get(i++));
    }
    for (Fragment fragment : cssUtil) {
      assertEquals(fragment, cssOgCommon.get(i++));
    }
    
    List<Fragment> jsOgCommon = bundleManager.getBundle("ogCommon.js").getAllFragments();
    assertNotNull(jsOgCommon);
    assertEquals(jsBundleCommon.size(), jsOgCommon.size());
    int j = 0;
    for (Fragment fragment : jsBundleCommon) {
      assertEquals(fragment, jsOgCommon.get(j++));
    }    
    
    IOUtils.closeQuietly(xmlStream);
  }

}
