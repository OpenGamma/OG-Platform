/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.convention;

import java.util.EnumMap;

import com.opengamma.core.convention.ConventionType;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ConventionSearchSortOrder;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.masterdb.ConfigurableDbChangeProvidingMaster;
import com.opengamma.masterdb.bean.AbstractDelegatingBeanMaster;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;
import com.opengamma.masterdb.bean.DbBeanMaster;
import com.opengamma.util.db.DbConnector;

/**
 * A convention master implementation based on Joda-Beans using a database for persistence.
 * <p>
 * This is a full implementation of the convention master using an SQL database.
 * Data is stored based on the Joda-Beans API.
 * Full details of the API are in {@link ConventionMaster}.
 * <p>
 * Applications can configure this master using the callback class passed in.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbConventionBeanMaster
    extends AbstractDelegatingBeanMaster<ConventionDocument, ManageableConvention>
    implements ConventionMaster, ConfigurableDbChangeProvidingMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCnv";
  /**
   * The default callback.
   */
  public static final DbConventionBeanMasterCallback DEFAULT_CALLBACK = new DefaultDbConventionBeanMasterCallback();

  /**
   * SQL order by.
   */
  protected static final EnumMap<ConventionSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<>(ConventionSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(ConventionSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.NAME_DESC, "name DESC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.CONVENTION_TYPE_ASC, "sub_type ASC");
    ORDER_BY_MAP.put(ConventionSearchSortOrder.CONVENTION_TYPE_DESC, "sub_type DESC");
  }

  /**
   * The callback.
   */
  private final DbConventionBeanMasterCallback _callback;

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbConventionBeanMaster(final DbConnector dbConnector) {
    this(dbConnector, DEFAULT_CALLBACK);
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   * @param callback  the callback used to configure the master, not null
   */
  public DbConventionBeanMaster(final DbConnector dbConnector, DbConventionBeanMasterCallback callback) {
    super(new DbBeanMaster<>(dbConnector, IDENTIFIER_SCHEME_DEFAULT, callback));
    _callback = callback;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the callback object.
   * 
   * @return the callback object, not null
   */
  protected DbConventionBeanMasterCallback getCallback() {
    return _callback;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConventionMetaDataResult metaData(ConventionMetaDataRequest request) {
    ConventionMetaDataResult result = new ConventionMetaDataResult();
    if (request.isConventionTypes()) {
      for (String type : getDelegate().getAllSubTypes()) {
        result.getConventionTypes().add(ConventionType.of(type));
      }
    }
    if (request.isSchemaVersion()) {
      result.setSchemaVersion(getDelegate().getSchemaVersionString());
    }
    return result;
  }

  @Override
  public ConventionSearchResult search(ConventionSearchRequest request) {
    BeanMasterSearchRequest delegatedRequest = new BeanMasterSearchRequest();
    delegatedRequest.setPagingRequest(request.getPagingRequest());
    delegatedRequest.setVersionCorrection(request.getVersionCorrection());
    delegatedRequest.setObjectIds(request.getObjectIds());
    delegatedRequest.setExternalIdSearch(request.getExternalIdSearch());
    delegatedRequest.setExternalIdValue(request.getExternalIdValue());
    delegatedRequest.setExternalIdScheme(request.getExternalIdScheme());
    delegatedRequest.setAttributes(request.getAttributes());
    delegatedRequest.setName(request.getName());
    delegatedRequest.setSubType(request.getConventionType() != null ? request.getConventionType().getName() : null);
    delegatedRequest.setSortOrderSql(ORDER_BY_MAP.get(request.getSortOrder()));
    getCallback().buildIndexedPropertiesSearch(delegatedRequest, request);
    return getDelegate().search(delegatedRequest, new ConventionSearchResult());
  }

  @Override
  public ConventionHistoryResult history(ConventionHistoryRequest request) {
    return getDelegate().history(request, new ConventionHistoryResult());
  }

}
