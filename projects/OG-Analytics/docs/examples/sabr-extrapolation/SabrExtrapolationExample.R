### @export "libraries"
library(lattice)

### @export "read-data"
data = read.table("dexy--smile-multi-mu-data.txt", header=TRUE)
data$Mu <- as.factor(data$Mu)

### @export "extract-data-by-mu"
x1 <- subset(data$Price, data$Mu==5.0)
x2 <- subset(data$Price, data$Mu==40.0)
x3 <- subset(data$Price, data$Mu==90.0)
x4 <- subset(data$Price, data$Mu==150.0)

y1 <- subset(data$Strike, data$Mu==5.0)
y2 <- subset(data$Strike, data$Mu==40.0)
y3 <- subset(data$Strike, data$Mu==90.0)
y4 <- subset(data$Strike, data$Mu==150.0)

n <- length(x1)

### @export "calculate-density"
Density1 <- c(NA, (x1[3:n] + x1[1:(n-2)] - 2 * x1[2:(n-1)])
        /(y1[2:(n-1)] - y1[1:(n-2)])^2, NA)
Density2 <- c(NA, (x2[3:n] + x2[1:(n-2)] - 2 * x2[2:(n-1)])
        /(y2[2:(n-1)] - y2[1:(n-2)])^2, NA)
Density3 <- c(NA, (x3[3:n] + x3[1:(n-2)] - 2 * x3[2:(n-1)])
        /(y3[2:(n-1)] - y3[1:(n-2)])^2, NA)
Density4 <- c(NA, (x4[3:n] + x4[1:(n-2)] - 2 * x4[2:(n-1)])
        /(y4[2:(n-1)] - y4[1:(n-2)])^2, NA)

data$Density <- c(Density1, Density2, Density3, Density4)


### @export "plot-data"
png("dexy--extrapolation-price.png", width=600)
with(data,
xyplot(Price ~ Strike, groups=Mu, type="l", lwd=2,
        xlab=list(cex=2), ylab=list(cex=2), scales=list(x=list(cex=2), y=list(cex=2)),
        ylim=c(0, 0.0004), xlim=c(0.08,0.16),
        auto.key = list(cex=2, text = paste("mu =", levels(Mu)),
            corner=c(0.95,0.95), points = F, lines = T))
)
dev.off()

### @export "plot-data-pdf"
pdf("dexy--extrapolation-price.pdf", width=15)
with(data,
xyplot(Price ~ Strike, groups=Mu, type="l", lwd=2,
        xlab=list(cex=2), ylab=list(cex=2), scales=list(x=list(cex=2), y=list(cex=2)),
        ylim=c(0, 0.0004), xlim=c(0.08,0.16),
        auto.key = list(cex=2, text = paste("mu =", levels(Mu)),
            corner=c(0.95,0.95), points = F, lines = T))
)
dev.off()

### @export "plot-implied-vol"
png("dexy--extrapolation-smile.png", width=600)
with(data,
xyplot(ImpliedVolPct ~ Strike, groups=Mu, type="l", lwd=2,
        xlab=list(cex=2), ylab=list(cex=2), scales=list(x=list(cex=2), y=list(cex=2)),
        auto.key = list(cex=2, text = paste("mu =", levels(Mu)),
            corner=c(0.05,0.95), points = F, lines = T))
)
dev.off()

### @export "plot-implied-vol-pdf"
pdf("dexy--extrapolation-smile.pdf", width=15)
with(data,
xyplot(ImpliedVolPct ~ Strike, groups=Mu, type="l", lwd=2, 
        xlab=list(cex=2), ylab=list(cex=2), scales=list(x=list(cex=2), y=list(cex=2)),
        auto.key = list(cex=2, text = paste("mu =", levels(Mu)),
            corner=c(0.05,0.95), points = F, lines = T))
)
dev.off()

### @export "plot-density"
png("dexy--extrapolation-density.png", width=600)
with(data,
xyplot(Density ~ Strike, groups=Mu, type="l", lwd=2, ylim=c(0, 0.5),
        xlab=list(cex=2), ylab=list(cex=2), scales=list(x=list(cex=2), y=list(cex=2)),
        auto.key = list(cex=2, text = paste("mu =", levels(Mu)),
            corner=c(0.95,0.95), points = F, lines = T))
)
dev.off()

### @export "plot-density-pdf"
pdf("dexy--extrapolation-density.pdf", width=15)
with(data,
xyplot(Density ~ Strike, groups=Mu, type="l", lwd=2, ylim=c(0, 0.5),
        xlab=list(cex=2), ylab=list(cex=2), scales=list(x=list(cex=2), y=list(cex=2)),
        auto.key = list(cex=2, text = paste("mu =", levels(Mu)),
            corner=c(0.95,0.95), points = F, lines = T))
)
dev.off()

