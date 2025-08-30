package com.liftup.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.liftup.models.Beneficiary;
import com.liftup.models.Opportunity;

public class Exporter {

    private static final String EXPORT_DIR =
            System.getProperty("user.home") + File.separator + "LiftUp" + File.separator + "exports";

    private static File ensureDir() {
        File d = new File(EXPORT_DIR);
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static String exportBeneficiariesCSV(List<Beneficiary> list) throws IOException {
        File f = new File(ensureDir(), "beneficiaries.csv");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(w)) {

            bw.write("id,name,householdSize,skills");
            bw.write(System.lineSeparator());

            for (Beneficiary b : list) {
                String line = String.format(
                        "%s,%s,%d,%s",
                        csv(b.getId()),
                        csv(b.nameProperty().get()),
                        b.householdSizeProperty().get(),
                        csv(String.join("; ", b.getSkills()))
                );
                bw.write(line);
                bw.write(System.lineSeparator());
            }
        }
        return f.getAbsolutePath();
    }

    public static String exportOpportunitiesCSV(List<Opportunity> list) throws IOException {
        File f = new File(ensureDir(), "opportunities.csv");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(w)) {

            bw.write("id,title,requiredSkills,payout");
            bw.write(System.lineSeparator());

            for (Opportunity o : list) {
                String line = String.format(
                        "%s,%s,%s,%s",
                        csv(o.getId()),
                        csv(o.titleProperty().get()),
                        csv(String.join("; ", o.getRequiredSkills())),
                        csv(String.valueOf(o.payoutProperty().get()))
                );
                bw.write(line);
                bw.write(System.lineSeparator());
            }
        }
        return f.getAbsolutePath();
    }

    /**
     * CSV escape: wrap in double quotes and double any inner double-quotes
     * e.g. hello "world" -> "hello ""world"""
     */
    private static String csv(String raw) {
        if (raw == null) return "\"\"";
        String escaped = raw.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
