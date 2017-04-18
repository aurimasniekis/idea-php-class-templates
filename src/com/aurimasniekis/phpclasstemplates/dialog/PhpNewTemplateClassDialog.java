package com.aurimasniekis.phpclasstemplates.dialog;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.FormBuilder;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.actions.PhpNewFileDialog;
import org.apache.velocity.runtime.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.jvm.hotspot.ui.Editor;
import sun.jvm.hotspot.utilities.HashtableEntry;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PhpNewTemplateClassDialog extends PhpNewClassDialog {
    protected EditorTextField myMessageTextField;
    protected ComboBox myKindComboBox;
    protected JLabel myKindUpDownHint;
    protected FormBuilder myTemplateAttributes;
    protected FileTemplate myCurrentTemplate;
    protected Hashtable<String, EditorTextField> myTemplateAttributesFields;

    public PhpNewTemplateClassDialog(@NotNull Project project, @Nullable PsiDirectory directory) {
        super(project, directory);
    }

    @Override
    protected void subInit() {
        super.subInit();

        this.myTemplateAttributes = new FormBuilder();
        this.myTemplateAttributesFields = new Hashtable<String, EditorTextField>();

        this.myKindUpDownHint = new JLabel();
        this.myKindUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
        this.myKindUpDownHint.setToolTipText(PhpBundle.message("actions.new.php.base.arrows.kind.tooltip"));


        this.myKindComboBox = new ComboBox<String>();
        this.myKindComboBox.setMinimumAndPreferredWidth(400);
        this.myKindComboBox.setRenderer(new ListCellRendererWrapper<Trinity>() {
            public void customize(JList list, Trinity value, int index, boolean selected, boolean hasFocus) {
                this.setText((String) value.first);
                this.setIcon((Icon) value.second);
            }
        });
        ComboboxSpeedSearch var10001 = new ComboboxSpeedSearch(this.myKindComboBox) {
            protected String getElementText(Object element) {
                return (String) ((Trinity) element).first;
            }
        };
        KeyboardShortcut up = new KeyboardShortcut(KeyStroke.getKeyStroke(38, 0), (KeyStroke) null);
        KeyboardShortcut down = new KeyboardShortcut(KeyStroke.getKeyStroke(40, 0), (KeyStroke) null);
        AnAction kindArrow = PhpNewFileDialog.getCbArrowAction(this.myKindComboBox);
        kindArrow.registerCustomShortcutSet(new CustomShortcutSet(new Shortcut[]{up, down}), this.myNameTextField);
        List<Trinity> availableTemplates = this.getAvailableTemplates();

        for (Trinity type : availableTemplates) {
            this.myKindComboBox.addItem(type);
        }

        this.myKindComboBox.addActionListener(e -> {
            this.updateTemplateAttributes();
        });

        this.updateTemplateAttributes();
    }

    private void updateTemplateAttributes() {
        FileTemplate template = (FileTemplate) ((Trinity) this.myKindComboBox.getSelectedItem()).getThird();

        if (template.equals(this.myCurrentTemplate)) {
            return;
        }

        this.myCurrentTemplate = template;
        this.myTemplateAttributes.getPanel().removeAll();
        this.myTemplateAttributesFields.clear();

        String[] attrs = new String[0];
        try {
            attrs = template.getUnsetAttributes(this.getProperties(this.getDirectory()), this.myProject);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        List<String> ignoredAttributes = Arrays.asList("PROJECT_NAME", "FILE_NAME", "NAME", "USER", "DATE", "TIME", "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "PRODUCT_NAME", "MONTH_NAME_SHORT", "MONTH_NAME_FULL", "NAME", "NAMESPACE", "CLASS_NAME", "STATIC", "TYPE_HINT", "PARAM_DOC", "THROWS_DOC", "DS", "CARET");
        for (String attribute : attrs) {

            if (ignoredAttributes.contains(attribute)) {
                continue;
            }

            EditorTextField field = new EditorTextField();

            this.myTemplateAttributesFields.put(attribute, field);
            this.myTemplateAttributes.addLabeledComponent(attribute.concat(":"), field);
        }

        this.myTemplateAttributes.getPanel().revalidate();
        this.myTemplateAttributes.getPanel().repaint();
    }

    @Override
    protected void buildForm() {
        this.setTitle("Create New PHP Class From Template");
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        JPanel namePanel = new JPanel(layout);
        namePanel.add(this.myNameTextField, BorderLayout.CENTER);
        namePanel.add(this.myKindUpDownHint, BorderLayout.EAST);

        this.myBuilder.addLabeledComponent(new JLabel("Name:"), namePanel);
        this.myBuilder.addLabeledComponent(new JLabel("Template:"), this.myKindComboBox);
        this.myBuilder.addComponent(this.myTemplateAttributes.getPanel());

        super.buildForm();
    }

    private List<Trinity> getAvailableTemplates() {
        List<Trinity> templates = new ArrayList<Trinity>();

        FileTemplate classTemplate = FileTemplateManager.getInstance(this.myProject).getInternalTemplate("PHP Class");

        for (FileTemplate template : FileTemplateManager.getInstance(this.myProject).getAllTemplates()) {
            if (template.getExtension().equals("class.php")) {
                templates.add(new Trinity(template.getName(), PhpIcons.CLASS, template));
            }
        }

        if (templates.size() < 1) {
            templates.add(new Trinity(classTemplate.getName(), PhpIcons.CLASS, classTemplate));
        }

        return templates;
    }

    @NotNull
    public Properties getProperties(@NotNull PsiDirectory directory) {
        super.getProperties(directory);

        return this.myProperties;
    }

    @Override
    protected void doOKAction() {
        Iterator<Map.Entry<String, EditorTextField>> it = this.myTemplateAttributesFields.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, EditorTextField> entry = it.next();

            this.myProperties.setProperty(entry.getKey(), entry.getValue().getText());
        }

        super.doOKAction();
    }

    @NotNull
    public String getTemplateName() {
        return (String) ((Trinity) this.myKindComboBox.getSelectedItem()).getFirst();
    }

    @NotNull
    public String getFileTemplate() {
        return (String) ((Trinity) this.myKindComboBox.getSelectedItem()).getThird();
    }
}
