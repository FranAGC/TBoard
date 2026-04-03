package helium314.keyboard.latin.utils

object TranslatorServiceFactory {
    fun getService(serviceName: String): TranslationService {
        return when (serviceName) {
            "groq" -> GroqTranslationService()
            else -> GroqTranslationService() // defaults to Groq
        }
    }
}
