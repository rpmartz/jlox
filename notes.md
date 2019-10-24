
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

* when specifying a grammar, precedence and associativity rules can be used to help avoid ambiguity when parsing