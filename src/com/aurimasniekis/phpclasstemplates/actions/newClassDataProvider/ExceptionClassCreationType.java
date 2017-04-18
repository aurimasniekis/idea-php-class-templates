package com.aurimasniekis.phpclasstemplates.actions.newClassDataProvider;

import com.intellij.openapi.util.Trinity;
import com.jetbrains.php.PhpIcons;
import javax.swing.Icon;

public enum ExceptionClassCreationType {
    EXCEPTION(new Trinity("Exception", PhpIcons.CLASS, "PHP Exception"));

    private Trinity<String, Icon, String> myTrinity;

    private ExceptionClassCreationType(Trinity<String, Icon, String> trinity) {
        this.myTrinity = trinity;
    }

    public Trinity<String, Icon, String> getTrinity() {
        return this.myTrinity;
    }
}
