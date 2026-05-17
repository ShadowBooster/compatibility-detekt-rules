import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private fun configWithVersion(version: String) =
    TestConfig(LanguageVersionFeatureRule.LANGUAGE_VERSION_KEY to version)

class Kotlin11FeaturesRuleTest {

    private val suspendCode = """
        suspend fun fetchData(): String = "data"
    """.trimIndent()

    private val plainCode = """
        fun fetchData(): String = "data"
    """.trimIndent()

    @Nested
    inner class `language-version below 1_1` {

        private val rule = Kotlin11FeaturesRule(configWithVersion("1.0"))

        @Test
        fun `reports a suspend function`() {
            rule.lint(suspendCode) shouldHaveSize 1
        }

        @Test
        fun `reports message correctly`() {
            rule.lint(suspendCode).first().message shouldBe "Uses suspend function (Kotlin 1.1+)"
        }

        @Test
        fun `does not report a plain function`() {
            rule.lint(plainCode).shouldBeEmpty()
        }

        @Test
        fun `reports multiple suspend functions`() {
            rule.lint(
                """
                suspend fun a() {}
                suspend fun b() {}
                fun c() {}
                """.trimIndent()
            ) shouldHaveSize 2
        }
    }

    @Nested
    inner class `language-version at 1_1` {

        private val rule = Kotlin11FeaturesRule(configWithVersion("1.1"))

        @Test
        fun `does not report a suspend function when version matches`() {
            rule.lint(suspendCode).shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version above 1_1` {

        private val rule = Kotlin11FeaturesRule(configWithVersion("2.0"))

        @Test
        fun `does not report a suspend function`() {
            rule.lint(suspendCode).shouldBeEmpty()
        }
    }

    @Nested
    inner class `default language-version` {

        private val rule = Kotlin11FeaturesRule()

        @Test
        fun `does not report when using default version`() {
            rule.lint(suspendCode).shouldBeEmpty()
        }
    }
}

class Kotlin13FeaturesRuleTest {

    private val inlineClassCode = "inline class Wrapper(val value: String)"
    private val uintImportCode  = "import kotlin.UInt"

    @Nested
    inner class `language-version below 1_3` {

        private val rule = Kotlin13FeaturesRule(configWithVersion("1.0"))

        @Test
        fun `reports inline class`() {
            rule.lint(inlineClassCode) shouldHaveSize 1
        }

        @Test
        fun `reports UInt import`() {
            rule.lint(uintImportCode) shouldHaveSize 1
        }

        @Test
        fun `reports UByte import`() {
            rule.lint("import kotlin.UByte") shouldHaveSize 1
        }

        @Test
        fun `reports ULong import`() {
            rule.lint("import kotlin.ULong") shouldHaveSize 1
        }

        @Test
        fun `reports UShort import`() {
            rule.lint("import kotlin.UShort") shouldHaveSize 1
        }

        @Test
        fun `reports all four unsigned imports`() {
            rule.lint(
                """
                import kotlin.UByte
                import kotlin.UInt
                import kotlin.ULong
                import kotlin.UShort
                """.trimIndent()
            ) shouldHaveSize 4
        }

        @Test
        fun `reports UInt imported with an alias`() {
            rule.lint("import kotlin.UInt as MyUInt") shouldHaveSize 1
        }

        @Test
        fun `does not report a plain class`() {
            rule.lint("class Wrapper(val value: String)").shouldBeEmpty()
        }

        @Test
        fun `does not report an unrelated import`() {
            rule.lint("import com.example.Foo").shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version at 1_3` {

        private val rule = Kotlin13FeaturesRule(configWithVersion("1.3"))

        @Test
        fun `does not report inline class when version matches`() {
            rule.lint(inlineClassCode).shouldBeEmpty()
        }

        @Test
        fun `does not report unsigned import when version matches`() {
            rule.lint(uintImportCode).shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version above 1_3` {

        private val rule = Kotlin13FeaturesRule(configWithVersion("1.1"))

        @Test
        fun `reports inline class for version 1_1`() {
            rule.lint(inlineClassCode) shouldHaveSize 1
        }

        @Test
        fun `reports unsigned import for version 1_1`() {
            rule.lint(uintImportCode) shouldHaveSize 1
        }
    }
}

class Kotlin15FeaturesRuleTest {

    private val jvmInlineCode = """
        @JvmInline
        value class Password(val raw: String)
    """.trimIndent()

    @Nested
    inner class `language-version below 1_5` {

        private val rule = Kotlin15FeaturesRule(configWithVersion("1.0"))

        @Test
        fun `reports JvmInline annotation`() {
            rule.lint(jvmInlineCode) shouldHaveSize 1
        }

        @Test
        fun `reports message correctly`() {
            rule.lint(jvmInlineCode).first().message shouldBe
                "Uses @JvmInline value class (Kotlin 1.5+)"
        }

        @Test
        fun `reports multiple JvmInline annotations`() {
            rule.lint(
                """
                @JvmInline
                value class Name(val v: String)

                @JvmInline
                value class Age(val v: Int)
                """.trimIndent()
            ) shouldHaveSize 2
        }

        @Test
        fun `does not report an unrelated annotation`() {
            rule.lint("""@Suppress("unused") class Foo""").shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version at 1_5` {

        private val rule = Kotlin15FeaturesRule(configWithVersion("1.5"))

        @Test
        fun `does not report when version matches`() {
            rule.lint(jvmInlineCode).shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version 1_3 — still below 1_5` {

        private val rule = Kotlin15FeaturesRule(configWithVersion("1.3"))

        @Test
        fun `reports JvmInline for version 1_3`() {
            rule.lint(jvmInlineCode) shouldHaveSize 1
        }
    }
}

class Kotlin20FeaturesRuleTest {

    private val dataObjectCode  = "data object Singleton"
    private val plainObjectCode = "object MySingleton { fun greet() = \"hello\" }"

    @Nested
    inner class `language-version below 2_0` {

        private val rule = Kotlin20FeaturesRule(configWithVersion("1.0"))

        @Test
        fun `reports a data object`() {
            rule.lint(dataObjectCode) shouldHaveSize 1
        }

        @Test
        fun `reports message correctly`() {
            rule.lint(dataObjectCode).first().message shouldBe "Uses data object (Kotlin 2.0+)"
        }

        @Test
        fun `reports multiple data objects`() {
            rule.lint(
                """
                data object A
                data object B
                """.trimIndent()
            ) shouldHaveSize 2
        }

        @Test
        fun `does not report a plain object`() {
            rule.lint(plainObjectCode).shouldBeEmpty()
        }

        @Test
        fun `does not report a companion object`() {
            rule.lint(
                """
                class MyClass {
                    companion object { fun create() = MyClass() }
                }
                """.trimIndent()
            ).shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version at 2_0` {

        private val rule = Kotlin20FeaturesRule(configWithVersion("2.0"))

        @Test
        fun `does not report a data object when version matches`() {
            rule.lint(dataObjectCode).shouldBeEmpty()
        }
    }

    @Nested
    inner class `language-version 1_9 — still below 2_0` {

        private val rule = Kotlin20FeaturesRule(configWithVersion("1.9"))

        @Test
        fun `reports data object for version 1_9`() {
            rule.lint(dataObjectCode) shouldHaveSize 1
        }
    }

    @Nested
    inner class `default language-version` {

        private val rule = Kotlin20FeaturesRule()

        @Test
        fun `does not report when using default version`() {
            rule.lint(dataObjectCode).shouldBeEmpty()
        }
    }
}