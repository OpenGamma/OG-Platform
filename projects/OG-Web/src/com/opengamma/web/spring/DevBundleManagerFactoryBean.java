/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.web.bundle.BundleManager;
import com.opengamma.web.bundle.BundleParser;
import com.opengamma.web.bundle.DevBundleBuilder;


/**
 * Creates a BundleManager for Production from the Bundle XML configuration file
 */
public class DevBundleManagerFactoryBean extends AbstractBundleManagerFactoryBean {
  
  @Override
  protected BundleManager createObject() {
    DevBundleBuilder builder = new DevBundleBuilder(new BundleParser(resolveConfigurationFile(), resolveBaseDir()).getBundleManager());
    return builder.getDevBundleManager();
  }

}
