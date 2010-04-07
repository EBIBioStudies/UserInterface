package uk.ac.ebi.arrayexpress.utils.autocompletion;

/*
 * Copyright 2009-2010 Functional Genomics Group, European Bioinformatics Institute
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

public class AutocompleteData {

    public static final Character DATA_TEXT = 't';
    public static final Character DATA_EFO_NODE = 'o';
    public static final Character DATA_FIELD = 'f';

    private String text;
    private Character dataType;
    private String data;

    public AutocompleteData( String text, Character dataType, String data )
    {
        this.text = text;
        this.dataType = dataType;
        this.data = data;
    }

    public String getKey()
    {
        return (this.text + "_" + this.dataType + "_" + this.data).toLowerCase();
    }

    public String getText()
    {
        return this.text;
    }

    public String getData()
    {
        return this.data;
    }

    public Character getDataType()
    {
        return this.dataType;
    }

    public String toString()
    {
        return this.getKey();
    }
}
