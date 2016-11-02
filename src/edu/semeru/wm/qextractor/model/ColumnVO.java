package edu.semeru.wm.qextractor.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class ColumnVO {

    private String name;
    private String type;
    private String numericTypeScale; //digits
    private String numericTypePrecision; //decimals
    private String keyType;
    private String keyName;
    private String referencedTable;
    private String referencedColumn;
    private boolean autoNumeric;
    private String defaultValue;
    private boolean nullable;
    private String charMaxLength;
  
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the numericTypeScale
     */
    public String getNumericTypeScale() {
        return numericTypeScale;
    }

    /**
     * @param numericTypeScale the numericTypeScale to set
     */
    public void setNumericTypeScale(String numericTypeScale) {
        this.numericTypeScale = numericTypeScale;
    }

    /**
     * @return the numericTypePrecision
     */
    public String getNumericTypePrecision() {
        return numericTypePrecision;
    }

    /**
     * @param numericTypePrecision the numericTypePrecision to set
     */
    public void setNumericTypePrecision(String numericTypePrecision) {
        this.numericTypePrecision = numericTypePrecision;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the nullable
     */
    public boolean getNullable() {
        return nullable;
    }

    /**
     * @param nullable the nullable to set
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    
    /**
     * @return the keyType
     */
    public String getKeyType() {
        return keyType;
    }

    /**
     * @param keyType the keyType to set
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * @return the keyName
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * @param keyName the keyName to set
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * @return the referencedTable
     */
    public String getReferencedTable() {
        return referencedTable;
    }

    /**
     * @param referencedTable the referencedTable to set
     */
    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }

    /**
     * @return the referencedColumn
     */
    public String getReferencedColumn() {
        return referencedColumn;
    }

    /**
     * @param referencedColumn the referencedColumn to set
     */
    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn;
    }

    /**
     * @return the autoNumeric
     */
    public boolean getAutoNumeric() {
        return autoNumeric;
    }

    /**
     * @param autoNumeric the autoNumeric to set
     */
    public void setAutoNumeric(boolean autoNumeric) {
        this.autoNumeric = autoNumeric;
    }

	public String getCharMaxLength() {
		return charMaxLength;
	}

	public void setCharMaxLength(String charMaxLength) {
		this.charMaxLength = charMaxLength;
	}

  

}
