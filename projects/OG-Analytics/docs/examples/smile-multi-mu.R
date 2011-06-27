library(lattice)

### @export "read-data"
data = read.table("dexy--smile-data.txt", header=TRUE)
data$Mu <- as.factor(data$Mu)

### @export "plot-data"
png("dexy--smile.png", width=480*1.7)
with(data,
xyplot(Price ~ StrikePrice, groups=Mu, type="l", lwd=2,
        ylim=c(0, 0.0004), xlim=c(0.08,0.16), auto.key=TRUE)
)
dev.off()
