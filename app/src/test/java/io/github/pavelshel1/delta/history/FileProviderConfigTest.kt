package io.github.pavelshel1.delta.history

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileProviderConfigTest {

    @Test
    fun `manifest declares FileProvider with the expected authority`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:authorities=\"\${applicationId}.fileprovider\""))
        assertTrue(manifest.contains("android:resource=\"@xml/file_paths\""))
    }

    @Test
    fun `file_paths declares the exports cache directory`() {
        val paths = File("src/main/res/xml/file_paths.xml").readText()
        assertTrue(paths.contains("""<cache-path name="exports" path="exports/" />"""))
    }
}
