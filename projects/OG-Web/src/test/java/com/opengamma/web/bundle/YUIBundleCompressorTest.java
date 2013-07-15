/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test YUI Compression.
 */
@Test(groups = TestGroup.UNIT, enabled = false)
public class YUIBundleCompressorTest {

  private static final Logger s_logger = LoggerFactory.getLogger(YUIBundleCompressorTest.class);
  private static final String SCRIPTS_JS = "scripts.js";
  private YUIBundleCompressor _compressor;
  private Bundle _bundle;

  @BeforeMethod
  public void setUp() throws Exception {
    _bundle = createBundle();
    _compressor = createCompressor();
  }

  private YUIBundleCompressor createCompressor() {
    YUICompressorOptions compressorOptions = new YUICompressorOptions();
    compressorOptions.setLineBreakPosition(-1);
    compressorOptions.setMunge(false);
    compressorOptions.setPreserveAllSemiColons(true);
    compressorOptions.setOptimize(true);
    compressorOptions.setWarn(false);
    return new YUIBundleCompressor(compressorOptions);
  }

  private Bundle createBundle() throws URISyntaxException {
    URI scriptsResource = getClass().getResource(SCRIPTS_JS).toURI();
    Bundle bundle = new Bundle(SCRIPTS_JS);
    bundle.addChildNode(new Fragment(scriptsResource, ""));
    return bundle;
  }

  public void test() throws Exception {
    List<Fragment> allFragment = _bundle.getAllFragments();
    assertNotNull(allFragment);
    assertEquals(1, allFragment.size());

    Fragment fragment = allFragment.get(0);
    String uncompressed = IOUtils.toString(fragment.getUri());
    assertNotNull(uncompressed);
    s_logger.debug("uncompressed length {}", uncompressed.length());
    assertEquals(853389, uncompressed.length());

    String compressed = _compressor.compressBundle(_bundle);
    assertNotNull(compressed);
    s_logger.debug("compressed length {}", compressed.length());
    assertEquals(492128, compressed.length());

  }

}
