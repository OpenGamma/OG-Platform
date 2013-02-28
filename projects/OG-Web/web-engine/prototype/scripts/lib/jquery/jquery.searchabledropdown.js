/**
 * JQuery Searchable DropDown Plugin
 *
 * @required jQuery 1.3.x or above
 * @author Sascha Woo <xhaggi@users.sourceforge.net>
 * $Id: jquery.searchabledropdown.js 53 2012-11-22 08:48:14Z xhaggi $
 *
 * Copyright (c) 2012 xhaggi
 * https://sourceforge.net/projects/jsearchdropdown/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
(function($) {

	// register plugin
	var plugin = register("searchable");

    // defaults
	plugin.defaults = {
        maxListSize: 100,
        maxMultiMatch: 50,
        exactMatch: false,
        wildcards: true,
        ignoreCase: true,
        warnMultiMatch: "top {0} matches ...",
        warnNoMatch: "no matches ...",
        latency: 200,
        zIndex: "auto"
    };

	/**
	 * Execute function
	 * element-specific code here
	 * @param {Options} settings Settings
	 */
	plugin.execute = function(settings, zindex) {

		var timer = null;
        var searchCache = null;
        var search = null;

		// do not attach on IE6 or lower
		if ($.browser.msie && parseInt(jQuery.browser.version) < 7)
			return this;

    	// only active select elements with drop down capability
        if (this.nodeName != "SELECT" || this.size > 1)
            return this;

        var self = $(this);
        var storage = {index: -1, options: null}; // holds data for restoring
        var idxAttr = "lang";
        var enabled = false;

        // detecting chrome
        $.browser.chrome = /chrome/.test(navigator.userAgent.toLowerCase());
        if($.browser.chrome) $.browser.safari = false;

        // lets you override the options
        // inside the dom objects class property
        // requires the jQuery metadata plugin
        // <div class="hello {color: 'red'}">ddd</div>
        if ($.meta){
            settings = $.extend({}, options, self.data());
        }

        // objects
        var wrapper = $("<div/>");
        var overlay = $("<div/>");
        var input = $("<input/>");
        var selector = $("<select/>");

        // matching option items
        var topMatchItem = $("<option>"+settings.warnMultiMatch.replace(/\{0\}/g, settings.maxMultiMatch)+"</option>").attr("disabled", "true");
        var noMatchItem = $("<option>"+settings.warnNoMatch+"</option>").attr("disabled", "true");


        var selectorHelper = {
    		/**
             * Return DOM options of selector element
             */
			option: function(idx) {
	        	return $(selector.get(0).options[idx]);
	        },
	        /**
	         * Returns the selected item of selector element
	         */
	        selected: function() {
	        	return selector.find(":selected");
	        },
	        /**
	         * Get or Set the selectedIndex of the selector element
	         * @param {int} idx SelectedIndex
	         */
	        selectedIndex: function(idx) {
	        	if(idx > -1)
	        		selector.get(0).selectedIndex = idx;
	        	return selector.get(0).selectedIndex;
	        },
	        /**
	         * Resize selector depends on the parameter size
	         * @param {Number} size Size
	         */
	        size: function(size) {
	        	selector.attr("size", Math.max(2, Math.min(size, 20)));
	        },
	        /**
	         * Reset the entries, which can be choose to it's inital state depends on selectedIndex and maxMultiMatch
	         */
	        reset: function() {
	        	// return if selector has data and stored index equal selectedIndex of source select element
	        	if((self.get(0).selectedIndex-1) == self.data("index"))
	        		return;

	        	// calc start and length of iteration
	        	var idx = self.get(0).selectedIndex;
	        	var len = self.get(0).length;
	            var mc = Math.floor(settings.maxMultiMatch / 2);
                var begin = Math.max(1, (idx - mc));
	            var end = Math.min(len, Math.max(settings.maxMultiMatch, (idx + mc)));
	            var si = idx - begin;

	            // clear selector select element
	            selector.empty();
	            this.size(end-begin);

	            // append options
	            for (var i=begin; i < end; i++)
	            	selector.append($(self.get(0).options[i]).clone().attr(idxAttr, i-1));

	            // append top match item if length exceeds
	            if(end > settings.maxMultiMatch)
	            	selector.append(topMatchItem);

	            // set selectedIndex of selector
	            //selector.get(0).selectedIndex = si;
	        }
        };

        // draw it
        draw();

        /*
         * EVENT HANDLING
         */
        var suspendBlur = false;
        overlay.mouseover(function() {
        	suspendBlur = true;
        });
        overlay.mouseout(function() {
        	suspendBlur = false;
        });
        selector.mouseover(function() {
        	suspendBlur = true;
        });
        selector.mouseout(function() {
        	suspendBlur = false;
        });
        input.click(function(e) {
            selectorHelper.reset();
            //if(!enabled) enable(e, true);
    		//else disable(e, true);
        });
        input.blur(function(e) {
        	if(!suspendBlur && enabled)
        		disable(e, true);
        });
        self.keydown(function(e) {
        	if(e.keyCode != 9 && !e.shiftKey && !e.ctrlKey && !e.altKey)
        		input.click();
        });
        self.click(function(e) {
        	selector.focus();
        });
        selector.click(function(e) {
            if (selectorHelper.selectedIndex() < 0)
            	return;
            disable(e);
        });
        selector.focus(function(e) {
        	input.focus();
        });
        selector.blur(function(e) {
        	if(!suspendBlur)
        		disable(e, true);
        });
        selector.mousemove(function(e) {
            /*console.log($(e.srcElement));
            selectorHelper.selectedIndex($(e.target).get(0).index);*/

            // Disabled on opera because of <select> elements always return scrollTop of 0
            // Affects up to Opera 10 beta 1, can be removed if bug is fixed
            // http://www.greywyvern.com/code/opera/bugs/selectScrollTop
            if($.browser.opera && parseFloat(jQuery.browser.version) >= 9.8)
                return true;

            // get font-size of option
            var fs = Math.floor(parseFloat(/([0-9\.]+)px/.exec(selectorHelper.option(0).css("font-size"))));
            // calc line height depends on browser
            var fsdiff = 4;
            if($.browser.opera)
                fsdiff = 2.5;
            if($.browser.safari || $.browser.chrome)
                fsdiff = 3.5;
            fs += Math.round(fs / fsdiff);
            // set selectedIndex depends on mouse position and line height
            selectorHelper.selectedIndex(Math.floor((e.pageY - selector.offset().top + this.scrollTop) / fs));
        });

        // toggle click event on overlay div
        overlay.click(function(e) {
    		input.click();
        });

        // trigger event keyup
        input.keyup(function(e) {

        	// break searching while using navigation keys
        	if(jQuery.inArray(e.keyCode, new Array(9, 13, 16, 33, 34, 35, 36, 38, 40)) > -1)
        		return true;

        	// set search text
        	search = $.trim(input.val().toLowerCase());

        	// if a previous timer is running, stop it
        	clearSearchTimer();

            // start new timer
            timer = setTimeout(searching, settings.latency);
        });

        // trigger keydown event for keyboard usage
        input.keydown(function(e) {

        	// tab stop
        	/*if(e.keyCode == 9) {
        		disable(e);
        	}*/

        	// return on shift, ctrl, alt key mode
        	/*if(e.shiftKey || e.ctrlKey || e.altKey)
        		return;*/

        	// which key is pressed
            switch(e.keyCode) {
                case 13:  // enter
                	disable(e);
                	self.focus();
                    break;
                case 27: // escape
					//disable(e, true);
					self.focus();
                	break;
                case 33: // pgup
                    if (selectorHelper.selectedIndex() - selector.attr("size") > 0) {
                        selectorHelper.selectedIndex(selectorHelper.selectedIndex() - selector.attr("size"));
                    }
                    else {
					    selectorHelper.selectedIndex(0);
				    }
                    synchronize();
                    break;
                case 34: // pgdown
                    if (selectorHelper.selectedIndex() + selector.attr("size") < selector.get(0).options.length - 1) {
                        selectorHelper.selectedIndex(selectorHelper.selectedIndex() + selector.attr("size"));
                    }
                    else {
					    selectorHelper.selectedIndex(selector.get(0).options.length-1);
				    }
                    synchronize();
                    break;
                case 38: // up
                    if (selectorHelper.selectedIndex() > 0){
                        selectorHelper.selectedIndex(selectorHelper.selectedIndex()-1);
                        synchronize();
                    }
                    break;
                case 40: // down
                    if (selectorHelper.selectedIndex() < selector.get(0).options.length - 1){
                    	selectorHelper.selectedIndex(selectorHelper.selectedIndex()+1);
                        synchronize();
                    }
                    break;
                default:
                    return true;
            }

            // we handled the key.stop
            // doing anything with it!
            return false;
        });

        /**
         * Draw the needed elements
         */
        function draw() {

            // fix some styles
            self.css("text-decoration", "none");
            self.width(self.outerWidth());
            self.height(self.outerHeight());

            // wrapper styles
            wrapper.css("position", "relative");
            // wrapper.css("width", self.outerWidth());
            // relative div needs an z-index (related to IE z-index bug)
            if($.browser.msie)
            	wrapper.css("z-index", zindex);

            // overlay div to block events of source select element
            overlay.css({
                "position": "absolute",
                "top": 0,
                "left": 0,
                "width":  self.outerWidth(),
                "height": self.outerHeight(),
                "background-color": "#FFFFFF",
                "opacity": "0.01",
                "display": "none"
            });

            // overlay text field for searching capability
            input.attr("type", "text");
            // input.hide();
            input.height(self.outerHeight());

            // default styles for text field
            input.css({
                "position": "absolute",
                "top": 0,
                "left": 0,
                "margin": "0px",
                "padding": "0px",
                "outline-style": "none",
                "border-style": "solid",
                "border-bottom-style": "none",
                "border-color": "transparent",
                "background-color": "transparent"
//                "background-color": "red"
            });

            // copy selected styles to text field
    		var sty = new Array();
    		sty.push("border-left-width");
    		sty.push("border-top-width");
    		//sty.push("font-family");
    		sty.push("font-size");
    		sty.push("font-stretch");
    		sty.push("font-variant");
    		sty.push("font-weight");
    		sty.push("color");
    		sty.push("text-align");
    		sty.push("text-indent");
    		sty.push("text-shadow");
    		sty.push("text-transform");
    		sty.push("padding-left");
    		sty.push("padding-top");
    		for(var i=0; i < sty.length;i++)
    			input.css(sty[i], self.css(sty[i]));

    		// adjust search text field
    		// IE7
    		if($.browser.msie && parseInt(jQuery.browser.version) < 8) {
    			input.css("padding", "0px");
    			input.css("padding-left", "3px");
    			input.css("border-left-width", "2px");
    			input.css("border-top-width", "3px");
    		}
    		// chrome
    		else if($.browser.chrome) {
    			input.height(self.innerHeight());
    			input.css("text-transform", "none");
    			input.css("padding-left", parseFloatPx(input.css("padding-left"))+3);
    			input.css("padding-top", 2);
    		}
    		// safari
    		else if($.browser.safari) {
    			input.height(self.innerHeight());
    			input.css("padding-top", 2);
    			input.css("padding-left", 3);
    			input.css("text-transform", "none");
    		}
    		// opera
    		else if($.browser.opera) {
    			input.height(self.innerHeight());
    			var pl = parseFloatPx(self.css("padding-left"));
    			input.css("padding-left", pl == 1 ? pl+1 : pl);
    			input.css("padding-top", 0);
    		}
    		else if($.browser.mozilla) {
    			input.css("padding-top", "0px");
    			input.css("border-top", "0px");
    			input.css("padding-left", parseFloatPx(self.css("padding-left"))+3);
    		}
    		// all other browsers
    		else {
    			input.css("padding-left", parseFloatPx(self.css("padding-left"))+3);
    			input.css("padding-top", parseFloatPx(self.css("padding-top"))+1);
    		}

    		// adjust width of search field
    		var offset = parseFloatPx(self.css("padding-left")) + parseFloatPx(self.css("padding-right")) +
    		parseFloatPx(self.css("border-left-width")) + parseFloatPx(self.css("border-left-width")) + 23;
            input.width(self.outerWidth() - offset);

    		// store css width of source select object then set width
    		// to auto to obtain the maximum width depends on the longest entry.
    		// this is nessesary to set the width of the selector, because min-width
    		// do not work in all browser.
            var w = self.css("width");
            var ow = self.outerWidth();
            self.css("width", "auto");
            ow = ow > self.outerWidth() ? ow : self.outerWidth();
            self.css("width", w);

            // entries selector replacement
            selector.hide();
            selectorHelper.size(self.get(0).length);
            selector.css({
                "position": "absolute",
                "top": self.outerHeight(),
                "left": 0,
                "width": ow,
                "border": "1px solid #333",
                "font-weight": "normal",
                "padding": 0,
                "background-color": self.css("background-color"),
                "text-transform": self.css("text-transform")
            });

            // z-index handling
            var zIndex = /^\d+$/.test(self.css("z-index")) ? self.css("z-index") : 1;
            // if z-index option is defined, use it instead of select element z-index
            if (settings.zIndex && /^\d+$/.test(settings.zIndex))
            	zIndex = settings.zIndex;
            overlay.css("z-index", (zIndex).toString(10));
            input.css("z-index", (zIndex+1).toString(10));
            selector.css("z-index", (zIndex+2).toString(10));

            // append to container
            self.wrap(wrapper);
            self.after(overlay);
            self.after(input);
            self.after(selector);
        };

        /**
         * Enable the search facilities
         *
         * @param {Object} e Event
		 * @param {boolean} s Show selector
         * @param {boolean} v Verbose enabling
         */
        function enable(e, s, v) {

    		// exit event on disabled select element
    		if(self.attr("disabled"))
    			return false;

    		// prepend empty option
      		self.prepend("<option />");

    		// set state to enabled
    		if(typeof v == "undefined")
    			enabled = !enabled;

    		// reset selector
    		selectorHelper.reset();

    		// synchronize select and dropdown replacement
    		synchronize();

    		// store search result
        	store();

        	// show selector
        	if(s)
        		selector.show();

    		// show search field
    		input.show();
            input.focus();
            input.select();

            // select empty option
        	//self.get(0).selectedIndex = 0;

        	if(typeof e != "undefined")
        		e.stopPropagation();
        };

        /**
         * Disable the search facilities
         *
         * @param {Object} e Event
		 * @param {boolean} rs Restore last results
         */
        function disable(e, rs) {

        	// set state to disabled
        	enabled = false;

        	// remove empty option
        	self.find(":first").remove();

            // clear running search timer
        	clearSearchTimer();

			// hide search field and selector
			// input.hide();
        	selector.hide();

			// restore last results
			if(typeof rs != "undefined")
                restore();

			// populate changes
			populate();

            if(typeof e != "undefined")
            	e.stopPropagation();
        };

        /**
         * Clears running search timer
         */
        function clearSearchTimer() {
        	// clear running timer
            if(timer != null)
            	clearTimeout(timer);
        };

        /**
         * Populate changes to select element
         */
        function populate() {
        	// invalid selectedIndex or disabled elements do not be populate
        	if(selectorHelper.selectedIndex() < 0 || selectorHelper.selected().get(0).disabled)
        		return;

        	// store selectedIndex
    		self.get(0).selectedIndex = parseInt(selector.find(":selected").attr(idxAttr));

        	// trigger change event
        	self.change();

        	// store selected index
        	self.data("index", new Number(self.get(0).selectedIndex));
        };

        /**
         * Synchronize selected item on dropdown replacement with source select element
         */
        function synchronize() {
        	if(selectorHelper.selectedIndex() > -1 && !selectorHelper.selected().get(0).disabled)
        		input.val(selector.find(":selected").text());
        	else
        		input.val(self.find(":selected").text());
        };

        /**
         * Stores last results of selector
         */
        function store() {
			storage.index = selectorHelper.selectedIndex();
			storage.options = new Array();
        	for(var i=0;i<selector.get(0).options.length;i++)
        		storage.options.push(selector.get(0).options[i]);
        };

        /**
         * Restores last results of selector previously stored by store function
         */
        function restore() {
            if (storage && storage.options && storage.options.length) {
                selector.empty();
        	   for (var i=0;i<storage.options.length;i++) selector.append(storage.options[i]);

               selectorHelper.selectedIndex(storage.index);
        	   selectorHelper.size(storage.options.length);
            }
        };

        /**
         * Escape regular expression string
         *
         * @param str String
         * @return escaped regexp string
         */
        function escapeRegExp(str) {
        	var specials = ["/", ".", "*", "+", "?", "|", "(", ")", "[", "]", "{", "}", "\\", "^", "$"];
        	var regexp = new RegExp("(\\" + specials.join("|\\") + ")", "g");
        	return str.replace(regexp, "\\$1");
    	};

        /**
         * The actual searching gets done here
         */
        function searching() {
            if (searchCache == search) { // no change ...
                timer = null;
                return;
            }

            var matches = 0;
            searchCache = search;
            selector.hide();
            selector.empty();

            // escape regexp characters
            var regexp = escapeRegExp(search);
            // exact match
            if(settings.exactMatch)
            	regexp = "^" + regexp;
            // wildcard support
            if(settings.wildcards) {
            	regexp = regexp.replace(/\\\*/g, ".*");
            	regexp = regexp.replace(/\\\?/g, ".");
            }
            // ignore case sensitive
            var flags = null;
            if(settings.ignoreCase)
            	flags = "i";

            // RegExp object
            search = new RegExp(regexp, flags);

			// for each item in list
            for(var i=1;i<self.get(0).length && matches < settings.maxMultiMatch;i++){
            	// search
                if(search.length == 0 || search.test(self.get(0).options[i].text)){
                	var opt = $(self.get(0).options[i]).clone().attr(idxAttr, i-1);
                	if(self.data("index") == i)
                		opt.text(self.data("text"));
                    selector.append(opt);
                    matches++;
                }
            }

            // result actions
            if(matches >= 1){
                selectorHelper.selectedIndex(0);
            }
            else if(matches == 0){
                selector.append(noMatchItem);
            }

            // append top match item if matches exceeds maxMultiMatch
            if(matches >= settings.maxMultiMatch){
                selector.append(topMatchItem);
            }

            // resize selector
            selectorHelper.size(matches);
            selector.show();
            timer = null;
        };

        /**
         * Parse a given pixel size value to a float value
         * @param value Pixel size value
         */
        function parseFloatPx(value) {
			try {
				value = parseFloat(value.replace(/[\s]*px/, ""));
				if(!isNaN(value))
					return value;
			}
			catch(e) {}
			return 0;
		};

        return;
    };

    /**
     * Register plugin under given namespace
     *
     * Plugin Pattern informations
     * The function creates the namespace under jQuery
     * and bind the function to execute the plugin code.
     * The plugin code goes to the plugin.execute function.
     * The defaults can setup under plugin.defaults.
     *
     * @param {String} nsp Namespace for the plugin
     * @return {Object} Plugin object
     */
    function register(nsp) {

    	// init plugin namespace
    	var plugin = $[nsp] = {};

    	// bind function to jQuery fn object
    	$.fn[nsp] = function(settings) {
    		// extend default settings
    		settings = $.extend(plugin.defaults, settings);

    		var elmSize = this.size();
            return this.each(function(index) {
            	plugin.execute.call(this, settings, elmSize-index);
            });
        };

        return plugin;
	};

})(jQuery);