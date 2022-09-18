package com.intellij.codeInsight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import gnu.trove.THashSet;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Collection;
import java.util.Set;

public class CodeInsightUtilCore2 extends FileModificationService2 {

    private static final Logger LOG = Logger.getInstance(CodeInsightUtilCore.class);

    @Override
    public boolean preparePsiElementsForWrite(@NonNull Collection<? extends PsiElement> elements) {
        if (elements.isEmpty()) return true;
        Set<VirtualFile> files = new THashSet<VirtualFile>();
        Project project = null;
        for (PsiElement element : elements) {
            PsiFile file = element.getContainingFile();
            if (file == null) continue;
            project = file.getProject();
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile == null) continue;
            files.add(virtualFile);
        }
        if (!files.isEmpty()) {
            VirtualFile[] virtualFiles = VfsUtilCore.toVirtualFileArray(files);
            //            ReadonlyStatusHandler.OperationStatus status =
            // ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFiles);
            //            return !status.hasReadonlyFiles();
        }
        return true;
    }

    @Override
    public boolean prepareFileForWrite(@Nullable PsiFile psiFile) {
        if (psiFile == null) return false;
        final VirtualFile file = psiFile.getVirtualFile();
        final Project project = psiFile.getProject();

        //        final Editor editor =
        //                psiFile.isWritable() ? null :
        // FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project,
        // file), true);
        //        if (!ReadonlyStatusHandler.ensureFilesWritable(project, file)) {
        //            ApplicationManager.getApplication().invokeLater(new Runnable() {
        //                @Override
        //                public void run() {
        //                    if (editor != null && editor.getComponent().isDisplayable()) {
        //                        HintManager.getInstance()
        //                                .showErrorHint(editor,
        // CodeInsightBundle.message("error.hint.file.is.readonly", file.getPresentableUrl()));
        //                    }
        //                }
        //            });
        //
        //            return false;
        //        }

        return true;
    }

    @Override
    public boolean prepareVirtualFilesForWrite(
            @NonNull Project project, @NonNull Collection<? extends VirtualFile> files) {
        return true;
    }
}
