package com.liftup.models;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Beneficiary {
    private final String id;
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty householdSize = new SimpleIntegerProperty();
    private final List<String> skills;
    private final IntegerProperty score = new SimpleIntegerProperty();

    public Beneficiary(String id, String name, int householdSize, List<String> skills){
        this.id = id; this.name.set(name); this.householdSize.set(householdSize); this.skills = new ArrayList<>(skills); this.score.set(Math.min(100, 10 + householdSize * 10));
    }
    public String getId(){ return id; }
    public StringProperty nameProperty(){ return name; }
    public IntegerProperty householdSizeProperty(){ return householdSize; }
    public IntegerProperty scoreProperty(){ return score; }
    public List<String> getSkills(){ return skills; }
    public StringProperty skillsCsvProperty(){ return new SimpleStringProperty(String.join(", ", skills)); }
    @Override public String toString(){ return name.get() + " (" + (id.length() > 6 ? id.substring(0,6) : id) + ")"; }
}
