package uk.ac.ebi.arrayexpress.model;

import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;
import java.util.List;

public class ExperimentBean
{
    private String id;
    private String accession;
    private String name;
    private String releaseDate;
    private List<String> species;
    private List<String> user;
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

    public void addUser(String user)
    {
        if (null == this.user) {
            this.user = new ArrayList<String>(1);
        }
        this.user.add(user);
    }

    public void addSecondaryAccession(String secondaryAccession)
    {
        if (null == this.secondaryAccession) {
            this.secondaryAccession = new ArrayList<String>(1);
        }
        this.secondaryAccession.add(secondaryAccession);
    }

    public void addSampleAttribute(String category, String value)
    {
        if (null == this.sampleAttribute) {
            this.sampleAttribute = new ArrayList<SampleAttributeBean>(1);
        }
        boolean didAppendToExistingCategory = false;

        for (SampleAttributeBean sampleAttribute : this.sampleAttribute) {
            if (sampleAttribute.getCategory().equals(category)) {
                if (!sampleAttribute.getValue().contains(value)) {
                    sampleAttribute.getValue().add(value);
                }
                didAppendToExistingCategory = true;
                break;
            }
        }
        if (!didAppendToExistingCategory) {
            SampleAttributeBean sampleAttribute = new SampleAttributeBean();
            sampleAttribute.setCategory(category);
            List<String> valueList = new ArrayList<String>(1);
            valueList.add(value);
            sampleAttribute.setValue(valueList);
            this.sampleAttribute.add(sampleAttribute);
        }
    }

    // bean getters and setters

    public String getId()
    {
        return id;
    }

    @Field
    public void setId(String id)
    {
        this.id = id;
    }

    public String getAccession()
    {
        return accession;
    }

    @Field
    public void setAccession(String accession)
    {
        this.accession = accession;
    }

    public String getName()
    {
        return name;
    }

    @Field
    public void setName(String name)
    {
        this.name = name;
    }

    public String getReleaseDate()
    {
        return releaseDate;
    }

    @Field
    public void setReleaseDate(String releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public String getMiameGold()
    {
        return miameGold;
    }

    @Field
    public void setMiameGold(String miameGold)
    {
        this.miameGold = miameGold;
    }

    public String getSamples()
    {
        return samples;
    }

    @Field
    public void setSamples(String samples)
    {
        this.samples = samples;
    }

    public String getAssays()
    {
        return assays;
    }

    @Field
    public void setAssays(String assays)
    {
        this.assays = assays;
    }

    public List<String> getSpecies()
    {
        return species;
    }

    @Field
    public void setSpecies(List<String> species)
    {
        this.species = species;
    }

    public List<String> getUser()
    {
        return user;
    }

    @Field
    public void setUser(List<String> user)
    {
        this.user = user;
    }

    public List<String> getSecondaryAccession()
    {
        return secondaryAccession;
    }

    @Field
    public void setSecondaryAccession(List<String> secondaryAccession)
    {
        this.secondaryAccession = secondaryAccession;
    }

    public List getSampleAttribute()
    {
        return sampleAttribute;
    }

    @Field
    public void setSampleAttribute(List sampleAttribute)
    {
        this.sampleAttribute = new ArrayList<SampleAttributeBean>(sampleAttribute.size());
        for (Object sa : sampleAttribute ) {
            this.sampleAttribute.add(new SampleAttributeBean().fromString(sa.toString()));
        }
    }

    public List<ExperimentalFactorBean> getExperimentalFactor()
    {
        return experimentalFactor;
    }

    @Field
    public void setExperimentalFactor(List<ExperimentalFactorBean> experimentalFactor)
    {
        this.experimentalFactor = experimentalFactor;
    }

    public List<BioAssayDataGroupBean> getBioAssayDataGroup()
    {
        return bioAssayDataGroup;
    }

    @Field
    public void setBioAssayDataGroup(List<BioAssayDataGroupBean> bioAssayDataGroup)
    {
        this.bioAssayDataGroup = bioAssayDataGroup;
    }
}

