/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.bond.BondSecuritySearchRequest;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code FinancialSecuritySource} implemented using an underlying {@code SecurityMaster}.
 * <p>
 * The {@link SecuritySource} interface provides securities to the engine via a narrow API. This class provides the source on top of a standard {@link SecurityMaster}.
 */
public class MasterFinancialSecuritySource extends MasterSecuritySource implements FinancialSecuritySource {

  /**
   * Creates an instance with an underlying master.
   * 
   * @param master the master, not null
   */
  public MasterFinancialSecuritySource(final SecurityMaster master) {
    super(master);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds the all the available bonds with an issuer type of the provided string. This would be used, for example, to construct a bond curve.
   * 
   * @param issuerName the issuer name, wildcards allowed, may be null
   * @return a collection of bond securities with the issuerType specified, not null
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    ArgumentChecker.notNull(issuerName, "issuerName");
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName(issuerName);
    return (Collection) search(request).getSecurities(); // cast safe as supplied list will not be altered
  }

}
