package com.liftup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import com.liftup.models.Beneficiary;
import com.liftup.models.Opportunity;
import com.liftup.services.DataStore;
import com.liftup.services.MatchingService;
import com.liftup.services.SettingsService;
import com.liftup.services.WalletService;
import com.liftup.util.Exporter;
import com.liftup.util.IconProvider;
import com.liftup.util.Overlays;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    private final DataStore store = new DataStore("/com/liftup/views", "data/sample-data.json");
    private final SettingsService settings = new SettingsService();
    private final MatchingService matcher = new MatchingService();
    private final WalletService wallet = new WalletService();

    private final ObservableList<Beneficiary> beneficiaries = FXCollections.observableArrayList();
    private final ObservableList<Opportunity> opportunities = FXCollections.observableArrayList();
    private final ObservableList<Opportunity> matches = FXCollections.observableArrayList();

    private final Label statusLabel = new Label("Ready");
    private StackPane root;
    private Scene scene;
    private Overlays overlays;

    private String currentTheme = "light";
    private double fontScale = 1.0; // 1.0 = 16px base

    @Override
    public void start(Stage stage) {
        root = new StackPane(); overlays = new Overlays(root);
        scene = new Scene(root, 1400, 900);

        applyTheme(settings.getTheme());
        applyFontScale(settings.getFontScale());

        BorderPane shell = new BorderPane();
        ToolBar tb = new ToolBar();
        VBox titleBox = new VBox();
        Label title = new Label("LiftUp - Social Protection & Livelihoods"); title.getStyleClass().add("h1");
        Label subtitle = new Label("Register people / List opportunities / Match by skills / Send support (demo)"); subtitle.getStyleClass().add("subtitle");
        titleBox.getChildren().addAll(title, subtitle); titleBox.setSpacing(2);
        Pane spacer = new Pane(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Global Back button bound to overlay depth
        Button backBtn = btn("Back", "info", MaterialDesign.MDI_ARROW_LEFT);
        backBtn.disableProperty().bind(overlays.depthProperty().lessThanOrEqualTo(0));
        backBtn.setOnAction(e -> overlays.pop());

        Button aboutBtn = btn("About", "primary", MaterialDesign.MDI_INFORMATION_OUTLINE);
        aboutBtn.setOnAction(e -> showAbout());
        aboutBtn.setTooltip(new Tooltip("Show application information and guide"));

        Button sampleBtn = btn("Show Sample", "primary", MaterialDesign.MDI_DATABASE_PLUS);
        sampleBtn.setOnAction(e -> onLoadSample());
        sampleBtn.setTooltip(new Tooltip("Load sample data to explore features"));

        Button resetBtn = btn("Reset Demo", "warn", MaterialDesign.MDI_DELETE_SWEEP);
        resetBtn.setOnAction(e -> onReset());
        resetBtn.setTooltip(new Tooltip("Clear all local data and reset the application"));

        Button saveBtn = btn("Save", "success", MaterialDesign.MDI_CONTENT_SAVE);
        saveBtn.setOnAction(e -> {
            saveAll();
            toast("Saved");
        });
        saveBtn.setTooltip(new Tooltip("Save all beneficiaries and opportunities (Ctrl+S)"));

        ToggleButton themeTgl = tglBtn("Dark Mode", "primary", MaterialDesign.MDI_THEME_LIGHT_DARK);
        themeTgl.setOnAction(e -> {
            toggleTheme(themeTgl.isSelected());
        });
        themeTgl.setTooltip(new Tooltip("Toggle between light and dark themes"));

        Button fsBtn = btn("Full Screen", "primary", MaterialDesign.MDI_FULLSCREEN);
        fsBtn.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        fsBtn.setTooltip(new Tooltip("Toggle full screen mode (F11)"));

        HBox zoomBox = new HBox(6);
        Button zoomOut = btn("A-", "info", MaterialDesign.MDI_MINUS);
        zoomOut.setOnAction(e -> adjustFont(-0.1));
        zoomOut.setTooltip(new Tooltip("Decrease font size (Ctrl+-)"));

        Button zoomReset = btn("A", "info", MaterialDesign.MDI_FORMAT_SIZE);
        zoomReset.setOnAction(e -> setFontScale(1.0));
        zoomReset.setTooltip(new Tooltip("Reset font size (Ctrl+0)"));

        Button zoomIn = btn("A+", "info", MaterialDesign.MDI_PLUS);
        zoomIn.setOnAction(e -> adjustFont(0.1));
        zoomIn.setTooltip(new Tooltip("Increase font size (Ctrl+=)"));
        zoomBox.getChildren().addAll(zoomOut, zoomReset, zoomIn);

        tb.getItems().addAll(titleBox, spacer, backBtn, aboutBtn, sampleBtn, resetBtn, saveBtn, new Separator(),
                themeTgl, fsBtn, zoomBox);
        shell.setTop(tb);

        TabPane tabs = new TabPane(); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(buildBeneficiariesTab());
        tabs.getTabs().add(buildOpportunitiesTab());
        tabs.getTabs().add(buildMatchWalletTab());
        tabs.getTabs().add(buildInsightsTab());
        shell.setCenter(tabs);

        HBox status = new HBox(statusLabel); status.getStyleClass().add("statusbar"); statusLabel.getStyleClass().add("status");
        shell.setBottom(status);

        root.getChildren().add(shell);

        // Shortcuts
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), this::saveAll);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), () -> overlays.pop());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F11), () -> stage.setFullScreen(!stage.isFullScreen()));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN), () -> adjustFont(0.1));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN), () -> adjustFont(-0.1));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN), () -> setFontScale(1.0));

        loadData();

        stage.setTitle("LiftUp - No Poverty (SDG 1)");
        stage.setScene(scene);
        stage.setFullScreenExitHint("Press F11 to exit Full Screen");
        stage.setMaximized(true);
        stage.show();

        toast("Welcome! Click Help for a quick guide.");
    }

    private void applyTheme(String theme){
        currentTheme = (theme==null||theme.isBlank())?"light":theme;
        scene.getStylesheets().clear();
        if("dark".equals(currentTheme)) scene.getStylesheets().add(getClass().getResource("/com/liftup/views/theme-dark.css").toExternalForm());
        else scene.getStylesheets().add(getClass().getResource("/com/liftup/views/theme-light.css").toExternalForm());
    }
    private void toggleTheme(boolean dark){ applyTheme(dark?"dark":"light"); }
    private void applyFontScale(double scale){ fontScale = Math.max(0.8, Math.min(1.8, scale)); scene.getRoot().setStyle("-fx-font-size: "+(int)(16*fontScale)+"px;"); }
    private void adjustFont(double delta){ setFontScale(fontScale+delta); }
    private void setFontScale(double value){ applyFontScale(value); }
    private Button btn(String text, String style){ Button b = new Button(text); b.getStyleClass().add(style); return b; }
    private Button btn(String text, String style, MaterialDesign icon){ Button b = btn(text, style); b.setGraphic(IconProvider.getIcon(icon)); return b; }
    private ToggleButton tglBtn(String text, String style, MaterialDesign icon){ ToggleButton b = new ToggleButton(text); b.getStyleClass().add(style); b.setGraphic(IconProvider.getIcon(icon)); return b; }

    private void showAbout() {
        String title = "About LiftUp: A Vision for a World Without Poverty";

        Accordion accordion = new Accordion();

        TitledPane visionPane = new TitledPane("Our Vision", createAboutSection(
            "To create a world where every person has the opportunity to achieve economic independence and live a life of dignity."
        ));

        TitledPane beneficiaryPane = new TitledPane("Who is a Beneficiary?", createAboutSection(
            "A 'Beneficiary' is an individual or family we aim to support. In this app, you can register them, noting their skills and household size to help us find the best opportunities for them."
        ));

        TitledPane opportunityPane = new TitledPane("What is an Opportunity?", createAboutSection(
            "An 'Opportunity' is a job, training program, or any other chance for a beneficiary to improve their livelihood. Each opportunity has required skills and a potential payout."
        ));

        TitledPane matchingPane = new TitledPane("How Matching Works", createAboutSection(
            "Our smart system connects people to opportunities. On the 'Match & Support' tab, you can select a beneficiary, and the app will find the most suitable opportunities based on their skills."
        ));
        
        TitledPane goalPane = new TitledPane("Our Goal", createAboutSection(
            "By connecting people with opportunities, we aim to build a self-sustaining cycle of empowerment. Every match made is a step towards lifting a community out of poverty."
        ));

        accordion.getPanes().addAll(visionPane, beneficiaryPane, opportunityPane, matchingPane, goalPane);
        accordion.setExpandedPane(visionPane);

        ScrollPane sp = new ScrollPane(accordion);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        overlays.pushSheet(title, sp, 600);
    }

    private VBox createAboutSection(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(550);
        VBox layout = new VBox(label);
        layout.setPadding(new Insets(10));
        return layout;
    }

    private void onLoadSample(){
        Label body = new Label("Append adds samples to what you have. Replace clears current data first.");
        Button bAppend = btn("Append", "primary", MaterialDesign.MDI_PLUS_BOX);
        Button bReplace = btn("Replace", "warn", MaterialDesign.MDI_REFRESH);
        HBox actions = new HBox(10, bReplace, bAppend); actions.setAlignment(Pos.CENTER_RIGHT);
        VBox content = new VBox(12, body, actions);
        bAppend.setOnAction(e -> { mergeSample(false); overlays.pop(); });
        bReplace.setOnAction(e -> { mergeSample(true); overlays.pop(); });
        overlays.pushSheet("Load Sample Data", content);
    }

    private void mergeSample(boolean replace){
        try{
            List<Beneficiary> sb = store.loadBeneficiaries(true);
            List<Opportunity> so = store.loadOpportunities(true);
            if(replace){ beneficiaries.clear(); opportunities.clear(); }
            Set<String> bIds = beneficiaries.stream().map(Beneficiary::getId).collect(Collectors.toSet());
            for(Beneficiary b: sb){ if(!bIds.contains(b.getId())) beneficiaries.add(b); }
            Set<String> oIds = opportunities.stream().map(Opportunity::getId).collect(Collectors.toSet());
            for(Opportunity o: so){ if(!oIds.contains(o.getId())) opportunities.add(o); }
            toast((replace?"Replaced with":"Appended") + " sample data");
        }catch(Exception ex){
            System.err.println("Could not load sample data: " + ex.getMessage());
            alert("Could not load sample data");
        }
    }

    private void onReset(){
        overlays.showConfirm("Reset Demo", "This clears saved data on this computer. Continue?", "Yes, reset", "Cancel", () -> {
            store.reset(); beneficiaries.clear(); opportunities.clear(); matches.clear(); toast("Demo reset. Click 'Show Sample'.");
        });
    }

    private void toast(String msg){
        Label t = new Label(msg); t.getStyleClass().add("toast");
        StackPane.setAlignment(t, Pos.TOP_CENTER); root.getChildren().add(t);
        FadeTransition ft = new FadeTransition(Duration.millis(2000), t); ft.setFromValue(0); ft.setToValue(1); ft.setAutoReverse(true); ft.setCycleCount(2); ft.setOnFinished(e -> root.getChildren().remove(t)); ft.play();
        statusLabel.setText(msg);
    }

    private void alert(String msg){ overlays.showInfo("Error", msg); }

    private void saveAll(){ store.saveBeneficiaries(beneficiaries); store.saveOpportunities(opportunities); }

    private Tab buildBeneficiariesTab(){
        TextField search = new TextField(); search.setPromptText("Filter by name or skill (Ctrl/Cmd+F)"); search.setTooltip(new Tooltip("Type any name or skill to filter"));
        TableView<Beneficiary> table = new TableView<>(); table.setEditable(true); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Beneficiary, String> nameCol = new TableColumn<>("Name"); nameCol.setCellValueFactory(c -> c.getValue().nameProperty()); nameCol.setCellFactory(TextFieldTableCell.forTableColumn()); nameCol.setOnEditCommit(ev -> ev.getRowValue().nameProperty().set(ev.getNewValue()));
        TableColumn<Beneficiary, Number> hhCol = new TableColumn<>("Household"); hhCol.setCellValueFactory(c -> c.getValue().householdSizeProperty());
        TableColumn<Beneficiary, String> skillsCol = new TableColumn<>("Skills"); skillsCol.setCellValueFactory(c -> c.getValue().skillsCsvProperty()); skillsCol.setCellFactory(TextFieldTableCell.forTableColumn()); skillsCol.setOnEditCommit(ev -> { List<String> s = Arrays.stream(ev.getNewValue().split(",")).map(String::trim).filter(x->!x.isEmpty()).toList(); ev.getRowValue().getSkills().clear(); ev.getRowValue().getSkills().addAll(s); });
        TableColumn<Beneficiary, Number> scoreCol = new TableColumn<>("Score"); scoreCol.setCellValueFactory(c -> c.getValue().scoreProperty());
        table.getColumns().addAll(Arrays.asList(nameCol, hhCol, skillsCol, scoreCol));

        FilteredList<Beneficiary> filtered = new FilteredList<>(beneficiaries, b -> true);
        search.textProperty().addListener((obs, o, n) -> { String q = n==null?"":n.toLowerCase(); filtered.setPredicate(b -> b.nameProperty().get().toLowerCase().contains(q) || String.join(", ", b.getSkills()).toLowerCase().contains(q)); });
        SortedList<Beneficiary> sorted = new SortedList<>(filtered); sorted.comparatorProperty().bind(table.comparatorProperty()); table.setItems(sorted);

        VBox card = new VBox(10); card.getStyleClass().add("card"); Label h = new Label("Add Beneficiary"); h.getStyleClass().add("h2");
        TextField bName = new TextField(); bName.setPromptText("Full name"); TextField bHouse = new TextField(); bHouse.setPromptText("Household size (>=1)"); TextField bSkills = new TextField(); bSkills.setPromptText("e.g., cooking, sewing");
        Button add = btn("Add", "success", MaterialDesign.MDI_ACCOUNT_PLUS);
        add.setDefaultButton(true);
        add.setOnAction(e -> {
            try {
                String n = bName.getText().trim();
                if (n.isEmpty()) {
                    alert("Please enter a name.");
                    return;
                }
                int hh = Integer.parseInt(bHouse.getText().trim());
                if (hh < 1) {
                    alert("Household size must be at least 1.");
                    return;
                }
                List<String> s = Arrays.stream(bSkills.getText().split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
                beneficiaries.add(new Beneficiary(UUID.randomUUID().toString(), n, hh, s));
                toast("Beneficiary added");
                bName.clear();
                bHouse.clear();
                bSkills.clear();
            } catch (NumberFormatException ex) {
                alert("Household size must be a valid number.");
            }
        });
        Button del = btn("Delete Selected", "warn", MaterialDesign.MDI_ACCOUNT_MINUS);
        del.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));
        del.setOnAction(e -> {
            Beneficiary b = table.getSelectionModel().getSelectedItem();
            if (b != null) {
                beneficiaries.remove(b);
                toast("Beneficiary removed");
            }
        });
        Button export = btn("Export CSV", "primary", MaterialDesign.MDI_EXPORT);
        export.setOnAction(e -> {
            try {
                String path = Exporter.exportBeneficiariesCSV(new ArrayList<>(beneficiaries));
                toast("Exported to " + path);
            } catch (java.io.IOException ex) {
                alert("Export failed");
            }
        });
        GridPane gp = new GridPane(); gp.setHgap(10); gp.setVgap(8); gp.addRow(0, new Label("Name:"), bName); gp.addRow(1, new Label("Household:"), bHouse); gp.addRow(2, new Label("Skills:"), bSkills); gp.add(new HBox(8, add, del), 1, 3);
        card.getChildren().addAll(h, gp);
        HBox topRow = new HBox(12, new Label("Filter:"), search, export); topRow.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(search, Priority.SOMETIMES);
        HBox row = new HBox(12, table, card); HBox.setHgrow(table, Priority.ALWAYS); VBox.setVgrow(table, Priority.ALWAYS);
        VBox box = new VBox(12, topRow, row); box.setPadding(new Insets(16));
        return new Tab("Beneficiaries", box);
    }

    private Tab buildOpportunitiesTab(){
        TextField search = new TextField(); search.setPromptText("Filter by title or skill");
        TableView<Opportunity> table = new TableView<>(); table.setEditable(true); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ms-MY"));
        TableColumn<Opportunity, String> tCol = new TableColumn<>("Title"); tCol.setCellValueFactory(c -> c.getValue().titleProperty()); tCol.setCellFactory(TextFieldTableCell.forTableColumn()); tCol.setOnEditCommit(ev -> ev.getRowValue().titleProperty().set(ev.getNewValue()));
        TableColumn<Opportunity, String> rCol = new TableColumn<>("Required Skills"); rCol.setCellValueFactory(c -> c.getValue().requiredSkillsCsvProperty()); rCol.setCellFactory(TextFieldTableCell.forTableColumn()); rCol.setOnEditCommit(ev -> { List<String> s = Arrays.stream(ev.getNewValue().split(",")).map(String::trim).filter(x->!x.isEmpty()).toList(); ev.getRowValue().getRequiredSkills().clear(); ev.getRowValue().getRequiredSkills().addAll(s); });
        TableColumn<Opportunity, String> pCol = new TableColumn<>("Payout"); pCol.setCellValueFactory(c -> new SimpleStringProperty(currency.format(c.getValue().payoutProperty().get())));
        table.getColumns().addAll(Arrays.asList(tCol, rCol, pCol));

        FilteredList<Opportunity> filtered = new FilteredList<>(opportunities, o -> true);
        search.textProperty().addListener((obs,o,n) -> { String q=n==null?"":n.toLowerCase(); filtered.setPredicate(op -> op.titleProperty().get().toLowerCase().contains(q) || String.join(", ", op.getRequiredSkills()).toLowerCase().contains(q)); });
        SortedList<Opportunity> sorted = new SortedList<>(filtered); sorted.comparatorProperty().bind(table.comparatorProperty()); table.setItems(sorted);

        VBox card = new VBox(10); card.getStyleClass().add("card"); Label h = new Label("Add Opportunity"); h.getStyleClass().add("h2");
        TextField oTitle = new TextField(); oTitle.setPromptText("Title"); TextField oSkills = new TextField(); oSkills.setPromptText("e.g., data entry, typing"); TextField oPayout = new TextField(); oPayout.setPromptText("Payout (MYR)");
        Button add = btn("Add", "success", MaterialDesign.MDI_PLUS);
        add.setDefaultButton(true);
        add.setOnAction(e -> {
            try {
                String tt = oTitle.getText().trim();
                if (tt.isEmpty()) {
                    alert("Please enter a title.");
                    return;
                }
                double pay = Double.parseDouble(oPayout.getText().trim());
                if (pay <= 0) {
                    alert("Payout must be a positive number.");
                    return;
                }
                List<String> s = Arrays.stream(oSkills.getText().split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
                opportunities.add(new Opportunity(UUID.randomUUID().toString(), tt, s, pay));
                toast("Opportunity added");
                oTitle.clear();
                oSkills.clear();
                oPayout.clear();
            } catch (NumberFormatException ex) {
                alert("Payout must be a valid number.");
            }
        });
        Button del = btn("Delete Selected", "warn", MaterialDesign.MDI_MINUS);
        del.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));
        del.setOnAction(e -> {
            Opportunity o = table.getSelectionModel().getSelectedItem();
            if (o != null) {
                opportunities.remove(o);
                toast("Opportunity removed");
            }
        });
        Button export = btn("Export CSV", "primary", MaterialDesign.MDI_EXPORT);
        export.setOnAction(e -> {
            try {
                String path = Exporter.exportOpportunitiesCSV(new ArrayList<>(opportunities));
                toast("Exported to " + path);
            } catch (java.io.IOException ex) {
                alert("Export failed");
            }
        });

        GridPane gp = new GridPane(); gp.setHgap(10); gp.setVgap(8); gp.addRow(0, new Label("Title:"), oTitle); gp.addRow(1, new Label("Required Skills:"), oSkills); gp.addRow(2, new Label("Payout (MYR):"), oPayout); gp.add(new HBox(8, add, del), 1, 3);
        card.getChildren().addAll(h, gp);

        HBox topRow = new HBox(12, new Label("Filter:"), search, export); topRow.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(search, Priority.SOMETIMES);
        HBox row = new HBox(12, table, card); HBox.setHgrow(table, Priority.ALWAYS); VBox box = new VBox(12, topRow, row); box.setPadding(new Insets(16)); VBox.setVgrow(table, Priority.ALWAYS);
        return new Tab("Opportunities", box);
    }

    private Tab buildMatchWalletTab() {
        // --- Main Container ---
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(16));

        // --- 1. Beneficiary Selection ---
        VBox selectionPane = new VBox(10);
        Label selectionHeader = new Label("Step 1: Select a Beneficiary");
        selectionHeader.getStyleClass().add("h2");
        ComboBox<Beneficiary> picker = new ComboBox<>(beneficiaries);
        picker.setPromptText("Choose a beneficiary to begin...");
        picker.setPrefWidth(400);
        selectionPane.getChildren().addAll(selectionHeader, picker);

        // --- 2. Matching Pane ---
        VBox matchingPane = new VBox(12);
        Label matchingHeader = new Label("Step 2: Find Opportunity Matches");
        matchingHeader.getStyleClass().add("h3");

        // Matching Controls
        Slider overlap = new Slider(0, 5, 1);
        overlap.setShowTickMarks(true);
        overlap.setShowTickLabels(true);
        overlap.setMajorTickUnit(1);
        overlap.setMinorTickCount(0);
        overlap.setSnapToTicks(true);
        Label overlapLbl = new Label();
        overlapLbl.textProperty().bind(Bindings.format("Minimum Skills in Common: %.0f", overlap.valueProperty()));
        Button matchBtn = btn("Find Matches", "primary", MaterialDesign.MDI_ACCOUNT_SEARCH);

        GridPane matchingGrid = new GridPane();
        matchingGrid.setHgap(10);
        matchingGrid.setVgap(10);
        matchingGrid.add(overlapLbl, 0, 0);
        matchingGrid.add(overlap, 1, 0);
        matchingGrid.add(matchBtn, 1, 1);
        GridPane.setHgrow(overlap, Priority.ALWAYS);
        GridPane.setHalignment(matchBtn, javafx.geometry.HPos.RIGHT);

        // Matching Results Table
        TableView<Opportunity> table = new TableView<>(matches);
        table.setPlaceholder(new Label("Matching results will appear here."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Opportunity, String> tCol = new TableColumn<>("Matched Opportunity");
        tCol.setCellValueFactory(c -> c.getValue().titleProperty());
        TableColumn<Opportunity, String> rCol = new TableColumn<>("Required Skills");
        rCol.setCellValueFactory(c -> c.getValue().requiredSkillsCsvProperty());
        TableColumn<Opportunity, Number> pCol = new TableColumn<>("Payout");
        pCol.setCellValueFactory(c -> c.getValue().payoutProperty());
        table.getColumns().addAll(Arrays.asList(tCol, rCol, pCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        matchingPane.getChildren().addAll(matchingHeader, matchingGrid, table);
        
        // --- 3. Wallet Pane ---
        VBox walletPane = new VBox(12);
        Label walletHeader = new Label("Step 3: Provide Direct Support");
        walletHeader.getStyleClass().add("h3");
        Label walletDesc = new Label();
        walletDesc.setWrapText(true);

        // Wallet Controls
        TextField fromField = new TextField();
        fromField.setPromptText("e.g., 'Community Fund'");
        TextField amount = new TextField();
        amount.setPromptText("e.g., 150.00");
        Button sendBtn = btn("Send Support", "success", MaterialDesign.MDI_SEND);

        GridPane walletGrid = new GridPane();
        walletGrid.setHgap(10);
        walletGrid.setVgap(8);
        walletGrid.addRow(0, new Label("From (Donor):"), fromField);
        walletGrid.addRow(1, new Label("Amount (MYR):"), amount);
        walletGrid.add(sendBtn, 1, 2);
        GridPane.setHalignment(sendBtn, javafx.geometry.HPos.RIGHT);

        walletPane.getChildren().addAll(walletHeader, walletDesc, walletGrid);

        // --- Event Handling & Logic ---
        matchingPane.setDisable(true);
        walletPane.setDisable(true);

        picker.valueProperty().addListener((obs, old, val) -> {
            boolean disabled = (val == null);
            matchingPane.setDisable(disabled);
            walletPane.setDisable(disabled);
            matches.clear();
            if (val != null) {
                walletDesc.setText("You are about to send support to " + val.nameProperty().get() + ". This is a demo; no real money will be transferred.");
            }
        });

        matchBtn.setOnAction(e -> {
            Beneficiary b = picker.getValue();
            List<Opportunity> out = matcher.match(b, new ArrayList<>(opportunities));
            int min = (int) overlap.getValue();
            matches.setAll(out.stream().filter(o -> overlapCount(b, o) >= min).collect(Collectors.toList()));
            toast("Found " + matches.size() + " matches for " + b.nameProperty().get());
        });

        sendBtn.setOnAction(e -> {
            try {
                String from = fromField.getText().trim();
                if (from.isEmpty()) {
                    alert("Please enter a donor name or source.");
                    return;
                }
                String toId = picker.getValue().getId();
                double amt = Double.parseDouble(amount.getText().trim());
                if (amt <= 0) {
                    alert("Amount must be a positive number.");
                    return;
                }
                String msg = wallet.transfer(from, toId, amt);
                overlays.showInfo("Transfer Successful", msg);
                amount.clear();
                fromField.clear();
                toast("Transfer simulated successfully.");
            } catch (NumberFormatException ex) {
                alert("Amount must be a valid number.");
            } catch (Exception ex) {
                System.err.println("Transfer failed: " + ex.getMessage());
                alert("Transfer failed unexpectedly.");
            }
        });

        // --- ASSEMBLY ---
        mainLayout.getChildren().addAll(selectionPane, new Separator(javafx.geometry.Orientation.HORIZONTAL), matchingPane, new Separator(javafx.geometry.Orientation.HORIZONTAL), walletPane);
        return new Tab("Match & Support", mainLayout);
    }

    private int overlapCount(Beneficiary b, Opportunity o){ int c=0; for(String s: o.getRequiredSkills()) for(String t: b.getSkills()) if(s.equalsIgnoreCase(t)) c++; return c; }

    private Tab buildInsightsTab() {
        // --- 1. KPI TILES ---
        TilePane kpiPane = new TilePane(12, 12);
        kpiPane.setPrefColumns(4);

        Label totalBeneficiaries = new Label();
        totalBeneficiaries.textProperty().bind(Bindings.size(beneficiaries).asString());
        Label totalOpportunities = new Label();
        totalOpportunities.textProperty().bind(Bindings.size(opportunities).asString());

        Label avgHouseholdSize = new Label();
        avgHouseholdSize.textProperty().bind(Bindings.createStringBinding(() -> 
            String.format("%.1f", beneficiaries.stream().mapToInt(b -> b.householdSizeProperty().get()).average().orElse(0)),
            beneficiaries
        ));

        Label totalPayout = new Label();
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ms-MY"));
        totalPayout.textProperty().bind(Bindings.createStringBinding(() -> 
            currency.format(opportunities.stream().mapToDouble(o -> o.payoutProperty().get()).sum()),
            opportunities
        ));

        kpiPane.getChildren().addAll(
            createKpiCard("Total Beneficiaries", totalBeneficiaries),
            createKpiCard("Total Opportunities", totalOpportunities),
            createKpiCard("Avg. Household Size", avgHouseholdSize),
            createKpiCard("Total Opportunity Value", totalPayout)
        );

        // --- 2. CHARTS ---
        // Pie Chart: Top 5 Required Skills
        PieChart skillsPieChart = new PieChart();
        skillsPieChart.setTitle("Top 5 In-Demand Skills");
        skillsPieChart.setLegendVisible(true);
        skillsPieChart.setLegendSide(javafx.geometry.Side.RIGHT);

        opportunities.addListener((javafx.collections.ListChangeListener.Change<? extends Opportunity> c) -> {
            Map<String, Long> skillCounts = opportunities.stream()
                .flatMap(o -> o.getRequiredSkills().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            List<PieChart.Data> pieData = skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            
            skillsPieChart.setData(FXCollections.observableArrayList(pieData));
        });


        // Bar Chart: Top 5 Highest Payout Opportunities
        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Payout (MYR)");
        BarChart<Number, String> payoutBarChart = new BarChart<>(xAxis, yAxis);
        payoutBarChart.setTitle("Top 5 Highest Payout Opportunities");
        payoutBarChart.setLegendVisible(false);

        opportunities.addListener((javafx.collections.ListChangeListener.Change<? extends Opportunity> c) -> {
            XYChart.Series<Number, String> series = new XYChart.Series<>();
            opportunities.stream()
                .sorted(Comparator.comparingDouble(o -> -o.payoutProperty().get()))
                .limit(5)
                .forEach(o -> series.getData().add(new XYChart.Data<>(o.payoutProperty().get(), o.titleProperty().get())));
            payoutBarChart.setData(FXCollections.observableArrayList(series));
        });

        // Bar Chart: Beneficiaries by Skill Count
        CategoryAxis skillCountXAxis = new CategoryAxis();
        skillCountXAxis.setLabel("Number of Skills");
        NumberAxis skillCountYAxis = new NumberAxis();
        skillCountYAxis.setLabel("Number of Beneficiaries");
        skillCountYAxis.setTickUnit(1);
        skillCountYAxis.setMinorTickCount(0);

        BarChart<String, Number> skillCountBarChart = new BarChart<>(skillCountXAxis, skillCountYAxis);
        skillCountBarChart.setTitle("Beneficiary Skill Distribution");
        skillCountBarChart.setLegendVisible(false);

        beneficiaries.addListener((javafx.collections.ListChangeListener.Change<? extends Beneficiary> c) -> {
            Map<Integer, Long> skillCounts = beneficiaries.stream()
                .collect(Collectors.groupingBy(b -> b.getSkills().size(), Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Beneficiaries");

            skillCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String category = entry.getKey() + (entry.getKey() == 1 ? " Skill" : " Skills");
                    series.getData().add(new XYChart.Data<>(category, entry.getValue()));
                });

            skillCountBarChart.setData(FXCollections.observableArrayList(series));
        });


        // --- ASSEMBLY ---
        GridPane chartsPane = new GridPane();
        chartsPane.setHgap(20);
        chartsPane.setVgap(20);
        chartsPane.add(skillsPieChart, 0, 0);
        chartsPane.add(payoutBarChart, 1, 0);
        chartsPane.add(skillCountBarChart, 0, 1, 2, 1); // Span 2 columns
        GridPane.setHgrow(skillsPieChart, Priority.ALWAYS);
        GridPane.setHgrow(payoutBarChart, Priority.ALWAYS);
        GridPane.setHgrow(skillCountBarChart, Priority.ALWAYS);
        VBox.setVgrow(chartsPane, Priority.ALWAYS);

        VBox mainContent = new VBox(20, kpiPane, chartsPane);
        mainContent.setPadding(new Insets(16));
        
        ScrollPane sp = new ScrollPane(mainContent);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        return new Tab("Dashboard", sp);
    }

    private VBox createKpiCard(String title, Label valueLabel) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kpi-title");

        valueLabel.getStyleClass().add("kpi-value");

        FontIcon icon = switch (title) {
            case "Total Beneficiaries" -> IconProvider.getIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE);
            case "Total Opportunities" -> IconProvider.getIcon(MaterialDesign.MDI_BRIEFCASE);
            case "Avg. Household Size" -> IconProvider.getIcon(MaterialDesign.MDI_HOME_OUTLINE);
            case "Total Opportunity Value" -> IconProvider.getIcon(MaterialDesign.MDI_CURRENCY_USD);
            default -> null;
        };

        if (icon != null) {
            icon.getStyleClass().add("kpi-icon");
            HBox titleRow = new HBox(8, icon, titleLabel);
            titleRow.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().addAll(titleRow, valueLabel);
        } else {
            card.getChildren().addAll(titleLabel, valueLabel);
        }

        return card;
    }

    private void loadData(){
        beneficiaries.addAll(store.loadBeneficiaries(false));
        opportunities.addAll(store.loadOpportunities(false));
    }

    public static void main(String[] args){ launch(args); }
}
