# Markdown Data

A markdown compatible file format for data serialization and exchange.

## Why

Markdown has long since won the war for the wiki formats and is currently the markup language of choice when communicating with LLM's.

I was looking for a data serialization format that focuses on **simplicity** and **readability** that I could integrate in my LLM-based AI tool as a structural data formats that bridges the gap between being useful but also readable.

Enter MDD (**m**ark**d**own **d**ata): the human readable format that is also syntactically correct markdown.

# Syntax

## Scalar values

Scalar values are (at the core) all strings. They can off course be parsed further into numbers and other types of scalars. None of those scalar values however are surrounded by quotes.

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
[[test1, test2]]
```

> The following documentation actually doubles as the unit tests and is copied from there.

# Scalar values

## Basic scalars

A scalar value is a singular value, in the simplest case this is a string.

```mdd
this is a scalar value
```

Expected result:

```expected
this is a scalar value
```

The parser will initially parse everything as a string, even if it appears to be a number or a boolean or...

```mdd
42
```

Expected result:

```expected
42
```

## Multilines

Anything that follows a value is considered a multiline if it is at the same level (or deeper) and does not start a new structural block, so this is considered one long scalar value:

```mdd
This is a sentence
with a linefeed in it
another linefeed
     and some leading spaces!
and some trailing content as well
```

Expected result:

```expected
This is a sentence
with a linefeed in it
another linefeed
     and some leading spaces!
and some trailing content as well
```

## Scalar collections

A basic scalar collection:

```mdd
- 1
- 2
- 3
```

Should return a basic collection:

```json
["1", "2", "3"]
```

## Scalars in objects

By prepending a scalar with a key, it becomes a named scalar which needs to reside in an object:

```mdd
value: test
```

```json
{"value": "test"}
```

When we have scalar values in a collection, it will look for an element with the given name in the parent, make sure it is a collection and add it to that.

```mdd
- name: john
- age: 30
```

Expected result:

```json
{
	"name": ["john"], 
	"age": ["30"]
}
```

If you add items in a different order, it will be parsed correctly but the original order will be gone so can not be reconstructed based on the parsed data:

```mdd
- name: john
- age: 30
- name: jim
- age: 40
```

Expected result:

```json
{
	"name": ["john", "jim"], 
	"age": ["30", "40"]
}
```

## Combined example

We can create an object and a list with scalars. Each multiline must have at least the same depth as the object it belongs to:

```mdd
reason:
* comment: this is a comment
	that spans multiple lines!
* something:
	* comment: that has a comment
		that features multiple lines!

list:
- This is the first
	test
- this is the second
		test
		
value: multiline
in a text

value2: multiline
in another text
```

When we parse this, it yields the following (expressed in JSON):

```json
{
	"reason": {
		"comment": "this is a comment\nthat spans multiple lines!", 
		"something": {
			"comment": "that has a comment\nthat features multiple lines!"
		}
	}, 
	"list": [ "This is the first\ntest", "this is the second\n\ttest"], 
	"value": "multiline\nin a text", 
	"value2": "multiline\nin another text"
}
```


# Objects

An object is a complex data type that consists of one or more child values which can in turn also be objects, scalars or collections.

## Simple example

An object on the root can be defined like this:

```mdd
root1:
* test: child of root1

root2:
* test: child of root2
```

This is the same as the following JSON:

```json
{ 
	"root1": {
		"test": "child of root1"
	},
	"root2": {
		"test": "child of root2"
	}
}
```

This is identical to:

```mdd
* root1:
	* test: child of root1

* root2:
	* test: child of root2
```

Expected result:

```json
{ 
	"root1": {
		"test": "child of root1"
	},
	"root2": {
		"test": "child of root2"
	}
}
```

## Combined example

We can combine this with other elements:

```mdd
* myObject:
	* test1: some value
	* list:
		- 1
		- 2
		- 3
	* test2: another value
		but this time with a multiline
		with even more content!
	* test: 
		* value: nested object
```

```json
{
	"myObject": {
		"test1": "some value", 
		"list": ["1", "2", "3"], 
		"test2": "another value\nbut this time with a multiline\nwith even more content!", 
		"test": {
			"value": "nested object"
		}
	}
}
```

## Collection of objects

Suppose you want to generate a collection of objects, you can write this:

```mdd
book:
* author: John
- review:
	* user: Jim
	* score: 5
- review:
	* user: Bob
	* score: 3
* tags:
	- scifi
	- fantasy
* isbn: 1234
```

This yields:

```json
{
	"book": {
		"author": "John", 
		"review": [
			{
				"user": "Jim", 
				"score": "5"
			}, 
			{
				"user": "Bob", 
				"score": "3"
			}
		], 
		"tags": ["scifi", "fantasy"], 
		"isbn": "1234"
	}
}
```

## Unnamed collection

You can create unnamed collections of objects by wrapping a key in italics:

```mdd
- _book_:
	* author: john
	* isbn: 1234
- _book_:
	* author: bob
	* isbn: 2345
```

In this case the actual key name "book" is not part of the data structure, it is merely there for readability. It can obviously not be reconstructed verbatim from the resulting dataset:

```json
[
	{ "author": "john", "isbn": "1234"}, 
	{ "author": "bob", "isbn": "2345"}
]
```

For completeness let's compare it to the named variant:

```mdd
- book:
	* author: john
	* isbn: 1234
- book:
	* author: bob
	* isbn: 2345
```

```json
{ 
	"book": [
		{ "author": "john", "isbn": "1234"}, 
		{ "author": "bob", "isbn": "2345"}
	]
}
```

## Prettification

You can also prettify keys for readability, these prettifications will be ignored when parsing the data:

```mdd
- book:
	* **author**: john
	* **isbn**: 1234
- **book**:
	* author: bob
	* **isbn**: 2345
```

```json
{ 
	"book": [
		{ "author": "john", "isbn": "1234"}, 
		{ "author": "bob", "isbn": "2345"}
	]
}
```


# Nested collections

Nested collections are interesting in certain situations though they rarely appear in enterprise data sets.
Only the absolute depth of the dash matters in determining the collection depth.

To force the system to start a new collection rather than appending to a previous collection, empty dashes can be inserted to reset the level.

## A simple matrix

A basic 2x3 matrix. The empty entry is to reset the array to a certain depth. If you add whitespace to the entry, it will be regarded as an entry of whitespace.

```mdd
-
	- test1
	- test2
	- test3
-
	- test2
	- test3
	- test4
```

```json
[
	["test1", "test2", "test3"], 
	["test2", "test3", "test4"]
]
```

Because only the absolute depth counts, this is equivalent to:

```mdd
	- test1
	- test2
	- test3
-
	- test2
	- test3
	- test4
```

```json
[
	["test1", "test2", "test3"], 
	["test2", "test3", "test4"]
]
```

## Deeper nesting

By simply shifting everything one tab to the right, we get a collection with deeper nesting:

```mdd
	-
		- test1
		- test2
		- test3
	-
		- test2
		- test3
		- test4
```

```json
[[
	["test1", "test2", "test3"], 
	["test2", "test3", "test4"]
]]
```

## Combinations

You can mix and match elements, note that in the example below the second "empty" dash actually has whitespace that is counted as a value.

```mdd
- test-begin
-
	- test1
	- test2
	- test3
-
	- test2
	- test3
	- test4
-    
	- test3
	- test4
- test-end
	- test5
		- test6
			with multiline
	- test7
```

As JSON:

```json
[
	"test-begin", 
	["test1", "test2", "test3"],
	["test2", "test3", "test4"], 
	"   ", 
	["test3", "test4"], 
	"test-end", 
	[
		"test5", 
		["test6\nwith multiline"], 
		"test7"
	]
]
```

## Complex nesting

You can also generate more complex nested collections. As you can see in the example, it is the absolute depth of the list marker that counts, not if all list markers for every depth actually exist.

```mdd
-
	-
		-
			- test1
			- test2
			- test3
	- 
		-
			- test4
			- test5
			- test6
	-
			- test7
			- test8
				and more!
			- test9
	-
				- test10
					- test11
						with additional content
				- test12
					- test13
```

This generates:

```json
[
	[
		[["test1", "test2", "test3"]], 
		[["test4", "test5", "test6"]], 
		[["test7", "test8\nand more!", "test9"]],
		[[["test10", ["test11\nwith additional content"], "test12", ["test13"]]]]
	]
]
```

Because only the absolute depth counts, you can quickly create a deeply nested array:

```mdd
					- test1
					- test2
```

This actually becomes

```json
[[[[[["test1", "test2"]]]]]]
```
