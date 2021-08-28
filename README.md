# Mutation-Guided Fuzzing

Documentation for running and understanding the implementation of mutation-guided fuzzing provided here.

<!-- For project writeup, see [this document](https://saphirasnow.github.io/17-355/Bella_Laybourn_17355_Project.pdf). -->

For more questions, feel free to email [Bella Laybourn](mailto:ilaybour@andrew.cmu.edu), [Rafaello Sanna](mailto:rsanna@u.rochester.edu), or [Rohan Padhye](https://rohan.padhye.org).

## Build + Test + Install

This repository works together with the [`mu2` branch of JQF](https://github.com/rohanpadhye/JQF/tree/mu2) and the [`sort-benchmarks`](https://github.com/cmu-pasta/sort-benchmarks). The current dependency structure is as follows:

```
jqf-fuzz --> jqf-instrument
sort-benchmarks --> jqf-fuzz (for API only)
mu2 --> jqf-fuzz
mu2 --> sort-benchmarks (for testing only)
jqf-maven-plugin --> jqf-fuzz
jqf-maven-plugin --> mu2 (to be able to run MutationGuidance)
```

Due to build inter-dependencies between Mu2 and JQF, the current process to bootstrap the build for the first time is as follows.

### Step 1: Install JQF from `mu2` branch but disable the `maven-plugin`

```
git clone https://github.com/rohanpadhye/JQF --branch mu2 && cd JQF
mvn install -pl '!maven-plugin'
cd ..
```

This installs `jqf-fuzz` and `jqf-instrument`, but does not compile the maven plugin, which itself depends on `mu2`. 

### Step 2 (optional): Install `sort-benchmarks` for testing

```
git clone https://github.com/cmu-pasta/sort-benchmarks && cd sort-benchmarks
mvn install
cd ..
```

### Step 3: Install `mu2` with full integration tests

Install mu2 with:
```
git clone https://github.com/cmu-pasta/mu2 && cd mu2
mvn install
cd ..
```

This step depends on step 1 (because `mu2` depends on `jqf-fuzz` to compile) as well as step 2 (because the integration tests depend on `sort-benchmarks`). If you don't install the `sort-benchmarks`, you can install `mu2` via `mvn install -DskipTests`. This is not recommended.

### Step 4: Install JQF with `maven-plugin`

Now mu2 can be used as a dependency of JQF (see next section).
```
cd JQF
mvn install -pl 'maven-plugin'
cd ..
```

This step builds the `maven-plugin` from JQF's `mu2` branch, which allows you to run `mvn jqf:fuzz -Dengine=mutation`.

(TODO: Improve the build process to untangle the bootsrap dependencies)

### Development

When making changes to mu2 or JQF, you only need to install that particular project's SNAPSHOT for the changes to take effect. You do not have to do the long bootstrap build as above during regular development.

To test changes to `mu2`, run the integration tests as follows from the `mu2` repository:
```
mvn verify
```

Coverage reports should be available in `target/site/jacoco-integration-test-coverage-report/index.html`.

## External Usage

Once everything is installed, you can use JQF/mu2 to fuzz target applications as follows.

### Mutation-Guided Fuzzing

Runs like Zest, just add flag `-Dengine=mutation` on `jqf:fuzz` terminal commands. You can also use the flag `-DrandomSeed` to set the fuzzing seed to an explicit value and `-Dtrials` to set a limit on the number of trials to be run. Adding these two together gives you an entirely deterministic run (assuming the test you're running is deterministic).

Example: 

```sh
mvn jqf:fuzz -Dclass=package.class -Dmethod=method -Dengine=mutation -Dtrials=1000 -Dincludes=prefix
```

### Mutate Goal (aka Mu2 Repro)

For reproducing results from mutation-guided fuzzing. Run using `jqf:mutate` with the `-Dclass` and `-Dmethod` flags. 
You can set an explicit corpus by providing the `-Dcorpus` flag.

Example: 

```sh
mvn jqf:mutate -Dclass=package.class -Dmethod=method -Dincludes=prefix -Dinput=/path/to/corpus
```

### Selecting Instrumentable Classes

You can use the `-Dincludes` flags to select which code will be mutated. 

```sh
mvn jqf:mutate -Dclass=package.class -Dmethod=method -Dincludes=prefix1,prefix2
```

## Implementation

### MutationInstance

A `MutationInstance` is an instance of a mutation: e.g. the class in which the mutation resides, the kind of mutation performed (the `Mutator` - see below), and the location in which the mutation takes places (given as the n'th opportunity for the `Mutator` to be applied to the given class). Each instance also has an ID, which can be used as its index in the `MutationInstance.mutationInstances` array.

### ClassLoaders

#### MutationClassLoader

A `ClassLoader` which loads a mutated version of a class specified by a `MutationInstance`. For all classes except that specified by the `MutationInstance`, it loads it normally. For the class specified, it mutates the class as specified by the `Mutator` and the mutator index.

This class can be used on its own, or can be used through a `MutationClassLoaders` object, which takes the class-loading information as a parameter then provides a map from `MutationInstances` to `MutationClassLoader`s.

#### CartographyClassLoader

Initial class loading should take place using this ClassLoader. It creates a list of MutationInstances representing all of the mutation opportunities (the "cartograph"). It does this by instrumenting the input class using the `Cartographer`: a `SafeClassWriter` which both logs which mutants are possible, and transforms the input class into one which measures which mutants are run for later optimization.

#### MutationClassLoaders

A class that contains references to both the `CartographyClassLoader` and a mapping of each `MutationInstance` to its `MutationClassLoader`. A `MutationClassLoaders` object is the external entry point, and such an object is passed to a `MutationGuidance` during construction.

### Mutators

#### Mutator Format

Each mutator replaces one instruction with another, or with a series of instructions. 
They sometimes take the form `prefix_oldInstruction_TO_newInstruction(Opcodes.oldInstruction, returnType, [list, of, instruction, calls, ...])` and other times, as appropriate to the type of mutator, are just descriptors.

To make that more clear, three actual examples, broken down:

`I_ADD_TO_SUB(Opcodes.IADD, null, new InstructionCall(Opcodes.ISUB))`
refers to swapping <b>I</b>nteger <b>ADD</b>ition for <b>SUB</b>traction, which requires no relevant return type.

`VOID_REMOVE_STATIC(Opcodes.INVOKESTATIC, "V", new InstructionCall(Opcodes.NOP))`
refers to removing calls to void functions made with `invokestatic`, which requires that the return type of the called function be <b>V</b>oid.

`S_IRETURN_TO_0(Opcodes.IRETURN, "S", new InstructionCall(Opcodes.POP), new InstructionCall(Opcodes.ICONST_0), new InstructionCall(Opcodes.IRETURN))`
refers to making a function with return type <b>S</b>hort return the short `0` instead of what it would normally have returned. This requires that the return type of the returning function be <b>S</b>hort.

Note how in the second two examples, the opcode alone was not enough to identify the relevant return type, which is why that information was included separately.

##### Pops: An Aside

When calling functions, the arguments are loaded onto the stack. This means that, when removing a function call, those arguments must be popped off the stack before continuing. 
Typically, we would pop off the same number of arguments as the function has, but when the opcode is `invokestatic` there's one fewer because there's an implicit `this` argument added in other cases that `invokestatic` doesn't need.

#### InstructionCall

Just a wrapper for bytecode instructions so their arguments can be included.

### MutationSnoop

A class whose static methods are invoked from mu2-instrumented classes. Used to track infinite loops in mutated code and to perform speculative mutation analysis in the original (cartography) test run.

### MutationGuidance

For the most part, an extension of `Zest`.
For each fuzz input, runs first the class loaded by the `CartographyClassLoader`, then each of the `MutationInstance`s it has generated that hasn't been killed by a previous fuzz input and have been marked as run by the class loaded by the `CartographyClassLoader`.
Saves for reason `+mutants` along with Zest's `+coverage`, etc.

### Timeout

To prevent hanging forever on an infinite loop, both MutationInstances and CartographyClassLoaders outfit their classes with a timeout functionality that essentially kills the program after some maximum number of control jumps.
This assumes that if the number of control jumps exceeds that then the program has encountered an infinite loop and will not ascertain any new information by continuing to run.
