/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.opengamma.web.bundle.BundleManager;
import com.opengamma.web.bundle.BundleParser;

/**
 * Creates a BundleManager for Production from the Bundle XML configuration file.
 */
public class BundleManagerFactoryBean extends AbstractBundleManagerFactoryBean {

  @Override
  protected BundleManager createObject() {
    InputStream xmlStream = getXMLStream();
    try {
      BundleManager manager = null;
      if (xmlStream != null) {
        BundleParser parser = new BundleParser();
        parser.setBaseDir(resolveBaseDir());
        manager = parser.parse(xmlStream);
      }
      return manager;
    } finally {
      IOUtils.closeQuietly(xmlStream);
    }
  }

}
