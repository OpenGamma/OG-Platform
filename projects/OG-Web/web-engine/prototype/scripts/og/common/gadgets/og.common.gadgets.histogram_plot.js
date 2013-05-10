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
                alive = og.common.id('gadget_histogram_plot'), width, height, buckets_height = 30,
                line_tmpl = Handlebars.compile('<div class="og-histogram-{{{type}}}-line" style="left:{{{left}}}px;height:{{{height}}}px;"></div>');
                label_tmpl = Handlebars.compile('<div class="og-histogram-var-label" style="left:{{{left}}}px;top:{{{top}}}px;">{{label}}}</div>');
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
            gadget.update = function (config) {
                $plot.setData(config);
                $plot.setupGrid();
                $plot.draw();
            }
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
                    $selector.find('span.og-bucket-select').bind('click', function (event) {
                        var $elm = $(event.target), rebucket_data = config.callback($elm .attr('name'));
                        $elm.siblings().removeClass('OG-link-active');
                        $elm.addClass('OG-link-active');
                        $plot.setData(plot_data(rebucket_data));
                        $plot.setupGrid();
                        $plot.draw();
                    });
                    $selector.find('span.og-histogram-refresh').bind('click', function (event) {
                        $plot.setData(plot_data(config.update()));
                        $plot.setupGrid();
                        $plot.draw();
                    });
                    gadget.resize();
                });
            };

            var plot_data = function (input) {
                return data = [
                {
                    label: 'Histogram', hoverable: true,
                    data: input ? input.histogram_data : config.histogram_data,
                    bars: {
                        show: true, barWidth: input ? input.interval : config.interval,
                        fill: true, lineWidth: 1, order: 1, fillColor: '#42669a'
                    },
                    color: '#fff'
                },
                {
                    label: "Probability density - Normal Distribution", data: config.norm_pdf_data, hoverable: false,
                    lines: {show: false, fill: false},
                    points: {show: true},
                    color: '#AA4643',
                    yaxis: 2,
                }];
            }
            var load_plots = function () {
                var previousPoint = null, var99pos, var95pos, cvar99pos, cvar95pos,
                    $plot_selector = $(selector + ' .og-histogram-plot');
                options = {
                    grid: {
                        borderWidth: 0, labelMargin: 4, color: '#999', minBorderMargin: 0,
                        backgroundColor: '#fff', hoverable: true, aboveData: false
                    },
                    legend: {show: false},
                    bars: {show: true, lineWidth: 0, barWidth: 0, fill: true, align: 'left', horizontal: false}
                };
                $plot = $.plot($(selector + ' .og-histogram-plot'), plot_data(), options);
                if(config.vars){
                    var99pos = $plot.pointOffset({ x: config.vars.var99, y: 0});
                    var95pos = $plot.pointOffset({ x: config.vars.var95, y: 0});
                    cvar99pos = $plot.pointOffset({ x: config.vars.cvar99, y: 0});
                    cvar95pos = $plot.pointOffset({ x: config.vars.cvar95, y: 0});
                    $plot_selector.append(line_tmpl({type:'var99', left: var99pos.left, height: $plot.height()}));
                    $plot_selector.append(line_tmpl({type:'var95', left: var95pos.left, height: $plot.height()}));
                    $plot_selector.append(line_tmpl({type:'cvar99', left: cvar99pos.left, height: $plot.height()}));
                    $plot_selector.append(line_tmpl({type:'cvar95', left: cvar95pos.left, height: $plot.height()}));
                    $plot_selector.append(label_tmpl({left: var99pos.left + 4, top: '75', label: 'Var 99%'}));
                    $plot_selector.append(label_tmpl({left: var95pos.left + 4, top: '25', label: 'Var 95%'}));
                    $plot_selector.append(label_tmpl({left: cvar99pos.left + 4, top: '100', label: 'CVar 99%'}));
                    $plot_selector.append(label_tmpl({left: cvar95pos.left + 4, top: '50', label: 'CVar 95%'}));
                }
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