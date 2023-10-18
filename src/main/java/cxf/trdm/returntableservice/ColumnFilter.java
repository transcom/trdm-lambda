
package trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for columnFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="columnFilter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="column" type="{http://trdm/ReturnTableService}column"/&gt;
 *         &lt;element name="columnFilterTypes" type="{http://trdm/ReturnTableService}columnFilterTypeAndValues"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "columnFilter", propOrder = {
    "column",
    "columnFilterTypes"
})
public class ColumnFilter {

    @XmlElement(required = true)
    protected String column;
    @XmlElement(required = true)
    protected ColumnFilterTypeAndValues columnFilterTypes;

    /**
     * Gets the value of the column property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColumn(String value) {
        this.column = value;
    }

    /**
     * Gets the value of the columnFilterTypes property.
     * 
     * @return
     *     possible object is
     *     {@link ColumnFilterTypeAndValues }
     *     
     */
    public ColumnFilterTypeAndValues getColumnFilterTypes() {
        return columnFilterTypes;
    }

    /**
     * Sets the value of the columnFilterTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnFilterTypeAndValues }
     *     
     */
    public void setColumnFilterTypes(ColumnFilterTypeAndValues value) {
        this.columnFilterTypes = value;
    }

}
