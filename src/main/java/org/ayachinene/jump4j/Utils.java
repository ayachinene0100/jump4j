package org.ayachinene.jump4j;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.maddyhome.idea.vim.api.VimEditor;
import com.maddyhome.idea.vim.newapi.IjVimEditor;

public class Utils {

    public static String command(String keys) {
        return "<Plug>(jump4j-" + keys + ")";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static PsiFile getPsiFile(VimEditor vimEditor) {
        Project project = ((IjVimEditor) vimEditor).getEditor().getProject();
        if (project == null) {
            return null;
        }
        return PsiDocumentManager.getInstance(project).getPsiFile(((IjVimEditor) vimEditor).getEditor().getDocument());
    }

    public static <T extends PsiElement> T findOuterElement(PsiElement element, Class<T> elementType) {
        if (elementType.isInstance(element)) {
            //noinspection unchecked
            return (T) element;
        }
        if (element == null || element.getParent() instanceof PsiFile) {
            return null;
        }
        return findOuterElement(element.getParent(), elementType);
    }
}
