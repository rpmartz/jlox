
## Chapter 4: Scanning

* a _token_ is the smallest sequence of characters that still represent something
* these are called *lexemes*. In the example code `var langauge = "lox";`, the lexemes are `var`, `language`, `=`, etc.
* when handling ambiguous tokens, match the longest substring possible. This is called _maximal munch_

## Chapter 5

* a grammar is a set of rules that contains a potentially infinite amount of valid strings
* If you start with the rules and generate the strings, the generated strings are called **derivations**
* the rules are called **productions** because they produce strings in the grammar
* each production has a **head** (its name) and a **body** which describes what it generates
* in its pure form a body is just a list of symbols, which come in two potential forms:
    + a **terminal** is a letter from the grammar's alphabet, can be though of as a literal value
    + a **nonterminal** is a named reference to another rule in the grammar
    
* it's permissible to have multiple rules with the same name, and when you reach a nonterminal with that name, you can pick any rules for it

* Now that we have classes to represent the expressions that will make up our AST, we need to implement code to handle different kinds of expressions. We could include this as methods on the `Expr` class and do `instanceof` classes, but this mixes concerns with classes that will be shared with both the parser and the interpereter and will be kind of slow in some cases based on the order of the `instanceof` checks

* This problem is actually called the _Expression problem_ in languages and different language paradigms handle it in different ways - object oriented langauges like java group operations on similar types together (in classes) and its very easy to add new types but adding new behavior requires modifying all the existing types (/classes). Functional languages make it easy to add new operations (functions) but it's more difficult to add new types because you have to update everywhere that you do pattern matching. Neither style makes it easy to add both types and operations and this is the expression problem 
    + we are going to get around this by using the Visitor pattern, which is a way to approximate the functional style within an OOP language -> we define all the behavior of a new operation on a set of types in one place, without having to touch the types themselves
    
### Visitor Pattern

* Here's an example applied to breakfast pastries

```java
abstract class Pastry { }

class Beignet extends Pastry {}

class Cruller extends Pastry {}
```

* Now let's say we want to define new operations for each of them (cooking, decorating, eating, etc) **without having to add a new method to each class every time**. We'll add an interface:

```java
interface PastryVisitor {
    void visitBeignet(Beignet beginet);
    void visitCruller(Cruller cruller);
}
```

Now, we add a method to the `Pastry` class so that we can route actions to the correct method on the visitory based on the concrete type of the `Pastry`:

```java
abstract class Pastry {
    abstract void accept(PastryVisitor visitor);
}
```

And then imlement it on all subclasses:

```java
class Beignet extends Pastry {
    
    @Override
    void accept(PastryVisitor visitor) {
        visitor.visitBeignet(this);
    }
}
```

In this way, to perform an operation on a pastry, we call its `accept()` method and pass in the visitor for the operation we want to execute, and the pasty, the concrete implementation of it, passes itself to the the visitor.

* ***Using this trick, you can add one `accept()` method to each class and reuse it for as many visitors as you need to without ever having to touch the pastry classes again***
* in the above example, the `visit` and `accept` methods did not return anything but in our classes we'll use generics to provide the flexibility needed to return data

## Chapter 6

* in one sense, parsing is like the inverse of using rules to create productions; given a production, what rules generated it?
* in parsing, ambiguity introduces the possibility that the parser may misunderstand the user's code

* Lox's initial grammar needs some refinement; the expression `6 / 3 - 1` could be parsed as `(6 / 3) - 1` or `6 / (3/1)`
    + i.e `expression operator number` -> `binary operator number` or `number operator expression` -> `number operator binary`
* when specifying a grammar, precedence and associativity rules can be used to help avoid ambiguity when parsing
     + _precedence_ determines which operator is evaluated first in an expression containing a mix of operators
     + _associativity_ determines which operator is evaluated first
     
* if an operator is *left associative*, like `-`, the operators on the left evaluate before operators on the right, e.g. `5 - 3 - 1` = `(5-3) - 1`
* *right-associative* operators like assignment: `a = b = c` is equivalent to `a = (b = c)`
* Lox uses the same precedence as C:
    + unary (`!` and `-`) (right associative) 
    + multiplication (`*` and `/`)
    + addition (`-` and `+`)
    + comparison (`>`, `>-`, `<`, `<=`)
    + equality (`==` and `!=`)
    
* given those preference rules, the rule `binary -> expression operator expression` is problematic because we can pick any kind of expression as an operand, regardless of which operator we picked, but we cannot have `+` as an operand of a `*` expression because the `+` has lower precedence
    + it's more accurate to say `multiplication -> unary ("*" | "/") unary` but that breaks associativity and does not allow chained multiplication
    + `multiplication -> multiplication ("*" | "/" ) unary` is correct, noting that the grouping on the left makes it left-associative, but when you have the first nonterminal in the body of the rule the same as the head of a rele, your proudction is **left-recursive**, which some parsing techniques have trouble with
    + `multiplication -> unary ( ("/" | "*") unary )*` sidesteps left recursion by saying a multiplication expression is a flat sequence of multiplications or divisions
    
### New Grammar

```
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "false" | "true" | "nil"
               | "(" expression ")" ;
```

* the Lox parser will use `recursive descent` which is considered a `top-down parser` because it walks "down" the grammar starting at the outermost grammar rule (`expresion`) and works its way down the nested subexpressions before reaching the leaves of the syntax tree
    + by comparison, a bottom-up parser like LR start with primary expression and compose them into larger and larger chunks of syntax
* a recursive descent parser is a literal translation of the grammar's rules straight into imperative code

* While not strictly needed, error reporting is part of the parser's user interface and errors will be encountered all the time (i.e. typing in an IDE)
* Once you do have an error, you need to jump back out to the next "valid" chunk to continue parsing, which is called *synchronization*
* traditionally, we synchronize in between statements but we don't have them at this point

## Chapter 7

* Thinking about what an interpreter does, given some value we need to :

    + determine its type (at runtime since Lox is dynamically typed)
    + tell if the object is truthy or not (if it is in an `if` condition)
    + tell if two objects are equal
    
* if we can determine an object's type, we can implement thee as methods that check the type and do the right thing for each type of value

### 7.2 Evaluating Expressions

* We need blobs of code to implement the evaluation logic for each kind of expression we parse. We could put them into the syntax tree classes directly in something like an `interpret()` method, **this is GoF's _Intepreter_ design pattern** but this gets messy by jamming different logic into the tree classes

* _literals_ are something in the user's source code (as opposed to some kind of computed value that don't exist in the user's source code)

## Chapter 8 

```
program   → statement* EOF ;

statement → exprStmt
          | printStmt ;

exprStmt  → expression ";" ;
printStmt → "print" expression ";" ;
```

* There is no place in the grammar where both an expression and a statement is allowed.

* once we add variables, the grammar looks like this:

```
program     → declaration* EOF ;

declaration → varDecl
            | statement ;

statement   → exprStmt
            | printStmt ;
```

where the rule for defining a variable looks like:

```
varDecl -> "var" IDENTIFIER ( "=" expression )? ";"
```

accessing a variable, we define a new kind of primary expression:

```
primary → "true" | "false" | "nil"
        | NUMBER | STRING
        | "(" expression ")"
        | IDENTIFIER ;
```

### 8.3 Environments

* an _environment_ is the name for the area with the association of names to values, and is named such since that's what the Lispers called it.
* This is implemented as a hash table for lookups
 
### 8.4 Assignment
 
 * In Lox and in most C style languages, assignment is an expression and not a statement
 * As in C, it is the lowest precedence expression form, so it slots between expression and equality, the next lowest precedence operator: 
 
 ```
expression → assignment ;
assignment → IDENTIFIER "=" assignment
           | equality ;
```

### 8.5 Scope

* a **scope** is a region where a name maps to a certain entity
* **lexical scope**, sometimes called **static scope**, is a specific style of scope where the
text of the program itself shows the scope of the variable (e.g. within a block)
* **dynamic scope** is where you don't know what the variable is until you execute the code, e.g.:

```
fun playIt(thing) {
    thing.play();
}
```

it's not clear what `thing` is just by reading the code.

* In order to give a block access to variables in "higher"/"outer" scopes, each environment has a reference to its parent environment

#### 8.5.2 Block Syntax and semantics

Now that we have nested environments, the grammar expands like so:

```
statement → exprStmt
          | printStmt
          | block ;

block     → "{" declaration* "}" ;
```

## Questions

* How does a multi-file interpeter work/parse the programs?
* can lox set a variable to a block?