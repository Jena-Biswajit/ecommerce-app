package com.example.catlog.domain;

import java.math.BigDecimal;

public class AttributeValue {
    private Long attributeId;
    private String name;          // e.g., RAM, Panel
    private String valueType;     // STRING or NUMBER
    private String valueString;
    private BigDecimal valueNumber;

    public Long getAttributeId() { return attributeId; }
    public void setAttributeId(Long attributeId) { this.attributeId = attributeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public String getValueString() { return valueString; }
    public void setValueString(String valueString) { this.valueString = valueString; }
    public BigDecimal getValueNumber() { return valueNumber; }
    public void setValueNumber(BigDecimal valueNumber) { this.valueNumber = valueNumber; }
}
