/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.roots.ui.configuration.libraryEditor;

import com.intellij.codeInsight.ExternalAnnotationsManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.ui.Util;
import com.intellij.openapi.roots.AnnotationOrderRootType;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.OrderRootTypeUIFactory;
import com.intellij.openapi.roots.ui.configuration.PathUIUtils;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * @author nik
 */
public class DefaultLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {
  @Override
  public OrderRootTypePresentation getRootTypePresentation(@NotNull OrderRootType type) {
    return getDefaultPresentation(type);
  }

  @NotNull
  @Override
  public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
    return Arrays.asList(new AttachUrlJavadocDescriptor());
  }

  @NotNull
  @Override
  public List<? extends RootDetector> getRootDetectors() {
    return Arrays.asList(new FileTypeBasedRootFilter(OrderRootType.CLASSES, false, StdFileTypes.CLASS, "classes"),
                         new FileTypeBasedRootFilter(OrderRootType.CLASSES, true, StdFileTypes.CLASS, "jar directory"),
                         PathUIUtils.JAVA_SOURCE_ROOT_DETECTOR,
                         new FileTypeBasedRootFilter(OrderRootType.SOURCES, true, StdFileTypes.JAVA, "source archive directory"),
                         new JavadocRootDetector(),
                         new AnnotationsRootFilter());
  }

  public static OrderRootTypePresentation getDefaultPresentation(OrderRootType type) {
    final OrderRootTypeUIFactory factory = OrderRootTypeUIFactory.FACTORY.getByKey(type);
    return new OrderRootTypePresentation(factory.getNodeText(), factory.getIcon());
  }

  private static class JavadocRootDetector extends RootDetector {
    private JavadocRootDetector() {
      super(JavadocOrderRootType.getInstance(), false, "JavaDocs");
    }

    @NotNull
    @Override
    public Collection<VirtualFile> detectRoots(@NotNull VirtualFile rootCandidate, @NotNull ProgressIndicator progressIndicator) {
      List<VirtualFile> result = new ArrayList<VirtualFile>();
      collectJavadocRoots(rootCandidate, result, progressIndicator);
      return result;
    }

    private static void collectJavadocRoots(VirtualFile file, final List<VirtualFile> result, final ProgressIndicator progressIndicator) {
      VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {
        @Override
        public boolean visitFile(@NotNull VirtualFile file) {
          progressIndicator.checkCanceled();
          if (file.isDirectory() && file.findChild("allclasses-frame.html") != null && file.findChild("allclasses-noframe.html") != null) {
            result.add(file);
            return false;
          }
          return true;
        }
      });
    }
  }

  private static class AnnotationsRootFilter extends FileTypeBasedRootFilter {
    private AnnotationsRootFilter() {
      super(AnnotationOrderRootType.getInstance(), false, StdFileTypes.XML, "external annotations");
    }

    @Override
    protected boolean isFileAccepted(VirtualFile virtualFile) {
      return super.isFileAccepted(virtualFile) && virtualFile.getName().equals(ExternalAnnotationsManager.ANNOTATIONS_XML);
    }
  }

  private static class AttachUrlJavadocDescriptor extends AttachRootButtonDescriptor {
    private AttachUrlJavadocDescriptor() {
      super(JavadocOrderRootType.getInstance(), ProjectBundle.message("module.libraries.javadoc.url.button"));
    }

    @Override
    public VirtualFile[] selectFiles(@NotNull JComponent parent,
                                     @Nullable VirtualFile initialSelection,
                                     @Nullable Module contextModule,
                                     @NotNull LibraryEditor libraryEditor) {
      final VirtualFile vFile = Util.showSpecifyJavadocUrlDialog(parent);
      if (vFile != null) {
        return new VirtualFile[]{vFile};
      }
      return VirtualFile.EMPTY_ARRAY;
    }
  }
}
