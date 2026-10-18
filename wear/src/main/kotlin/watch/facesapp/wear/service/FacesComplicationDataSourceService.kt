package watch.facesapp.wear.service

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import watch.facesapp.wear.datalayer.WearMessageListener

/**
 * Complication provider service that exposes the active Faces template name and configuration
 * to external watch faces and Wear OS complication slots.
 */
class FacesComplicationDataSourceService : ComplicationDataSourceService() {

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val config = WearMessageListener.loadSavedConfig(this)
        val styleTitle = config.style.name.replace("_", " ")

        val complicationData: ComplicationData? = when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(config.style.id.uppercase()).build(),
                    contentDescription = PlainComplicationText.Builder("Faces Style: $styleTitle").build()
                ).setTitle(PlainComplicationText.Builder("FACES").build())
                 .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(styleTitle).build(),
                    contentDescription = PlainComplicationText.Builder("Faces Style: $styleTitle").build()
                ).setTitle(PlainComplicationText.Builder("Faces Watch").build())
                 .build()
            }
            else -> null
        }

        listener.onComplicationData(complicationData)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("OUAT").build(),
                    contentDescription = PlainComplicationText.Builder("Faces Style: Once Upon A Time").build()
                ).setTitle(PlainComplicationText.Builder("FACES").build())
                 .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Once Upon A Time").build(),
                    contentDescription = PlainComplicationText.Builder("Faces Style: Once Upon A Time").build()
                ).setTitle(PlainComplicationText.Builder("Faces Watch").build())
                 .build()
            }
            else -> null
        }
    }
}
