package com.IceCreamQAQ.Yu.web

import com.IceCreamQAQ.Yu.loader.transformer.ClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

//class WebClassTransformer : ClassTransformer {
//    override fun transform(bytes: ByteArray, className: String): ByteArray {

//        return when (className) {
//            "org.smartboot.http.server.handle.Handle" -> {
//                val reader = ClassReader(bytes)
//                val node = ClassNode()
//                reader.accept(node, 0)
//
//                node.access = Opcodes.ACC_PUBLIC
//
//                val cw = ClassWriter(0)
//                node.accept(cw)
//                cw.toByteArray()
//            }
//            "org.smartboot.socket.transport.IoServerConfig" -> {
//                val reader = ClassReader(bytes)
//                val node = ClassNode()
//                reader.accept(node, 0)
//
//                for (methodNode in node.methods as List<MethodNode>) if (methodNode.name == "<init>") for (i in 0 until methodNode.instructions.size()) if (methodNode.instructions[i] is FieldInsnNode) if ((methodNode.instructions[i] as FieldInsnNode).name == "bannerEnabled") methodNode.instructions.set(methodNode.instructions[i - 1], InsnNode(Opcodes.ICONST_0))
//
//                val cw = ClassWriter(0)
//                node.accept(cw)
//                cw.toByteArray()
//            }
//            else -> {
//                bytes
//            }
//        }
//    }

//}
