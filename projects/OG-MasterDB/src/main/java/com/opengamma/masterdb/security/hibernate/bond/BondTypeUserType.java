/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the BondType enum
 */
public class BondTypeUserType extends EnumUserType<BondType> {

  private static final String CORPORATE_BOND_TYPE = "Corporate";
  private static final String GOVERNMENT_BOND_TYPE = "Government";
  private static final String MUNICIPAL_BOND_TYPE = "Municipal";
  private static final String INFLATION_BOND_TYPE = "Inflation";

  public BondTypeUserType() {
    super(BondType.class, BondType.values());
  }

  @Override
  protected String enumToStringNoCache(BondType value) {
    return value.accept(new FinancialSecurityVisitorAdapter<String>() {

      @Override
      public String visitCorporateBondSecurity(CorporateBondSecurity bond) {
        return CORPORATE_BOND_TYPE;
      }

      @Override
      public String visitGovernmentBondSecurity(GovernmentBondSecurity bond) {
        return GOVERNMENT_BOND_TYPE;
      }

      @Override
      public String visitMunicipalBondSecurity(MunicipalBondSecurity bond) {
        return MUNICIPAL_BOND_TYPE;
      }
      
      @Override
      public String visitInflationBondSecurity(InflationBondSecurity bond) {
        return INFLATION_BOND_TYPE;
      }
    });
  }

}
