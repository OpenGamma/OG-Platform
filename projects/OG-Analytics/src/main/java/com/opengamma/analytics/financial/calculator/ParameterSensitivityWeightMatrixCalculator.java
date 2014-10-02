/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.list.IntArrayList;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Compute weight matrix used in {@link PortfolioHedgingCalculator}
 */
public class ParameterSensitivityWeightMatrixCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(ParameterSensitivityWeightMatrixCalculator.class);
  private static final double TOL = 1.0e-8;

  /**
   * Matrix projecting the nodes, to which portfolio has sensitivities, to new nodes (objNodes), to which sensitivity is accounted for, where the risks of each maturity 
   * is considered separately for each curve.
   * @param curves The multicurve
   * @param order The ordered set of name with number of parameters, should be the same as "order" used in {@link PortfolioHedgingCalculator}
   * @param objNodes The objective nodes on which sensitivity is to be accounted for
   * @return The matrix
   */
  public DoubleMatrix2D projectCurveNodes(final MulticurveProviderDiscount curves, final LinkedHashSet<Pair<String, Integer>> order, final double[] objNodes) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(order, "order");
    ArgumentChecker.notNull(objNodes, "objNodes");

    int nCurves = order.size();
    ArgumentChecker.isTrue(nCurves > 0, "order should not be empty");
    Iterator<Pair<String, Integer>> it = order.iterator();
    DoubleMatrix2D[] resArray = new DoubleMatrix2D[nCurves];
    int nRowsTotal = 0;
    int nColsTotal = 0;
    for (int i = 0; i < nCurves; ++i) {
      resArray[i] = projectSingleCurveNodes(curves, it.next(), objNodes);
      nRowsTotal += resArray[i].getNumberOfRows();
      nColsTotal += resArray[i].getNumberOfColumns();
    }

    double[][] res = new double[nRowsTotal][nColsTotal];
    int offsetRow = 0;
    int offsetCol = 0;
    for (int i = 0; i < nCurves; ++i) {
      int nRows = resArray[i].getNumberOfRows();
      int nCols = resArray[i].getNumberOfColumns();
      for (int j = 0; j < nRows; ++j) {
        for (int k = 0; k < nCols; ++k) {
          res[j + offsetRow][k + offsetCol] = resArray[i].getData()[j][k];
        }
      }
      offsetRow += nRows;
      offsetCol += nCols;
    }

    return new DoubleMatrix2D(res);
  }

  /**
   * Matrix projecting the nodes, to which portfolio has sensitivities, to new nodes (objNodes), to which sensitivity is accounted for, where the 
   * total risk of each maturity for all the relevant curves is considered by summing them up. 
   * NOTE THAT all of the relevant curves should contain all of the nodes in objNodes.
   * @param curves The multicurve
   * @param order The ordered set of name with number of parameters, should be the same as "order" used in {@link PortfolioHedgingCalculator}
   * @param objNodes The objective nodes on which sensitivity is to be accounted for
   * @return The matrix
   */
  public DoubleMatrix2D projectCurvesAndNodes(final MulticurveProviderDiscount curves, final LinkedHashSet<Pair<String, Integer>> order, final double[] objNodes) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(order, "order");
    ArgumentChecker.notNull(objNodes, "objNodes");

    int nCurves = order.size();
    ArgumentChecker.isTrue(nCurves > 0, "order should not be empty");
    Iterator<Pair<String, Integer>> it = order.iterator();
    DoubleMatrix2D[] resArray = new DoubleMatrix2D[nCurves];
    int nColsTotal = 0;
    for (int i = 0; i < nCurves; ++i) {
      resArray[i] = projectSingleCurveNodes(curves, it.next(), objNodes);
      nColsTotal += resArray[i].getNumberOfColumns();
    }
    for (int i = 0; i < nCurves; ++i) {
      ArgumentChecker.isTrue(objNodes.length == resArray[i].getNumberOfRows(), "All of the elements in objNodes should be found in the curves for this method");
    }
    int nRows = resArray[0].getNumberOfRows();

    double[][] res = new double[nRows][nColsTotal];
    int offsetCol = 0;
    for (int i = 0; i < nCurves; ++i) {
      int nCols = resArray[i].getNumberOfColumns();
      for (int j = 0; j < nRows; ++j) {
        for (int k = 0; k < nCols; ++k) {
          res[j][k + offsetCol] = resArray[i].getData()[j][k];
        }
      }
      offsetCol += nCols;
    }

    return new DoubleMatrix2D(res);
  }

  /**
   * Matrix reducing the nodes, to which portfolio has sensitivities, to new nodes (objNodes), to which sensitivity is accounted for. 
   * Thus the sensitivities to nodes which are not in objNodes are 
   * @param curves The multicurve
   * @param order The ordered set of name with number of parameters, should be the same as "order" used in {@link PortfolioHedgingCalculator}
   * @param objNodes The objective nodes on which sensitivity is to be accounted for
   * @return The matrix
   */
  public DoubleMatrix2D reduceCurveNodes(final MulticurveProviderDiscount curves, final LinkedHashSet<Pair<String, Integer>> order, final double[] objNodes) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(order, "order");
    ArgumentChecker.notNull(objNodes, "objNodes");

    int nCurves = order.size();
    ArgumentChecker.isTrue(nCurves > 0, "order should not be empty");
    YieldAndDiscountCurve[] objCurves = new YieldAndDiscountCurve[nCurves];
    Iterator<Pair<String, Integer>> it = order.iterator();

    DoubleArrayList result = new DoubleArrayList();

    for (int i = 0; i < nCurves; ++i) {
      objCurves[i] = curves.getCurve(it.next().getFirst());
      Double[] nodes = getNodes(objCurves[i]);
      for (final double element : nodes) {
        result.add(element);
      }
    }

    int totalNum = result.size();

    int[] tmp = toPositions(objNodes, result.toDoubleArray());
    int objNum = tmp.length;
    double[][] res = new double[objNum][totalNum];
    for (int i = 0; i < objNum; ++i) {
      Arrays.fill(res[i], 0.0);
      res[i][tmp[i]] = 1.0;
    }

    return new DoubleMatrix2D(res);
  }

  /**
   * Matrix reducing the nodes, to which portfolio has sensitivities, to new nodes (objNodes), for which sensitivity is accounted for, when relevant curves are already known
   * @param curves The relevant curves
   * @param objNodes The objective nodes on which sensitivity is to be accounted for
   * @return The matrix
   */
  public DoubleMatrix2D reduceCurveNodes(final YieldAndDiscountCurve[] curves, final double[] objNodes) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(objNodes, "objNodes");

    int nCurves = curves.length;
    DoubleArrayList result = new DoubleArrayList();
    for (int i = 0; i < nCurves; ++i) {
      Double[] nodes = getNodes(curves[i]);
      for (final double element : nodes) {
        result.add(element);
      }
    }

    int totalNum = result.size();

    int[] tmp = toPositions(objNodes, result.toDoubleArray());
    int objNum = tmp.length;
    double[][] res = new double[objNum][totalNum];
    for (int i = 0; i < objNum; ++i) {
      Arrays.fill(res[i], 0.0);
      res[i][tmp[i]] = 1.0;
    }

    return new DoubleMatrix2D(res);
  }

  /**
   * Matrix reducing the nodes, to which portfolio has sensitivities, to new nodes (objNodes), for which sensitivity is accounted for, when total nodes are already known
   * @param totalNodes The original total nodes on which sensitivity is accounted for
   * @param objNodes The objective nodes on which sensitivity is to be accounted for
   * @return The matrix
   */
  public DoubleMatrix2D reduceCurveNodes(final double[] totalNodes, final double[] objNodes) {
    ArgumentChecker.notNull(totalNodes, "totalNodes");
    ArgumentChecker.notNull(objNodes, "objNodes");

    int totalNum = totalNodes.length;
    int[] tmp = toPositions(objNodes, totalNodes);
    int objNum = tmp.length;
    double[][] res = new double[objNum][totalNum];
    for (int i = 0; i < objNum; ++i) {
      Arrays.fill(res[i], 0.0);
      res[i][tmp[i]] = 1.0;
    }

    return new DoubleMatrix2D(res);
  }

  private DoubleMatrix2D projectSingleCurveNodes(final MulticurveProviderDiscount curves, final Pair<String, Integer> curveName, final double[] objNodes) {
    YieldAndDiscountCurve objCurves = curves.getCurve(curveName.getFirst());
    Double[] nodes = getNodes(objCurves);
    int nNodes = nodes.length;

    int[] tmp = toPositions(objNodes, nodes);
    Arrays.sort(tmp);
    int objNum = tmp.length;
    double[][] res = new double[objNum][nNodes];

    if (objNum == 1) {
      Arrays.fill(res[0], 1.0);
      return new DoubleMatrix2D(res);
    }

    for (int i = 1; i < objNum - 1; ++i) {
      Arrays.fill(res[i], 0.0);
      int start = tmp[i - 1];
      for (int j = start; j < tmp[i]; ++j) {
        int position = j + 1;
        res[i][position] = 1.0;
      }
    }

    Arrays.fill(res[0], 0.0);
    for (int j = 0; j < tmp[0] + 1; ++j) {
      res[0][j] = 1.0;
    }

    Arrays.fill(res[objNum - 1], 0.0);
    int start = tmp[objNum - 2] + 1;
    for (int j = start; j < nNodes; ++j) {
      res[objNum - 1][j] = 1.0;
    }

    return new DoubleMatrix2D(res);
  }

  private int[] toPositions(final double[] objNodes, final Double[] totalNodes) {
    int nObjNodes = objNodes.length;
    int nTotalNodes = totalNodes.length;

    IntArrayList result = new IntArrayList();
    for (int i = 0; i < nObjNodes; ++i) {
      boolean findNodes = false;
      for (int j = 0; j < nTotalNodes; ++j) {
        if (Math.abs(objNodes[i] - totalNodes[j]) < TOL) {
          result.add(j);
          findNodes = true;
        }
      }
      if (!findNodes) {
        s_logger.info(i + "-th objective node with value " + objNodes[i] + " is not found in curve nodes");
      }
    }

    int nObjInt = result.size();
    ArgumentChecker.isTrue(nObjInt > 0, "None of the objective nodes are found in curve nodes");
    int[] res = new int[nObjInt];
    for (int i = 0; i < nObjInt; ++i) {
      res[i] = result.get(i);
    }

    return res;
  }

  private int[] toPositions(final double[] objNodes, final double[] totalNodes) {
    int nObjNodes = objNodes.length;
    int nTotalNodes = totalNodes.length;

    IntArrayList result = new IntArrayList();
    for (int i = 0; i < nObjNodes; ++i) {
      boolean findNodes = false;
      for (int j = 0; j < nTotalNodes; ++j) {
        if (Math.abs(objNodes[i] - totalNodes[j]) < TOL) {
          result.add(j);
          findNodes = true;
        }
      }
      if (!findNodes) {
        s_logger.info(i + "-th objective node with value " + objNodes[i] + " is not found in curve nodes");
      }
    }

    int nObjInt = result.size();
    ArgumentChecker.isTrue(nObjInt > 0, "None of the objective nodes are found in curve nodes");
    int[] res = new int[nObjInt];
    for (int i = 0; i < nObjInt; ++i) {
      res[i] = result.get(i);
    }

    return res;
  }

  private Double[] getNodes(final YieldAndDiscountCurve curve) {
    if (curve instanceof YieldCurve) {
      return ((YieldCurve) curve).getCurve().getXData();
    }
    if (curve instanceof DiscountCurve) {
      return ((DiscountCurve) curve).getCurve().getXData();
    }
    if (curve instanceof YieldAndDiscountAddZeroFixedCurve) {
      return getNodes(((YieldAndDiscountAddZeroFixedCurve) curve).getCurve());
    }
    if (curve instanceof YieldPeriodicCurve) {
      return ((YieldPeriodicCurve) curve).getCurve().getXData();
    }
    if (curve instanceof YieldAndDiscountAddZeroSpreadCurve) {
      YieldAndDiscountAddZeroSpreadCurve castCurve = (YieldAndDiscountAddZeroSpreadCurve) curve;
      YieldAndDiscountCurve[] curves = castCurve.getCurves();
      DoubleArrayList result = new DoubleArrayList();
      int nCurves = curves.length;
      for (int i = 0; i < nCurves; ++i) {
        Double[] nodes = getNodes(curves[i]);
        int nNodes = nodes.length;
        for (int j = 0; j < nNodes; ++j) {
          result.add(nodes[j]);
        }
      }
    }
    throw new IllegalArgumentException("node points can not be extracted");
  }
}
