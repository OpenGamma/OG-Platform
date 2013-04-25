/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.orgs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.id.ExternalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.orgs.ManageableOrganization;
import com.opengamma.master.orgs.OrganizationDocument;
import com.opengamma.master.orgs.OrganizationHistoryRequest;
import com.opengamma.master.orgs.OrganizationHistoryResult;
import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.master.orgs.OrganizationSearchRequest;
import com.opengamma.master.orgs.OrganizationSearchResult;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;

/**
 * An organization master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the organization master using an SQL database.
 * Full details of the API are in {@link com.opengamma.master.orgs.OrganizationMaster}.
 * <p>
 * The SQL is stored externally in {@code DbOrganizationMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbOrganizationMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbOrganizationMaster
    extends AbstractDocumentDbMaster<OrganizationDocument>
    implements OrganizationMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbOrganizationMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbOrg";
  
  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbOrganizationMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbOrganizationMaster.class));
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationSearchResult search(final OrganizationSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final OrganizationSearchResult result = new OrganizationSearchResult(vc);
    

    final List<ObjectId> organizationObjectIds = request.getOrganizationObjectIds();
    if (organizationObjectIds != null && organizationObjectIds.size() == 0) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
        .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
        .addValueNullIgnored("obligor_short_name", getDialect().sqlWildcardAdjustValue(request.getObligorShortName()))
        .addValueNullIgnored("obligor_red_code", getDialect().sqlWildcardAdjustValue(request.getObligorREDCode()))
        .addValueNullIgnored("obligor_ticker", getDialect().sqlWildcardAdjustValue(request.getObligorTicker()));
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());

    if (organizationObjectIds != null) {
      StringBuilder buf = new StringBuilder(organizationObjectIds.size() * 10);
      for (ObjectId objectId : organizationObjectIds) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_object_ids", buf.toString());
    }
    
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    doSearch(request.getPagingRequest(), sql, args, new OrganizationDocumentExtractor(), result);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument get(final UniqueId uniqueId) {
    return doGet(uniqueId, new OrganizationDocumentExtractor(), "Organization");
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new OrganizationDocumentExtractor(), "Organization");
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganizationHistoryResult history(final OrganizationHistoryRequest request) {
    return doHistory(request, new OrganizationHistoryResult(), new OrganizationDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected OrganizationDocument insert(final OrganizationDocument document) {
    ArgumentChecker.notNull(document.getOrganization(), "document.organization");
    ArgumentChecker.notNull(document.getOrganization().getObligor(), "document.organization.obligor");

    ArgumentChecker.notNull(document.getOrganization().getObligor().getObligorShortName(),
                            "organization.trade.obligor_short_name");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getObligorREDCode(),
                            "organization.trade.obligor_red_code");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getObligorTicker(),
                            "organization.trade.obligor_ticker");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getCompositeRating(),
                            "organization.trade.obligor_composite_rating");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getCountry(), "organization.trade.obligor_country");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getFitchCreditRating(),
                            "organization.trade.obligor_fitch_credit_rating");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getMoodysCreditRating(),
                            "organization.trade.obligor_moodys_credit_rating");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getRegion(), "organization.trade.obligor_region");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getSector(), "organization.trade.obligor_sector");
    ArgumentChecker.notNull(document.getOrganization().getObligor().getStandardAndPoorsCreditRating(),
                            "organization.trade.obligor_standard_and_poors_credit_rating");

    final long docId = nextId("org_organisation_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    final UniqueId organizationUid = createUniqueId(docOid, docId);
    final ManageableOrganization organization = document.getOrganization();

    // the arguments for inserting into the organization table
    final DbMapSqlParameterSource docArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addValue("doc_oid", docOid)
        .addTimestamp("ver_from_instant", document.getVersionFromInstant())
        .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
        .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
        .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
        .addValue("provider_scheme",
            document.getProviderId() != null ? document.getProviderId().getScheme().getName() : null,
            Types.VARCHAR)
        .addValue("provider_value",
            document.getProviderId() != null ? document.getProviderId().getValue() : null,
            Types.VARCHAR)
        .addValue("obligor_short_name", organization.getObligor().getObligorShortName(), Types.VARCHAR)
        .addValue("obligor_red_code", organization.getObligor().getObligorREDCode(), Types.VARCHAR)
        .addValue("obligor_ticker", organization.getObligor().getObligorTicker(), Types.VARCHAR)
        .addValue("obligor_composite_rating", organization.getObligor().getCompositeRating().name(), Types.VARCHAR)
        .addValue("obligor_country", organization.getObligor().getCountry(), Types.VARCHAR)
        .addValue("obligor_fitch_credit_rating", organization.getObligor().getFitchCreditRating().name(), Types.VARCHAR)
        .addValue("obligor_implied_rating", organization.getObligor().getImpliedRating().name(), Types.VARCHAR)
        .addValue("obligor_moodys_credit_rating",
                  organization.getObligor().getMoodysCreditRating().name(),
                  Types.VARCHAR)
        .addValue("obligor_region", organization.getObligor().getRegion().name(), Types.VARCHAR)
        .addValue("obligor_sector", organization.getObligor().getSector().name(), Types.VARCHAR)
        .addValue("obligor_standard_and_poors_credit_rating",
                  organization.getObligor().getStandardAndPoorsCreditRating().name(),
                  Types.VARCHAR)
        .addValue("obligor_has_defaulted",
                  organization.getObligor().isHasDefaulted() ? 1 : 0,
                  Types.TINYINT);


    final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);

    // set the uniqueId
    organization.setUniqueId(organizationUid);
    document.setUniqueId(organizationUid);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableOrganization getOrganization(final UniqueId uniqueId) {
    return get(uniqueId).getOrganization();
  }

  //-------------------------------------------------------------------------
  @Override
  public AbstractHistoryResult<OrganizationDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    OrganizationHistoryRequest historyRequest = new OrganizationHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest);
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a OrganizationDocument.
   */
  protected final class OrganizationDocumentExtractor implements ResultSetExtractor<List<OrganizationDocument>> {
    private long _lastOrganizationId = -1;
    private ManageableOrganization _organization;
    private List<OrganizationDocument> _documents = new ArrayList<OrganizationDocument>();

    @Override
    public List<OrganizationDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long organizationId = rs.getLong("ORGANISATION_ID");
        if (_lastOrganizationId != organizationId) {
          _lastOrganizationId = organizationId;
          buildOrganization(rs, organizationId);
        }
      }
      return _documents;
    }

    private void buildOrganization(final ResultSet rs, final long organizationId) throws SQLException {
      final long organizationOid = rs.getLong("ORGANISATION_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");

      final String providerScheme = rs.getString("PROVIDER_SCHEME");
      final String providerValue = rs.getString("PROVIDER_VALUE");

      final String shortName = rs.getString("obligor_short_name");
      final String redCode = rs.getString("obligor_red_code");
      final String ticker = rs.getString("obligor_ticker");
      final String compositeRating = rs.getString("obligor_composite_rating");
      final String country = rs.getString("obligor_country");
      final String fitchCreditRating = rs.getString("obligor_fitch_credit_rating");
      final String impliedRating = rs.getString("obligor_implied_rating");
      final String moodysCreditRating = rs.getString("obligor_moodys_credit_rating");
      final String region = rs.getString("obligor_region");
      final String sector = rs.getString("obligor_sector");
      final String standardAndPoorsCreditRating = rs.getString("obligor_standard_and_poors_credit_rating");
      final boolean hasDefaulted = rs.getBoolean("obligor_has_defaulted");


      _organization = new ManageableOrganization(
          shortName,
          redCode,
          ticker,
          Region.valueOf(region),
          country,
          Sector.valueOf(sector),
          CreditRating.valueOf(compositeRating),
          CreditRating.valueOf(impliedRating),
          CreditRatingFitch.valueOf(fitchCreditRating),
          CreditRatingMoodys.valueOf(moodysCreditRating),
          CreditRatingStandardAndPoors.valueOf(standardAndPoorsCreditRating),
          hasDefaulted
      );
      _organization.setUniqueId(createUniqueId(organizationOid, organizationId));
      OrganizationDocument doc = new OrganizationDocument(_organization);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(createUniqueId(organizationOid, organizationId));
      if (providerScheme != null && providerValue != null) {
        doc.setProviderId(ExternalId.of(providerScheme, providerValue));
      }
      _documents.add(doc);
    }
  }

}
