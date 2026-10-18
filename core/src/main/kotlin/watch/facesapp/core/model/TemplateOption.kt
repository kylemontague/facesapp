package watch.facesapp.core.model

/**
 * Represents an extensible configuration option for a watch face template
 * (e.g. 12h/24h toggle, tick marks style, second hand visibility, timezones).
 */
sealed class TemplateOption<T>(
    val key: String,
    val displayName: String,
    val defaultValue: T,
    val description: String = ""
) {
    class BooleanOption(
        key: String,
        displayName: String,
        defaultValue: Boolean,
        description: String = ""
    ) : TemplateOption<Boolean>(key, displayName, defaultValue, description)

    class ChoiceOption(
        key: String,
        displayName: String,
        defaultValue: String,
        val choices: List<String>,
        description: String = ""
    ) : TemplateOption<String>(key, displayName, defaultValue, description)
}
