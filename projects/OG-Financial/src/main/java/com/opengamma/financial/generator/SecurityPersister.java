/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.util.HashMap;
import java.util.Map;

import org.joda.beans.JodaBeanUtils;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;

/**
 * Service for storing securities and returning identifiers to reference them from portfolios.
 */
public abstract class SecurityPersister {

  private final Map<ManageableSecurity, ExternalIdBundle> _securities = new HashMap<ManageableSecurity, ExternalIdBundle>();
  private final ExternalScheme _scheme;

  public SecurityPersister(final ExternalScheme scheme) {
    _scheme = scheme;
  }

  public SecurityPersister() {
    this(ExternalScheme.of("RANDOM_SECURITY_GENERATOR"));
  }

  protected abstract void storeSecurityImpl(ManageableSecurity security);

  public ExternalScheme getScheme() {
    return _scheme;
  }

  protected String createGuid() {
    return GUIDGenerator.generate().toString();
  }

  /**
   * Produces an {@link ExternalIdBundle} referencing the security.
   * 
   * @param security the security to store, not null
   * @return the identifier bundle, not null
   */
  public final ExternalIdBundle storeSecurity(final ManageableSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final ManageableSecurity clone = JodaBeanUtils.clone(security);
    clone.setUniqueId(null);
    clone.setExternalIdBundle(clone.getExternalIdBundle().withoutScheme(getScheme()));
    ExternalIdBundle identifiers = _securities.get(clone);
    if (identifiers != null) {
      return identifiers;
    }
    final ExternalId guid = ExternalId.of(getScheme(), createGuid());
    identifiers = security.getExternalIdBundle().withExternalId(guid);
    security.setExternalIdBundle(identifiers);
    _securities.put(clone, identifiers);
    storeSecurityImpl(security);
    return identifiers;
  }

}
