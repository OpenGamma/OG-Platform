/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AmericanVanillaOptionFunctionProviderTest {
  private static final BinomialTreeOptionPricingModel _model = new BinomialTreeOptionPricingModel();
  private static final TrinomialTreeOptionPricingModel _modelTrinomial = new TrinomialTreeOptionPricingModel();
  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {81., 97., 105., 105.1, 114., 138. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0.001, 0.005, 0.01 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.02 };

  /**
   * 
   */
  @Test
  public void smoothingTest() {
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int nDivs = DIVIDENDS.length;

    final LatticeSpecification lattice = new CoxRossRubinsteinLatticeSpecification();

    for (int j = 0; j < nInterests; ++j) {
      for (int k = 0; k < nStrikes; ++k) {
        for (int l = 0; l < nVols; ++l) {
          for (int m = 0; m < nDivs; ++m) {
            for (int i = 0; i < 15; ++i) {
              final int steps = 50 + 16 * i;
              final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false);
              final OptionFunctionProvider1D functionS = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              final OptionFunctionProvider1D functionCall = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, true);
              final OptionFunctionProvider1D functionSCall = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, true, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              final double priceP = _model.getPrice(lattice, function, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              final double pricePS = _model.getPrice(lattice, functionS, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              assertEquals(priceP, pricePS, 2. * Math.max(0.1, pricePS) / steps);
              final double priceCall = _model.getPrice(lattice, functionCall, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              final double priceSCall = _model.getPrice(lattice, functionSCall, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              assertEquals(priceCall, priceSCall, 2. * Math.max(0.1, priceCall) / steps);

              /*
               * Both of acceralation and truncation applied
               */
              final OptionFunctionProvider1D functionTrAc = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false, VOLS[l], INTERESTS[j], DIVIDENDS[m], 10., true);
              final OptionFunctionProvider1D functionTrAcCall = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, true, VOLS[l], INTERESTS[j], DIVIDENDS[m], 10., true);
              final double pricePTrAc = _model.getPrice(lattice, functionTrAc, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              assertEquals(pricePS, pricePTrAc, Math.max(.1, pricePS) / Math.sqrt(steps));
              final double priceTrAcCall = _model.getPrice(lattice, functionTrAcCall, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              assertEquals(priceSCall, priceTrAcCall, Math.max(.1, priceSCall) / Math.sqrt(steps));
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void truncationTest() {
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int nDivs = DIVIDENDS.length;

    final LatticeSpecification lattice = new TianLatticeSpecification();

    for (int j = 0; j < nInterests; ++j) {
      for (int k = 0; k < nStrikes; ++k) {
        for (int l = 0; l < nVols; ++l) {
          for (int m = 0; m < nDivs; ++m) {
            for (int i = 0; i < 15; ++i) {
              final int steps = 50 + 20 * i;
              final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false);
              final OptionFunctionProvider1D functionTr = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false, VOLS[l], INTERESTS[j], DIVIDENDS[m], 4., false);
              final OptionFunctionProvider1D functionCall = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, true);
              final OptionFunctionProvider1D functionTrCall = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, true, VOLS[l], INTERESTS[j], DIVIDENDS[m], 4.);
              final double priceP = _model.getPrice(lattice, function, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              final double pricePTr = _model.getPrice(lattice, functionTr, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              assertEquals(pricePTr, priceP, Math.max(0.1, priceP) / steps);
              final double priceCall = _model.getPrice(lattice, functionCall, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              final double priceTrCall = _model.getPrice(lattice, functionTrCall, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[m]);
              assertEquals(priceTrCall, priceCall, Math.max(0.1, priceCall) / steps);
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void putCallSymmetryTest() {
    /*
     * Two sample lattices are checked 
     */
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 401;
    for (final double strike : STRIKES) {
      final OptionFunctionProvider1D functionCall = new AmericanVanillaOptionFunctionProvider(strike, TIME, steps, true);
      final OptionFunctionProvider1D functionPut = new AmericanVanillaOptionFunctionProvider(SPOT * SPOT / strike, TIME, steps, false);
      for (final double interest : INTERESTS) {
        for (final double vol : VOLS) {
          for (final double dividend : DIVIDENDS) {
            for (final LatticeSpecification lattice : lattices) {
              final GreekResultCollection greekCall = _model.getGreeks(lattice, functionCall, SPOT, vol, interest, dividend);
              final GreekResultCollection greekPut = _model.getGreeks(lattice, functionPut, SPOT, vol, dividend, interest);
              final double priceCall = greekCall.get(Greek.FAIR_PRICE);
              final double thetaCall = greekCall.get(Greek.THETA);
              final double pricePut = greekPut.get(Greek.FAIR_PRICE);
              final double thetaPut = greekPut.get(Greek.THETA);
              final double refPrice = strike * pricePut / SPOT;
              final double refTheta = strike * thetaPut / SPOT;
              assertEquals(priceCall, refPrice, Math.max(refPrice, 1.) * 1.e-5);
              assertEquals(thetaCall, refTheta, Math.max(Math.abs(refTheta), 1.) * 1.e-2);
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void putPriceTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    final int nLattices = lattices.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int steps = 101;

    /*
     * Reference values computed by Leisen & Reimer with n=1517
     */
    final double[][][] expected = new double[][][] { { {0.058556821282147, 1.423987043411806, 27.594175137559205 }, {2.567397920364287, 6.631469777788842, 38.470266225289116 }, {
        6.997116228834605, 11.187942858140278, 44.283278841420419 }, {7.067960593857507, 11.252770980174546, 44.357339168435061 }, {14.515506902403871, 17.702451797170493, 51.075917865430618 }, {
        38.923092465651131, 39.592796431785167, 70.271437840386440 } }, { {0.015039762225174, 0.881641734429296, 25.289896569285318 }, {1.214752608311750, 4.706363895479459, 35.418274680478810 }, {
        4.094588917958109, 8.361058070122976, 40.848245125395458 }, {4.145461254087033, 8.414427418702433, 40.917488765417581 }, {10.064152039859630, 13.869714139477221, 47.205092138702085 }, {
        32.999999999999986, 33.715871341375923, 65.223659867497304 } }, { {0.008949793225201, 0.742484278065933, 24.596867328889161 }, {0.926795362260926, 4.195273759411491, 34.520923345086018 }, {
        3.452142183825225, 7.628512315996570, 39.850075948866980 }, {3.499089828086897, 7.679299041128621, 39.918068529919609 }, {9.351414622160069, 12.948354591146975, 46.095399019293197 }, {
        33.000000000000014, 33.065144904304397, 63.829711734824144 } }, { {0.004575370004527, 0.598492302803965, 23.787299679796622 }, {0.659819531124806, 3.648809197956719, 33.479804561431592 }, {
        2.841041595429597, 6.853189962076901, 38.696363157646154 }, {2.884758217377101, 6.901476841705181, 38.762965967889727 }, {9.007929443878707, 12.024232122541374, 44.818592298574579 }, {
        32.999999999999915, 32.999999999999872, 62.247311829954604 } } };

    for (int m = 0; m < nLattices; ++m) {
      final LatticeSpecification lattice = lattices[m];
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false);
            final double priceP = _model.getPrice(lattice, function, SPOT, VOLS[l], INTERESTS[j], 0.);
            assertEquals(priceP, expected[j][k][l], Math.max(expected[j][k][l], 1.) / Math.sqrt(steps));
            if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                lattice instanceof TianLatticeSpecification) {
              final double priceTrinomial = _modelTrinomial.getPrice(lattice, function, SPOT, VOLS[l], INTERESTS[j], 0.);
              assertEquals(priceTrinomial, expected[j][k][l], Math.max(expected[j][k][l], 1.) * 1.e-2);
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void putGreeksTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification() };
    final int nLattices = lattices.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int steps = 117;

    /*
     * Reference values computed by Leisen & Reimer with n=1517
     */
    final double[][][] exDelta = new double[][][] { { {-0.014881626500638, -0.122320306886183, -0.234365060695953 }, {
        -0.339205802251104, -0.388140843643441, -0.291610061032319 }, {
        -0.640018487753769, -0.540781192942660, -0.318687114262474 }, {
        -0.643486955912236, -0.542623510155043, -0.319018720523979 }, {
        -0.877139461972654, -0.692711380546793, -0.347845580051385 }, {
        -0.998755591884291, -0.924427280526098, -0.418882111538830 } }, { {-0.004359663851066, -0.082635496280172, -0.221039092578087 }, {
        -0.194675346985548, -0.306306489596917, -0.276738870762684 }, {
        -0.467861792085781, -0.453143653469079, -0.303244197015975 }, {
        -0.471614945555351, -0.454997113579120, -0.303569414952370 }, {
        -0.773762547122799, -0.613193112736144, -0.331896785778517 }, {
        -1.000000000000000, -0.897032874544827, -0.402166458946481 } }, { {-0.002718398851028, -0.071926275952036, -0.217768812175972 }, {
        -0.160046104951321, -0.285073304969120, -0.273573506780415 }, {
        -0.434578188835797, -0.434274902201187, -0.300242522038988 }, {
        -0.438716786916757, -0.436205122256318, -0.300570217125642 }, {
        -0.821505741180240, -0.605995994831714, -0.329157980446281 }, {
        -1.000000000000000, -0.957415459022728, -0.400479269245190 } }, { {-0.001471871574208, -0.060447364696806, -0.214193661718878 }, {
        -0.125406038139822, -0.262290629158548, -0.270341499298573 }, {
        -0.407792576384516, -0.416612620804891, -0.297335864919290 }, {
        -0.412630608150302, -0.418679612797512, -0.297668226568210 }, {
        -0.962854119062361, -0.608703017663675, -0.326727321400418 }, {
        -1.000000000000000, -1.000000000000000, -0.399814406410798 } } };
    final double[][][] exGamma = new double[][][] { { {0.003503805481590, 0.009432955804714, 0.002853154917297 }, {
        0.034041463367876, 0.017813793247544, 0.003190059240294 }, {
        0.034783499077940, 0.018448167151535, 0.003317888080333 }, {
        0.034666416887891, 0.018439178355934, 0.003319333747143 }, {
        0.018914950520415, 0.016336220066209, 0.003434198128643 }, {
        0.000383497870915, 0.006620379246081, 0.003629148565422 } }, { {0.001193629984056, 0.007097376469900, 0.002764379147412 }, {
        0.025740817970497, 0.016380009581449, 0.003116904386891 }, {
        0.037556354385415, 0.018550045767377, 0.003253974256007 }, {
        0.037592591302524, 0.018561089156062, 0.003255539350256 }, {
        0.030084620771250, 0.018058047361325, 0.003381305326465 }, {
        0, 0.009360073399808, 0.003606760222134 } }, { {0.000784673757861, 0.006435086766269, 0.002761737676874 }, {
        0.023498065265425, 0.016249673656883, 0.003131819743348 }, {
        0.041360397172803, 0.019312837399267, 0.003278742898400 }, {
        0.041511333183470, 0.019336680526852, 0.003280434748468 }, {
        0.045383252199437, 0.020027001354998, 0.003417784472141 }, {
        0, 0.014028030261333, 0.003676695651071 } }, { {0.000453167081619, 0.005693670613991, 0.002767712806744 }, {
        0.020996909681787, 0.016243112150641, 0.003163841577838 }, {
        0.048265326559198, 0.020672573685421, 0.003325401936554 }, {
        0.048629035127462, 0.020717086287208, 0.003327282324859 }, {
        0.080702324548011, 0.023488350349200, 0.003481891522142 }, {
        0, 0, 0.003791052234156 } } };
    final double[][][] exTheta = new double[][][] { { {-0.064498095331651, -0.662667881399460, -4.454029185506673 }, {
        -0.850973988605841, -1.455847936374344, -5.087193616867534 }, {
        -1.221350671097671, -1.696654895399556, -5.349918769099305 }, {
        -1.224087467382668, -1.698742102335414, -5.352999868515796 }, {
        -1.326823165954796, -1.804905598695592, -5.608751333744251 }, {
        -1.443209376168559, -1.731525014810447, -6.143960962242465 } }, { {-0.015976908751189, -0.381684509059388, -3.761161011237013 }, {
        -0.333084983614123, -0.866079482874228, -4.231008002073541 }, {
        -0.464353431787041, -0.966630131242269, -4.411694385747229 }, {
        -0.464407868350514, -0.966990915383428, -4.413747889735914 }, {
        -0.323294460516038, -0.917194869816280, -4.577807148389552 }, {
        0.138000000000000, -0.388069722995840, -4.863115293071950 } }, { {-0.009341876862604, -0.313260441725454, -3.568706772905693 }, {
        -0.235174530028392, -0.725123406429818, -3.999808376016376 }, {
        -0.324583713479766, -0.798483276398991, -4.161639853043242 }, {
        -0.324256298162962, -0.798530329652530, -4.163459431091307 }, {
        -0.147390357143069, -0.721098779451898, -4.306849290838867 }, {
        0.690000000000000, -0.105326327647521, -4.537546019103879 } }, { {-0.004653989990591, -0.244408936636584, -3.351477870191915 }, {
        -0.151089126194059, -0.583508304708060, -3.741512554579546 }, {
        -0.208563910485908, -0.633600472942907, -3.883653254072278 }, {
        -0.208059169618752, -0.633401019727906, -3.885229656370252 }, {
        -0.011102790723010, -0.535414823227399, -4.007232143496033 }, {
        1.379999999999999, 1.379999999999999, -4.182265615165154 } } };

    for (int m = 0; m < nLattices; ++m) {
      final LatticeSpecification lattice = lattices[m];
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES[k], TIME, steps, false);
            final GreekResultCollection greeks = _model.getGreeks(lattice, function, SPOT, VOLS[l], INTERESTS[j], 0.);
            assertEquals(greeks.get(Greek.DELTA), exDelta[j][k][l], Math.max(Math.abs(exDelta[j][k][l]), 1.) / Math.sqrt(steps));
            assertEquals(greeks.get(Greek.GAMMA), exGamma[j][k][l], Math.max(Math.abs(exGamma[j][k][l]), 1.) / Math.sqrt(steps));
            /*
             * Because theta is poorly approximated in binomial models, the output is not tested here
             * For the 3 cases below, the resulting value hugely depends on lattice specifications
             */
            if (exTheta[j][k][l] != -0.01110279072301 && exTheta[j][k][l] != 0.138 && exTheta[j][k][l] != 0.69 && exTheta[j][k][l] != 1.379999999999999) {
              assertEquals(greeks.get(Greek.THETA), exTheta[j][k][l], Math.max(Math.abs(exTheta[j][k][l]), 1.) / Math.sqrt(steps));
            }
            if (lattice instanceof CoxRossRubinsteinLatticeSpecification || lattice instanceof JarrowRuddLatticeSpecification || lattice instanceof TrigeorgisLatticeSpecification ||
                lattice instanceof TianLatticeSpecification) {
              final GreekResultCollection greeksTrinomial = _modelTrinomial.getGreeks(lattice, function, SPOT, VOLS[l], INTERESTS[j], 0.);
              assertEquals(greeksTrinomial.get(Greek.DELTA), exDelta[j][k][l], Math.max(Math.abs(exDelta[j][k][l]), 1.) / Math.sqrt(steps));
              assertEquals(greeksTrinomial.get(Greek.GAMMA), exGamma[j][k][l], Math.max(Math.abs(exGamma[j][k][l]), 1.) / Math.sqrt(steps));
              if (exTheta[j][k][l] != -0.01110279072301 && exTheta[j][k][l] != 0.138 && exTheta[j][k][l] != 0.69 && exTheta[j][k][l] != 1.379999999999999) {
                assertEquals(greeksTrinomial.get(Greek.THETA), exTheta[j][k][l], Math.max(Math.abs(exTheta[j][k][l]), 1.) / Math.sqrt(steps));
              }
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void greeksContDividendTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };
    final boolean[] tfSet = new boolean[] {true, false };

    final double time = 10.;

    final int nLattices = lattices.length;
    final int nStrikes = STRIKES.length;
    final int nInterests = INTERESTS.length;
    final int nVols = VOLS.length;
    final int nTf = 2;
    final int nDiv = DIVIDENDS.length;
    final int steps = 117;
    for (int m = 0; m < nLattices; ++m) {
      final LatticeSpecification lattice = lattices[m];
      for (int j = 0; j < nInterests; ++j) {
        for (int k = 0; k < nStrikes; ++k) {
          for (int l = 0; l < nVols; ++l) {
            for (int n = 0; n < nTf; ++n) {
              for (int i = 0; i < nDiv; ++i) {
                final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(STRIKES[k], time, steps, tfSet[n]);
                final GreekResultCollection resNew = _model.getGreeks(lattice, function, SPOT, VOLS[l], INTERESTS[j], DIVIDENDS[i]);
                final BjerksundStenslandModel bs = new BjerksundStenslandModel();
                final double[] first = bs.getPriceAdjoint(SPOT, STRIKES[k], INTERESTS[j], INTERESTS[j] - DIVIDENDS[i], time, VOLS[l], tfSet[n]);
                final double[] deltaGamma = bs.getPriceDeltaGamma(SPOT, STRIKES[k], INTERESTS[j], INTERESTS[j] - DIVIDENDS[i], time, VOLS[l], tfSet[n]);
                //                System.out.println(SPOT + "\t" + STRIKES[k] + "\t" + INTERESTS[j] + "\t" + DIVIDENDS[i] + "\t" + time + "\t" + VOLS[l] + "\t" + tfSet[n] + "\t" + m);
                assertEquals(resNew.get(Greek.FAIR_PRICE), deltaGamma[0], Math.abs(deltaGamma[0]));
                assertEquals(resNew.get(Greek.DELTA), deltaGamma[1], Math.abs(deltaGamma[1]));
                /*
                 * If the spot is close to the exercise boundary, c_{20} = c_{21} = c_{22} sometimes occurs in some lattice specification leading to vanishing gamma
                 * In this case, the gamma value is not accurate and we need to try other lattice specifications in order to to check if this is an artifact or not
                 */
                if (Math.abs(resNew.get(Greek.GAMMA)) > 1.e-5) {
                  assertEquals(resNew.get(Greek.GAMMA), deltaGamma[2], Math.max(Math.abs(deltaGamma[2]), 0.1));
                }
                /*
                 * Bjerksund-Stensland Model produces a negative "theta" by some reason
                 */
                if (first[5] > 0.) {
                  assertEquals(resNew.get(Greek.THETA), -first[5], Math.max(Math.abs(first[5]), 0.1));
                }

              }
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @Test
  public void timeVaryingVolTest() {
    final LatticeSpecification lattice1 = new TimeVaryingLatticeSpecification();
    final double[] time_set = new double[] {0.5, 1.2 };
    final int steps = 801;

    final double[] vol = new double[steps];
    final double[] rate = new double[steps];
    final double[] dividend = new double[steps];
    final int stepsTri = 761;
    final double[] volTri = new double[stepsTri];
    final double[] rateTri = new double[stepsTri];
    final double[] dividendTri = new double[stepsTri];
    final double constA = 0.01;
    final double constB = 0.001;
    final double constC = 0.1;
    final double constD = 0.05;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double time : time_set) {
          for (int i = 0; i < steps; ++i) {
            rate[i] = constA + constB * i * time / steps;
            vol[i] = constC + constD * Math.sin(i * time / steps);
            dividend[i] = 0.005;
          }
          for (int i = 0; i < stepsTri; ++i) {
            rateTri[i] = constA + constB * i * time / steps;
            volTri[i] = constC + constD * Math.sin(i * time / steps);
            dividendTri[i] = 0.005;
          }
          final double rateRef = constA + 0.5 * constB * time;
          final double volRef = Math.sqrt(constC * constC + 0.5 * constD * constD + 2. * constC * constD / time * (1. - Math.cos(time)) - constD * constD * 0.25 / time * Math.sin(2. * time));

          final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, time, steps, isCall);
          final double resPrice = _model.getPrice(function, SPOT, vol, rate, dividend);
          final GreekResultCollection resGreeks = _model.getGreeks(function, SPOT, vol, rate, dividend);

          final double resPriceConst = _model.getPrice(lattice1, function, SPOT, volRef, rateRef, dividend[0]);
          final GreekResultCollection resGreeksConst = _model.getGreeks(lattice1, function, SPOT, volRef, rateRef, dividend[0]);
          assertEquals(resPrice, resPriceConst, Math.abs(resPriceConst) * 1.e-1);
          assertEquals(resGreeks.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeks.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));

          final OptionFunctionProvider1D functionTri = new AmericanVanillaOptionFunctionProvider(strike, time, stepsTri, isCall);
          final double resPriceTrinomial = _modelTrinomial.getPrice(functionTri, SPOT, volTri, rateTri, dividendTri);
          assertEquals(resPriceTrinomial, resPriceConst, Math.max(Math.abs(resPriceConst), .1) * 1.e-1);
          final GreekResultCollection resGreeksTrinomial = _modelTrinomial.getGreeks(functionTri, SPOT, volTri, rateTri, dividendTri);
          assertEquals(resGreeksTrinomial.get(Greek.FAIR_PRICE), resGreeksConst.get(Greek.FAIR_PRICE), Math.max(Math.abs(resGreeksConst.get(Greek.FAIR_PRICE)), 0.1) * 0.1);
          assertEquals(resGreeksTrinomial.get(Greek.DELTA), resGreeksConst.get(Greek.DELTA), Math.max(Math.abs(resGreeksConst.get(Greek.DELTA)), 0.1) * 0.1);
          assertEquals(resGreeksTrinomial.get(Greek.GAMMA), resGreeksConst.get(Greek.GAMMA), Math.max(Math.abs(resGreeksConst.get(Greek.GAMMA)), 0.1) * 0.1);
          assertEquals(resGreeksTrinomial.get(Greek.THETA), resGreeksConst.get(Greek.THETA), Math.max(Math.abs(resGreeksConst.get(Greek.THETA)), 0.1));
        }
      }
    }
  }

  /**
   * Consistency is checked
   */
  @Test
  public void priceCashMultipleDividendsTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 101;
    final double[][] dividendTimesMat = new double[][] { {TIME * (steps - 4.) / (steps - 1), TIME * (steps - 3.) / (steps - 1), TIME * (steps - 2.) / (steps - 1) },
        {TIME / (steps - 2), TIME * 2. / (steps - 2), TIME * 3. / (steps - 2) } };
    final double[][] cashDividendsMat = new double[][] { {0.5, 0.5, 0.5 }, {0.1, 0.3, 0.2 } };
    final int divDim = cashDividendsMat.length;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification model : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            if (interest > 0.) {
              for (final double vol : VOLS) {
                for (int j = 0; j < divDim; ++j) {
                  final double[] dividendTimes = dividendTimesMat[j];
                  final double[] cashDividends = cashDividendsMat[j];
                  final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, TIME, steps, isCall);
                  final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
                  double priceDiv = _model.getPrice(model, function, SPOT, vol, interest, cashDividend);
                  double modSpot = SPOT;
                  final int divTimes = cashDividends.length;
                  for (int i = 0; i < divTimes; ++i) {
                    modSpot -= cashDividends[i] * Math.exp(-interest * dividendTimes[i]);
                  }
                  double priceMod = _model.getPrice(model, function, modSpot, vol, interest, 0.);
                  double price = _model.getPrice(model, function, SPOT, vol, interest, 0.);

                  final double ref = Math.abs(priceDiv - priceMod) > Math.abs(priceDiv - price) ? price : priceMod;
                  assertEquals(priceDiv, ref, Math.max(ref, 1.) * 1.e-1);
                  if (model instanceof LeisenReimerLatticeSpecification) {
                    final LatticeSpecification[] latticesTri = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
                        new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
                    final int stepsTri = 71;
                    for (final LatticeSpecification latticeTri : latticesTri) {
                      final OptionFunctionProvider1D functionTri = new AmericanVanillaOptionFunctionProvider(strike, TIME, stepsTri, isCall);
                      final double priceTrinomial = _modelTrinomial.getPrice(latticeTri, functionTri, SPOT, vol, interest, 0.);
                      assertEquals(priceTrinomial, price, Math.max(price, 1.) * 1.e-2);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Consistency is checked
   */
  @Test
  public void pricePropMultipleDividendsTest() {

    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
        new TrigeorgisLatticeSpecification(), new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final int steps = 101;
    final double[][] dividendTimesMat = new double[][] { {TIME * (steps - 4.) / (steps - 1), TIME * (steps - 3.) / (steps - 1), TIME * (steps - 2.) / (steps - 1) },
        {TIME / (steps - 2), TIME * 2. / (steps - 2), TIME * 3. / (steps - 2) } };
    final double[][] ProportionalDividendsMat = new double[][] { {.001, .001, .001 }, {.0015, .002, .001 } };
    final int divDim = ProportionalDividendsMat.length;

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification model : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            if (interest > 0.) {
              for (final double vol : VOLS) {
                for (int j = 0; j < divDim; ++j) {
                  final double[] dividendTimes = dividendTimesMat[j];
                  final double[] propDividends = ProportionalDividendsMat[j];
                  final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, TIME, steps, isCall);
                  final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);

                  double priceDiv = _model.getPrice(model, function, SPOT, vol, interest, propDividend);
                  double modSpot = SPOT;
                  final int divTimes = propDividends.length;
                  for (int i = 0; i < divTimes; ++i) {
                    modSpot -= propDividends[i] * Math.exp(-interest * dividendTimes[i]);
                  }
                  double priceMod = _model.getPrice(model, function, modSpot, vol, interest, 0.);
                  double price = _model.getPrice(model, function, SPOT, vol, interest, 0.);
                  final double ref = Math.abs(priceDiv - priceMod) > Math.abs(priceDiv - price) ? price : priceMod;
                  assertEquals(priceDiv, ref, Math.max(ref, 1.) * 1.e-1);

                  if (model instanceof LeisenReimerLatticeSpecification) {
                    final LatticeSpecification[] latticesTri = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
                        new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
                    final int stepsTri = 71;
                    for (final LatticeSpecification latticeTri : latticesTri) {
                      final OptionFunctionProvider1D functionTri = new AmericanVanillaOptionFunctionProvider(strike, TIME, stepsTri, isCall);
                      final double priceTrinomial = _modelTrinomial.getPrice(latticeTri, functionTri, SPOT, vol, interest, 0.);
                      assertEquals(priceTrinomial, price, Math.max(price, 1.) * 1.e-2);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Consistency is checked
   */
  @Test
  public void greeksDiscreteDividendLatticeTest() {
    final LatticeSpecification[] lattices = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(), new TrigeorgisLatticeSpecification(),
        new JabbourKraminYoungLatticeSpecification(), new TianLatticeSpecification(), new LeisenReimerLatticeSpecification() };

    final double[] propDividends = new double[] {0.0015, 0.002, 0.001 };
    final double[] cashDividends = new double[] {.1, .3, .2 };
    final double[] dividendTimes = new double[] {TIME / 6., TIME / 3., TIME / 2. };

    final boolean[] tfSet = new boolean[] {true, false };
    for (final LatticeSpecification lattice : lattices) {
      for (final boolean isCall : tfSet) {
        for (final double strike : STRIKES) {
          for (final double interest : INTERESTS) {
            for (final double vol : VOLS) {
              final int nSteps = 21;
              final double resSpot = SPOT * (1. - propDividends[0]) * (1. - propDividends[1]) * (1. - propDividends[2]);
              final double modSpot = SPOT - cashDividends[0] * Math.exp(-interest * dividendTimes[0]) - cashDividends[1] * Math.exp(-interest * dividendTimes[1]) - cashDividends[2] *
                  Math.exp(-interest * dividendTimes[2]);
              final OptionFunctionProvider1D function = new AmericanVanillaOptionFunctionProvider(strike, TIME, nSteps, isCall);

              final GreekResultCollection resRes = _model.getGreeks(lattice, function, resSpot, vol, interest, 0.);
              final GreekResultCollection resMod = _model.getGreeks(lattice, function, modSpot, vol, interest, 0.);
              final GreekResultCollection resBare = _model.getGreeks(lattice, function, SPOT, vol, interest, 0.);

              final DividendFunctionProvider cashDividend = new CashDividendFunctionProvider(dividendTimes, cashDividends);
              final DividendFunctionProvider propDividend = new ProportionalDividendFunctionProvider(dividendTimes, propDividends);
              final GreekResultCollection resProp = _model.getGreeks(lattice, function, SPOT, vol, interest, propDividend);
              final GreekResultCollection resCash = _model.getGreeks(lattice, function, SPOT, vol, interest, cashDividend);

              final double refPriceProp = Math.abs(resProp.get(Greek.FAIR_PRICE) - resRes.get(Greek.FAIR_PRICE)) > Math.abs(resProp.get(Greek.FAIR_PRICE) - resBare.get(Greek.FAIR_PRICE)) ? resBare
                  .get(Greek.FAIR_PRICE) : resRes.get(Greek.FAIR_PRICE);
              final double refDeltaProp = Math.abs(resProp.get(Greek.DELTA) - resRes.get(Greek.DELTA)) > Math.abs(resProp.get(Greek.DELTA) - resBare.get(Greek.DELTA)) ? resBare
                  .get(Greek.DELTA) : resRes.get(Greek.DELTA);
              final double refGammaProp = Math.abs(resProp.get(Greek.GAMMA) - resRes.get(Greek.GAMMA)) > Math.abs(resProp.get(Greek.GAMMA) - resBare.get(Greek.GAMMA)) ? resBare
                  .get(Greek.GAMMA) : resRes.get(Greek.GAMMA);
              final double refThetaProp = Math.abs(resProp.get(Greek.THETA) - resRes.get(Greek.THETA)) > Math.abs(resProp.get(Greek.THETA) - resBare.get(Greek.THETA)) ? resBare
                  .get(Greek.THETA) : resRes.get(Greek.THETA);
              assertEquals(resProp.get(Greek.FAIR_PRICE), refPriceProp, Math.max(1., Math.abs(refPriceProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.DELTA), refDeltaProp, Math.max(1., Math.abs(refDeltaProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.GAMMA), refGammaProp, Math.max(1., Math.abs(refGammaProp)) * 1.e-1);
              assertEquals(resProp.get(Greek.THETA), refThetaProp, Math.max(1., Math.abs(refThetaProp)));

              final double refPriceCash = Math.abs(resCash.get(Greek.FAIR_PRICE) - resMod.get(Greek.FAIR_PRICE)) > Math.abs(resCash.get(Greek.FAIR_PRICE) - resBare.get(Greek.FAIR_PRICE)) ? resBare
                  .get(Greek.FAIR_PRICE) : resRes.get(Greek.FAIR_PRICE);
              final double refDeltaCash = Math.abs(resCash.get(Greek.DELTA) - resMod.get(Greek.DELTA)) > Math.abs(resCash.get(Greek.DELTA) - resBare.get(Greek.DELTA)) ? resBare
                  .get(Greek.DELTA) : resRes.get(Greek.DELTA);
              final double refGammaCash = Math.abs(resCash.get(Greek.GAMMA) - resMod.get(Greek.GAMMA)) > Math.abs(resCash.get(Greek.GAMMA) - resBare.get(Greek.GAMMA)) ? resBare
                  .get(Greek.GAMMA) : resRes.get(Greek.GAMMA);
              final double refThetaCash = Math.abs(resCash.get(Greek.THETA) - resMod.get(Greek.THETA)) > Math.abs(resCash.get(Greek.THETA) - resBare.get(Greek.THETA)) ? resBare
                  .get(Greek.THETA) : resRes.get(Greek.THETA);
              assertEquals(resCash.get(Greek.FAIR_PRICE), refPriceCash, Math.max(1., Math.abs(refPriceCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.DELTA), refDeltaCash, Math.max(1., Math.abs(refDeltaCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.GAMMA), refGammaCash, Math.max(1., Math.abs(refGammaCash)) * 1.e-1);
              assertEquals(resCash.get(Greek.THETA), refThetaCash, Math.max(1., Math.abs(refThetaCash)));

              if (lattice instanceof TrigeorgisLatticeSpecification) {
                final LatticeSpecification[] latticesTri = new LatticeSpecification[] {new CoxRossRubinsteinLatticeSpecification(), new JarrowRuddLatticeSpecification(),
                    new TrigeorgisLatticeSpecification(), new TianLatticeSpecification() };
                final int stepsTri = 11;
                for (final LatticeSpecification latticeTri : latticesTri) {
                  final OptionFunctionProvider1D functionTri = new AmericanVanillaOptionFunctionProvider(strike, TIME, stepsTri, isCall);
                  final GreekResultCollection resPropTrinomial = _modelTrinomial.getGreeks(latticeTri, functionTri, SPOT, vol, interest, propDividend);
                  final GreekResultCollection resCashTrinomial = _modelTrinomial.getGreeks(latticeTri, functionTri, SPOT, vol, interest, cashDividend);

                  assertEquals(resPropTrinomial.get(Greek.FAIR_PRICE), resProp.get(Greek.FAIR_PRICE), Math.max(1., Math.abs(resProp.get(Greek.FAIR_PRICE))) * 1.e-1);
                  assertEquals(resPropTrinomial.get(Greek.DELTA), resProp.get(Greek.DELTA), Math.max(1., Math.abs(resProp.get(Greek.DELTA))) * 1.e-1);
                  assertEquals(resPropTrinomial.get(Greek.GAMMA), resProp.get(Greek.GAMMA), Math.max(1., Math.abs(resProp.get(Greek.GAMMA))) * 1.e-1);
                  if (resProp.get(Greek.THETA) > 0.) {
                    assertEquals(resPropTrinomial.get(Greek.THETA), resProp.get(Greek.THETA), Math.max(1., Math.abs(resProp.get(Greek.THETA))));
                  }
                  assertEquals(resCashTrinomial.get(Greek.FAIR_PRICE), resCash.get(Greek.FAIR_PRICE), Math.max(1., Math.abs(resCash.get(Greek.FAIR_PRICE))) * 1.e-1);
                  assertEquals(resCashTrinomial.get(Greek.DELTA), resCash.get(Greek.DELTA), Math.max(1., Math.abs(resCash.get(Greek.DELTA))) * 1.e-1);
                  assertEquals(resCashTrinomial.get(Greek.GAMMA), resCash.get(Greek.GAMMA), Math.max(1., Math.abs(resCash.get(Greek.GAMMA))) * 1.e-1);
                  if (resCash.get(Greek.THETA) > 0.) {
                    assertEquals(resCashTrinomial.get(Greek.THETA), resCash.get(Greek.THETA), Math.max(1., Math.abs(resCash.get(Greek.THETA))));
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void errorVolTest1() {
    new AmericanVanillaOptionFunctionProvider(STRIKES[1], TIME, 10, false, -VOLS[1], INTERESTS[1], DIVIDENDS[1]);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void errorVolTest2() {
    new AmericanVanillaOptionFunctionProvider(STRIKES[1], TIME, 10, false, -VOLS[1], INTERESTS[1], DIVIDENDS[1], 4., true);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void errorVolTest3() {
    new AmericanVanillaOptionFunctionProvider(STRIKES[1], TIME, 10, false, -VOLS[1], INTERESTS[1], DIVIDENDS[1], 4., false);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void errorDiv1Test() {
    new AmericanVanillaOptionFunctionProvider(STRIKES[1], TIME, 10, false, VOLS[1], INTERESTS[1], DIVIDENDS[1], -4., true);
  }

  /**
   * 
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void errorDiv2Test() {
    new AmericanVanillaOptionFunctionProvider(STRIKES[1], TIME, 10, false, VOLS[1], INTERESTS[1], DIVIDENDS[1], -4., false);
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    final OptionFunctionProvider1D ref = new AmericanVanillaOptionFunctionProvider(100., 1., 53, true);
    final OptionFunctionProvider1D[] function = new OptionFunctionProvider1D[] {ref, new AmericanVanillaOptionFunctionProvider(100., 1., 53, true),
        new EuropeanVanillaOptionFunctionProvider(100., 1., 53, true), null };
    final int len = function.length;
    for (int i = 0; i < len; ++i) {
      if (ref.equals(function[i])) {
        assertTrue(ref.hashCode() == function[i].hashCode());
      }
    }
    for (int i = 0; i < len - 1; ++i) {
      assertTrue(function[i].equals(ref) == ref.equals(function[i]));
    }
    assertFalse(ref.equals(new EuropeanSpreadOptionFunctionProvider(100., 1., 53, true)));
  }
}
