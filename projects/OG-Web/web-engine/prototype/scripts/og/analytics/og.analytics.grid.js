/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, api_text = og.api.text, counter = 1,
            css_tmpl = api_text.partial({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
            header_tmpl = api_text.partial({module: 'og.analytics.grid.header_tash'}),
            container_tmpl = api_text.partial({module: 'og.analytics.grid.container_tash'}),
            templates = null,
            scrollbar_size = 19;
        return function (config) {
            var selector = config.selector, total_width = config.width, total_height = config.height;
            $.when(css_tmpl(), header_tmpl(), container_tmpl()).then(function (css_tmpl, header_tmpl, container_tmpl) {
                var gen_rows, gen_head, gen_data, gen_css, viewport, header, width, columns,
                    data = new og.analytics.Data, id = '#analytics_grid_' + counter++, col_css;
                if (!templates) templates = {
                    css: Handlebars.compile(css_tmpl),
                    header: Handlebars.compile(header_tmpl),
                    container: Handlebars.compile(container_tmpl)
                };
                gen_data = function (cols) {
                    var matrix = [], i = 60, k, gen_num = function () {
                        return (Math.random() * 1000);
                    };
                    while (i--) {
                      k = cols;
                      matrix.push(function () {
                          var row = [];
                          while (k--) row.push(gen_num());
                          return row;
                      }());
                    }
                    return matrix;
                };
                gen_rows = function (fixed) {
                    var data = gen_data(19), html,
                        col_from = fixed ? 0 : columns.fixed.length,
                        col_to = fixed ? columns.fixed.length : columns.fixed.length + columns.scroll.length,
                        height = data.length * 20;
                    html = data.reduce(function (acc, row, idx) {
                        acc.push('<div class="OG-g-row r'+ idx +'" style="top: ' + (idx * 20) + 'px">');
                        acc.push.apply(acc, row.slice(col_from, col_to).reduce(function (acc, val, idx) {
                            return acc.concat('<div class="OG-g-cell c' + (col_from + idx) + '">' + val + '</div>');
                        }, []));
                        acc.push('</div>\n\n');
                        return acc;
                    }, ['<div class="OG-g-rows" style="height: ' + (height + (fixed ? scrollbar_size : 0)) + 'px">']);
                    html.push('</div>');
                    return html.join('');
                };
                gen_head = function (columns, start_at) {
                    return templates.header({
                        width: start_at ? width.scroll : width.fixed,
                        padding_right: start_at ? scrollbar_size : 0,
                        cols: columns.reduce(function (acc, val, idx) {
                            return acc + '<div class="OG-g-h-col c' + (idx + (start_at || 0)) + '">' +
                                val.name + '</div>';
                        }, '')
                    });
                };
                load_css = function (css) {
                    var $style = $('<style type="text/css" />').appendTo($('head'));
                    if ($style[0].styleSheet) $style[0].styleSheet.cssText = css; // IE
                    else $style[0].appendChild(document.createTextNode(css));
                };
                col_css = function (columns, col_offset) {
                    var partial_width = 0, total_width = columns.reduce(function (acc, val) {
                        return +val.width + acc;
                    }, 0);
                    col_offset = col_offset || 0;
                    return columns.map(function (val, idx) {
                        var width = +val.width, css = {
                            prefix: id,
                            selector: 'c' + (idx + col_offset),
                            left: partial_width, right: total_width - partial_width - width
                        };
                        partial_width += width;
                        return css;
                    })
                };
                data.init(function (result) {
                    columns = result;
                    header = {height: '51'}; // Stub
                    width = (function (columns) { // width of fixed and scrollable column areas
                        return {
                            fixed: columns.fixed.reduce(function (acc, val) {return acc + val.width;}, 0),
                            scroll: columns.scroll.reduce(function (acc, val) {return acc + val.width;}, 0)
                        };
                    })(columns);
                    viewport = {width: total_width - width.fixed, height: total_height};
                    var css_data = {
                        id: id,
                        height: viewport.height,
                        viewport_width: viewport.width,
                        scroll_width: width.scroll,
                        header_height: header.height,
                        fixed_width: width.fixed,
                        columns: col_css(columns.fixed)
                            .concat(col_css(columns.scroll, columns.fixed.length))
                    };
                    load_css(templates.css(css_data));
                    $(selector).html(templates.container({
                        id: id.substring(1), height: viewport.height, width: total_width
                    }));
                    $(id + ' .OG-g-b-scroll').scroll(function (e) { // sync scroll
                        $(id + ' .OG-g-b-fixed').scrollTop(e.target.scrollTop);
                        $(id + ' .OG-g-h-scroll').scrollLeft(e.target.scrollLeft);
                    });
                    $(id + ' .OG-g-b-fixed').html(gen_rows(true));
                    $(id + ' .OG-g-b-scroll').html(gen_rows(false));
                    $(id + ' .OG-g-h-fixed').html(gen_head(columns.fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_head(columns.scroll, columns.fixed.length));
                    var scroll_len = columns.scroll.length;
                    var fixed_num = columns.fixed.length;
                    setInterval(function () {
                      $(id + ' .OG-g-b-scroll').html(gen_rows(false));
                    }, 30000);
                });
            });
        };
    }
});