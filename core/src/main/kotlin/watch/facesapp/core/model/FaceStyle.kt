package watch.facesapp.core.model

enum class FaceStyle(val id: String, val displayName: String) {
    OUA_TIME("OUA_TIME", "Once Upon A Time"),
    FILL_TIME("FILL_TIME", "Filling Time"),
    BUSINESS_TIME("BUSINESS_TIME", "Business Time"),
    TWO_TIME("TWO_TIME", "Two Time"),
    PRISM_TIME("PRISM_TIME", "Space-Time Prism");

    val template: FaceTemplate
        get() = watch.facesapp.core.template.TemplateRegistry.get(id)
            ?: watch.facesapp.core.template.TemplateRegistry.defaultTemplate

    companion object {
        fun fromId(id: String): FaceStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OUA_TIME
    }
}
