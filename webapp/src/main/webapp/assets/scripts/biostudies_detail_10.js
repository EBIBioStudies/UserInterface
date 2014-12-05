/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
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

$(function() {
	/*
	 * hint to the lucene highlight - the right side is the link, so I dont need
	 */

	$("#bs_detail").find(".ae_text_hit").attr("title",
			"This is exact string matched for input query terms");
	$("#bs_detail").find(".ae_text_syn").attr("title",
			"This is synonym matched from Experimental Factor Ontology");
	$("#bs_detail").find(".ae_text_efo").attr("title",
			"This is matched child term from Experimental Factor Ontology");
	/* hint to the lucene highlight */
});

function ShwHid(divId) {
	if (document.getElementById(divId).style.display == 'none') {
		document.getElementById(divId).style.display = 'block';
	} else {
		document.getElementById(divId).style.display = 'none';
	}
}