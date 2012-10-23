/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.common.events', 'og.analytics.Data', 'og.analytics.CellMenu'],
    obj: function () {
        var module = this, counter = 1, row_height = 21, title_height = 31, set_height = 24, 
            templates = null, default_col_width = 175, scrollbar_size = (function () {
                var html = '<div style="width: 100px; height: 100px; position: absolute; \
                    visibility: hidden; overflow: auto; left: -10000px; z-index: -10000; bottom: 100px" />';
                return 100 - $(html).appendTo('body').append('<div />').find('div').css('height', '200px').width();
            })(), do_not_expand = {DOUBLE: null, PRIMITIVE: null};
        var available = (function () {
            var nodes;
            var all = function (total) {
                for (var result = [], lcv = 0; lcv < total; lcv += 1) result.push(lcv);
                return result;
            };
            var unravel = function (arr, result) {
                var start = arr[0], end = arr[1], children = arr[2], last_end = null,
                    i, j, len = children.length, child, curr_start, curr_end;
                result.push(start);
                if (!nodes[start]) return result;
                for (i = 0; i < len; i += 1) {
                    child = children[i]; curr_start = child[0]; curr_end = child[1];
                    for (j = (last_end || start) + 1; j < curr_start; j += 1) result.push(j);
                    last_end = start = curr_end;
                    unravel(child, result);
                }
                while (++start <= end) result.push(start);
                return result;
            };
            return function (meta) {
                nodes = meta.nodes
                return meta.structure.length ? unravel(meta.structure, []) : all(meta.data_rows);
            };
        })();
        var background = function (sets, width, bg_color) {
            var columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                height = row_height, pixels = [], lcv, fg_color = 'dadcdd', dots = columns
                    .reduce(function (acc, col) {return acc.concat([[bg_color, col.width - 1], [fg_color, 1]]);}, []);
            for (lcv = 0; lcv < height - 1; lcv += 1) Array.prototype.push.apply(pixels, dots);
            pixels.push([fg_color, width]);
            return BMP.rle8(width, height, pixels);
        };
        var col_css = function (id, sets, offset) {
            var partial_width = 0, columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
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
            var grid = this, css = og.api.text({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
                header = og.api.text({module: 'og.analytics.grid.header_tash'}),
                container = og.api.text({module: 'og.analytics.grid.container_tash'}),
                row = og.api.text({module: 'og.analytics.grid.row_tash'}), compile = Handlebars.compile;
            $.when(css, header, container, row).then(function (css, header, container, row) {
                templates = {
                    css: compile(css), header: compile(header), container: compile(container), row: compile(row)
                };
                handler.call(grid);
            });
        };
        var constructor = function (config) {
            var grid = this;
            grid.config = config || {};
            grid.elements = {empty: true, parent: $(config.selector).html('instantiating grid...')};
            grid.events = {
                cellhoverin: [], cellhoverout: [], cellselect: [], mousedown: [],
                rangeselect: [], render: [], scroll: [], select: [], viewchange: []
            };
            grid.formatter = new og.analytics.Formatter(grid);
            grid.id = '#analytics_grid_' + counter++ + '_' + +new Date;
            grid.meta = null;
            grid.source = config.source;
            grid.updated = (function (last, delta) {
                return function (time) {
                    return time ? (last ? ((delta = time - last), (last = time), delta) : ((last = time), 0)) : delta;
                };
            })(null, 0);
            if (templates) init_data.call(grid); else compile_templates.call(grid, init_data);
        };
        var fire = og.common.events.fire;
        var init_data = function () {
            var grid = this, config = grid.config;
            grid.elements.parent.html('<blink>&nbsp;initializing data connection...</blink>');
            grid.dataman = new og.analytics.Data(grid.source).on('meta', init_grid, grid).on('data', render_rows, grid)
                .on('fatal', function (error) {grid.elements.parent.html('fatal error: ' + error);})
                .on('types', function (types) {
                    grid.views = Object.keys(types).filter(function (key) {return types[key];}).map(function (key) {
                        return {
                            value: key.toUpperCase(),
                            selected: config.source.type ? key === config.source.type : key === 'portfolio'
                        };
                    });
                    if (grid.elements.empty) return; else render_header.call(grid);
                });
        };
        var init_elements = function () {
            var grid = this, config = grid.config, elements, cellmenu,
                last_x, last_y, page_x, page_y, last_corner, cell // cached values for mousemove and mouseleave events;
            (elements = grid.elements).style = $('<style type="text/css" />').appendTo('head');
            elements.parent.html(templates.container({id: grid.id.substring(1)}))
                .on('click', '.OG-g-h-set-name a', function (event) {
                    return fire(grid.events.viewchange, $(this).html().toLowerCase()), false;
                })
                .on('mousedown', function (event) {
                    var $target = $(event.target), row;
                    event.preventDefault();
                    if (!$target.is('.node')) return fire(grid.events.mousedown, event), void 0;
                    grid.meta.nodes[row = +$target.attr('data-row')] = !grid.meta.nodes[row];
                    grid.resize().selector.clear();
                    return false; // kill bubbling if it's a node
                })
                .on('mousemove', '.OG-g-sel, .OG-g-cell', (function () {
                    var resolution = 8, counter = 0; // only accept 1/resolution of the mouse moves, we have too many
                    return function (event) {
                        (page_x = event.pageX), (page_y = event.pageY);
                        if (counter++ % resolution) return;
                        if (counter > resolution) counter = 1;
                        if (grid.selector.busy()) return last_x = last_y = last_corner = null;
                        if (page_x === last_x && page_y === last_y) return;
                        var scroll_left = grid.elements.scroll_body.scrollLeft(),
                            scroll_top = grid.elements.scroll_body.scrollTop(),
                            fixed_width = grid.meta.columns.width.fixed,
                            x = page_x - grid.offset.left + (page_x > fixed_width ? scroll_left : 0),
                            y = page_y - grid.offset.top + scroll_top - grid.meta.header_height, corner, corner_cache,
                            rectangle = {top_left: (corner = grid.nearest_cell(x, y)), bottom_right: corner},
                            selection = grid.selector.selection(rectangle);
                        if (!selection || last_corner === (corner_cache = JSON.stringify(corner))) return;
                        if (!(cell = grid.cell(selection))) return;
                        cell.top = corner.top - scroll_top + grid.meta.header_height + grid.offset.top;
                        cell.right = corner.right - (page_x > fixed_width ? scroll_left : 0);
                        last_corner = corner_cache; last_x = page_x; last_y = page_y;
                        fire(grid.events.cellhoverin, cell);
                    };
                })(last_x = null, last_y = null, page_x, page_y, last_corner))
                .on('mouseleave', function (event) {(last_corner = null), fire(grid.events.cellhoverout, cell);});
            elements.parent[0].onselectstart = function () {return false;}; // stop selections in IE
            elements.main = $(grid.id);
            elements.fixed_body = $(grid.id + ' .OG-g-b-fixed');
            elements.scroll_body = $(grid.id + ' .OG-g-b-scroll');
            elements.scroll_head = $(grid.id + ' .OG-g-h-scroll');
            elements.fixed_head = $(grid.id + ' .OG-g-h-fixed');
            (function () {
                var started, pause = 200,
                    jump = function () {viewport.call(grid, function () {grid.dataman.busy(false);}), started = null;};
                elements.fixed_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.fixed_body)) started = 'fixed';
                        if (started !== 'fixed') return clearTimeout(timeout);
                        grid.dataman.busy(true);
                        if (cellmenu) cellmenu.hide();
                        elements.scroll_body.scrollTop(elements.fixed_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    };
                })(null));
                elements.scroll_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.scroll_body)) started = 'scroll';
                        if (started !== 'scroll') return clearTimeout(timeout);
                        grid.dataman.busy(true);
                        if (cellmenu) cellmenu.hide();
                        elements.scroll_head.scrollLeft(elements.scroll_body.scrollLeft());
                        elements.fixed_body.scrollTop(elements.scroll_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    };
                })(null));
            })();
            grid.selector = new og.analytics.Selector(grid).on('select', function (selection) {
                var cell, meta = grid.meta, events;
                if (1 === selection.rows.length && 1 === selection.cols.length && (cell = grid.cell(selection)))
                    fire(grid.events.cellselect, cell, selection);
                else
                    fire(grid.events.rangeselect, selection);
                fire(grid.events.select, selection); // fire for single and multiple selections
            });
            if (config.cellmenu) try {cellmenu = new og.analytics.CellMenu(grid);}
                catch (error) {og.dev.warn(module.name + ': cellmenu failed', error);}
            og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize, context: grid});
            elements.empty = false;
        };
        var init_grid = function (meta) {
            var grid = this, config = grid.config, columns = meta.columns;
            grid.meta = meta;
            meta.row_height = row_height;
            meta.header_height =  (config.source.depgraph ? 0 : set_height) + title_height;
            meta.scrollbar_size = scrollbar_size;
            grid.col_widths();
            columns.headers = [];
            columns.types = [];
            columns.fixed[0].columns
                .forEach(function (col) {columns.headers.push(col.header); columns.types.push(col.type);});
            columns.scroll.forEach(function (set) {
                set.columns.forEach(function (col) {columns.headers.push(col.header); columns.types.push(col.type);});
            });
            unravel_structure.call(grid);
            if (grid.elements.empty) {
                grid.clipboard = new og.analytics.Clipboard(grid);
                init_elements.call(grid);
            }
            grid.resize();
        };
        var render_header = (function () {
            var head_data = function (grid, sets, col_offset, set_offset) {
                var width = grid.meta.columns.width, index = 0, depgraph = grid.config.source.depgraph;
                return {
                    width: col_offset ? width.scroll : width.fixed, padding_right: col_offset ? scrollbar_size : 0,
                    sets: sets.map(function (set, idx) {
                        var columns = set.columns.map(function (col) {
                            return {index: (col_offset || 0) + index++, name: col.header, width: col.width};
                        });
                        return {
                            // only send views in for fixed columns (and if there is a viewchange handler)
                            views: !col_offset && grid.events.viewchange.length ? grid.views : null,
                            name: set.name, index: idx + (set_offset || 0), columns: columns, not_depgraph: !depgraph,
                            width: columns.reduce(function (acc, col) {return acc + col.width;}, 0)
                        };
                    })
                };
            };
            return function () {
                var grid = this, meta = grid.meta, columns = meta.columns, fixed_sets = meta.columns.fixed.length;
                grid.elements.fixed_head.html(templates.header(head_data(grid, columns.fixed)));
                grid.elements.scroll_head
                    .html(templates.header(head_data(grid, columns.scroll, meta.fixed_length, fixed_sets)));
            };
        })();
        var render_rows = (function () {
            var row_data = function (grid, data, fixed) {
                var meta = grid.meta, viewport = meta.viewport, fixed_len = meta.fixed_length, i, j, index, data_row,
                    cols = viewport.cols, rows = viewport.rows, grid_row = meta.available.indexOf(rows[0]), value,
                    types = meta.columns.types, type, total_cols = cols.length, formatter = grid.formatter, col_end,
                    row_len = rows.length, col_len = fixed ? fixed_len : total_cols - fixed_len, column, cells,
                    result = {holder_height: viewport.height + (fixed ? scrollbar_size : 0), rows: []};
                for (i = 0; i < row_len; i += 1) {
                    result.rows.push({top: grid_row++ * row_height, cells: (cells = [])});
                    if (fixed) {j = 0; col_end = col_len;} else {j = fixed_len; col_end = col_len + fixed_len;}
                    for (data_row = rows[i]; j < col_end; j += 1) {
                        index = i * total_cols + j; column = cols[j];
                        value = (formatter[type = types[column]] ? formatter[type](data[index]) : data[index]) || '';
                        cells.push({
                            column: column,
                            value: fixed && !j ? meta.unraveled_cache[meta.unraveled[data_row]] + value : value
                        });
                    }
                }
                return result;
            };
            return function (data) {
                var grid = this;
                if (grid.dataman.busy()) return; else grid.dataman.busy(true); // don't accept more data if rendering
                grid.data = data;
                grid.elements.fixed_body.html(templates.row(row_data(grid, data, true)));
                grid.elements.scroll_body.html(templates.row(row_data(grid, data, false)));
                grid.updated(+new Date);
                grid.dataman.busy(false);
                grid.elements.main.find('.node').each(function (idx, val) {
                    var $node = $(this);
                    $node.addClass(grid.meta.nodes[$node.attr('data-row')] ? 'collapse' : 'expand');
                });
                fire(grid.events.render);
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
        var unravel_structure = (function () {
            var rep_str =  '&nbsp;&nbsp;&nbsp;', rep_memo = {}, cache, counter;
            var all = function (total) {
                cache[''] = 0;
                for (var result = [], lcv = 0; lcv < total; lcv += 1) result.push({prefix: 0});
                return result;
            };
            var rep = function (times, lcv, result) {
                if (times in rep_memo) return rep_memo[times];
                if ((result = '') || (lcv = times)) while (lcv--) result += rep_str;
                return rep_memo[times] = result;
            };
            var unravel = function (arr, result, indent) {
                var start = arr[0], end = arr[1], children = arr[2], prefix, last_end = null, str,
                    i, j, len = children.length, child, curr_start, curr_end;
                prefix = (cache[rep(indent) + '<span data-row="' + start + '" class="node"></span>&nbsp;'] = counter++);
                result.push({prefix: prefix, node: true, length: end - start});
                for (i = 0; i < len; i += 1) {
                    child = children[i]; curr_start = child[0]; curr_end = child[1]; j = (last_end || start) + 1;
                    if (j < curr_start) prefix = (str = rep(indent + 2)) in cache ? cache[str] : cache[str] = counter++;
                    for (; j < curr_start; j += 1) result.push({prefix: prefix});
                    last_end = start = curr_end;
                    unravel(child, result, indent + 1);
                }
                prefix = (str = rep(indent + 2)) in cache ? cache[str] : (cache[str] = counter++);
                while (++start <= end) result.push({prefix: prefix});
                return result;
            };
            return function () {
                var grid = this, meta = grid.meta, unraveled, prefix;
                cache = {}; counter = 0; grid.meta.unraveled_cache = [];
                unraveled = meta.structure.length ? unravel(meta.structure, [], 0) : all(meta.data_rows);
                meta.nodes = unraveled.reduce(function (acc, val, idx) {
                    if (val.node) (acc[idx] = true), (acc.all.push(idx)), (acc.ranges.push(val.length));
                    return acc;
                }, {all: [], ranges: []});
                for (prefix in cache) meta.unraveled_cache[+cache[prefix]] = prefix;
                meta.unraveled = unraveled.pluck('prefix');
            };
        })();
        var viewport = function (handler) {
            var grid = this, meta = grid.meta, viewport = meta.viewport, elements = grid.elements,
                top_position = elements.scroll_body.scrollTop(), left_position = elements.scroll_head.scrollLeft(),
                fixed_len = meta.fixed_length, row_start = Math.floor((top_position / viewport.height) * meta.rows),
                row_len = meta.visible_rows, row_end = Math.min(row_start + row_len, meta.available.length),
                lcv = row_start, scroll_position = left_position + viewport.width, scroll_cols = meta.columns.scroll
                    .reduce(function (acc, set) {return acc.concat(set.columns);}, []);
            viewport.rows = [];
            while (lcv < row_end) viewport.rows.push(meta.available[lcv++]);
            (viewport.cols = []), (lcv = 0);
            while (lcv < fixed_len) viewport.cols.push(lcv++);
            viewport.cols = viewport.cols.concat(scroll_cols.reduce(function (acc, col, idx) {
                if (!('scan' in acc)) return acc;
                if ((acc.scan += col.width) >= left_position) acc.cols.push(idx + fixed_len);
                if (acc.scan > scroll_position) delete acc.scan;
                return acc;
            }, {scan: 0, cols: []}).cols);
            grid.dataman.viewport(viewport);
            return (handler && handler.call(grid)), grid;
        };
        constructor.prototype.alive = function () {
            var grid = this, live = $(grid.id).length;
            if (grid.elements.empty || live) return true; // if elements collection is empty, grid is still loading
            try {grid.dataman.kill();} catch (error) {return false;}
            try {grid.clipboard.dataman.kill();} catch (error) {return false;}
            try {grid.elements.style.remove();} catch (error) {return false;}
        };
        constructor.prototype.cell = function (selection) {
            if (!this.data || 1 !== selection.rows.length || 1 !== selection.cols.length) return null;
            var grid = this, meta = grid.meta, viewport = grid.meta.viewport, rows = viewport.rows,
                cols = viewport.cols, row = selection.rows[0], col = selection.cols[0], col_index = cols.indexOf(col),
                data_index = rows.indexOf(row) * cols.length + col_index, cell = grid.data[data_index];
            return typeof cell === 'undefined' ? null : {
                row: selection.rows[0], col: selection.cols[0], value: cell, type: cell.t || selection.type[0],
                row_name: grid.data[data_index - col_index], col_name: meta.columns.headers[col]
            };
        };
        constructor.prototype.col_widths = function () {
            var grid = this, meta = grid.meta, avg_col_width, fixed_width, scroll_cols = meta.columns.scroll,
                scroll_width, last_set, remainder, parent_width = grid.elements.parent.width();
            meta.fixed_length = meta.columns.fixed[0].columns.length;
            meta.scroll_length = meta.columns.scroll.reduce(function (acc, set) {return acc + set.columns.length;}, 0);
            fixed_width = meta.columns.fixed[0].columns
                .reduce(function (acc, col, idx) {return acc + (col.width = idx ? 150 : 250);}, 0);
            remainder = (scroll_width = parent_width - fixed_width - scrollbar_size) -
                ((avg_col_width = Math.floor(scroll_width / meta.scroll_length)) * meta.scroll_length);
            scroll_cols.forEach(function (set) {
                set.columns.forEach(function (col) {col.width = Math.max(default_col_width, avg_col_width);});
            });
            (last_set = scroll_cols[scroll_cols.length - 1].columns)[last_set.length - 1].width += remainder;
        };
        constructor.prototype.nearest_cell = function (x, y) {
            var grid = this, top, bottom, lcv, scan = grid.meta.columns.scan.all, len = scan.length,
                row_height = grid.meta.row_height, grid_height = grid.meta.viewport.height;
            for (lcv = 0; lcv < len; lcv += 1) if (scan[lcv] > x) break;
            bottom = (Math.floor(Math.max(0, Math.min(y, grid_height - row_height)) / row_height) + 1) * row_height;
            top = bottom - row_height;
            return {top: top, bottom: bottom, left: scan[lcv - 1] || 0, right: scan[lcv]};
        };
        constructor.prototype.off = og.common.events.off;
        constructor.prototype.on = og.common.events.on;
        constructor.prototype.range = function (selection, expanded) {
            var grid = this, viewport = grid.meta.viewport, row_indices,
                col_indices, cols_len = viewport.cols.length, types = [], result = null,
                available = !selection.rows.some(function (row) {return !~viewport.rows.indexOf(row);}) &&
                    !selection.cols.some(function (col) {return !~viewport.cols.indexOf(col);});
            if (!available) return null;
            row_indices = selection.rows.map(function (row) {return viewport.rows.indexOf(row);});
            col_indices = selection.cols.map(function (col) {return viewport.cols.indexOf(col);});
            result = row_indices.map(function (row_idx) {
                return col_indices.map(function (col_idx, idx) {
                    var cell = grid.data[row_idx * cols_len + col_idx];
                    return {value: cell, type: cell.t || selection.type[idx]};
                });
            });
            return !expanded ? result : result.every(function (row) {
                return row.pluck('type').every(function (type) {return type in do_not_expand;});
            }) ? result : null;
        };
        constructor.prototype.resize = function (handler) {
            var grid = this, config = grid.config, meta = grid.meta, columns = meta.columns, id = grid.id, css, sheet,
                width = grid.elements.parent.width(), data_width, height = grid.elements.parent.height(),
                header_height = meta.header_height;
            grid.col_widths();
            columns.width = {
                fixed: columns.fixed.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col) {return acc + col.width;}, 0);
                }, 0),
                scroll: columns.scroll.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col) {return acc + col.width;}, 0);
                }, 0)
            };
            columns.scan = {
                fixed: columns.fixed.reduce(function (acc, set) {
                    return set.columns
                        .reduce(function (acc, col) {return acc.arr.push(acc.val += col.width), acc;}, acc);
                }, {arr: [], val: 0}).arr,
                scroll: columns.scroll.reduce(function (acc, set) {
                    return set.columns
                        .reduce(function (acc, col) {return acc.arr.push(acc.val += col.width), acc;}, acc);
                }, {arr: [], val: 0}).arr
            };
            columns.scan.all = columns.scan.fixed
                .concat(columns.scan.scroll.map(function (val) {return val + columns.width.fixed;}));
            data_width = columns.scan.all[columns.scan.all.length - 1] + scrollbar_size;
            meta.rows = (meta.available = available(grid.meta)).length;
            meta.viewport = {
                scroll_height: height - header_height, height: meta.rows * row_height,
                width: Math.min(width, data_width) - columns.width.fixed
            };
            meta.visible_rows = Math.min(Math.ceil(meta.viewport.scroll_height / row_height), meta.rows);
            css = templates.css({
                id: id, viewport_width: meta.viewport.width,
                fixed_bg: background(columns.fixed, columns.width.fixed, 'ecf5fa'),
                scroll_bg: background(columns.scroll, columns.width.scroll, 'ffffff'),
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed + scrollbar_size,
                scroll_left: columns.width.fixed,
                height: meta.viewport.scroll_height, header_height: header_height, row_height: row_height,
                set_height: config.source.depgraph ? 0 : set_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, meta.fixed_length)),
                sets: set_css(id, columns.fixed).concat(set_css(id, columns.scroll, columns.fixed.length))
            });
            if ((sheet = grid.elements.style[0]).styleSheet) sheet.styleSheet.cssText = css; // IE
            else sheet.appendChild(document.createTextNode(css));
            grid.offset = grid.elements.parent.offset();
            return viewport.call(grid, render_header);
        };
        return constructor;
    }
});