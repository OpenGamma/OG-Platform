/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.BondSecurityVisitor;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;

public enum BondType {
  CORPORATE,
  MUNICIPAL,
  GOVERNMENT;
  
  public static BondType identify (final BondSecurity object) {
    return object.accept (new BondSecurityVisitor<BondType> () {

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
      
    });
  }
  
  public static interface Visitor<T> {
    public T visitCorporateBondType ();
    public T visitGovernmentBondType ();
    public T visitMunicipalBondType ();
  }
  
  public <T> T accept (final Visitor<T> visitor) {
    switch (this) {
    case CORPORATE : return visitor.visitCorporateBondType ();
    case GOVERNMENT : return visitor.visitGovernmentBondType ();
    case MUNICIPAL : return visitor.visitMunicipalBondType ();
    default : throw new OpenGammaRuntimeException ("unexpected BondType: " + this);
    } 
  }
  
}