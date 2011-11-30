/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
(function ($, undefined) {
    var table_resizers = [], t, scrollbar_width;
    $(window).resize(function () {
        if (t) clearTimeout(t);
        t = setTimeout(function () {
            table_resizers.forEach(function (val) {val();});
        }, 300);
    });
    $.fn.awesometable = function (options) {
        if (!this.is('table')) throw new TypeError('$.awesometable: needs to be called on a table element');
        var self = this, $dup,
            get_scrollbar_width = function () {
                var html = '<div style="width: 100px; height: 100px; position: absolute; \
                    visibility: hidden; overflow: auto; left: -10000px; z-index: -10000; bottom: 100px" />';
                return scrollbar_width || (scrollbar_width = 100 - $(html)
                    .appendTo('body').append('<div />').find('div').css('height', '200px').width());
            };
        if (!self.parent().parent().hasClass('js-awesometable')) { // initialize
            if (self.height() - self.find('thead').height() <= options.height) return;
            self.css('margin-top', '-1px'); // compensate for thead height being 1px
            self.wrap('<div />').parent()
                .css({height: options.height + 'px', overflow: 'auto'})
                .wrap('<div class="js-awesometable"></div>');
            ($dup = self.clone()).find('tbody').remove();
            self.parentsUntil('.js-awesometable').parent().prepend($dup);
            table_resizers.push(function () {$.fn.awesometable.call(self, options)});
        }
        // resize the new header to mimic original
        (function () {
            var last, len = self.find('th').length, $dup = $('.js-awesometable > table th'),
                reset_css = {
                    'height': '0', 'line-height': '0', 'padding': '0', 'margin': '0',
                    'overflow': 'hidden', 'visibility': 'hidden'
                };
            self.find('thead').css(reset_css).find('*').css(reset_css);
            self.find('th').each(function (index, elm) {
                last = index === len - 1 ? get_scrollbar_width() : 0;
                $($dup[index]).width($(elm).width() + last);
            });
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