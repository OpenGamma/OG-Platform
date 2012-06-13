/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, counter = 1, scrollbar_size = 19, header_height = 49, row_height = 19, templates = null;
        if (window.parent !== window && window.parent.og.analytics && window.parent.og.analytics.Grid)
            return window.parent.og.analytics.Grid.partial(undefined, $); // if already compiled, use that
        var background = function (sets, width, bg_color) {
            var height = row_height, pixels = [], lcv, fg_color = 'dadcdd',
                columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                dots = columns
                    .reduce(function (acc, col) {return acc.concat([[bg_color, col.width - 1], [fg_color, 1]]);}, []);
            for (lcv = 0; lcv < height - 1; lcv += 1) Array.prototype.push.apply(pixels, dots);
            pixels.push([fg_color, width]);
            return BMP.rle8(width, height, pixels);
        };
        var col_css = function (id, sets, offset) {
            var partial_width = 0,
                columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
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
        var fire = function (grid, type) {
            var args = Array.prototype.slice.call(arguments, 2);
            grid.events[type].forEach(function (value) {value.handler.apply(null, value.args.concat(args));});
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
            grid.events = {mousedown: [], scroll: []};
            grid.meta = metadata;
            grid.meta.row_height = row_height;
            grid.meta.header_height = header_height;
            grid.meta.fixed_length = grid.meta.columns.fixed.reduce(function (acc, set) {
                return acc + set.columns.length;
            }, 0);
            grid.meta.scroll_length = grid.meta.columns.fixed.reduce(function (acc, set) {
                return acc + set.columns.length;
            }, 0);
            grid.on = function (type, handler) {
                if (type in grid.events)
                    grid.events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
            };
            grid.elements.style = $('<style type="text/css" />').appendTo('head');
            grid.elements.parent = $(config.selector).html(templates.container({id: grid.id.substring(1)}));
            grid.elements.main = $(grid.id);
            grid.elements.fixed_body = $(grid.id + ' .OG-g-b-fixed');
            grid.elements.scroll_body = $(grid.id + ' .OG-g-b-scroll');
            grid.elements.scroll_head = $(grid.id + ' .OG-g-h-scroll');
            grid.elements.fixed_head = $(grid.id + ' .OG-g-h-fixed');
            grid.elements.parent[0].onselectstart = function () {return false;}; // stop selections in IE
            grid.elements.parent.on('mousedown', function (event) {
                event.preventDefault();
                fire(grid, 'mousedown', event);
            });
            grid.elements.scroll_body.on('scroll', (function (timeout) {
                return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                    grid.dataman.busy(true);
                    grid.elements.scroll_head.scrollLeft(grid.elements.scroll_body.scrollLeft());
                    grid.elements.fixed_body.scrollTop(grid.elements.scroll_body.scrollTop());
                    timeout = clearTimeout(timeout) ||
                        setTimeout(function () {set_viewport(grid, function () {grid.dataman.busy(false);})}, 200);
                }
            })(null));
            grid.selector = new og.analytics.Selector(grid);
            set_size(grid, config);
            render_header(grid);
            grid.dataman.on('data', render_rows, grid);
            og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize});
        };
        var render_header = (function () {
            var head_data = function (meta, sets, col_offset, set_offset) {
                var width = meta.columns.width, index = 0;
                return {
                    width: col_offset ? width.scroll : width.fixed, padding_right: col_offset ? scrollbar_size : 0,
                    sets: sets.map(function (set, idx) {
                        var columns = set.columns.map(function (col) {
                            return {index: (col_offset || 0) + index++, name: col.name, width: col.width};
                        });
                        return {
                            name: set.name,
                            index: idx + (set_offset || 0),
                            width: columns.reduce(function (acc, col) {return acc + col.width;}, 0),
                            columns: columns
                        };
                    })
                };
            };
            return function (grid) {
                var meta = grid.meta, columns = meta.columns, fixed_sets = meta.columns.fixed.length,
                    fixed_html = templates.header(head_data(meta, columns.fixed)),
                    scroll_html = templates.header(head_data(meta, columns.scroll, meta.fixed_length, fixed_sets));
                grid.elements.fixed_head.html(fixed_html);
                grid.elements.scroll_head.html(scroll_html);
            };
        })();
        var render_rows = (function () {
            var row_data = function (meta, data, fixed) {
                var fixed_length = meta.fixed_length;
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
                if (grid.dataman.busy()) return;
                grid.elements.fixed_body.html(templates.row(row_data(grid.meta, data, true)));
                grid.elements.scroll_body.html(templates.row(row_data(grid.meta, data, false)));
                grid.selector.render();
            };
        })();
        var set_css = function (id, sets, offset) {
            var partial_width = 0,
                columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return sets.map(function (set, idx) {
                var set_width = set.columns.reduce(function (acc, val) {return val.width + acc;}, 0), css;
                css = {
                    prefix: id, index: idx + (offset || 0),
                    left: partial_width, right: total_width - partial_width - set_width
                };
                return (partial_width += set_width), css;
            });
        };
        var set_size = function (grid, config) {
            var meta = grid.meta, css, width = config.width || grid.elements.parent.width(),
                height = config.height || grid.elements.parent.height(), columns = meta.columns, id = grid.id;
            meta.columns.width = {
                fixed: meta.columns.fixed.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col) {return acc + col.width;}, 0);
                }, 0),
                scroll: meta.columns.scroll.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col) {return acc + col.width;}, 0);
                }, 0)
            };
            meta.columns.scan = {
                fixed: meta.columns.fixed.reduce(function (acc, set) {
                    return set.columns.reduce(function (acc, col) {
                        return acc.arr.push(acc.val += col.width), acc;
                    }, acc);
                }, {arr: [], val: 0}).arr,
                scroll: meta.columns.scroll.reduce(function (acc, set) {
                    return set.columns.reduce(function (acc, col) {
                        return acc.arr.push(acc.val += col.width), acc;
                    }, acc);
                }, {arr: [], val: 0}).arr
            };
            meta.columns.scan.all = meta.columns.scan.fixed
                .concat(meta.columns.scan.scroll.map(function (val) {return val + meta.columns.width.fixed;}));
            meta.viewport = {height: meta.rows * row_height, width: width - meta.columns.width.fixed};
            meta.visible_rows = Math.ceil((height - header_height) / row_height);
            css = templates.css({
                id: id, viewport_width: meta.viewport.width,
                fixed_bg: background(columns.fixed, meta.columns.width.fixed, 'ecf5fa'),
                scroll_bg: background(columns.scroll, meta.columns.width.scroll, 'ffffff'),
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed,
                height: height - header_height, header_height: header_height, row_height: row_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, meta.fixed_length)),
                sets: set_css(id, columns.fixed).concat(set_css(id, columns.scroll, columns.fixed.length))
            });
            set_viewport(grid);
            if (grid.elements.style[0].styleSheet) return grid.elements.style[0].styleSheet.cssText = css; // IE
            grid.elements.style[0].appendChild(document.createTextNode(css));
        };
        var set_viewport = function (grid, handler) {
            var top_position = grid.elements.scroll_body.scrollTop(),
                left_position = grid.elements.scroll_head.scrollLeft(),
                row_start, scroll_position = left_position + grid.meta.viewport.width,
                scroll_cols = grid.meta.columns.scroll
                    .reduce(function (acc, set) {return acc.concat(set.columns);}, []);
            grid.meta.viewport.rows = [
                row_start = Math.floor((top_position / grid.meta.viewport.height) * grid.meta.rows),
                row_start + grid.meta.visible_rows
            ];
            grid.meta.viewport.cols = scroll_cols.reduce(function (acc, col, idx) {
                if (!('scan' in acc)) return acc;
                if ((acc.scan += col.width) >= left_position) acc.cols.push(idx + grid.meta.fixed_length);
                if (acc.scan > scroll_position) delete acc.scan;
                return acc;
            }, {scan: 0, cols: []}).cols;
            grid.dataman.viewport(grid.meta.viewport);
            if (handler) handler();
        };
        return function (config, dollar) {
            this.$ = dollar || $; // each grid holds a reference to its frame/window's $
            if (templates) init_data(this, config); else compile_templates(init_data.partial(this, config));
        };
    }
});