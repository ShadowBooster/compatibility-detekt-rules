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
 * [sinceVersion], meaning the feature is unavailable at the declared version.
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
    private val sinceVersion: KotlinVersion,
) : Rule(config) {

    companion object {
        const val LANGUAGE_VERSION_KEY = "language-version"
        const val LANGUAGE_VERSION_DEFAULT = "2.2"
    }

    /**
     * The configured target language version, parsed lazily once per rule instance.
     * Defaults to 2.2 (i.e. no findings) when not explicitly configured.
     */
    private val languageVersion: KotlinVersion by lazy {
        val raw = valueOrDefault(LANGUAGE_VERSION_KEY, LANGUAGE_VERSION_DEFAULT)
        parseKotlinVersion(raw)
    }

    /**
     * Returns true when the feature introduced in [sinceVersion] would not be
     * available at the configured [languageVersion].
     */
    protected fun featureUnavailable(): Boolean = languageVersion < sinceVersion

    private fun parseKotlinVersion(version: String): KotlinVersion {
        val parts = version.trim().split(".")
        require(parts.size >= 2) {
            "Invalid Kotlin version format '$version'. Expected 'major.minor' (e.g. '1.3')."
        }
        return KotlinVersion(parts[0].toInt(), parts[1].toInt())
    }
}

// ---------------------------------------------------------------------------
// Kotlin 1.1 — suspend functions
// ---------------------------------------------------------------------------

class Kotlin11FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    sinceVersion = KotlinVersion(1, 1),
) {
    override val issue = Issue(
        id = "Kotlin11Features",
        severity = Severity.Minor,
        description = "Detects Kotlin 1.1+ features (suspend functions)",
        debt = Debt.FIVE_MINS,
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        if (featureUnavailable() &&
            function.modifierList?.hasModifier(KtTokens.SUSPEND_KEYWORD) == true
        ) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(function),
                    message = "Uses suspend function (Kotlin 1.1+)",
                )
            )
        }
        super.visitNamedFunction(function)
    }
}

// ---------------------------------------------------------------------------
// Kotlin 1.3 — inline classes, unsigned types
// ---------------------------------------------------------------------------

class Kotlin13FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    sinceVersion = KotlinVersion(1, 3),
) {
    override val issue = Issue(
        id = "Kotlin13Features",
        severity = Severity.Minor,
        description = "Detects Kotlin 1.3+ features (inline classes, unsigned types)",
        debt = Debt.FIVE_MINS,
    )

    private val unsignedTypes = setOf("UByte", "UInt", "ULong", "UShort")

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        if (featureUnavailable() &&
            classOrObject.modifierList?.hasModifier(KtTokens.INLINE_KEYWORD) == true
        ) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(classOrObject),
                    message = "Uses inline class (Kotlin 1.3+)",
                )
            )
        }
        super.visitClassOrObject(classOrObject)
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        if (featureUnavailable()) {
            val importedName = importDirective.importedFqName?.shortName()?.asString()
            if (importedName in unsignedTypes) {
                report(
                    CodeSmell(
                        issue = issue,
                        entity = Entity.from(importDirective),
                        message = "Uses unsigned types (Kotlin 1.3+)",
                    )
                )
            }
        }
        super.visitImportDirective(importDirective)
    }
}

// ---------------------------------------------------------------------------
// Kotlin 1.5 — @JvmInline value classes
// ---------------------------------------------------------------------------

class Kotlin15FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    sinceVersion = KotlinVersion(1, 5),
) {
    override val issue = Issue(
        id = "Kotlin15Features",
        severity = Severity.Minor,
        description = "Detects Kotlin 1.5+ features (@JvmInline value classes)",
        debt = Debt.FIVE_MINS,
    )

    override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
        if (featureUnavailable() &&
            annotationEntry.shortName?.asString() == "JvmInline"
        ) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(annotationEntry),
                    message = "Uses @JvmInline value class (Kotlin 1.5+)",
                )
            )
        }
        super.visitAnnotationEntry(annotationEntry)
    }
}

// ---------------------------------------------------------------------------
// Kotlin 2.0 — data objects
// ---------------------------------------------------------------------------

class Kotlin20FeaturesRule(config: Config = Config.empty) : LanguageVersionFeatureRule(
    config,
    sinceVersion = KotlinVersion(2, 0),
) {
    override val issue = Issue(
        id = "Kotlin20Features",
        severity = Severity.Minor,
        description = "Detects Kotlin 2.0+ features (data objects)",
        debt = Debt.FIVE_MINS,
    )

    override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
        if (!declaration.isCompanion() &&
            featureUnavailable() &&
            declaration.modifierList?.hasModifier(KtTokens.DATA_KEYWORD) == true
        ) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(declaration),
                    message = "Uses data object (Kotlin 2.0+)",
                )
            )
        }
        super.visitObjectDeclaration(declaration)
    }
}