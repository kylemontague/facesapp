package watch.facesapp.core.template

import watch.facesapp.core.model.FaceTemplate
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for all available watch face templates.
 * Provides the single source of truth for template metadata, color roles, options, and rendering.
 * Supports runtime registration of new templates.
 */
object TemplateRegistry {

    private val templates = ConcurrentHashMap<String, FaceTemplate>()

    init {
        reset()
    }

    /**
     * Resets the registry back to the default built-in templates.
     */
    fun reset() {
        templates.clear()
        register(OUATTemplate)
        register(BusinessTemplate)
        register(FillTemplate)
        register(TwoTimeTemplate)
        register(PrismTemplate)
    }

    /**
     * Register a new or custom watch face template.
     */
    fun register(template: FaceTemplate) {
        templates[template.id.lowercase()] = template
    }

    /**
     * Look up a template by its unique identifier.
     */
    fun get(id: String?): FaceTemplate? {
        if (id == null) return null
        return templates[id.lowercase()]
    }

    /**
     * Return all registered templates in presentation order.
     */
    fun all(): List<FaceTemplate> = listOf(
        OUATTemplate,
        BusinessTemplate,
        FillTemplate,
        TwoTimeTemplate,
        PrismTemplate
    ) + templates.values.filterNot {
        it == OUATTemplate || it == BusinessTemplate || it == FillTemplate ||
        it == TwoTimeTemplate || it == PrismTemplate
    }

    val defaultTemplate: FaceTemplate get() = OUATTemplate
}
