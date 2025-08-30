package com.liftup.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liftup.models.Beneficiary;
import com.liftup.models.Opportunity;

public class DataStore {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String baseResourcePath; private final String dataFile;

    public DataStore(String baseResourcePath, String dataFile){ this.baseResourcePath = baseResourcePath; this.dataFile = dataFile; }

    private File resolve(){ try { File dir = new File(System.getProperty("user.home"), ".liftup"); if(!dir.exists()) dir.mkdirs(); return new File(dir, "data.json"); } catch(Exception e){ return new File("data.json"); } }

    static class BeneficiaryDTO { String id; String name; int householdSize; List<String> skills; }
    static class OpportunityDTO { String id; String title; List<String> requiredSkills; double payout; }

    public List<Beneficiary> loadBeneficiaries(boolean sample){ try{ List<BeneficiaryDTO> dtos = sample? readBeneficiaryDTOsFromSample() : readBeneficiaryDTOsFromFile(); List<Beneficiary> out = new ArrayList<>(); for(BeneficiaryDTO d: dtos){ if(d!=null) out.add(new Beneficiary(nz(d.id), nz(d.name), d.householdSize, nzList(d.skills))); } return out; } catch(Exception e){ System.err.println("Failed to load beneficiaries: " + e.getMessage()); return new ArrayList<>(); } }
    public List<Opportunity> loadOpportunities(boolean sample){ try{ List<OpportunityDTO> dtos = sample? readOpportunityDTOsFromSample() : readOpportunityDTOsFromFile(); List<Opportunity> out = new ArrayList<>(); for(OpportunityDTO d: dtos){ if(d!=null) out.add(new Opportunity(nz(d.id), nz(d.title), nzList(d.requiredSkills), d.payout)); } return out; } catch(Exception e){ System.err.println("Failed to load opportunities: " + e.getMessage()); return new ArrayList<>(); } }

    public void saveBeneficiaries(List<Beneficiary> list){ File f = resolve(); Map<String,Object> map = readMapFromFile(f); List<BeneficiaryDTO> dtos = new ArrayList<>(); for(Beneficiary b: list){ BeneficiaryDTO d=new BeneficiaryDTO(); d.id=b.getId(); d.name=b.nameProperty().get(); d.householdSize=b.householdSizeProperty().get(); d.skills=new ArrayList<>(b.getSkills()); dtos.add(d);} map.put("beneficiaries", dtos); writeMapToFile(f, map); }
    public void saveOpportunities(List<Opportunity> list){ File f = resolve(); Map<String,Object> map = readMapFromFile(f); List<OpportunityDTO> dtos = new ArrayList<>(); for(Opportunity o: list){ OpportunityDTO d=new OpportunityDTO(); d.id=o.getId(); d.title=o.titleProperty().get(); d.requiredSkills=new ArrayList<>(o.getRequiredSkills()); d.payout=o.payoutProperty().get(); dtos.add(d);} map.put("opportunities", dtos); writeMapToFile(f, map); }

    public void reset(){ File f = resolve(); if(f.exists()) f.delete(); }

    private Map<String,Object> readMapFromFile(File f){ if(!f.exists()) return new HashMap<>(); try(Reader r=new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)){ Map<String,Object> m=gson.fromJson(r, new TypeToken<Map<String,Object>>(){}.getType()); return m!=null?m:new HashMap<>(); } catch(Exception e){ System.err.println("Failed to read map from file: " + e.getMessage()); return new HashMap<>(); } }
    private void writeMapToFile(File f, Map<String,Object> map){ try(Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)){ gson.toJson(map, w);} catch(Exception e){ System.err.println("Failed to write map to file: " + e.getMessage()); } }

    private List<BeneficiaryDTO> readBeneficiaryDTOsFromFile(){ File f=resolve(); if(!f.exists()) return readBeneficiaryDTOsFromSample(); try(Reader r=new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)){ Map<String,Object> map=gson.fromJson(r, new TypeToken<Map<String,Object>>(){}.getType()); String json=gson.toJson(map!=null?map.get("beneficiaries"):null); Type t=new TypeToken<List<BeneficiaryDTO>>(){}.getType(); List<BeneficiaryDTO> list=gson.fromJson(json,t); return list!=null?list:new ArrayList<>(); } catch(Exception e){ System.err.println("Failed to read beneficiaries from file: " + e.getMessage()); return new ArrayList<>(); } }
    private List<OpportunityDTO> readOpportunityDTOsFromFile(){ File f=resolve(); if(!f.exists()) return readOpportunityDTOsFromSample(); try(Reader r=new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)){ Map<String,Object> map=gson.fromJson(r, new TypeToken<Map<String,Object>>(){}.getType()); String json=gson.toJson(map!=null?map.get("opportunities"):null); Type t=new TypeToken<List<OpportunityDTO>>(){}.getType(); List<OpportunityDTO> list=gson.fromJson(json,t); return list!=null?list:new ArrayList<>(); } catch(Exception e){ System.err.println("Failed to read opportunities from file: " + e.getMessage()); return new ArrayList<>(); } }
    private List<BeneficiaryDTO> readBeneficiaryDTOsFromSample(){ try(InputStream is=getClass().getResourceAsStream("/data/sample-data.json")){ if(is==null) return new ArrayList<>(); try(Reader r=new InputStreamReader(is, StandardCharsets.UTF_8)){ Map<String,Object> map=gson.fromJson(r, new TypeToken<Map<String,Object>>(){}.getType()); String json=gson.toJson(map!=null?map.get("beneficiaries"):null); Type t=new TypeToken<List<BeneficiaryDTO>>(){}.getType(); List<BeneficiaryDTO> list=gson.fromJson(json,t); return list!=null?list:new ArrayList<>(); } } catch(Exception e){ System.err.println("Failed to read beneficiaries from sample: " + e.getMessage()); } return new ArrayList<>(); }
    private List<OpportunityDTO> readOpportunityDTOsFromSample(){ try(InputStream is=getClass().getResourceAsStream("/data/sample-data.json")){ if(is==null) return new ArrayList<>(); try(Reader r=new InputStreamReader(is, StandardCharsets.UTF_8)){ Map<String,Object> map=gson.fromJson(r, new TypeToken<Map<String,Object>>(){}.getType()); String json=gson.toJson(map!=null?map.get("opportunities"):null); Type t=new TypeToken<List<OpportunityDTO>>(){}.getType(); List<OpportunityDTO> list=gson.fromJson(json,t); return list!=null?list:new ArrayList<>(); } } catch(Exception e){ System.err.println("Failed to read opportunities from sample: " + e.getMessage()); } return new ArrayList<>(); }

    private static String nz(String s){ return s==null? "" : s; }
    private static List<String> nzList(List<String> in){ return in==null? new ArrayList<>() : in; }
}
