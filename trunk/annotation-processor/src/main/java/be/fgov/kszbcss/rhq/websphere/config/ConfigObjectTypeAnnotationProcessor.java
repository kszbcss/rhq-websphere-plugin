/*
 * RHQ WebSphere Plug-in
 * Copyright (C) 2013 Crossroads Bank for Social Security
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package be.fgov.kszbcss.rhq.websphere.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes(value={"be.fgov.kszbcss.rhq.websphere.config.ConfigObjectType"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ConfigObjectTypeAnnotationProcessor extends AbstractProcessor {
    private final Properties mappings = new Properties();
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            // Since we support a single annotation, the set is a singleton containing only
            // the ConfigObjectType annotation type
            TypeElement annotationType = annotations.iterator().next();
            for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
                TypeElement type = (TypeElement)element;
                for (AnnotationMirror annotation : type.getAnnotationMirrors()) {
                    if (annotation.getAnnotationType().asElement() == annotationType) {
                        String name = null;
                        for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
                            if (entry.getKey().getSimpleName().contentEquals("name")) {
                                name = (String)entry.getValue().getValue();
                            }
                        }
                        mappings.setProperty(name, type.getQualifiedName().toString());
                    }
                }
            }
        }
        if (roundEnv.processingOver()) {
            try {
                OutputStream out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/config-object-types.index").openOutputStream();
                try {
                    mappings.store(out, null);
                } finally {
                    out.close();
                }
            } catch (IOException ex) {
                // TODO
                ex.printStackTrace();
            }
        }
        return true;
    }
}
