package com.zerokol.views.joystickView

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

//todo: these to lines made compilation error so I put them in comment (Asaf).
/*import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;*/ /**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext =
            InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("com.zerokol.views.joysticView", appContext.packageName)
    }
}