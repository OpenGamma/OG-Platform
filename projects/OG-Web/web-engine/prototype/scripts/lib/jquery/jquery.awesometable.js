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
    $.fn.awesometable = function(options) {
        if (!this.is('table')) throw new TypeError('$.table needs to be called on a table element');
        var self = this, cons = arguments.callee, $dup,
            get_scrollbar_width = function () {
            return 100 - $('<div style="width: 100px; height: 100px; position: absolute; overflow: auto" />')
              .appendTo('body').append('<div />').find('div').css('height', '200px').width();
        };
        if (!self.parent().parent().hasClass('og-js-table')) { // initialize
            if (self.height() - self.find('thead').height() <= options.height) return;
            self.wrap('<div />').parent().css({
                height: options.height + 'px', // TODO: if options.height not provided calculate space left
                overflow: 'auto'
            }).wrap('<div class="og-js-table"></div>');
            $dup = self.clone(), $dup.find('tbody').remove();
            self.parentsUntil('.og-js-table').parent().prepend($dup);
            table_resizers.push(function () {cons.call(self, options)});
        }
        (function () { // resize the new header to mimic original
            var last, len = self.find('th').length, $dup = $('.og-js-table > table th');
            self.find('thead').css('visibility', 'hidden').show();
            self.find('th').each(function (index, elm) {
                index === len - 1 ? last = get_scrollbar_width() : last = 0;
                $($dup[index]).width($(elm).width() + last);
            });
            self.find('thead').hide();
        }());
        return self;
    };
})(jQuery);