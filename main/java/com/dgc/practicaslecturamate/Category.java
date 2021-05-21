package com.dgc.practicaslecturamate;

public class Category {
    public static final int LETRAS = 1;
    public static final int SILABAS = 2;
    public static final int PALABRAS = 3;
    public static final int ORACIONES = 4;
    public static final int COMPRENSIONLECTORA = 5;
    public static final int MATEMATICAS = 6;

    private int id;
    private String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

