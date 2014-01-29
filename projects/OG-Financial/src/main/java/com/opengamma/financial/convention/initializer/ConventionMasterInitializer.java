/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;

/**
 * A tool that allows a convention master to be initialized.
 * <p>
 * Conventions are typically stored in a master database, however they may be
 * initialized from code as they rarely change.
 */
public abstract class ConventionMasterInitializer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ConventionMasterInitializer.class);

  /**
   * Initializes the specified master.
   * 
   * @param master  the master to initialize, not null
   */
  public abstract void init(ConventionMaster master);

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

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
