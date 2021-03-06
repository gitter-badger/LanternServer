/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.event.filter.delegate;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.filter.data.Has;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HasDataFilterDelegate implements ParameterFilterDelegate {

    private final Has anno;

    public HasDataFilterDelegate(Has anno) {
        this.anno = anno;
    }

    @Override
    public void write(ClassWriter cw, MethodVisitor mv, Method method, Parameter param, int localParam) {
        if (!DataHolder.class.isAssignableFrom(param.getType())) {
            throw new IllegalStateException("Annotated type for data filter is not a DataHolder");
        }

        mv.visitVarInsn(ALOAD, localParam);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(DataHolder.class));
        mv.visitLdcInsn(Type.getType(this.anno.value()));
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(DataHolder.class), "get", "(Ljava/lang/Class;)Ljava/util/Optional;", true);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false);
        Label success = new Label();
        if (this.anno.inverse()) {
            mv.visitJumpInsn(IFEQ, success);
        } else {
            mv.visitJumpInsn(IFNE, success);
        }
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(success);
    }

}
