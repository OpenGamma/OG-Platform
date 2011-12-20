// @export "imports"
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;

// @export "util-imports"
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

// @export "class-def"
public class SabrExtrapolationExample {
    // @export "class-constants-sabr-data"
    static final double ALPHA = 0.05;
    static final double BETA = 0.50;
    static final double RHO = -0.25;
    static final double NU = 0.50;
    static final SABRFormulaData SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);

    // @export "class-constants-sabr-extrapolation-function"
    static final double FORWARD = 0.05;
    static final double CUT_OFF_STRIKE = 0.10; // Set low for the test
    static final double RANGE_STRIKE = 0.02;
    static final double N_PTS = 100;
    static final double TIME_TO_EXPIRY = 2.0;
    static final double[] MU_VALUES = {5.0, 40.0, 90.0, 150.0};

    // @export "generateSabrData"
    public static void generateSabrData(PrintStream out) throws IOException {
        double mu;
        double strike;
        double price;
        double impliedVolatilityPct;
        SABRExtrapolationRightFunction sabrExtra;

        BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
        BlackFunctionData blackData = new BlackFunctionData(FORWARD, 1.0, 0.0);

        // @export "data-file"
        File data_file = new File("../dexy--smile-multi-mu-data.txt");
        PrintStream data_stream = new PrintStream(data_file);
        data_stream.println("Mu\tPrice\tStrike\tImpliedVolPct");

        // @export "loop"
        for (int i = 0; i < MU_VALUES.length; i++) {
            mu = MU_VALUES[i];
            sabrExtra = new SABRExtrapolationRightFunction(FORWARD, SABR_DATA, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu);

            for (int p = 0; p <= N_PTS; p++) {
                strike = CUT_OFF_STRIKE - RANGE_STRIKE + p * 4.0 * RANGE_STRIKE / N_PTS;
                EuropeanVanillaOption option = new EuropeanVanillaOption(strike, TIME_TO_EXPIRY, true);
                price = sabrExtra.price(option);
                impliedVolatilityPct = implied.getImpliedVolatility(blackData, option, price) * 100;
                data_stream.format("%4.0f\t%1.10f\t%1.10f\t%1.10f%n", mu, price, strike, impliedVolatilityPct);
            }
        }
        // @export "save-data"
        data_stream.close();
    }

    // @export "main"
    public static void main(String[] args) throws Exception {
        String[] ogargs = {
            "SabrExtrapolationExample",
            "../dexy--sabr-extrapolation-output.json",
            "../dexy--sabr-extrapolation-fields.json"
        };
        OpenGammaExampleClass.main(ogargs);
    }
    // @end
}
