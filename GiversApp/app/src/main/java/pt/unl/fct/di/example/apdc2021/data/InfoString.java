package pt.unl.fct.di.example.apdc2021.data;

public class InfoString {

    String excludesFromIndexes;
    Integer meaning;
    String value;
    String valueType;

    public InfoString(String excludesFromIndexes, Integer meaning, String value, String valueType) {
        this.excludesFromIndexes = excludesFromIndexes;
        this.meaning = meaning;
        this.value = value;
        this.valueType = valueType;
    }

    public String getExcludesFromIndexes() {
        return excludesFromIndexes;
    }

    public Integer getMeaning() {
        return meaning;
    }

    public String getValue() {
        return value;
    }

    public String getValueType() {
        return valueType;
    }
}
