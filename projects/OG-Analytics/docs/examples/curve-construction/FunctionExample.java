// @export "imports"
import com.opengamma.math.function.RealPolynomialFunction1D;
import java.io.PrintStream;
import java.util.Arrays;

// @export "classDefinition"
public class FunctionExample {
    // @export "polyDerivativeDemo"
    public static RealPolynomialFunction1D getFunction() {
        double[] coefficients = {-125,75,-15,1};
        return new RealPolynomialFunction1D(coefficients);
    }

    public static void polyDerivativeDemo(PrintStream out) {
        RealPolynomialFunction1D f = getFunction();

        assert f.evaluate(5.0) == 0.0;

        RealPolynomialFunction1D d = f.derivative();
        double[] coefficients = d.getCoefficients();
        out.println(Arrays.toString(coefficients));
    }

    // @export "addFunctionDemo"
    public static void addFunctionDemo(PrintStream out) {
    }

    // @end
    public static void main(String[] args) throws Exception {
        String[] ogargs = {"FunctionExample", "../dexy--function-output.json"};
        OpenGammaExample.main(ogargs);
    }
}
