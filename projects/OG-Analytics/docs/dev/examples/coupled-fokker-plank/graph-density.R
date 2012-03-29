graph_data_file <- function(data_filename, graph_filename, ...) {
    cat("name and dims of", data_filename, scan(data_filename, n=1, sep="\n", what=character()), "\n")

    # TODO parse these numbers from file...
    nx <- 150
    ny <- 50

    x <- as.vector(na.omit(scan(data_filename, sep="\t", skip=1, n=nx+1)))

    data <- scan(data_filename, sep="\t", skip=2)
    m <- matrix(data, nx+1, ny)

    y <- m[1,]
    z <- m[-1,]

    png(graph_filename)
    persp(x, y, z, ...)
    dev.off()
}

graph_data_file(
        "dexy--state-1-density.txt",
        "dexy--state-1-plot.png",
        col="purple", phi=0
        )

graph_data_file(
        "dexy--state-2-density.txt",
        "dexy--state-2-plot.png",
        col="green", phi=15, theta=5
        )

