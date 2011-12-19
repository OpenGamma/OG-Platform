import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import java.io.PrintStream;
import javax.time.calendar.LocalDate;

public class TimeSeriesExample {
    public static void timeSeriesExample(PrintStream out) {
        MutableLocalDateDoubleTimeSeries ts1 = new ListLocalDateDoubleTimeSeries();
        ts1.putDataPoint(LocalDate.of(2010, 1, 1), 2.1d);
        ts1.putDataPoint(LocalDate.of(2010, 1, 2), 2.2d);
        ts1.putDataPoint(LocalDate.of(2010, 1, 3), 2.3d);
        out.println("ts1: " + ts1);

        LocalDateDoubleTimeSeries ts2 = new ArrayLocalDateDoubleTimeSeries(ts1);
        out.println("ts2: " + ts2);

        LocalDateDoubleTimeSeries ts3 = new ArrayLocalDateDoubleTimeSeries(
                new LocalDate[] { LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 2) },
                new double[] { 1.1d, 1.2d }
                );
        out.println("ts3: " + ts3);

        LocalDateDoubleTimeSeries ts4 = (LocalDateDoubleTimeSeries) ts2.subSeries(LocalDate.of(2010, 1, 2), LocalDate.of(2010, 1, 3));
        out.println("ts4: " + ts4);
    }

    public static void main(String[] args) throws Exception {
        String[] ogargs = {"TimeSeriesExample", "../dexy--time-series-output.json"};
        OpenGammaExampleClass.main(ogargs);
    }
}
