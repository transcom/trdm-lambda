
package cxf.trdm.returntableservice;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValueDateTimeFilterType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="singleValueDateTimeFilterType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IS_AFTER"/&gt;
 *     &lt;enumeration value="IS_ON_OR_AFTER"/&gt;
 *     &lt;enumeration value="IS_BEFORE"/&gt;
 *     &lt;enumeration value="IS_ON_OR_BEFORE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "singleValueDateTimeFilterType")
@XmlEnum
public enum SingleValueDateTimeFilterType {

    IS_AFTER,
    IS_ON_OR_AFTER,
    IS_BEFORE,
    IS_ON_OR_BEFORE;

    public String value() {
        return name();
    }

    public static SingleValueDateTimeFilterType fromValue(String v) {
        return valueOf(v);
    }

}
