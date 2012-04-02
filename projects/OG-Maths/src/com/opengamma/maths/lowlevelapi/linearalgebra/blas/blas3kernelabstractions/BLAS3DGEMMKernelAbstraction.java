/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas3kernelabstractions;

import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.MatrixPrimitive;

/**
 * BLAS2*KernelAbstraction classes are used to kinda emulate function pointer passing.
 * We want a unified set of BLAS templates with Matrix type, machine and performance specific kernels.
 * This class helps the class hierarchy mangle achieve this for DGEMM.
 *
 * If you want to add a new BLAS3 DGEMM kernel set for matrix types of your choice then just implement this
 * and update the corresponding hashmap in BLAS3.
 * @param <TYPEA> a kind of matrix A
 * @param <TYPEB> a kind of matrix B
 * @param <TYPEC> a kind of matrix C
 * @param <TYPER> return type. TODO: Fix this, needs to happen via visitors or similar. 
 * Function names starting with "dm_" are "direct mathematics" functions, their names are deliberately written with
 * underscores in to split out the mathematical operations they perform for ease of reading.
 * 
 */
public abstract class BLAS3DGEMMKernelAbstraction<TYPER extends Matrix<Double>, TYPEA extends Matrix<Double>, TYPEB extends Matrix<Double>, TYPEC extends Matrix<Double>> {
  // yet another everything with everything combo magic situation, think we have something like 8 types which means 8!/(6!)=56 variants to implement!
  //GROUP1:: A*B
  //GROUP2:: AT*B
  //GROUP3:: A*BT
  //GROUP4:: AT*BT
  //GROUP5:: alpha*A*B
  //GROUP6:: alpha*AT*B
  //GROUP7:: alpha*A*BT
  //GROUP8:: alpha*AT*BT
  //GROUP9:: A*B + C
  //GROUP10:: AT*B + C
  //GROUP11:: A*BT + C
  //GROUP12:: AT*BT + C
  //GROUP13:: alpha*A*B + C
  //GROUP14:: alpha*AT*B + C
  //GROUP15:: alpha*A*BT + C
  //GROUP16:: alpha*AT*BT + C
  //GROUP17:: A*B + beta*C
  //GROUP18:: AT*B + beta*C
  //GROUP19:: A*BT + beta*C
  //GROUP20:: AT*BT + beta*C
  //GROUP21:: alpha*A*B + beta*C
  //GROUP22:: alpha*AT*B + beta*C
  //GROUP23:: alpha*A*BT + beta*C
  //GROUP24:: alpha*AT*BT + beta*C

  /* Stateless manipulators */
  //  {

  /* GROUP1:: A*B */
  /**
   * Stateless DGEMM:: returns A*B
   */
  public abstract MatrixPrimitive dm_stateless_A_times_B(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP2:: AT*B */
  /**
   * Stateless DGEMM:: returns AT*B
   */
  public abstract double[] dm_stateless_AT_times_B(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP3:: A*BT */
  /**
   * Stateless DGEMM:: returns A*BT
   */
  public abstract double[] dm_stateless_A_times_BT(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP4:: AT*BT */
  /**
   * Stateless DGEMM:: returns AT*BT
   */
  public abstract double[] dm_stateless_AT_times_BT(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP5:: alpha*A*B */
  /**
   * Stateless DGEMM:: returns alpha*A*B
   */
  public abstract double[] dm_stateless_alpha_A_times_B(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP6:: alpha*AT*B */
  /**
   * Stateless DGEMM:: returns alpha*AT*B
   */
  public abstract double[] dm_stateless_alpha_AT_times_B(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP7:: alpha*A*BT */
  /**
   * Stateless DGEMM:: returns alpha*A*BT
   */
  public abstract double[] dm_stateless_alpha_A_times_BT(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP8:: alpha*AT*BT */
  /**
   * Stateless DGEMM:: returns alpha*AT*BT
   */
  public abstract double[] dm_stateless_alpha_AT_times_BT(TYPEA A, TYPEB B); //CSIGNORE

  /* GROUP9:: A*B+C */
  /**
   * Stateless DGEMM:: returns A*B+C
   */
  public abstract double[] dm_stateless_A_times_B_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP10:: AT*B+C */
  /**
   * Stateless DGEMM:: returns AT*B+C
   */
  public abstract double[] dm_stateless_AT_times_B_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP11:: A*BT+C */
  /**
   * Stateless DGEMM:: returns A*BT+C
   */
  public abstract double[] dm_stateless_A_times_BT_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP12:: AT*BT+C */
  /**
   * Stateless DGEMM:: returns AT*BT+C
   */
  public abstract double[] dm_stateless_AT_times_BT_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP13:: alpha*A*B+C */
  /**
   * Stateless DGEMM:: returns alpha*A*B+C
   */
  public abstract double[] dm_stateless_alpha_A_times_B_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP14:: alpha*AT*B+C */
  /**
   * Stateless DGEMM:: returns alpha*AT*B+C
   */
  public abstract double[] dm_stateless_alpha_AT_times_B_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP15:: alpha*A*BT+C */
  /**
   * Stateless DGEMM:: returns alpha*A*BT+C
   */
  public abstract double[] dm_stateless_alpha_A_times_BT_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP16:: alpha*AT*BT+C */
  /**
   * Stateless DGEMM:: returns alpha*AT*BT+C
   */
  public abstract double[] dm_stateless_alpha_AT_times_BT_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP17:: A*B+beta*C */
  /**
   * Stateless DGEMM:: returns A*B+beta*C
   */
  public abstract double[] dm_stateless_A_times_B_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP18:: AT*B+beta*C */
  /**
   * Stateless DGEMM:: returns AT*B+beta*C
   */
  public abstract double[] dm_stateless_AT_times_B_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP19:: A*BT+beta*C */
  /**
   * Stateless DGEMM:: returns A*BT+beta*C
   */
  public abstract double[] dm_stateless_A_times_BT_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP20:: AT*BT+beta*C */
  /**
   * Stateless DGEMM:: returns AT*BT+beta*C
   */
  public abstract double[] dm_stateless_AT_times_BT_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP21:: alpha*A*B+beta*C */
  /**
   * Stateless DGEMM:: returns alpha*A*B+beta*C
   */
  public abstract double[] dm_stateless_alpha_times_A_times_B_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP22:: alpha*AT*B+beta*C */
  /**
   * Stateless DGEMM:: returns alpha*AT*B+beta*C
   */
  public abstract double[] dm_stateless_alpha_times_AT_times_B_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP23:: alpha*A*BT+beta*C */
  /**
   * Stateless DGEMM:: returns alpha*A*BT+beta*C
   */
  public abstract double[] dm_stateless_alpha_times_A_times_BT_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP24:: alpha*AT*BT+beta*C */
  /**
   * Stateless DGEMM:: returns alpha*AT*BT+beta*C
   */
  public abstract double[] dm_stateless_alpha_times_AT_times_BT_plus_beta_times_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  //} end stateless

  //{ Stateful
  //GROUP1:: A*B
  //GROUP2:: AT*B
  //GROUP3:: A*BT
  //GROUP4:: AT*BT
  //GROUP5:: alpha*A*B
  //GROUP6:: alpha*AT*B
  //GROUP7:: alpha*A*BT
  //GROUP8:: alpha*AT*BT
  //GROUP9:: A*B + C
  //GROUP10:: AT*B + C
  //GROUP11:: A*BT + C
  //GROUP12:: AT*BT + C
  //GROUP13:: alpha*A*B + C
  //GROUP14:: alpha*AT*B + C
  //GROUP15:: alpha*A*BT + C
  //GROUP16:: alpha*AT*BT + C
  //GROUP17:: A*B + beta*C
  //GROUP18:: AT*B + beta*C
  //GROUP19:: A*BT + beta*C
  //GROUP20:: AT*BT + beta*C
  //GROUP21:: alpha*A*B + beta*C
  //GROUP22:: alpha*AT*B + beta*C
  //GROUP23:: alpha*A*BT + beta*C
  //GROUP24:: alpha*AT*BT + beta*C

  /* groups 1-8 are meaningless for inplace ops as it is C that is modified inplace and groups 1-8 do not contain C.
  //GROUP1:: A*B
  //GROUP2:: AT*B
  //GROUP3:: A*BT
  //GROUP4:: AT*BT
  //GROUP5:: alpha*A*B
  //GROUP6:: alpha*AT*B
  //GROUP7:: alpha*A*BT
  //GROUP8:: alpha*AT*BT
  */

  /* GROUP9:: performs C:= A*B+C */
  /**
   * In place DGEMM::  performs C:= A*B+C
   */
  public abstract void dm_inplace_A_times_B_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP10:: performs C:=  AT*B+C */
  /**
   * In place DGEMM::  performs C:= AT*B+C
   */
  public abstract void dm_inplace_AT_times_B_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP11:: performs C:=  A*BT+C */
  /**
   * In place DGEMM::  performs C:= A*BT+C
   */
  public abstract void dm_inplace_A_times_BT_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP12:: performs C:=  AT*BT+C */
  /**
   * In place DGEMM::  performs C:= AT*BT+C
   */
  public abstract void dm_inplace_AT_times_BT_plus_C(TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP13:: performs C:=  alpha*A*B+C */
  /**
   * In place DGEMM::  performs C:= alpha*A*B+C
   */
  public abstract void dm_inplace_alpha_A_times_B_plus_C(double alpha, TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP14:: performs C:=  alpha*AT*B+C */
  /**
   * In place DGEMM::  performs C:= alpha*AT*B+C
   */
  public abstract void dm_inplace_alpha_AT_times_B_plus_C(double alpha, TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP15:: performs C:=  alpha*A*BT+C */
  /**
   * In place DGEMM::  performs C:= alpha*A*BT+C
   */
  public abstract void dm_inplace_alpha_A_times_BT_plus_C(double alpha, TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP16:: alpha*AT*BT+C */
  /**
   * In place DGEMM::  performs C:= alpha*AT*BT+C
   */
  public abstract void dm_inplace_alpha_AT_times_BT_plus_C(double alpha, TYPEA A, TYPEB B, TYPEC C); //CSIGNORE

  /* GROUP17:: A*B+beta*C */
  /**
   * In place DGEMM::  performs C:= A*B+beta*C
   */
  public abstract void dm_inplace_A_times_B_plus_beta_times_C(TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP18:: AT*B+beta*C */
  /**
   * In place DGEMM::  performs C:= AT*B+beta*C
   */
  public abstract void dm_inplace_AT_times_B_plus_beta_times_C(TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP19:: A*BT+beta*C */
  /**
   * In place DGEMM::  performs C:= A*BT+beta*C
   */
  public abstract void dm_inplace_A_times_BT_plus_beta_times_C(TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP20:: AT*BT+beta*C */
  /**
   * In place DGEMM:: returns AT*BT+beta*C
   */
  public abstract void dm_inplace_AT_times_BT_plus_beta_times_C(TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP21:: alpha*A*B+beta*C */
  /**
   * In place DGEMM::  performs C:= alpha*A*B+beta*C
   */
  public abstract void dm_inplace_alpha_times_A_times_B_plus_beta_times_C(double alpha, TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP22:: alpha*AT*B+beta*C */
  /**
   * In place DGEMM::  performs C:= alpha*AT*B+beta*C
   */
  public abstract void dm_inplace_alpha_times_AT_times_B_plus_beta_times_C(double alpha, TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP23:: alpha*A*BT+beta*C */
  /**
   * In place DGEMM::  performs C:= alpha*A*BT+beta*C
   */
  public abstract void dm_inplace_alpha_times_A_times_BT_plus_beta_times_C(double alpha, TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  /* GROUP24:: alpha*AT*BT+beta*C */
  /**
   * In place DGEMM::  performs C:= alpha*AT*BT+beta*C
   */
  public abstract void dm_inplace_alpha_times_AT_times_BT_plus_beta_times_C(double alpha, TYPEA A, TYPEB B, double beta, TYPEC C); //CSIGNORE

  //} end stateful

}
