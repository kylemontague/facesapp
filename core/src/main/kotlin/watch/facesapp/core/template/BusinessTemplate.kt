package watch.facesapp.core.template

import android.content.Context
import watch.facesapp.core.model.ColorRoleDefinition
import watch.facesapp.core.model.ContrastConstraint
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.FaceTemplate
import watch.facesapp.core.model.TemplateOption
import watch.facesapp.core.render.TemplateRenderer
import watch.facesapp.core.render.faces.BusinessRenderer

object BusinessTemplate : FaceTemplate {
    override val id: String = FaceStyle.BUSINESS_TIME.id
    override val displayName: String = "Business Time"
    override val description: String = "Clean precision analog watch face with distinct hand roles."

    override val colorRoles: List<ColorRoleDefinition> = listOf(
        ColorRoleDefinition("BG", "Background", 0xFF131947.toInt()),
        ColorRoleDefinition("HR", "Hour Hand", 0xFFC7FD00.toInt()),
        ColorRoleDefinition("MIN", "Minute Hand", 0xFF55EE45.toInt()),
        ColorRoleDefinition("SEC", "Second Hand", 0xFF3C797F.toInt())
    )

    override val options: List<TemplateOption<*>> = listOf(
        TemplateOption.BooleanOption(
            key = "show_seconds",
            displayName = "Second Hand",
            defaultValue = true,
            description = "Display active ticking second hand."
        )
    )

    override val contrastConstraints: List<ContrastConstraint> = listOf(
        ContrastConstraint(
            foregroundRole = "HR",
            backgroundRole = "BG",
            minRatio = 3.0,
            description = "Hour hand against dial background"
        ),
        ContrastConstraint(
            foregroundRole = "MIN",
            backgroundRole = "BG",
            minRatio = 3.0,
            description = "Minute hand against dial background"
        )
    )

    override fun createRenderer(context: Context): TemplateRenderer = BusinessRenderer(context)
}
