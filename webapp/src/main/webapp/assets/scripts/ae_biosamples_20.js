/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

(function($, undefined) {
	if ($ == undefined)
		throw "jQuery not loaded";

	var sortDefault = {
		id : "ascending",
		description : "ascending",
		samples : "ascending"
	};

	var sortTitle = {
		id : "id",
		description : "description",
		samples : "samples"
	};

	$(function() {
		// this will be executed when DOM is ready
		if ($.query == undefined)
			throw "jQuery.query not loaded";

		var sortby = $.query.get("sortby") || "id";
		var sortorder = $.query.get("sortorder") || "ascending";

		var localPath = /(\/.+)$/.exec(decodeURI(window.location.pathname))[1];

		$("th.sortable")
				.each(
						function() {
							var thisObj = $(this);
							var colname = /col_(\w+)/.exec(thisObj
									.attr("class"))[1];

							// so the idea is to set default sorting for all
							// columns except the "current" one
							// (which will be inverted) against its current
							// state
							var newOrder = (colname === sortby) ? ("ascending" === sortorder ? "descending"
									: "ascending")
									: sortDefault[colname];
							var queryString = $.query.set("sortby", colname)
									.set("sortorder", newOrder).toString();

							thisObj.wrapInner("<a href=\"" + localPath
									+ queryString
									+ "\" title=\"Click to sort by "
									+ sortTitle[colname] + "\"/>");
						});

		$("#ae_results_pager").aePager();
		
		
		
		  // added autocompletion
	    var basePath = decodeURI(window.location.pathname).replace(/\/\w+\.\w+$/,"/");
	    alert(basePath);
	    $("#ae_keywords_field").autocomplete(
	            + basePath + "keywords.txt"
	            , { matchContains: false
	                , selectFirst: false
	                , scroll: true
	                , max: 50
	                , requestTreeUrl: basePath + "efotree.txt"
	            }
	        );
	});

})(window.jQuery);

//if I;m showing the detail I will get all the samples using paging
$(function() {
	
	 var url = String(window.location);
//	 alert(url);
     if ( -1 == url.indexOf("browse") ) {
    	 var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
    	 var urlPage="../biosamplessample/" + pageName;
    	 updateSamplesList(urlPage);
     }
});



var page=1;
var sortBy="";
var sortOrder="";
	
	
function updateSamplesList(urlPage) {
	$.get(urlPage, function(data) {
//		  alert('Load was performed.' + data);
		  $("#ae_samples_list").html(data);		

		  
		  	var total = $("#ae_results_total").text();
		    var from = $("#ae_results_from").text();
		    var to = $("#ae_results_to").text();
		    var curpage = $("#ae_results_page").text();
		    var pagesize = $("#ae_results_pagesize").text();


		    if ( total > 0 ) {


		        var queryString = $.query.toString();

		        var totalPages = total > 0 ? Math.floor( ( total - 1 ) / pagesize ) + 1 : 0;
		        
		        var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];

		        if ( totalPages > 1 ) {

		            var pagerHtml = "Pages: ";
		            for ( var page = 1; page <= totalPages; page++ ) {
		                if ( curpage == page ) {
		                    pagerHtml = pagerHtml + "<span class=\"pager_current\">" + page + "</span>";
		                } else if ( 2 == page && curpage > 6 && totalPages > 11 ) {
		                    pagerHtml = pagerHtml + "..";
		                } else if ( totalPages - 1 == page && totalPages - curpage > 5 && totalPages > 11 ) {
		                    pagerHtml = pagerHtml + "..";
		                } else if ( 1 == page || ( curpage < 7 && page < 11 ) || ( Math.abs( page - curpage ) < 5 ) || ( totalPages - curpage < 6 && totalPages - page < 10 ) || totalPages == page || totalPages <= 11 ) {
		                    //var newQuery = $.query.set( "page", page ).set( "pagesize", pagesize ).toString();
		                    var urlPage="../biosamplessample/" + pageName + "?page=" + page ;
		                    pagerHtml = pagerHtml + "<a href=\"javascript:goToPage(" + page + ")\">" + page + "</a>";
		                    //pagerHtml = pagerHtml + "<a href=\"" +  urlPage + "\">" + page + "</a>";
		                }
		            }
//		            alert('pagerHtml->' + pagerHtml);
		            $("#ae_results_pager_samples").html( pagerHtml );
		        }
		    }
		  
		})
	  .success(function() { alert("second success"); })
	  .error(function() { alert("error"); })
	  .complete(function() { alert("complete"); });
	
}

function
sort( pSortBy, pSortOrder )
{	
	sortBy=pSortBy;
	sortOrder=pSortOrder;
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	pageName="../biosamplessample/" + pageName + "?page=" + page + "&sortby=" + sortBy + "&sortorder=" + sortOrder;
	updateSamplesList(pageName);
}


function
goToPage(pPage)
{	
	page=pPage;
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	pageName="../biosamplessample/" + pageName + "?page=" + page + "&sortby=" + sortBy + "&sortorder=" + sortOrder;
	//alert("url->" + pageName);
	updateSamplesList(pageName);
}



function
searchSamples(pKeywords)
{
//	alert("par->" + pKeywords);
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	pageName="../biosamplessample/" + pageName + "?keywords=" + pKeywords +   "&page=" + page + "&sortby=" + sortBy + "&sortorder=" + sortOrder;
//	alert("url->" + pageName);
	updateSamplesList(pageName);
}