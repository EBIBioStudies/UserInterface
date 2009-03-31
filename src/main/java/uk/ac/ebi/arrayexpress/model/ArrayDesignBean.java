package uk.ac.ebi.arrayexpress.model;

public class ArrayDesignBean
{
    private String id;
    private String accession;
    private String name;
    private String count; //TODO - what is this?

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getAccession()
    {
        return accession;
    }

    public void setAccession(String accession)
    {
        this.accession = accession;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }
}
