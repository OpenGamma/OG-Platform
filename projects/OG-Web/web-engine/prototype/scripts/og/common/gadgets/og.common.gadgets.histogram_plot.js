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
                    position: 'absolute', display: 'none', top: y + 5, left: x + 5, border: '1px solid #fdd',
                    padding: '2px', backgroundColor: '#fee', opacity: 0.80, zIndex: 6
                }).appendTo("body").fadeIn(200);
            };
            load_plots = function () {
                var previousPoint = null;
                options = { grid: { hoverable: true}};
                var data = [
                    {
                        label: "Histogram",
                        hoverable: true,
                        data: config.histogram_data,
                        bars: {
                            show: true,
                            barWidth: config.interval,
                            fill: true,
                            lineWidth: 1
                        },
                        color: "#42669a"
                    },
                    {
                        label: "Probability density - Normal Distribution",
                        data: config.norm_pdf_data,
                        hoverable: false,
                        lines: {
                            show: true,
                            fill: false
                        },
                        color: '#AA4643',
                        yaxis: 2
                    }
                ];
                $plot = $.plot($(selector), data, options);
                $(selector).bind("plothover", function (event, pos, item) {
                    if (item && item.series.hoverable) {
                        if (previousPoint != item.dataIndex) {
                            var x = item.datapoint[0], y = item.datapoint[1], delta = x+config.bar, 
                                msg = y + " occurrences in range<br/>" + x.toFixed(5) + " to " + delta.toFixed(5);
                                previousPoint = item.dataIndex;
                            $("#tooltip").remove();
                            show_tooltip(item.pageX, item.pageY, msg);
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