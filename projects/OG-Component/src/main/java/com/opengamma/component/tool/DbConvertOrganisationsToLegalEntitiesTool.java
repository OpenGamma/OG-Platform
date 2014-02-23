/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.Capability;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.masterdb.legalentity.DbLegalEntityBeanMaster;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.tool.DbToolContext;

/**
 * Tool converting Organistaion Database to Legal Entity Database
 */
@Scriptable
public class DbConvertOrganisationsToLegalEntitiesTool extends AbstractDbTool<DbToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(DbConvertOrganisationsToLegalEntitiesTool.class);

  @Override
  protected void doRun(boolean write, File outputFile) throws Exception {
    String getAllSql = "SELECT ID,OID,VER_FROM_INSTANT,VER_TO_INSTANT,CORR_FROM_INSTANT,CORR_TO_INSTANT," +
        "PROVIDER_SCHEME,PROVIDER_VALUE,OBLIGOR_SHORT_NAME,OBLIGOR_RED_CODE,OBLIGOR_TICKER,OBLIGOR_COUNTRY," +
        "OBLIGOR_REGION,OBLIGOR_SECTOR,OBLIGOR_COMPOSITE_RATING,OBLIGOR_IMPLIED_RATING,OBLIGOR_FITCH_CREDIT_RATING," +
        "OBLIGOR_MOODYS_CREDIT_RATING,OBLIGOR_STANDARD_AND_POORS_CREDIT_RATING,OBLIGOR_HAS_DEFAULTED FROM ORG_ORGANISATION";

    Connection conn = getDbToolContext().getDbConnector().getDataSource().getConnection();

    Statement stmt = conn.createStatement(
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
    stmt.setFetchSize(Integer.MIN_VALUE);
    // The combination of a forward-only, read-only result set, with a fetch size of Integer.MIN_VALUE serves as 
    // a signal to the driver to stream result sets row-by-row. After this any result sets created with the statement 
    // will be retrieved row-by-row.
    ResultSet rs = stmt.executeQuery(getAllSql);

    LegalEntityMaster legalEntityMaster = new DbLegalEntityBeanMaster(getDbToolContext().getDbConnector());

    while (rs.next()) {
      long id = rs.getLong("ID");
      long oid = rs.getLong("OID");
      Instant verFrom = DbDateUtils.fromSqlTimestamp(rs.getTimestamp("VER_FROM_INSTANT"));
      Instant verTo = DbDateUtils.fromSqlTimestamp(rs.getTimestamp("VER_TO_INSTANT"));
      Instant corrFrom = DbDateUtils.fromSqlTimestamp(rs.getTimestamp("CORR_FROM_INSTANT"));
      Instant corrTo = DbDateUtils.fromSqlTimestamp(rs.getTimestamp("CORR_TO_INSTANT"));
      String providerScheme = rs.getString("PROVIDER_SCHEME");
      String providerValue = rs.getString("PROVIDER_VALUE");
      String name = rs.getString("OBLIGOR_SHORT_NAME");
      String redCode = rs.getString("OBLIGOR_RED_CODE");
      String ticker = rs.getString("OBLIGOR_TICKER");
      String country = rs.getString("OBLIGOR_COUNTRY");
      String region = rs.getString("OBLIGOR_REGION");
      String sector = rs.getString("OBLIGOR_SECTOR");
      String compositeRating = rs.getString("OBLIGOR_COMPOSITE_RATING");
      String impliedRating = rs.getString("OBLIGOR_IMPLIED_RATING");
      String fitchCreditRating = rs.getString("OBLIGOR_FITCH_CREDIT_RATING");
      String moodysCreditRating = rs.getString("OBLIGOR_MOODYS_CREDIT_RATING");
      String standardAndPoorsCreditRating = rs.getString("OBLIGOR_STANDARD_AND_POORS_CREDIT_RATING");
      String hasDefaulted = rs.getString("OBLIGOR_HAS_DEFAULTED");

      ManageableLegalEntity legalEntity = new ManageableLegalEntity(name, ExternalIdBundle.of(ExternalSchemes.MARKIT_RED_CODE, redCode));
      legalEntity.setUniqueId(UniqueId.of(DbLegalEntityBeanMaster.IDENTIFIER_SCHEME_DEFAULT, Long.toString(oid), Long.toString(id - oid)));
      legalEntity.addExternalId(ExternalId.of("TICKER", ticker));
      legalEntity.addAttribute("country", country);
      legalEntity.addAttribute("region", region);
      legalEntity.addAttribute("sector", sector);
      legalEntity.addAttribute("hasDefaulted", hasDefaulted);

      legalEntity.addDetail("providerScheme", providerScheme);
      legalEntity.addDetail("providerValue", providerValue);

      legalEntity.getCapabilities().add(new Capability("ISSUER"));
      legalEntity.getRatings().add(new Rating("composite", CreditRating.valueOf(compositeRating), null));
      legalEntity.getRatings().add(new Rating("implied", CreditRating.valueOf(impliedRating), null));
      legalEntity.getRatings().add(new Rating("Fitch", CreditRating.valueOf(fitchCreditRating), null));
      legalEntity.getRatings().add(new Rating("Moodys", CreditRating.valueOf(moodysCreditRating), null));
      legalEntity.getRatings().add(new Rating("StandardAndPoors", CreditRating.valueOf(standardAndPoorsCreditRating), null));

      LegalEntityDocument document = new LegalEntityDocument();
      document.setCorrectionFromInstant(corrFrom);
      document.setCorrectionToInstant(corrTo);
      document.setVersionFromInstant(verFrom);
      document.setVersionToInstant(verTo);

      legalEntityMaster.add(document);
    }
  }

}
