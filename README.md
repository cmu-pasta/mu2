# Mutation-Guided Fuzzing

Mu2 is a fuzz testing platform for mutation-guided fuzzing, built on top of the [JQF](https://github.com/rohanpadhye/JQF/) platform for fuzzing Java programs. Documentation for running and understanding the implementation of mutation-guided fuzzing provided here.

<!-- For project writeup, see [this document](https://saphirasnow.github.io/17-355/Bella_Laybourn_17355_Project.pdf). -->

For more questions, feel free to email [Bella Laybourn](mailto:ilaybour@andrew.cmu.edu), [Vasu Vikram](mailto:vasumv@cmu.edu), or [Rohan Padhye](https://rohan.padhye.org).

## Research Paper - ISSTA 2023

Mu2 is described in the following research paper:

> Vasudev Vikram, Isabella Laybourn, Ao Li, Nicole Nair, Kelton OBrien, Rafaello Sanna, and Rohan Padhye. 2023. **Guiding Greybox Fuzzing with Mutation Testing**. In Proceedings of the 32nd ACM SIGSOFT International Symposium on Software Testing and Analysis (ISSTA 2023). Association for Computing Machinery, New York, NY, USA, 929–941. https://doi.org/10.1145/3597926.3598107

## Build + Test + Install

To install Mu2, run the following:

```
git clone https://github.com/cmu-pasta/mu2 && cd mu2
mvn install
```

### Development

Mu2 is a multimodule project containing the following modules:

1. `core`: all the core instrumentation and guidance logic
2. `examples`: example fuzz drivers for applying mutation-guided fuzzing
3. `integration-tests`: integration tests for mutation-guided fuzzing on sorting targets in `examples`

When making changes to mu2 or JQF, you only need to install that particular project's SNAPSHOT for the changes to take effect.

To test changes to `mu2`, run the integration tests as follows from the `mu2` repository:
```
mvn verify
```

Coverage reports should be available in `integration-tests/target/site/jacoco-integration-test-coverage-report/index.html`.

## Differential Mutation Testing Fuzzing

To use the differential fuzzing goal `mu2:diff`, instrument your tests with `@Diff` and `@Comparison` annotations instead of `@Test` and use the `DiffGoal` and `MutateDiffGoal` as described below.

### Annotations

In the differential testing framework, each test is a pair of functions, one annotated with `@Diff` and the other with `@Comparison`.

The function annotated with `@Diff` should **not** be `static` and should return an object (neither void nor a primitive, though boxed primitives work).
The result of this diff function will be compared with other results using the comparison function (annotated with `@Comparison`). 

The comparison function should be `static`, take two arguments of the same type as the return type of any associated diff functions, and return a `Boolean` 
(`Boolean.TRUE` if the two inputs are equivalent, `Boolean.FALSE` otherwise).

Multiple functions with these annotations can be included in a single test class, so, to uniquely match the diff function with a comparison function, 
pass the name of the comparison function as an argument to `@Diff` as `cmp` (see below for example). If no argument is passed to `cmp`, `Objects.equals()` 
will be used as a default.

Each diff function is associated with up to one comparison function, but arbitrarily many diff functions may use a comparison function.

Example of a differential test for a function `populateList(List<Integer>)` when the comparison only cares about the size of the populated list:

```java
@Diff(cmp = "compare")
public List<Integer> testPopulate(List<Integer> input) {
    return populateList(input);
}

@Comparison
public static Boolean compare(List<Integer> list1, List<Integer> list2) {
    return list1.size() == list2.size();
}
```

### Diff Goal

For running differential fuzzing. Currently defaults to mutation-guided fuzzing.

Example: 

```sh
mvn mu2:diff -Dclass=package.class -Dmethod=method -Dincludes=prefix
```

### Mutate Goal

For repro on mutation guidance with diff-based tests.

Example: 

```sh
mvn mu2:mutate -Dclass=package.class -Dmethod=method -Dincludes=prefix -Dinput=path/to/corpus
```

### Repro Goal

For repro on other guidances with diff-based tests.

Example: 

```sh
mvn mu2:repro -Dclass=package.class -Dmethod=method -Dincludes=prefix -Dinput=path/to/corpus
```

### Selecting Instrumentable Classes

You can use the `-Dincludes` flags to select which code will be mutated. 

```sh
mvn mu2:diff -Dclass=package.class -Dmethod=method -Dincludes=prefix1,prefix2
```

Additionally, setting the `-DtargetIncludes` flag to a comma-separated list that includes every mutated class 
as well as all classes that depend on a mutable class anywhere in their dependency trees improves efficiency by 
loading classes not in the list with a separate intermediate ClassLoader.

Example using a test of Scala's chess library:
```sh
mvn mu2:diff -Dclass=edu.berkeley.cs.jqf.examples.chess.FENTest -Dmethod=testWithGenerator -Dincludes=chess.format.Forsyth,chess.Situation -DtargetIncludes=edu.berkeley.cs.jqf.examples.chess,chess
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

## Scripts

### Setup

```
pip install -r requirements.txt
```

### Plot Mutants

This is a script for visualizing the various mutant numbers across trials of a given fuzzing session run with `-Dengine=mutation`. It takes in an input `plot_data` CSV file and 
outputs an image plotting found mutants, killed mutants, and optionally seen/infected mutants.
Usage:
```
python scripts/plot_mutant_data.py <plot_data_file> <output_image_file> 
```

### Experiments and Venn Diagram
Usage:
```
./getMutants.sh <testClass> <testMethod> <includesClass> <experiments> <trials> <baseDirectory>

./getMutants.sh edu.berkeley.cs.jqf.examples.commons.PatriciaTrieTest testPrefixMap org.apache.commons.collections4.trie 3 1000 ../../jqf/examples
```
Venn diagram script alone:
```
python scripts/venn.py --filters_dir <filters_dir> --num_experiments <num_experiments> --output_img <output_img_name>
```

## Acknowledgments

This project is supported in part by the National Science Foundation under Grant Number [CCF-2120955](https://www.nsf.gov/awardsearch/showAward?AWD_ID=2120955). Any opinions, findings, and conclusions or recommendations expressed in this material are those of the author(s) and do not necessarily reflect the views of the National Science Foundation. 

This project is also partly supported by [CyLab](https://www.cylab.cmu.edu/).
