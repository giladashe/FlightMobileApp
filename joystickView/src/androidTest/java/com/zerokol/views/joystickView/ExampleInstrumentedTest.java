package com.zerokol.views.joystickView;

import android.content.Context;
//todo: these to lines made compilation error so I put them in comment (Asaf).
/*import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;*/

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.zerokol.views.joysticView", appContext.getPackageName());
    }
}
