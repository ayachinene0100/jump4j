package org.ayachinene.jump4j;

import com.intellij.psi.*;
import com.maddyhome.idea.vim.api.ExecutionContext;
import com.maddyhome.idea.vim.api.VimCaret;
import com.maddyhome.idea.vim.api.VimEditor;
import com.maddyhome.idea.vim.extension.ExtensionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ParameterHandler implements ExtensionHandler {

    private final boolean reverse;

    public ParameterHandler(boolean reverse) {
        this.reverse = reverse;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void execute(@NotNull VimEditor vimEditor, @NotNull ExecutionContext context) {
        PsiFile psiFile = Utils.getPsiFile(vimEditor);
        if (psiFile == null) {
            return;
        }

        for (VimCaret caret : vimEditor.carets()) {
            PsiElement element = psiFile.findElementAt(caret.getOffset().getPoint());

            PsiExpressionList expressionList = Utils.findOuterElement(element, PsiExpressionList.class);
            PsiParameterList parameterList = Utils.findOuterElement(element, PsiParameterList.class);
            PsiAnnotationParameterList annotationParameterList = Utils.findOuterElement(element, PsiAnnotationParameterList.class);

            if (expressionList != null) {
                onInParamList(caret, element, expressionList::getExpressions, ParameterHandler::defaultOnInParam);
                return;
            }
            if (parameterList != null) {
                onInParamList(caret, element, parameterList::getParameters, (p, e, i) -> {
                    if (e.getTextOffset() >= p.getTextOffset())
                        return i + 1;
                    return i;
                });
                return;
            }
            if (annotationParameterList != null) {
                onInParamList(caret, element, annotationParameterList::getAttributes, ParameterHandler::defaultOnInParam);
                return;
            } else {
                onNotInParamList(caret, element);
            }

        }
    }

    @FunctionalInterface
    private interface OnInParam {
        int getTargetOffset(PsiElement param, PsiElement element, int i);
    }

    private static int defaultOnInParam(PsiElement param, PsiElement element, int i) {
        return i + 1;
    }

    private <T extends PsiElement> void
    onInParamList(VimCaret caret,
                  PsiElement element,
                  Supplier<T[]> paramListSupplier,
                  OnInParam onInParam
    ) {
        T[] params = paramListSupplier.get();
        if (params == null) {
            return;
        }

        int i;
        if (!reverse) {
            for (i = 0; i < params.length; i++) {
                if (params[i].getTextRange().contains(caret.getOffset().getPoint())) {
                    i = onInParam.getTargetOffset(params[i], element, i);
                    break;
                } else if (element.getTextOffset() < params[i].getTextOffset()) {
                    break;
                }
            }
            if (i > params.length - 1) {
                onNotInParamList(caret, element);
                return;
            }
        } else {
            for (i = params.length - 1; i >= 0; --i) {
                if (params[i].getTextRange().contains(element.getTextOffset())) {
                    if (caret.getOffset().getPoint() != params[i].getTextOffset()) {
                        break;
                    } else {
                        i -= 1;
                        break;
                    }
                } else if (element.getTextOffset() > params[i].getTextOffset()) {
                    break;
                }
            }
            if (i < 0) {
                onNotInParamList(caret, element);
                return;
            }
        }
        caret.moveToOffset(params[i].getTextOffset());
    }

    private void
    onNotInParamList(VimCaret caret,
                     PsiElement element) {
        if (element == null) {
            return;
        }
        PsiElement next = element;
        if (!reverse) {
            do {
                int end = next.getTextRange().getEndOffset();
                next = element.getContainingFile().findElementAt(end);
                if (next instanceof PsiJavaToken token) {
                    if (token.getText().equals("(")) {
                        caret.moveToOffset(token.getTextOffset());
                        break;
                    }
                }
            } while (next != null);
        } else {
            do {
                int begin = next.getTextRange().getStartOffset();
                next = element.getContainingFile().findElementAt(begin - 1);
                if (next instanceof PsiJavaToken token) {
                    if (token.getText().equals(")")) {
                        PsiElement prevSibling = next.getPrevSibling();
                        while (prevSibling instanceof PsiJavaToken) {
                            if (prevSibling.getText().equals("(")) {
                                caret.moveToOffset(prevSibling.getTextOffset());
                                return;
                            }
                            prevSibling = prevSibling.getPrevSibling();
                        }
                        if (prevSibling == null) {
                            return;
                        }
                        caret.moveToOffset(prevSibling.getTextOffset());
                        return;
                    }
                }
            } while (next != null && next.getTextOffset() != 0);
        }
    }
}
