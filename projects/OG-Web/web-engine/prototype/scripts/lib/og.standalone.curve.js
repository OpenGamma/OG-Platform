(function ($, undefined) {
    var css = {}, html = {},
        tooltip_class = 'tooltip' + (Math.random() + '').substring(2),
        color_arr = ['#42669a', '#ff9c00', '#00e13a', '#313b44'], tooltip_font_color = '#fff',
        flot_options = {
            colors: [], zoom: {interactive: true}, selection: {mode: null},
            grid: {borderWidth: 0, color: '#999', minBorderMargin: 0, labelMargin: 4, hoverable: true},
            lines: {lineWidth: 1, fill: false, fillColor: '#f8fbfd'},
            pan: {interactive: true, cursor: "move", frameRate: 30},
            series: {shadowSize: 1, points: {radius: 2, lineWidth: 1, fill: true, fillColor: "#ffffff"}},
            xaxis: {mode: 'years', tickLength: 'full', labelHeight: 14},
            yaxis: {position: 'left', tickLength: 'full', labelWidth: 30}
        };
    // reset image data url
    css.reset_durl = 'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAPklEQVQYV2NkgAL30KT/O1fPY4Tx0Wmw' +
                     'BEgRiMarEKYIl0kwA4g3kSQ34rIW2YM4fYnuSRQ34vMQaSYS40YAH8YmCJNUMdcAAAAASUVORK5CYII=';
    // reset container
    css.reset_container = {
        background: '#fff', padding: '3px 5px', border: '1px solid #ddd',
        boxShadow: '0 3px 5px rgba(0, 0, 0, 0.1)', zIndex: 1, position: 'absolute',
        top: 0, left: 0
    };
    // reset
    css.reset = {
        background: 'url(data:image/png;base64,' + css.reset_durl + ') 0 5px no-repeat', padding: '0 0 0 13px',
        textDecoration: 'underline', cursor: 'pointer'
    };
    // flot container
    css.plot = {background: '#fff'};
    // nodal points tooltip
    css.tooltip = {
        opacity: '0.5', position: 'absolute', background: '#000',
        padding: '2px', display: 'none', border: '1px solid #fff'
    };
    html.tooltip = '<div><table>\
                        <tr><td>X:&nbsp;&nbsp;</td><td>{x}</td></tr><tr><td>Y:&nbsp;&nbsp;</td><td>{y}</td></tr>\
                    </table></div>';
    html.init_msg = '<strong>Zoom</strong>: mouse scroll wheel <br /> <strong>Pan</strong>: left click drag';
    $.fn.ogcurve = function (input) {
        var $selector = $(this), $reset, $init_msg, $plot, $flot, curve = {}, data = {},
            range = {xmin: null, xmax: null, ymin: null, ymax: null};
        css.plot.width = $selector.width();
        css.plot.height = $selector.height();
        /**
         * Format data object and update flot option object
         * @param data {Array} array of objects each containing a curve array and an optional nodes array
         * @param options {Object} flot options object. colors and ranges need updating based on the data
         */
        var formatter = function (data, options) {
            var obj = {options: options, data: []},
                update_range = function (obj) {
                    range = obj.reduce(function (acc, val) {
                        if (acc.xmin === null) acc.xmin = val[0];
                        if (acc.xmax === null) acc.xmax = val[0];
                        if (acc.ymin === null) acc.ymin = val[1];
                        if (acc.ymax === null) acc.ymax = val[1];
                        if (val[0] < acc.xmin) acc.xmin = val[0];
                        if (val[0] > acc.xmax) acc.xmax = val[0];
                        if (val[1] < acc.ymin) acc.ymin = val[1];
                        if (val[1] > acc.ymax) acc.ymax = val[1];
                        return acc;
                    }, range);
                };
            if ($.isArray(data)) data.forEach(function (val, i) {
                if (val.curve) {
                    obj.data.push({data: val.curve});
                    obj.options.colors.push(color_arr[i]);
                    update_range(val.curve);
                }
                if (val.nodes) {
                    obj.data.push({data: val.nodes, points: {show: true}});
                    obj.options.colors.push(color_arr[i]);
                    update_range(val.nodes);
                }
            });
            return obj;
        };
        curve.load = function () {
            var previous_hover = null, sel = '.' + tooltip_class;
            data = formatter(input, flot_options);
            $selector.css({position: 'relative'}).empty().html((function () {
                $reset = $('<div />').css(css.reset_container).html($('<div>Reset</div>').css(css.reset)).hide();
                $init_msg = $('<div />').css(css.reset_container).html(html.init_msg);
                $plot = $('<div />').css(css.plot);
                $flot = $.plot($plot, data.data, data.options);
                return $('<div />').html([$reset[0], $init_msg[0], $plot[0]]);
            }()));
            /**
             * Implement tooltip
             */
            $plot.bind('plothover', function (event, pos, item) {
                if (item && previous_hover != item.dataIndex) {
                    $(sel).remove(), previous_hover = item.dataIndex;
                    css.tooltip.top = item.pageY + 5;
                    css.tooltip.left = item.pageX + 5;
                    $(html.tooltip.replace('{x}', item.datapoint[0]).replace('{y}', item.datapoint[1]))
                        .addClass(tooltip_class).css(css.tooltip)
                        .appendTo('body').show().find('td').css({color: tooltip_font_color});
                }
                if (!item) $(sel).remove(), previous_hover = null;
            });
            /**
             * Add axes, remove init message and add reset button on pan/zoom
             */
            $plot.bind('plotpan plotzoom', function () {
                $flot.getOptions().grid.markings = [
                    {xaxis: {from: 0, to: range.xmax}, yaxis: {from: 0, to: 0}, color: '#bbb'},
                    {xaxis: {from: 0, to: 0}, yaxis: {from: 0, to: range.ymax}, color: '#bbb'}
                ];
                $reset.show(), $init_msg.hide();
                curve.update(input);
            });
            $reset.on('click', function () {curve.reload();});
        };
        /**
         * Resets zooming and panning
         */
        curve.reload = function () {
            $flot = $.plot($plot, data.data, data.options);
            $reset.hide();
        };
        /**
         * Updates data only
         */
        curve.update = function (input) {
            $flot.setData(formatter(input, flot_options).data);
            $flot.draw();
        };
        curve.load();
        return curve;
    }
})(jQuery);