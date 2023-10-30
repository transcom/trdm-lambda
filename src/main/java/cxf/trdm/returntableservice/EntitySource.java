
package cxf.trdm.returntableservice;

import java.math.BigInteger;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for entitySource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entitySource"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="dataAuthority" type="{http://trdm/ReturnTableService}dataAuthorityType"/&gt;
 *         &lt;element name="dataLocation" type="{http://trdm/ReturnTableService}dataLocationType"/&gt;
 *         &lt;element name="authorityOrLocationDetails" type="{http://trdm/ReturnTableService}authorityOrLocationDetailsType"/&gt;
 *         &lt;element name="lastUpdatedDate"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="lastValidatedDate"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="validationFrequencey"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="300"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="dataStewardName"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="60"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/all&gt;
 *       &lt;attribute name="num" type="{http://trdm/ReturnTableService}rowNum" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entitySource", propOrder = {

})
public class EntitySource {

    @XmlElement(required = true, nillable = true)
    protected String dataAuthority;
    @XmlElement(required = true, nillable = true)
    protected String dataLocation;
    @XmlElement(required = true, nillable = true)
    protected String authorityOrLocationDetails;
    @XmlElement(required = true, nillable = true)
    protected XMLGregorianCalendar lastUpdatedDate;
    @XmlElement(required = true, nillable = true)
    protected XMLGregorianCalendar lastValidatedDate;
    @XmlElement(required = true, nillable = true)
    protected String validationFrequencey;
    @XmlElement(required = true, nillable = true)
    protected String dataStewardName;
    @XmlAttribute(name = "num")
    protected BigInteger num;

    /**
     * Gets the value of the dataAuthority property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataAuthority() {
        return dataAuthority;
    }

    /**
     * Sets the value of the dataAuthority property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataAuthority(String value) {
        this.dataAuthority = value;
    }

    /**
     * Gets the value of the dataLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataLocation() {
        return dataLocation;
    }

    /**
     * Sets the value of the dataLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataLocation(String value) {
        this.dataLocation = value;
    }

    /**
     * Gets the value of the authorityOrLocationDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorityOrLocationDetails() {
        return authorityOrLocationDetails;
    }

    /**
     * Sets the value of the authorityOrLocationDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorityOrLocationDetails(String value) {
        this.authorityOrLocationDetails = value;
    }

    /**
     * Gets the value of the lastUpdatedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /**
     * Sets the value of the lastUpdatedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastUpdatedDate(XMLGregorianCalendar value) {
        this.lastUpdatedDate = value;
    }

    /**
     * Gets the value of the lastValidatedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastValidatedDate() {
        return lastValidatedDate;
    }

    /**
     * Sets the value of the lastValidatedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastValidatedDate(XMLGregorianCalendar value) {
        this.lastValidatedDate = value;
    }

    /**
     * Gets the value of the validationFrequencey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidationFrequencey() {
        return validationFrequencey;
    }

    /**
     * Sets the value of the validationFrequencey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidationFrequencey(String value) {
        this.validationFrequencey = value;
    }

    /**
     * Gets the value of the dataStewardName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataStewardName() {
        return dataStewardName;
    }

    /**
     * Sets the value of the dataStewardName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataStewardName(String value) {
        this.dataStewardName = value;
    }

    /**
     * Gets the value of the num property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNum() {
        return num;
    }

    /**
     * Sets the value of the num property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNum(BigInteger value) {
        this.num = value;
    }

}
