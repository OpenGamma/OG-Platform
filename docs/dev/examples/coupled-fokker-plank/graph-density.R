library("rjson")

graph_data <- function(data_source, data_name, graph_filename, ...) {
    # Set up a textConnection so we can use 'scan'
    raw_data = data_source[[data_name]]
    data_conn <- textConnection(raw_data)

    # The row of x values starts with \t implying an empty first slot, but we
    # skip this with na.omit so are left with the ySteps+1 values.
    x <- as.vector(na.omit(scan(data_conn, sep="\t", skip=1, nlines=1)))
    nx = length(x) + 1 

    # We have already read first 2 rows, this scans the remaining rows.
    data <- scan(data_conn, sep="\t")
    ny <- length(data)/nx
    m <- matrix(data, nx, ny) 

    y <- m[1,]
    z <- m[-1,]

    png(graph_filename)
    persp(x, y, z, ...)
    dev.off()
}

json_data = fromJSON(file="../../../shared/example-output.json")$com.opengamma.analytics.example.coupledfokkerplank.CoupledFokkerPlankExample$runCoupledFokkerPlank
example_output = fromJSON(json_data)

graph_data(
           example_output,
           "state_1_data",
           "dexy--state-1-plot.png",
           col="purple", phi=0
           )

graph_data(
           example_output,
           "state_2_data",
           "dexy--state-2-plot.png",
           col="green", phi=15, theta=5
           )

