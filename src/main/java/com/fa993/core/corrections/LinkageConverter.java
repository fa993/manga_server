package com.fa993.core.corrections;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;

@Converter(autoApply = true)
public class LinkageConverter implements AttributeConverter<LinkageType, String> {


    private LinkageType[] vals = LinkageType.values();

    @Override
    public String convertToDatabaseColumn(LinkageType attribute) {
        if(attribute == null) {
            return null;
        } else {
            return attribute.getValue();
        }
    }

    @Override
    public LinkageType convertToEntityAttribute(String dbData) {
        if(dbData == null){
            return null;
        } else {
            return Arrays.stream(vals).filter(t -> t.getValue().equals(dbData)).findFirst().orElseThrow(IllegalArgumentException::new);
        }
    }
}
