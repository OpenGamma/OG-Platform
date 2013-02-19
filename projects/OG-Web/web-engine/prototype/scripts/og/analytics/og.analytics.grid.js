/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.common.events', 'og.analytics.Data', 'og.analytics.CellMenu'],
    obj: function () {
        var module = this, row_height = 21, title_height = 31, set_height = 24, logging = 'logLevel',
            templates = null, default_col_width = 175, HTML = 'innerHTML', scrollbar = og.common.util.scrollbar_size,
            do_not_expand = {
                DOUBLE: null, FUNGIBLE_TRADE: null, NODE: null, OTC_TRADE: null, POSITION: null, STRING: null
            };
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
            return function () {
                var grid = this, meta = grid.meta, state = grid.state;
                nodes = state.nodes;
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
            var partial = 0, columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return columns.map(function (val, idx) {
                var css = {
                    prefix: id, index: idx + (offset || 0),
                    left: partial, right: total_width - partial - val.width
                };
                return (partial += val.width), css;
            });
        };
        var col_reorder = (function () {
            var namespace = '.og_analytics_reorder', index, last, first, grid_width, timeout = null, nearest,
                line = 'OG-g-h-line', reorderer = 'OG-g-h-reorderer', offset_left;
            var auto_scroll = function (direction, scroll_left) {
                var increment = 10, interval = 75, scroll_body = grid.elements.scroll_body;
                clearTimeout(timeout);
                if (direction === 'right') scroll_body.scrollLeft(scroll_left + increment);
                else if (direction === 'left') scroll_body.scrollLeft(scroll_left - increment);
                timeout = setTimeout(function () {auto_scroll(direction, scroll_body.scrollLeft());}, interval);
            };
            var clean_up = function (grid) {
                $('.' + reorderer).remove();
                $('.' + line).remove();
                $(document).off(namespace);
                timeout = clearTimeout(timeout), null;
                grid.elements.scroll_body.off(namespace);
                grid.selector.clear();
            };
            var mousemove = function (grid, event) {
                var scroll_left = grid.elements.scroll_body.scrollLeft(),
                    x = event.pageX - grid.offset.left + scroll_left, fixed_width = grid.meta.columns.width.fixed,
                    scan = grid.meta.columns.scan.all, line_left;
                nearest = Math.max(
                    grid.meta.fixed_length - 1,
                    grid.meta.columns.scan.all.reduce(function (acc, val, idx) {return val < x ? idx : acc;}, 0)
                );
                line_left = grid.offset.left - scroll_left + scan[nearest];
                $('.' + reorderer).css({left: event.pageX - offset_left});
                if (line_left <= fixed_width + grid.offset.left) return auto_scroll('left', scroll_left);
                if (line_left >= grid_width) return auto_scroll('right', scroll_left);
                timeout = clearTimeout(timeout), null;
                $('.' + line).show().css({left: line_left});
            };
            var mouseup = function (grid, event) {
                clean_up(grid);
                console.log('nearest', nearest);
            };
            var scroll = function () {$('.' + line).hide();};
            return function (event, $target) {
                var grid = this, $clone;
                clean_up(grid);
                if (event.which === 3 || event.button === 2) return; // ignore right clicks
                index = +$target.attr('data-index');
                first = grid.meta.fixed_length;
                last = grid.meta.columns.widths.length - 1;
                grid_width = grid.elements.parent.width();
                $clone = $target.clone();
                $clone.find('.resize').remove();
                offset_left = event.pageX - grid.offset.left + grid.elements.scroll_body.scrollLeft() -
                    grid.meta.columns.scan.all[index - 1];
                $clone.addClass(reorderer).css({
                    top: grid.offset.top + grid.meta.set_height - 6,
                    left: event.pageX - offset_left,
                    width: grid.meta.columns.widths[index]
                }).appendTo(document.body);
                $('<div class="' + line + '" />').css({
                    top: grid.offset.top + grid.meta.header_height,
                    left: grid.offset.left - grid.elements.scroll_body.scrollLeft() +
                        grid.meta.columns.scan.all[index === last ? index - 1 : index] - 3,
                    height: grid.elements.parent.height() - grid.meta.header_height
                }).appendTo(document.body);
                grid.elements.scroll_body.on('scroll' + namespace, scroll);
                $(document)
                    .on('mousemove' + namespace, mousemove.partial(grid))
                    .on('mouseup' + namespace, mouseup.partial(grid));
            };
        })();
        var col_resize = (function () {
            var namespace = '.og_analytics_resizer', line = 'OG-g-h-line',
                index, start_left, start_width, min_size = 50;
            var clean_up = function (grid) {
                $('.' + line).remove();
                $(document).off(namespace);
                grid.selector.clear();
            };
            var mousemove = function (grid, event) {
                $('.' + line).css({left: Math.max(event.pageX - 3, (start_left - start_width) + min_size)});
            };
            var mouseup = function (grid, event) {
                clean_up(grid);
                grid.state.col_override[index] = Math.max(min_size, start_width + (event.pageX - start_left));
                grid.resize();
            };
            return function (event, $target) {
                var grid = this, offset_left;
                clean_up(grid);
                if (event.which === 3 || event.button === 2) return; // ignore right clicks
                index = +$target.parents('div.OG-g-h-col:first').attr('data-index');
                offset_left = grid.offset.left -
                    (index < grid.meta.fixed_length ? 0 : grid.elements.scroll_body.scrollLeft());
                start_width = grid.meta.columns.widths[index];
                $(document)
                    .on('mousemove' + namespace, mousemove.partial(grid))
                    .on('mouseup' + namespace, mouseup.partial(grid));
                $('<div class="' + line + '" />').css({
                    top: grid.offset.top + grid.meta.set_height,
                    left: start_left = offset_left + grid.meta.columns.scan.all[index] - 3,
                    height: grid.elements.parent.height() - grid.meta.set_height
                }).appendTo(document.body);
            };
        })();
        var compile_templates = function (handler) {
            var grid = this, css = og.api.text({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
                header = og.api.text({module: 'og.analytics.grid.header_tash'}),
                container = og.api.text({module: 'og.analytics.grid.container_tash'}),
                loading = og.api.text({module: 'og.analytics.loading_tash'}),
                row = og.api.text({module: 'og.analytics.grid.row_tash'}), compile = Handlebars.compile;
            $.when(css, header, container, loading, row).then(function (css, header, container, loading, row) {
                templates = {
                    css: compile(css), header: compile(header),
                    container: compile(container), loading: compile(loading), row: compile(row)
                };
                handler.call(grid);
            });
        };
        var Grid = function (config) {
            if (!config) return;
            var grid = this;
            grid.config = config;
            grid.elements = {empty: true, parent: $(config.selector).html('&nbsp;instantiating grid...')};
            grid.formatter = new og.analytics.Formatter(grid);
            grid.id = '#' + og.common.id('grid');
            grid.meta = null;
            grid.state = {col_override: [], nodes: null, structure: null};
            grid.source = config.source;
            grid.updated = (function (last, delta) {
                return function (time) {
                    return time ? (last ? ((delta = time - last), (last = time), delta) : ((last = time), 0)) : delta;
                };
            })(null, 0);
            if (templates) init_data.call(grid); else compile_templates.call(grid, init_data);
        };
        var init_data = function () {
            var grid = this, config = grid.config;
            grid.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            grid.elements.parent.html(templates.loading({text: 'preparing view...'}));
            grid.dataman = new (config.dataman || og.analytics.Data)(grid.source, {bypass: false, label: grid.label})
                .on('meta', init_grid, grid).on('data', render_rows, grid)
                .on('disconnect', function () {
                    if (grid.selector) grid.selector.clear(); // may not have been instantiated yet
                    grid.clipboard.clear();
                })
                .on('fatal', function (error) {
                    grid.kill(), grid.elements.parent.html('&nbsp;fatal error: ' + error), grid.fire('fatal');
                })
                .on('types', function (types) {
                    grid.views = {selected: config.source.type || 'portfolio'};
                    grid.views.rest = Object.keys(types)
                        .filter(function (key) {return types[key] && key !== grid.views.selected;});
                    if (grid.elements.empty) return; else render_header.call(grid);
                });
            grid.clipboard = new og.analytics.Clipboard(grid);
        };
        var init_elements = function () {
            var grid = this, config = grid.config, elements, in_timeout, out_timeout, stall = 15,
                last_x, last_y, page_x, page_y, last_corner, cell; // cached values for hover events
            var hoverin_handler = function (event, silent) {
                (page_x = event.pageX), (page_y = event.pageY);
                if (grid.selector.busy()) return last_x = last_y = last_corner = null;
                if (page_x === last_x && page_y === last_y) return;
                var scroll_left = grid.elements.scroll_body.scrollLeft(),
                    scroll_top = grid.elements.scroll_body.scrollTop(),
                    fixed_width = grid.meta.columns.width.fixed, corner, corner_cache,
                    x = page_x - grid.offset.left + (page_x > fixed_width ? scroll_left : 0),
                    y = page_y - grid.offset.top + scroll_top - grid.meta.header_height,
                    rectangle = {top_left: (corner = grid.nearest_cell(x, y)), bottom_right: corner},
                    selection = grid.selector.selection(rectangle);
                if (!selection || last_corner === (corner_cache = JSON.stringify(corner))) return;
                if (!(cell = grid.cell(selection))) return hoverout_handler(); // cell is undefined
                if (!cell.value.v) return hoverout_handler(); // cell is empty
                cell.top = corner.top - scroll_top + grid.meta.header_height + grid.offset.top;
                cell.right = corner.right - (page_x > fixed_width ? scroll_left : 0);
                last_corner = corner_cache; last_x = page_x; last_y = page_y;
                if (!silent) grid.fire('cellhoverin', cell);
            };
            var hoverout_handler = function () {
                if (!last_x) return; else (last_x = last_y = last_corner = null), grid.fire('cellhoverout', cell);
            };
            (elements = grid.elements).style = $('<style type="text/css" />').appendTo('head');
            elements.parent.unbind().html(templates.container({id: grid.id.substring(1)}))
                .on('click', '.OG-g-h-set-name .og-js-viewchange', function (event) {
                    return grid.fire('viewchange', $(this).html().toLowerCase()), false;
                })
                .on('click', '.OG-g-h-set-name .og-dropdown', function (event) {
                    return $('.OG-g-h-set-name .og-menu').toggle(), false;
                })
                .on('click', '.OG-g-h-set-name .og-sparklines', function (event) {
                    return $(this).toggleClass('og-active').find('span')
                        .html((grid.config.sparklines = !grid.config.sparklines) ? 'ON' : 'OFF'), false;
                })
                .on('mousedown', function (event) {
                    var $target = $(event.target), row;
                    event.preventDefault();
                    if ($target.is('.resize')) return col_resize.call(grid, event, $target), void 0;
                    // if ($target.is('.reorder')) return col_reorder.call(grid, event, $target), void 0;
                    if (!$target.is('.node')) return grid.fire('mousedown', event), void 0;
                    if ($target.is('.loading')) return; else $target.removeClass('expand collapse').addClass('loading');
                    grid.state.nodes[row = +$target.attr('data-row')] = !grid.state.nodes[row];
                    grid.resize().selector.clear();
                    return false; // kill bubbling
                })
                .on('contextmenu', '.OG-g-sel, .OG-g-cell', function (event) { // for cells
                    hoverin_handler(event, true); // silently run hoverin_handler to set cell
                    return grid.fire('contextmenu', event, cell, null);
                })
                .on('contextmenu', '.OG-g-h-cols', function (event) { // for column headers
                    hoverin_handler(event, true); // silently run hoverin_handler to set cell
                    var col = cell.col, columns = grid.meta.columns;
                    return grid.fire('contextmenu', event, null, ['description', 'header', 'type']
                        .reduce(function (acc, key) {return (acc[key] = columns[key + 's'][col]), acc;}, {col: col}));
                })
                .on('mousemove', '.OG-g-sel, .OG-g-cell', function (event) {
                    in_timeout = clearTimeout(in_timeout) || setTimeout(hoverin_handler.partial(event), stall);
                })
                .on('mouseover', '.OG-g-rest, .OG-g-h-cols', function () {
                    out_timeout = clearTimeout(out_timeout) || setTimeout(hoverout_handler, stall);
                })
                .on('mouseover', '.OG-g-sel, .OG-g-cell', function (event) {
                    in_timeout = clearTimeout(in_timeout) || setTimeout(hoverin_handler.partial(event), stall);
                })
                .on('mouseleave', function () {
                    out_timeout = clearTimeout(out_timeout) || setTimeout(hoverout_handler, stall);
                });
            elements.parent[0].onselectstart = function () {return false;}; // stop selections in IE
            elements.main = $(grid.id);
            elements.fixed_body = $(grid.id + ' .OG-g-b-fixed');
            elements.scroll_body = $(grid.id + ' .OG-g-b-scroll');
            elements.scroll_head = $(grid.id + ' .OG-g-h-scroll');
            elements.fixed_head = $(grid.id + ' .OG-g-h-fixed');
            (function () {
                var started, pause = 200, jump = function () {
                    grid.fire('scrollend'), viewport.call(grid, function () {grid.busy(false);}), started = null;
                };
                elements.fixed_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.fixed_body))
                            grid.fire('scrollstart'), started = 'fixed';
                        if (started !== 'fixed') return clearTimeout(timeout);
                        grid.busy(true);
                        elements.scroll_body.scrollTop(elements.fixed_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    };
                })(null));
                elements.scroll_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.scroll_body))
                            grid.fire('scrollstart'), started = 'scroll';
                        if (started !== 'scroll') return clearTimeout(timeout);
                        grid.busy(true);
                        elements.scroll_head.scrollLeft(elements.scroll_body.scrollLeft());
                        elements.fixed_body.scrollTop(elements.scroll_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    };
                })(null));
            })();
            grid.selector = new og.analytics.Selector(grid)
                .on('select', function (selection) {grid.fire('select', selection);})
                .on('deselect', function () {grid.fire('deselect');});
            if (config.cellmenu) try {new og.analytics.CellMenu(grid);}
                catch (error) {og.dev.warn(module.name + ': cellmenu failed', error);}
            if (!config.child) // if this is a child gadget, rely on its parent to register with manager
                og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize, context: grid});
            elements.empty = false;
        };
        var init_grid = function (meta) {
            var grid = this, columns = meta.columns, config = grid.config,
                col_fields = ['description', 'header', 'type'];
            var populate = function (col) {col_fields.forEach(function (key) {columns[key + 's'].push(col[key]);});};
            grid.meta = meta;
            ['show_sets', 'show_views', 'start_expanded']
                .forEach(function (key) {meta[key] = key in config ? config[key] : true;});
            meta.show_save = 'show_save' in config ? config['show_save'] : false;
            meta.viewport = {format: 'CELL'};
            meta.row_height = row_height;
            meta.set_height = meta.show_sets ? set_height : 0;
            meta.header_height =  meta.set_height + title_height;
            meta.scrollbar = scrollbar;
            meta.fixed_length = meta.columns.fixed.length && meta.columns.fixed[0].columns.length;
            meta.scroll_length = meta.columns.scroll.reduce(function (acc, set) {return acc + set.columns.length;}, 0);
            columns.orig_widths = columns.fixed.concat(columns.scroll).reduce(function (acc, set) {
                return acc.concat(set.columns.map(function (col) {return col.width || null;}));
            }, []);
            verify_state.call(grid);
            grid.col_widths();
            col_fields.forEach(function (key) {columns[key + 's'] = [];}); // plural version
            columns.fixed.forEach(function (set) {set.columns.forEach(populate);});
            columns.scroll.forEach(function (set) {set.columns.forEach(populate);});
            meta.row_class = {}; // TODO populate with added, deleted, edited by data row index
            if (grid.elements.empty) init_elements.call(grid);
            grid.resize(!meta.start_expanded);
            render_rows.call(grid, null, true);
        };
        var render_header = (function () {
            var head_data = function (grid, sets, col_offset, set_offset) {
                var meta = grid.meta, width = meta.columns.width, index = 0, show_save = meta.show_save,
                    show_sets = meta.show_sets, show_views = meta.show_views, fixed = !col_offset;
                return {
                    width: col_offset ? width.scroll : width.fixed, padding_right: col_offset ? scrollbar : 0,
                    sets: sets.map(function (set, idx) {
                        var columns = set.columns.map(function (col) {
                            return {
                                index: (col_offset || 0) + index++,
                                name: col.header, description: col.description, width: col.width
                            };
                        });
                        return {
                            columns: columns, index: idx + (set_offset || 0), name: set.name, reorder: !fixed,
                            show_save: show_save, show_sets: show_sets, show_sparklines: grid.config.sparklines,
                            views: fixed && show_views ? grid.views : null,
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
            var row_data = function (grid, data, fixed, loading) {
                var meta = grid.meta, state = grid.state, fixed_len = meta.fixed_length, i, j, index, data_row,
                    inner = meta.inner, prefix, cols = meta.viewport.cols, rows = meta.viewport.rows,
                    grid_row = state.available.indexOf(rows[0]), types = meta.columns.types, type,
                    total_cols = cols.length, formatter = grid.formatter, col_end, row_len = rows.length,
                    col_len = fixed ? fixed_len : total_cols - fixed_len, column, cells, value,
                    widths = meta.columns.widths, row_class = meta.row_class, result = {rows: [], loading: loading};
                if (loading) return result;
                for (i = 0; i < row_len; i += 1) {
                    result.rows.push({
                        top: row_height * grid_row++, cells: (cells = []), row_class: row_class[data_row = rows[i]]
                    });
                    if (fixed) {j = 0; col_end = col_len;} else {j = fixed_len; col_end = col_len + fixed_len;}
                    for (; j < col_end; j += 1) {
                        index = i * total_cols + j; column = cols[j];
                        value = formatter[type = types[column]] ?
                            data[index] && formatter[type](data[index], widths[column], row_height)
                                : data[index] && data[index].v || '';
                        prefix = fixed && j === 0 ? state.unraveled_cache[state.unraveled[data_row]]({
                            state: grid.state.nodes[data_row] ? 'collapse' : 'expand'
                        }) : '';
                        cells.push({
                            column: column, value: prefix + value,
                            logging: data[index] && data[index][logging], error: data[index] && data[index].error
                        });
                    }
                }
                return result;
            };
            return function (data, loading) { // TODO handle scenario where grid was busy when data stopped ticking
                var grid = this, meta = grid.meta;
                if (grid.busy()) return; else grid.busy(true); // don't accept more data if rendering
                grid.data = data;
                grid.elements.fixed_body[0][HTML] = templates.row(row_data(grid, data, true, loading));
                grid.elements.scroll_body
                    .html(grid.formatter.transform(templates.row(row_data(grid, data, false, loading))));
                grid.updated(+new Date);
                if (loading) {
                    if (!grid.elements.notified) grid.elements.main
                        .append(grid.elements.notified = $(templates.loading({text: 'waiting for data...'})));
                } else {
                    if (grid.elements.notified) grid.elements.notified = (grid.elements.notified.remove(), null);
                }
                grid.busy(false);
                grid.fire('render');
            };
        })();
        var set_css = function (id, sets, offset) {
            var partial = 0, columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return sets.map(function (set, idx) {
                var set_width = set.columns.reduce(function (acc, val) {return val.width + acc;}, 0), css;
                css = {
                    prefix: id, index: idx + (offset || 0), left: partial, right: total_width - partial - set_width
                };
                return (partial += set_width), css;
            });
        };
        var unravel_structure = (function () {
            var rep_str =  '&nbsp;&nbsp;&nbsp;', rep_memo = {}, cache, counter;
            var all = function (total) {
                for (var result = [], lcv = 0; lcv < total; lcv += 1) result.push({prefix: 0});
                return (cache[''] = 0), result;
            };
            var rep = function (times, lcv, result) {
                if (times in rep_memo) return rep_memo[times];
                if ((result = '') || (lcv = times)) while (lcv--) result += rep_str;
                return rep_memo[times] = result;
            };
            var unravel = function (arr, result, indent) {
                var start = arr[0], end = arr[1], children = arr[2], expand = !arr[3], prefix, last_end = null, str,
                    i, j, len = children.length, child, curr_start, curr_end, html;
                html = '<span data-row="' + start + '" class="node {{state}}"></span>&nbsp;'
                prefix = cache[rep(indent) + html] = counter++;
                result.push({prefix: prefix, node: true, indent: indent, length: end - start, expand: expand});
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
                var grid = this, meta = grid.meta, state = grid.state, unraveled;
                cache = {}; counter = 0; state.unraveled_cache = [];
                unraveled = meta.structure.length ? unravel(meta.structure, [], 0) : all(meta.data_rows);
                state.nodes = unraveled.reduce(function (acc, val, idx) {
                    if (!val.node) return acc;
                    acc[idx] = val.expand;
                    acc.all.push(idx);
                    if (val.indent > 1) acc.collapse.push(idx);
                    return acc;
                }, {all: [], collapse: []});
                Object.keys(cache)
                    .forEach(function (prefix) {state.unraveled_cache[+cache[prefix]] = Handlebars.compile(prefix);});
                state.unraveled = unraveled.pluck('prefix');
                state.structure = meta.structure;
            };
        })();
        var verify_state = function () {
            var grid = this, meta = grid.meta, state = grid.state;
            if (state.col_override.length !== meta.fixed_length + meta.scroll_length) // if length has changed (in meta)
                state.col_override = new Array(meta.fixed_length + meta.scroll_length);
            if (JSON.stringify(meta.structure) !== JSON.stringify(state.structure)) unravel_structure.call(grid);
        };
        var viewport = function (handler) {
            var grid = this, meta = grid.meta, viewport = meta.viewport, inner = meta.inner, elements = grid.elements,
                top_position = elements.scroll_body.scrollTop(), left_position = elements.scroll_head.scrollLeft(),
                fixed_len = meta.fixed_length, row_start = Math.floor((top_position / inner.height) * meta.rows),
                scroll_position = left_position + inner.width, buffer = viewport_buffer.call(grid), lcv,
                row_end = Math.min(row_start + meta.visible_rows + buffer.row, grid.state.available.length),
                scroll_cols = meta.columns.scroll.reduce(function (acc, set) {return acc.concat(set.columns);}, []);
            lcv = Math.max(0, row_start - buffer.row); viewport.rows = [];
            while (lcv < row_end) viewport.rows.push(grid.state.available[lcv++]);
            (viewport.cols = []), (lcv = 0);
            while (lcv < fixed_len) viewport.cols.push(lcv++);
            viewport.cols = viewport.cols.concat(scroll_cols.reduce(function (acc, col, idx) {
                var lcv;
                if (!('scan' in acc)) return acc;
                if ((acc.scan += col.width) >= left_position) {
                    if (!acc.cols.length && idx) // pad before
                        for (lcv = Math.max(0, idx - buffer.col); lcv < idx; lcv += 1) acc.cols.push(lcv + fixed_len);
                    acc.cols.push(idx + fixed_len);
                }
                if (acc.scan > scroll_position) {
                    for (lcv = idx + 1; lcv < Math.min(idx + buffer.col, scroll_cols.length); lcv += 1)
                        acc.cols.push(lcv + fixed_len);
                    delete acc.scan;
                }
                return acc;
            }, {scan: 0, cols: []}).cols);
            grid.dataman.viewport(viewport);
            return (handler && handler.call(grid)), grid;
        };
        var viewport_buffer = function () {
            var grid = this, meta = grid.meta, sparklines = grid.config.sparklines;
            return {col: sparklines ? 0 : 3, row: sparklines ? 0 : Math.min(meta.visible_rows, 20)};
        };
        Grid.prototype.alive = function () {
            var grid = this;
            return (grid.elements && grid.elements.empty) || // if empty, grid is still loading
                $(grid.id).length || (grid.kill(), false);
        };
        Grid.prototype.cell = function (selection) {
            if (!this.data || 1 !== selection.rows.length || 1 !== selection.cols.length) return null;
            var grid = this, meta = grid.meta, viewport = grid.meta.viewport, rows = viewport.rows,
                cols = viewport.cols, row = selection.rows[0], col = selection.cols[0], col_index = cols.indexOf(col),
                data_index = rows.indexOf(row) * cols.length + col_index, cell = grid.data[data_index];
            return typeof cell === 'undefined' ? null : {
                row: selection.rows[0], col: selection.cols[0], value: cell, type: cell.t || selection.type[0],
                row_name: grid.data[data_index - col_index].v.name, col_name: meta.columns.headers[col],
                row_value: grid.data[data_index - col_index].v
            };
        };
        Grid.prototype.col_widths = function () {
            var grid = this, state = grid.state, meta = grid.meta, avg_col_width, fixed_width,
                fixed_cols = meta.columns.fixed, scroll_cols = meta.columns.scroll, scroll_data_width,
                def_scrolls, scroll_width, last_set, remainder, parent_width = grid.elements.parent.width();
            (function (idx) {
                fixed_cols.concat(scroll_cols).forEach(function (set) {
                    set.columns.forEach(function (col) {
                        col.width = state.col_override[idx] || meta.columns.orig_widths[idx];
                        idx += 1;
                    });
                });
            })(0);
            def_scrolls = scroll_cols.reduce(function (top, set) {
                top.length += set.columns.reduce(function (acc, col) {
                    top.width += +col.width;
                    return col.width ? acc + 1 : acc;
                }, 0);
                return top;
            }, {length: 0, width: 0});
            meta.columns.widths = [];
            fixed_width = meta.columns.fixed.length && meta.columns.fixed[0].columns.reduce(function (acc, col, idx) {
                meta.columns.widths.push(col.width = col.width || (idx ? 150 : 250));
                return acc + col.width;
            }, 0);
            scroll_width = parent_width - fixed_width - scrollbar;
            avg_col_width = Math.floor((scroll_width - def_scrolls.width) / (meta.scroll_length - def_scrolls.length));
            scroll_cols.forEach(function (set) {
                set.columns.forEach(function (col) {
                    meta.columns.widths.push(col.width = col.width || Math.max(default_col_width, avg_col_width));
                });
            });
            scroll_data_width = scroll_cols.pluck('columns').reduce(function (acc, set) {return acc + set.pluck('width')
                .reduce(function (acc, val) {return acc + val;});}, 0);
            if ((remainder = scroll_width - scroll_data_width) <= 0) return;
            meta.columns.widths[meta.columns.widths.length - 1] += remainder;
            (last_set = scroll_cols[scroll_cols.length - 1].columns)[last_set.length - 1].width += remainder;
        };
        Grid.prototype.fire = og.common.events.fire;
        Grid.prototype.kill = function () {
            var grid = this;
            try {grid.dataman.kill();} catch (error) {}
            try {grid.elements.style.remove();} catch (error) {}
        };
        Grid.prototype.label = 'grid';
        Grid.prototype.nearest_cell = function (x, y) {
            var grid = this, top, bottom, lcv, scan = grid.meta.columns.scan.all, len = scan.length,
                row_height = grid.meta.row_height, grid_height = grid.meta.inner.height;
            for (lcv = 0; lcv < len; lcv += 1) if (scan[lcv] > x) break;
            bottom = (Math.floor(Math.max(0, Math.min(y, grid_height - row_height)) / row_height) + 1) * row_height;
            top = bottom - row_height;
            return {top: top, bottom: bottom, left: scan[lcv - 1] || 0, right: scan[lcv]};
        };
        Grid.prototype.off = og.common.events.off;
        Grid.prototype.on = og.common.events.on;
        Grid.prototype.range = function (selection, expanded) {
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
        Grid.prototype.resize = function (collapse) {
            var grid = this, config = grid.config, meta = grid.meta, state = grid.state, columns = meta.columns,
                id = grid.id, css, sheet, width = grid.elements.parent.width(), data_width,
                height = grid.elements.parent.height(), header_height = meta.header_height;
            grid.col_widths();
            columns.width = {
                fixed: columns.fixed.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col, idx) {return acc + col.width;}, 0);
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
            data_width = columns.scan.all[columns.scan.all.length - 1] + scrollbar;
            if (collapse) state.nodes.collapse.forEach(function (node) {state.nodes[node] = false;});
            meta.rows = (state.available = available.call(grid)).length;
            meta.inner = {
                scroll_height: height - header_height, height: meta.rows * row_height,
                width: Math.min(width, data_width) - columns.width.fixed
            };
            meta.visible_rows = Math.min(Math.ceil(meta.inner.scroll_height / row_height), meta.rows);
            css = templates.css({
                id: id, viewport_width: meta.inner.width, rest_top: meta.inner.height,
                fixed_bg: background(columns.fixed, columns.width.fixed, 'ecf5fa'),
                scroll_bg: background(columns.scroll, columns.width.scroll, 'ffffff'),
                scroll_height: Math.max(meta.inner.height, meta.inner.scroll_height - scrollbar),
                fixed_height: Math.max(meta.inner.height + scrollbar, meta.inner.scroll_height),
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed + scrollbar,
                scroll_left: columns.width.fixed, set_height: meta.set_height,
                height: meta.inner.scroll_height, header_height: header_height, row_height: row_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, meta.fixed_length)),
                sets: set_css(id, columns.fixed).concat(set_css(id, columns.scroll, columns.fixed.length))
            });
            grid.elements.style.empty();
            if ((sheet = grid.elements.style[0]).styleSheet) sheet.styleSheet.cssText = css; // IE
            else sheet.appendChild(document.createTextNode(css));
            grid.offset = grid.elements.parent.offset();
            return viewport.call(grid, render_header);
        };
        Grid.prototype.toggle = function (bool) {
            var grid = this, state = typeof bool !== 'undefined' ? !bool : !grid.busy();
            return grid.busy(state);
        };
        return Grid;
    }
});