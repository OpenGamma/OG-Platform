/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test YUI Compression.
 */
@Test
public class YUIBundleCompressorTest {

  private static final Logger s_logger = LoggerFactory.getLogger(YUIBundleCompressorTest.class);
  private static final String SCRIPTS_JS = "scripts.js";
  private YUIBundleCompressor _compressor;
  private BundleManager _bundleManager;

  @BeforeMethod
  public void setUp() throws Exception {
    _bundleManager = createBundleManager();
    _compressor = createCompressor();
  }

  private YUIBundleCompressor createCompressor() {
    YUICompressorOptions compressorOptions = new YUICompressorOptions();
    compressorOptions.setLineBreakPosition(-1);
    compressorOptions.setMunge(false);
    compressorOptions.setPreserveAllSemiColons(true);
    compressorOptions.setOptimize(true);
    compressorOptions.setWarn(false);
    return new YUIBundleCompressor(_bundleManager, compressorOptions);
  }

  private BundleManager createBundleManager() {
    String path = getClass().getResource(SCRIPTS_JS).getPath();
    BundleManager bundleManager = new BundleManager();
    Bundle bundle = new Bundle(SCRIPTS_JS);
    bundle.addChildNode(new Fragment(new File(path)));
    bundleManager.addBundle(bundle);
    return bundleManager;
  }

  public void test() throws Exception {
    Bundle bundle = _bundleManager.getBundle(SCRIPTS_JS);
    List<Fragment> allFragment = bundle.getAllFragments();
    assertNotNull(allFragment);
    assertEquals(1, allFragment.size());
    
    Fragment fragment = allFragment.get(0);
    fragment.getFile();
    String uncompressed = FileUtils.readFileToString(fragment.getFile());
    assertNotNull(uncompressed);
    s_logger.debug("uncompressed length {}", uncompressed.length());
    
    String compressed = _compressor.getBundle(SCRIPTS_JS);
    assertNotNull(compressed);
    s_logger.debug("compressed length {}", compressed.length());
    
    assertTrue(uncompressed.length() > compressed.length());
  }

}
