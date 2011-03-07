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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test YUI Compression
 */
public class YUIBundleCompressorTest {

  private static final Logger s_logger = LoggerFactory.getLogger(YUIBundleCompressorTest.class);
  private static final String SCRIPTS_JS = "scripts.js";
  private YUIBundleCompressor _compressor;
  private BundleManager _bundleManager;

  /**
   * @throws java.lang.Exception
   */
  @Before
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

  /**
   * @return
   */
  private BundleManager createBundleManager() {
    String path = getClass().getResource(SCRIPTS_JS).getPath();
    BundleManager bundleManager = new BundleManager();
    Bundle bundle = new Bundle(SCRIPTS_JS);
    bundle.addChildNode(new Fragment(new File(path)));
    bundleManager.addBundle(bundle);
    return bundleManager;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void test() throws Exception {
    
    Bundle bundle = _bundleManager.getBundle(SCRIPTS_JS);
    List<Fragment> allFragment = bundle.getAllFragment();
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
