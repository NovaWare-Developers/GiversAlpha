package pt.unl.fct.di.example.apdc2021.data;
 
public class InfoLong {

    String excludesFromIndexes;
    Integer meaning;
    long value;
    String valueType;

    public InfoLong(String excludesFromIndexes, Integer meaning, long value, String valueType) {
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

    public long getValue() {
        return value;
    }

    public String getValueType() {
        return valueType;
    }
}
