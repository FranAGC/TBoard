package helium314.keyboard.latin.utils

interface TranslationService {
    fun translate(text: String, apiKey: String, language: String): String?
}
