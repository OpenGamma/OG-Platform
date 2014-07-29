/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.convention;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.masterdb.bean.BeanMasterCallback;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;

/**
 * Callback used to configure the bean convention master.
 * <p>
 * Applications may subclass this class to change behavior.
 * Subclasses should have a no-argument public constructor.
 * <p>
 * If using standard OpenGamma convention classes, applications must extend {@link DefaultDbConventionBeanMasterCallback}.
 * Otherwise applications must extend this class.
 */
public class DbConventionBeanMasterCallback extends BeanMasterCallback<ConventionDocument, ManageableConvention> {

  @Override
  protected String getSqlTablePrefix() {
    return "cnv";
  }

  @Override
  protected String getMasterName() {
    return "Convention";
  }

  @Override
  protected Class<ManageableConvention> getRootType() {
    return ManageableConvention.class;
  }

  @Override
  protected ConventionDocument createDocument(ManageableConvention value) {
    return new ConventionDocument(value);
  }

  @Override
  protected String getName(ManageableConvention value) {
    return value.getName();
  }

  @Override
  protected ExternalIdBundle getExternalIdBundle(ManageableConvention value) {
    return value.getExternalIdBundle();
  }

  @Override
  protected Map<String, String> getAttributes(ManageableConvention value) {
    return value.getAttributes();
  }

  @Override
  protected Map<String, String> getIndexedProperties(ManageableConvention value) {
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
  protected char getMainType(ManageableConvention value) {
    return 'C';
  }

  @Override
  protected String getSubType(ManageableConvention value) {
    return value.getConventionType().getName();
  }

}
