
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValueDateFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="singleValueDateFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_ON"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "singleValueDateFilterType")
@XmlEnum
public enum SingleValueDateFilterType {

    IS_ON;

    public String value() {
        return name();
    }

    public static SingleValueDateFilterType fromValue(String v) {
        return valueOf(v);
    }

}
