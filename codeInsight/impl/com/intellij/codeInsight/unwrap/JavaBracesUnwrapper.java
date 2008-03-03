package com.intellij.codeInsight.unwrap;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;

public class JavaBracesUnwrapper extends JavaUnwrapper {
  public JavaBracesUnwrapper() {
    super(CodeInsightBundle.message("unwrap.braces"));
  }

  public boolean isApplicableTo(PsiElement e) {
    return e instanceof PsiBlockStatement && !belongsToControlStructures(e);
  }

  private boolean belongsToControlStructures(PsiElement e) {
    PsiElement p = e.getParent();

    return p instanceof PsiIfStatement
           || p instanceof PsiLoopStatement
           || p instanceof PsiTryStatement
           || p instanceof PsiCatchSection;
  }

  public void unwrap(Editor editor, PsiElement element) throws IncorrectOperationException {
    extractFromBlockOrSingleStatement((PsiStatement)element, element);
    element.delete();
  }
}