
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for columnFilterMatchesCriteria.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="columnFilterMatchesCriteria"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ALL"/&gt;
 *     &lt;enumeration value="ANY"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "columnFilterMatchesCriteria")
@XmlEnum
public enum ColumnFilterMatchesCriteria {

    ALL,
    ANY;

    public String value() {
        return name();
    }

    public static ColumnFilterMatchesCriteria fromValue(String v) {
        return valueOf(v);
    }

}
