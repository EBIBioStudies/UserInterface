package uk.ac.ebi.arrayexpress.model;

import java.util.ArrayList;
import java.util.List;

public class SampleAttributeBean
{
    private String category;
    private List<String> value;

    // toString() override for lucene indexing
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.category);
        for (String value : this.value) {
            sb.append('\t').append(value);
        }
        return sb.toString();
    }

    // fromString(String) - to parse indexed string
    public SampleAttributeBean fromString(String string) {
        String arr[] = string.split("\t");
        if (null != arr && arr.length > 0) {
            this.category = arr[0];
            if (arr.length > 1) {
                this.value = new ArrayList<String>(arr.length - 1);
                for (int i = 1; i < arr.length; ++i) {
                    this.value.add(arr[i]);
                }
            }
        }
        return this;
    }

    // bean getters and setters
    
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public List<String> getValue()
    {
        return value;
    }

    public void setValue(List<String> value)
    {
        this.value = value;
    }
}
