/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, counter = 1, scrollbar_size = 19, header_height = 49, row_height = 19,
            templates = null, win = window, $ = win.$;
        if (win.parent !== win && win.parent.og && win.parent.og.analytics && win.parent.og.analytics.Grid)
            return win.parent.og.analytics.Grid.partial(undefined, win.$); // if already compiled, use that
        var background = function (columns, width) {
            var height = row_height, canvas = $('<canvas height="' + height + '" width="' + width + '" />')[0], context;
            if (!canvas.getContext) return ''; // don't bother with IE8
            (context = canvas.getContext('2d')).fillStyle = '#dadcdd';
            context.fillRect(0, height - 1, width, 1);
            columns
                .reduce(function (acc, col) {return context.fillRect((acc += col.width) - 1, 0, 1, height), acc;}, 0);
            return canvas.toDataURL('image/png');
        };
        var mousedown_delegator = function (event) {};
        var mouseup_delegator = function (event) {};
        var col_css = function (id, columns, offset) {
            var partial_width = 0, total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return columns.map(function (val, idx) {
                var css = {
                    prefix: id, index: idx + (offset || 0),
                    left: partial_width, right: total_width - partial_width - val.width
                };
                return (partial_width += val.width), css;
            });
        };
        var compile_templates = function (handler) {
            var css = og.api.text({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
                header = og.api.text({module: 'og.analytics.grid.header_tash'}),
                container = og.api.text({module: 'og.analytics.grid.container_tash'}),
                row = og.api.text({module: 'og.analytics.grid.row_tash'}), compile = Handlebars.compile;
            $.when(css, header, container, row).then(function (css, header, container, row) {
                templates = {
                    css: compile(css), header: compile(header), container: compile(container), row: compile(row)
                };
                handler();
            });
        };
        var init_data = function (grid, config) {
            grid.alive = function () {return $(grid.id).length ? true : !grid.style.remove();};
            grid.id = '#analytics_grid_' + counter++;
            grid.meta = null;
            grid.resize = set_size.partial(grid, config);
            grid.style = null;
            (grid.dataman = new og.analytics.Data).on('init', init_grid, grid, config);
        };
        var init_grid = function (grid, config, metadata) {
            var columns = metadata.columns, $style,
                scroll_end = set_viewport.partial(grid, function () {grid.dataman.busy(false);});
            grid.meta = metadata;
            set_size(grid, config);
            $(config.selector).html(templates.container({id: grid.id.substring(1)}))
                .on('mousedown', mousedown_delegator).on('mouseup', mouseup_delegator);
            $(grid.id + ' .OG-g-b-scroll').scroll(scroll_observer(grid, null, scroll_end));
            render_header(grid);
            grid.dataman.on('data', render_rows, grid);
            og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize});
        };
        var render_header = (function () {
            var meta, columns, head_data = function (columns, offset) {
                var width = meta.columns.width;
                return {
                    width: offset ? width.scroll : width.fixed, padding_right: offset ? scrollbar_size : 0,
                    columns: columns.map(function (val, idx) {return {index: idx + (offset || 0), name: val.name};})
                };
            };
            return function (grid) {
                (meta = grid.meta), (columns = meta.columns);
                $(grid.id + ' .OG-g-h-fixed').html(templates.header(head_data(columns.fixed)));
                $(grid.id + ' .OG-g-h-scroll').html(templates.header(head_data(columns.scroll, columns.fixed.length)));
            };
        })();
        var render_rows = (function () {
            var meta, row_data = function (data, fixed) {
                var fixed_length = meta.columns.fixed.length;
                return data.reduce(function (acc, row, idx) {
                    var slice = fixed ? row.slice(0, fixed_length) : row.slice(fixed_length);
                    acc.rows.push({
                        top: (idx + meta.viewport.rows[0]) * row_height,
                        cells: slice.reduce(function (acc, val, idx) {
                            return val === null ? acc
                                : acc.concat({column: fixed ? idx : fixed_length + idx, value: val});
                        }, [])
                    });
                    return acc;
                }, {holder_height: meta.viewport.height + (fixed ? scrollbar_size : 0), rows: []});
            };
            return function (grid, data) {
                meta = grid.meta;
                if (grid.dataman.busy()) return;
                $(grid.id + ' .OG-g-b-fixed').html(templates.row(row_data(data, true)));
                $(grid.id + ' .OG-g-b-scroll').html(templates.row(row_data(data, false)));
            };
        })();
        var scroll_observer = function (grid, timeout, update_viewport, $section, $fixed, $head) {
            return function () { // sync scroll instantaneously and set viewport after scroll stops
                grid.dataman.busy(true);
                ($fixed || ($fixed = $(grid.id + ' .OG-g-b-fixed')))
                    .scrollTop(($section || ($section = $(grid.id + ' .OG-g-b-scroll'))).scrollTop());
                ($head || ($head = $(grid.id + ' .OG-g-h-scroll'))).scrollLeft($section.scrollLeft());
                timeout = clearTimeout(timeout) || setTimeout(update_viewport, 200);
            }
        };
        var set_size = function (grid, config) {
            var meta = grid.meta, css, $style, $parent = $(config.selector), width = config.width || $parent.width(),
                height = config.height || $parent.height(), columns = meta.columns, id = grid.id;
            grid.style = $style = grid.style || $('<style type="text/css" />').appendTo($('head'));
            meta.columns.width = {
                fixed: meta.columns.fixed.reduce(function (acc, val) {return acc + val.width;}, 0),
                scroll: meta.columns.scroll.reduce(function (acc, val) {return acc + val.width;}, 0)
            };
            meta.viewport = {height: meta.rows * row_height, width: width - meta.columns.width.fixed};
            meta.visible_rows = Math.ceil((height - header_height) / row_height);
            css = templates.css({
                id: id, viewport_width: meta.viewport.width,
                fixed_bg: background(columns.fixed, meta.columns.width.fixed),
                scroll_bg: background(columns.scroll, meta.columns.width.scroll),
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed,
                height: height - header_height, header_height: header_height, row_height: row_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, columns.fixed.length))
            });
            set_viewport(grid);
            if ($style[0].styleSheet) return $style[0].styleSheet.cssText = css; // IE
            $style[0].appendChild(document.createTextNode(css));
        };
        var set_viewport = function (grid, handler) {
            var top_position = $(grid.id + ' .OG-g-b-fixed').scrollTop(),
                left_position = $(grid.id + ' .OG-g-h-scroll').scrollLeft(),
                row_start, scroll_position = left_position + grid.meta.viewport.width;
            grid.meta.viewport.rows = [
                row_start = Math.floor((top_position / grid.meta.viewport.height) * grid.meta.rows),
                row_start + grid.meta.visible_rows
            ];
            grid.meta.viewport.cols = grid.meta.columns.scroll.reduce(function (acc, val, idx) {
                if (!('scan' in acc)) return acc;
                if ((acc.scan += val.width) >= left_position) acc.cols.push(idx + grid.meta.columns.fixed.length);
                if (acc.scan > scroll_position) delete acc.scan;
                return acc;
            }, {scan: 0, cols: []}).cols;
            grid.dataman.viewport(grid.meta.viewport);
            if (handler) handler();
        };
        return function (config, dollar) {
            if (dollar) $ = dollar;
            return templates ? init_data(this, config) : compile_templates(init_data.partial(this, config));
        };
    }
});