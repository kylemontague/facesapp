package watch.facesapp.core.template

import android.content.Context
import watch.facesapp.core.model.ColorRoleDefinition
import watch.facesapp.core.model.ContrastConstraint
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.FaceTemplate
import watch.facesapp.core.model.TemplateOption
import watch.facesapp.core.render.TemplateRenderer
import watch.facesapp.core.render.faces.TwoTimeRenderer

object TwoTimeTemplate : FaceTemplate {
    override val id: String = FaceStyle.TWO_TIME.id
    override val displayName: String = "Two Time"
    override val description: String = "Bold diagonal dual-tone digital layout with prominent typography."

    override val colorRoles: List<ColorRoleDefinition> = listOf(
        ColorRoleDefinition("BG", "Background", 0xFF080A0D.toInt()),
        ColorRoleDefinition("ACC", "Split Accent", 0xFFFFFFFF.toInt()),
        ColorRoleDefinition("TXT", "Digits & Line", 0xFFFFFFFF.toInt())
    )

    override val options: List<TemplateOption<*>> = listOf(
        TemplateOption.ChoiceOption(
            key = "time_format",
            displayName = "Time Format",
            defaultValue = "12H",
            choices = listOf("12H", "24H"),
            description = "12-hour or 24-hour digital clock display."
        )
    )

    override val contrastConstraints: List<ContrastConstraint> = listOf(
        ContrastConstraint(
            foregroundRole = "TXT",
            backgroundRole = "BG",
            minRatio = 3.0,
            description = "Digits against background"
        )
    )

    override fun createRenderer(context: Context): TemplateRenderer = TwoTimeRenderer(context)
}
