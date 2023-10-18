
package trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValueNumericalFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleValueNumericalFilter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filterType" type="{http://trdm/ReturnTableService}singleValueNumericalFilterType"/&gt;
 *         &lt;element name="filterValue" type="{http://trdm/ReturnTableService}numericalFilterValue"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleValueNumericalFilter", propOrder = {
    "filterType",
    "filterValue"
})
public class SingleValueNumericalFilter {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SingleValueNumericalFilterType filterType;
    protected double filterValue;

    /**
     * Gets the value of the filterType property.
     * 
     * @return
     *     possible object is
     *     {@link SingleValueNumericalFilterType }
     *     
     */
    public SingleValueNumericalFilterType getFilterType() {
        return filterType;
    }

    /**
     * Sets the value of the filterType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleValueNumericalFilterType }
     *     
     */
    public void setFilterType(SingleValueNumericalFilterType value) {
        this.filterType = value;
    }

    /**
     * Gets the value of the filterValue property.
     * 
     */
    public double getFilterValue() {
        return filterValue;
    }

    /**
     * Sets the value of the filterValue property.
     * 
     */
    public void setFilterValue(double value) {
        this.filterValue = value;
    }

}
