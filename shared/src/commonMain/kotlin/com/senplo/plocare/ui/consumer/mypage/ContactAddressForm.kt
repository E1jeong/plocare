package com.senplo.plocare.ui.consumer.mypage

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

internal const val CONTACT_NOTE_MAX_LENGTH = 30
private const val MOBILE_NUMBER_MAX_DIGITS = 11

/** Contact, address, and consents shared by Figma 11 (114:1721) and its first-use sheets (79:1713, 180:1079). */
internal data class ContactAddressDraft(
    val name: String = "",
    val phoneDigits: String = "",
    val address: String = "",
    val addressDetail: String = "",
    val note: String = "",
    val privacyConsent: Boolean = false,
    val partnerConsent: Boolean = false,
)

internal enum class ContactConsent(val label: String) {
    PRIVACY("[필수] 개인정보 수집•이용 동의"),
    PARTNER_SHARING("[필수] 담당 기사• 센터 제공 (번호는 안심번호로)"),
}

internal fun sanitizePhoneDigits(raw: String): String =
    raw.filter { it.isDigit() }.take(MOBILE_NUMBER_MAX_DIGITS)

/** Progressive 3-4-4 grouping that matches the `010-0000-0000` placeholder. */
internal fun formatPhoneNumber(digits: String): String = buildString {
    digits.forEachIndexed { index, digit ->
        if (index == 3 || index == 7) append('-')
        append(digit)
    }
}

internal fun isValidMobileNumber(digits: String): Boolean =
    digits.length in 10..MOBILE_NUMBER_MAX_DIGITS && digits.startsWith("01")

internal fun limitContactNote(raw: String): String = raw.take(CONTACT_NOTE_MAX_LENGTH)

internal fun contactNoteCounter(note: String): String = "${note.length}/$CONTACT_NOTE_MAX_LENGTH"

/** Sheet step 1 (79:1713) asks for the name; the full screen (114:1721) does not. */
internal fun ContactAddressDraft.hasContactBasics(requireName: Boolean): Boolean =
    (!requireName || name.isNotBlank()) && isValidMobileNumber(phoneDigits) && address.isNotBlank()

internal fun ContactAddressDraft.hasRequiredAgreement(signed: Boolean): Boolean =
    privacyConsent && partnerConsent && signed

internal fun ContactAddressDraft.consented(consent: ContactConsent): Boolean = when (consent) {
    ContactConsent.PRIVACY -> privacyConsent
    ContactConsent.PARTNER_SHARING -> partnerConsent
}

internal fun ContactAddressDraft.withConsent(consent: ContactConsent, agreed: Boolean): ContactAddressDraft =
    when (consent) {
        ContactConsent.PRIVACY -> copy(privacyConsent = agreed)
        ContactConsent.PARTNER_SHARING -> copy(partnerConsent = agreed)
    }

/** Shows stored digits with hyphens while keeping the cursor on the matching digit. */
internal object PhoneNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = formatPhoneNumber(digits)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + (if (offset > 3) 1 else 0) + (if (offset > 7) 1 else 0)

            override fun transformedToOriginal(offset: Int): Int =
                (offset - (if (offset > 3) 1 else 0) - (if (offset > 8) 1 else 0)).coerceIn(0, digits.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
