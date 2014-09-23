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

// the next funtions are used for paging the samples inside a group
// ######### PAGING SAMPLES INSIDE A GROUP OF SAMPLES ############
var query = new Object();
var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
var anchor = decodeURI(window.location.hash);

// if I;m showing the detail I will get all the samples using paging
$(function() {
	// alert("2121");
	// manage 2 scrolbars

	//hint for lucene highlights
	$("#bs_browse").find(".ae_text_hit").attr("title",
			"This is exact string matched for input query terms");
	$("#bs_browse").find(".ae_text_syn").attr("title",
			"This is synonym matched from Experimental Factor Ontology");
	$("#bs_browse").find(".ae_text_efo").attr("title",
			"This is matched child term from Experimental Factor Ontology");
	//hint for lucene highlights
	

	var tableWidth = $("#attr_table").width(); // 
	// alert("dynamic table width->" + tableWidth);
	$("#div_top_scroll").css('width', tableWidth);

	$("#wrapper_top_scroll").scroll(
			function() {
				// alert("wrapper_top_scroll SCROLL");
				$(".attr_table_scroll").scrollLeft(
						$("#wrapper_top_scroll").scrollLeft());
			});
	$(".attr_table_scroll").scroll(
			function() {
				// alert("attr_table_scroll SCROLL");
				$("#wrapper_top_scroll").scrollLeft(
						$(".attr_table_scroll").scrollLeft());
			});

});

// ######### PAGING SAMPLES INSIDE A GROUP OF SAMPLES ############
