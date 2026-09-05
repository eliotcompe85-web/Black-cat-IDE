package com.ide.mobile.feature.editor

import com.ide.mobile.core.model.LanguageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartCodeFormatterTest {

    @Test
    fun testFormatDartCode() {
        val unformattedDart = """
void main(){
runApp(const MyApp());
}
class MyApp extends StatelessWidget{
@override
Widget build(BuildContext context){
return Container();
}
}
        """.trimIndent()

        val formatted = SmartCodeFormatter.format(unformattedDart, LanguageType.DART)

        assertTrue("Debe indentar el cuerpo de la función", formatted.contains("  runApp(const MyApp());"))
        assertTrue("Debe indentar el método dentro de la clase", formatted.contains("  Widget build(BuildContext context){"))
        assertTrue("Debe indentar el return con 4 espacios anidados", formatted.contains("    return Container();"))
    }

    @Test
    fun testFormatKotlinCode() {
        val unformattedKotlin = """
fun calculateTotal(price: Double): Double {
val tax = 0.16
return price * (1 + tax)
}
        """.trimIndent()

        val formatted = SmartCodeFormatter.format(unformattedKotlin, LanguageType.KOTLIN)

        assertTrue("Kotlin debe usar 4 espacios de indentación", formatted.contains("    val tax = 0.16"))
        assertTrue("Kotlin debe indentar el return", formatted.contains("    return price * (1 + tax)"))
    }

    @Test
    fun testFormatJsonCode() {
        val unformattedJson = """{"name":"Black Cat","version":"1.0.0","active":true}"""
        val formatted = SmartCodeFormatter.format(unformattedJson, LanguageType.OTHER)

        assertTrue("El JSON debe contener saltos de línea e indentación", formatted.contains("\n  \"name\": \"Black Cat\","))
        assertTrue("El JSON debe formatear la última propiedad", formatted.contains("\"active\": true"))
    }

    @Test
    fun testFormatXmlCode() {
        val unformattedXml = """
<manifest>
<application>
<activity android:name=".MainActivity" />
</application>
</manifest>
        """.trimIndent()

        val formatted = SmartCodeFormatter.format(unformattedXml, LanguageType.XML)

        assertTrue("Debe indentar <application>", formatted.contains("  <application>"))
        assertTrue("Debe indentar <activity>", formatted.contains("    <activity android:name=\".MainActivity\" />"))
    }
}
