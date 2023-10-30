
package cxf.trdm.returntableservice;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for multiValueFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="multiValueFilter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filterType" type="{http://trdm/ReturnTableService}multiValueFilterType"/&gt;
 *         &lt;element name="filterValue" type="{http://trdm/ReturnTableService}filterValue" maxOccurs="100"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multiValueFilter", propOrder = {
    "filterType",
    "filterValue"
})
public class MultiValueFilter {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected MultiValueFilterType filterType;
    @XmlElement(required = true)
    protected List<String> filterValue;

    /**
     * Gets the value of the filterType property.
     * 
     * @return
     *     possible object is
     *     {@link MultiValueFilterType }
     *     
     */
    public MultiValueFilterType getFilterType() {
        return filterType;
    }

    /**
     * Sets the value of the filterType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MultiValueFilterType }
     *     
     */
    public void setFilterType(MultiValueFilterType value) {
        this.filterType = value;
    }

    /**
     * Gets the value of the filterValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the filterValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilterValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFilterValue() {
        if (filterValue == null) {
            filterValue = new ArrayList<String>();
        }
        return this.filterValue;
    }

}
