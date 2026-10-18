package watch.facesapp.core.model

import android.content.Context
import watch.facesapp.core.render.TemplateRenderer

/**
 * Cohesive definition of a watch face template, encompassing identity,
 * color roles, layout options, and renderer instantiation.
 */
interface FaceTemplate {
    val id: String
    val displayName: String
    val description: String
    val colorRoles: List<ColorRoleDefinition>
    val defaultColors: List<Int> get() = colorRoles.map { it.defaultColor }
    val options: List<TemplateOption<*>> get() = emptyList()

    fun createRenderer(context: Context): TemplateRenderer
}
