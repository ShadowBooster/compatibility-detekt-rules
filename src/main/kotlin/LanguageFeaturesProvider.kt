import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import kotlin.collections.listOf

class LanguageFeaturesProvider : RuleSetProvider {
    override val ruleSetId: String = "language-features"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                Kotlin11FeaturesRule(config),
                Kotlin13FeaturesRule(config),
                Kotlin15FeaturesRule(config),
                Kotlin19FeaturesRule(config),
            )
        )
    }
}
