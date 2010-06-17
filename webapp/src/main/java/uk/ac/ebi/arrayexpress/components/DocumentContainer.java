package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.DocumentInfo;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;

import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2009-2010 European Molecular Biology Laboratory
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

public class DocumentContainer extends ApplicationComponent {

    private Map<String, DocumentInfo> documents = new HashMap<String, DocumentInfo>();

    public DocumentContainer() {
        super("DocumentContainer");
    }

    /**
     * Implements ApplicaitonComponent.initialize()
     *
     * @throws Exception
     */
    public void initialize() throws Exception {
    }

    /**
     * Implements ApplicationComponent.terminate()
     *
     * @throws Exception
     */
    public void terminate() throws Exception {
    }

    /**
     * Checks if the document with given ID exists in the storage container
     *
     * @param documentId
     * @return
     */
    //ToDo: maybe it's better to use Enum instead of Strings
    public boolean hasDocument(String documentId) {
        return documents.containsKey(documentId);
    }

    /**
     * Returns Saxon TinyTree document object by its document ID
     *
     * @param documentId
     * @return
     */
    public DocumentInfo getDocument(String documentId) {
        return documents.get(documentId);
    }

    /**
     * Puts Saxon TinyTree document associated with its ID
     *
     * @param documentId
     * @param document
     */
    public void putDocument(String documentId, DocumentInfo document) {
        documents.put(documentId, document);
        // do index that doc as well
        SearchEngine search = (SearchEngine) getComponent("SearchEngine");
        search.getController().index(documentId, document);

    }
}