package com.opengamma.financial.security;

import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
public interface CommodityForwardSecurityVisitor<T> {

  T visitEnergyForwardSecurity(EnergyForwardSecurity security);

  T visitAgricultureForwardSecurity(AgricultureForwardSecurity security);

  T visitMetalForwardSecurity(MetalForwardSecurity security);
}
