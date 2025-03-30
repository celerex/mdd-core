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

## Combining

You can combine data types:

```mdd
- import: http://example.com/test
- import:
	* url: http://example.com/otherTest
	* alias: renameTo
- import:
	* uri: http://example.com/first
	- alias:
		* name: test1
		* as: test2
```

This results in:

```json
{
	"import": [
		"http://example.com/test",
		{
			"url": "http://example.com/otherTest", 
			"alias": "renameTo"
		},
		{
			"uri": "http://example.com/first", 
			"alias": [
				{
					"name": "test1", 
					"as": "test2"
				}
			]
		}
	]
}
```

## Similar collections

Consider this collection:

```mdd
* enumeration:
	- true
	- false
```

This results in:

```json
{
	"enumeration": [ "true", "false" ]
}
```

This collection is actually the same thing:

```mdd
- enumeration: true
- enumeration: false
```

This also resolves to:

```json
{
	"enumeration": [ "true", "false" ]
}
```

## Metadata

Metadata is by default not supported, you need to explicitly configure a metadata provider in order to use it. A default STL one is provided though.

Even though in markdown the "+" denotes a list, it is not processed as a list in MDD, this means you can not do this:

```mdd
+ import: http://example.com
+ import: http://example2.com
```

This will result in a single key with the second value (as it overwrites the first).

```json
{
	"$meta": {
		"import": "http://example2.com"
	}
}
```

You can make it a list using this syntax though:

```mdd
+ import:
	- http\://example.com
	- http\://example2.com
```

Note that we escape the urls to prevent them from being picked up as keys. This results in:

```json
{
	"$meta":
	{
		"import": [
			"http://example.com", 
			"http://example2.com"
		]
	}
}
```
