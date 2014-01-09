/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 *
 * Renders a Canvas plot using Flot
 * @see http://code.google.com/p/flot/
 */
$.register_module({
    name: 'og.common.Timeseries',
    dependencies: ['og.api.rest'],
    obj: function () {
        var api = og.api;
        /**
         * @param {object} config
         * @param {String} config.selector
         * @param {String} config.id
         * @param {String} config.height optional height of Timeseries, gadget will calculate height from parent
         * if not supplied
         * @param {Boolean} config.child Manage resizing gadget manualy
         * @param {Object} config.data Spoffed Data - temporary solution
         */
        return function (config) {
            var timeseries = this, handler, x_max, alive = og.common.id('gadget_timeseries_plot'),
                selector = config.selector, load_plots, initial_preset, $refresh,
                meta = {}, // object that stores the structure and data of the plots
                plot_template, data_template, common_plot_options, top_plot_options, bot_plot_options, spoofed_data,
                colors_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'], // line colors for plot 1 data sets
                colors_arr_p2 = ['#fff', '#fff', '#fff', '#fff']; // line colors for plot 2 data sets
            timeseries.update = function (data) {load_plots(data);};
            timeseries.resize = (function (timeout) {
               var resize = function () {
                   var height = config.height ? config.height : $(selector).parent().height(),
                       width = $(selector).width(),
                       h_ticks = Math.ceil(width / 100),
                       v_ticks = Math.ceil((height - 80) / 50);
                   $(selector).find('.og-js-p1, .og-js-p2, .og-flot-xaxis').width(width - 2 + 'px');
                   $(selector).find('.og-js-p1').height(height - 83);
                   top_plot_options.xaxis.ticks = bot_plot_options.xaxis.ticks = h_ticks;
                   top_plot_options.yaxis.ticks = bot_plot_options.yaxis.ticks = v_ticks;
                   load_plots();
               };
               return function () {timeout = clearTimeout(timeout) || setTimeout(resize, 0);}
            })(null);
            timeseries.alive = function () {return !!$('.' + alive).length;};
            timeseries.display_refresh = function () {
                $refresh.show();
            };
            spoofed_data = (function (data) {
                if (!data) return null;
                data.forEach(function (val, idx) {
                    var field = val.template_data.data_field, time = val.template_data.observation_time;
                    if (!meta[field]) meta[field] = {};
                    meta[field][time] = {error: false, data: data[idx]};
                });
                return {main: {error: false, data: data[0]}, search: {error: false, data: {data: []}}};
            })(config.data);
            common_plot_options = {
                grid: {borderWidth: 0, color: '#999', aboveData: false, minBorderMargin: 0},
                lines: {lineWidth: 1, fill: true, fillColor: '#f8fbfd'},
                legend: {backgroundColor: null},
                series: {shadowSize: 0, threshold: {below: 0, color: '#960505'}},
                xaxis: {ticks: 6, mode: 'time', tickLength: 0},
                yaxis: {position: 'left', color: '#4a6d9e'}
            };
            top_plot_options = $.extend(true, {}, common_plot_options, {
                colors: colors_arr,
                crosshair: {mode: 'x', color: '#e5e5e5', lineWidth: '1'},
                grid: {labelMargin: 4, hoverable: true, aboveData: true},
                lines: {fillColor: '#f8fbfd'},
                legend: {show: true, labelBoxBorderColor: 'transparent', position: 'nw', margin: 1},
                pan: {interactive: true, cursor: "move", frameRate: 30},
                selection: {mode: null},
                xaxis: {labelHeight: 14, color: '#4a6d9e', tickColor: '#fff', min: initial_preset, max: x_max},
                yaxis: {ticks: 5, panRange: false, tickLength: 'full', tickColor: '#f3f3f3', labelWidth: 40}
            });
            bot_plot_options = $.extend(true, {}, common_plot_options, {
                colors: colors_arr_p2,
                grid: {aboveData: true, labelMargin: -13, minBorderMargin: 1},
                lines: {fill: false},
                legend: {show: false},
                selection: {mode: 'x', color: '#fff'},
                xaxis: {labelHeight: 13, tickColor: '#fff', color: '#fff'},
                yaxis: {show: false}
            });
            handler = function (result) {
                if (result.error) return;
                var data = result.data,
                    init_ob_time = data.template_data.observation_time,
                    data_arr = [{
                        data: data.timeseries.data,
                        label: data.template_data.observation_time,
                        data_provider: data.template_data.data_provider,
                        data_source: data.template_data.data_source,
                        object_id: data.template_data.object_id
                    }],
                    state = {}, // keeps a record of the current data sets in the plot along with zoom and pan data
                    $p1, p1_options, p1_selector = selector + ' .og-js-p1',
                    $p2, p2_options, p2_selector = selector + ' .og-js-p2',
                    tenor = selector + ' .og-tenor',
                    $legend, panning, hover_pos = null,
                    reset_options,
                    empty_plots, update_legend, rescale_yaxis, resize,
                    calculate_y_values, get_legend;
                $(selector).html((Handlebars.compile(plot_template))({alive: alive}));
                $refresh = $(selector).find('div.og-timeseries-refresh');
                $refresh.on('click', function (event) {
                    timeseries.update(handler({data: config.update()}));
                });
                get_legend = function () {return $(selector + ' .legend');}; // the legend is often regenerated
                reset_options = function () {p1_options = top_plot_options, p2_options = bot_plot_options;};
                empty_plots = function () {
                    var d = ['1', '2'], disabled_options,
                        msg = $('<div>Not enough data to render plot</div>').css({
                            position: 'absolute', color: '#999', left: '10px', top: '10px'
                        });
                    reset_options();
                    disabled_options = {
                        grid: {borderWidth: 0},
                        xaxis: {show: false},
                        yaxis: {show: false},
                        selection: {mode: null},
                        pan: {interactive: false}
                    };
                    $(tenor + ', .og-flot-xaxis').css({visibility: 'hidden'});
                    $p1 = $.plot($(p1_selector), d, $.extend(true, {}, p1_options, disabled_options));
                    $p2 = $.plot($(p2_selector), d, $.extend(true, {}, p2_options, disabled_options));
                    $(p1_selector).append(msg);
                    setTimeout(show_plot);
                };
                load_plots = function (new_data) {
                    if (new_data) data_arr = new_data;
                    if (data_arr[0] === void 0 || data_arr[0].data.length < 2) {empty_plots(); return}
                    var d = data_arr, data = data_arr[0].data;
                    (function () { // set up presets
                        var new_max_date_obj, _1m, _3m, _6m, _1y, _2y, _3y, counter = 0, presets = {};
                        x_max = data[data.length-1][0];
                        new_max_date_obj = function () {return new Date(x_max);};
                        presets._1d = x_max - 86400 * 1000; // in milliseconds
                        presets._1w = x_max - 7 * 86400 * 1000;
                        _1m = new_max_date_obj(), _1m.setUTCMonth(_1m.getUTCMonth() - 1), presets._1m = +_1m;
                        _3m = new_max_date_obj(), _3m.setUTCMonth(_3m.getUTCMonth() - 3), presets._3m = +_3m;
                        _6m = new_max_date_obj(), _6m.setUTCMonth(_6m.getUTCMonth() - 6), presets._6m = +_6m;
                        _1y = new_max_date_obj(), _1y.setUTCFullYear(_1y.getUTCFullYear() - 1), presets._1y = +_1y;
                        _2y = new_max_date_obj(), _2y.setUTCFullYear(_2y.getUTCFullYear() - 2), presets._2y = +_2y;
                        _3y = new_max_date_obj(), _3y.setUTCFullYear(_3y.getUTCFullYear() - 3), presets._3y = +_3y;
                        presets._all = data[0][0];
                        $(tenor).html(['all', '3y', '2y', '1y', '6m', '3m', '1m', '1w', '1d']
                            .reduce(function (pre, cur) {
                                var is_valid = presets['_' + cur] >= presets._all,
                                    classes = is_valid ? 'OG-link og-js-' + cur : 'OG-link-disabled';
                                if (is_valid) ++counter;
                                if (!state.zoom && counter === 3) { // pick the 3rd largest valid preset by default
                                    classes += ' OG-link-active', initial_preset = presets['_' + cur];
                                    state.zoom = '_' + cur;
                                } else if (state.zoom === '_' + cur) { // or the one last set by the user
                                    classes += ' OG-link-active', initial_preset = presets[state.zoom];
                                }
                                pre.unshift('<span class="'+ classes +'" style="margin-right: 5px;">'
                                    + cur +'</span>');
                                return pre;
                            }, []).join('')).css({visibility: 'visible'}).unbind().bind('click', function (e) {
                                e.stopPropagation();
                                var target = e.target, from = presets['_' + target.textContent], $elm = $(target);
                                if ($elm.hasClass('OG-link-disabled') || !$elm.is('span')) return;
                                state.zoom = '_' + target.textContent;
                                $elm.siblings().removeClass('OG-link-active'), $elm.addClass('OG-link-active');
                                state.from = from, state.to = x_max;
                                $p2.setSelection({xaxis: {from: from , to: x_max}}, true);
                                p1_options.xaxis.min = from, p1_options.xaxis.max = x_max;
                                rescale_yaxis();
                                $legend = get_legend();
                                $legend.hide();
                            });
                    }());
                    reset_options();
                    p1_options.xaxis.panRange = [data[0][0], data[data.length-1][0]];
                    if (!(state.from && state.to)) {state.from = initial_preset, state.to = x_max;}
                    // in xaxis, min/max sets the pan, from/to sets the selection
                    p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                    p2_options.xaxis.from = state.from, p1_options.xaxis.to = state.to;
                    calculate_y_values();
                    if (data_arr.length > 1) p1_options.lines.fill = p2_options.lines.fill = false;
                    else p1_options.lines.fill = true;
                    $p1 = $.plot($(p1_selector), d, p1_options);
                    $p2 = $.plot($(p2_selector), d, p2_options);
                    $p2.setSelection({xaxis: {from: state.from, to: state.to}}, true);
                    // connect the two plots
                    $(p1_selector).unbind('plotselected').bind('plotselected', function (e, r) { // events, ranges
                        state.from = r.xaxis.from, state.to = r.xaxis.to;
                        p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                        $p1 = $.plot($(p1_selector), d, p1_options);
                        $legend = get_legend(), $legend.hide();
                    });
                    $(p1_selector).unbind('plotpan').bind('plotpan', function (e, obj) { // panning
                        var mouseup = function () {
                            setTimeout(function () {panning = false;}, 0);
                            $(document).unbind('mouseup', mouseup);
                            rescale_yaxis();
                        },
                        xaxes = obj.getXAxes()[0];
                        panning = true;
                        $(document).bind('mouseup', mouseup);
                        $legend = get_legend(), $legend.hide();
                        $p2.setSelection({xaxis: {from: state.from = xaxes.min, to: state.to = xaxes.max}}, true);
                    });
                    $(p2_selector).unbind('plotselecting').bind('plotselecting', function (e, r) {
                        if (r) $p1.setSelection(r);
                        rescale_yaxis();
                    });
                    $(p2_selector).unbind('plotselected').bind('plotselected', function (e, r) {
                        $(tenor + ' .OG-link').removeClass('OG-link-active');
                    });
                    $(p1_selector).unbind('plothover').bind('plothover', function (e, pos) {
                        if (!panning) hover_pos = pos, setTimeout(update_legend, 50);
                    });
                    $legend = get_legend(), $legend.hide();
                    rescale_yaxis();
                    setTimeout(show_plot);
                };
                calculate_y_values = function () {
                    var cur, // the current data set
                        idx_from, idx_to,// indexes used to slice [cur] to the visible range
                        sliced, // temporary sliced array
                        get_values, // function that takes an array and return the min an max values with a buffer
                        p1_vals, p2_vals,
                        i = data_arr.length,
                        arr_full = [], // the full data set (for p2)
                        arr_sel = []; // the selection data set (for p1)
                    // create 2 arrays, 1 of the VIEWABLE data points of all visible data sets, for p1,
                    // and the other of the FULL data points of all visible data sets for p2
                    while(i--) {
                        if (!data_arr[i].data.length) continue; // account for threshold data
                        cur = data_arr[i].data;
                        arr_full = arr_full.concat(cur);
                        // Find closest min / max index in x range, lazy
                        // This only needs to be done for one series
                        if (!idx_from) data_arr[0].data.map(function (v, i) {
                            if (!idx_from && v[0] >= state.from) i < 2 ? idx_from = 0 : idx_from = i - 1;
                            if (!idx_to && v[0] >= state.to) i > cur.length - 3 ? idx_to = cur.length - 1 : idx_to = i;
                        });
                        // if the number of data points between the indices is less than 10, add more
                        // this doesn't change the viewable data, just the yaxis range
                        while (idx_to - idx_from < Math.min(10, cur.length - 1)) {
                            if (idx_to !== cur.length - 1) ++idx_to;
                            if (idx_from !== 0) --idx_from;
                        }
                        sliced = data_arr[i].data.slice(idx_from, idx_to);
                        arr_sel = arr_sel.concat(sliced);
                    }
                    get_values = function (arr) {
                        var min, max, buffer;
                        max = (function (arr) {return Math.max.apply(null, arr.pluck(1));})(arr);
                        min = (function (arr) {return Math.min.apply(null, arr.pluck(1));})(arr);
                        buffer = (max - min) / 10, max += buffer, min -= buffer;
                        return {min: min, max: max}
                    };
                    p1_vals = get_values(arr_sel), p2_vals = get_values(arr_full);
                    p1_options.yaxis.min = p1_vals.min, p1_options.yaxis.max = p1_vals.max;
                    p2_options.yaxis.min = p2_vals.min, p2_options.yaxis.max = p2_vals.max;
                };
                rescale_yaxis = function () {
                    calculate_y_values();
                    p1_options.xaxis.min = state.from, p1_options.xaxis.max = state.to;
                    $p1.setSelection({
                        xaxis: {from: state.from, to: state.to},
                        yaxis: {from: p1_options.yaxis.min, to: p1_options.yaxis.max}
                    });
                };
                update_legend = function () {
                    var $legends = $(selector + ' .legendLabel'),
                        axes = $p1.getAxes(), j, dataset = $p1.getData(), i = dataset.length, series, y, p1, p2,
                        $date_elm = $legend.find('.og-date'), date, format_date, legend_height,
                        rel_cursor_pos = hover_pos.pageX - ($(selector).offset().left) + 5;
                    format_date = function (date) {
                        return new Date(date).toUTCString().replace(/(^.*:[0-9]{2}\s).*$/, '$1');
                    };
                    $legend = get_legend();
                    if (hover_pos.x < axes.xaxis.min || hover_pos.x > axes.xaxis.max ||
                        hover_pos.y < axes.yaxis.min || hover_pos.y > axes.yaxis.max) {
                        $legend.hide();
                        return;
                    }
                    $legends.each(function () {$(this).css('line-height', 0)});
                    $legend.find('table').css({'top': '17px'});
                    legend_height = $legend.find('table').prev().height() + 15;
                    $legend.css({
                        left: rel_cursor_pos > $(selector).width() - 310 ? rel_cursor_pos - 175 : rel_cursor_pos,
                        visibility: 'visible', display: 'block', 'height': legend_height + 'px'
                    });
                    $legend.find('div, table').css({'left': '5px', 'background': 'none'});
                    while (i--) {
                        if (!dataset[i].data.length) continue;
                        series = dataset[i];
                        // find the closest x points
                        for (j = 0; j < series.data.length; ++j) if (series.data[j][0] > hover_pos.x) break;
                        // now interpolate
                        p1 = series.data[j - 1], p2 = series.data[j];
                        if (p1 == null) y = p2[1], date = format_date(p2[0]); // first
                        else if (p2 == null) y = p1[1], date = format_date(p1[0]); // last
                        else y = p1[1], date = format_date(p1[0]); // all others
                        if ($date_elm.length) $date_elm.html(date);
                        else {
                            $('<div class="og-date"></div>').css({
                                position: 'absolute', top: '0', 'white-space': 'nowrap',
                                padding: '0 0 0 2px', 'font-size': '10px', 'color': '#999'
                            }).prependTo($legend);
                        }
                        if (y) $legends.eq(i).text(y); // otherwise there is no data, so no update
                    }
                };
                var show_plot = function () {
                    $(selector + ' .og-plots').css('visibility', 'visible');
                    $(selector + ' .og-loading').remove();
                };
                if (!config.child) og.common.gadgets.manager.register(timeseries);
                timeseries.resize();
            };
            $.when(api.text({module: 'og.views.gadgets.timeseries.plot_tash'})).then(function (tmpl) {
                plot_template = tmpl;
                if (spoofed_data) return handler(spoofed_data.main);
                api.rest.timeseries.get({dependencies: ['id'], id: config.id, cache_for: 10000}).pipe(handler);
            });
        };
    }
});