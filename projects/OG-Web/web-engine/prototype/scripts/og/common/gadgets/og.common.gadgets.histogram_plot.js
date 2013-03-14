/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.HistogramPlot',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var module = this, loading_template;
        return function (config) {
            var gadget = this, selector = config.selector, $plot, options = {};
            gadget.resize = function () {
                load_plots();
            };
            show_tooltip = function (x, y, contents){
                $('<div id="tooltip">' + contents + '</div>').css( {
                    position: 'absolute', display: 'none', top: y - 70, left: x - 20,
                    padding: '2px', backgroundColor: '#000', opacity: 0.50, zIndex: 6, color: '#fff'
                }).appendTo("body").fadeIn(200);
            };
            percentage = function (val, points) {
                return (val*100).toFixed(points || 2) +"%";  
            };
            load_plots = function () {
                var previousPoint = null;
                options = {
                    grid: {
                        borderWidth: 0, labelMargin: 4, color: '#999', minBorderMargin: 0, backgroundColor: '#fff',
                        hoverable: true, aboveData: false
                    },
                    legend: {show: false},
                    bars: {
                        show: true,
                        lineWidth: 0, // in pixels
                        barWidth: 0, // in units of the x axis
                        fill: true,
                        align: 'left', // or "center"
                        horizontal: false
                    },
                    xaxis: {tickFormatter: function (val, axis) {return percentage(val);}}
                };
                var data = [
                    {
                        label: 'Histogram',
                        hoverable: true,
                        data: config.histogram_data,
                        bars: {
                            show: true,
                            barWidth: config.interval,
                            fill: true,
                            lineWidth: 1,
                            order: 1,
                            fillColor: '#42669a'
                         },
                         color: '#fff'
                    }/*,
                    {
                        label: "Probability density - Normal Distribution",
                        data: config.norm_pdf_data,
                        hoverable: false,
                        lines: {
                            show: false,
                            fill: false
                        },
                        points: {show: true},
                        color: '#AA4643',
                        yaxis: 2
                    }*/
                ];
                $plot = $.plot($(selector), data, options);
                $(selector).bind('plothover', function (event, pos, item) {
                    if (item) {
                        if (previousPoint != item.dataIndex) {
                            var x = item.datapoint[0], y = item.datapoint[1], delta = x+config.interval,
                                occur = y == 1 ? ' occurrence ' : ' occurrences ',
                                msg = y + occur + 'in range<br/>' + percentage(x, 5) + ' to ' + percentage(delta,5);
                                previousPoint = item.dataIndex;
                            $('#tooltip').remove();
                            show_tooltip(pos.pageX, pos.pageY, msg);
                        }
                    }
                    else {
                        $("#tooltip").remove();
                        previousPoint = null;
                    }
                });
            };
            if (!config.child) og.common.gadgets.manager.register(gadget);
            load_plots();
        };
    }
});