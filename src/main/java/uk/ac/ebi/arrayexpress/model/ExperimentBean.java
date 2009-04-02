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
    private List<MiameScoreBean> miameScore;
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

    public ExperimentBean()
    {
    }

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

        for (SampleAttributeBean item : this.sampleAttribute) {
            if ((null == item.getCategory() && null == category) || null != item.getCategory() && item.getCategory().equals(category)) {
                if (!item.getValue().contains(value)) {
                    item.getValue().add(value);
                }
                didAppendToExistingCategory = true;
                break;
            }
        }
        if (!didAppendToExistingCategory) {
            SampleAttributeBean item = new SampleAttributeBean();
            item.setCategory(category);
            List<String> valueList = new ArrayList<String>(1);
            valueList.add(value);
            item.setValue(valueList);
            this.sampleAttribute.add(item);
        }
    }

    public void addExperimentalFactor(String name, String value)
    {
        if (null == this.experimentalFactor) {
            this.experimentalFactor = new ArrayList<ExperimentalFactorBean>(1);
        }
        boolean didAppendToExistingName = false;

        for (ExperimentalFactorBean item : this.experimentalFactor) {
            if ((null == item.getName() && null == name) || null != item.getName() && item.getName().equals(name)) {
                if (!item.getValue().contains(value)) {
                    item.getValue().add(value);
                }
                didAppendToExistingName = true;
                break;
            }
        }
        if (!didAppendToExistingName) {
            ExperimentalFactorBean item = new ExperimentalFactorBean();
            item.setName(name);
            List<String> valueList = new ArrayList<String>(1);
            valueList.add(value);
            item.setValue(valueList);
            this.experimentalFactor.add(item);
        }
    }

    public void addMiameScore(String name, String value)
    {
        if (null == this.miameScore) {
            this.miameScore = new ArrayList<MiameScoreBean>(1);
        }

        MiameScoreBean item = new MiameScoreBean();
        item.setName(name);
        item.setValue(value);
        this.miameScore.add(item);
    }

    public void addArrayDesign(String id, String accession, String name, String count)
    {
        if (null == this.arrayDesign) {
            this.arrayDesign = new ArrayList<ArrayDesignBean>(1);
        }

        ArrayDesignBean item = new ArrayDesignBean();
        item.setId(id);
        item.setAccession(accession);
        item.setName(name);
        item.setCount(count);
        this.arrayDesign.add(item);
    }

    public void addBioAssayDataGroup(String id, String name, String bioAssayDataCubes, String arrayDesignProvider, String dataFormat, String bioAssays, String isDerived)
    {
        if (null == this.bioAssayDataGroup) {
            this.bioAssayDataGroup = new ArrayList<BioAssayDataGroupBean>(1);
        }

        BioAssayDataGroupBean item = new BioAssayDataGroupBean();
        item.setId(id);
        item.setName(name);
        item.setBioAssayDataCubes(bioAssayDataCubes);
        item.setArrayDesignProvider(arrayDesignProvider);
        item.setDataFormat(dataFormat);
        item.setBioAssays(bioAssays);
        item.setIsDerived(isDerived);
        this.bioAssayDataGroup.add(item);
    }


    public void addBibliography(String accession, String publication, String authors, String title, String year, String volume, String issue, String pages, String uri)
    {
        if (null == this.bibliography) {
            this.bibliography = new ArrayList<BibliographyBean>(1);
        }

        BibliographyBean item = new BibliographyBean();
        item.setAccession(accession);
        item.setPublication(publication);
        item.setAuthors(authors);
        item.setTitle(title);
        item.setYear(year);
        item.setVolume(volume);
        item.setIssue(issue);
        item.setPages(pages);
        item.setUri(uri);
        this.bibliography.add(item);
    }

    public void addProvider(String contact, String email, String role)
    {
        if (null == this.provider) {
            this.provider = new ArrayList<ProviderBean>(1);
        }

        ProviderBean item = new ProviderBean();
        item.setContact(contact);
        item.setEmail(email);
        item.setRole(role);
        this.provider.add(item);
    }

    public void addExperimentDesign(String experimentDesign)
    {
        if (null == this.experimentDesign) {
            this.experimentDesign = new ArrayList<String>(1);
        }
        this.experimentDesign.add(experimentDesign);
    }

    public void addExperimentType(String experimentType)
    {
        if (null == this.experimentType) {
            this.experimentType = new ArrayList<String>(1);
        }
        this.experimentType.add(experimentType);
    }

    public void addDescription(String id, String text)
    {
        if (null == this.description) {
            this.description = new ArrayList<DescriptionBean>(1);
        }

        DescriptionBean item = new DescriptionBean();
        item.setId(id);
        item.setText(text);
        this.description.add(item);
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

    public List<MiameScoreBean> getMiameScore()
    {
        return miameScore;
    }

    public void setMiameScore(List<MiameScoreBean> miameScore)
    {
        this.miameScore = miameScore;
    }

    public List<DescriptionBean> getDescription()
    {
        return description;
    }

    public void setDescription(List<DescriptionBean> description)
    {
        this.description = description;
    }

    public List<ArrayDesignBean> getArrayDesign()
    {
        return arrayDesign;
    }

    public void setArrayDesign(List<ArrayDesignBean> arrayDesign)
    {
        this.arrayDesign = arrayDesign;
    }

    public List<BibliographyBean> getBibliography()
    {
        return bibliography;
    }

    public void setBibliography(List<BibliographyBean> bibliography)
    {
        this.bibliography = bibliography;
    }

    public List<ProviderBean> getProvider()
    {
        return provider;
    }

    public void setProvider(List<ProviderBean> provider)
    {
        this.provider = provider;
    }

    public List<String> getExperimentDesign()
    {
        return experimentDesign;
    }

    public void setExperimentDesign(List<String> experimentDesign)
    {
        this.experimentDesign = experimentDesign;
    }

    public List<String> getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(List<String> experimentType)
    {
        this.experimentType = experimentType;
    }
}

