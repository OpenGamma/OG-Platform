(function($) {
    $.plot.plugins.push({
        init: init,
        name: 'dashes',
        version: '1.0',
        options: {
            series: {
                dashes: {
                    dashLength: 10, //Length of dash and space, default 10px {integer} or {2d array} - [length, space]
                    lineWidth: 2, //Width of dash, default 2px {integer}
                    show: false //Display dashes for series, default false {boolean}
                }
            }
        }
    });
    function init(plot) {
        plot.hooks.drawSeries.push(function (plot, canvascontext, series) {
            if (!series.dashes.show || !series.dashes.lineWidth) {return; }
            var plotOffset = plot.getPlotOffset(), sqrt = Math.sqrt, pow = Math.pow;
            canvascontext.save();
            canvascontext.translate(plotOffset.left, plotOffset.top);
            canvascontext.lineWidth = series.dashes.lineWidth;
            canvascontext.strokeStyle = series.color;
            plotter();
            canvascontext.restore();
            function plotter() {
                var i, points = series.datapoints.points, ps = series.datapoints.pointsize, axisx = series.xaxis,
                    axisy = series.yaxis, prevx = null, prevy = null, remainder = 0, drawing = true, ax1, ay1, ax2, ay2,
                    dashLength = series.dashes.dashLength[0] ? series.dashes.dashLength[0] : series.dashes.dashLength,
                    spaceLength = series.dashes.dashLength[1] ? series.dashes.dashLength[1] : dashLength;
                canvascontext.beginPath();
                for (i = ps; i < points.length; i += ps) {
                    var x1 = points[i - ps], y1 = points[i - ps + 1],
                        x2 = points[i], y2 = points[i + 1];
                    //same logic as plotLine in jquery.flot.js
                    if (x1 === null || x2 === null)
                        continue;
                    // clip with ymin
                    if (y1 <= y2 && y1 < axisy.min) {
                        if (y2 < axisy.min)
                            continue;   // line segment is outside
                        // compute new intersection point
                        x1 = (axisy.min - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y1 = axisy.min;
                    }
                    else if (y2 <= y1 && y2 < axisy.min) {
                        if (y1 < axisy.min)
                            continue;
                        x2 = (axisy.min - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y2 = axisy.min;
                    }
                    // clip with ymax
                    if (y1 >= y2 && y1 > axisy.max) {
                        if (y2 > axisy.max)
                            continue;
                        x1 = (axisy.max - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y1 = axisy.max;
                    }
                    else if (y2 >= y1 && y2 > axisy.max) {
                        if (y1 > axisy.max)
                            continue;
                        x2 = (axisy.max - y1) / (y2 - y1) * (x2 - x1) + x1;
                        y2 = axisy.max;
                    }
                    // clip with xmin
                    if (x1 <= x2 && x1 < axisx.min) {
                        if (x2 < axisx.min)
                            continue;
                        y1 = (axisx.min - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x1 = axisx.min;
                    }
                    else if (x2 <= x1 && x2 < axisx.min) {
                        if (x1 < axisx.min)
                            continue;
                        y2 = (axisx.min - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x2 = axisx.min;
                    }
                    // clip with xmax
                    if (x1 >= x2 && x1 > axisx.max) {
                        if (x2 > axisx.max)
                            continue;
                        y1 = (axisx.max - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x1 = axisx.max;
                    }
                    else if (x2 >= x1 && x2 > axisx.max) {
                        if (x1 > axisx.max)
                            continue;
                        y2 = (axisx.max - x1) / (x2 - x1) * (y2 - y1) + y1;
                        x2 = axisx.max;
                    }
                    //end: same logic as plotLine in jquery.flot.js
                    ax1 = axisx.p2c(x1);
                    ay1 = axisy.p2c(y1);
                    ax2 = axisx.p2c(x2);
                    ay2 = axisy.p2c(y2);
                    if (x1 != prevx || y1 != prevy)
                        canvascontext.moveTo(ax1, ay1);
                    while(true) {
                        var hypotenuse, segmentLength = remainder > 0 ? remainder : drawing ? dashLength : spaceLength,
                            deltax, deltay;
                            hypotenuse = sqrt(pow(ax2 - ax1, 2) + pow(ay2 - ay1, 2));
                        if (hypotenuse <= segmentLength) {
                            deltax = ax2 - ax1;
                            deltay = ay2 - ay1;
                            remainder = segmentLength - hypotenuse;
                        } else {
                            var xsign = (ax2 > ax1) ? 1 : -1, ysign = (ay2 > ay1) ? 1 : -1;
                            deltax = xsign * sqrt(pow(segmentLength, 2) / (1 + pow((ay2 - ay1)/(ax2 - ax1), 2)));
                            deltay = ysign * sqrt(pow(segmentLength, 2) - pow(segmentLength, 2) / (1 + pow((ay2 - ay1)/(ax2 - ax1), 2)));
                            hypotenuse = segmentLength;
                            remainder = 0;
                        }
                        if (!!deltax || !!deltay) {
                            if (drawing) {
                                canvascontext.lineTo(ax1 + deltax, ay1 + deltay);
                            } else {
                                canvascontext.moveTo(ax1 + deltax, ay1 + deltay);
                            }
                        }
                        drawing = !drawing;
                        ax1 += deltax;
                        ay1 += deltay;
                        if(hypotenuse <= 0) break;
                    }
                    prevx = x2;
                    prevy = y2;
                }//for each point
                canvascontext.stroke();
            }//plotter
        });//hook
    }//init
})(jQuery);