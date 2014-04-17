/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.marketdatasnapshot;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeTypeDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchSortOrder;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.paging.Paging;

/**
 * A snapshot master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the exchange master using an SQL database.
 * Full details of the API are in {@link MarketDataSnapshotMaster}.
 * <p>
 * The SQL is stored externally in {@code DbMarketDataSnapshotMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbMarketDataSnapshotMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbMarketDataSnapshotMaster
    extends AbstractDocumentDbMaster<MarketDataSnapshotDocument>
    implements MarketDataSnapshotMaster {

  static {
    //Registered here because I can't guarantee that the classes themselves are loaded
    FudgeTypeDictionary typeDictionary = OpenGammaFudgeContext.getInstance().getTypeDictionary();
    typeDictionary.registerClassRename("com.opengamma.master.marketdatasnapshot.ManageableUnstructuredMarketDataSnapshot", ManageableUnstructuredMarketDataSnapshot.class);
    typeDictionary.registerClassRename("com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot", ManageableMarketDataSnapshot.class);
    typeDictionary.registerClassRename("com.opengamma.master.marketdatasnapshot.ManageableYieldCurveSnapshot", ManageableYieldCurveSnapshot.class);
    typeDictionary.registerClassRename("com.opengamma.master.marketdatasnapshot.ManageableCurveSnapshot", ManageableCurveSnapshot.class);
    typeDictionary.registerClassRename("com.opengamma.master.marketdatasnapshot.ManageableVolatilitySurfaceSnapshot", ManageableVolatilitySurfaceSnapshot.class);
    typeDictionary.registerClassRename("com.opengamma.master.marketdatasnapshot.ManageableVolatilityCubeSnapshot", ManageableVolatilityCubeSnapshot.class);
  }

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbMarketDataSnapshotMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbSnp";
  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  
  /**
   * SQL order by.
   */
  protected static final EnumMap<MarketDataSnapshotSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<MarketDataSnapshotSearchSortOrder, String>(MarketDataSnapshotSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(MarketDataSnapshotSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(MarketDataSnapshotSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(MarketDataSnapshotSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(MarketDataSnapshotSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(MarketDataSnapshotSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(MarketDataSnapshotSearchSortOrder.NAME_DESC, "name DESC");
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbMarketDataSnapshotMaster(DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbMarketDataSnapshotMaster.class));
  }

  //-------------------------------------------------------------------------
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final MarketDataSnapshotSearchResult result = new MarketDataSnapshotSearchResult(vc);
    
    final List<ObjectId> snapshotIds = request.getSnapshotIds();
    if (snapshotIds != null && snapshotIds.size() == 0) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final DbMapSqlParameterSource args = createParameterSource();
    args.addTimestamp("version_as_of_instant", vc.getVersionAsOf());
    args.addTimestamp("corrected_to_instant", vc.getCorrectedTo());
    args.addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()));
    args.addValue("details", request.isIncludeData());
    if (snapshotIds != null) {
      StringBuilder buf = new StringBuilder(snapshotIds.size() * 10);
      for (ObjectId snapshotId : snapshotIds) {
        checkScheme(snapshotId);
        buf.append(extractOid(snapshotId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_object_ids", buf.toString());
    }
    args.addValue("sort_order", ORDER_BY_MAP.get(request.getSortOrder()));
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());
    
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    doSearch(request.getPagingRequest(), sql, args, new MarketDataSnapshotDocumentExtractor(request.isIncludeData()), result);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final UniqueId uniqueId) {
    return doGet(uniqueId, new MarketDataSnapshotDocumentExtractor(true), "MarketDataSnapshot");
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new MarketDataSnapshotDocumentExtractor(true), "MarketDataSnapshot");
  }

  //-------------------------------------------------------------------------
  public MarketDataSnapshotHistoryResult history(final MarketDataSnapshotHistoryRequest request) {
    return doHistory(request, new MarketDataSnapshotHistoryResult(), new MarketDataSnapshotDocumentExtractor(request.isIncludeData()));
  }

  @Override
  protected DbMapSqlParameterSource argsHistory(AbstractHistoryRequest request) {
    DbMapSqlParameterSource args = super.argsHistory(request);
    args.addValue("details", ((MarketDataSnapshotHistoryRequest) request).isIncludeData());
    return args;
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected MarketDataSnapshotDocument insert(final MarketDataSnapshotDocument document) {
    ArgumentChecker.notNull(document.getSnapshot(), "document.snapshot");
    ArgumentChecker.notNull(document.getName(), "document.name");
    
    final ManageableMarketDataSnapshot marketDataSnaphshot = document.getSnapshot();
    final long docId = nextId("snp_snapshot_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // set the uniqueId (needs to go in Fudge message)
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    marketDataSnaphshot.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    
    // the arguments for inserting into the marketDataSnaphshot table
    FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(marketDataSnaphshot);
    byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    final DbMapSqlParameterSource marketDataSnaphshotArgs = createParameterSource().addValue("doc_id", docId)
        .addValue("doc_oid", docOid).addTimestamp("ver_from_instant", document.getVersionFromInstant())
        .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
        .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
        .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
        .addValue("name", document.getName())
        .addValue("detail", new SqlLobValue(bytes, getDialect().getLobHandler()), Types.BLOB);
    
    final String sql = getElSqlBundle().getSql("Insert", marketDataSnaphshotArgs);
    getJdbcTemplate().update(sql, marketDataSnaphshotArgs);
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a MarketDataSnapshotDocument.
   */
  protected final class MarketDataSnapshotDocumentExtractor implements ResultSetExtractor<List<MarketDataSnapshotDocument>> {
    private final boolean _includeData;
    private final List<MarketDataSnapshotDocument> _documents = new ArrayList<MarketDataSnapshotDocument>();

    public MarketDataSnapshotDocumentExtractor(final boolean includeData) {
      _includeData = includeData;
    }

    @Override
    public List<MarketDataSnapshotDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        buildConfig(rs, docId);
      }
      return _documents;
    }

    private void buildConfig(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      UniqueId uniqueId = createUniqueId(docOid, docId);
      
      ManageableMarketDataSnapshot marketDataSnapshot;
      //PLAT-1378
      if (_includeData) {
        LobHandler lob = getDialect().getLobHandler();
        byte[] bytes = lob.getBlobAsBytes(rs, "DETAIL");
        marketDataSnapshot = FUDGE_CONTEXT.readObject(ManageableMarketDataSnapshot.class,
            new ByteArrayInputStream(bytes));
        if (!_includeData) {
          marketDataSnapshot.setGlobalValues(null);
          marketDataSnapshot.setYieldCurves(null);
        }
      } else {
        marketDataSnapshot = new ManageableMarketDataSnapshot();
        marketDataSnapshot.setName(rs.getString("NAME"));
        marketDataSnapshot.setUniqueId(uniqueId);
      }
      MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
      doc.setUniqueId(uniqueId);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setSnapshot(marketDataSnapshot);
      _documents.add(doc);
    }
  }

  @Override
  protected AbstractHistoryResult<MarketDataSnapshotDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    MarketDataSnapshotHistoryRequest historyRequest = new MarketDataSnapshotHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest);
  }

}
