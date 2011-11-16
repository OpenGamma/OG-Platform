// @export "imports"
import com.opengamma.math.TrigonometricFunctionUtils;
import com.opengamma.math.number.ComplexNumber;
import java.lang.Math;

// @export "class-def"
public class HelloOpenGammaWorldLongerExample {
    public static void main(String[] args) {
        // @export "init"
        ComplexNumber cn = new ComplexNumber(0.0, Math.PI);
        System.out.println("The complex number is " + cn.toString());

        // @export "calculate"
        System.out.println("The sine is " + TrigonometricFunctionUtils.sin(cn));
        System.out.println("The cosine is " + TrigonometricFunctionUtils.cos(cn));
    }
}
