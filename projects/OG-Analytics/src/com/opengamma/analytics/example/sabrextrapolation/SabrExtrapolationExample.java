package com.opengamma.analytics.example.sabrextrapolation;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Class;
import java.lang.IllegalAccessException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SabrExtrapolationExample {
    public static final double ALPHA = 0.05;
    public static final double BETA = 0.50;
    public static final double RHO = -0.25;
    public static final double NU = 0.50;
    public static final SABRFormulaData SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);
    public static final double FORWARD = 0.05;
    public static final double CUT_OFF_STRIKE = 0.10; // Set low for the test
    public static final double RANGE_STRIKE = 0.02;
    public static final double N_PTS = 100;
    public static final double TIME_TO_EXPIRY = 2.0;
    public static final double[] MU_VALUES = {5.0, 40.0, 90.0, 150.0};

    public static void generateSabrData(PrintStream out) throws IOException {
        double mu;
        double strike;
        double price;
        double impliedVolatilityPct;
        SABRExtrapolationRightFunction sabrExtra;

        BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
        BlackFunctionData blackData = new BlackFunctionData(FORWARD, 1.0, 0.0);

        out.println("Mu\tPrice\tStrike\tImpliedVolPct");

        for (int i = 0; i < MU_VALUES.length; i++) {
            mu = MU_VALUES[i];
            sabrExtra = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu);

            for (int p = 0; p <= N_PTS; p++) {
                strike = CUT_OFF_STRIKE - RANGE_STRIKE + p * 4.0 * RANGE_STRIKE / N_PTS;
                EuropeanVanillaOption option = new EuropeanVanillaOption(strike, TIME_TO_EXPIRY, true);
                price = sabrExtra.price(option);
                impliedVolatilityPct = implied.getImpliedVolatility(blackData, option, price) * 100;
                out.format("%4.0f\t%1.10f\t%1.10f\t%1.10f%n", mu, price, strike, impliedVolatilityPct);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        generateSabrData(System.out);
    }
}
