//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.29 at 03:34:24 PM GMT 
//


package uk.ac.ebi.xml.jaxb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}accession"/>
 *         &lt;element ref="{}id"/>
 *         &lt;element ref="{}name"/>
 *         &lt;element ref="{}arraydesign" maxOccurs="unbounded"/>
 *         &lt;element ref="{}assays"/>
 *         &lt;element ref="{}bibliography" maxOccurs="unbounded"/>
 *         &lt;element ref="{}bioassaydatagroup"/>
 *         &lt;element ref="{}experimentalfactor" maxOccurs="unbounded"/>
 *         &lt;element ref="{}experimenttype" maxOccurs="unbounded"/>
 *         &lt;element ref="{}fgemdatafiles"/>
 *         &lt;element ref="{}lastupdatedate"/>
 *         &lt;element ref="{}loaddate"/>
 *         &lt;element ref="{}miamescores"/>
 *         &lt;element ref="{}minseqescores"/>
 *         &lt;element ref="{}provider"/>
 *         &lt;element ref="{}rawdatafiles"/>
 *         &lt;element ref="{}releasedate"/>
 *         &lt;element ref="{}sampleattribute" maxOccurs="unbounded"/>
 *         &lt;element ref="{}samples" maxOccurs="unbounded"/>
 *         &lt;element ref="{}secondaryaccession"/>
 *         &lt;element ref="{}species" maxOccurs="unbounded"/>
 *         &lt;element ref="{}submissiondate"/>
 *         &lt;element ref="{}user" maxOccurs="unbounded"/>
 *         &lt;element ref="{}experimentdesign" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}description" maxOccurs="unbounded"/>
 *         &lt;element ref="{}protocol" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}seqdatauri" minOccurs="0"/>
 *         &lt;element ref="{}source"/>
 *       &lt;/sequence>
 *       &lt;attribute name="loadedinatlas" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accession",
    "id",
    "name",
    "arraydesign",
    "assays",
    "bibliography",
    "bioassaydatagroup",
    "experimentalfactor",
    "experimenttype",
    "fgemdatafiles",
    "lastupdatedate",
    "loaddate",
    "miamescores",
    "minseqescores",
    "provider",
    "rawdatafiles",
    "releasedate",
    "sampleattribute",
    "samples",
    "secondaryaccession",
    "species",
    "submissiondate",
    "user",
    "experimentdesign",
    "description",
    "protocol",
    "seqdatauri",
    "source"
})
@XmlRootElement(name = "experiment")
public class Experiment {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String accession;
    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected List<Arraydesign> arraydesign;
    @XmlElement(required = true)
    protected BigInteger assays;
    @XmlElement(required = true)
    protected List<Bibliography> bibliography;
    @XmlElement(required = true)
    protected Bioassaydatagroup bioassaydatagroup;
    @XmlElement(required = true)
    protected List<Experimentalfactor> experimentalfactor;
    @XmlElement(required = true)
    protected List<String> experimenttype;
    @XmlElement(required = true)
    protected Fgemdatafiles fgemdatafiles;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar lastupdatedate;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar loaddate;
    @XmlElement(required = true)
    protected Miamescores miamescores;
    @XmlElement(required = true)
    protected Minseqescores minseqescores;
    @XmlElement(required = true)
    protected Provider provider;
    @XmlElement(required = true)
    protected Rawdatafiles rawdatafiles;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar releasedate;
    @XmlElement(required = true)
    protected List<Sampleattribute> sampleattribute;
    @XmlElement(required = true)
    protected List<BigInteger> samples;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String secondaryaccession;
    @XmlElement(required = true)
    protected List<String> species;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar submissiondate;
    @XmlElement(required = true)
    protected List<User> user;
    protected List<String> experimentdesign;
    @XmlElement(required = true)
    protected List<Description> description;
    protected List<Protocol> protocol;
    @XmlSchemaType(name = "anyURI")
    protected String seqdatauri;
    @XmlElement(required = true)
    protected Source source;
    @XmlAttribute
    protected Boolean loadedinatlas;

    /**
     * Gets the value of the accession property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccession() {
        return accession;
    }

    /**
     * Sets the value of the accession property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccession(String value) {
        this.accession = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the arraydesign property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arraydesign property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArraydesign().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Arraydesign }
     * 
     * 
     */
    public List<Arraydesign> getArraydesign() {
        if (arraydesign == null) {
            arraydesign = new ArrayList<Arraydesign>();
        }
        return this.arraydesign;
    }

    /**
     * Gets the value of the assays property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAssays() {
        return assays;
    }

    /**
     * Sets the value of the assays property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAssays(BigInteger value) {
        this.assays = value;
    }

    /**
     * Gets the value of the bibliography property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bibliography property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBibliography().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bibliography }
     * 
     * 
     */
    public List<Bibliography> getBibliography() {
        if (bibliography == null) {
            bibliography = new ArrayList<Bibliography>();
        }
        return this.bibliography;
    }

    /**
     * Gets the value of the bioassaydatagroup property.
     * 
     * @return
     *     possible object is
     *     {@link Bioassaydatagroup }
     *     
     */
    public Bioassaydatagroup getBioassaydatagroup() {
        return bioassaydatagroup;
    }

    /**
     * Sets the value of the bioassaydatagroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bioassaydatagroup }
     *     
     */
    public void setBioassaydatagroup(Bioassaydatagroup value) {
        this.bioassaydatagroup = value;
    }

    /**
     * Gets the value of the experimentalfactor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the experimentalfactor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExperimentalfactor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Experimentalfactor }
     * 
     * 
     */
    public List<Experimentalfactor> getExperimentalfactor() {
        if (experimentalfactor == null) {
            experimentalfactor = new ArrayList<Experimentalfactor>();
        }
        return this.experimentalfactor;
    }

    /**
     * Gets the value of the experimenttype property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the experimenttype property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExperimenttype().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getExperimenttype() {
        if (experimenttype == null) {
            experimenttype = new ArrayList<String>();
        }
        return this.experimenttype;
    }

    /**
     * Gets the value of the fgemdatafiles property.
     * 
     * @return
     *     possible object is
     *     {@link Fgemdatafiles }
     *     
     */
    public Fgemdatafiles getFgemdatafiles() {
        return fgemdatafiles;
    }

    /**
     * Sets the value of the fgemdatafiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fgemdatafiles }
     *     
     */
    public void setFgemdatafiles(Fgemdatafiles value) {
        this.fgemdatafiles = value;
    }

    /**
     * Gets the value of the lastupdatedate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastupdatedate() {
        return lastupdatedate;
    }

    /**
     * Sets the value of the lastupdatedate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastupdatedate(XMLGregorianCalendar value) {
        this.lastupdatedate = value;
    }

    /**
     * Gets the value of the loaddate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLoaddate() {
        return loaddate;
    }

    /**
     * Sets the value of the loaddate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLoaddate(XMLGregorianCalendar value) {
        this.loaddate = value;
    }

    /**
     * Gets the value of the miamescores property.
     * 
     * @return
     *     possible object is
     *     {@link Miamescores }
     *     
     */
    public Miamescores getMiamescores() {
        return miamescores;
    }

    /**
     * Sets the value of the miamescores property.
     * 
     * @param value
     *     allowed object is
     *     {@link Miamescores }
     *     
     */
    public void setMiamescores(Miamescores value) {
        this.miamescores = value;
    }

    /**
     * Gets the value of the minseqescores property.
     * 
     * @return
     *     possible object is
     *     {@link Minseqescores }
     *     
     */
    public Minseqescores getMinseqescores() {
        return minseqescores;
    }

    /**
     * Sets the value of the minseqescores property.
     * 
     * @param value
     *     allowed object is
     *     {@link Minseqescores }
     *     
     */
    public void setMinseqescores(Minseqescores value) {
        this.minseqescores = value;
    }

    /**
     * Gets the value of the provider property.
     * 
     * @return
     *     possible object is
     *     {@link Provider }
     *     
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Sets the value of the provider property.
     * 
     * @param value
     *     allowed object is
     *     {@link Provider }
     *     
     */
    public void setProvider(Provider value) {
        this.provider = value;
    }

    /**
     * Gets the value of the rawdatafiles property.
     * 
     * @return
     *     possible object is
     *     {@link Rawdatafiles }
     *     
     */
    public Rawdatafiles getRawdatafiles() {
        return rawdatafiles;
    }

    /**
     * Sets the value of the rawdatafiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link Rawdatafiles }
     *     
     */
    public void setRawdatafiles(Rawdatafiles value) {
        this.rawdatafiles = value;
    }

    /**
     * Gets the value of the releasedate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReleasedate() {
        return releasedate;
    }

    /**
     * Sets the value of the releasedate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReleasedate(XMLGregorianCalendar value) {
        this.releasedate = value;
    }

    /**
     * Gets the value of the sampleattribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sampleattribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSampleattribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sampleattribute }
     * 
     * 
     */
    public List<Sampleattribute> getSampleattribute() {
        if (sampleattribute == null) {
            sampleattribute = new ArrayList<Sampleattribute>();
        }
        return this.sampleattribute;
    }

    /**
     * Gets the value of the samples property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the samples property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSamples().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     * 
     * 
     */
    public List<BigInteger> getSamples() {
        if (samples == null) {
            samples = new ArrayList<BigInteger>();
        }
        return this.samples;
    }

    /**
     * Gets the value of the secondaryaccession property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecondaryaccession() {
        return secondaryaccession;
    }

    /**
     * Sets the value of the secondaryaccession property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecondaryaccession(String value) {
        this.secondaryaccession = value;
    }

    /**
     * Gets the value of the species property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the species property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecies().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSpecies() {
        if (species == null) {
            species = new ArrayList<String>();
        }
        return this.species;
    }

    /**
     * Gets the value of the submissiondate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSubmissiondate() {
        return submissiondate;
    }

    /**
     * Sets the value of the submissiondate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSubmissiondate(XMLGregorianCalendar value) {
        this.submissiondate = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the user property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link User }
     * 
     * 
     */
    public List<User> getUser() {
        if (user == null) {
            user = new ArrayList<User>();
        }
        return this.user;
    }

    /**
     * Gets the value of the experimentdesign property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the experimentdesign property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExperimentdesign().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getExperimentdesign() {
        if (experimentdesign == null) {
            experimentdesign = new ArrayList<String>();
        }
        return this.experimentdesign;
    }

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Description }
     * 
     * 
     */
    public List<Description> getDescription() {
        if (description == null) {
            description = new ArrayList<Description>();
        }
        return this.description;
    }

    /**
     * Gets the value of the protocol property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the protocol property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProtocol().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Protocol }
     * 
     * 
     */
    public List<Protocol> getProtocol() {
        if (protocol == null) {
            protocol = new ArrayList<Protocol>();
        }
        return this.protocol;
    }

    /**
     * Gets the value of the seqdatauri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeqdatauri() {
        return seqdatauri;
    }

    /**
     * Sets the value of the seqdatauri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeqdatauri(String value) {
        this.seqdatauri = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setSource(Source value) {
        this.source = value;
    }

    /**
     * Gets the value of the loadedinatlas property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLoadedinatlas() {
        if (loadedinatlas == null) {
            return false;
        } else {
            return loadedinatlas;
        }
    }

    /**
     * Sets the value of the loadedinatlas property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLoadedinatlas(Boolean value) {
        this.loadedinatlas = value;
    }

}
