package watch.facesapp.core.template

import android.content.Context
import watch.facesapp.core.model.ColorRoleDefinition
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.FaceTemplate
import watch.facesapp.core.render.TemplateRenderer
import watch.facesapp.core.render.faces.FillRenderer

object FillTemplate : FaceTemplate {
    override val id: String = FaceStyle.FILL_TIME.id
    override val displayName: String = "Filling Time"
    override val description: String = "Minimal dynamic liquid vertical fill representing minute progression."

    override val colorRoles: List<ColorRoleDefinition> = listOf(
        ColorRoleDefinition("BG", "Background", 0xFF89C4BB.toInt()),
        ColorRoleDefinition("ACC", "Fill Accent", 0xFFCDE9E6.toInt()),
        ColorRoleDefinition("TXT", "Time Text", 0xFFFFFFFF.toInt())
    )

    override fun createRenderer(context: Context): TemplateRenderer = FillRenderer(context)
}
