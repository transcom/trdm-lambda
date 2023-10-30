
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for noValueFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="noValueFilter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filterType" type="{http://trdm/ReturnTableService}noValueFilterType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "noValueFilter", propOrder = {
    "filterType"
})
public class NoValueFilter {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected NoValueFilterType filterType;

    /**
     * Gets the value of the filterType property.
     * 
     * @return
     *     possible object is
     *     {@link NoValueFilterType }
     *     
     */
    public NoValueFilterType getFilterType() {
        return filterType;
    }

    /**
     * Sets the value of the filterType property.
     * 
     * @param value
     *     allowed object is
     *     {@link NoValueFilterType }
     *     
     */
    public void setFilterType(NoValueFilterType value) {
        this.filterType = value;
    }

}
