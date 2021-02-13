package demo;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.sh.ShLanguage;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLScalarImpl;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class DemoYamlInjector implements MultiHostInjector, DumbAware {
    private static final Logger LOG = Logger.getInstance("#demo");

    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(YAMLKeyValue.class);
    }

    @Override
    public void getLanguagesToInject(MultiHostRegistrar registrar, PsiElement context) {
        if (!(context instanceof YAMLKeyValue)) {
            return;
        }

        // only inject into values of key "run" for this demo
        if (!"run".equals(((YAMLKeyValue)context).getKeyText())) {
            return;
        }

        if (!(((YAMLKeyValue)context).getValue() instanceof YAMLScalarImpl)) {
            return;
        }

        YAMLScalarImpl host = (YAMLScalarImpl)((YAMLKeyValue)context).getValue();
        if (host == null || !host.isValidHost()) {
            return;
        }

        List<TextRange> ranges = host.getContentRanges();
        if (ranges.isEmpty()) {
            LOG.warn("No content ranges found for host");
            return;
        }

        // avoid injections into non-physical files for easier debugging
        // seems to called twice for a physical file (once for physical, once for non-physical copy)
        if (!host.getContainingFile().isPhysical()) {
            return;
        }

        String hostText = host.getText();

        // inject shell script language into each element of the content ranges
        registrar.startInjecting(ShLanguage.INSTANCE);
        for (TextRange range : ranges) {
            LOG.warn("Injecting into range " + range + ", host text: " + range.substring(hostText));

            // using a prefix like ";\n" also break "edit fragment" of the injection
            registrar.addPlace(null, null, host, range);
        }
        registrar.doneInjecting();
    }
}
