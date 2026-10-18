package watch.facesapp.core.template

import android.content.Context
import watch.facesapp.core.model.ColorRoleDefinition
import watch.facesapp.core.model.ContrastConstraint
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.FaceTemplate
import watch.facesapp.core.render.TemplateRenderer
import watch.facesapp.core.render.faces.PrismRenderer

object PrismTemplate : FaceTemplate {
    override val id: String = FaceStyle.PRISM_TIME.id
    override val displayName: String = "Space-Time Prism"
    override val description: String = "Procedural Delaunay triangulated mesh backdrop rendered from palette colors."

    override val colorRoles: List<ColorRoleDefinition> = listOf(
        ColorRoleDefinition("BG", "Mesh Primary", 0xFF2369AD.toInt()),
        ColorRoleDefinition("ACC", "Mesh Secondary", 0xFF32A3C2.toInt()),
        ColorRoleDefinition("TXT", "Time Text", 0xFFFFFFFF.toInt())
    )

    override val contrastConstraints: List<ContrastConstraint> = listOf(
        ContrastConstraint(
            foregroundRole = "TXT",
            backgroundRole = "BG",
            minRatio = 3.0,
            description = "Time text against mesh primary"
        ),
        ContrastConstraint(
            foregroundRole = "TXT",
            backgroundRole = "ACC",
            minRatio = 3.0,
            description = "Time text against mesh secondary"
        )
    )

    override fun createRenderer(context: Context): TemplateRenderer = PrismRenderer(context)
}
