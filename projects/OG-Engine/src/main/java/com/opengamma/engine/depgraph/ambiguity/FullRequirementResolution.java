/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Service for checking whether, and how, any given value requirement may be ambiguous.
 */
public final class FullRequirementResolution {

  private final ValueRequirement _requirement;
  private final List<Collection<RequirementResolution>> _resolutions = new ArrayList<Collection<RequirementResolution>>();
  private int _hashCode;

  public FullRequirementResolution(final ValueRequirement requirement) {
    _requirement = ArgumentChecker.notNull(requirement, "requirement");
    _hashCode = getClass().hashCode() * 31 + _requirement.hashCode();
  }

  /**
   * Returns the requirement this resolution result describes.
   * 
   * @return the requirement, not null
   */
  public ValueRequirement getRequirement() {
    return _requirement;
  }

  /**
   * Returns all of the possible resolutions in descending priority order.
   * <p>
   * Each entry in the list will, under non-ambiguous circumstances, be a singleton collection. When an ambiguity exists elements will be sets containing all of the possible resolutions with null
   * included if non-resolution is a possible outcome.
   * 
   * @return the resolutions, not null
   */
  public List<Collection<RequirementResolution>> getResolutions() {
    return Collections.unmodifiableList(_resolutions);
  }

  // Note: there is not a addResolution method to avoid confusion. It would add a singleton set; repeated calls to it would not be the same as making a single call to addResolutions which is the
  // more typical behavior when there appear to be bulk and single operation

  /**
   * Stores a resolution result set into the overall result. If this is a non-ambiguous result, the collection must contain only a single value. If this is an ambiguous result, the collection may
   * contain all possible values and null if one of the outcomes was non-resolution.
   * 
   * @param resolutions the possible resolutions for this value requirement, not null, and non-empty
   * @throws IllegalArgumentException if the parameters are invalid or attempt to create a recursive structure
   */
  public void addResolutions(Collection<RequirementResolution> resolutions) {
    resolutions = Collections.unmodifiableSet(new HashSet<RequirementResolution>(resolutions));
    final int size = resolutions.size();
    if (size == 0) {
      // Empty is not allowed
      throw new IllegalArgumentException("resolutions");
    } else if (size == 1) {
      if (resolutions.contains(null)) {
        // Nulls are only allowed when there is an ambiguity
        throw new IllegalArgumentException("resolutions");
      }
      resolutions = Collections.singleton(resolutions.iterator().next());
    }
    for (RequirementResolution resolution : resolutions) {
      if ((resolution != null) && resolution.contains(this)) {
        throw new IllegalArgumentException("Circular reference from " + resolution + " to " + this);
      }
    }
    _resolutions.add(resolutions);
    _hashCode = (_hashCode * 31) + resolutions.hashCode();
  }

  /**
   * Tests whether the given resolution is present in any of the inputs to this resolution. This prevents recursive structures from being constructed.
   */
  /* package */boolean contains(final FullRequirementResolution parent) {
    for (Collection<RequirementResolution> resolutions : _resolutions) {
      for (RequirementResolution resolution : resolutions) {
        if ((resolution != null) && resolution.contains(parent)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Tests if there is any ambiguity in this resolution. This only applies to the exact value requirement; there may be ambiguities in resolving items deeper in the graph but which have no effect on
   * this requirement.
   * 
   * @return true if there is an ambiguity, false otherwise
   */
  public boolean isAmbiguous() {
    for (Collection<RequirementResolution> resolutions : _resolutions) {
      if (resolutions.size() > 1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests if there is any ambiguity in this resolution or any of its inputs.
   * 
   * @return true if there is an ambiguity, false otherwise
   */
  public boolean isDeeplyAmbiguous() {
    for (Collection<RequirementResolution> resolutions : _resolutions) {
      if (resolutions.size() > 1) {
        return true;
      }
      if (resolutions.iterator().next().isAmbiguous()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests if there is a successful resolution of this requirement.
   * 
   * @return true if there is a resolution, false otherwise
   */
  public boolean isResolved() {
    return !_resolutions.isEmpty();
  }

  // Object

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof FullRequirementResolution)) {
      return false;
    }
    final FullRequirementResolution other = (FullRequirementResolution) o;
    return (_hashCode == other._hashCode) && _requirement.equals(other._requirement) && _resolutions.equals(other._resolutions);
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public String toString() {
    return _requirement + "->" + _resolutions;
  }

}
