package com.charm.phr

import android.util.Log
import com.charm.phr.mphr.util.UIUtil
import org.junit.Assert.*
import org.junit.Test
import java.util.logging.Logger

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun formMemberInitials_Two_Word_Is_Correct() {

        assertEquals(UIUtil.formMemberInitials("Jhone Doe"), "JD")
        assertEquals(UIUtil.formMemberInitials("Jhone D"), "JD")


    }
    @Test
    fun formMemberInitials_Three_Word_Is_Correct() {
        assertEquals(UIUtil.formMemberInitials("P.Jhone Doe Hauk"), "PJ")
        assertEquals(UIUtil.formMemberInitials("Dr. Jhone Doe Hauk"), "JD")
        assertEquals(UIUtil.formMemberInitials("Dr. P.Jhone Doe Hauk"), "PJ")
//        assertEquals(UIUtil.formMemberInitials("Jhone.J"), "JJ")
//        assertEquals(UIUtil.formMemberInitials("Jhone  "), "J")
    }

    @Test
    fun formMemberInitials_Single_Word_Is_Correct() {
        assertEquals(UIUtil.formMemberInitials(" Jhone"), "J")
        assertEquals(UIUtil.formMemberInitials(" Jhone."), "J")
        assertEquals(UIUtil.formMemberInitials("Jhone "), "J")
        assertEquals(UIUtil.formMemberInitials("J"), "J")
        assertEquals(UIUtil.formMemberInitials(" J"), "J")
        assertEquals(UIUtil.formMemberInitials(" J "), "J")
    }


}
