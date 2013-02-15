/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Wrapper for a "primitive" value. This is to allow the {@link ComputationTargetType#PRIMITIVE} to be defined as something more specific than {@link UniqueIdentifiable} which causes function
 * resolution issues.
 */
public class Primitive implements UniqueIdentifiable {

  /**
   * Implementation of {@link Primitive} that is based on an external identifier
   */
  public static final class ExternalIdentifiablePrimitive extends Primitive implements ExternalIdentifiable {

    private final ExternalId _eid;

    public ExternalIdentifiablePrimitive(final UniqueId uid, final ExternalId eid) {
      super(uid);
      assert eid != null;
      _eid = eid;
    }

    @Override
    public ExternalId getExternalId() {
      return _eid;
    }

  }

  /**
   * Implementation of {@link Primitive} that is based on an external identifier bundle.
   */
  public static final class ExternalBundleIdentifiablePrimitive extends Primitive implements ExternalBundleIdentifiable {

    private final ExternalIdBundle _eids;

    public ExternalBundleIdentifiablePrimitive(final UniqueId uid, final ExternalIdBundle eids) {
      super(uid);
      assert eids != null;
      _eids = eids;
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return _eids;
    }

  }

  private final UniqueId _uid;

  public Primitive(final UniqueId uid) {
    assert uid != null;
    _uid = uid;
  }

  @Override
  public UniqueId getUniqueId() {
    return _uid;
  }

}
