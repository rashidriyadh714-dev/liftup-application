package com.liftup.services;

import com.google.gson.Gson;
import java.io.*;

public class SettingsService {
    private String theme = "light"; // or "dark"
    private double fontScale = 1.0;

    public String getTheme(){ return theme; }
    public void setTheme(String t){ theme = t; }
    public double getFontScale(){ return fontScale; }
    public void setFontScale(double s){ fontScale = s; }

    public void save(){ try(FileWriter w = new FileWriter(prefsFile())){ new Gson().toJson(this, w);} catch(Exception ignored){} }
    public SettingsService(){ try(FileReader r = new FileReader(prefsFile())){ SettingsService s = new Gson().fromJson(r, SettingsService.class); if(s!=null){ this.theme=s.theme; this.fontScale=s.fontScale; } } catch(Exception ignored){} }
    private File prefsFile(){ File dir = new File(System.getProperty("user.home"), ".liftup"); if(!dir.exists()) dir.mkdirs(); return new File(dir, "prefs.json"); }
}
