package watch.facesapp.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import watch.facesapp.core.model.ColorRoleDefinition
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.FaceTemplate
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.render.TemplateRenderer
import watch.facesapp.core.template.TemplateRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class TemplateRegistryTest {

    @Before
    fun setUp() {
        TemplateRegistry.reset()
    }

    @Test
    fun testBuiltInTemplatesRegistered() {
        val allTemplates = TemplateRegistry.all()
        assertEquals(5, allTemplates.size)

        for (style in FaceStyle.entries) {
            val template = TemplateRegistry.get(style.id)
            assertNotNull("Template should exist for ${style.id}", template)
            assertEquals(style.id, template!!.id)
            assertTrue("Template must have color roles", template.colorRoles.isNotEmpty())
            assertEquals(template.colorRoles.size, template.defaultColors.size)
        }
    }

    @Test
    fun testRegisterCustomTemplateDynamically() {
        val customTemplate = object : FaceTemplate {
            override val id: String = "CUSTOM_BAUHAUS"
            override val displayName: String = "Bauhaus Minimal"
            override val description: String = "Clean geometric Bauhaus chronograph."
            override val colorRoles: List<ColorRoleDefinition> = listOf(
                ColorRoleDefinition("BG", "Dial", 0xFFFFFFFF.toInt()),
                ColorRoleDefinition("ACC", "Accent Ring", 0xFFFF0000.toInt())
            )
            override fun createRenderer(context: Context): TemplateRenderer = object : TemplateRenderer {
                override fun render(
                    canvas: Canvas,
                    bounds: Rect,
                    config: FaceConfig,
                    calendar: Calendar,
                    isAmbient: Boolean,
                    shape: WatchShape,
                    faceMode: FaceMode,
                    stepCount: Long,
                    complicationState: watch.facesapp.core.model.ComplicationState
                ) {
                    // Custom render logic
                }
            }
        }

        TemplateRegistry.register(customTemplate)

        val retrieved = TemplateRegistry.get("CUSTOM_BAUHAUS")
        assertNotNull(retrieved)
        assertEquals("Bauhaus Minimal", retrieved?.displayName)
        assertEquals(2, retrieved?.colorRoles?.size)
        assertEquals(0xFFFFFFFF.toInt(), retrieved?.defaultColors?.get(0))
    }

    @Test
    fun testFaceConfigWithOptions() {
        val config = FaceConfig(
            style = FaceStyle.BUSINESS_TIME,
            options = mapOf("show_seconds" to "true", "timezone" to "UTC")
        )

        assertEquals("true", config.options["show_seconds"])
        assertEquals("UTC", config.options["timezone"])
    }
}
