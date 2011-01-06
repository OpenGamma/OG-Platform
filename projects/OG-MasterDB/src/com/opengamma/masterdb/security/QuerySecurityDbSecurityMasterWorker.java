/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

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

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Security master worker to get the security.
 */
public class QuerySecurityDbSecurityMasterWorker extends DbSecurityMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorker.class);
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "s.id AS security_id, " +
        "s.oid AS security_oid, " +
        "s.ver_from_instant AS ver_from_instant, " +
        "s.ver_to_instant AS ver_to_instant, " +
        "s.corr_from_instant AS corr_from_instant, " +
        "s.corr_to_instant AS corr_to_instant, " +
        "s.name AS name, " +
        "s.sec_type AS sec_type, " +
        "i.key_scheme AS key_scheme, " +
        "i.key_value AS key_value ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM sec_security s LEFT JOIN sec_security2idkey si ON (si.security_id = s.id) LEFT JOIN sec_idkey i ON (si.idkey_id = i.id) ";

  /**
   * Creates an instance.
   */
  public QuerySecurityDbSecurityMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityDocument get(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getById(uid);
    } else {
      return getByLatest(uid);
    }
  }

  /**
   * Gets a security by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the security document, null if not found
   */
  protected SecurityDocument getByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getSecurityByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final SecurityHistoryRequest request = new SecurityHistoryRequest(uid, now, now);
    request.setFullDetail(true);
    final SecurityHistoryResult result = getMaster().history(request);
    if (result.getDocuments().size() != 1) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    return result.getFirstDocument();
  }

  /**
   * Gets a security by identifier.
   * @param uid  the unique identifier
   * @return the security document, null if not found
   */
  protected SecurityDocument getById(final UniqueIdentifier uid) {
    s_logger.debug("getSecurityById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("security_id", extractRowId(uid));
    
    final SecurityDocumentExtractor extractor = new SecurityDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<SecurityDocument> docs = namedJdbc.query(sqlGetSecurityById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    loadDetail(docs);
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a security by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetSecurityById() {
    return SELECT + FROM + "WHERE s.id = :security_id ";
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecuritySearchResult search(SecuritySearchRequest request) {
    s_logger.debug("searchSecurities: {}", request);
    final SecuritySearchResult result = new SecuritySearchResult();
    if ((request.getSecurityIds() != null && request.getSecurityIds().size() == 0) ||
        (IdentifierSearch.canMatch(request.getSecurityKeys()) == false)) {
      return result;
    }
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValueNullIgnored("sec_type", request.getSecurityType());
    if (request.getSecurityKeys() != null) {
      int i = 0;
      for (Identifier id : request.getSecurityKeys()) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
    }
    searchWithPaging(request.getPagingRequest(), sqlSearchSecurities(request), args, new SecurityDocumentExtractor(), result);
    if (request.isFullDetail()) {
      loadDetail(result.getDocuments());
    }
    return result;
  }

  /**
   * Gets the SQL to search for securities.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchSecurities(final SecuritySearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getSecurityType() != null) {
      where += "AND sec_type = :sec_type ";
    }
    if (request.getSecurityIds() != null) {
      StringBuilder buf = new StringBuilder(request.getSecurityIds().size() * 10);
      for (UniqueIdentifier uid : request.getSecurityIds()) {
        getMaster().checkScheme(uid);
        buf.append(extractOid(uid)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getSecurityKeys() != null && request.getSecurityKeys().size() > 0) {
      where += sqlSelectMatchingSecurityKeys(request.getSecurityKeys());
    }
    String selectFromWhereInner = "SELECT id FROM sec_security " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE s.id IN (" + inner + ") ORDER BY s.id";
    String count = "SELECT COUNT(*) FROM sec_security " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to find all the ids for all bundles in the set.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeys(final IdentifierSearch idSearch) {
    switch (idSearch.getSearchType()) {
      case EXACT:
        return "AND id IN (" + sqlSelectMatchingSecurityKeysExact(idSearch) + ") ";
      case ALL:
        return "AND id IN (" + sqlSelectMatchingSecurityKeysAll(idSearch) + ") ";
      case ANY:
        return "AND id IN (" + sqlSelectMatchingSecurityKeysAny(idSearch) + ") ";
      case NONE:
        return "AND id NOT IN (" + sqlSelectMatchingSecurityKeysAny(idSearch) + ") ";
    }
    throw new UnsupportedOperationException("Search type is not supported: " + idSearch.getSearchType());
  }

  /**
   * Gets the SQL to find all the securities matching.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysExact(final IdentifierSearch idSearch) {
    // compare size of all matched to size in total
    // filter by dates to reduce search set
    String a = "SELECT security_id AS matched_security_id, COUNT(security_id) AS matched_count " +
      "FROM sec_security2idkey, sec_security main " +
      "WHERE security_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingSecurityKeysOr(idSearch) + ") " +
      "GROUP BY security_id " +
      "HAVING COUNT(security_id) >= " + idSearch.size() + " ";
    String b = "SELECT security_id AS total_security_id, COUNT(security_id) AS total_count " +
      "FROM sec_security2idkey, sec_security main " +
      "WHERE security_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "GROUP BY security_id ";
    String select = "SELECT matched_security_id AS security_id " +
      "FROM (" + a + ") AS a, (" + b + ") AS b " +
      "WHERE matched_security_id = total_security_id " +
        "AND matched_count = total_count ";
    return select;
  }

  /**
   * Gets the SQL to find all the securities matching.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysAll(final IdentifierSearch idSearch) {
    // only return security_id when all requested ids match (having count >= size)
    // filter by dates to reduce search set
    String select = "SELECT security_id " +
      "FROM sec_security2idkey, sec_security main " +
      "WHERE security_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingSecurityKeysOr(idSearch) + ") " +
      "GROUP BY security_id " +
      "HAVING COUNT(security_id) >= " + idSearch.size() + " ";
    return select;
  }

  /**
   * Gets the SQL to find all the securities matching any identifier.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysAny(final IdentifierSearch idSearch) {
    // optimized search for commons case of individual ORs
    // filter by dates to reduce search set
    String select = "SELECT DISTINCT security_id " +
      "FROM sec_security2idkey, sec_security main " +
      "WHERE security_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingSecurityKeysOr(idSearch) + ") ";
    return select;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysOr(final IdentifierSearch idSearch) {
    String select = "SELECT id FROM sec_idkey ";
    for (int i = 0; i < idSearch.size(); i++) {
      select += (i == 0 ? "WHERE " : "OR ");
      select += "(key_scheme = :key_scheme" + i + " AND key_value = :key_value" + i + ") ";
    }
    return select;
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityHistoryResult history(final SecurityHistoryRequest request) {
    s_logger.debug("searchSecurityHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("security_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final SecurityHistoryResult result = new SecurityHistoryResult();
    searchWithPaging(request.getPagingRequest(), sqlSearchSecurityHistoric(request), args, new SecurityDocumentExtractor(), result);
    if (request.isFullDetail()) {
      loadDetail(result.getDocuments());
    }
    return result;
  }

  /**
   * Gets the SQL for searching the history of a security.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchSecurityHistoric(final SecurityHistoryRequest request) {
    String where = "WHERE oid = :security_oid ";
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
    String selectFromWhereInner = "SELECT id FROM sec_security " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE s.id IN (" + inner + ") ORDER BY s.ver_from_instant DESC, s.corr_from_instant DESC";
    String count = "SELECT COUNT(*) FROM sec_security " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the detail of the security for the document.
   * @param docs  the documents to load detail for, not null
   */
  protected void loadDetail(final List<SecurityDocument> docs) {
    SecurityMasterDetailProvider detailProvider = getMaster().getWorkers().getDetailProvider();
    if (detailProvider != null) {
      for (SecurityDocument doc : docs) {
        doc.setSecurity(detailProvider.loadSecurityDetail(doc.getSecurity()));
      }
    }
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
      final ResultSetExtractor<List<SecurityDocument>> extractor, final AbstractDocumentsResult<SecurityDocument> result) {
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
   * Mapper from SQL rows to a SecurityDocument.
   */
  protected final class SecurityDocumentExtractor implements ResultSetExtractor<List<SecurityDocument>> {
    private long _lastSecurityId = -1;
    private ManageableSecurity _security;
    private List<SecurityDocument> _documents = new ArrayList<SecurityDocument>();

    @Override
    public List<SecurityDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long securityId = rs.getLong("SECURITY_ID");
        if (_lastSecurityId != securityId) {
          _lastSecurityId = securityId;
          buildSecurity(rs, securityId);
        }
        final String idScheme = rs.getString("KEY_SCHEME");
        final String idValue = rs.getString("KEY_VALUE");
        if (idScheme != null && idValue != null) {
          Identifier id = Identifier.of(idScheme, idValue);
          _security.setIdentifiers(_security.getIdentifiers().withIdentifier(id));
        }
      }
      return _documents;
    }

    private void buildSecurity(final ResultSet rs, final long securityId) throws SQLException {
      final long securityOid = rs.getLong("SECURITY_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = rs.getString("NAME");
      final String type = rs.getString("SEC_TYPE");
      UniqueIdentifier uid = createUniqueIdentifier(securityOid, securityId);
      _security = new ManageableSecurity(uid, name, type, IdentifierBundle.EMPTY);
      SecurityDocument doc = new SecurityDocument(_security);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(uid);
      _documents.add(doc);
    }
  }

}
