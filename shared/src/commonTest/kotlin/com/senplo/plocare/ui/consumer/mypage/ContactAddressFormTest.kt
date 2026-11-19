package com.senplo.plocare.ui.consumer.mypage

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContactAddressFormTest {
    @Test
    fun phoneInputKeepsElevenDigitsOnly() {
        assertEquals("01012345678", sanitizePhoneDigits("010-1234-5678 9"))
        assertEquals("", sanitizePhoneDigits("abc"))
    }

    @Test
    fun phoneNumberIsGroupedThreeFourFour() {
        assertEquals("010", formatPhoneNumber("010"))
        assertEquals("010-1", formatPhoneNumber("0101"))
        assertEquals("010-1234-5", formatPhoneNumber("01012345"))
        assertEquals("010-1234-5678", formatPhoneNumber("01012345678"))
    }

    @Test
    fun cursorMapsAcrossHyphens() {
        val mapping = PhoneNumberTransformation.filter(AnnotatedString("01012345678")).offsetMapping
        assertEquals(3, mapping.originalToTransformed(3))
        assertEquals(5, mapping.originalToTransformed(4))
        assertEquals(13, mapping.originalToTransformed(11))
        assertEquals(3, mapping.transformedToOriginal(4))
        assertEquals(7, mapping.transformedToOriginal(9))
        assertEquals(11, mapping.transformedToOriginal(13))
    }

    @Test
    fun onlyMobileNumbersAreAccepted() {
        assertTrue(isValidMobileNumber("01012345678"))
        assertTrue(isValidMobileNumber("0111234567"))
        assertFalse(isValidMobileNumber("0212345678"))
        assertFalse(isValidMobileNumber("010123"))
    }

    @Test
    fun noteIsCappedAtThirtyCharacters() {
        val long = "가".repeat(40)
        assertEquals(30, limitContactNote(long).length)
        assertEquals("0/30", contactNoteCounter(""))
        assertEquals("30/30", contactNoteCounter(limitContactNote(long)))
    }

    @Test
    fun contactBasicsNeedPhoneAndAddressAndNameOnlyInTheSheet() {
        val draft = ContactAddressDraft(phoneDigits = "01012345678", address = "서울 성동구 성수일로 10")
        assertTrue(draft.hasContactBasics(requireName = false))
        assertFalse(draft.hasContactBasics(requireName = true))
        assertTrue(draft.copy(name = "홍길동").hasContactBasics(requireName = true))
        assertFalse(draft.copy(address = " ").hasContactBasics(requireName = false))
        assertFalse(draft.copy(phoneDigits = "0101234").hasContactBasics(requireName = false))
    }

    @Test
    fun agreementNeedsBothConsentsAndASignature() {
        val consented = ContactAddressDraft()
            .withConsent(ContactConsent.PRIVACY, true)
            .withConsent(ContactConsent.PARTNER_SHARING, true)
        assertTrue(consented.hasRequiredAgreement(signed = true))
        assertFalse(consented.hasRequiredAgreement(signed = false))
        assertFalse(consented.withConsent(ContactConsent.PARTNER_SHARING, false).hasRequiredAgreement(signed = true))
    }
}
