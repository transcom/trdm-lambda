
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for noValueFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="noValueFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_NULL"/&gt;
 *     &lt;enumeration value="IS_NOT_NULL"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "noValueFilterType")
@XmlEnum
public enum NoValueFilterType {

    IS_NULL,
    IS_NOT_NULL;

    public String value() {
        return name();
    }

    public static NoValueFilterType fromValue(String v) {
        return valueOf(v);
    }

}
