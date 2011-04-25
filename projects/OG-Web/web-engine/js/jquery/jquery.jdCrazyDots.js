(function($) {

	$.jdCrazyDots = function(el, options) {
        
		var base = this,
				rotateInProgress;        	       
		 		base.$el = $(el).css("position", "relative");
		 		base.$jdloading = $("<div id='jdloading'></div>");  
		       
		$.fn.duplicate = function(count, cloneEvents) {
			var tmp = [];
			for ( var i = 0; i < count; i++ ) {
				$.merge( tmp, this.clone( cloneEvents ).get() );
			}
			return this.pushStack( tmp );
		};
		       
		base.init = function() {

			base.options = $.extend({},$.jdCrazyDots.defaultOptions, options);
      
      if (base.options.empty) {      
				base.$el.empty().append(base.$jdloading);
			} else {
				base.$el.append(base.$jdloading);
			}
       		
			$("<div class='dot'></div>")
				.appendTo(base.$jdloading)
				.duplicate(7).appendTo(base.$jdloading);	      	
	      
      base.$dots = base.$el.find('.dot');
      
			base.$dots.eq(0).css({ 'top' : '15%', 'left' : '45%' });                            //10 20
			base.$dots.eq(1).transform({ rotate: 45 }).css({ 'top' : '25%', 'left' : '65%' });  //20 10
			base.$dots.eq(2).transform({ rotate: 90 }).css({ 'top' : '45%', 'left' : '75%' });  //20 10
			base.$dots.eq(3).transform({ rotate: 135 }).css({ 'top' : '65%', 'left' : '65%' }); //10 20
			base.$dots.eq(4).transform({ rotate: 180 }).css({ 'top' : '75%', 'left' : '45%' }); //10 20
			base.$dots.eq(5).transform({ rotate: 225 }).css({ 'top' : '65%', 'left' : '25%' }); //20 10
			base.$dots.eq(6).transform({ rotate: 270 }).css({ 'top' : '45%', 'left' : '15%' }); //20 10
			base.$dots.eq(7).transform({ rotate: 315 }).css({ 'top' : '25%', 'left' : '25%' }); 	
			
		
		if($.browser.msie) {
			base.$dots.eq(0).css({ 'top' : '15%', 'left' : '50%' });
			base.$dots.eq(2).css({ 'top' : '50%', 'left' : '75%' });
			base.$dots.eq(4).css({ 'top' : '75%', 'left' : '50%' });
			base.$dots.eq(6).css({ 'top' : '50%', 'left' : '15%' });
		}

      base.$jdloading
      	.css({
	      	"position": "absolute",
	      	"width": base.options.size,
	          "height": base.options.size,
	          "top": (base.$el.outerHeight() / 5) - (base.options.size / 2),
	          "left": (base.$el.outerWidth() / 2) - (base.options.size / 2) 					                
	      });
            
			base.$dots
				.css({
	      	"width": base.options.dotWidth,
	      	"height": base.options.dotHeight            
	      });
			
			var dotCount = base.$dots.length,
					headDot = 0, 
					neckDot = 0,
					armDot,
					waistDot,
					buttDot,
					tailDot;
			
			base.spin = function() {	
			
				
				base.$dots.eq(headDot).animate({opacity: 1.0}, base.options.speed * 1.5);
					
				headDot = Math.ceil((neckDot + 1) % dotCount);
				
				base.$dots.eq(tailDot).animate(
					{opacity: 0.1}, base.options.speed
				);
					
				base.$dots.eq(buttDot).animate(
					{opacity: 0.2}, base.options.speed
				);
					
				base.$dots.eq(waistDot).animate(
					{opacity: 0.4}, base.options.speed
				);	
					
				base.$dots.eq(armDot).animate(
					{opacity: 0.6}, base.options.speed
				);
								
				base.$dots.eq(neckDot).animate(
					{opacity: 0.8}, base.options.speed
				);
					
				base.$dots.eq(headDot).animate(
					{opacity: 1.0}, base.options.speed * 1.5, function() {
						pause = setTimeout(base.spin, base.options.speed / 2);
					}
				);
					
				tailDot  = buttDot;
				buttDot  = waistDot;
				waistDot = armDot;
				armDot   = neckDot;
				neckDot  = headDot;
				
			};
			
			base.spin(); 
			 
        };

        base.init();
        
    };

    $.jdCrazyDots.defaultOptions = {
        speed: 75,
        size: 75,
        dotWidth: "15%",
        dotHeight: "15%",
        empty: true
    };

    $.fn.jdCrazyDots = function(options) {
        return this.each(function() {
            (new $.jdCrazyDots(this, options));
        });
    };

})(jQuery);