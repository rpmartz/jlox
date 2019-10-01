
## Chapter 4: Scanning

* a _token_ is the smallest sequence of characters that still represent something
* these are called *lexemes*. In the example code `var langauge = "lox";`, the lexemes are `var`, `language`, `=`, etc.
* when handling ambiguous tokens, match the longest substring possible. This is called _maximal munch_