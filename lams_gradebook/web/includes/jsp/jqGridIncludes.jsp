<%@ taglib uri="tags-fmt" prefix="fmt"%>
<%@ taglib uri="tags-lams" prefix="lams" %>
<%@ taglib uri="tags-core" prefix="c"%>

<%-- 
Include this jsp in your jqGrid page head to get some jqGrid functionality
 --%>
 
<link type="text/css" href="<lams:LAMSURL/>css/free.ui.jqgrid.min.css" rel="stylesheet">
<script type="text/javascript" src="<lams:LAMSURL/>includes/javascript/jquery.js"></script>
<script type="text/javascript" src="<lams:LAMSURL/>includes/javascript/bootstrap.min.js"></script>
<script type="text/javascript" src="<lams:LAMSURL/>includes/javascript/free.jquery.jqgrid.min.js"></script>
<script type="text/javascript" src="<lams:LAMSURL/>includes/javascript/portrait.js"></script>

<script type="text/javascript">
		
	// JQGRID LANGUAGE ENTRIES ---------------------------------------------
	
	// editing entries
	$.jgrid.edit = {
	    addCaption: "Add Record",
	    editCaption: "Edit Record",
	    bSubmit: "Submit",
	    bCancel: "Cancel",
		bClose: "Close",
	    processData: "Processing...",
	    msg: {
	        required:"Field is required",
	        number:"<fmt:message key="gradebook.function.error.enterNumber"/>",
	        minValue:"value must be greater than or equal to ",
	        maxValue:"value must be less than or equal to",
	        email: "is not a valid e-mail",
	        integer: "Please, enter valid integer value",
			date: "Please, enter valid date value"
	    }
	};
	
	// search entries
	$.jgrid.search = {
		    caption: "<fmt:message key="gradebook.function.search.title"/>",
		    Find: "<fmt:message key="label.find"/>",
		    Reset: "<fmt:message key="label.reset"/>",
		    odata : [
		    	"<fmt:message key="gradebook.function.search.equalTo"/>", 
		    	"<fmt:message key="gradebook.function.search.notEqualTo"/>", 
		    	'less', 
		    	'less or equal',
		    	'greater',
		    	'greater or equal', 
		    	"<fmt:message key="gradebook.function.search.startsWith"/>",
		    	"<fmt:message key="gradebook.function.search.endsWith"/>",
		    	"<fmt:message key="gradebook.function.search.contains"/>" 
		    ]
	};
	
	// setcolumns module
	$.jgrid.col = {
	    caption: "<fmt:message key="gradebook.function.window.showColumns"/>",
	    bSubmit: "<fmt:message key="label.ok"/>",
	    bCancel: "<fmt:message key="label.cancel"/>"
	};
	
	// ---------------------------------------------------------------------
	
	
	// Applies tooltips to a jqgrid
	function toolTip(gRowObject, tooltipDiv) {
		var my_tooltip = null; // Div created for tooltip
		if ( tooltipDiv != undefined && tooltipDiv.length > 0 )
			my_tooltip = $('#'+tooltipDiv);
		else
			my_tooltip = $('#tooltip'); 
		gRowObject.css({ 
			cursor: 'pointer' 
        }).mouseover(function(kmouse){ 
               if (checkCell(kmouse)) {
               	showToolTip(my_tooltip, kmouse);
               	//setTimeout(function(){showToolTip(my_tooltip, kmouse);}, 1000);
               }
        }).mousemove(function(kmouse){ 
               if (checkCell(kmouse)) {
               	moveToolTipBox(my_tooltip, kmouse);
               	//setTimeout(function(){moveToolTipBox(my_tooltip, kmouse);}, 1000);
               }
           }).mouseout(function(){ 
               my_tooltip.stop().fadeOut(400); 
       	}).css({cursor:'pointer'}).click(function(e){
               my_tooltip.stop().fadeOut(400); 
       	});
	}
	
	// Check a cell before opening tooltip to make sure empty or invalid cells do not display
	function checkCell(kmouse) {
		var cell = $(kmouse.target).html();
		if (cell != null && cell !="" && cell !="&nbsp;" && cell != "-" && cell.charAt(0) != '<' && cell.indexOf('popover')==-1 ) {
			var parent = $(kmouse.target).parent().html();
			return ( ! ( parent.indexOf("<a") == 0 && parent.indexOf('popover')>-1 ) );
		}
		return false;
	}
	
	// Shows a tootip and applies the cell value
	function showToolTip(my_tooltip, kmouse) {
		
		var cell = $(kmouse.target).html();
		my_tooltip.html(cell);
              my_tooltip.css({ 
              	opacity: 0.75, 
              	display: "none" 
          	}).stop().fadeIn(400);
	}
	
	// Moves the tooltip box so it is not in the way of the mouse
	function moveToolTipBox(my_tooltip, kmouse) {
		var border_top = $(window).scrollTop(); 
        var border_right = $(window).width(); 
        var left_pos; 
        var top_pos; 
        var offset = 20;  
        if (border_right - (offset * 2) >= my_tooltip.width() + kmouse.pageX)  
        { 
        	left_pos = kmouse.pageX + offset;  
        }  
        else  
        { 
       		left_pos = border_right - my_tooltip.width() - offset; 
        }  
        if (border_top + (offset * 2) >= kmouse.pageY - my_tooltip.height())  
        { 
        	top_pos = border_top + offset;  
        }  
        else  
        { 
        	top_pos = kmouse.pageY - my_tooltip.height() - offset; 
        } 
        my_tooltip.css({ 
	        left: left_pos, 
	        top: top_pos 
        }); 
	}
	
	// launches a popup from the page
	function launchPopup(url,title,width,height) {
		var wd = null;
		if(wd && wd.open && !wd.closed){
			wd.close();
		}
		//wd = window.open(url,title,'resizable,width=796,height=570,scrollbars');
		wd = window.open(url,title,'resizable,width='+width+',height='+height+',scrollbars');
		wd.window.focus();
	}
	
	// Removes html tags from a string
	function removeHTMLTags(string) {
	 	var strTagStrippedText = string.replace(/<\/?[^>]+(>|$)/g, "");
	 	return strTagStrippedText;
	}
	
	function trim(str) {
		str = str.replace(/^\s+|\s+$/g, '');
		return str;
	}
		
	function fixPagerInCenter(pagername, numcolshift) {
		$('#'+pagername+'_right').css('display','inline');
		if ( numcolshift > 0 ) {
			var marginshift = - numcolshift * 12;
			$('#'+pagername+'_center table').css('margin-left', marginshift+'px');
		}
	}

	<%--  gradebook dialog windows	on the ipad do not update the grid width properly using setGridWidth. Calling this is 
	-- setting the grid to parentWidth-1 and the width of the parent to parentWidth+1, leading to growing width window
	-- that overflows the dialog window. Keep the main grids slightly smaller than their containers and all is well. 
	--%>
    function resizeJqgrid(jqgrids) {
        jqgrids.each(function(index) {
            var gridId = $(this).attr('id');
            var parent = jQuery('#gbox_' + gridId).parent();
            var gridParentWidth = parent.width();
            if ( parent.hasClass('grid-holder') ) {
                	gridParentWidth = gridParentWidth - 2;
            }
            jQuery('#' + gridId).setGridWidth(gridParentWidth, true);
        });
    };


</script>
