package com.liftup.models;

import javafx.beans.property.*;
import java.util.*;

public class Opportunity {
    private final String id;
    private final StringProperty title = new SimpleStringProperty();
    private final List<String> requiredSkills;
    private final DoubleProperty payout = new SimpleDoubleProperty();

    public Opportunity(String id, String title, List<String> requiredSkills, double payout){ this.id = id; this.title.set(title); this.requiredSkills = new ArrayList<>(requiredSkills); this.payout.set(payout); }
    public String getId(){ return id; }
    public StringProperty titleProperty(){ return title; }
    public List<String> getRequiredSkills(){ return requiredSkills; }
    public StringProperty requiredSkillsCsvProperty(){ return new SimpleStringProperty(String.join(", ", requiredSkills)); }
    public DoubleProperty payoutProperty(){ return payout; }
}
