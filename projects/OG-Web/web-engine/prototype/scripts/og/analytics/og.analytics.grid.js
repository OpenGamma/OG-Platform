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
            var selector = config.selector;
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
                      k = 17;
                      matrix.push(function () {
                          var row = [];
                          while(k--) row.push(gen_num());
                          return row;
                      }());
                    }
                    return matrix;
                };
                gen_rows = function (config) {
                    var fixed = config.fixed,
                        data = gen_data(17), html, len = data.length * 20;
                    html = data.reduce(function (acc, row, idx) {
                        var value = data[idx], lcv = fixed ? 0 : columns.columns_fixed.length,
                            len = fixed ? columns.columns_fixed.length : value.length;
                        acc.push('<div class="OG-g-row r'+ idx +'" style="top: ' + (idx * 20) + 'px">');
                        for (; lcv < len; lcv += 1)
                            acc.push('<div class="OG-g-cell c' + lcv + '">' + value[lcv] + '</div>');
                        acc.push('</div>\n\n');
                        return acc;
                    }, ['<div class="OG-g-rows" style="height: ' + (len + scrollbar_size) + 'px">']);
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
                col_css = function (columns, cols_preceding) {
                    var partial_width = 0, total_width = columns.reduce(function (acc, val) {
                        return +val.width + acc;
                    }, 0);
                    cols_preceding = cols_preceding || 0;
                    return columns.map(function (val, idx) {
                        var width = +val.width, css = {
                            selector: 'c' + (idx + cols_preceding),
                            left: partial_width, right: total_width - partial_width - width
                        };
                        partial_width += width;
                        return css;
                    })
                };
                data.init(function (result) {
                    columns = result;
                    viewport = {width: '800', height: '600'}; // Stub
                    header = {height: '51'}; // Stub
                    width = (function (columns) { // width of fixed and scrollable column areas
                        return {
                            fixed: columns.columns_fixed.reduce(function (acc, val) {return acc + val.width;}, 0),
                            scroll: columns.columns_scroll.reduce(function (acc, val) {return acc + val.width;}, 0)
                        };
                    })(columns);
                    load_css(templates.css({
                        id: id,
                        height: viewport.height,
                        width: viewport.width,
                        header_height: header.height,
                        fixed_width: width.fixed,
                        scroll_width: width.scroll,
                        columns: col_css(columns.columns_fixed)
                            .concat(col_css(columns.columns_scroll, columns.columns_fixed.length))
                    }));
                    $(selector).html(templates.container({id: id.substring(1)}));
                    $(id + ' .OG-g-b-scroll').scroll(function (e) { // sync scroll
                        $(id + ' .OG-g-b-fixed').scrollTop(e.target.scrollTop);
                        $(id + ' .OG-g-h-scroll').scrollLeft(e.target.scrollLeft);
                    });
                    $(id + ' .OG-g-b-fixed').html(gen_rows({fixed: true}));
                    $(id + ' .OG-g-b-scroll').html(gen_rows(columns.columns_scroll.length, columns.columns_fixed.length));
                    $(id + ' .OG-g-h-fixed').html(gen_head(columns.columns_fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_head(columns.columns_scroll, columns.columns_fixed.length));
                    var scroll_len = columns.columns_scroll.length;
                    var fixed_num = columns.columns_fixed.length;
                    setInterval(function () {
                      $(id + ' .OG-g-b-scroll').html(gen_rows({}));
                    }, 1000);
                });
            });
        };
    }
});