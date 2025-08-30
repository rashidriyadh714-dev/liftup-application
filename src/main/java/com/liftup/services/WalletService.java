package com.liftup.services;

import java.util.HashMap;
import java.util.Map;

public class WalletService {
    private final Map<String, Double> balances = new HashMap<>();

    public void credit(String beneficiaryId, double amount) {
        balances.merge(beneficiaryId, amount, Double::sum);
    }

    public void debit(String beneficiaryId, double amount) {
        if (getBalance(beneficiaryId) >= amount) {
            balances.merge(beneficiaryId, -amount, Double::sum);
        }
    }

    public double getBalance(String beneficiaryId) {
        return balances.getOrDefault(beneficiaryId, 0.0);
    }

    public String transfer(String fromId, String toId, double amount) {
        if (getBalance(fromId) >= amount) {
            debit(fromId, amount);
            credit(toId, amount);
            return String.format("Transferred %.2f MYR from %s to %s", amount, fromId, toId);
        }
        return "Insufficient funds";
    }
}
