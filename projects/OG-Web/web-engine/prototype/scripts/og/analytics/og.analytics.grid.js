/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, api_text = og.api.text, counter = 1, compile = Handlebars.compile,
            css_tmpl = api_text.partial({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
            header_tmpl = api_text.partial({module: 'og.analytics.grid.header_tash'}),
            container_tmpl = api_text.partial({module: 'og.analytics.grid.container_tash'}),
            row_tmpl = api_text.partial({module: 'og.analytics.grid.row_tash'}),
            scrollbar_size = 19, header_height = 49, row_height = 19, templates = null;
        var col_css = function (id, columns, col_offset) {
            var partial_width = 0, total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return columns.map(function (val, idx) {
                var width = +val.width, css = {
                    prefix: id, selector: 'c' + (idx + (col_offset || 0)),
                    left: partial_width, right: total_width - partial_width - width
                };
                return (partial_width += width), css;
            });
        };
        var compile_templates = function (grid, config) {
            $.when(css_tmpl(), header_tmpl(), container_tmpl(), row_tmpl())
                .then(function (css_tmpl, header_tmpl, container_tmpl, row_tmpl) {
                    templates = {
                        css: compile(css_tmpl), header: compile(header_tmpl),
                        container: compile(container_tmpl), row: compile(row_tmpl)
                    };
                    init_data(grid, config);
            });
        };
        var init_data = function (grid, config) {
            (grid.dataman = new og.analytics.Data).on('init', init_grid, grid, config);
        };
        var init_grid = function (grid, config, metadata) {
            var columns = metadata.columns, $style,
                scroll_end = set_viewport.partial(grid, function () {grid.dataman.busy(false);});
            grid.id = '#analytics_grid_' + counter++;
            grid.meta = metadata;
            set_size(grid, config);
            $(config.selector).html(templates.container({id: grid.id.substring(1)}));
            $(grid.id + ' .OG-g-b-scroll').scroll(scroll_observer(grid, null, scroll_end));
            render_header(grid);
            grid.dataman.on('data', render_rows, grid);
            og.common.gadgets.manager.register({
                alive: function () {return $(grid.id).length ? true : !grid.style.remove();},
                resize: set_size.partial(grid, config)
            });
        };
        var render_header = (function () {
            var meta, head_data = function (columns, offset) {
                var width = meta.columns.width;
                return {
                    width: offset ? width.scroll : width.fixed,
                    padding_right: offset ? scrollbar_size : 0,
                    columns: columns.map(function (val, idx) {return {index: idx + (offset || 0), name: val.name};})
                };
            };
            return function (grid) {
                meta = grid.meta;
                $(grid.id + ' .OG-g-h-fixed').html(templates.header(head_data(meta.columns.fixed)));
                $(grid.id + ' .OG-g-h-scroll')
                    .html(templates.header(head_data(meta.columns.scroll, meta.columns.fixed.length)));
            };
        })();
        var render_rows = (function ($fixed, $scroll) {
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
                ($fixed || ($fixed = $(grid.id + ' .OG-g-b-fixed'))).html(templates.row(row_data(data, true)));
                ($scroll || ($scroll = $(grid.id + ' .OG-g-b-scroll'))).html(templates.row(row_data(data, false)));
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
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed,
                height: height - header_height, header_height: header_height, row_height: row_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, columns.fixed.length))
            });
            set_viewport(grid);
            if ($style[0].styleSheet) return $style[0].styleSheet.cssText = css; // IE
            $style[0].appendChild(document.createTextNode(css));
        };
        var set_viewport = function (grid, handler) {
            var id = grid.id, meta = grid.meta, viewport = meta.viewport, dataman = grid.dataman, row_start,
                top_position = $(id + ' .OG-g-b-fixed').scrollTop(),
                left_position = $(id + ' .OG-g-h-scroll').scrollLeft(),
                scroll_position = left_position + viewport.width;
            viewport.rows = [
                row_start = Math.floor((top_position / viewport.height) * meta.rows),
                row_start + meta.visible_rows
            ];
            viewport.cols = meta.columns.scroll.reduce(function (acc, val, idx) {
                if (!('scan' in acc)) return acc;
                if ((acc.scan += val.width) >= left_position) acc.cols.push(idx + meta.columns.fixed.length);
                if (acc.scan > scroll_position) delete acc.scan;
                return acc;
            }, {scan: 0, cols: []}).cols;
            dataman.viewport(viewport);
            if (handler) handler();
        };
        return function (config) {return templates ? init_data(this, config) : compile_templates(this, config);};
    }
});