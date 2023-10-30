
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValueFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleValueFilter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filterType" type="{http://trdm/ReturnTableService}singleValueFilterType"/&gt;
 *         &lt;element name="filterValue" type="{http://trdm/ReturnTableService}filterValue"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleValueFilter", propOrder = {
    "filterType",
    "filterValue"
})
public class SingleValueFilter {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SingleValueFilterType filterType;
    @XmlElement(required = true)
    protected String filterValue;

    /**
     * Gets the value of the filterType property.
     * 
     * @return
     *     possible object is
     *     {@link SingleValueFilterType }
     *     
     */
    public SingleValueFilterType getFilterType() {
        return filterType;
    }

    /**
     * Sets the value of the filterType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleValueFilterType }
     *     
     */
    public void setFilterType(SingleValueFilterType value) {
        this.filterType = value;
    }

    /**
     * Gets the value of the filterValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilterValue() {
        return filterValue;
    }

    /**
     * Sets the value of the filterValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilterValue(String value) {
        this.filterValue = value;
    }

}
