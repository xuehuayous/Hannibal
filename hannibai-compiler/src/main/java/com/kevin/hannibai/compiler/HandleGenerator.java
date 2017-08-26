/*
 * Copyright (c) 2017 Kevin zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevin.hannibai.compiler;

import com.kevin.hannibai.annotation.Apply;
import com.kevin.hannibai.annotation.Commit;
import com.kevin.hannibai.annotation.DefInt;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.kevin.hannibai.compiler.Constants.CLASS_JAVA_DOC;
import static com.kevin.hannibai.compiler.Constants.GET;
import static com.kevin.hannibai.compiler.Constants.GET_METHOD_JAVA_DOC;
import static com.kevin.hannibai.compiler.Constants.PUT_METHOD_JAVA_DOC;
import static com.kevin.hannibai.compiler.Constants.REMOVE;
import static com.kevin.hannibai.compiler.Constants.REMOVE_ALL;
import static com.kevin.hannibai.compiler.Constants.REMOVE_ALL_METHOD_JAVA_DOC;
import static com.kevin.hannibai.compiler.Constants.REMOVE_METHOD_JAVA_DOC;
import static com.kevin.hannibai.compiler.Constants.SET;

/**
 * Created by zhouwenkai on 2017/8/13.
 */

class HandleGenerator extends ElementGenerator {

    public HandleGenerator(TypeElement element, String classNameSuffix) {
        super(element, classNameSuffix);
    }

    @Override
    public TypeSpec onCreateTypeSpec(TypeElement element, String packageName, String className) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        HashSet<MethodSpec> methodSpecs = new LinkedHashSet<>();
        for (Element enclosedElement : enclosedElements) {

            if (enclosedElement.getKind() == ElementKind.FIELD) {
                String formatName = Utils.capitalize(enclosedElement.getSimpleName());

                AnnotationSpec defValueAnnotation = HannibaiUtils.createDefValueAnnotation(enclosedElement,
                        enclosedElement.asType().toString());

                boolean isCommit = enclosedElement.getAnnotation(Commit.class) == null;

                AnnotationSpec expireAnnotation = HannibaiUtils.getExpireAnnotation(enclosedElement);

                // The get method
                methodSpecs.add(
                        defValueAnnotation == null ?
                                MethodSpec.methodBuilder(GET + formatName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .returns(ClassName.get(enclosedElement.asType()))
                                        .addAnnotation(expireAnnotation)
                                        .addJavadoc(String.format(GET_METHOD_JAVA_DOC,
                                                enclosedElement.getSimpleName())
                                        )
                                        .build()
                                :
                                MethodSpec.methodBuilder(GET + formatName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .returns(ClassName.get(enclosedElement.asType()))
                                        .addAnnotation(defValueAnnotation)
                                        .addAnnotation(expireAnnotation)
                                        .addJavadoc(String.format(GET_METHOD_JAVA_DOC,
                                                enclosedElement.getSimpleName())
                                        )
                                        .build()
                );

                // The set method
                methodSpecs.add(
                        isCommit ?
                                MethodSpec.methodBuilder(SET + formatName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .addParameter(ClassName.get(enclosedElement.asType()),
                                                enclosedElement.getSimpleName().toString(), Modifier.FINAL)
                                        .returns(TypeName.VOID)
                                        .addAnnotation(AnnotationSpec.builder(Apply.class).build())
                                        .addJavadoc(String.format(PUT_METHOD_JAVA_DOC,
                                                enclosedElement.getSimpleName(),
                                                enclosedElement.getSimpleName())
                                        )
                                        .build()
                                :
                                MethodSpec.methodBuilder(SET + formatName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .addParameter(ClassName.get(enclosedElement.asType()),
                                                enclosedElement.getSimpleName().toString(), Modifier.FINAL)
                                        .returns(TypeName.BOOLEAN)
                                        .addAnnotation(AnnotationSpec.builder(Commit.class).build())
                                        .addJavadoc(String.format(PUT_METHOD_JAVA_DOC,
                                                enclosedElement.getSimpleName(),
                                                enclosedElement.getSimpleName())
                                        )
                                        .build()
                );

                // The remove method
                methodSpecs.add(
                        isCommit ?
                                MethodSpec.methodBuilder(REMOVE + formatName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .returns(TypeName.VOID)
                                        .addAnnotation(AnnotationSpec.builder(Apply.class).build())
                                        .addJavadoc(String.format(REMOVE_METHOD_JAVA_DOC,
                                                enclosedElement.getSimpleName())
                                        )
                                        .build()
                                :
                                MethodSpec.methodBuilder(REMOVE + formatName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .returns(TypeName.BOOLEAN)
                                        .addAnnotation(AnnotationSpec.builder(Commit.class).build())
                                        .addJavadoc(String.format(REMOVE_METHOD_JAVA_DOC,
                                                enclosedElement.getSimpleName())
                                        )
                                        .build()
                );
            }
        }

        if (methodSpecs.size() > 0) {
            // The deleteAll method
            MethodSpec methodDelete = MethodSpec.methodBuilder(REMOVE_ALL)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(TypeName.VOID)
                    .addAnnotation(AnnotationSpec.builder(Apply.class).build())
                    .addJavadoc(REMOVE_ALL_METHOD_JAVA_DOC)
                    .build();
            methodSpecs.add(methodDelete);
        }

        return TypeSpec.interfaceBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecs)
                .addJavadoc(CLASS_JAVA_DOC)
                .build();
    }

}
