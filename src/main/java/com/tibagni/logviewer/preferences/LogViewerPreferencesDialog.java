package com.tibagni.logviewer.preferences;

import com.tibagni.logviewer.lookandfeel.LookNFeelProvider;
import com.tibagni.logviewer.lookandfeel.LookNFeel;
import com.tibagni.logviewer.util.JFileChooserExt;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogViewerPreferencesDialog extends JDialog {
  private static final String FILTER_PATH_PREF_ID = "filter_path";
  private static final String LAST_FILTER_OPEN_ID = "open_last_filter";
  private static final String LOG_PATH_PREF_ID = "log_path";
  private static final String LOOK_FEEL_PREF_ID = "look_and_feel";

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JComboBox lookAndFeelCbx;
  private JTextField filtersPathTxt;
  private JButton filtersPathBtn;
  private JCheckBox openLastFilterChbx;
  private JTextField logsPathTxt;
  private JButton logsPathBtn;

  private JFileChooser filterFolderChooser;
  private JFileChooser logsFolderChooser;
  private final LogViewerPreferences userPrefs;

  private Map<String, Runnable> saveActions = new HashMap<>();

  public LogViewerPreferencesDialog(JFrame owner) {
    super(owner);
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    userPrefs = LogViewerPreferences.getInstance();

    buttonOK.addActionListener(e -> onOK());
    buttonCancel.addActionListener(e -> onCancel());

    initFiltersPathPreference();
    initLogsPathPreference();
    initLookAndFeelPreference();

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void initFiltersPathPreference() {
    filterFolderChooser = new JFileChooserExt(userPrefs.getDefaultFiltersPath());
    filterFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    filtersPathBtn.addActionListener(e -> onSelectFilterPath());
    filtersPathTxt.setText(userPrefs.getDefaultFiltersPath().getAbsolutePath());

    openLastFilterChbx.addActionListener(e -> onOpenLastFilterChanged());
    openLastFilterChbx.setSelected(userPrefs.shouldOpenLastFilter());
  }

  private void initLogsPathPreference() {
    logsFolderChooser = new JFileChooserExt(userPrefs.getDefaultLogsPath());
    logsFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    logsPathBtn.addActionListener(e -> onSelectLogsPath());
    logsPathTxt.setText(userPrefs.getDefaultLogsPath().getAbsolutePath());
  }

  private void initLookAndFeelPreference() {
    LookNFeelProvider lnfProvider = LookNFeelProvider.getInstance();
    List<LookNFeel> lookAndFeels = lnfProvider.getAvailableLookNFeels();

    String currLnf = UIManager.getLookAndFeel().getName();
    LookNFeel selectedItem = lnfProvider.getBySystemName(currLnf);
    for (LookNFeel lnf : lookAndFeels) {
      lookAndFeelCbx.addItem(lnf);
    }

    if (selectedItem != null) {
      lookAndFeelCbx.setSelectedItem(selectedItem);
    }

    lookAndFeelCbx.addActionListener(l -> {
      LookNFeel lookNFeel = (LookNFeel) lookAndFeelCbx.getSelectedItem();
      saveActions.put(LOOK_FEEL_PREF_ID, () -> {
        String lnfClass = lookNFeel.getCls();
        userPrefs.setLookAndFeel(lnfClass);
      });
    });
  }

  private void onOK() {
    saveActions.forEach((s, runnable) -> runnable.run());
    dispose();
  }

  private void onCancel() {
    dispose();
  }

  private void onSelectFilterPath() {
    int selectedOption = filterFolderChooser.showOpenDialog(this);
    if (selectedOption == JFileChooser.APPROVE_OPTION) {
      File selectedFolder = filterFolderChooser.getSelectedFile();
      filtersPathTxt.setText(selectedFolder.getAbsolutePath());
      saveActions.put(FILTER_PATH_PREF_ID,
          () -> userPrefs.setDefaultFiltersPath(selectedFolder));
    }
  }

  private void onSelectLogsPath() {
    int selectedOption = logsFolderChooser.showOpenDialog(this);
    if (selectedOption == JFileChooser.APPROVE_OPTION) {
      File selectedFolder = logsFolderChooser.getSelectedFile();
      logsPathTxt.setText(selectedFolder.getAbsolutePath());
      saveActions.put(LOG_PATH_PREF_ID,
          () -> userPrefs.setDefaultLogsPath(selectedFolder));
    }
  }

  private void onOpenLastFilterChanged() {
    boolean isChecked = openLastFilterChbx.getModel().isSelected();
    saveActions.put(LAST_FILTER_OPEN_ID, () -> userPrefs.setOpenLastFilter(isChecked));
  }

  public static void showPreferencesDialog(JFrame parent) {
    LogViewerPreferencesDialog dialog = new LogViewerPreferencesDialog(parent);

    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }
}