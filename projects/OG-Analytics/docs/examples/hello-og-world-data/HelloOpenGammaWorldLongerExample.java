// @export "imports"
import com.opengamma.math.TrigonometricFunctionUtils;
import com.opengamma.math.number.ComplexNumber;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.Math;

// @export "class-def"
public class HelloOpenGammaWorldLongerExample {
    public static void main(String[] args) throws FileNotFoundException {
        // @export "open-data-file"
        File data_file = new File("../dexy--hello-world-data.txt");
        PrintStream data_stream = new PrintStream(data_file);

        // @export "init"
        ComplexNumber cn = new ComplexNumber(0.0, 1.0);
        data_stream.println("The complex number is " + cn.toString());

        // @export "calculate"
        data_stream.println("The sine is " + TrigonometricFunctionUtils.sin(cn));
        data_stream.println("The cosine is " + TrigonometricFunctionUtils.cos(cn));
        data_stream.println("The tangent is " + TrigonometricFunctionUtils.tan(cn));

        // @export "close-data-file"
        data_stream.close();
    }
}
