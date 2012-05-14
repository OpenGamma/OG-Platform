/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, counter = 1, scrollbar_size = 19, header_height = 49, row_height = 19, templates = null;
        if (window.parent !== window && window.parent.og.analytics && window.parent.og.analytics.Grid)
            return window.parent.og.analytics.Grid.partial(undefined, $); // if already compiled, use that
        var background = function (columns, width) {
            var height = row_height, canvas = $('<canvas height="' + height + '" width="' + width + '" />')[0], context;
            if (!canvas.getContext) return ''; // don't bother with IE8
            (context = canvas.getContext('2d')).fillStyle = '#dadcdd';
            context.fillRect(0, height - 1, width, 1);
            columns
                .reduce(function (acc, col) {return context.fillRect((acc += col.width) - 1, 0, 1, height), acc;}, 0);
            return canvas.toDataURL('image/png');
        };
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
            grid.alive = function () {return grid.$(grid.id).length ? true : !grid.elements.style.remove();};
            grid.elements = {};
            grid.id = '#analytics_grid_' + counter++;
            grid.meta = null;
            grid.resize = set_size.partial(grid, config);
            (grid.dataman = new og.analytics.Data).on('init', init_grid, grid, config);
        };
        var init_grid = function (grid, config, metadata) {
            var $ = grid.$, columns = metadata.columns;
            grid.meta = metadata;
            grid.row_height = row_height;
            grid.header_height = header_height;
            grid.elements.style = $('<style type="text/css" />').appendTo('head');
            grid.elements.parent = $(config.selector).html(templates.container({id: grid.id.substring(1)}));
            grid.elements.main = $(grid.id);
            grid.elements.fixed_body = $(grid.id + ' .OG-g-b-fixed');
            grid.elements.scroll_body = $(grid.id + ' .OG-g-b-scroll');
            grid.elements.scroll_head = $(grid.id + ' .OG-g-h-scroll');
            grid.elements.fixed_head = $(grid.id + ' .OG-g-h-fixed');
            grid.set_viewport = set_viewport.partial(grid);
            grid.selector = new og.analytics.Selector(grid);
            set_size(grid, config);
            render_header(grid);
            grid.dataman.on('data', render_rows, grid);
            og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize});
        };
        var render_header = (function () {
            var head_data = function (meta, columns, offset) {
                var width = meta.columns.width;
                return {
                    width: offset ? width.scroll : width.fixed, padding_right: offset ? scrollbar_size : 0,
                    columns: columns.map(function (val, idx) {return {index: idx + (offset || 0), name: val.name};})
                };
            };
            return function (grid) {
                var meta = grid.meta, columns = meta.columns;
                grid.elements.fixed_head.html(templates.header(head_data(meta, columns.fixed)));
                grid.elements.scroll_head.html(templates.header(head_data(meta, columns.scroll, columns.fixed.length)));
            };
        })();
        var render_rows = (function () {
            var row_data = function (meta, data, fixed) {
                var fixed_length = meta.columns.fixed.length;
                return data.reduce(function (acc, row, idx) {
                    var slice = fixed ? row.slice(0, fixed_length) : row.slice(fixed_length);
                    acc.rows.push({
                        top: (idx + meta.viewport.rows[0]) * row_height,
                        row: idx,
                        cells: slice.reduce(function (acc, val, idx) {
                            return val === null ? acc
                                : acc.concat({column: fixed ? idx : fixed_length + idx, value: val});
                        }, [])
                    });
                    return acc;
                }, {holder_height: meta.viewport.height + (fixed ? scrollbar_size : 0), rows: []});
            };
            return function (grid, data) {
                if (grid.dataman.busy()) return;
                grid.elements.fixed_body.html(templates.row(row_data(grid.meta, data, true)));
                grid.elements.scroll_body.html(templates.row(row_data(grid.meta, data, false)));
            };
        })();
        var set_size = function (grid, config) {
            var meta = grid.meta, css, width = config.width || grid.elements.parent.width(),
                height = config.height || grid.elements.parent.height(), columns = meta.columns, id = grid.id;
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
            grid.set_viewport();
            if (grid.elements.style[0].styleSheet) return grid.elements.style[0].styleSheet.cssText = css; // IE
            grid.elements.style[0].appendChild(document.createTextNode(css));
        };
        var set_viewport = function (grid, handler) {
            var top_position = grid.elements.scroll_body.scrollTop(),
                left_position = grid.elements.scroll_head.scrollLeft(),
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
            // because the same code is used in multiple frames, each grid holds a reference to its frame's $
            this.$ = dollar || $;
            if (templates) init_data(this, config); else compile_templates(init_data.partial(this, config));
        };
    }
});