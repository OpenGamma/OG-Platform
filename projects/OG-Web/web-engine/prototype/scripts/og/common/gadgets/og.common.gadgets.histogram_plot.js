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
            var gadget = this, selector = config.selector, $selector, $plot, options = {}, plot_template,
                alive = og.common.id('gadget_histogram_plot'), width, height, buckets_height = 30;
            gadget.resize = function () {
                height = $selector.parent().height();
                width = $selector.width();
                $selector.find('.og-buckets').height(buckets_height);
                $selector.find('.og-histogram-plot').height(height - buckets_height);
                $selector.find('.og-histogram-plot').width(width);
                load_plots();
            };
            gadget.alive = function () {
                return !!$('.' + alive).length;
            };
            show_tooltip = function (x, y, contents) {
                $('<div id="tooltip">' + contents + '</div>').css( {
                    position: 'absolute', display: 'none', top: y - 70, left: x - 20,
                    padding: '2px', backgroundColor: '#000', opacity: 0.50, zIndex: 6, color: '#fff'
                }).appendTo("body").fadeIn(200);
            };
            var setup = function () {
                $.when(og.api.text({module: 'og.views.gadgets.histogram.plot_tash'})).then(function (plot_tmpl) {
                    plot_template = plot_tmpl;
                    $selector = $(selector);
                    $selector.html((Handlebars.compile(plot_template))({alive: alive, buckets: config.buckets}));
                    $selector.find('.og-bucket-mid').addClass('OG-link-active');
                    $selector.find('span').bind('click', function (event) {
                        var $elm = $(event.target), new_data = config.callback($elm .attr('name'));
                        $elm.siblings().removeClass('OG-link-active');
                        $elm.addClass('OG-link-active');
                        $plot.setData(get_data(new_data));
                        $plot.setupGrid();
                        $plot.draw();
                    });
                    gadget.resize();
                });
            };
            var get_data = function (input) {
                return data = [{
                    label: 'Histogram', hoverable: true,
                    data: input ? input.histogram_data : config.histogram_data,
                    bars: { show: true, barWidth: input ? input.interval : config.interval,
                            fill: true, lineWidth: 1, order: 1, fillColor: '#42669a'},
                    color: '#fff'},
                    {label: "Probability density - Normal Distribution", data: config.norm_pdf_data, hoverable: false,
                     lines: {show: false, fill: false}, points: {show: true}, color: '#AA4643', yaxis: 2 }
                ];
            }
            var load_plots = function () {
                var previousPoint = null;
                options = {grid: { borderWidth: 0, labelMargin: 4, color: '#999', minBorderMargin: 0,
                    backgroundColor: '#fff', hoverable: true, aboveData: false}, legend: {show: false},
                    bars: {show: true, lineWidth: 0, barWidth: 0, fill: true, align: 'left', horizontal: false}};
                $plot = $.plot($(selector + ' .og-histogram-plot'), get_data(), options);
                $(selector).bind('plothover', function (event, pos, item) {
                    if (item) {
                        if (previousPoint != item.dataIndex) {
                            var x = item.datapoint[0], y = item.datapoint[1], delta = x+config.interval,
                                occur = y == 1 ? ' occurrence ' : ' occurrences ',
                                msg = y + occur + 'in range<br/>' + x.toFixed(5) + ' to ' + x.toFixed(5);
                                previousPoint = item.dataIndex;
                            $('#tooltip').remove();
                            show_tooltip(pos.pageX, pos.pageY, msg);
                        }
                    } else {
                        $("#tooltip").remove();
                        previousPoint = null;
                    }
                });
            };
            if (!config.child) og.common.gadgets.manager.register(gadget);
            setup();
        };
    }
});