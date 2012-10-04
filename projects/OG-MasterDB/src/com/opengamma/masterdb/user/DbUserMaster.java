/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.*;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.user.*;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class DbUserMaster extends AbstractDocumentDbMaster<ManageableOGUser, UserDocument> implements UserMaster {
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(DbUserMaster.class);
  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbUsr";
  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbUserMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbUserMaster.class));
  }

  @Override
  public UserDocument get(UniqueId uniqueId) {
    return doGet(uniqueId, new UserDocumentExtractor(), "User");
  }

  @Override
  public UserDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new UserDocumentExtractor(), "User");
  }

  @Override
  protected UserDocument insert(UserDocument document) {
    ArgumentChecker.notNull(document.getObject(), "document.user");
    ArgumentChecker.notNull(document.getName(), "document.name");
    
    final ManageableOGUser user = document.getObject();
    final long docId = nextId("usr_oguser_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    user.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    final DbMapSqlParameterSource docArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("password", user.getPasswordHash());
    
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectIdKey = getElSqlBundle().getSql("SelectIdKey");
    for (ExternalId id : user.getExternalIdBundle()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey, assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("usr_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    
    final List<DbMapSqlParameterSource> entitlementList = new ArrayList<DbMapSqlParameterSource>();
    int iEntitlement = 0;
    for (String entitlement : user.getEntitlements()) {
      entitlementList.add(new DbMapSqlParameterSource()
        .addValue("oguser_id", docId)
        .addValue("entitlement_index", iEntitlement)
        .addValue("entitlement_pattern", entitlement));
      iEntitlement++;
    }
    
    final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
    final String sqlIdKey = getElSqlBundle().getSql("InsertIdKey");
    final String sqlDoc2IdKey = getElSqlBundle().getSql("InsertDoc2IdKey");
    final String sqlEntitlement = getElSqlBundle().getSql("InsertEntitlement");
    getJdbcTemplate().update(sqlDoc, docArgs);
    getJdbcTemplate().batchUpdate(sqlIdKey, idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlDoc2IdKey, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
    getJdbcTemplate().batchUpdate(sqlEntitlement, entitlementList.toArray(new DbMapSqlParameterSource[entitlementList.size()]));
    return document;
  }

  /**
   * Mapper from SQL rows to a UserDocument.
   */
  protected final class UserDocumentExtractor implements ResultSetExtractor<List<UserDocument>> {
    private long _previousDocId = -1L;
    private long _previousKeyId = -1L;
    private boolean _processEntitlements = true;
    private ManageableOGUser _currUser;
    private List<UserDocument> _documents = new ArrayList<UserDocument>();

    @Override
    public List<UserDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        // DOC_ID tells us when we're on a new document.
        if (docId != _previousDocId) {
          _previousDocId = docId;
          buildUser(rs, docId);
          _previousKeyId = -1L;
          _processEntitlements = true;
        }
        
        // IDKEY_ID tells us when we're on a new external ID
        // This tells us to both process the ID and NOT to process the entitlement
        // patterns.
        final long keyId = rs.getLong("IDKEY_ID");
        if (rs.wasNull()) {
          // No external IDs. Have to process entitlements.
          _processEntitlements = true;
        } else if (keyId != _previousKeyId) {
          // We've rolled. Don't process entitlements this pass.
          if (_previousKeyId != -1L) {
            _processEntitlements = false;
          }
          
          _previousKeyId = keyId;
          String idKey = rs.getString("KEY_SCHEME");
          String idValue = rs.getString("KEY_VALUE");
          ExternalIdBundle idBundle = _currUser.getExternalIdBundle().withExternalId(
              ExternalId.of(idKey, idValue));
          _currUser.setExternalIdBundle(idBundle);
        }
        
        String entitlementPattern = rs.getString("ENTITLEMENT_PATTERN");
        if (_processEntitlements && !rs.wasNull()) {
          _currUser.getEntitlements().add(entitlementPattern);
        }
      }
      return _documents;
    }

    private void buildUser(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      
      UniqueId uniqueId = createUniqueId(docOid, docId);
      
      ManageableOGUser user = new ManageableOGUser();
      user.setName(rs.getString("NAME"));
      user.setPasswordHash(rs.getString("PASSWORD"));
      user.setUniqueId(uniqueId);
      _currUser = user;
      
      UserDocument doc = new UserDocument();
      doc.setUniqueId(uniqueId);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setObject(user);
      _documents.add(doc);
    }
  }

  @Override
  public UserSearchResult search(UserSearchRequest request) {
    // TODO kirk 2012-08-20 -- Yep, this is exactly what it looks like. Yes, it needs
    // replacing.
    List<UserDocument> docs = new LinkedList<UserDocument>();
    
    final String sql = getElSqlBundle().getSql("GetAll");
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    @SuppressWarnings("unchecked")
    final List<UserDocument> queryResult = namedJdbc.query(sql, Collections.EMPTY_MAP, new UserDocumentExtractor());
    for (UserDocument doc : queryResult) {
      if (request.matches(doc)) {
        docs.add(doc);
      }
    }
    
    return new UserSearchResult(docs);
  }

  @Override
  public AbstractHistoryResult<UserDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    UserHistoryRequest historyRequest = new UserHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return doHistory(request, new UserHistoryResult(), new UserDocumentExtractor());
  }
}
