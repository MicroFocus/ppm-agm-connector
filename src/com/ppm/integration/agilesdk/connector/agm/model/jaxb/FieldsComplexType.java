//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.06.12 at 10:29:05 PM CST
//


package com.ppm.integration.agilesdk.connector.agm.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FieldsComplexType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FieldsComplexType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Field" type="{http://www.hp.com/ppm/integration/sass/agm/model}FieldComplexType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldsComplexType", propOrder = {
    "field"
})
public class FieldsComplexType {

    @XmlElement(name = "Field", required = true)
    protected List<FieldComplexType> field;

    /**
     * Gets the value of the field property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FieldComplexType }
     *
     *
     */
    public List<FieldComplexType> getField() {
        if (field == null) {
            field = new ArrayList<FieldComplexType>();
        }
        return this.field;
    }

}
