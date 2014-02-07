/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.legalentity;

import java.util.EnumMap;

import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntityMetaDataRequest;
import com.opengamma.master.legalentity.LegalEntityMetaDataResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.LegalEntitySearchSortOrder;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.masterdb.ConfigurableDbChangeProvidingMaster;
import com.opengamma.masterdb.bean.AbstractDelegatingBeanMaster;
import com.opengamma.masterdb.bean.BeanMasterSearchRequest;
import com.opengamma.masterdb.bean.DbBeanMaster;
import com.opengamma.util.db.DbConnector;

/**
 * A legal entity master implementation based on Joda-Beans using a database for persistence.
 * <p/>
 * This is a full implementation of the legal entity master using an SQL database.
 * Data is stored based on the Joda-Beans API.
 * Full details of the API are in {@link com.opengamma.master.legalentity.LegalEntityMaster}.
 * <p/>
 * Applications can configure this master using the callback class passed in.
 * <p/>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbLegalEntityBeanMaster
    extends AbstractDelegatingBeanMaster<LegalEntityDocument, ManageableLegalEntity>
    implements LegalEntityMaster, ConfigurableDbChangeProvidingMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbLen";
  /**
   * The default callback.
   */
  public static final DbLegalEntityBeanMasterCallback DEFAULT_CALLBACK = new DefaultDbLegalEntityBeanMasterCallback();

  /**
   * SQL order by.
   */
  protected static final EnumMap<LegalEntitySearchSortOrder, String> ORDER_BY_MAP = new EnumMap<>(LegalEntitySearchSortOrder.class);

  static {
    ORDER_BY_MAP.put(LegalEntitySearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(LegalEntitySearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(LegalEntitySearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(LegalEntitySearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(LegalEntitySearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(LegalEntitySearchSortOrder.NAME_DESC, "name DESC");
  }

  /**
   * The callback.
   */
  private final DbLegalEntityBeanMasterCallback _callback;

  /**
   * Creates an instance.
   *
   * @param dbConnector the database connector, not null
   */
  public DbLegalEntityBeanMaster(final DbConnector dbConnector) {
    this(dbConnector, DEFAULT_CALLBACK);
  }

  /**
   * Creates an instance.
   *
   * @param dbConnector the database connector, not null
   * @param callback    the callback used to configure the master, not null
   */
  public DbLegalEntityBeanMaster(final DbConnector dbConnector, DbLegalEntityBeanMasterCallback callback) {
    super(new DbBeanMaster<>(dbConnector, IDENTIFIER_SCHEME_DEFAULT, callback));
    _callback = callback;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the callback object.
   *
   * @return the callback object, not null
   */
  protected DbLegalEntityBeanMasterCallback getCallback() {
    return _callback;
  }

  //-------------------------------------------------------------------------
  @Override
  public LegalEntityMetaDataResult metaData(LegalEntityMetaDataRequest request) {
    LegalEntityMetaDataResult result = new LegalEntityMetaDataResult();
    if (request.isSchemaVersion()) {
      result.setSchemaVersion(getDelegate().getSchemaVersionString());
    }
    return result;
  }

  @Override
  public LegalEntitySearchResult search(LegalEntitySearchRequest request) {
    BeanMasterSearchRequest delegatedRequest = new BeanMasterSearchRequest();
    delegatedRequest.setPagingRequest(request.getPagingRequest());
    delegatedRequest.setVersionCorrection(request.getVersionCorrection());
    delegatedRequest.setObjectIds(request.getObjectIds());
    delegatedRequest.setExternalIdSearch(request.getExternalIdSearch());
    delegatedRequest.setExternalIdValue(request.getExternalIdValue());
    delegatedRequest.setExternalIdScheme(request.getExternalIdScheme());
    delegatedRequest.setAttributes(request.getAttributes());
    delegatedRequest.setName(request.getName());
    delegatedRequest.setSortOrderSql(ORDER_BY_MAP.get(request.getSortOrder()));
    getCallback().buildIndexedPropertiesSearch(delegatedRequest, request);
    return getDelegate().search(delegatedRequest, new LegalEntitySearchResult());
  }

  @Override
  public LegalEntityHistoryResult history(LegalEntityHistoryRequest request) {
    return getDelegate().history(request, new LegalEntityHistoryResult());
  }

}
