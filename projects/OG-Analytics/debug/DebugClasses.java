// Imports for classes being debugged
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;

// Imports needed by script
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class DebugClasses {
    public static void main(String[] args) {

        // Feel free to add/remove classes from this array as needed.
        // Either import each class at top of file or use full qualified name of class.
        Class[] classes = {
            BlackFunctionData.class,
            BlackPriceFunction.class,
            SABRFormulaData.class,
            SABRHaganVolatilityFunction.class
        };

        for (int i = 0; i < classes.length; i++) {
            Class klass = classes[i];
            System.out.println(klass);

            // The JAR/.class file being used to provide class definition
            java.net.URL url = DebugClasses.class.getResource("/" + klass.getName().replace(".", "/") + ".class");
            System.out.println("  " + url.toString().replace("!", "!\n    "));

            // Details of constructors that are defined
            Constructor[] constructors = klass.getConstructors();
            System.out.format("  Found %d constructors%n", constructors.length);
            for (int j = 0; j < constructors.length; j++) {
                Constructor constructor = constructors[j];
                Class[] parameters = constructor.getParameterTypes();
                System.out.format("    Constructor %d of %d - takes %d parameters%n", j+1, constructors.length, parameters.length);
                for (int k = 0; k < parameters.length; k++) {
                    Class param = parameters[k];
                    System.out.format("      Param %d: %s", k+1, param.toString());
                    Class component_type = param.getComponentType();
                    if (component_type != null) {
                        System.out.format(" (%s)", component_type.toString());
                    }
                    System.out.format("%n");
                }
            }
            // Blank line to separate classes.
            System.out.println();
        }
    }
}
