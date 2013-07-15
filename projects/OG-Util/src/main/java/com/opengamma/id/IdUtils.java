/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.Instant;

/**
 * Utility class for working with identifiers.
 * <p>
 * This class is a thread-safe static utility class.
 */
public final class IdUtils {

  /**
   * Restricted constructor.
   */
  private IdUtils() {
  }

  /**
   * Sets the unique identifier of an object if it implements {@code MutableUniqueIdentifiable}.
   * <p>
   * This provides uniform access to objects that support having their unique identifier
   * updated after construction.
   * <p>
   * For example, code in the database layer will need to update the unique identifier
   * when the object is stored.
   *
   * @param object  the object to set into
   * @param uniqueId  the unique identifier to set, may be null
   */
  public static void setInto(Object object, UniqueId uniqueId) {
    if (object instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) object).setUniqueId(uniqueId);
    }
  }

  //-------------------------------------------------------------------------

  /**
   * Converts a list of {@code UniqueId} or {@code ObjectId} to a list of strings.
   *
   * @param ids  the ids to convert, null returns empty list
   * @return the string list, not null
   */
  public static List<String> toStringList(Iterable<? extends ObjectIdentifiable> ids) {
    List<String> strs = new ArrayList<String>();
    if (ids != null) {
      for (ObjectIdentifiable obj : ids) {
        if (obj instanceof UniqueId) {
          strs.add(obj.toString());
        } else {
          strs.add(obj.getObjectId().toString());
        }
      }
    }
    return strs;
  }

  /**
   * Converts a list of strings to a list of {@code UniqueId}.
   *
   * @param uniqueIdStrs  the identifiers to convert, null returns empty list
   * @return the list of unique identifiers, not null
   */
  public static List<UniqueId> parseUniqueIds(Iterable<String> uniqueIdStrs) {
    List<UniqueId> uniqueIds = new ArrayList<UniqueId>();
    if (uniqueIdStrs != null) {
      for (String uniqueIdStr : uniqueIdStrs) {
        uniqueIds.add(UniqueId.parse(uniqueIdStr));
      }
    }
    return uniqueIds;
  }

  /**
   * Converts a list of strings to a list of {@code ObjectId}.
   *
   * @param objectIdStrs  the identifiers to convert, null returns empty list
   * @return the list of unique identifiers, not null
   */
  public static List<ObjectId> parseObjectIds(Iterable<String> objectIdStrs) {
    List<ObjectId> objectIds = new ArrayList<ObjectId>();
    if (objectIdStrs != null) {
      for (String objectIdStr : objectIdStrs) {
        objectIds.add(ObjectId.parse(objectIdStr));
      }
    }
    return objectIds;
  }

  /**
   * Returns true if asOf instant is between from and to instants
   * @param asOf the asOf instant
   * @param from the from instant
   * @param to the from instant
   * @return asOf instant is between from and to instants             
   */
  public static boolean isWithinTimeBounds(Instant asOf, Instant from, Instant to) {
    return
      (asOf == null && to == null)
        ||
        (to == null || to.isAfter(asOf)) && (from == null || !from.isAfter(asOf));
  }

  /**
   * Retruns true if the version-corrections is bounded by given instants
   * @param vc the version-correction
   * @param versionFrom the version from instant
   * @param versionTo the version to instant
   * @param correctionFrom the correction from instant
   * @param correctionTo the correction from instant
   * @return the version-corrections is bounded by given instants
   */
  public static boolean isVersionCorrection(VersionCorrection vc, Instant versionFrom, Instant versionTo, Instant correctionFrom, Instant correctionTo) {
    return isWithinTimeBounds(vc.getVersionAsOf(), versionFrom, versionTo) && isWithinTimeBounds(vc.getCorrectedTo(), correctionFrom, correctionTo);
  }

}
