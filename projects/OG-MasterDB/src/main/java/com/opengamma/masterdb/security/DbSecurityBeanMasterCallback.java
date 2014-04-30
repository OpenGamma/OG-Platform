/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.masterdb.bean.BeanMasterCallback;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;

/**
 * Callback used to configure the bean security master.
 * <p>
 * Applications may subclass this class to change behavior.
 * Subclasses should have a no-argument public constructor.
 * <p>
 * If using standard OpenGamma security classes, applications must extend {@link DefaultDbSecurityBeanMasterCallback}.
 * Otherwise applications must extend this class.
 */
public class DbSecurityBeanMasterCallback extends BeanMasterCallback<SecurityDocument, ManageableSecurity> {

  @Override
  protected String getSqlTablePrefix() {
    return "secb";
  }

  @Override
  protected String getMasterName() {
    return "Security";
  }

  @Override
  protected Class<ManageableSecurity> getRootType() {
    return ManageableSecurity.class;
  }

  @Override
  protected SecurityDocument createDocument(ManageableSecurity value) {
    return new SecurityDocument(value);
  }

  @Override
  protected String getName(ManageableSecurity value) {
    return value.getName();
  }

  @Override
  protected ExternalIdBundle getExternalIdBundle(ManageableSecurity value) {
    return value.getExternalIdBundle();
  }

  @Override
  protected Map<String, String> getAttributes(ManageableSecurity value) {
    return value.getAttributes();
  }

  @Override
  protected Map<String, String> getIndexedProperties(ManageableSecurity value) {
    return ImmutableMap.of();
  }

  /**
   * Builds the indexed properties to search for.
   * 
   * @param requestToBuild  the request to set search properties into, not null
   * @param requestToExtractFrom  the request to extract indexed properties from, not null
   */
  protected void buildIndexedPropertiesSearch(BeanMasterSearchRequest requestToBuild, AbstractSearchRequest requestToExtractFrom) {
    return;
  }

  @Override
  protected char getMainType(ManageableSecurity value) {
    return 'S';
  }

  @Override
  protected String getSubType(ManageableSecurity value) {
    return value.getSecurityType();
  }

}
