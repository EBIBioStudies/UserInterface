package uk.ac.ebi.arrayexpress.model;

public class BioAssayDataGroupBean
{
    private String id;
    private String name;
    private String bioAssayDataCubes;
    private String arrayDesignProvider;
    private String dataFormat;
    private String bioAssays;
    private String isDerived;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getBioAssayDataCubes()
    {
        return bioAssayDataCubes;
    }

    public void setBioAssayDataCubes(String bioAssayDataCubes)
    {
        this.bioAssayDataCubes = bioAssayDataCubes;
    }

    public String getArrayDesignProvider()
    {
        return arrayDesignProvider;
    }

    public void setArrayDesignProvider(String arrayDesignProvider)
    {
        this.arrayDesignProvider = arrayDesignProvider;
    }

    public String getDataFormat()
    {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat)
    {
        this.dataFormat = dataFormat;
    }

    public String getBioAssays()
    {
        return bioAssays;
    }

    public void setBioAssays(String bioAssays)
    {
        this.bioAssays = bioAssays;
    }

    public String getIsDerived()
    {
        return isDerived;
    }

    public void setIsDerived(String isDerived)
    {
        this.isDerived = isDerived;
    }
}
