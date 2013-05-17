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
import com.opengamma.master.orgs.ManageableOrganisation;
import com.opengamma.master.orgs.OrganisationDocument;
import com.opengamma.master.orgs.OrganisationHistoryRequest;
import com.opengamma.master.orgs.OrganisationHistoryResult;
import com.opengamma.master.orgs.OrganisationMaster;
import com.opengamma.master.orgs.OrganisationSearchRequest;
import com.opengamma.master.orgs.OrganisationSearchResult;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;

/**
 * A organisation master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the organisation master using an SQL database.
 * Full details of the API are in {@link com.opengamma.master.orgs.OrganisationMaster}.
 * <p>
 * The SQL is stored externally in {@code DbOrganisationMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbOrganisationMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbOrganisationMaster
    extends AbstractDocumentDbMaster<OrganisationDocument>
    implements OrganisationMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbOrganisationMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbOrg";
  
  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbOrganisationMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbOrganisationMaster.class));
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationSearchResult search(final OrganisationSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final OrganisationSearchResult result = new OrganisationSearchResult(vc);
    

    final List<ObjectId> organisationObjectIds = request.getOrganisationObjectIds();
    if (organisationObjectIds != null && organisationObjectIds.size() == 0) {
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

    if (organisationObjectIds != null) {
      StringBuilder buf = new StringBuilder(organisationObjectIds.size() * 10);
      for (ObjectId objectId : organisationObjectIds) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_object_ids", buf.toString());
    }
    
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    searchWithPaging(request.getPagingRequest(), sql, args, new OrganisationDocumentExtractor(), result);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument get(final UniqueId uniqueId) {
    return doGet(uniqueId, new OrganisationDocumentExtractor(), "Organisation");
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new OrganisationDocumentExtractor(), "Organisation");
  }

  //-------------------------------------------------------------------------
  @Override
  public OrganisationHistoryResult history(final OrganisationHistoryRequest request) {
    return doHistory(request, new OrganisationHistoryResult(), new OrganisationDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected OrganisationDocument insert(final OrganisationDocument document) {
    ArgumentChecker.notNull(document.getOrganisation(), "document.organisation");
    ArgumentChecker.notNull(document.getOrganisation().getObligor(), "document.organisation.obligor");

    ArgumentChecker.notNull(document.getOrganisation().getObligor().getObligorShortName(),
                            "organisation.trade.obligor_short_name");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getObligorREDCode(),
                            "organisation.trade.obligor_red_code");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getObligorTicker(),
                            "organisation.trade.obligor_ticker");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getCompositeRating(),
                            "organisation.trade.obligor_composite_rating");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getCountry(), "organisation.trade.obligor_country");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getFitchCreditRating(),
                            "organisation.trade.obligor_fitch_credit_rating");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getMoodysCreditRating(),
                            "organisation.trade.obligor_moodys_credit_rating");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getRegion(), "organisation.trade.obligor_region");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getSector(), "organisation.trade.obligor_sector");
    ArgumentChecker.notNull(document.getOrganisation().getObligor().getStandardAndPoorsCreditRating(),
                            "organisation.trade.obligor_standard_and_poors_credit_rating");

    final long docId = nextId("org_organisation_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    final UniqueId organisationUid = createUniqueId(docOid, docId);
    final ManageableOrganisation organisation = document.getOrganisation();

    // the arguments for inserting into the organisation table
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
        .addValue("obligor_short_name", organisation.getObligor().getObligorShortName(), Types.VARCHAR)
        .addValue("obligor_red_code", organisation.getObligor().getObligorREDCode(), Types.VARCHAR)
        .addValue("obligor_ticker", organisation.getObligor().getObligorTicker(), Types.VARCHAR)
        .addValue("obligor_composite_rating", organisation.getObligor().getCompositeRating().name(), Types.VARCHAR)
        .addValue("obligor_country", organisation.getObligor().getCountry(), Types.VARCHAR)
        .addValue("obligor_fitch_credit_rating", organisation.getObligor().getFitchCreditRating().name(), Types.VARCHAR)
        .addValue("obligor_implied_rating", organisation.getObligor().getImpliedRating().name(), Types.VARCHAR)
        .addValue("obligor_moodys_credit_rating",
                  organisation.getObligor().getMoodysCreditRating().name(),
                  Types.VARCHAR)
        .addValue("obligor_region", organisation.getObligor().getRegion().name(), Types.VARCHAR)
        .addValue("obligor_sector", organisation.getObligor().getSector().name(), Types.VARCHAR)
        .addValue("obligor_standard_and_poors_credit_rating",
                  organisation.getObligor().getStandardAndPoorsCreditRating().name(),
                  Types.VARCHAR)
        .addValue("obligor_has_defaulted",
                  organisation.getObligor().isHasDefaulted() ? 1 : 0,
                  Types.TINYINT);


    final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);

    // set the uniqueId
    organisation.setUniqueId(organisationUid);
    document.setUniqueId(organisationUid);
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableOrganisation getOrganisation(final UniqueId uniqueId) {
    return get(uniqueId).getOrganisation();
  }

  //-------------------------------------------------------------------------
  @Override
  public AbstractHistoryResult<OrganisationDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    OrganisationHistoryRequest historyRequest = new OrganisationHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest);
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a OrganisationDocument.
   */
  protected final class OrganisationDocumentExtractor implements ResultSetExtractor<List<OrganisationDocument>> {
    private long _lastOrganisationId = -1;
    private ManageableOrganisation _organisation;
    private List<OrganisationDocument> _documents = new ArrayList<OrganisationDocument>();

    @Override
    public List<OrganisationDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long organisationId = rs.getLong("ORGANISATION_ID");
        if (_lastOrganisationId != organisationId) {
          _lastOrganisationId = organisationId;
          buildOrganisation(rs, organisationId);
        }
      }
      return _documents;
    }

    private void buildOrganisation(final ResultSet rs, final long organisationId) throws SQLException {
      final long organisationOid = rs.getLong("ORGANISATION_OID");
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


      _organisation = new ManageableOrganisation(
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
      _organisation.setUniqueId(createUniqueId(organisationOid, organisationId));
      OrganisationDocument doc = new OrganisationDocument(_organisation);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(createUniqueId(organisationOid, organisationId));
      if (providerScheme != null && providerValue != null) {
        doc.setProviderId(ExternalId.of(providerScheme, providerValue));
      }
      _documents.add(doc);
    }
  }

}
