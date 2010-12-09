/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.lob.LobHandler;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Exchange master worker to get the exchange.
 */
public class QueryExchangeDbExchangeMasterWorker extends DbExchangeMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QueryExchangeDbExchangeMasterWorker.class);
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "main.id AS exchange_id, " +
        "main.oid AS exchange_oid, " +
        "main.ver_from_instant AS ver_from_instant, " +
        "main.ver_to_instant AS ver_to_instant, " +
        "main.corr_from_instant AS corr_from_instant, " +
        "main.corr_to_instant AS corr_to_instant, " +
        "main.detail AS detail ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM exg_exchange main ";

  /**
   * Creates an instance.
   */
  public QueryExchangeDbExchangeMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeDocument get(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getById(uid);
    } else {
      return getByLatest(uid);
    }
  }

  /**
   * Gets a exchange by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the exchange document, null if not found
   */
  protected ExchangeDocument getByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getExchangeByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final ExchangeHistoryRequest request = new ExchangeHistoryRequest(uid, now, now);
    final ExchangeHistoryResult result = getMaster().history(request);
    if (result.getDocuments().size() != 1) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
    return result.getFirstDocument();
  }

  /**
   * Gets a exchange by identifier.
   * @param uid  the unique identifier
   * @return the exchange document, null if not found
   */
  protected ExchangeDocument getById(final UniqueIdentifier uid) {
    s_logger.debug("getExchangeById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("exchange_id", extractRowId(uid));
    
    final ExchangeDocumentExtractor extractor = new ExchangeDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<ExchangeDocument> docs = namedJdbc.query(sqlGetExchangeById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Exchange not found: " + uid);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a exchange by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetExchangeById() {
    return SELECT + FROM + "WHERE main.id = :exchange_id ";
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeSearchResult search(ExchangeSearchRequest request) {
    s_logger.debug("searchExchanges: {}", request);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()));
    List<IdentifierBundle> bundles = new ArrayList<IdentifierBundle>(request.getIdentifiers());  // lock order
    int i = 0;
    for (IdentifierBundle bundle : bundles) {
      for (Identifier id : bundle) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
    }
    final ExchangeSearchResult result = new ExchangeSearchResult();
    searchWithPaging(request.getPagingRequest(), sqlSearchExchanges(request, bundles), args, new ExchangeDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for exchanges.
   * @param request  the request, not null
   * @param bundles  the ordered set of bundles, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchExchanges(final ExchangeSearchRequest request, final List<IdentifierBundle> bundles) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getIdentifiers().size() > 0) {
      where += "AND id IN (" + sqlSelectMatchingBundles(bundles) + ") ";
    }
    String selectFromWhereInner = "SELECT id FROM exg_exchange " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE main.id IN (" + inner + ") ORDER BY main.id ";
    String count = "SELECT COUNT(*) FROM exg_exchange " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to find all the ids for all bundles in the set.
   * @param bundles  the ordered set of bundles, not null
   * @return the SQL search and count, not null
   */
  protected String sqlSelectMatchingBundles(final List<IdentifierBundle> bundles) {
    String select = "";
    int i = 0;
    for (IdentifierBundle bundle : bundles) {
      select += (i == 0 ? "" : "UNION ");
      select += sqlSelectMatchingBundle(bundle, i);
      i += bundle.size();
    }
    return select;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * @param bundle  the bundle, not null
   * @param count  the overall count of the bundle parameters
   * @return the SQL search and count, not null
   */
  protected String sqlSelectMatchingBundle(final IdentifierBundle bundle, final int count) {
    // only return exchange_id when all requested ids match (having count >= size)
    // filter by dates to reduce search set
    String select = "SELECT exchange_id " +
      "FROM exg_exchange2idkey, exg_exchange main " +
      "WHERE exchange_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectAllMatchingBundle(bundle, count) + ") " +
      "GROUP BY exchange_id " +
      "HAVING COUNT(exchange_id) >= " + bundle.size() + " ";
    return select;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * @param bundle  the bundle, not null
   * @param count  the overall count of the bundle parameters
   * @return the SQL search and count, not null
   */
  protected String sqlSelectAllMatchingBundle(final IdentifierBundle bundle, final int count) {
    String select = "SELECT id FROM exg_idkey ";
    for (int i = 0; i < bundle.getIdentifiers().size(); i++) {
      select += (i == 0 ? "WHERE " : "OR ");
      select += "(key_scheme = :key_scheme" + (i + count) + " AND key_value = :key_value" + (i + count) + ") ";
    }
    return select;
  }

  //-------------------------------------------------------------------------
  @Override
  protected ExchangeHistoryResult history(final ExchangeHistoryRequest request) {
    s_logger.debug("searchExchangeHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("exchange_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final ExchangeHistoryResult result = new ExchangeHistoryResult();
    searchWithPaging(request.getPagingRequest(), sqlSearchExchangeHistoric(request), args, new ExchangeDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL for searching the history of a exchange.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchExchangeHistoric(final ExchangeHistoryRequest request) {
    String where = "WHERE oid = :exchange_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      where += "AND ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) " +
                            "OR ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_to_instant AND ver_to_instant > :versions_to_instant) " +
                            "OR ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      where += "AND corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) " +
                            "OR corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_to_instant AND ver_to_instant > :corrections_to_instant) " +
                            "OR corr_to_instant < :corrections_to_instant) ";
      }
    }
    String selectFromWhereInner = "SELECT id FROM exg_exchange " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE main.id IN (" + inner + ") ORDER BY main.ver_from_instant DESC, main.corr_from_instant DESC ";
    String count = "SELECT COUNT(*) FROM exg_exchange " + where;
    return new String[] {search, count};
  }

  /**
   * Searches for documents with paging.
   * 
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected void searchWithPaging(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<ExchangeDocument>> extractor, final AbstractDocumentsResult<ExchangeDocument> result) {
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(result.getDocuments(), pagingRequest));
    } else {
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(new Paging(pagingRequest, count));
      if (count > 0) {
        result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ExchangeDocument.
   */
  protected final class ExchangeDocumentExtractor implements ResultSetExtractor<List<ExchangeDocument>> {
    private List<ExchangeDocument> _documents = new ArrayList<ExchangeDocument>();

    @Override
    public List<ExchangeDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long exchangeId = rs.getLong("EXCHANGE_ID");
        buildConfig(rs, exchangeId);
      }
      return _documents;
    }

    private void buildConfig(final ResultSet rs, final long configId) throws SQLException {
      final long configOid = rs.getLong("EXCHANGE_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      LobHandler lob = getDbHelper().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "DETAIL");
      ManageableExchange exchange = FUDGE_CONTEXT.readObject(ManageableExchange.class, new ByteArrayInputStream(bytes));
      
      ExchangeDocument doc = new ExchangeDocument();
      doc.setUniqueId(createUniqueIdentifier(configOid, configId));
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setExchange(exchange);
      _documents.add(doc);
    }
  }

}
