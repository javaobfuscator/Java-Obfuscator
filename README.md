HardObfuscator

Enterprise-grade Java bytecode obfuscator with GUI and CLI



Features • Quick Start • Configuration • Transformers • Docs • Contributing



HardObfuscator protects Java applications by transforming bytecode inside JAR files. It ships with a modern Swing GUI (FlatLaf dark theme), a CLI for automation, JSON-based configuration, package-scoped obfuscation, and a plugin API for custom transformers.

Maintained by @javaobfuscator.

Features







Category



Capabilities





Rename



Class, method, field, and package renaming with reference rewriting





String protection



Runtime string encryption with configurable cipher modes





Control flow



Opaque predicates, bogus jumps, dead code insertion





Constants



Number hiding and constant mutation





Metadata



Debug info removal, annotation manipulation





Resources



Optional resource encryption





Bytecode



Stack and instruction mutation





Scope



Obfuscate only classes inside a target package tree





Runtime



Auto-injected decryption and integrity helpers





Tooling



GUI dashboard, CLI, transformer profiler, parallel pipeline

Quick Start

Requirements





JDK 17 or newer



Maven 3.9+ (or build from your IDE)

Build

git clone https://github.com/javaobfuscator/hardobfuscator.git
cd hardobfuscator
mvn package -DskipTests

Output: target/hardobfuscator.jar

Download a pre-built JAR from the latest release.

GUI

java -jar target/hardobfuscator.jar





Select input JAR and output path



(Optional) set Target Package to limit obfuscation scope



Toggle transformers on the Transformers tab



Click Build

CLI

java -jar target/hardobfuscator.jar \
  --config examples/obfuscation.json

Or with inline paths:

java -cp target/hardobfuscator.jar dev.hardobfuscator.cli.HardObfuscatorCli \
  --input app.jar --output app-obf.jar

Verify output

java -jar app-obf.jar

Configuration

Configuration is JSON. See [examples/obfuscation.json](examples/obfuscation.json) for a full template.

{
  "input": "app.jar",
  "output": "app-obf.jar",
  "targetPackage": "com.myapp",
  "transformers": {
    "classRename": true,
    "methodRename": true,
    "fieldRename": true,
    "stringEncryption": true,
    "controlFlow": true
  },
  "exclusions": {
    "classes": ["com.example.Main"],
    "packages": []
  },
  "runtime": {
    "injectRuntime": true,
    "encryptionMode": "XOR_ROTATE"
  }
}







Field



Description





targetPackage



Only classes under this package are obfuscated; dependencies are preserved





exclusions.classes



Fully qualified class names to skip





runtime.injectRuntime



Embed decryption/runtime helpers into the output JAR





advanced.threads



Parallel transformer worker count

Full reference: docs/CONFIG.md

Transformers







Key



Transformer





classRename



Rename classes and update all references





methodRename



Rename methods (declarations + invoke sites)





fieldRename



Rename fields (declarations + get/put sites)





packageRename



Flatten or relocate packages





stringEncryption



Encrypt string literals at compile time





controlFlow



Control-flow flattening and opaque branches





bogusJumps



Insert unreachable jump blocks





deadCode



Insert dead bytecode sequences





numberHiding



Hide numeric constants





constantMutation



Mutate constant pool entries





debugRemoval



Strip line numbers and local variable tables





annotationManipulation



Transform or strip annotations





resourceEncryption



Encrypt non-class JAR entries





stackMutation



Insert stack noise instructions





instructionMutation



Low-level instruction substitution

Details: docs/TRANSFORMERS.md

Project structure

src/main/java/dev/hardobfuscator/
├── cli/            Command-line interface
├── gui/            Swing GUI (FlatLaf)
├── core/           Engine, pipeline, I/O, events
├── config/         JSON config loader & validator
├── plugins/        Transformer plugin API
├── transformers/   Built-in obfuscation passes
├── runtime/        String/resource decryption at runtime
└── examples/       Sample plugin

Plugins

Implement dev.hardobfuscator.plugins.Transformer and register via the plugin loader. See [ExamplePlugin.java](src/main/java/dev/hardobfuscator/examples/plugin/ExamplePlugin.java).

Docs





Configuration reference



Transformer guide



Contributing



Changelog



Security policy

Contributing

Contributions are welcome. Please read CONTRIBUTING.md before opening a pull request.

License

This project is licensed under the MIT License.



Built with ASM · FlatLaf · Jackson · @javaobfuscator
