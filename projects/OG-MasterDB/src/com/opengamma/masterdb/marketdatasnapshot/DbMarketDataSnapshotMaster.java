/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.marketdatasnapshot;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.hsqldb.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A snapshot master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the exchange master using an SQL database.
 * Full details of the API are in {@link MarketDataSnapshotMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbMarketDataSnapshotMaster extends AbstractDocumentDbMaster<MarketDataSnapshotDocument> implements
    MarketDataSnapshotMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbMarketDataSnapshotMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbSnp";

  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  
  /**
   * SQL select.
   */
  protected static final String SELECT = "SELECT " + "main.id AS doc_id, " + "main.oid AS doc_oid, "
      + "main.ver_from_instant AS ver_from_instant, " + "main.ver_to_instant AS ver_to_instant, "
      + "main.corr_from_instant AS corr_from_instant, " + "main.corr_to_instant AS corr_to_instant, "
      + "main.detail AS detail ";
  /**
   * SQL from.
   */
  protected static final String FROM = "FROM snp_snapshot main ";


  /**
   * Creates an instance.
   * 
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbMarketDataSnapshotMaster(DbSource dbSource) {
    super(dbSource, IDENTIFIER_SCHEME_DEFAULT);
  }

  //-------------------------------------------------------------------------
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);

    final MarketDataSnapshotSearchResult result = new MarketDataSnapshotSearchResult();

    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(Instant.now(getTimeSource()));
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
        .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
        .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()));

    searchWithPaging(request.getPagingRequest(), sqlSearchMarketDataSnapshots(request), args,
        new MarketDataSnapshotDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for documents.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchMarketDataSnapshots(final MarketDataSnapshotSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant "
        + "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }

    where += sqlAdditionalWhere();

    String selectFromWhereInner = "SELECT id FROM snp_snapshot " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = sqlSelectFrom() + "WHERE main.id IN (" + inner + ") ORDER BY main.id" + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM snp_snapshot " + where;
    return new String[] {search, count };
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final UniqueIdentifier uniqueId) {
    return doGet(uniqueId, new MarketDataSnapshotDocumentExtractor(), "MarketDataSnapshot");
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataSnapshotDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new MarketDataSnapshotDocumentExtractor(),
        "MarketDataSnapshot");
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
    final UniqueIdentifier uniqueId = createUniqueIdentifier(docOid, docId);
    marketDataSnaphshot.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);

    // the arguments for inserting into the marketDataSnaphshot table
    FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(marketDataSnaphshot);
    byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    final MapSqlParameterSource marketDataSnaphshotArgs = new DbMapSqlParameterSource().addValue("doc_id", docId)
        .addValue("doc_oid", docOid).addTimestamp("ver_from_instant", document.getVersionFromInstant())
        .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
        .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
        .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
        .addValue("name", document.getName())
        .addValue("detail", new SqlLobValue(bytes, getDbHelper().getLobHandler()), Types.BLOB);

    getJdbcTemplate().update(sqlInsertMarketDataSnapshot(), marketDataSnaphshotArgs);
    return document;
  }

  /**
   * Gets the SQL for inserting a document.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertMarketDataSnapshot() {
    return "INSERT INTO snp_snapshot "
        + "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, detail) "
        + "VALUES "
        + "(:doc_id, :doc_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :detail)";
  }

  //-------------------------------------------------------------------------
  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String mainTableName() {
    return "snp_snapshot";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a MarketDataSnapshotDocument.
   */
  protected final class MarketDataSnapshotDocumentExtractor implements
      ResultSetExtractor<List<MarketDataSnapshotDocument>> {
    private List<MarketDataSnapshotDocument> _documents = new ArrayList<MarketDataSnapshotDocument>();

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
      LobHandler lob = getDbHelper().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "DETAIL");
      ManageableMarketDataSnapshot marketDataSnaphshot = FUDGE_CONTEXT.readObject(ManageableMarketDataSnapshot.class,
          new ByteArrayInputStream(bytes));

      MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument();
      doc.setUniqueId(createUniqueIdentifier(docOid, docId));
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setSnapshot(marketDataSnaphshot);
      _documents.add(doc);
    }
  }

}
