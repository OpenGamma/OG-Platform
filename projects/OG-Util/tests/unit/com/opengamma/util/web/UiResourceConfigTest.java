/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.web;


import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

/**
 * 
 */
public class UiResourceConfigTest {

  @Test
  public void testUnmarshal() throws Exception {
    InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("uiResourceConfig.xml"));
    JAXBContext context = JAXBContext.newInstance(UiResourceConfig.class);
    Unmarshaller um = context.createUnmarshaller();
    UiResourceConfig conifg = (UiResourceConfig) um.unmarshal(reader);
    List<Bundle> bundles = conifg.getBundles();
    assertEquals(3, bundles.size());
    
    Bundle cssBundleCommon = new Bundle();
    cssBundleCommon.setId("cssBundleCommon");
    cssBundleCommon.setFragment(Arrays.asList("styles/common/og.common.buttons.css", "styles/common/og.common.core.css"));
    assertTrue(bundles.contains(cssBundleCommon));
    
    Bundle cssOther = new Bundle();
    cssOther.setId("cssOther");
    cssOther.setFragment(Arrays.asList("styles/lib/jquery/ui/jquery.ui.core.css", "styles/lib/jquery/ui/jquery.ui.datepicker.css"));
    assertTrue(bundles.contains(cssOther));
    
    Bundle jsBundleCommon = new Bundle();
    jsBundleCommon.setId("jsBundleCommon");
    jsBundleCommon.setFragment(Arrays.asList("scripts/og/common/og.common.core.js", "scripts/og/common/og.common.init.js", "scripts/og/og.common.jquery.rest.js"));
    assertTrue(bundles.contains(jsBundleCommon));
    
    List<File> files = conifg.getFiles();
    assertEquals(2, files.size());
    
    File cssCommon = new File();
    cssCommon.setId("styles/ogCommon");
    cssCommon.setSuffix("css");
    cssCommon.setBundle(Arrays.asList("cssBundleCommon", "cssOther"));
    assertTrue(files.contains(cssCommon));
    
    File jsCommon = new File();
    jsCommon.setId("scripts/ogCommon");
    jsCommon.setSuffix("js");
    jsCommon.setBundle(Arrays.asList("jsBundleCommon"));
    assertTrue(files.contains(cssCommon));
    
    reader.close();
  }

}
