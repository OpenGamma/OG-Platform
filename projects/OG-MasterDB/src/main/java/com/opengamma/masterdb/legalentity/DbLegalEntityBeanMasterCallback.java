/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.legalentity;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.masterdb.bean.BeanMasterCallback;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;

/**
 * Callback used to configure the bean legal entity master.
 * <p/>
 * Applications may subclass this class to change behavior.
 * Subclasses should have a no-argument public constructor.
 * <p/>
 * If using standard OpenGamma legal entity classes, applications must extend {@link DefaultDbLegalEntityBeanMasterCallback}.
 * Otherwise applications must extend this class.
 */
public class DbLegalEntityBeanMasterCallback extends BeanMasterCallback<LegalEntityDocument, ManageableLegalEntity> {

  @Override
  protected String getSqlTablePrefix() {
    return "len";
  }

  @Override
  protected String getMasterName() {
    return "LegalEntity";
  }

  @Override
  protected Class<ManageableLegalEntity> getRootType() {
    return ManageableLegalEntity.class;
  }

  @Override
  protected LegalEntityDocument createDocument(ManageableLegalEntity value) {
    return new LegalEntityDocument(value);
  }

  @Override
  protected String getName(ManageableLegalEntity value) {
    return value.getName();
  }

  @Override
  protected ExternalIdBundle getExternalIdBundle(ManageableLegalEntity value) {
    return value.getExternalIdBundle();
  }

  @Override
  protected Map<String, String> getAttributes(ManageableLegalEntity value) {
    return value.getAttributes();
  }

  @Override
  protected Map<String, String> getIndexedProperties(ManageableLegalEntity value) {
    return ImmutableMap.of();
  }

  /**
   * Builds the indexed properties to search for.
   *
   * @param requestToBuild       the request to set search properties into, not null
   * @param requestToExtractFrom the request to extract indexed properties from, not null
   */
  protected void buildIndexedPropertiesSearch(BeanMasterSearchRequest requestToBuild, AbstractSearchRequest requestToExtractFrom) {
    return;
  }

  @Override
  protected char getMainType(ManageableLegalEntity value) {
    return 'L';
  }

  @Override
  protected String getSubType(ManageableLegalEntity value) {
    return "com.opengamma.core.legalentity.LegalEntity"; //TODO ask Stephen what to do with bean without subtypes
  }

}
