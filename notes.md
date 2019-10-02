
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
