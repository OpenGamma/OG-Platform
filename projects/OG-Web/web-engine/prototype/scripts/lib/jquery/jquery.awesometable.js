/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
(function ($, undefined) {
    var table_resizers = [], t, scrollbar_width = 0;
    /**
     * @param {object} options awesometable configuration object
     * @param {Number} options.height max height of table before awesometable kicks in
     * @param {Function} options.resize resize manager that overwrides window resize (optional)
     */
    $.fn.awesometable = function (options) {
        var $self = this, $dup, resize = function () {
            if (t) clearTimeout(t);
            t = setTimeout(function () {$.each(table_resizers, function (idx, val) {val();});}, 10);
        };
        if (typeof options.resize === 'function') options.resize(resize); // custom resize manager
        else $(window).resize(resize);
        if (!$self.is('table')) throw new TypeError('awesometable: needs to be called on a table element');
        if (!options || !options.height || typeof options.height !== 'number')
            throw new TypeError('awesometable: requires an object with numeric height property');
        scrollbar_width = scrollbar_width || (function () {
            var html = '<div style="width: 100px; height: 100px; position: absolute; \
                visibility: hidden; overflow: auto; left: -10000px; z-index: -10000; bottom: 100px" />';
            return 100 - $(html).appendTo('body').append('<div />').find('div').css('height', '200px').width();
        })();
        if (!$self.parent().parent().hasClass('js-awesometable')) { // initialize
            if ($self.height() - $self.find('thead').height() <= options.height) return $self;
            $self.wrap('<div />').parent().css({height: options.height + 'px', overflow: 'auto'})
                .wrap('<div class="js-awesometable"></div>').css({width: $self.width() + scrollbar_width + 'px'});
            ($dup = $self.clone()).find('tbody').remove();
            $self.parentsUntil('.js-awesometable').parent().prepend($dup);
            table_resizers.push(function () {$.fn.awesometable.call($self, options);});
        } else {
            $self.parentsUntil('.js-awesometable').css({
                width: $self.parentsUntil('.js-awesometable').parent().find(' > table thead').width() + 'px'
            });
        }
        $self.find('thead tr').hide().parent().find('tr:last').show(); // if multiple header rows, use last only
        // cascade header click
        if ($dup) $dup.find('tr:last th').on('click', function () {
            $($self.find('tr:last-child th')[$(this).index()]).click();
        });
        // resize the new header to mimic original
        (function () {
            var len = $self.find('th').length, $dup = $('.js-awesometable > table th'),
                reset_css = {
                    'padding-top': '0', 'padding-bottom': '0', 'margin-top': '0', 'margin-bottom': '0',
                    'height': '0', 'line-height': '0', 'overflow': 'hidden', 'visibility': 'hidden'
                };
            $self.find('thead').css(reset_css).find('*').css(reset_css);
            $self.find('th').each(function (idx, elm) {
                $($dup[idx]).width($(elm).width() + (idx === len - 1 ? scrollbar_width : 0));
            });
        }());
        // deal with and css before and after content
        (function () {
            if (table_resizers.length > 1) return;
            var $style = $('<style type="text/css" />').appendTo($('head')),
                css = '.js-awesometable div thead :before, .js-awesometable div thead :after {display: none}';
            if ($style[0].styleSheet) $style[0].styleSheet.cssText = css; // IE
            else $style[0].appendChild(document.createTextNode(css));
        }());
        $self.css('margin-top', '-' + $self.find('thead').height() * 2 + 'px'); // compensate for thead height
        return $self;
    };
})(jQuery);