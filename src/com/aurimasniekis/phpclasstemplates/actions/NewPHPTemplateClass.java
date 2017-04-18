package com.aurimasniekis.phpclasstemplates.actions;

import com.aurimasniekis.phpclasstemplates.dialog.PhpNewExceptionClassDialog;
import com.aurimasniekis.phpclasstemplates.dialog.PhpNewTemplateClassDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.actions.PhpNewBaseAction;
import com.jetbrains.php.templates.PhpCreateFileFromTemplateDataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewPHPTemplateClass extends PhpNewBaseAction {
    public NewPHPTemplateClass()
    {
        super("PHP Class From Template", "Creates new PHP Class from template");
    }

    protected PhpCreateFileFromTemplateDataProvider getDataProvider(@NotNull Project project, @NotNull PsiDirectory dir, @Nullable PsiFile file) {
        PhpNewTemplateClassDialog dialog = new PhpNewTemplateClassDialog(project, dir);

        if (!dialog.showAndGet()) {
            return null;
        }

        return dialog;
    }
}
