/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;

/**
 * Bond type enumeration.
 */
public enum BondType {
  /**
   * 
   */
  CORPORATE,
  /**
   * 
   */
  MUNICIPAL,
  /**
   * 
   */
  GOVERNMENT,
  /**
   * 
   */
  INFLATION;
  
  public static BondType identify(final BondSecurity object) {
    return object.accept(new FinancialSecurityVisitorAdapter<BondType>() {

      @Override
      public BondType visitCorporateBondSecurity(CorporateBondSecurity security) {
        return CORPORATE;
      }

      @Override
      public BondType visitGovernmentBondSecurity(
          GovernmentBondSecurity security) {
        return GOVERNMENT;
      }

      @Override
      public BondType visitMunicipalBondSecurity(MunicipalBondSecurity security) {
        return MUNICIPAL;
      }
      
      @Override
      public BondType visitInflationBondSecurity(InflationBondSecurity security) {
        return INFLATION;
      }
      
    });
  }
  
  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    switch (this) {
      case CORPORATE:
        return visitor.visitCorporateBondSecurity(null);
      case GOVERNMENT:
        return visitor.visitGovernmentBondSecurity(null);
      case MUNICIPAL:
        return visitor.visitMunicipalBondSecurity(null);
      case INFLATION:
        return visitor.visitInflationBondSecurity(null);
      default:
        throw new OpenGammaRuntimeException("unexpected BondType: " + this);
    } 
  }

}
