// Sample Kotlin file for testing compilation
// This file can be used to test the ADB Compiler Integration

fun main() {
    println("🎉 Hello from Kotlin Text Editor!")
    println("This code was compiled using the desktop bridge service.")
    
    // Test basic Kotlin features
    val editor = TextEditor("sample.kt")
    editor.displayInfo()
    
    // Test collections
    val languages = listOf("Kotlin", "Java", "Python", "JavaScript")
    println("\nSupported languages:")
    languages.forEach { lang ->
        println("  • $lang")
    }
    
    // Test lambda expressions
    val kotlinFiles = languages.filter { it.startsWith("K") }
    println("\nKotlin-related: $kotlinFiles")
}

class TextEditor(private val fileName: String) {
    
    fun displayInfo() {
        println("\n📝 Text Editor Information:")
        println("   File: $fileName")
        println("   Features: Syntax highlighting, ADB compilation")
        println("   Platform: Android with Desktop Integration")
    }
    
    companion object {
        const val VERSION = "1.0.0"
    }
}

