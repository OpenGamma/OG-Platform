/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecuritySearchRequest;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;

/**
 * Callback used to configure the bean security master.
 * <p>
 * Applications may subclass this class to change behavior.
 * Subclasses should have a no-argument public constructor.
 * <p>
 * If using standard OpenGamma security classes, applications must extend this class.
 * Otherwise applications must extend {@link DbSecurityBeanMasterCallback}.
 */
public class DefaultDbSecurityBeanMasterCallback extends DbSecurityBeanMasterCallback {

  /** Search key. */
  private static final String BOND_ISSUER_NAME = "bondIssuerName";
  /** Search key. */
  private static final String BOND_ISSUER_TYPE = "bondIssuerType";

  @Override
  protected Map<String, String> getIndexedProperties(ManageableSecurity value) {
    if (value instanceof BondSecurity) {
      BondSecurity bond = (BondSecurity) value;
      HashMap<String, String> map = Maps.newHashMapWithExpectedSize(2);
      if (bond.getIssuerName() != null) {
        map.put(BOND_ISSUER_NAME, bond.getIssuerName());
      }
      if (bond.getIssuerType() != null) {
        map.put(BOND_ISSUER_TYPE, bond.getIssuerType());
      }
      return map;
    }
    return super.getIndexedProperties(value);
  }

  @Override
  protected void buildIndexedPropertiesSearch(BeanMasterSearchRequest requestToBuild, AbstractSearchRequest requestToExtractFrom) {
    if (requestToExtractFrom instanceof BondSecuritySearchRequest) {
      BondSecuritySearchRequest extended = (BondSecuritySearchRequest) requestToExtractFrom;
      if (extended.getIssuerName() != null) {
        requestToBuild.getIndexedProperties().put(BOND_ISSUER_NAME, extended.getIssuerName());
      }
      if (extended.getIssuerType() != null) {
        requestToBuild.getIndexedProperties().put(BOND_ISSUER_TYPE, extended.getIssuerType());
      }
    }
    super.buildIndexedPropertiesSearch(requestToBuild, requestToExtractFrom);
  }

}
