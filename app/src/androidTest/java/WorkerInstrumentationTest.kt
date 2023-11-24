import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkerInstrumentationTest {
    lateinit var context: Context
    val mockUriInput =
        KEY_IMAGE_URI to "android.resource://com.example.bluromatic/drawable/android_cupcake"
    val mockBlurLevelInput = KEY_BLUR_LEVEL to 1

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun cleanupWorker_doWork_resultSuccess() {
        val cleanupWorker = TestListenableWorkerBuilder<CleanupWorker>(context).build()
        runBlocking {
            val result = cleanupWorker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }
    }

    @Test
    fun blurWorker_doWork_resultSuccessReturnsUri() {
        val blurWorker = TestListenableWorkerBuilder<BlurWorker>(context)
            .setInputData(workDataOf(mockUriInput, mockBlurLevelInput))
            .build()

        runBlocking {
            val result = blurWorker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
            val resultUri = result.outputData.getString(KEY_IMAGE_URI)
            assertTrue(result.outputData.keyValueMap.containsKey(KEY_IMAGE_URI))
            assertTrue(
                resultUri?.startsWith("file:///data/user/0/com.example.bluromatic/files/blur_filter_outputs/blur-filter-output-")
                    ?: false
            )
        }
    }

    @Test
    fun saveFileWorker_doWork_resultSuccess() {
        val saveImageWorker = TestListenableWorkerBuilder<SaveImageToFileWorker>(context)
            .setInputData(workDataOf(mockUriInput))
            .build()

        runBlocking {
            val result = saveImageWorker.doWork()
            val resultUri = result.outputData.getString(KEY_IMAGE_URI)
            assertTrue(result is ListenableWorker.Result.Success)
            assertTrue(result.outputData.keyValueMap.containsKey(KEY_IMAGE_URI))
            assertTrue(resultUri?.startsWith("content://media/external/images/media/") ?: false)

        }
    }
}