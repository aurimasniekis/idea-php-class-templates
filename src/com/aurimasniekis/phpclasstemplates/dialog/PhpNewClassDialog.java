package com.aurimasniekis.phpclasstemplates.dialog;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorTextField;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.FormBuilder;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.actions.PhpNewFileDialog;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.refactoring.PhpNameUtil;
import com.jetbrains.php.roots.PhpDirectoryByPsrProvider;
import com.jetbrains.php.roots.PhpNamespaceCompositeProvider;
import com.jetbrains.php.roots.ui.PhpNamespaceComboBox;
import com.jetbrains.php.roots.ui.PhpPsrDirectoryComboBox;
import com.jetbrains.php.templates.PhpCreateFileFromTemplateDataProvider;
import com.jetbrains.php.templates.PhpFileTemplateUtil;
import com.jetbrains.php.templates.PhpTemplatesSettings;
import com.jetbrains.php.ui.PhpUiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.List;
import java.util.Properties;

abstract public class PhpNewClassDialog extends DialogWrapper implements PhpCreateFileFromTemplateDataProvider {
    protected FormBuilder myBuilder;

    protected Project myProject;
    protected PsiDirectory myDirectory;
    protected DocumentListener myNameFieldListener;
    protected final Alarm myAlarm;
    protected boolean myDisposed;

    protected EditorTextField myNameTextField;
    protected PhpNamespaceComboBox myNamespaceCombobox;
    protected PhpPsrDirectoryComboBox myDirectoryCombobox;
    protected EditorTextField myFileNameTextField;
    protected ComboBox myExtensionComboBox;
    protected Properties myProperties;

    protected JLabel myExtensionUpDownHint;

    public PhpNewClassDialog(@NotNull Project project, @Nullable PsiDirectory directory) {
        super(project);

        this.myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

        Disposer.register(this.getDisposable(), new Disposable() {
            public void dispose() {
                PhpNewClassDialog.this.myAlarm.cancelAllRequests();
                PhpNewClassDialog.this.myDisposed = true;
            }
        });

        this.myProperties = new Properties();

        this.myProject = project;
        this.myDirectory = directory;

        init();
    }

    protected void initMainFields() {
        // Setup Name Field
        this.myNameTextField = new EditorTextField();
        this.myNameTextField.setCaretPosition(0);

        // Setup Namespace Combobox
        this.myNamespaceCombobox = new PhpNamespaceComboBox(
                this.myProject,
                "",
                this.getDisposable()
        );

        this.myNamespaceCombobox.setEditable(true);
        this.myNamespaceCombobox.setMinimumAndPreferredWidth(400);

        this.myFileNameTextField = new EditorTextField();

        this.myDirectoryCombobox = new PhpPsrDirectoryComboBox(this.myProject) {
            public List<String> suggestDirectories(@NotNull String namespace) {
                return PhpDirectoryByPsrProvider.getSourceRootProvide().suggestDirectories(
                        PhpNewClassDialog.this.myProject,
                        namespace,
                        this.myBaseDir,
                        this.myBaseNamespace
                );
            }
        };


        if(this.myDirectory != null) {
            List<String> namespaces = PhpNamespaceCompositeProvider.INSTANCE.suggestNamespaces(this.myDirectory);

            String mainSuggestion = "";
            if (namespaces != null && !namespaces.isEmpty()) {
                mainSuggestion = (String)namespaces.get(0);
            }

            List<String> suggestions = null;
            if (namespaces != null && namespaces.size() > 1) {
                suggestions = namespaces.subList(1, namespaces.size());
            }

            this.myNamespaceCombobox.updateItems(mainSuggestion, suggestions);
            this.myDirectoryCombobox.init(
                    this.myDirectory.getVirtualFile(),
                    this.getNamespace()
            );
        } else {
            this.myDirectoryCombobox.init(
                    this.myProject.getBaseDir(),
                    ""
            );
        }

        this.myDirectoryCombobox.getComboBox().addFocusListener(
            new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    PhpNewClassDialog.this.myDirectoryCombobox.getComboBox().setEditable(true);
                }
            }
        );

        // File Extension Combobox
        this.myExtensionComboBox = new ComboBox();
        this.myExtensionComboBox.setMinimumAndPreferredWidth(400);

        String[] registeredExtensions = PhpFileTemplateUtil.getRegisteredPhpFileExtensions();
        this.myExtensionComboBox.setModel(new DefaultComboBoxModel<String>(registeredExtensions));
        int indexOfLast = ArrayUtil.indexOf(
                registeredExtensions,
                PhpTemplatesSettings.getInstance(this.myProject).NEW_PHP_CLASS_LAST_EXTENSION
        );

        if(indexOfLast > -1) {
            this.myExtensionComboBox.setSelectedIndex(indexOfLast);
        } else {
            this.myExtensionComboBox.setSelectedIndex(0);
        }


        this.myExtensionUpDownHint = new JLabel();
        this.myExtensionUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
        this.myExtensionUpDownHint.setToolTipText(PhpBundle.message("actions.new.php.base.arrows.extension.tooltip"));


        if(this.isFileNameAutoUpdate()) {
            this.myNameFieldListener = new DocumentAdapter() {
                public void documentChanged(DocumentEvent e) {
                    PhpNewClassDialog.this.addUpdateRequest(() -> {
                        PhpNewClassDialog.this.myFileNameTextField.setText(PhpNewClassDialog.this.getName());
                    }, 10);
                }
            };
            this.myNameTextField.getDocument().addDocumentListener(this.myNameFieldListener);
            this.myNamespaceCombobox.getEditorTextField().addDocumentListener(new DocumentAdapter() {
                public void documentChanged(DocumentEvent event) {
                    PhpNewClassDialog.this.addUpdateRequest(() -> {
                        PhpNewClassDialog.this.myDirectoryCombobox.updateDirectories(PhpNewClassDialog.this.getNamespace());
                    }, 10);
                }
            });
        }

        AnAction extensionArrow = PhpNewFileDialog.getCbArrowAction(this.myExtensionComboBox);
        KeyboardShortcut up = new KeyboardShortcut(KeyStroke.getKeyStroke(38, 0), (KeyStroke)null);
        KeyboardShortcut down = new KeyboardShortcut(KeyStroke.getKeyStroke(40, 0), (KeyStroke)null);
        extensionArrow.registerCustomShortcutSet(new CustomShortcutSet(new Shortcut[]{up, down}), this.myFileNameTextField);
    }

    protected void subInit() {
        this.myBuilder = new FormBuilder();
        this.myBuilder.setVerticalGap(5);
        this.initMainFields();
    }

    @Override
    protected void init() {
        this.subInit();

        this.buildForm();

        super.init();
    }

    protected void buildForm() {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        JPanel filenamePanel = new JPanel(layout);
        filenamePanel.add(this.myFileNameTextField, BorderLayout.CENTER);
        filenamePanel.add(this.myExtensionUpDownHint, BorderLayout.EAST);


        this.myBuilder.addLabeledComponent(new JLabel("Namespace:"), this.myNamespaceCombobox);
        this.myBuilder.addTooltip(
                PhpBundle.message(
                        "0.completion.shortcut",
                        "namespace", PhpUiUtil.getShortcutTextByActionName("CodeCompletion"))
        );
        this.myBuilder.addLabeledComponent(new JLabel("Filename:"), filenamePanel);
        this.myBuilder.addLabeledComponent(new JLabel("Directory:"), this.myDirectoryCombobox);
        this.myBuilder.addTooltip(
                PhpBundle.message(
                        "0.completion.shortcut",
                        "path", PhpUiUtil.getShortcutTextByActionName("CodeCompletion"))
        );
        this.myBuilder.addLabeledComponent(new JLabel("File Extension:"), this.myExtensionComboBox);
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return this.myNameTextField;
    }

    @Override
    protected JComponent createCenterPanel() {
        return this.myBuilder.getPanel();
    }

    @NotNull
    public String getFileName() {
        return this.getFileNameText().trim();
    }

    public String getFileNameText() {
        return this.myFileNameTextField.getText();
    }

    @NotNull
    protected String getName() {
        return this.myNameTextField.getText().trim();
    }

    protected void doOKAction() {
        PhpTemplatesSettings.getInstance(this.myProject).NEW_PHP_CLASS_LAST_EXTENSION = this.getExtension();
        super.doOKAction();
    }

    @NotNull
    public String getTemplateName() {
        return "PHP Class";
    }

    @NotNull
    protected String getExtension() {
        return (String)this.myExtensionComboBox.getSelectedItem();
    }

    @NotNull
    @Override
    public PsiDirectory getBaseDirectory() {
        return this.myDirectory;
    }

    public String getClassName() {
        return this.myNameTextField.getText();
    }

    public String getNamespaceName() {
        return this.getNamespace();
    }

    @NotNull
    protected final String getNamespace() {
        return PhpLangUtil.toName(this.myNamespaceCombobox.getSelectedNamespace().trim());
    }

    protected boolean isFileNameAutoUpdate() {
        return true;
    }

    protected void addUpdateRequest(@NotNull Runnable runnable) {
        this.addUpdateRequest(runnable, 100);
    }

    protected void addUpdateRequest(@NotNull Runnable runnable, int delay) {
        SwingUtilities.invokeLater(() -> {
            if(!this.myDisposed) {
                this.myAlarm.cancelAllRequests();
                this.myAlarm.addRequest(runnable, delay);
            }
        });
    }

    protected void dispose() {
        if(this.isFileNameAutoUpdate()) {
            this.myNameTextField.getDocument().removeDocumentListener(this.myNameFieldListener);
        }

        super.dispose();
    }

    @NotNull
    public final String getFilePath() {
        String chosenExtension = this.getExtension();
        String filename = PathUtil.toSystemIndependentName(this.getFileName());
        String extension = PhpNameUtil.getExtension(filename);
        String fullFileName = chosenExtension.equals(extension)?filename:PhpNameUtil.getFullFileName(filename, chosenExtension);
        String relativePath = this.myDirectoryCombobox.getRelativePath();
        return StringUtil.isEmpty(relativePath)?fullFileName:relativePath + "/" + StringUtil.trimEnd(fullFileName, "/");
    }

    @Nullable
    protected PsiDirectory getDirectory() {
        VirtualFile directory = this.myDirectoryCombobox.getExistingParent();
        if(directory != null) {
            PsiDirectory psiDirectory = PsiManager.getInstance(this.myProject).findDirectory(directory);
            if(psiDirectory != null) {
                return psiDirectory;
            }
        }

        return null;
    }

    @NotNull
    public Properties getProperties(@NotNull PsiDirectory directory) {

        this.myProperties.setProperty("NAME", this.getName());
        String namespace = this.getNamespace();
        if(StringUtil.isNotEmpty(namespace)) {
            this.myProperties.setProperty("NAMESPACE", namespace);
        }

        return this.myProperties;
    }

    protected boolean postponeValidation() {
        return true;
    }

    protected ValidationInfo doValidate() {
        String name = this.getName();
        if(!PhpNameUtil.isValidClassName(name)) {
            return new ValidationInfo(
                PhpBundle.message(
                    "validation.class.not.valid.name",
                        name),
                this.myNameTextField
            );
        } else {
            String namespace = this.getNamespace();
            if(StringUtil.isNotEmpty(namespace) && !PhpNameUtil.isValidNamespaceFullName(namespace)) {
                return new ValidationInfo(
                    PhpBundle.message(
                        "validation.namespace.not.valid.name",
                            namespace),
                    this.myNamespaceCombobox
                );
            } else {
                PsiDirectory directory = this.getDirectory();
                if(directory == null) {
                    return new ValidationInfo(
                        PhpBundle.message(
                            "validation.value.is.not.specified.or.invalid",
                                "directory")
                    );
                } else {
                    String errorMessage = this.isValidFilePath(this.getFilePath(), directory);
                    return errorMessage != null?new ValidationInfo(errorMessage, this.myFileNameTextField):null;
                }
            }
        }
    }

    protected String isValidFilePath(@NotNull String fullFilePath, @NotNull PsiDirectory baseDirectory) {
        String filePath = StringUtil.replace(
                fullFilePath,
                File.separator,
                "/"
        );

        if(filePath.length() == 0) {
            return PhpBundle.message(
                "validation.file.not.valid.name",
                    fullFilePath);
        } else {
            List<String> split = StringUtil.split(filePath, "/");
            if(split.size() == 0) {
                return PhpBundle.message(
                    "validation.file.not.valid.name",
                        fullFilePath);
            } else {
                for (String aSplit : split) {
                    if (!PhpNameUtil.isValidFileName(aSplit)) {
                        return PhpBundle.message(
                            "validation.file.not.valid.name",
                                fullFilePath);
                    }
                }

                VirtualFile baseDirectoryFile = baseDirectory.getVirtualFile();
                VirtualFile fileByRelativePath = baseDirectoryFile.findFileByRelativePath(
                        PhpNameUtil.trimStart(
                                filePath,
                                '/')
                );

                if (fileByRelativePath != null) {
                    return PhpBundle.message(
                        "validation.file.already.exists",
                            fullFilePath);
                } else {
                    return null;
                }
            }
        }
    }
}
