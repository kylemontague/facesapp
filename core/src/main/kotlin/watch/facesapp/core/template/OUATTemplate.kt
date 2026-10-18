package watch.facesapp.core.template

import android.content.Context
import watch.facesapp.core.model.ColorRoleDefinition
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.FaceTemplate
import watch.facesapp.core.model.TemplateOption
import watch.facesapp.core.render.TemplateRenderer
import watch.facesapp.core.render.faces.OUATRenderer

object OUATTemplate : FaceTemplate {
    override val id: String = FaceStyle.OUA_TIME.id
    override val displayName: String = "Once Upon A Time"
    override val description: String = "Artisanal typographical word clock with live seconds arc."

    override val colorRoles: List<ColorRoleDefinition> = listOf(
        ColorRoleDefinition("BG", "Background", 0xFF080A0D.toInt()),
        ColorRoleDefinition("HR", "Word Text", 0xFFFFFFFF.toInt()),
        ColorRoleDefinition("SEC", "Seconds Arc", 0xFFFFFFFF.toInt())
    )

    override val options: List<TemplateOption<*>> = listOf(
        TemplateOption.BooleanOption(
            key = "show_seconds_arc",
            displayName = "Seconds Arc",
            defaultValue = true,
            description = "Draw live second progression sweep arc."
        )
    )

    override fun createRenderer(context: Context): TemplateRenderer = OUATRenderer(context)
}
