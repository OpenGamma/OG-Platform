/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
(function($, undefined) {
    var table_resizers = [], t;
    $(window).resize(function () {
        if (t) clearTimeout(t);
        t = setTimeout(function () {
            table_resizers.forEach(function (val) {val()});
        }, 300);
    });
    // TODO: if any cell in the header is longer than all of the cells in that col then the header wont match
    // possibly add a dynamic class with a min-width of the same as the header
    $.fn.awesometable = function(options) {
        if (!this.is('table')) throw new TypeError('$.awesometable: needs to be called on a table element');
        var self = this, cons = arguments.callee, $dup,
            get_scrollbar_width = function () {
            return 100 - $('<div style="width: 100px; height: 100px; position: absolute; overflow: auto" />')
              .appendTo('body').append('<div />').find('div').css('height', '200px').width();
        };
        if (!self.parent().parent().hasClass('og-js-table')) { // initialize
            if (self.height() - self.find('thead').height() <= options.height) return;
            self.css('margin-top', '-1px'); // compensate for thead height being 1px
            self.wrap('<div />').parent()
                .css({height: options.height + 'px', overflow: 'auto'})
                .wrap('<div class="js-awesometable"></div>');
            $dup = self.clone(), $dup.find('tbody').remove();
            self.parentsUntil('.js-awesometable').parent().prepend($dup);
            table_resizers.push(function () {cons.call(self, options)});
        }
        // resize the new header to mimic original
        (function () {
            var last, len = self.find('th').length, $dup = $('.js-awesometable > table th'),
                reset_css = {
                    'height': '0', 'line-height': '0', 'padding': '0', 'margin': '0',
                    'overflow': 'hidden', 'visibility': 'hidden'
                };
            self.find('th').each(function (index, elm) {
                index === len - 1 ? last = get_scrollbar_width() : last = 0;
                $($dup[index]).width($(elm).width() + last);
            });
            self.find('thead').css(reset_css).find('*').css(reset_css);
        }());
        // deal with and css before and after content
        (function () {
            if (table_resizers.length > 1) return;
            var $style = $('<style type="text/css" />').appendTo($('head')),
                css = '.js-awesometable div thead *:before, .js-awesometable div thead *:after {display: none}';
            if ($style[0].styleSheet) $style[0].styleSheet.cssText = css; // IE
            else $style[0].appendChild(document.createTextNode(css));
        }());
        return self;
    };
})(jQuery);