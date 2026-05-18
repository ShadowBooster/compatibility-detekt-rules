import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.lexer.KtTokens

/**
 * Base class for rules that detect Kotlin language features introduced in a specific version.
 *
 * Reads `language-version` from the rule's detekt config block (e.g. "1.0", "1.3", "2.0").
 * A finding is only reported when the configured language version is strictly less than
 * [stable or expermental since], meaning the feature is unavailable at the declared version.
 *
 * Example detekt.yml:
 * ```yaml
 * LanguageVersionFeatures:
 *   Kotlin11Features:
 *     language-version: '1.0'
 *   Kotlin13Features:
 *     language-version: '1.0'
 * ```
 */
abstract class LanguageVersionFeatureRule(
    config: Config,
    private val experimentalSince: KotlinVersion,
    private val stableSince: KotlinVersion,
) : Rule(config) {

    companion object {
        const val LANGUAGE_VERSION_KEY = "language-version"
        const val LANGUAGE_VERSION_DEFAULT = "2.2"
    }

    abstract val majorIssue: Issue
    abstract val minorIssue: Issue?

    override val issue get() = majorIssue

    private val languageVersion: KotlinVersion by lazy {
        val raw = valueOrDefault(LANGUAGE_VERSION_KEY, LANGUAGE_VERSION_DEFAULT)
        parseKotlinVersion(raw)
    }

    protected fun activeIssue(): Issue? = when {
        languageVersion < experimentalSince -> majorIssue
        languageVersion < stableSince -> minorIssue
        else -> null
    }

    private fun parseKotlinVersion(version: String): KotlinVersion {
        val parts = version.trim().split(".")
        require(parts.size >= 2) {
            "Invalid Kotlin version format '$version'. Expected 'major.minor' (e.g. '1.3')."
        }
        return KotlinVersion(parts[0].toInt(), parts[1].toInt())
    }
}

class Kotlin11FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    experimentalSince = KotlinVersion(1, 1),
    stableSince = KotlinVersion(1, 3),
) {
    override val majorIssue = Issue(
        id = "Kotlin11Features",
        severity = Severity.Defect,
        description = "Detects suspend functions before Kotlin 1.1 (not yet experimental)",
        debt = Debt.FIVE_MINS,
    )

    override val minorIssue = Issue(
        id = "Kotlin11Features",
        severity = Severity.Minor,
        description = "Detects suspend functions before Kotlin 1.3 (experimental only)",
        debt = Debt.FIVE_MINS,
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        val issue = activeIssue()
        if (issue != null && function.modifierList?.hasModifier(KtTokens.SUSPEND_KEYWORD) == true) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(function),
                    message = "Uses suspend function (experimental since Kotlin 1.1, stable since Kotlin 1.3)",
                )
            )
        }
        super.visitNamedFunction(function)
    }
}

class Kotlin13FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    experimentalSince = KotlinVersion(1, 3),
    stableSince = KotlinVersion(1, 5),
) {
    override val majorIssue = Issue(
        id = "Kotlin13Features",
        severity = Severity.Defect,
        description = "Detects Kotlin 1.3+ features (inline classes, unsigned types) before experimental",
        debt = Debt.FIVE_MINS,
    )

    override val minorIssue = Issue(
        id = "Kotlin13Features",
        severity = Severity.Minor,
        description = "Detects Kotlin 1.3+ features (inline classes, unsigned types) before stable",
        debt = Debt.FIVE_MINS, 
    )

    private val unsignedTypes = setOf("UByte", "UInt", "ULong", "UShort")

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val issue = activeIssue()
        if (issue != null && classOrObject.modifierList?.hasModifier(KtTokens.INLINE_KEYWORD) == true) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(classOrObject),
                    message = "Uses inline class (experimental since Kotlin 1.3, stable since Kotlin 1.5)",
                )
            )
        }
        super.visitClassOrObject(classOrObject)
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        val issue = activeIssue()
        if (issue != null) {
            val importedName = importDirective.importedFqName?.shortName()?.asString()
            if (importedName in unsignedTypes) {
                report(
                    CodeSmell(
                        issue = issue,
                        entity = Entity.from(importDirective),
                        message = "Uses unsigned types (experimental since Kotlin 1.3, stable since Kotlin 1.5)",
                    )
                )
            }
        }
        super.visitImportDirective(importDirective)
    }
}

class Kotlin15FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    experimentalSince = KotlinVersion(1, 5),
    stableSince = KotlinVersion(1, 5),
) {
    override val majorIssue = Issue(
        id = "Kotlin15Features",
        severity = Severity.Defect,
        description = "Detects @JvmInline value classes before Kotlin 1.5",
        debt = Debt.FIVE_MINS,
    )

    override val minorIssue: Issue? = null

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        val issue = activeIssue()
        if (issue != null && annotationEntry.shortName?.asString() == "JvmInline") {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(annotationEntry),
                    message = "Uses @JvmInline value class (stable since Kotlin 1.5)",
                )
            )
        }
        super.visitAnnotationEntry(annotationEntry)
    }
}

class Kotlin19FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    experimentalSince = KotlinVersion(1, 8),
    stableSince = KotlinVersion(1, 9),
) {
    override val majorIssue = Issue(
        id = "Kotlin19Features",
        severity = Severity.Defect,
        description = "Detects data objects before Kotlin 1.8 (not yet experimental)",
        debt = Debt.FIVE_MINS,
    )

    override val minorIssue = Issue(
        id = "Kotlin19Features",
        severity = Severity.Minor,
        description = "Detects data objects before Kotlin 1.9 (experimental only)",
        debt = Debt.FIVE_MINS,
    )

    override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
        val issue = activeIssue()
        if (issue != null &&
            !declaration.isCompanion() &&
            declaration.modifierList?.hasModifier(KtTokens.DATA_KEYWORD) == true
        ) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(declaration),
                    message = "Uses data object (experimental since Kotlin 1.8, stable since Kotlin 1.9)",
                )
            )
        }
        super.visitObjectDeclaration(declaration)
    }
}