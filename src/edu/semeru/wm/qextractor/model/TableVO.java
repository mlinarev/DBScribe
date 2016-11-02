package edu.semeru.wm.qextractor.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

public class TableVO {


    private int id;
    private String name;
    private HashMap<String, ColumnVO> columns = new HashMap<String, ColumnVO>();
    private String description;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, ColumnVO> getColumns() {
        return columns;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

   

}
