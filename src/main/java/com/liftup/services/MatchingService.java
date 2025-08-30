package com.liftup.services;

import java.util.ArrayList;
import java.util.List;

import com.liftup.models.Beneficiary;
import com.liftup.models.Opportunity;

public class MatchingService {
    public List<Opportunity> match(Beneficiary beneficiary, ArrayList<Opportunity> opportunities) {
        return opportunities.stream()
            .filter(opp -> isMatch(beneficiary, opp))
            .toList();
    }

    private boolean isMatch(Beneficiary beneficiary, Opportunity opportunity) {
        // Basic matching logic - can be enhanced based on specific requirements
        return beneficiary.getSkills().stream()
            .anyMatch(skill -> opportunity.getRequiredSkills().contains(skill));
    }
}
