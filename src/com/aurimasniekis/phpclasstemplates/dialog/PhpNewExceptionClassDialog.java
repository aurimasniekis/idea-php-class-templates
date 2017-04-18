package com.aurimasniekis.phpclasstemplates.dialog;

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
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.actions.PhpNewFileDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PhpNewExceptionClassDialog extends PhpNewClassDialog {
    protected EditorTextField myMessageTextField;
    protected ComboBox myKindComboBox;
    protected JLabel myKindUpDownHint;

    public PhpNewExceptionClassDialog(@NotNull Project project, @Nullable PsiDirectory directory) {
        super(project, directory);
    }

    @Override
    protected void subInit() {
        super.subInit();

        this.myMessageTextField = new EditorTextField("");
        this.myKindUpDownHint      = new JLabel();
        this.myKindUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
        this.myKindUpDownHint.setToolTipText(PhpBundle.message("actions.new.php.base.arrows.kind.tooltip"));


        this.myKindComboBox = new ComboBox<String>();
        this.myKindComboBox.setMinimumAndPreferredWidth(400);
        this.myKindComboBox.setRenderer(new ListCellRendererWrapper<Trinity>() {
            public void customize(JList list, Trinity value, int index, boolean selected, boolean hasFocus) {
                this.setText((String)value.first);
                this.setIcon((Icon)value.second);
            }
        });
        ComboboxSpeedSearch var10001 = new ComboboxSpeedSearch(this.myKindComboBox) {
            protected String getElementText(Object element) {
                return (String)((Trinity)element).first;
            }
        };
        KeyboardShortcut up = new KeyboardShortcut(KeyStroke.getKeyStroke(38, 0), (KeyStroke)null);
        KeyboardShortcut down = new KeyboardShortcut(KeyStroke.getKeyStroke(40, 0), (KeyStroke)null);
        AnAction kindArrow = PhpNewFileDialog.getCbArrowAction(this.myKindComboBox);
        kindArrow.registerCustomShortcutSet(new CustomShortcutSet(new Shortcut[]{up, down}), this.myNameTextField);
        List<Trinity> exceptionTypes = this.getExceptionTypes();

        for(Trinity type : exceptionTypes) {
            this.myKindComboBox.addItem(type);
        }
    }

    @Override
    protected void buildForm() {
        this.setTitle("Create New PHP Exception Class");
        this.myNameTextField.setText("Exception");
        this.myFileNameTextField.setText("Exception");
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        JPanel namePanel = new JPanel(layout);
        namePanel.add(this.myNameTextField, BorderLayout.CENTER);
        namePanel.add(this.myKindUpDownHint, BorderLayout.EAST);

        this.myBuilder.addLabeledComponent(new JLabel("Name:"), namePanel);
        this.myBuilder.addLabeledComponent(new JLabel("Type:"), this.myKindComboBox);
        this.myBuilder.addTooltip("Exeception class to extend");
        this.myBuilder.addLabeledComponent(new JLabel("Message:"), this.myMessageTextField);
        this.myBuilder.addTooltip("Enter message to create constructor with specified message");


        super.buildForm();
    }

    private List<Trinity> getExceptionTypes() {
        List<Trinity> types = new ArrayList<Trinity>();

        types.add(new Trinity("Exception", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("Error", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("BadFunctionCallException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("BadMethodCallException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("DomainException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("InvalidArgumentException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("LengthException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("LogicException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("OutOfBoundsException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("OutOfRangeException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("OverflowException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("RangeException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("RuntimeException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("UnderflowException", PhpIcons.EXCEPTION, "PHP Exception"));
        types.add(new Trinity("UnexpectedValueException", PhpIcons.EXCEPTION, "PHP Exception"));

        return types;
    }

    @NotNull
    public Properties getProperties(@NotNull PsiDirectory directory) {
        super.getProperties(directory);

        this.myProperties.setProperty("EXCEPTION", (String)((Trinity)this.myKindComboBox.getSelectedItem()).getFirst());
        String exceptionMessage = this.myMessageTextField.getText();

        if (StringUtil.isNotEmpty(exceptionMessage)) {
            this.myProperties.setProperty("EXCEPTION_MESSAGE", exceptionMessage);
        }

        return this.myProperties;
    }

    @NotNull
    public String getTemplateName() {
        return (String)((Trinity)this.myKindComboBox.getSelectedItem()).getThird();
    }
}
