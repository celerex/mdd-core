# Markdown Data

A markdown compatible file format for data serialization and exchange.

## Why

Markdown has long since won the war for the wiki formats and is currently the markup language of choice when communicating with LLM's.

I was looking for a data serialization format that focuses on **simplicity** and **readability** that I could integrate in my LLM-based AI tool as a structural data format that is both useful and readable.

Enter MDD (**m**ark**d**own **d**ata): the human readable format that is also syntactically correct markdown.

# Syntax

## Scalar values

Scalar values are (at the core) all strings. It is possible to automatically parse the values based on their contents but this can of course lead to false positives.
By default the parser will return every scalar as a string, given a schema or rules, automatic parsing can be applied.

## Multiline

Multiline values are natively supported: any content that is at the same level (or deeper) from the previous content and does not start a new structural element. This represents a single string:

```mdd
multiline
value
```

## Objects

Complex objects have fields, these fields are indicated as a markdown list starting with an asterisk.

```mdd
myObject:
* myField: myValue
```

## Collections

Collections have entries, each entry is indicated as markdown list starting with a dash.

```mdd
- entry1
- entry2
```

## Depth

Depth is calculated based on leading whitespace and syntactical elements (if any). For example this list is defined at depth 2: 1 depth from a leading tab, 1 depth from the actual list sign:

```mdd
	- test1
	- test2
```

The collection here can be represented in json as:

```json
[["test1", "test2"]]
```

More complex examples can be found in the [unit tests](https://github.com/celerex/mdd-core/tree/master/src/test/resources/examples).

# Java

This package contains a parser and formatter written in java. Parsing a markdown file can be as simple as:

```java
String markdown = ...;
Object parsed = new MDDParser().parse(markdown);
```

Formatting can be done with:

```java
Object object = ...;
String markdown = new MDDFormatter().format(object);
```
