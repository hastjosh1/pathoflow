package com.example.ui.translation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

enum class Language(val code: String) {
    ENGLISH("en"),
    GUJARATI("gu")
}

object Translations {
    private val dictionary = mapOf(
        "app_title" to mapOf(Language.ENGLISH to "Accurate Lab Collection", Language.GUJARATI to "એક્યુરેટ લેબ કલેક્શન"),
        "login" to mapOf(Language.ENGLISH to "Login", Language.GUJARATI to "લોગિન"),
        "secure_login" to mapOf(Language.ENGLISH to "Secure Pathology Login", Language.GUJARATI to "સુરક્ષિત પેથોલોજી લોગિન"),
        "username" to mapOf(Language.ENGLISH to "Username", Language.GUJARATI to "વપરાશકર્તા નામ"),
        "password" to mapOf(Language.ENGLISH to "PIN / Password", Language.GUJARATI to "PIN / પાસવર્ડ"),
        "remember_me" to mapOf(Language.ENGLISH to "Remember Login", Language.GUJARATI to "લોગિન યાદ રાખો"),
        "login_error" to mapOf(Language.ENGLISH to "Invalid username or PIN", Language.GUJARATI to "ખોટું યુઝરનામ અથવા પિન"),
        "dashboard" to mapOf(Language.ENGLISH to "Dashboard", Language.GUJARATI to "ડેશબોર્ડ"),
        "today_patients" to mapOf(Language.ENGLISH to "Today's Patients", Language.GUJARATI to "આજના દર્દીઓ"),
        "today_revenue" to mapOf(Language.ENGLISH to "Today's Revenue", Language.GUJARATI to "આજની આવક"),
        "pending_payments" to mapOf(Language.ENGLISH to "Pending Payments", Language.GUJARATI to "બાકી ચૂકવણી"),
        "recent_entries" to mapOf(Language.ENGLISH to "Recent Entries", Language.GUJARATI to "તાજેતરની નોંધણીઓ"),
        "new_entry" to mapOf(Language.ENGLISH to "New Patient Entry", Language.GUJARATI to "નવા દર્દીની નોંધણી"),
        "edit_entry" to mapOf(Language.ENGLISH to "Edit Patient", Language.GUJARATI to "દર્દીની માહિતી સુધારો"),
        "manage_tests" to mapOf(Language.ENGLISH to "Manage Tests", Language.GUJARATI to "ટેસ્ટ મેનેજમેન્ટ"),
        "reports" to mapOf(Language.ENGLISH to "Reports & Analytics", Language.GUJARATI to "રિપોર્ટ્સ અને એનાલિટિક્સ"),
        "settings" to mapOf(Language.ENGLISH to "Settings", Language.GUJARATI to "સેટિંગ્સ"),
        "logout" to mapOf(Language.ENGLISH to "Logout", Language.GUJARATI to "લોગઆઉટ"),
        "patient_name" to mapOf(Language.ENGLISH to "Patient Name", Language.GUJARATI to "દર્દીનું નામ"),
        "age" to mapOf(Language.ENGLISH to "Age (Years)", Language.GUJARATI to "ઉંમર (વર્ષ)"),
        "gender" to mapOf(Language.ENGLISH to "Gender / Sex", Language.GUJARATI to "લિંગ / જાતિ"),
        "male" to mapOf(Language.ENGLISH to "Male", Language.GUJARATI to "પુરુષ"),
        "female" to mapOf(Language.ENGLISH to "Female", Language.GUJARATI to "સ્ત્રી"),
        "other" to mapOf(Language.ENGLISH to "Other", Language.GUJARATI to "અન્ય"),
        "doctor_name" to mapOf(Language.ENGLISH to "Referred Doctor Name", Language.GUJARATI to "સંદર્ભિત ડૉક્ટરનું નામ"),
        "test_selection" to mapOf(Language.ENGLISH to "Test Selection", Language.GUJARATI to "ટેસ્ટ પસંદગી"),
        "search_tests" to mapOf(Language.ENGLISH to "Search tests quickly...", Language.GUJARATI to "ઝડપથી ટેસ્ટ શોધો..."),
        "mobile_number" to mapOf(Language.ENGLISH to "Mobile Number (WhatsApp)", Language.GUJARATI to "મોબાઇલ નંબર (વોટ્સએપ)"),
        "email" to mapOf(Language.ENGLISH to "Email Address", Language.GUJARATI to "ઇમેઇલ સરનામું"),
        "address" to mapOf(Language.ENGLISH to "Address", Language.GUJARATI to "સરનામું"),
        "collection_type" to mapOf(Language.ENGLISH to "Collection Type", Language.GUJARATI to "કલેક્શન પ્રકાર"),
        "home_collection" to mapOf(Language.ENGLISH to "Home Collection", Language.GUJARATI to "ઘર બેઠા સેમ્પલ કલેક્શન"),
        "lab_visit" to mapOf(Language.ENGLISH to "Lab Visit", Language.GUJARATI to "લેબ મુલાકાત"),
        "total_amount" to mapOf(Language.ENGLISH to "Total Amount", Language.GUJARATI to "કુલ રકમ"),
        "payment_status" to mapOf(Language.ENGLISH to "Payment Status", Language.GUJARATI to "ચૂકવણીની સ્થિતિ"),
        "payment_mode" to mapOf(Language.ENGLISH to "Payment Mode", Language.GUJARATI to "ચૂકવણીનો પ્રકાર"),
        "paid" to mapOf(Language.ENGLISH to "Paid", Language.GUJARATI to "ચૂકવેલ"),
        "partial" to mapOf(Language.ENGLISH to "Partial", Language.GUJARATI to "અંશતઃ ચૂકવેલ"),
        "pending" to mapOf(Language.ENGLISH to "Pending", Language.GUJARATI to "બાકી"),
        "upi" to mapOf(Language.ENGLISH to "UPI (QR)", Language.GUJARATI to "UPI (ક્યુઆર કોડ)"),
        "cash" to mapOf(Language.ENGLISH to "Cash", Language.GUJARATI to "રોકડા"),
        "card" to mapOf(Language.ENGLISH to "Card", Language.GUJARATI to "કાર્ડ"),
        "credit" to mapOf(Language.ENGLISH to "Credit", Language.GUJARATI to "ક્રેડિટ / ઉધાર"),
        "save_entry" to mapOf(Language.ENGLISH to "Save Patient Entry", Language.GUJARATI to "દર્દીની માહિતી સાચવો"),
        "send_to_whatsapp" to mapOf(Language.ENGLISH to "Send to Lab WhatsApp", Language.GUJARATI to "લેબ વોટ્સએપ પર મોકલો"),
        "tests_selected" to mapOf(Language.ENGLISH to "Tests Selected", Language.GUJARATI to "પસંદ કરેલ ટેસ્ટ"),
        "frequently_used" to mapOf(Language.ENGLISH to "Frequently Used", Language.GUJARATI to "વારંવાર વપરાતી શોર્ટક્ટ"),
        "recent_tests" to mapOf(Language.ENGLISH to "Recent / Pinneds", Language.GUJARATI to "તાજેતરના / પિન કરેલ"),
        "amount_payable" to mapOf(Language.ENGLISH to "Amount Payable", Language.GUJARATI to "ચૂકવવાપાત્ર રકમ"),
        "amount_paid_input" to mapOf(Language.ENGLISH to "Amount Paid", Language.GUJARATI to "ચૂકવેલી રકમ"),
        "add_new_test" to mapOf(Language.ENGLISH to "Add New Test", Language.GUJARATI to "નવો ટેસ્ટ ઉમેરો"),
        "edit_test" to mapOf(Language.ENGLISH to "Edit Test", Language.GUJARATI to "ટેસ્ટ સુધારો"),
        "test_name" to mapOf(Language.ENGLISH to "Test Name", Language.GUJARATI to "ટેસ્ટનું નામ"),
        "price" to mapOf(Language.ENGLISH to "Price (₹)", Language.GUJARATI to "કિંમત (₹)"),
        "category" to mapOf(Language.ENGLISH to "Category", Language.GUJARATI to "કેટેગરી"),
        "save" to mapOf(Language.ENGLISH to "Save", Language.GUJARATI to "સાચવો"),
        "cancel" to mapOf(Language.ENGLISH to "Cancel", Language.GUJARATI to "રદ કરો"),
        "delete" to mapOf(Language.ENGLISH to "Delete", Language.GUJARATI to "કાઢી નાખો"),
        "confirm_delete_patient" to mapOf(Language.ENGLISH to "Are you sure you want to delete this patient entry?", Language.GUJARATI to "શું તમે આ દર્દીની એન્ટ્રી કાઢી નાખવા માંગો છો?"),
        "confirm_delete_test" to mapOf(Language.ENGLISH to "Are you sure you want to delete this test?", Language.GUJARATI to "શું તમે આ ટેસ્ટ કાઢી નાખવા માંગો છો?"),
        "collection_status" to mapOf(Language.ENGLISH to "Collection Status", Language.GUJARATI to "સેમ્પલ સ્થિતિ"),
        "sample_collected" to mapOf(Language.ENGLISH to "Sample Collected", Language.GUJARATI to "સેમ્પલ એકત્રિત કરેલ"),
        "sent_to_lab" to mapOf(Language.ENGLISH to "Sent to Lab", Language.GUJARATI to "લેબમાં મોકલેલ"),
        "report_ready" to mapOf(Language.ENGLISH to "Report Ready", Language.GUJARATI to "રિપોર્ટ તૈયાર છે"),
        "delivered" to mapOf(Language.ENGLISH to "Delivered to Patient", Language.GUJARATI to "દર્દીને ડિલિવર કરેલ"),
        "search_patient_placeholder" to mapOf(Language.ENGLISH to "Search by Name, Doctor or ID...", Language.GUJARATI to "નામ, ડૉક્ટર અથવા આઈડીથી શોધો..."),
        "admin_pin_required" to mapOf(Language.ENGLISH to "Admin Security PIN Required", Language.GUJARATI to "એડમિન સુરક્ષા પિન જરૂરી"),
        "enter_admin_pin" to mapOf(Language.ENGLISH to "Enter 4-digit Admin PIN", Language.GUJARATI to "૪-અંકનો એડમિન પિન દાખલ કરો"),
        "submit" to mapOf(Language.ENGLISH to "Submit", Language.GUJARATI to "સબમિટ"),
        "incorrect_pin" to mapOf(Language.ENGLISH to "Incorrect Security PIN!", Language.GUJARATI to "ખોટો સુરક્ષા પિન!"),
        "daily_summary" to mapOf(Language.ENGLISH to "Daily Collections Summary", Language.GUJARATI to "દૈનિક સંગ્રહ સારાંશ"),
        "revenue_summary" to mapOf(Language.ENGLISH to "Revenue Summary", Language.GUJARATI to "આવકનો સારાંશ"),
        "export_csv" to mapOf(Language.ENGLISH to "Export Report to CSV", Language.GUJARATI to "રિપોર્ટ CSV માં નિકાસ કરો"),
        "export_success" to mapOf(Language.ENGLISH to "Successfully exported data offline!", Language.GUJARATI to "ડેટા ઓફલાઇન નિકાસ થઈ ગયો!"),
        "backup_db" to mapOf(Language.ENGLISH to "Local Database Safety Backup", Language.GUJARATI to "લોકલ ડેટાબેઝ સેફ્ટી બેકઅપ"),
        "backup_now" to mapOf(Language.ENGLISH to "Backup & Reset", Language.GUJARATI to "બેકઅપ અને રીસેટ"),
        "lab_whatsapp" to mapOf(Language.ENGLISH to "WhatsApp Numbers (comma separated)", Language.GUJARATI to "વોટ્સએપ નંબર્સ (અલ્પવિરામથી જોડો)"),
        "upi_id_config" to mapOf(Language.ENGLISH to "Lab Merchant UPI ID", Language.GUJARATI to "લેબ મર્ચન્ટ UPI ID"),
        "upi_name_config" to mapOf(Language.ENGLISH to "Lab Merchant Name", Language.GUJARATI to "લેબ મર્ચન્ટ નામ"),
        "admin_pin_config" to mapOf(Language.ENGLISH to "Change Admin Security PIN", Language.GUJARATI to "એડમિન સુરક્ષા પિન બદલો"),
        "registered_by" to mapOf(Language.ENGLISH to "Registered By", Language.GUJARATI to "નોંધણી કરનાર"),
        "collected_by_label" to mapOf(Language.ENGLISH to "Collected By", Language.GUJARATI to "એકત્રિત કરનાર"),
        "voice_name" to mapOf(Language.ENGLISH to "Voice Record Patient Name", Language.GUJARATI to "નામ રેકોર્ડ કરવા બોલો"),
        "voice_listening" to mapOf(Language.ENGLISH to "Listening... Speak name clearly.", Language.GUJARATI to "સાંભળી રહ્યા છીએ... દર્દીનું નામ બોલો."),
        "duplicate_patient" to mapOf(Language.ENGLISH to "Duplicate Dynamic Entry", Language.GUJARATI to "નકલ બનાવો (ક્વિક રી-ફિલ)"),
        "future_ready_title" to mapOf(Language.ENGLISH to "Future-ready cloud & printer features enabled", Language.GUJARATI to "પ્રિન્ટર અને ક્લાઉડ સિંક સપોર્ટ સક્રિય છે"),
        "today" to mapOf(Language.ENGLISH to "Today", Language.GUJARATI to "આજે")
    )

    fun getString(key: String, lang: Language): String {
        return dictionary[key]?.get(lang) ?: key
    }
}

@Composable
@ReadOnlyComposable
fun translate(key: String, language: Language): String {
    return Translations.getString(key, language)
}
