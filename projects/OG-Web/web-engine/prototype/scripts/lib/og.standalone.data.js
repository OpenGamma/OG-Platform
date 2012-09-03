(function ($, undefined) {
    var settings = {
        background_color: '#fff',
        max_col_width: 300 // the maximum default width for a column
    };
    $.fn.ogdata = function (input) {
        var $selector = $(this), grid_width = $selector.width() / input.length, util = {}, gadget = {};
        /**
         * @param data {Object} object with data & labels arrays
         * @param num
         */
        gadget.add_grid = function (data, num) {
            if (!data.labels) data.labels = ['Label', 'Value'];
            var ismatrix = ($.isArray(data.labels[0])),
                slick_data, slick_columns = [],
                xlabels = ismatrix ? data.labels[0] : data.labels,
                ylabels = ismatrix ? data.labels[1] : null,
                col_width = util.column_width(data, settings.max_col_width, ismatrix);
            if (ismatrix){
                slick_columns.push({id: 'ylabelscol', name: '', field: 'ylabelscol', width: col_width});
                $selector.addClass('matrix');
            }
            xlabels.forEach(function (val) {
                slick_columns.push({id: val, name: val, field: val, width: col_width});
            });
            slick_data = data.data.slice().reverse().reduce(function (acc, val, i) {
                var obj = {};
                if (ismatrix) obj.ylabelscol = ylabels[i];
                val.forEach(function (val, i) {obj[xlabels[i]] = val;});
                return acc.push(obj) && acc;
            }, []);
            new Slick.Grid($selector.find('.data_' + num), slick_data, slick_columns);
        };
        gadget.load = function () {
            $selector.css('backgroundColor', settings.background_color);
            util.setup_framework();
            input.forEach(function (val, i) {gadget.add_grid(input[i], i);});
        };
        /**
         * Create html columns to house the grids
         */
        util.setup_framework = function () {
            var i = 0,
                width = 'width: ' + grid_width + 'px',
                height = 'height: ' + $selector.height() + 'px',
                css = width + '; float: left; ' + height,
                html = '';
            for (; i < input.length; i++) {
                html += '<div class="data_' + i + '" style="' + css + '"></div>'
            }
            $selector.html(html);
        };
        /**
         * Calculate the default width of columns
         * @param data {Object} object with data & labels arrays
         * @param max {Number} maximum width to return
         * @param matrix {Boolean}
         * @return {Number}
         */
        util.column_width = function (data, max, matrix) {
            var cols = data.data[0].length + (matrix ? 1 : 0),
                potential_width = grid_width / cols,
                scrollbar_width = 18; // TODO: get shared scroll width
            return potential_width > max ? max : potential_width - (scrollbar_width / cols);
        };
        gadget.load();
    }
})(jQuery);