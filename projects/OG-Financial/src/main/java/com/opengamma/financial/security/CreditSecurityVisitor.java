/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;

/**
 * @param <T> The return type of the visitor
 */
public interface CreditSecurityVisitor<T> {

  T visitCDSSecurity(CDSSecurity security);

  T visitStandardVanillaCDSSecurity(StandardVanillaCDSSecurity security);

  T visitStandardFixedRecoveryCDSSecurity(StandardFixedRecoveryCDSSecurity security);

  T visitStandardRecoveryLockCDSSecurity(StandardRecoveryLockCDSSecurity security);

  T visitLegacyVanillaCDSSecurity(LegacyVanillaCDSSecurity security);

  T visitLegacyFixedRecoveryCDSSecurity(LegacyFixedRecoveryCDSSecurity security);

  T visitLegacyRecoveryLockCDSSecurity(LegacyRecoveryLockCDSSecurity security);
}
