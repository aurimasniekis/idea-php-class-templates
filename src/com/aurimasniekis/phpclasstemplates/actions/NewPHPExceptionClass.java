package com.aurimasniekis.phpclasstemplates.actions;

import com.aurimasniekis.phpclasstemplates.dialog.PhpNewExceptionClassDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.actions.PhpNewBaseAction;
import com.jetbrains.php.templates.PhpCreateFileFromTemplateDataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewPHPExceptionClass extends PhpNewBaseAction {
    public NewPHPExceptionClass()
    {
        super("PHP Exception Class", "Creates new PHP exception class");
    }

    protected PhpCreateFileFromTemplateDataProvider getDataProvider(@NotNull Project project, @NotNull PsiDirectory dir, @Nullable PsiFile file) {
        PhpNewExceptionClassDialog dialog = new PhpNewExceptionClassDialog(project, dir);

        if (!dialog.showAndGet()) {
            return null;
        }

        return dialog;
    }
}
