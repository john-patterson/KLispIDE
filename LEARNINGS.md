# Learnings
Due to my own lack of experience with the JVM-ecosystem, the most time-consuming part of this project was learning how 
to structure my project within IntelliJ IDEA, learning how to use Gradle, and learning all the libraries I needed to make
this possible. Here were my struggles and what I learned to get around them.

I am writing this mid-October 2020. Here are the versions of everything I am using so that the user can judge the pertinence
of this information to their own situation:
* IntelliJ IDEA, version 2020.2.3
* Gradle, version 6.3
* Kotlin / JVM, version 1.3.72
* JVM, version 1.8
* TornadoFX, version 1.7.20
* JUnit, version 5.3.1
* JDK, version 14.0.2

### Setting up the main method
  > Error: Main method not found in class org.sc.koro.Main, please define the main method as:
     public static void main(String[] args)

   - Kotlin compiles classes you write into classes with the "Kt" suffix. For instance, the class Main in the package
   `com.statelesscoder.klisp` has the class path: `com.statelesscoder.klisp.MainKt`.
   - When defining an application, Gradle expects you to use the [application plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
   which requires you to specify a `mainClassName`.
   - While I spent my time wondering what I was doing wrong, I took advantage of Kotlin's ability to work with Java and
   I created a standard Java entrypoint while I went on. Since there is no renaming and there are many Java hello world
   samples in the world, this proved faster to get going for the novice.
   - This stemmed from my newcomer's eyes on the type I was using. 
   
     This signature:
     
     `fun main(args: List<string>)`
     
     Is not the same as this one:
     
     `fun main(args: Array<string>)`
     
     Seems simple in hindsight, huh?
     
   
### Adding dependencies with Gradle
When I first tried to add a dependency, TornadoFX, I could not for the life of me get it to resolve in my Kotlin file.
After a _significant_ amount of time toying around with it, the Internet pointed me to IDEA's `Invalidate Caches / Restart`
feature. It turns out my build was fine with the dependency, my IDE was not.

### Unit Testing
This wasn't so much a challenge as the other two, but I did do research and spend time figuring it out, so it is worth
writing down. First I found [this slide deck from JetBrains](https://resources.jetbrains.com/storage/products/kotlinconf2018/slides/4_Best%20Practices%20for%20Unit%20Testing%20in%20Kotlin.pdf)
, creators of Kotlin, on writing unit tests in Kotlin. This helped me get started quicker.

The gist of it is that your `build.gradle` file needs this stuff:


```groovy
 test {
      useJUnitPlatform()
  }
  
  dependencies {
      // ...
      testImplementation 'org.junit.jupiter:junit-jupiter-api:5.3.1'
      testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.3.1'
  }
```

Then you can write a test like so:

```kotlin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenizerTests {
    @Nested
    inner class ListTests {
        @Test
        fun `empty list`() {
            val result = Tokenizer().scan("()")
            val (leftParen, rightParen) = result
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(Token("(", TokenType.LEFT_PARENS), leftParen)
            Assertions.assertEquals(Token(")", TokenType.RIGHT_PARENS), rightParen)
        }
    }
}
```

I learned this `@Nested` class trick from the slide deck. It cleaned up my test-suite in the test runner. I also
enjoyed the back-tick syntax for identifiers in Kotlin. One of my favorite languages, F#, has a similar feature that I use for my
test suites. It really is much more readable than the standard C#/Java-ism of `Method_Conditions_Results` test names.

### Multi-projects in Gradle and IDEA
For my project I knew I had at least three, maybe four, compiled artifacts I wanted to produce: a library with my
compiler code, a server for the language service, a desktop-app IDE, and perhaps a test runner service.

The structure I decided to go with was having one IDEA project and multiple modules. I quickly learned that Gradle does
not support compiling multiple artifacts from the same `build.gradle` file, or at least that it did not do so in a manner
that I could quickly use without throwing learning Groovy on my pile. My solution, then, was learning how to setup multiple
modules in IDEA.

I learned this is the structure of a module if you don't want to go against the grain:
```
module_name
| build
| src
    | main
         | java
             | Main.java
             | Foo.java
         | kotlin
             | Bar.kt
    | test 
         | java
             | FooTests.java
         | kotlin
             | BarTests.kt
    | build.gradle
```

wherein `main` and `test` are actually submodules and `build` is ignored.

You may setup or alter this relationship with the 
Modules tab of the Project Structure editor in IDEA (that's `File > Project Structure > Modules` or `Cmd;` on Mac), but
you can also just right click the project in the Project pane to the left and choose `New > Module`, pick `Gradle` on the
left, then `Kotlin/JVM`. This will create an empty module with a `build.gradle` file setup. To add the correct 
structure, you should right click the module in the Project pane and choose `New > Directory`. This will popup the 
"Gradle Source Sets" menu, where you can select the node of the above structure that you want to add.

Each module that you add should be included in the root project's `settings.gradle` files like so:

```
include 'server'
include 'editor'
```

Usually IDEA does this for you, though.
