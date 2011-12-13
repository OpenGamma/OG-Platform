// @export "imports"
import java.io.PrintStream;

// @export "classDeclaration"
public class SimpleExample {
    public static String HELLO = "Hello, world!";

    // @export "basicDemo"
    public static void basicDemo(PrintStream out) {
        out.println("Say: " + HELLO);
    }

    // @export "helperMethod"
    public static int helperMethod() {
        return 5;
    }

    // @export "useHelperMethod"
    public static void useHelperMethod(PrintStream out) {
        out.println("The answer is " + helperMethod());
    }

    // @export "main"
    public static void main(String[] args) throws Exception {
        String[] ogargs = {
            "SimpleExample",
            "../dexy--simple-output.json",
            "../dexy--simple-fields.json"
        };
        OpenGammaExample.main(ogargs);
    }
    // @end
}
