package com.afzaln.guttercolor

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilBase
import com.intellij.ui.ColorChooser
import com.intellij.util.FunctionUtil
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.TwoColorsIcon
import org.jetbrains.annotations.NotNull
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.parents
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression
import org.jetbrains.uast.resolveToUElement
import org.jetbrains.uast.toUElement
import org.jetbrains.uast.tryResolve
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.Icon


class ColorLineMarkerProvider : LineMarkerProvider {

    private val myExtensions = ElementColorProvider.EP_NAME.extensions

    override fun getLineMarkerInfo(@NotNull element: PsiElement): LineMarkerInfo<*>? {
        // TODO constant Colors, don't know if this is possible
        //  check langauge
        if (element is KtCallExpression) {
            if (element.toUElement()?.tryResolve() == null) {
                // ignore errors
                return null
            }
            try {
                val color: Color? = getColorFrom(element)
                if (color != null) {
                    return MyInfo(element, color, myExtensions[0])
                }
            } catch (ex: Exception) {
                println("An error occurred while parsing ${element.text}: ${ex.message}")
            }
        }
        return null
    }

    private fun getColorFrom(element: KtCallExpression): Color? {
        if (isComposeColorFunction(element)) {
            // TODO Color constructor with ULong
            val arguments = element.valueArguments
            when (arguments.size) {
                5 -> {
                    //  TODO fun Color(Int r,g,b,a,colorspace) ignore colorspace
                    val (r, g, b, a) = arguments.map {
                        it.text.toFloat()
                    }
                    println("Got 5 arguments: $r, $g, $b, $a, ignored colorspace")
                    val composeColor = composeColor(r, g, b, a)
                    return composeColor.run {
                        Color(red, green, blue, alpha)
                    }
                }
                4 -> {
                    //  TODO fun Color(IntRange 0 to 0xFF r, g, b, a)
                    val (r, g, b, a) = arguments.map {
                        it.text.replace("0x", "").toIntOrNull(16)
                    }
                    if (r != null && g != null && b != null && a != null) {
                        println("Got 4 arguments: $r, $g, $b, $a")
                        val composeColor = composeColor(r, g, b, a)
                        return composeColor.run {
                            Color(red, green, blue, alpha)
                        }
                    } else if (r == null) {
                        val (rf, gf, bf, af) = arguments.map {
                            it.text.toFloatOrNull()
                        }
                        if (rf != null && gf != null && bf != null && af != null) {
                            println("Got 4 arguments: $rf, $gf, $bf, $af")
                            val composeColor = composeColor(rf, gf, bf, af)
                            return composeColor.run {
                                Color(red, green, blue, alpha)
                            }
                        }
                    }
                    // TODO variables as args
                    //  (arguments[0].children[0].toUElement() as KotlinUSimpleReferenceExpression).resolve().originalElement.toUElement().sourcePsi.children[0].text
                }
                3 -> {
                    val (r, g, b) = arguments.map {
                        it.text.replace("0x", "").toIntOrNull(16)
                    }
                    if (r != null && g != null && b != null) {
                        println("Got 4 arguments: $r, $g, $b")
                        val composeColor = composeColor(r, g, b)
                        return composeColor.run {
                            Color(red, green, blue, alpha)
                        }
                    } else if (r == null) {
                        val (rf, gf, bf) = arguments.map {
                            it.text.toFloatOrNull()
                        }
                        if (rf != null && gf != null && bf != null) {
                            println("Got 4 arguments: $rf, $gf, $bf")
                            val composeColor = composeColor(rf, gf, bf)
                            return composeColor.run {
                                Color(red, green, blue, alpha)
                            }
                        }
                    }
                }
                1 -> {
                    val arg = arguments[0].text

                    // TODO
                    //  fun Color(ColorInt)
                    if (arg.startsWith("0x", ignoreCase = true)) {
                        val composeColor = if (arg.length == 8) {
                            // fun Color(Int)
                            composeColor(arg.substring(2, arg.length).toInt(16)).copy(alpha = 1.0f)
                        } else {
                            // fun Color(Long)
                            composeColor(arg.substring(2, arg.length).toLong(16))
                        }

                        return composeColor.run {
                            Color(red, green, blue, alpha)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun isComposeColorFunction(element: KtCallExpression): Boolean {
        val uElement = element.toUElement()
        if (uElement is KotlinUFunctionCallExpression) {
            val resolvedUElement = uElement.resolveToUElement()
            if (resolvedUElement != null) {
                val sourcePsi = resolvedUElement.sourcePsi
                if (sourcePsi is KtDeclaration) {
                    val parentsOfReturnType = sourcePsi.type()?.constructor?.declarationDescriptor?.parents
                    if (parentsOfReturnType != null) {
                        if (parentsOfReturnType.iterator().hasNext()) {
                            val next = parentsOfReturnType.iterator().next()
                            val fqName = next.fqNameOrNull()?.asString() ?: ""
                            return fqName == "androidx.compose.ui.graphics"
                        }
                    }
                }
            }
        }

        return false
    }

    // override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, result: MutableCollection<LineMarkerInfo<PsiElement>>) {}

    private class MyInfo(@NotNull element: PsiElement, color: Color, colorProvider: ElementColorProvider) : MergeableLineMarkerInfo<PsiElement>(
        element,
        element.textRange,
        ColorIcon(12, color),
        Pass.UPDATE_ALL,
        FunctionUtil.nullConstant<Any, String>(),
        object : GutterIconNavigationHandler<PsiElement?> {
            override fun navigate(e: MouseEvent?, elt: PsiElement?) {
                if (elt == null || !elt.isWritable) return

                val editor: Editor = PsiUtilBase.findEditor(element)!!
                val c: Color? = ColorChooser.chooseColor(editor.component, "Choose color", color, true)
                if (c != null) {
                    ApplicationManager.getApplication().runWriteAction {
                        println("Found color $c")
                        colorProvider.setColorTo(element, c)
                    }
                }
            }
        },
        GutterIconRenderer.Alignment.LEFT
    ) {
        private val myColor: Color = color
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>): Boolean {
            return info is MyInfo
        }

        override fun getCommonIcon(infos: (MutableList<out MergeableLineMarkerInfo<*>>)): Icon {
            return if (infos.size == 2 && infos[0] is MyInfo && infos[1] is MyInfo) {
                TwoColorsIcon(12, (infos[1] as MyInfo).myColor, (infos[0] as MyInfo).myColor)
            } else AllIcons.Gutter.Colors
        }

        // override fun getCommonTooltip(infos: MutableList<MergeableLineMarkerInfo<PsiElement>>): Function<in PsiElement, String> {
        //     return FunctionUtil.nullConstant()
        // }

    }


}