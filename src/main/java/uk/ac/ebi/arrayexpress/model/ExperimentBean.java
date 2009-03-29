package uk.ac.ebi.arrayexpress.model;

import java.util.List;

public class ExperimentBean
{
    private String id;
    private String accession;
    private String name;
    private String releaseDate;
    private List<String> species;
    private String samples;
    private String assays;
    private String miameGold;
    private MiameScoresBean miameScores;
    private List<String> secondaryAccession;
    private List<DescriptionBean> description;
    private List<ArrayDesignBean> arrayDesign;
    private List<SampleAttributeBean> sampleAttribute;
    private List<ExperimentalFactorBean> experimentalFactor;
    private List<BioAssayDataGroupBean> bioAssayDataGroup;
    private List<BibliographyBean> bibliography;
    private List<ProviderBean> provider;
    private List<String> experimentDesign;
    private List<String> experimentType;

    public void setAttributes(String id, String accession, String name, String releaseDate, String miameGold)
    {
        this.id = id;
        this.accession = accession;
        this.name = name;
        this.releaseDate = releaseDate;
        this.miameGold = miameGold;
    }

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

    public String getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public String getMiameGold()
    {
        return miameGold;
    }

    public void setMiameGold(String miameGold)
    {
        this.miameGold = miameGold;
    }

    public String getSamples()
    {
        return samples;
    }

    public void setSamples(String samples)
    {
        this.samples = samples;
    }

    public String getAssays()
    {
        return assays;
    }

    public void setAssays(String assays)
    {
        this.assays = assays;
    }
}

