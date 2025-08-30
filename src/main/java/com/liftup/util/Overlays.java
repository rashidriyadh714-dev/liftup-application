package com.liftup.util;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Overlay manager with a stack (push/pop), header back/close, ESC to close, backdrop click to dismiss. */
public class Overlays {
    private final StackPane root;
    private final Deque<StackPane> stack = new ArrayDeque<>();
    private final IntegerProperty depth = new SimpleIntegerProperty(0);

    public Overlays(StackPane root){ this.root = root; }

    /** Push a sheet overlay with standard header */
    public void pushSheet(String title, Node content){
        // Backdrop
        StackPane backdrop = new StackPane(); backdrop.getStyleClass().add("overlay-backdrop"); backdrop.setPickOnBounds(true);
        // Header
        Label header = new Label(title); header.getStyleClass().add("overlay-title");
        Button back = new Button("Back"); back.getStyleClass().add("back-btn"); back.setOnAction(e -> pop());
        Button close = new Button("Close"); close.getStyleClass().add("close-btn"); close.setOnAction(e -> pop());
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox headerBox = new HBox(10, back, header, spacer, close); headerBox.getStyleClass().add("overlay-header");
        BorderPane sheet = new BorderPane(); sheet.getStyleClass().add("overlay-sheet");
        VBox box = new VBox(12, headerBox, content); box.setPadding(new Insets(4,0,0,0)); sheet.setCenter(box);
        // Wrapper
        StackPane wrapper = new StackPane(backdrop, sheet); StackPane.setAlignment(sheet, Pos.CENTER);
        backdrop.setOnMouseClicked(e -> pop());
        // ESC handler
        wrapper.addEventFilter(KeyEvent.KEY_PRESSED, ev -> { if(ev.getCode()== KeyCode.ESCAPE){ ev.consume(); pop(); } });
        // Animate in
        FadeTransition ft = new FadeTransition(Duration.millis(140), backdrop); ft.setFromValue(0); ft.setToValue(1); ft.play();
        // Push
        stack.push(wrapper); depth.set(stack.size());
        root.getChildren().add(wrapper);
        wrapper.requestFocus();
    }

    /** Push a sheet overlay with standard header and custom width */
    public void pushSheet(String title, Node content, double prefWidth) {
        // Backdrop
        StackPane backdrop = new StackPane();
        backdrop.getStyleClass().add("overlay-backdrop");
        backdrop.setPickOnBounds(true);
        // Header
        Label header = new Label(title);
        header.getStyleClass().add("overlay-title");
        Button back = new Button("Back");
        back.getStyleClass().add("back-btn");
        back.setOnAction(e -> pop());
        Button close = new Button("Close");
        close.getStyleClass().add("close-btn");
        close.setOnAction(e -> pop());
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox headerBox = new HBox(10, back, header, spacer, close);
        headerBox.getStyleClass().add("overlay-header");
        BorderPane sheet = new BorderPane();
        sheet.getStyleClass().add("overlay-sheet");
        sheet.setMaxWidth(prefWidth); // Set the max width
        VBox box = new VBox(12, headerBox, content);
        box.setPadding(new Insets(4, 0, 0, 0));
        sheet.setCenter(box);
        // Wrapper
        StackPane wrapper = new StackPane(backdrop, sheet);
        StackPane.setAlignment(sheet, Pos.CENTER);
        backdrop.setOnMouseClicked(e -> pop());
        // ESC handler
        wrapper.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) {
                ev.consume();
                pop();
            }
        });
        // Animate in
        FadeTransition ft = new FadeTransition(Duration.millis(140), backdrop);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        // Push
        stack.push(wrapper);
        depth.set(stack.size());
        root.getChildren().add(wrapper);
        wrapper.requestFocus();
    }

    /** Simple info */
    public void showInfo(String title, String message){
        Label body = new Label(message); body.setWrapText(true);
        Button ok = new Button("OK"); ok.getStyleClass().add("primary");
        HBox actions = new HBox(10, ok); actions.setAlignment(Pos.CENTER_RIGHT); actions.getStyleClass().add("overlay-actions");
        VBox content = new VBox(14, body, actions);
        pushSheet(title, content); ok.setOnAction(e -> pop());
    }

    /** Confirm sheet */
    public void showConfirm(String title, String message, String okText, String cancelText, Runnable onOk){
        Label body = new Label(message); body.setWrapText(true);
        Button ok = new Button(okText); ok.getStyleClass().add("success");
        Button cancel = new Button(cancelText); cancel.getStyleClass().add("warn");
        HBox actions = new HBox(10, cancel, ok); actions.setAlignment(Pos.CENTER_RIGHT); actions.getStyleClass().add("overlay-actions");
        VBox content = new VBox(14, body, actions);
        pushSheet(title, content);
        ok.setOnAction(e -> { if(onOk!=null) onOk.run(); pop(); });
        cancel.setOnAction(e -> pop());
    }

    /** Pop the top overlay if present */
    public void pop(){
        if(stack.isEmpty()) return;
        StackPane w = stack.pop(); depth.set(stack.size());
        root.getChildren().remove(w);
    }

    public IntegerProperty depthProperty(){ return depth; }
}
