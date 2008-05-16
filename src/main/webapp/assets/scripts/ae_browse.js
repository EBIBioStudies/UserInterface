//
//  AE Browse Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// runs on page reload after rendering is done
$(document).ready( function() {

    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if ( navigator.userAgent.indexOf('MSIE') != -1 ) {
        document.getElementById('head').allowTransparency = true;
    }

    // step 1: parse query string (if any) and set filters as appropriate

    
    // step 2: attach event handlers on additional filter object
    $("#ae_add_filter_select").change( onAddFilterChange );

    if ($.browser.opera && $.browser.version < 9.5) {
        $(window).resize( onWindowResize );
    } else {
        onWindowResize();
    }

    var keywords = $.query.get("keywords");
    $.get("/microarray-as/ae/servlets/query", {keywords : keywords, stylesheet : "browse-experiments", type : "html"}, function(data){
        $("#ae_results_table_body_inner").removeAttr("class");
        $("#ae_results_tbody").html(data);
        onWindowResize();
    });
});

function
onAddFilterChange( eventObj )
{
    $("#ae_add_filter_select").hide();
    $("#ae_add_filter_select option:selected").removeAttr("selected");
    $("#ae_add_filter_select option:first").attr( "selected", "true" );
    $("#ae_add_filter").before("<div><label for=\"ae_filter_1\">Species</label><select class=ae_filter id=ae_filter_1><option>Select species ...</option></select>");
    $("#ae_filter_1").focus();
    $("#ae_add_filter_select").show();
}

function
onWindowResize( eventObj )
{
    var outerWidth = $("#ae_results_table_body").width();
    var innerWidth = $("#ae_results_table_body table").width();
    var padding = outerWidth - innerWidth;
    if ( padding >= 0 ) {
        $("#ae_results_table_hdr").css( "right", padding + "px" );
    }
}