package watch.facesapp.core.model

/**
 * Definition of a single customizable color slot for a watch face template.
 */
data class ColorRoleDefinition(
    val code: String,
    val displayName: String,
    val defaultColor: Int
)
