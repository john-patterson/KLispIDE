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

   - The JVM requires an application's entrypoint be defined as a static method named `main` inside of the class which 
   you highlight in the manifest.
   - Kotlin compiles classes you write into classes with the "Kt" suffix. For instance, the class Main in the package
   `com.statelesscoder.klisp` has the class path: `com.statelesscoder.klisp.MainKt`.
   - When defining an application, Gradle expects you to use the [application plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
   which requires you to specify a `mainClassName`.
   - There are ways to get Kotlin code to show up correctly for this entrypoint, but I found it much easier to define
   my application entrypoint as a Java class, then call into my Kotlin code. 
   
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

### UI is a whole thing and is not cross-plat due to JavaFX
At home, I develop on both a Windows desktop and a Mac laptop. Practically, it means I have one of the worst experiences
of everything that I try. Things that I can get just working on one, break down on the other. For this project, the main
stumbling block as the UI component.

In the beginning&mdash;as the project name suggests&mdash;I wanted to create a simple
IDE for this language as well. A quick search found TornadoFX as the go-to framework for Kotlin. After my struggle with
Gradle dependencies described above, I got a simple little Hello World UI popping up. You can see this in 
[my initial commit](https://github.com/john-patterson/KLispIDE/commit/6ee29f19540138d2fd4374fe8cdc9ef7358e6c7a).

Not only did this quickly fall out of scope as I'd given myself a million things to learn, but when I pulled the code on 
my Mac, everything broke. From what I can gather, my Mac was using a version of the JDK which did not have JavaFX, 
the core Java UI framework on which TornadoFX was written, bundled with it.

There were ways out of this, but I wasn't sure if fixing the problem on my Mac side would break the Windows side.
Given that it seems the UI layer is quite a bit to learn if you are not used to it, the JDK incompatibilities between my 
environments, and all the other issues I was learning about, I decided to tackle learning UI as a stretch-goal.

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