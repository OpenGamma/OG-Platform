/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.position;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.production.tool.AbstractProductionTool;
import com.opengamma.util.db.DbDateUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.time.InstantProvider;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tool to remove positions with expired bonds.
 * <p>
 * This will not remove the bonds themselves, or the references from portfolios.
 */
public class ExpiredBondRemovePositionTool extends AbstractProductionTool {

  /** The scheme to use. */
  private static final String SCHEME = "BLOOMBERG_BUID";
  /** The period to remove. */
  private static final Period IN_FUTURE = Period.ofMonths(3);
  /** Whether to actually remove the positions. */
  private static final boolean REMOVE = false;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExpiredBondRemovePositionTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the tool.
   */
  @Override 
  protected void doRun() {
    DbSecurityMaster secMaster = (DbSecurityMaster) getToolContext().getSecurityMaster();
    PositionMaster posMaster = getToolContext().getPositionMaster();
    List<UniqueId> secIds = findSecurityIds(secMaster);
    List<ExternalId> secKeys = findSecurityKeys(secMaster, secIds);
    removePositions(posMaster, secKeys);
  }

  private static List<UniqueId> findSecurityIds(DbSecurityMaster secMaster) {
    InstantProvider instant = OffsetDateTime.now().plus(IN_FUTURE);
    String selectExpiredBonds = "SELECT security_id FROM sec_bond WHERE maturity_date < ?";
    String selectSecurityUids = "SELECT id, oid FROM sec_security WHERE id IN (" + selectExpiredBonds + ")";
    final SimpleJdbcTemplate template = secMaster.getDbConnector().getJdbcTemplate();
    List<Map<String, Object>> results = template.queryForList(selectSecurityUids, DbDateUtils.toSqlTimestamp(instant));
    List<UniqueId> secIds = new ArrayList<UniqueId>();
    for (Map<String, Object> map : results) {
      UniqueId secId = secMaster.createUniqueId((Long) map.get("OID"), (Long) map.get("ID"));
      secIds.add(secId);
      System.out.println("Security id: " + secId);
    }
    return secIds;
  }

  private static List<ExternalId> findSecurityKeys(SecurityMaster secMaster, List<UniqueId> secIds) {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setObjectIds(secIds);
    SecuritySearchResult result = secMaster.search(request);
    List<ExternalId> secKeys = new ArrayList<ExternalId>();
    for (ManageableSecurity sec : result.getSecurities()) {
      ExternalId secKey = sec.getExternalIdBundle().getExternalId(ExternalScheme.of(SCHEME));
      if (secKey != null) {
        secKeys.add(secKey);
        System.out.println("Security key: " + secKey);
      }
    }
    return secKeys;
  }

  private static void removePositions(PositionMaster posMaster, List<ExternalId> secKeys) {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdSearch(new ExternalIdSearch(secKeys));
    PositionSearchResult result = posMaster.search(request);
    for (PositionDocument posDoc : result.getDocuments()) {
      System.out.println("Position to remove: " + posDoc);
      if (REMOVE) {
        posMaster.remove(posDoc.getUniqueId());
      }
    }
  }

}
