/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import java.util.EnumMap;
import java.util.Map;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.masterdb.bean.AbstractDelegatingBeanMaster;
import com.opengamma.masterdb.bean.BeanMasterCallback;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;
import com.opengamma.masterdb.bean.DbBeanMaster;
import com.opengamma.util.db.DbConnector;

/**
 * A security master implementation based on Joda-Beans using a database for persistence.
 * <p>
 * This is a full implementation of the security master using an SQL database.
 * Data is stored based on the Joda-Beans API.
 * Full details of the API are in {@link SecurityMaster}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbSecurityBeanMaster
    extends AbstractDelegatingBeanMaster<SecurityDocument, ManageableSecurity>
    implements SecurityMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbSec";

  /**
   * SQL order by.
   */
  protected static final EnumMap<SecuritySearchSortOrder, String> ORDER_BY_MAP = new EnumMap<SecuritySearchSortOrder, String>(SecuritySearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(SecuritySearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.NAME_DESC, "name DESC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.SECURITY_TYPE_ASC, "sub_type ASC");
    ORDER_BY_MAP.put(SecuritySearchSortOrder.SECURITY_TYPE_DESC, "sub_type DESC");
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbSecurityBeanMaster(final DbConnector dbConnector) {
    super(new DbBeanMaster<>(dbConnector, IDENTIFIER_SCHEME_DEFAULT, new Callback()));
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityMetaDataResult metaData(SecurityMetaDataRequest request) {
    SecurityMetaDataResult result = new SecurityMetaDataResult();
    if (request.isSecurityTypes()) {
      result.setSecurityTypes(getDelegate().getAllSubTypes());
    }
    if (request.isSchemaVersion()) {
      result.setSchemaVersion(getDelegate().getSchemaVersionString());
    }
    return result;
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    BeanMasterSearchRequest delegatedRequest = new BeanMasterSearchRequest();
    delegatedRequest.setPagingRequest(request.getPagingRequest());
    delegatedRequest.setVersionCorrection(request.getVersionCorrection());
    delegatedRequest.setObjectIds(request.getObjectIds());
    delegatedRequest.setExternalIdSearch(request.getExternalIdSearch());
    delegatedRequest.setExternalIdValue(request.getExternalIdValue());
    delegatedRequest.setExternalIdScheme(request.getExternalIdScheme());
    delegatedRequest.setAttributes(request.getAttributes());
    delegatedRequest.setName(request.getName());
    delegatedRequest.setSubType(request.getSecurityType());
    delegatedRequest.setSortOrderSql(ORDER_BY_MAP.get(request.getSortOrder()));
    return getDelegate().search(delegatedRequest, new SecuritySearchResult());
  }

  @Override
  public SecurityHistoryResult history(SecurityHistoryRequest request) {
    return getDelegate().history(request, new SecurityHistoryResult());
  }

  //-------------------------------------------------------------------------
  /**
   * Callback.
   */
  private static final class Callback extends BeanMasterCallback<SecurityDocument, ManageableSecurity> {
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
    protected char getMainType(ManageableSecurity value) {
      return 'S';
    }
    @Override
    protected String getSubType(ManageableSecurity value) {
      return value.getSecurityType();
    }
  }

}
