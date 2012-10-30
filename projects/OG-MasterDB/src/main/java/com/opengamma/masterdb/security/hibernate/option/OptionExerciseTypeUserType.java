/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.financial.security.option.ExerciseTypeNameVisitor;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the OptionExerciseType enum
 */
public class OptionExerciseTypeUserType extends EnumUserType<OptionExerciseType> {

  public OptionExerciseTypeUserType() {
    super(OptionExerciseType.class, OptionExerciseType.values());
  }

  @Override
  protected String enumToStringNoCache(OptionExerciseType value) {
    return value.accept(new ExerciseTypeNameVisitor());
  }

}
