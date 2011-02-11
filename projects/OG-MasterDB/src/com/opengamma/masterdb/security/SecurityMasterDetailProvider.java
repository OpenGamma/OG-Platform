/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Provider allowing the detail of loading and storing a security to be separated
 * from the main versioning.
 * <p>
 * The security structure is large and complex. This interface is designed to allow
 * the basic loading and the detail loading to be written separately and integrated.
 * For example, using JDBC for the basic load and Hibernate for the detail.
 */
public interface SecurityMasterDetailProvider {

  /**
   * Initializes the detail provider with the same database source as the master.
   * 
   * @param master  the security master, not null
   */
  void init(DbSecurityMaster master);

  /**
   * Loads the security based on the supplied base.
   * <p>
   * The caller will already have loaded the contents of {@code ManageableSecurity}
   * but will not have created a class of the correct type. The implementation
   * must load the full detail and copy the data from the base object to the result.
   * 
   * @param base  the base security, not null
   * @return the loaded security, not null
   */
  ManageableSecurity loadSecurityDetail(ManageableSecurity base);

  /**
   * Stores the specified security.
   * <p>
   * The caller will already have stored the contents of {@code ManageableSecurity}
   * so the implementation only needs to store details of the subclass.
   * 
   * @param security  the security to store, not null
   */
  void storeSecurityDetail(ManageableSecurity security);

  /**
   * Extends the search based on subclasses of the search request.
   * <p>
   * The implementation should check if the request is of a known additional type
   * and process it. The arguments may be updated and the SQL returned.
   * <p>
   * A no-op implementation will return {@code select + where}.
   * A simple search would add to the arguments and return
   * {@code select + "LEFT JOIN ... " + where + "AND ... "}.
   * 
   * @param request  the request to search for, not null
   * @param args  the search arguments, not null
   * @param select  the select clause to add to ending in a space, not null
   * @param where  the where clause to add to ending in a space, not null
   * @return the combined select and where clauses extended as necessary, ending in a space, not null
   */
  String extendSearch(SecuritySearchRequest request, DbMapSqlParameterSource args, String select, String where);

}
