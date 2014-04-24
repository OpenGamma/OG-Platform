/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * A tool that allows a convention master to be initialized.
 * <p>
 * Conventions are typically stored in a master database, however they may be
 * initialized from code as they rarely change.
 *
 * Convention lookup relies on appropriate securities being present see {@code com.opengamma.financial.security.index.IborIndex}.
 * Old style behaviour (without security based lookup) is preserved by calling the deprecated init() and associated functions.
 */
public abstract class ConventionMasterInitializer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ConventionMasterInitializer.class);

  /**
   * Initializes the specified master.
   * 
   * @param master  the master to initialize, not null
   * @deprecated use the init() that also takes a SecurityMaster
   */
  @Deprecated
  public abstract void init(ConventionMaster master);

  /**
   * Initializes the specified master.
   *
   * Default implementation, should be overridden by child if security master should be populated.
   *
   * @param master  the master to initialize, not null
   * @param securityMaster the security master, not null
   */
  public void init(ConventionMaster master, SecurityMaster securityMaster) {
    init(master);
  }

  /**
   * Adds a convention to the specified master.
   * 
   * @param master  the master to initialize, not null
   * @param convention  the convention to add, null ignored
   */
  protected void addConvention(ConventionMaster master, ManageableConvention convention) {
    if (convention != null) {
      ConventionSearchRequest request = new ConventionSearchRequest();
      request.setName(convention.getName());
      ConventionSearchResult result = master.search(request);
      switch (result.getDocuments().size()) {
        case 0:
          master.add(new ConventionDocument(convention));
          break;
        case 1:
          if (JodaBeanUtils.equalIgnoring(convention, result.getFirstConvention(), ManageableConvention.meta().uniqueId()) == false) {
            ConventionDocument doc = result.getFirstDocument();
            doc.setConvention(convention);
            master.update(doc);
          }
          break;
        default:
          // these are supposed to be unique by name in the database
          s_logger.warn("Multiple conventions with the same name in database: " + convention.getName());
          for (ManageableConvention similar : result.getConventions()) {
            if (JodaBeanUtils.equalIgnoring(convention, similar, ManageableConvention.meta().uniqueId())) {
              return;  // already in database
            }
          }
          master.add(new ConventionDocument(convention));
          break;
      }
    }
  }

  protected void addSecurity(SecurityMaster securityMaster, ManageableSecurity security) {
    if (securityMaster == null) {
      s_logger.warn("Tried to add a security to aid convention lookup but no security master set: " + security.getName());
      return;
    }
    SecurityMasterUtils.addOrUpdateSecurity(securityMaster, security);
  }

  protected void addIborSecurity(final SecurityMaster securityMaster, final VanillaIborLegConvention convention) {
    ArgumentChecker.notEmpty(convention.getExternalIdBundle(), "externalIdBundle");
    addSecurity(securityMaster, new IborIndex(convention.getName(), convention.getName(), convention.getResetTenor(), convention.getIborIndexConvention(), convention.getExternalIdBundle()));
  }

  protected void addOvernightSecurity(final SecurityMaster securityMaster, final OvernightIndexConvention convention) {
    ArgumentChecker.notEmpty(convention.getExternalIdBundle(), "externalIdBundle");
    addSecurity(securityMaster,
                new OvernightIndex(convention.getName(), convention.getName(), convention.getExternalIdBundle().iterator().next(),
                                   convention.getExternalIdBundle()));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
