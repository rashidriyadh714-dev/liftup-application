package com.liftup.util;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class IconProvider {

    public static FontIcon getIcon(MaterialDesign iconCode) {
        return new FontIcon(iconCode);
    }
}
