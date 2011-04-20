/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.server;

import com.opengamma.web.bundle.BundleManager;
import com.opengamma.web.bundle.BundleParser;

/**
 * Creates a BundleManager for Production from the Bundle XML configuration file
 */
public class ProdBundleManagerFactoryBean extends AbstractBundleManagerFactoryBean {
    
  @Override
  protected BundleManager createObject() {
    BundleParser bundleParser = new BundleParser(resolveConfigurationFile(), resolveBaseDir());
    return bundleParser.getBundleManager();
  }

}
